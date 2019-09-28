package com.fenquen.rdelay.client.receiver;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.exception.BusinessProcessException;
import com.fenquen.rdelay.exception.HttpRespReadException;
import com.fenquen.rdelay.model.resp.ExecutionResp;
import com.fenquen.rdelay.model.resp.ReceiveResp;
import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.model.task.TaskType;
import com.fenquen.rdelay.utils.HttpUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class Receiver implements ApplicationContextAware, InitializingBean, DisposableBean {
    private ThreadPoolExecutor threadPoolExecutor;
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private StrContentTaskConsumer strContentTaskConsumer;

    private String submitUrl = "http://127.0.0.1:8086/receiveExecResp";

    private ConcurrentHashMap<String, TaskBase> taskid_taskBase = new ConcurrentHashMap<>();

    private static final Logger LOGGER = Logger.getLogger(Receiver.class.getName());


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LOGGER.info("setApplicationContext");
        this.applicationContext = applicationContext;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        threadPoolExecutor = new ThreadPoolExecutor(6, 16,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("RDEALY_TASK_EXEC_THREAD_POOL" + UUID.randomUUID());
                    return thread;
                });

        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream(""));
            submitUrl = properties.getProperty("submit.url");
        } catch (Exception e) {
            // noop
        }

    }


    @Override
    public void destroy() throws Exception {
        threadPoolExecutor.shutdown();
        HttpUtils.destroy();
    }

    @RequestMapping(value = "/rdelay/receiveTask/{taskTypeStr}", method = RequestMethod.POST)
    public ReceiveResp receiveTask(@PathVariable String taskTypeStr,
                                   @RequestBody String taskJsonStr) {

        TaskType taskType = TaskType.valueOf(taskTypeStr);
        TaskBase task = JSON.parseObject(taskJsonStr, taskType.clazz);

        ReceiveResp receiveResp = new ReceiveResp(task);

        try {
            threadPoolExecutor.submit(() -> {
                ExecutionResp executionResp = new ExecutionResp(task);
                try {
                    processTask(task);
                    executionResp.success();
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, e.getMessage(), e);
                    executionResp.fail(e);
                }

                // the goal is solely to submit the exec resp to rdelay server,the resp status code is enough
                try {
                    HttpUtils.postStringContentSync(submitUrl, JSON.toJSONString(executionResp));
                } catch (IOException e) { // how to compensate
                    LOGGER.log(Level.INFO, e.getMessage(), e);
                } catch (BusinessProcessException e) { // http status not 200,how to compensate
                    LOGGER.log(Level.INFO, e.getMessage(), e);
                } catch (HttpRespReadException e) {
                    // rdelay always response success when status code is 200 because the goal is just to submit
                    LOGGER.info("HttpRespReadException ignore");
                }
            });
        } catch (Exception e) {
            receiveResp.fail(e);
            return receiveResp;
        }

        receiveResp.success();
        return receiveResp;
    }


    private void processTask(TaskBase task) throws Exception {
        switch (task.taskType) {
            case STR_CONTENT:
                receiveStrContentTask((StrContentTask) task);
                break;
            case REFLECTION:
                receiveReflectTask((ReflectionTask) task);
                break;
            default:
                throw new UnsupportedOperationException("can not support task type " + task.taskType.name());
        }
    }


    // @RequestMapping(value = "/rdelay/receiveTask/STR_CONTENT", method = RequestMethod.POST)
    private void receiveStrContentTask(StrContentTask strContentTask) throws Exception {
        if (strContentTaskConsumer != null) {
            strContentTaskConsumer.consumeTask(strContentTask);
        }
    }

    // @RequestMapping(value = "/rdelay/receiveTask/REFLECT", method = RequestMethod.POST)
    private void receiveReflectTask(ReflectionTask reflectionTask) throws Exception {

        // paramTypes
        Class[] paramTypes = new Class[reflectionTask.paramTypeNames.length];
        int a = 0;
        for (String paramTypeName : reflectionTask.paramTypeNames) {
            paramTypes[a++] = Class.forName(paramTypeName);
        }

        // params
        Object[] params = new Object[reflectionTask.params.length];
        for (int b = 0; params.length > b; b++) {
            String rawParam = reflectionTask.params[b];
            Object param;

            // need to know whether param is wrapper class
            String paramTypeName = paramTypes[b].getName();
            switch (paramTypeName) {
                case "java.lang.String":
                    param = rawParam;
                    break;
                case "java.lang.Character":
                    param = rawParam.charAt(0);
                    break;
                case "java.lang.Byte":
                    param = Byte.valueOf(rawParam);
                    break;
                case "java.lang.Short":
                    param = Short.valueOf(rawParam);
                    break;
                case "java.lang.Integer":
                    param = Integer.valueOf(rawParam);
                    break;
                case "java.lang.Long":
                    param = Long.valueOf(rawParam);
                    break;
                case "java.lang.Float":
                    param = Float.valueOf(rawParam);
                    break;
                case "java.lang.Double":
                    param = Double.valueOf(rawParam);
                    break;
                default:
                    param = JSON.parseObject(rawParam, paramTypes[b]);
            }

            params[b] = param;
        }

        // targetClass
        Class targetClass = Class.forName(reflectionTask.className);

        // targetMethod
        Method targetMethod = null;
        try {
            targetMethod = targetClass.getDeclaredMethod(reflectionTask.methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // noop
        }

        if (null == targetMethod) {
            targetMethod = targetClass.getMethod(reflectionTask.methodName, paramTypes);
        }

        targetMethod.setAccessible(true);

        // trig method when static
        if (Modifier.isStatic(targetMethod.getModifiers())) {
            targetMethod.invoke(paramTypes, params);
            return;
        }

        // not static,get the instance
        Object targetObj = null;

        Map<String, Object> beanMap = applicationContext.getBeansOfType(targetClass, true, false);

        // the instance not found in spring bean context,invoke its non-arg constructor
        if (beanMap == null || beanMap.size() == 0) {
            if (targetClass.isInterface() || Modifier.isAbstract(targetClass.getModifiers())) {
                throw new NoSuchBeanDefinitionException(targetClass);
            }

            targetObj = targetClass.newInstance();
        } else {
            // the 1st instance
            targetObj = beanMap.values().toArray()[0];
        }

        targetMethod.invoke(targetObj, params);

    }
}
