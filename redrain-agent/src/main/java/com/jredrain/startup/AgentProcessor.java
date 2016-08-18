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


package com.jredrain.startup;

import com.alibaba.fastjson.JSON;
import com.jredrain.base.job.*;
import org.apache.commons.exec.*;
import org.apache.thrift.TException;
import com.jredrain.base.utils.*;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import static com.jredrain.base.utils.CommonUtils.*;

/**
 * Created by benjo on 2016/3/25.
 */
public class AgentProcessor implements RedRain.Iface {

    private Logger logger = LoggerFactory.getLogger(AgentProcessor.class);

    private String password;

    private Integer agentPort;

    private Integer socketPort;

    private final String EXITCODE_KEY = "exitCode";

    private final String EXITCODE_SCRIPT = String.format(" || echo %s:$?", EXITCODE_KEY);

    private AgentMonitor agentMonitor;

    public AgentProcessor(String password, Integer agentPort) {
        this.password = password;
        this.agentPort = agentPort;
    }

    @Override
    public Response ping(Request request) throws TException {
        if (!this.password.equalsIgnoreCase(request.getPassword())) {
            return errorPasswordResponse(request);
        }
        return Response.response(request).setSuccess(true).setExitCode(RedRain.StatusCode.SUCCESS_EXIT.getValue()).end();
    }

