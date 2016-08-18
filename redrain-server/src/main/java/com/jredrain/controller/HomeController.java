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

import com.alibaba.fastjson.JSON;
import com.jredrain.base.utils.*;
import com.jredrain.domain.User;
import com.jredrain.job.Globals;
import net.sf.json.JSONObject;
import com.jredrain.base.job.RedRain;
import com.jredrain.base.job.Response;
import com.jredrain.domain.Job;
import com.jredrain.domain.Agent;
import com.jredrain.service.*;
import com.jredrain.tag.Page;
import com.jredrain.vo.ChartVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.jredrain.base.utils.CommonUtils.isEmpty;
import static com.jredrain.base.utils.CommonUtils.notEmpty;

/**
 * Created by ChenHui on 2016/2/17.
 */
@Controller
public class HomeController {

    @Autowired
    private HomeService homeService;

    @Autowired
    private RecordService recordService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ExecuteService executeService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;


    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/home")
    public String index(Model model) {

        /**
         * agent...
         */
        List<Agent> success = agentService.getAgentByStatus(1);
        List<Agent> failed = agentService.getAgentByStatus(0);
        model.addAttribute("success",success.size());
        model.addAttribute("failed",failed.size());

        success.addAll(failed);
        model.addAttribute("agents",success);

        /**
         * job
         */
        List<Job> singleton = jobService.getJobsByCategory(RedRain.JobCategory.SINGLETON);
        List<Job> flow = jobService.getJobsByCategory(RedRain.JobCategory.FLOW);

        model.addAttribute("singleton",singleton.size());
        model.addAttribute("flow",flow.size());
        singleton.addAll(flow);
        model.addAttribute("job",singleton.size());

        /**
         * 成功作业,自动执行
         */
        Long successAutoRecord = recordService.getRecords(1, RedRain.ExecType.AUTO);
        Long successOperRecord = recordService.getRecords(1, RedRain.ExecType.OPERATOR);

        model.addAttribute("successAutoRecord",successAutoRecord);
        model.addAttribute("successOperRecord",successOperRecord);
        model.addAttribute("successRecord",successAutoRecord+successOperRecord);

        /**
         * 失败作业
         */
        Long failedAutoRecord = recordService.getRecords(0, RedRain.ExecType.AUTO);
        Long failedOperRecord = recordService.getRecords(0, RedRain.ExecType.OPERATOR);
        model.addAttribute("failedAutoRecord",failedAutoRecord);
        model.addAttribute("failedOperRecord",failedOperRecord);
        model.addAttribute("failedRecord",failedAutoRecord+failedOperRecord);


        model.addAttribute("startTime", DateUtils.getCurrDayPrevDay(7));
        model.addAttribute("endTime", DateUtils.formatSimpleDate(new Date()));

        return "/home/index";
    }

    @RequestMapping("/darwchart")
    public void refreshChart(HttpServletResponse response) {
        JsonMapper jsonMapper = new JsonMapper();
        JSONObject json = new JSONObject();
        //执行类型占比数据
        json.put("execType", jsonMapper.toJson(recordService.getExecTypePieData()));
        //成功失败占比数据
        json.put("status", jsonMapper.toJson(recordService.getStatusDonutData()));

        WebUtils.writeJson(response, json.toString());
    }

    @RequestMapping("/diffchart")
    public void diffChart(HttpServletResponse response,String startTime, String endTime) {
        if (isEmpty(startTime)) {
            startTime = DateUtils.getCurrDayPrevDay(7);
        }
        if (isEmpty(endTime)) {
            endTime = DateUtils.formatSimpleDate(new Date());
        }
        //成功失败折线图数据
        JsonMapper jsonMapper = new JsonMapper();
        List<ChartVo> voList = recordService.getDiffData(startTime, endTime);
        if (isEmpty(voList)) {
            WebUtils.writeJson(response, "null");
        }else {
            WebUtils.writeJson(response, jsonMapper.toJson(voList));
        }
    }

    @RequestMapping("/monitor")
    public void port(HttpServletResponse response, Long agentId) throws Exception {
        Agent agent = agentService.getAgent(agentId);
        Response req = executeService.monitor(agent);
        /**
         * 直联
         */

        String format = "%d_%s";

        if (agent.getProxy().equals(RedRain.ConnType.CONN.getType())) {
            String port = req.getResult().get("port");
            String url = String.format("http://%s:%s", agent.getIp(), port);
            WebUtils.writeHtml(response, String.format(format,agent.getProxy(), url));
        } else {//代理
            WebUtils.writeHtml(response, String.format(format,agent.getProxy(),JSON.toJSONString(req.getResult())) );
        }
    }

   /* @RequestMapping("/monitor")
    public void monitor(HttpServletResponse response, Long agentId) throws Exception {
        Agent agent = agentService.getAgent(agentId);
        Map<String, String> data = executeService.monitor(agent);
        JsonMapper jsonMapper = new JsonMapper();
        PageIOUtils.writeJson(response, jsonMapper.toJson(data));
    }*/

    @RequestMapping("/cpuchart")
    public void cpuChart(HttpServletResponse response, Model model, Long agentId) throws Exception {
        //CPU图表数据
        if (notEmpty(agentId)) {
            model.addAttribute("agentId", agentId);
            JsonMapper jsonMapper = new JsonMapper();
            WebUtils.writeJson(response, jsonMapper.toJson(monitorService.getCpuData(agentId)));
        }
    }

