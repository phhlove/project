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

package com.jredrain.controller;

import java.util.*;

import com.jredrain.base.job.RedRain;
import com.jredrain.base.job.RedRain.ExecType;
import com.jredrain.domain.User;
import com.jredrain.job.Globals;
import com.jredrain.tag.Page;
import com.jredrain.base.utils.CommonUtils;
import com.jredrain.base.utils.JsonMapper;
import com.jredrain.base.utils.WebUtils;
import com.jredrain.domain.Agent;
import com.jredrain.domain.Job;
import com.jredrain.service.*;
import com.jredrain.vo.JobVo;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.jredrain.base.utils.CommonUtils.notEmpty;

@Controller
@RequestMapping("/job")
public class JobController {

    @Autowired
    private ExecuteService executeService;

    @Autowired
    private JobService jobService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private RecordService recordService;

    @Autowired
    private SchedulerService schedulerService;

    @RequestMapping("/view")
    public String view(HttpServletRequest request, HttpSession session, Page page, JobVo job, Model model) {

        model.addAttribute("agents", agentService.getAll());

        model.addAttribute("jobs", jobService.getAll());
        if (notEmpty(job.getAgentId())) {
            model.addAttribute("agentId", job.getAgentId());
        }
        if (notEmpty(job.getExecType())) {
            model.addAttribute("execType", job.getExecType());
        }
        if (notEmpty(job.getRedo())) {
            model.addAttribute("redo", job.getRedo());
        }
        jobService.getJobVos(session, page, job);
        if (request.getParameter("refresh") != null) {
            return "/job/refresh";
        }
        return "/job/view";
    }

    @RequestMapping("/checkname")
    public void checkName(HttpServletResponse response, Long jobId, Long agentId, String name) {
        String result = jobService.checkName(jobId, agentId, name);
        WebUtils.writeHtml(response, result);
    }

    @RequestMapping("/addpage")
    public String addpage(Model model, Long id) {
        if (notEmpty(id)) {
            Agent agent = agentService.getAgent(id);
            model.addAttribute("agent", agent);
            List<Agent> agents = agentService.getAll();
            model.addAttribute("agents", agents);
        } else {
            List<Agent> agents = agentService.getAll();
            model.addAttribute("agents", agents);
        }
        return "/job/add";
    }

    @RequestMapping(value = "/save")
    public String save(HttpSession session, Job job, HttpServletRequest request) throws SchedulerException {

        if (job.getJobId()!=null) {
            Job job1 = jobService.getJob(job.getJobId());
            /**
             * 将数据库中持久化的作业和当前修改的合并,当前修改的属性覆盖持久化的属性...
             */
            BeanUtils.copyProperties(job1,job,"jobName","cronType","cronExp","command","execType","comment","redo","runCount","category","runModel");
        }

        //单任务
        if ( RedRain.JobCategory.SINGLETON.getCode().equals(job.getCategory()) ) {
            job.setOperateId( ((User)session.getAttribute(Globals.LOGIN_USER)).getUserId() );
            job.setUpdateTime(new Date());
            job = jobService.addOrUpdate(job);
        } else { //流程任务
            Map<String, Object[]> map = request.getParameterMap();
            Object[] jobName = map.get("child.jobName");
            Object[] jobId = map.get("child.jobId");
            Object[] agentId = map.get("child.agentId");
            Object[] command = map.get("child.command");
            Object[] redo = map.get("child.redo");
            Object[] runCount = map.get("child.runCount");
            Object[] comment = map.get("child.comment");
            List<Job> chindren = new ArrayList<Job>(0);
            for (int i = 0; i < jobName.length; i++) {
                Job chind = new Job();

                if (CommonUtils.notEmpty(jobId[i])) {
                    //子任务修改的..
                    Long jobid = Long.parseLong((String) jobId[i]);
                    chind = jobService.getJob(jobid);
                }

                /**
                 * 新增并行和串行,子任务和最顶层的父任务一样
                 */
                chind.setRunModel(job.getRunModel());
                chind.setJobName((String) jobName[i]);
                chind.setAgentId(Long.parseLong((String) agentId[i]));
                chind.setCommand((String) command[i]);
                chind.setCronExp(job.getCronExp());
                chind.setComment((String) comment[i]);
                chind.setRedo(Integer.parseInt((String) redo[i]));
                if (chind.getRedo() == 0) {
                    chind.setRunCount(null);
                } else {
                    chind.setRunCount(Long.parseLong((String) runCount[i]));
                }
                chindren.add(chind);
            }

            //流程任务必须有子任务,没有的话不保存
            if (CommonUtils.isEmpty(chindren)) {
                return "redirect:/job/view";
            }

            if (job.getOperateId() == null) {
                job.setOperateId( ((User)session.getAttribute(Globals.LOGIN_USER)).getUserId());
            }

            jobService.saveFlowJob(job, chindren);
        }

        schedulerService.syncJobTigger(job.getJobId(),executeService);

        return "redirect:/job/view";
    }

    @RequestMapping("/editsingle")
    public void editSingleJob(HttpServletResponse response, Long id) {
        JobVo job = jobService.getJobVoById(id);
        JsonMapper json = new JsonMapper();
        WebUtils.writeJson(response, json.toJson(job));
    }

    @RequestMapping("/editflow")
    public String editFlowJob(Model model, Long id) {
        JobVo job = jobService.getJobVoById(id);
        model.addAttribute("job", job);
        List<Agent> agents = agentService.getAll();
        model.addAttribute("agents", agents);
        return "/job/edit";
    }


    @RequestMapping("/edit")
    public void edit(HttpServletResponse response, Job job) throws SchedulerException {
        Job jober = jobService.getJob(job.getJobId());
        jober.setExecType(job.getExecType());
        jober.setCronType(job.getCronType());
        jober.setCronExp(job.getCronExp());
        jober.setCommand(job.getCommand());
        jober.setJobName(job.getJobName());
        jober.setRedo(job.getRedo());
        jober.setRunCount(job.getRunCount());
        jober.setUpdateTime(new Date());
        jobService.addOrUpdate(jober);
        schedulerService.syncJobTigger(jober.getJobId(),executeService);
        WebUtils.writeHtml(response, "success");
    }

    @RequestMapping("/canrun")
    public void canRun(Long id, HttpServletResponse response) {
        WebUtils.writeJson(response, recordService.isRunning(id).toString());
    }

    @RequestMapping("/execute")
    public void remoteExecute(Long id) {
        JobVo job = jobService.getJobVoById(id);//找到要执行的任务
        //手动执行
        job.setExecType(ExecType.OPERATOR.getStatus());
        job.setAgent(agentService.getAgent(job.getAgentId()));
        try {
            this.executeService.executeJob(job);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/goexec")
    public String goExec(Model model) {
        model.addAttribute("agents", agentService.getAll());
        return "/job/exec";
    }

    @RequestMapping("/detail")
    public String showDetail(Model model, Long id) {
        JobVo jobVo = jobService.getJobVoById(id);
        model.addAttribute("job", jobVo);
        return "/job/detail";
    }

    @RequestMapping("/remove")
    public void removeJob(Long jobId, HttpServletResponse response) {
        WebUtils.writeHtml(response, jobService.delete(jobId) == 1 ? "true" : "false");
    }

}
