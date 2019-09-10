package com.fenquen.rdelay.client.receiver;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.execution.ExecutionResp;
import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

@RestController
public class Receiver implements ApplicationContextAware {
    {
        System.out.println("init");
        // SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private StrContentTaskConsumer strContentTaskConsumer;

    @RequestMapping(value = "/rdelay/receiveTask/STR_CONTENT", method = RequestMethod.POST)
    public ExecutionResp receiveStrContentTask(@RequestBody StrContentTask strContentTask) {
        ExecutionResp timeUpResp = new ExecutionResp();
        try {
            if (strContentTaskConsumer != null) {
                strContentTaskConsumer.consumeTask(strContentTask);
            }
            timeUpResp.success();
        } catch (Exception e) {
            e.printStackTrace();
            timeUpResp.fail(e);
        }

        return timeUpResp;
    }

    @RequestMapping(value = "/rdelay/receiveTask/REFLECT", method = RequestMethod.POST)
    public ExecutionResp receiveReflectTask(@RequestBody ReflectionTask reflectionTask) {
        ExecutionResp timeUpResp = new ExecutionResp();
        try {
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
                timeUpResp.success();
                return timeUpResp;
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
            timeUpResp.success();
        } catch (Exception e) {
            e.printStackTrace();
            timeUpResp.fail(e);
        }

        return timeUpResp;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("setApplicationContext");
        this.applicationContext = applicationContext;
    }
}
