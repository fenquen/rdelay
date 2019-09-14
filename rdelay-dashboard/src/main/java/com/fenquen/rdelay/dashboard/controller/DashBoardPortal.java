package com.fenquen.rdelay.dashboard.controller;

import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.model.task.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DashBoardPortal {
    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping("/queryTask")
    public Object queryTask(@RequestParam(defaultValue = "1") int pageNum,
                            @RequestParam(defaultValue = "20") int pageSize) {
        Map<String, Object> map = new HashMap<>();

        Query query = new Query();
        map.put("total", mongoTemplate.count(query, "TASK"));

        query.with(PageRequest.of(pageNum - 1, pageSize));
        map.put("rows", mongoTemplate.find(query, TaskBase.class, "TASK"));

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
            case REFLECT:
                clazz = ReflectionTask.class;
                break;
            default:
                throw new UnsupportedOperationException("unrecognized taskType " + taskType);
        }

        Criteria criteria = Criteria.where("taskid").is(taskid);

        return mongoTemplate.findOne(new Query(criteria), clazz, "TASK");

    }
}
