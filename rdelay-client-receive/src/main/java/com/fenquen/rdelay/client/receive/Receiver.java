package com.fenquen.rdelay.client.receive;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.task.AbstractTask;
import com.fenquen.rdelay.model.execution.ExecutionResp;
import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

@RestController
public class Receiver implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private TaskConsumer taskConsumer;

    @RequestMapping(value = "/rdelay/receiveTask/STR_CONTENT", method = RequestMethod.POST)
    public ExecutionResp receiveStrContentTask(@RequestBody StrContentTask strContentTask) {
        ExecutionResp timeUpResp = new ExecutionResp();
        try {
            if (taskConsumer != null) {
                taskConsumer.consumeTask(strContentTask);
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
                params[b] = JSON.parseObject(reflectionTask.params[b], paramTypes[b]);
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

            // the instance not found,invoke its non-arg constructor
            if (beanMap.size() == 0) {
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

    }
}
