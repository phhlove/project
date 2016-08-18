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

package com.jredrain.service;

import com.jredrain.dao.QueryDao;
import com.jredrain.domain.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by benjo on 2016/3/31.
 */

@Service
public class MonitorService {

    @Autowired
    private QueryDao queryDao;

    public void save(Monitor monitor) {
        queryDao.save(monitor);
    }

    public List<Map<String, ?>> getCpuData(Long agentId) {
        return queryDao.sqlQuery("SELECT DISTINCT DATE_FORMAT(t.monitTime,'%H:%i') AS cpuTime,TRUNCATE(cpuUs,2) AS cpuUs,TRUNCATE(cpuSy,2) as cpuSy,TRUNCATE(cpuUsage,2) AS cpuUsage,TRUNCATE(cpuId,2) AS cpuId FROM (SELECT cpuUs,cpuSy,cpuUs+cpuSy AS cpuUsage,cpuId,monitTime FROM monitor WHERE agentId = ? ORDER BY monitTime DESC LIMIT 180) AS t ORDER BY t.monitTime ASC", agentId);
    }
}
