package com.fenquen.rdelay.dashboard.controller;

import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import com.fenquen.rdelay.model.task.TaskBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashBoardPortal {
    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping("/queryTask")
    public void queryTask(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "20") int pageSize) {
        Query query = new Query();
        query.with(PageRequest.of(pageNum - 1, pageSize));

        mongoTemplate.find(query, TaskBase.class, "task");
        return;
    }


}
