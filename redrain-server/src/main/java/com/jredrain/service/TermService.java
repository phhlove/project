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
 *
 *
 */


package com.jredrain.service;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jredrain.base.utils.DigestUtils;
import com.jredrain.base.utils.HttpUtils;
import com.jredrain.dao.QueryDao;
import com.jredrain.domain.Term;
import com.jredrain.domain.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 *
 * @author <a href="mailto:benjobs@qq.com">benjobs@qq.com</a>
 * @name:CommonUtil
 * @version: 1.0.0
 * @company: com.jredrain
 * @description: webconsole核心类
 * @date: 2016-05-25 10:03<br/><br/>
 *
 * <b style="color:RED"></b><br/><br/>
 * 你快乐吗?<br/>
 * 风轻轻的问我<br/>
 * 曾经快乐过<br/>
 * 那时的湖面<br/>
 * 她踏着轻舟泛过<br/><br/>
 *
 * 你忧伤吗?<br/>
 * 雨悄悄的问我<br/>
 * 一直忧伤着<br/>
 * 此时的四季<br/>
 * 全是她的柳絮飘落<br/><br/>
 *
 * 你心痛吗?<br/>
 * 泪偷偷的问我<br/>
 * 心痛如刀割<br/>
 * 收到记忆的包裹<br/>
 * 都是她冰清玉洁还不曾雕琢<br/><br/>
 *
 * <hr style="color:RED"/>
 */

@Service
public class TermService {

    @Autowired
    private QueryDao queryDao;

    @Autowired
    private ConfigService configService;

    private Map<UUID, SocketIOClient> clients = new HashMap<UUID, SocketIOClient>(0);

    private Map<Long, Integer> sessionMap = new HashMap<Long, Integer>(0);

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Term getTerm(Long userId, String host) {
        return queryDao.sqlUniqueQuery(Term.class,"SELECT * FROM term WHERE userId=? AND host=? And status=1",userId,host);
    }

    public boolean saveOrUpdate(Term term) {

        Term dbTerm = queryDao.sqlUniqueQuery(Term.class,"SELECT * FROM term WHERE userId=? AND host=?",term.getUserId(),term.getHost());
        if (dbTerm!=null) {
            term.setTermId(dbTerm.getTermId());
        }

        try {
            queryDao.save(term);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getTermUrl(HttpServletRequest request, Agent agent) {
        /**
         * 创建一个用户连接websocket的空闲端口,存起来
         */
        final Long agentId = agent.getAgentId();

        if (sessionMap.get(agentId) == null) {
            final int port = HttpUtils.generagePort();
            this.sessionMap.put(agentId, port);

            /**
             * websocket server start......
             */
            Configuration configuration = new Configuration();
            configuration.setPort(port);
            final SocketIOServer server = new SocketIOServer(configuration);

            server.addEventListener("login", String.class, new DataListener<String>() {
                @Override
                public void onData(final SocketIOClient client, String str, AckRequest ackRequest) throws Exception {
                    /**
                     * 确保安全性,从数据库获取解密的私key,进行AES解密
                     */
                    String key = configService.getAeskey();
                    String jsonTerm = DigestUtils.aesDecrypt(key,str);
                    Term term = JSON.parseObject(jsonTerm,Term.class);
                    Session jschSession = createJschSession(term);
                    logger.info("[redrain]:SSHServer connected:SessionId @ {},port @ {}", client.getSessionId(), port);
                    clients.put(client.getSessionId(), client);
                    try {
                        jschSession.connect();
                        //登录成功,进入console
                        client.sendEvent("console", new VoidAckCallback() {
                            @Override
                            protected void onSuccess() {
                                System.out.println("ack from client: " + client.getSessionId());
                            }
                        }, "登录成功!webssh功能正在开发中,敬请期待");

                    } catch (JSchException e) {
                        /**
                         * ssh 登录失败
                         */
                        term.setStatus(0);
                        saveOrUpdate(term);
                        ackRequest.sendAckData(termFailCause(e));
                        logger.info("[redrain]:SSHServer connect error:", e.getLocalizedMessage());
                        server.stop();
                        sessionMap.remove(agentId);
                    }

                }
            });

            server.addDisconnectListener(new DisconnectListener() {
                @Override
                public void onDisconnect(SocketIOClient client) {
                    clients.remove(client.getSessionId());
                    if (clients.isEmpty()) {
                        sessionMap.remove(agentId);
                        logger.info("[redrain]:SSHServer disconnect:SessionId @ {},port @ {} ", client.getSessionId(), port);
                        server.stop();
                    }
                }
            });
            server.start();
            logger.debug("[redrain] SSHServer started @ {}", port);
        }

        return String.format("http://%s:%s",request.getServerName(),sessionMap.get(agentId));
    }

    public Session createJschSession(Term term) throws JSchException {
        JSch jsch = new JSch();
        Session jschSession = jsch.getSession(term.getUser(), term.getHost(), term.getPort());
        jschSession.setPassword(term.getPassword());

        java.util.Properties config = new java.util.Properties();
        //不记录本次登录的信息到$HOME/.ssh/known_hosts
        config.put("StrictHostKeyChecking", "no");
        jschSession.setConfig(config);
        return jschSession;
    }

    public String termFailCause(JSchException e) {
        if (e.getLocalizedMessage().equals("Auth fail")) {
            return "authfail";
        }else if(e.getLocalizedMessage().contentEquals("timeout")){
            return "timeout";
        }

        return e.getLocalizedMessage();
    }

}
