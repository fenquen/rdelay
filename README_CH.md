# rdelay[English](README.md)
rdelay是一款基于redis的分布式轻量级定时任务框架,它是个人闲暇时间实验性质的工程

### 原理
使用redis的ZSET维持基于任务执行时间的有序列表,后台会不断获取到达执行时间的任务并将器调度到该任务指定的应用服务(该服务需要集成rdelay-client-receiver),
redelay任务支持失败重试机制


### 注意
该框架目前还处在相对原始的状态还有许多功能和优化项需要完成,如果你确实需要使用请自行承担可能带来的风险


### 大体框架
![Rough structure](./assets/rdelay.png)

### 任务的生命周期
![lifecycle](./assets/lifecycle.png)

### 使用方式
##### rdelay-cli
项目导入rdelay-cli.jar

###### 生成任务(传递文本消息)
``````
 public static void sendTask() throws Exception {
         // designate the rdelay server address where the task to be sent
         RdelayCli.setDestSvrAddr("http://127.0.0.1:8086");
 
         //  while (true) {
         Req4CreateStrContentTask req4CreateStrContentTask = new Req4CreateStrContentTask();
 
         req4CreateStrContentTask.bizTag = "testBizTag";
         // run after 2s
         req4CreateStrContentTask.executionTime = System.currentTimeMillis() + 2000;
         req4CreateStrContentTask.executionAppSvrAddr = "http://127.0.0.1:8080";
         req4CreateStrContentTask.content = "testContent";
 
         Resp4CreateTask resp4CreateTask = RdelayCli.sendTask(req4CreateStrContentTask);
 
         System.out.println(resp4CreateTask.success + "_" + resp4CreateTask.errMsg);
 
         //  Thread.sleep(60000000);
         //  }
     }
``````
rdelay任务也支持cron表达式,以实现循环任务的循环调用
`````
public static void sendCronTask() throws Exception {
        // designate the rdelay server address where the task to be sent
        RdelayCli.setDestSvrAddr("http://127.0.0.1:8086");


        Req4CreateStrContentTask req4CreateStrContentTask = new Req4CreateStrContentTask();

        req4CreateStrContentTask.bizTag = "testBizTag";

        // enable cron
        req4CreateStrContentTask.enableCron = true;
        // when cron is enabled,"executionTime" will be ignored
        req4CreateStrContentTask.executionTime = System.currentTimeMillis() + 2000;
        // cron expression,executed per 2h
        req4CreateStrContentTask.cronExpression = "0/10 * * * * ? ";

        req4CreateStrContentTask.executionAppSvrAddr = "http://127.0.0.1:8080";
        req4CreateStrContentTask.content = "testContent";

        Resp4CreateTask resp4CreateTask = RdelayCli.sendTask(req4CreateStrContentTask);

        System.out.println(resp4CreateTask.success + "_" + resp4CreateTask.errMsg);

    }
``````
rdelay任务同样支持反射调用,使用Req4CreateReflectionTask
``````
 public static void sendReflectionTask() throws Exception {
        // designate the rdelay server address where the task to be sent
        RdelayCli.setDestSvrAddr("http://127.0.0.1:8086");

        Req4CreateReflectionTask req4CreateReflectionTask = new Req4CreateReflectionTask();

        req4CreateReflectionTask.bizTag = "testBizTag";

        // enable cron
        req4CreateReflectionTask.enableCron = true;
        // when cron is enabled,"executionTime" will be ignored
        req4CreateReflectionTask.executionTime = System.currentTimeMillis() + 2000;
        // cron expression,executed per 2h
        req4CreateReflectionTask.cronExpression = "0/10 * * * * ? ";

        req4CreateReflectionTask.executionAppSvrAddr = "http://127.0.0.1:8080";

        // necessary for reflection
        req4CreateReflectionTask.className = "com.a.d";
        req4CreateReflectionTask.methodName = "helloWorld";
        req4CreateReflectionTask.paramTypeNames = new String[]{"java.lang.String"};
        req4CreateReflectionTask.params = new String[]{"rdealy"};

        Resp4CreateTask resp4CreateTask = RdelayCli.sendTask(req4CreateReflectionTask);

        System.out.println(resp4CreateTask.success + "_" + resp4CreateTask.errMsg);
    }
``````
###### rdelay任务生命周期控制(暂停,恢复,停止)
``````
    public static void pauseTask() throws Exception {
        RespBase resp = RdelayCli.modifyTaskState(new Req4PauseTask("your taskid"));
        System.out.println(resp.success + "_" + resp.errMsg);
    }

    public static void resumeTask() throws Exception {
        RespBase resp = RdelayCli.modifyTaskState(new Req4ResumeTask("your taskid"));
        System.out.println(resp.success + "_" + resp.errMsg);
    }

    // the task is aborted, it can not be available any more
    public static void abortTaskManually() throws Exception {
        RespBase resp = RdelayCli.modifyTaskState(new Req4AbortTaskManually("your taskid"));
        System.out.println(resp.success + "_" + resp.errMsg);
    }
``````
#### rdelay任务接收(目前只实现了spring mvc controller模式)
项目引入rdelay-client-receiver.jar<br>

######用户实现StrContentTaskConsumer(非强制)使用自定的逻辑处理StrContentTask中的文本消息
```java
@Component
public class TaskConsumer implements StrContentTaskConsumer {
    @Override
    public void consumeTask(StrContentTask strContentTask) {
        System.out.println(strContentTask.content);
    }
}
```
######反射调用任务ReflectionTask会在receiver接收后自动调用,该过程对用户是透明的