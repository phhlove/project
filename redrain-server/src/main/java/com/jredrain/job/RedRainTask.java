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


package com.jredrain.job;

import com.jredrain.session.MemcacheCache;
import org.apache.log4j.Logger;
import com.jredrain.base.job.RedRain;
import com.jredrain.base.utils.CommonUtils;
import com.jredrain.domain.Record;
import com.jredrain.domain.Agent;
import com.jredrain.service.*;
import com.jredrain.vo.JobVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


@Component
public class RedRainTask implements InitializingBean {

    private final Logger logger = Logger.getLogger(RedRainTask.class);

    @Autowired
    private AgentService agentService;

    @Autowired
    private ExecuteService executeService;

    @Autowired
    private RecordService recordService;

    @Autowired
    private JobService jobService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private MemcacheCache memcacheCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        clearCache();
        schedulerService.initQuartz(executeService);
        schedulerService.startCrontab();
    }

    /**
     * 执行器通信监控,每10秒通信一次
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void ping() {
        logger.info("[redrain]:checking Agent connection...");
        List<Agent> agents = agentService.getAll();
        for (final Agent agent : agents) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Boolean result;
                    int countLimit = 0;
                    do {
                        result = executeService.ping(agent);
                        ++countLimit;
                    } while (!result && countLimit < 3);
                    if (!result) {
                        if (CommonUtils.isEmpty(agent.getFailTime()) || new Date().getTime() - agent.getFailTime().getTime() >= configService.getSysConfig().getSpaceTime() * 60 * 1000) {
                            noticeService.notice(agent);
                            //记录本次任务失败的时间
                            agent.setFailTime(new Date());
                            agent.setStatus(false);
                            agentService.addOrUpdate(agent);
                        }

                        if (agent.getStatus()) {
                            agent.setStatus(false);
                            agentService.addOrUpdate(agent);
                        }

                    } else {
                        if (!agent.getStatus()) {
                            agent.setStatus(true);
                            agentService.addOrUpdate(agent);
                        }
                    }
                }
            };
            runnable.run();
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void redoJob() {
        logger.info("[redrain] redojob running...");
        List<Record> records = recordService.getReExecuteRecord();
        for (final Record record : records) {
            JobVo jobVo = jobService.getJobVoById(record.getJobId());
            try {
                jobVo.setAgent(agentService.getAgent(jobVo.getAgentId()));
                executeService.reRunJob(record, jobVo, RedRain.JobCategory.SINGLETON);
            } catch (Exception e) {
                //任务执行失败,发送通知警告
                noticeService.notice(jobVo);
                throw new RuntimeException("reexecute job is failed while executing job:" + jobVo.getJobId());
            }
        }
    }


    private void clearCache() {
        memcacheCache.evict(Globals.CACHED_AGENT_ID);
        memcacheCache.evict(Globals.CACHED_CRONTAB_JOB);
    }


    //@Scheduled(cron = "0 0/1 * * * ?")
  /*
  //离线监控.....
  public void monitor() throws Exception {
        List<Agent> agents = agentService.getAll();

        for (Agent agent : agents) {
            Map<String, String> systemData = executeService.monitor(agent);

            String cpuUsage = systemData.remove("cpuUsage");
            Float cpuUs = Float.parseFloat(cpuUsage.split(",")[0]);
            Float cpuSy = Float.parseFloat(cpuUsage.split(",")[1]);
            Float cpuId = Float.parseFloat(cpuUsage.split(",")[2]);

            String memUsage = systemData.remove("memUsage");
            Long memUsed = Long.parseLong(memUsage.split(",")[0]);
            Long memFree = Long.parseLong(memUsage.split(",")[1]);

            Monitor monitor = new Monitor(agent.getAgentId(), cpuUs, cpuSy, cpuId, memUsed, memFree);
            monitorService.save(monitor);
        }
    }*/


}
