package com.jasonf.order.service.impl;

import com.jasonf.order.dao.TaskHisMapper;
import com.jasonf.order.dao.TaskMapper;
import com.jasonf.order.pojo.Task;
import com.jasonf.order.pojo.TaskHis;
import com.jasonf.order.service.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class TaskServiceImpl implements TaskService {
    @Resource
    private TaskHisMapper taskHisMapper;

    @Resource
    private TaskMapper taskMapper;

    @Override
    @Transactional
    public void pointTask(Task task) {
        // 记录历史数据
        TaskHis taskHis = new TaskHis();
        BeanUtils.copyProperties(task, taskHis);
        taskHis.setId(null);    // 主键自动递增
        taskHis.setDeleteTime(new Date());
        taskHisMapper.insertSelective(taskHis);
        // 删除响应任务
        taskMapper.deleteByPrimaryKey(task.getId());
    }
}
