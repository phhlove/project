/**
 * Copyright 2016 benjobs
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.jredrain.domain;

import com.jredrain.base.job.RedRain;
import com.jredrain.vo.JobVo;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

@Entity
@Table(name = "record")
public class Record implements Serializable {

    @Id
    @GeneratedValue
    private Long recordId;
    private Long jobId;
    private String command;
    private Integer returnCode;
    private Integer success;
    private Date startTime;
    private Date endTime;
    private Integer execType;
    private String message;
    private Long redoCount;
    private Integer status;
    private String pid;

    /**
     * 重跑记录对象的父记录
     */
    private Long parentId;

    private Long flowGroup;

    /**
     * 流程任务的执行序号
     */
    private Integer flowNum;

    /**
     * 任务类型(0:单一任务,1:流程任务)
     */
    private Integer category;

    public Record() {
    }

    public Record(JobVo task) {
        this.setJobId(task.getJobId());
        this.setExecType( task.getExecType() );
        this.setCommand(task.getCommand());//执行的命令
        this.setStartTime(new Date());//开始执行的时间
        this.setRedoCount(0L);//运行次数
        this.setSuccess(RedRain.ResultStatus.SUCCESSFUL.getStatus());
        this.setStatus(RedRain.RunStatus.RUNNING.getStatus());//任务还未完成
        this.setPid(UUID.randomUUID().toString().replace("-", ""));
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getExecType() {
        return execType;
    }

    public void setExecType(Integer execType) {
        this.execType = execType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRedoCount() {
        return redoCount;
    }

    public void setRedoCount(Long redoCount) {
        this.redoCount = redoCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Long getFlowGroup() {
        return flowGroup;
    }

    public void setFlowGroup(Long flowGroup) {
        this.flowGroup = flowGroup;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getFlowNum() {
        return flowNum;
    }

    public void setFlowNum(Integer flowNum) {
        this.flowNum = flowNum;
    }
}