    @Override
    public Response monitor(Request request) throws TException {
        RedRain.ConnType connType = RedRain.ConnType.getByName(request.getParams().get("connType"));
        Response response = Response.response(request);
        Map<String,String> map = new HashMap<String, String>(0);

        if (agentMonitor==null) {
            agentMonitor = new AgentMonitor();
        }

        switch (connType) {
            case CONN:
                if (  CommonUtils.isEmpty(agentMonitor,socketPort) || agentMonitor.stoped() ) {
                    //选举一个空闲可用的port
                    do {
                        this.socketPort = HttpUtils.generagePort();
                    }while (this.socketPort == this.agentPort);
                    try {
                        agentMonitor.start(socketPort);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                logger.debug("[redrain]:getMonitorPort @:{}", socketPort);
                map.put("port",this.socketPort.toString());
                response.setResult(map);
                return response;
            case PROXY:
                Monitor monitor = agentMonitor.monitor();
                map = serializableToMap(monitor);
                response.setResult(map);
                return response;
            default:
                return null;
        }
    }

    @Override
    public Response proxy(Request request) throws TException {
        String proxyHost = request.getParams().get("proxyHost");
        String proxyPort = request.getParams().get("proxyPort");
        String proxyAction = request.getParams().get("proxyAction");
        String proxyPassword = request.getParams().get("proxyPassword");

        //其他参数....
        String proxyParams = request.getParams().get("proxyParams");
        Map<String,String> params = new HashMap<String, String>(0);
        if (CommonUtils.notEmpty(proxyParams)) {
            params = (Map<String, String>) JSON.parse(proxyParams);
        }

        Request proxyReq = Request.request(proxyHost,toInt(proxyPort), Action.findByName(proxyAction),proxyPassword).setParams(params);

        logger.info("[redrain]proxy params:{}",proxyReq.toString());

        TTransport transport = null;
        /**
         * ping的超时设置为5毫秒,其他默认
         */
        if (proxyReq.getAction().equals(Action.PING)) {
            transport = new TSocket(proxyReq.getHostName(),proxyReq.getPort(),1000*5);
        }else {
            transport = new TSocket(proxyReq.getHostName(),proxyReq.getPort());
        }
        TProtocol protocol = new TBinaryProtocol(transport);
        RedRain.Client client = new RedRain.Client(protocol);
        transport.open();

        Response response = null;
        for(Method method:client.getClass().getMethods()){
            if (method.getName().equalsIgnoreCase(proxyReq.getAction().name())) {
                try {
                    response = (Response) method.invoke(client, proxyReq);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        transport.flush();
        transport.close();
        return response;
    }

    @Override
    public Response execute(Request request) throws TException {
        if (!this.password.equalsIgnoreCase(request.getPassword())) {
            return errorPasswordResponse(request);
        }

        String command = request.getParams().get("command") + EXITCODE_SCRIPT;

        String pid = request.getParams().get("pid");

        logger.info("[redrain]:execute:{},pid:{}", command, pid);

        File shellFile = CommandUtils.createShellFile(command, pid);

        Integer exitValue = 1;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Response response = Response.response(request);
        try {
            CommandLine commandLine = CommandLine.parse("/bin/bash +x " + shellFile.getAbsolutePath());
            DefaultExecutor executor = new DefaultExecutor();

            ExecuteStreamHandler stream = new PumpStreamHandler(outputStream, outputStream);
            executor.setStreamHandler(stream);
            response.setStartTime(new Date().getTime());
            exitValue = executor.execute(commandLine);
            exitValue = exitValue == null ? 0 : exitValue;
        } catch (Exception e) {
            if (e instanceof ExecuteException) {
                exitValue = ((ExecuteException) e).getExitValue();
            } else {
                exitValue = RedRain.StatusCode.ERROR_EXEC.getValue();
            }
            if (RedRain.StatusCode.KILL.getValue().equals(exitValue)) {
                logger.info("[redrain]:job has be killed!at pid :{}", request.getParams().get("pid"));
            } else {
                logger.info("[redrain]:job execute error:{}", e.getCause().getMessage());
            }
        } finally {
            if (outputStream != null) {
                String text = outputStream.toString();
                if (notEmpty(text)) {
                    try {
                        response.setMessage(text.substring(0, text.lastIndexOf(EXITCODE_KEY)));
                        response.setExitCode(Integer.parseInt(text.substring(text.lastIndexOf(EXITCODE_KEY) + EXITCODE_KEY.length() + 1).trim()));
                    } catch (IndexOutOfBoundsException e) {
                        response.setMessage(text);
                        response.setExitCode(exitValue);
                    } catch (NumberFormatException e) {
                        response.setExitCode(exitValue);
                    }
                } else {
                    response.setExitCode(exitValue);
                }
                try {
                    outputStream.close();
                } catch (Exception e) {
                    logger.error("[redrain]:error:{}", e);
                }

                /**
                 * 修复脚本里执行ssh可能连接超时导致任务失败的bug..
                 */
                String timeoutRegex = "^ssh:.+Connection\\stimed\\sout$";
                if (response.getMessage().matches(timeoutRegex)) {
                    //连接超时...
                    response.setExitCode(RedRain.StatusCode.TIME_OUT.getValue());
                }
            } else {
                response.setExitCode(exitValue);
            }
            response.setSuccess(response.getExitCode() == RedRain.StatusCode.SUCCESS_EXIT.getValue()).end();
            if (shellFile != null) {
                shellFile.delete();//删除文件
            }
        }
        logger.info("[redrain]:execute result:{}", response.toString());
        return response;
    }

    @Override
    public Response password(Request request) throws TException {
        if (!this.password.equalsIgnoreCase(request.getPassword())) {
            return errorPasswordResponse(request);
        }

        String newPassword = request.getParams().get("newPassword");
        Response response = Response.response(request);

        if (isEmpty(newPassword)) {
            return response.setSuccess(false).setExitCode(RedRain.StatusCode.SUCCESS_EXIT.getValue()).setMessage("密码不能为空").end();
        }
        this.password = newPassword.toLowerCase().trim();

        IOUtils.writeText(Globals.REDRAIN_PASSWORD_FILE, this.password, "UTF-8");

        return response.setSuccess(true).setExitCode(RedRain.StatusCode.SUCCESS_EXIT.getValue()).end();
    }

    @Override
    public Response kill(Request request) throws TException {

        if (!this.password.equalsIgnoreCase(request.getPassword())) {
            return errorPasswordResponse(request);
        }

        String pid = request.getParams().get("pid");
        logger.info("[redrain]:kill pid:{}", pid);

        Response response = Response.response(request);
        String text = CommandUtils.executeShell(Globals.REDRAIN_KILL_SHELL, request.getParams().get("pid"), EXITCODE_SCRIPT);
        String message = "";
        Integer exitVal = 0;

        if (notEmpty(text)) {
            try {
                message = text.substring(0, text.lastIndexOf(EXITCODE_KEY));
                exitVal = Integer.parseInt(text.substring(text.lastIndexOf(EXITCODE_KEY) + EXITCODE_KEY.length() + 1).trim());
            } catch (StringIndexOutOfBoundsException e) {
                message = text;
            }
        }

        response.setExitCode(RedRain.StatusCode.ERROR_EXIT.getValue().equals(exitVal) ? RedRain.StatusCode.ERROR_EXIT.getValue() : RedRain.StatusCode.SUCCESS_EXIT.getValue())
                .setMessage(message)
                .end();

        logger.info("[redrain]:kill result:{}" + response);
        return response;
    }

    private Response errorPasswordResponse(Request request) {
        return Response.response(request)
                .setSuccess(false)
                .setExitCode(RedRain.StatusCode.ERROR_PASSWORD.getValue())
                .setMessage(RedRain.StatusCode.ERROR_PASSWORD.getDescription())
                .end();
    }

    private Map<String, String> serializableToMap(Object obj) {
        if (isEmpty(obj))
            return Collections.EMPTY_MAP;
        Map<String, String> resultMap = new HashMap<String, String>(0);
        // 拿到属性器数组
        try {
            PropertyDescriptor[] pds = Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors();
            for (int index = 0; pds.length > 1 && index < pds.length; index++) {
                if (Class.class == pds[index].getPropertyType() || pds[index].getReadMethod() == null) {

                    continue;
                }
                Object value = pds[index].getReadMethod().invoke(obj);
                if (notEmpty(value)) {
                    if (isPrototype(pds[index].getPropertyType())//java里的原始类型(去除自己定义类型)
                            || pds[index].getPropertyType().isPrimitive()//基本类型
                            || ReflectUitls.isPrimitivePackageType(pds[index].getPropertyType())
                            || pds[index].getPropertyType() == String.class) {

                        resultMap.put(pds[index].getName(), value.toString());
                    }else {
                        resultMap.put(pds[index].getName(), JSON.toJSONString(value));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }


}
