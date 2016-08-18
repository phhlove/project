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


package com.jredrain.session;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Session存储在Memcached上
 *
 */
@SuppressWarnings("all")
public class SessionStore implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SessionStore.class);

    private SerializingTranscoder serializingTranscoder = new SerializingTranscoder();

    private MemcachedClient client;

    private String hosts = null;

    private boolean isSaveSessionDataOnAttributeChange = false;

    public boolean isSaveSessionDataOnAttributeChange() {
        return isSaveSessionDataOnAttributeChange;
    }

    /**
     * @param isSaveSessionDataOnAttributeChange default is false
     */
    public void setSaveSessionDataOnAttributeChange(boolean isSaveSessionDataOnAttributeChange) {
        this.isSaveSessionDataOnAttributeChange = isSaveSessionDataOnAttributeChange;
    }

    public void afterPropertiesSet() throws Exception {
        if (client == null)
            client = new MemcachedClient(AddrUtil.getAddresses(hosts));
    }

    public void setSerializingTranscoder(SerializingTranscoder serializingTranscoder) {
        this.serializingTranscoder = serializingTranscoder;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public void setClient(MemcachedClient memcachedClient) {
        this.client = memcachedClient;
    }

    public void deleteSession(String sessionId) {
        Future<Boolean> future = client.delete(sessionId);
    }

    public Map getSession(String sessionId, int timeoutSeconds) {
        Map result = (Map) get(sessionId);
        if (result == null) {
            result = new HashMap();
        }
        return result;
    }

    private Object get(String sessionId) {
        return client.get(sessionId, serializingTranscoder);
    }

    public void saveSession(String sessionId, Map sessionData, int timeoutSeconds) {
        Future<Boolean> future = client.set(sessionId, timeoutSeconds, sessionData, serializingTranscoder);
    }

    public void onSetAttribute(String sessionId, String key, Map sessionData, int timeoutSeconds) {
        if (isSaveSessionDataOnAttributeChange) {
            saveSession(sessionId, sessionData, timeoutSeconds);
        }
    }

    public void onRemoveAttribute(String sessionId, String key, Map sessionData, int timeoutSeconds) {
        if (isSaveSessionDataOnAttributeChange) {
            saveSession(sessionId, sessionData, timeoutSeconds);
        }
    }

}
