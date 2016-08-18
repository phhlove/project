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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by benjo on 2016/3/31.
 */

@Entity
@Table(name = "monitor")
public class Monitor implements Serializable {

    @Id
    @GeneratedValue
    private Long monitorId;

    private Long agentId;

    private Float cpuUs;

    private Float cpuSy;

    private Float cpuId;

    private Long memUsed;

    private Long memFree;

    private Date monitTime;

    public Monitor() {
    }

    public Monitor(Long agentId, Float cpuUs, Float cpuSy, Float cpuId, Long memUsed, Long memFree) {
        this.agentId = agentId;
        this.memFree = memFree;
        this.memUsed = memUsed;
        this.cpuUs = cpuUs;
        this.cpuSy = cpuSy;
        this.cpuId = cpuId;
        this.monitTime = new Date();
    }

    public Long getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(Long monitorId) {
        this.monitorId = monitorId;
    }

    public Long getWorkerId() {
        return agentId;
    }

    public void setWorkerId(Long agentId) {
        agentId = agentId;
    }


    public Float getCpuUs() {
        return cpuUs;
    }

    public void setCpuUs(Float cpuUs) {
        this.cpuUs = cpuUs;
    }

    public Float getCpuSy() {
        return cpuSy;
    }

    public void setCpuSy(Float cpuSy) {
        this.cpuSy = cpuSy;
    }

    public Float getCpuId() {
        return cpuId;
    }

    public void setCpuId(Float cpuId) {
        this.cpuId = cpuId;
    }

    public Long getMemUsed() {
        return memUsed;
    }

    public void setMemUsed(Long memUsed) {
        this.memUsed = memUsed;
    }

    public Long getMemFree() {
        return memFree;
    }

    public void setMemFree(Long memFree) {
        this.memFree = memFree;
    }

    public Date getMonitTime() {
        return monitTime;
    }

    public void setMonitTime(Date monitTime) {
        this.monitTime = monitTime;
    }
}
