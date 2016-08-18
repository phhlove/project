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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.jredrain.base.job.RedRain;
import com.jredrain.dao.QueryDao;
import com.jredrain.domain.User;
import com.jredrain.domain.Agent;
import com.jredrain.job.Globals;
import com.jredrain.session.MemcacheCache;
import com.jredrain.tag.Page;

import static com.jredrain.base.job.RedRain.*;

import com.jredrain.base.utils.CommonUtils;
import com.jredrain.domain.Job;
import com.jredrain.vo.JobVo;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;

import static com.jredrain.base.utils.CommonUtils.notEmpty;

@Service
public class JobService {

    @Autowired
    private QueryDao queryDao;

    @Autowired
    private AgentService agentService;

    @Autowired
    private MemcacheCache memcacheCache;

    private Logger logger = LoggerFactory.getLogger(JobService.class);

    public Job getJob(Long jobId) {
        return queryDao.get(Job.class,jobId);
    }

    /**
     * 获取将要执行的任务
     * @return
     */
    public List<JobVo> getJobVo(ExecType execType, CronType cronType) {
        String sql = "SELECT t.*,d.name AS agentName,d.port,d.ip,d.password,d.warning FROM job t LEFT JOIN agent d ON t.agentId = d.agentId WHERE IFNULL(t.flowNum,0)=0 AND cronType=? AND execType = ? AND t.status=1";
        List<JobVo> jobs = queryDao.sqlQuery(JobVo.class, sql, cronType.getType(), execType.getStatus());
        if (CommonUtils.notEmpty(jobs)) {
            for (JobVo job : jobs) {
                job.setAgent(agentService.getAgent(job.getAgentId()));
                job.setChildren(queryChildren(job));
            }
        }
        return jobs;
    }

    public List<JobVo> getJobVoByAgentId(Agent agent, ExecType execType, CronType cronType) {
        String sql = "SELECT t.*,d.name AS agentName,d.port,d.ip,d.password,d.warning FROM job t INNER JOIN agent d ON t.agentId = d.agentId WHERE IFNULL(t.flowNum,0)=0 AND cronType=? AND execType = ? AND t.status=1 and d.agentId=? ";
        List<JobVo> jobs = queryDao.sqlQuery(JobVo.class, sql, cronType.getType(), execType.getStatus(),agent.getAgentId());
        if (CommonUtils.notEmpty(jobs)) {
            for (JobVo job : jobs) {
                job.setAgent(agentService.getAgent(job.getAgentId()));
                job.setChildren(queryChildren(job));
            }
        }
        return jobs;
    }

    public List<Job> getJobsByCategory(JobCategory category){
        String sql = "SELECT * FROM job WHERE status=1 AND category=?";
        if (JobCategory.FLOW.equals(category)) {
            sql +=" AND flownum=0";
        }
        return queryDao.sqlQuery(Job.class,sql,category.getCode());
    }

    public List<JobVo> getCrontabJob() {
        logger.info("[redrain] init quartzJob...");
        return getJobVo(RedRain.ExecType.AUTO, RedRain.CronType.CRONTAB);
    }

    private void flushCronjob(){
        memcacheCache.evict(Globals.CACHED_CRONTAB_JOB);
        memcacheCache.put(Globals.CACHED_CRONTAB_JOB,getJobVo(RedRain.ExecType.AUTO, RedRain.CronType.CRONTAB));
    }

    public Page<JobVo> getJobVos(HttpSession session, Page page, JobVo job) {
        String sql = "SELECT t.*,d.name AS agentName,d.port,d.ip,d.password,u.userName AS operateUname " +
                " FROM job AS t LEFT JOIN agent AS d ON t.agentId = d.agentId LEFT JOIN user as u ON t.operateId = u.userId WHERE IFNULL(flowNum,0)=0 AND t.status=1 ";
        if (job != null) {
            if (notEmpty(job.getAgentId())) {
                sql += " AND t.agentId=" + job.getAgentId();
            }
            if (notEmpty(job.getExecType())) {
                sql += " AND t.execType=" + job.getExecType();
            }
            if (notEmpty(job.getRedo())) {
                sql += " AND t.redo=" + job.getRedo();
            }
            if (!(Boolean) session.getAttribute("permission")) {
                sql += " AND t.operateId = " + ((User)session.getAttribute(Globals.LOGIN_USER)).getUserId();
            }
        }
        page = queryDao.getPageBySql(page, JobVo.class, sql);
        List<JobVo> parentJobs = page.getResult();

        for (JobVo parentJob : parentJobs) {
            queryChildren(parentJob);
        }
        page.setResult(parentJobs);
        return page;
    }

