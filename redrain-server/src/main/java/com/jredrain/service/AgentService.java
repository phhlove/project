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

import java.util.List;

import com.jredrain.base.utils.CommonUtils;
import com.jredrain.job.Globals;
import com.jredrain.session.MemcacheCache;
import org.apache.commons.codec.digest.DigestUtils;
import com.jredrain.base.job.RedRain;
import com.jredrain.dao.QueryDao;
import com.jredrain.tag.Page;
import com.jredrain.domain.Agent;
import com.jredrain.vo.JobVo;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jredrain.base.utils.CommonUtils.notEmpty;

@Service
public class AgentService {

    @Autowired
    private QueryDao queryDao;

    @Autowired
    private ExecuteService executeService;

    @Autowired
    private JobService jobService;

    @Autowired
    private SchedulerService schedulerService;


    @Autowired
    private MemcacheCache memcacheCache;


    public List<Agent> getAll() {

        List<Agent> agents = memcacheCache.get(Globals.CACHED_AGENT_ID,List.class);

        if (CommonUtils.isEmpty(agents)) {
            flushAgent();
        }

       return memcacheCache.get(Globals.CACHED_AGENT_ID,List.class);
    }

    private void flushAgent(){
        memcacheCache.evict(Globals.CACHED_AGENT_ID);
        memcacheCache.put(Globals.CACHED_AGENT_ID,queryDao.getAll(Agent.class));
    }

    public List<Agent> getAgentByStatus(int status){
        String sql = "SELECT * FROM agent WHERE status=?";
        return queryDao.sqlQuery(Agent.class,sql,status);
    }

    public Page getAgent(Page page) {
        String sql = "SELECT * FROM agent";
        queryDao.getPageBySql(page, Agent.class, sql);
        return page;
    }

    public Agent getAgent(Long id) {
        return queryDao.get(Agent.class, id);
    }

    public void addOrUpdate(Agent agent) {
        /**
         * 修改过agent
         */
        boolean update = false;
        if (agent.getAgentId()!=null) {
            update = true;
        }

        /**
         * fix bug.....
         * 修改了agent要刷新所有在任务队列里对应的作业,
         * 否则一段端口改变了,任务队列里的还是更改前的连接端口,
         * 当作业执行的时候就会连接失败...
         *
         */
        if (update) {
            queryDao.save(agent);
            /**
             * 获取该执行器下所有的自动执行,并且是quartz类型的作业
             */
            List<JobVo> jobVos = jobService.getJobVoByAgentId(agent, RedRain.ExecType.AUTO, RedRain.CronType.QUARTZ);
            try {
                schedulerService.addOrModify(jobVos,this.executeService);
            } catch (SchedulerException e) {
                /**
                 * 创新任务列表失败,抛出异常,整个事务回滚...
                 */
                throw new RuntimeException(e.getCause());
            }
        }else {
            queryDao.save(agent);
        }

        /**
         * 同步缓存...
         */
        flushAgent();

    }

    public String checkName(Long id, String name) {
        String sql = "SELECT COUNT(1) FROM agent WHERE name=? ";
        if (notEmpty(id)) {
            sql += " AND agentId != " + id;
        }
        return (queryDao.getCountBySql(sql, name)) > 0L ? "no" : "yes";
    }

    public String editPwd(Long id, String pwd0, String pwd1, String pwd2) {
        Agent work = this.getAgent(id);
        String password = DigestUtils.md5Hex(pwd0);
        if (password.equals(work.getPassword())) {
            if (pwd1.equals(pwd2)) {
                pwd1 = DigestUtils.md5Hex(pwd1);
                work.setPassword(pwd1);
                Boolean flag = executeService.password(work,work.getIp(), work.getPort(), password, pwd1);
                if (flag) {
                    this.addOrUpdate(work);
                    return "success";
                } else {
                    return "failure";
                }
            } else {
                return "two";
            }
        } else {
            return "one";
        }
    }


}