    @RequestMapping("/login")
    public void login(HttpServletRequest request,HttpServletResponse response, HttpSession httpSession, @RequestParam String username, @RequestParam String password) throws Exception {
        //用户信息验证
        int status = homeService.checkLogin(httpSession, username, password);

        if (status == 500) {
            WebUtils.writeJson(response, "{\"msg\":\"用户名密码错误\"}");
            return;
        }
        if (status == 200) {
            User user = (User) httpSession.getAttribute(Globals.LOGIN_USER);
            if (user.getHeaderpic()!=null) {
                String name = user.getUserId() + "_140"+user.getPicExtName();
                String path = httpSession.getServletContext().getRealPath(File.separator) + "upload" + File.separator + name;
                IOUtils.writeFile(new File(path), user.getHeaderpic().getBinaryStream());
                user.setHreaderPath(WebUtils.getWebUrlPath(request)+"/upload/"+name);
            }
            WebUtils.writeJson(response, "{\"successUrl\":\"/home\"}");
            return;
        }
    }


    @RequestMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.removeAttribute(Globals.LOGIN_USER);
        return "redirect:/";
    }

    @RequestMapping("/upload/upimg")
    public void uploadimg(@RequestParam(value = "file", required = false) MultipartFile file,@RequestParam Long userId,HttpSession httpSession, HttpServletResponse response) throws Exception {
        User user = userService.getUserById(userId);
        String extensionName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String path = httpSession.getServletContext().getRealPath("/")+"upload"+File.separator;

        String fileName = user.getUserId()+"_preview." + extensionName.toLowerCase();
        File targetFile = new File(path, fileName);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }

        try {
            file.transferTo(targetFile);
            logger.info(" upload file successful @ "+fileName);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("upload exception:"+e.getMessage());
        }
        /**
         * 上传文件有可能很大,则生成一个300*300的图片返回.....
         */
        ImageUtils.zoom(targetFile);

        fileName =  user.getUserId()+"_300." + extensionName.toLowerCase();
        String filepath = path + fileName;
        if (IOUtils.fileExists(filepath)) {
            new File(filepath).delete();
        }

        float scale = ImageUtils.scale(targetFile.getPath(),filepath,1,false);
        logger.info("图片缩放倍数:"+scale);
        String format = "{\"fileUrl\":\"%s\",\"flag\":\"%s\",\"scale\":%f}";
        WebUtils.writeJson( response, String.format(format,"/upload/"+fileName+"?"+System.currentTimeMillis(),scale>-1f,scale));
    }

    @RequestMapping("/upload/cutimg")
    public void imageCut(HttpServletRequest request,HttpServletResponse response,HttpSession httpSession,
                         Long userId,Integer x,Integer y,Integer w,Integer h,Float f,String p) throws IllegalStateException, IOException {

        p = p.replaceAll("\\?\\d+$","");
        String extensionName = p.substring(p.lastIndexOf("."));
        String path = httpSession.getServletContext().getRealPath("/")+"upload"+File.separator;
        //获取上传的原始图片
        String imagePath = path+userId+"_300" + extensionName.toLowerCase();

        String imgName = userId+"_140"+extensionName.toLowerCase();
        String createImgPath = path + imgName;

        if(f==-1f){
            ImageUtils.zoom(imagePath,createImgPath,140,140);
        }else{
            //进行剪切图片操作
            ImageUtils.abscut(imagePath, createImgPath, x,y,w, h);
            ImageUtils.zoom(createImgPath,createImgPath,140,140);
        }

        //保存入库.....
        userService.uploadimg(new File(createImgPath),userId);
        String contextPath = WebUtils.getWebUrlPath(request);

        String imgPath = contextPath+"/upload/"+imgName+"?"+System.currentTimeMillis();
        User user =  (User) httpSession.getAttribute(Globals.LOGIN_USER);
        user.setHreaderPath(imgPath);
        httpSession.setAttribute(Globals.LOGIN_USER,user);

        String format = "{\"fileUrl\":\"%s\"}";
        WebUtils.writeJson( response, String.format(format,imgPath));
    }


    @RequestMapping("/notice/view")
    public String log(HttpSession session, Model model, Page page, Long agentId, String sendTime) {
        model.addAttribute("agents", agentService.getAll());
        if (notEmpty(agentId)) {
            model.addAttribute("agentId", agentId);
        }
        if (notEmpty(sendTime)) {
            model.addAttribute("sendTime", sendTime);
        }
        homeService.getLog(session,page, agentId, sendTime);
        return "notice/view";
    }


    @RequestMapping("/notice/uncount")
    public void uncount(HttpSession session, HttpServletResponse response) {
         Long count = homeService.getUnReadCount(session);
         WebUtils.writeHtml(response,count.toString());
    }

    /**
     * 未读取的站类信
     * @param session
     * @param model
     * @return
     */
    @RequestMapping("/notice/unread")
    public String nuread(HttpSession session, Model model) {
        model.addAttribute("message",homeService.getUnReadMessage(session));
        return "notice/info";
    }

    @RequestMapping("/notice/detail")
    public String detail(Model model, Long logId) {
        model.addAttribute("sender", configService.getSysConfig().getSenderEmail());
        model.addAttribute("log", homeService.getLogDetail(logId));
        return "notice/detail";
    }
}