    private List<JobVo> queryChildren(JobVo job) {
        if (job.getCategory().equals(JobCategory.FLOW.getCode())) {
            String sql = "SELECT t.*,d.name AS agentName,d.port,d.ip,d.password,u.userName AS operateUname" +
                    " FROM job AS t LEFT JOIN agent AS d ON t.agentId = d.agentId LEFT JOIN user AS u " +
                    " ON t.operateId = u.userId WHERE t.status=1 AND t.flowId = ? AND t.flowNum>0 ORDER BY t.flowNum ASC";
            List<JobVo> childJobs = queryDao.sqlQuery(JobVo.class, sql, job.getFlowId());
            if (CommonUtils.notEmpty(childJobs)) {
                for(JobVo jobVo:childJobs){
                    jobVo.setAgent(agentService.getAgent(jobVo.getAgentId()));
                }
            }
            job.setChildren(childJobs);
            return childJobs;
        }
        return Collections.emptyList();
    }

    public Job addOrUpdate(Job job) {
        Job saveJob = (Job)queryDao.save(job);
        flushCronjob();
        return saveJob;
    }

    public JobVo getJobVoById(Long id) {
        String sql = "SELECT t.*,d.name AS agentName,d.port,d.ip,d.password,u.userName AS operateUname " +
                " FROM job AS t LEFT JOIN agent AS d ON t.agentId = d.agentId LEFT JOIN user AS u ON t.operateId = u.userId WHERE t.jobId =?";
        JobVo job = queryDao.sqlUniqueQuery(JobVo.class, sql, id);
        job.setAgent(agentService.getAgent(job.getAgentId()));
        queryChildren(job);
        return job;
    }

    public List<Job> getAll() {
        return queryDao.getAll(Job.class);
    }

    public List<JobVo> getJobByAgentId(Long agentId) {
        String sql = "SELECT t.*,d.name AS agentName,d.port,d.ip,d.password,u.userName AS operateUname " +
                " FROM job t LEFT JOIN user u ON t.operateId = u.userId LEFT JOIN agent d ON t.agentId = d.agentId WHERE t.agentId =?";
        return queryDao.sqlQuery(JobVo.class, sql, agentId);
    }

    public String checkName(Long jobId,Long agentId, String name) {
        String sql = "SELECT COUNT(1) FROM job WHERE agentId=? AND status=1 AND jobName=? ";
        if (notEmpty(jobId)) {
            sql += " AND jobId != " + jobId + " AND flowId != " + jobId;
        }
        return (queryDao.getCountBySql(sql, agentId,name)) > 0L ? "no" : "yes";
    }

    @Transactional(readOnly = false)
    public int delete(Long jobId) {
        int count = queryDao.createSQLQuery("update job set status=0 WHERE jobId = " + jobId).executeUpdate();
        flushCronjob();
        return count;
    }

    @Transactional(readOnly = false)
    public void saveFlowJob(Job job, List<Job> children) throws SchedulerException {
        job.setLastFlag(false);
        job.setUpdateTime(new Date());
        job.setFlowNum(0);//顶层sort是0

        /**
         * 保存最顶层的父级任务
         */
        if (job.getJobId()!=null) {
            addOrUpdate(job);
            /**
             * 当前作业已有的子作业
             */
            JobVo jobVo = new JobVo();
            jobVo.setCategory(JobCategory.FLOW.getCode());
            jobVo.setFlowId(job.getFlowId());

            /**
             * 取差集..
             */
            List<JobVo> hasChildren = queryChildren(jobVo);
            //数据库里已经存在的子集合..
            top:for(JobVo hasChild:hasChildren) {
                //当前页面提交过来的子集合...
                for(Job child:children){
                    if ( child.getJobId()!=null && child.getJobId().equals(hasChild.getJobId()) ) {
                        continue top;
                    }
                }
                /**
                 * 已有的子作业被删除的,则做删除操作...
                 */
                delete(hasChild.getJobId());
            }
        }else {
            Job job1 = addOrUpdate(job);
            job1.setFlowId(job1.getJobId());//flowId
            addOrUpdate(job1);
            job.setJobId(job1.getJobId());

        }

        for (int i=0;i<children.size();i++) {
            Job chind = children.get(i);
            /**
             * 子作业的流程编号都为顶层父任务的jobId
             */
            chind.setFlowId(job.getJobId());
            chind.setOperateId(job.getOperateId());
            chind.setExecType(job.getExecType());
            chind.setUpdateTime(new Date());
            chind.setCategory(JobCategory.FLOW.getCode());
            chind.setFlowNum(i+1);
            chind.setLastFlag(chind.getFlowNum()==children.size());
            addOrUpdate(chind);
        }
    }


}
