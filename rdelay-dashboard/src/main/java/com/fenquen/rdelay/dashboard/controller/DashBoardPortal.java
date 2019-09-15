package com.fenquen.rdelay.dashboard.controller;

import com.fenquen.rdelay.model.Persistence;
import com.fenquen.rdelay.model.resp.ExecutionResp;
import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.model.task.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DashBoardPortal {
    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping("/listTasks")
    public Object listTasks(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int rows) {
        Map<String, Object> map = new HashMap<>();

        Query query = new Query();
        map.put("total", mongoTemplate.count(query, Persistence.DbMetaData.TASK.tableName));

        query.with(PageRequest.of(page - 1, rows));
        query.with(Sort.by(Sort.Order.desc("createTime")));
        map.put("rows", mongoTemplate.find(query, TaskBase.class, Persistence.DbMetaData.TASK.tableName));

        return map;
    }

    @RequestMapping("/queryTaskDetail")
    public Object queryTaskDetail(@RequestParam String taskid,
                                  @RequestParam("taskType") String taskTypeName) {

        TaskType taskType = TaskType.valueOf(taskTypeName);

        Class<? extends TaskBase> clazz;
        switch (taskType) {
            case STR_CONTENT:
                clazz = StrContentTask.class;
                break;
            case REFLECTION:
                clazz = ReflectionTask.class;
                break;
            default:
                throw new UnsupportedOperationException("unrecognized taskType " + taskType);
        }

        Criteria criteria = Criteria.where("taskid").is(taskid);

        return mongoTemplate.findOne(new Query(criteria), clazz, Persistence.DbMetaData.TASK.tableName);

    }

    @RequestMapping("/listExecutionResps")
    public Object listExecutionResps(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int rows) {

        Map<String, Object> map = new HashMap<>();

        Query query = new Query();
        map.put("total", mongoTemplate.count(query, Persistence.DbMetaData.EXECUTION_RESP.tableName));

        query.with(PageRequest.of(page - 1, rows));
        query.with(Sort.by(Sort.Order.desc("executionTime")));
        map.put("rows", mongoTemplate.find(query, ExecutionResp.class, Persistence.DbMetaData.EXECUTION_RESP.tableName));

        return map;

    }
}
