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

import net.spy.memcached.MemcachedClient;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.util.Assert;


public class MemcacheCache implements Cache {

    private MemcachedClient client;

    private String name;

    private final int expire = 0;//永不过期

    public MemcacheCache() {
    }

    public MemcacheCache(MemcachedClient client, String name) {
        Assert.notNull(client, "Memcache client must not be null");
        this.client = client;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this.client;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object value = null;
        try {
            value = this.client.get(getKey(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (value != null ? new SimpleValueWrapper(value) : null);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper valueWrapper = this.get(key);
        return valueWrapper == null ? null : (T) valueWrapper.get();
    }

    @Override
    public void put(Object key, Object value) {
        if (value != null) {
            this.client.set(getKey(key), this.expire, value);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return null;
    }

    public void put(Object key, Object value, int exp) {
        if (value != null) {
            this.client.set(getKey(key), exp, value);
        }
    }

    @Override
    public void evict(Object key) {
        this.client.delete(getKey(key));
    }

    @Override
    public void clear() {
        this.client.flush();
    }

    private String toString(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof String) {
            return (String) object;
        } else {
            return object.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private String getKey(Object key) {
        return toString(key).toUpperCase();
    }

    public void setClient(MemcachedClient client) {
        this.client = client;
    }

    public MemcachedClient getClient() {
        return client;
    }

    public void setName(String name) {
        this.name = name;
    }

}