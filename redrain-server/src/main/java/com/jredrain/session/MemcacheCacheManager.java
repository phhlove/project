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
import org.springframework.cache.support.AbstractCacheManager;

import java.util.Collection;

public class MemcacheCacheManager extends AbstractCacheManager {

    private Collection<Cache> caches;

    private MemcachedClient client;

    public MemcacheCacheManager() {
    }

    public MemcacheCacheManager(MemcachedClient client) {
        setClient(client);
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return this.caches;
    }

    public Cache getCache(String name) {
        if (client == null) {
            throw new IllegalStateException("MemcacheClient must not be null.");
        }
        Cache cache = super.getCache(name);
        if (cache == null) {
            cache = new MemcacheCache(client, name);
            addCache(cache);
        }
        return cache;
    }

    @SuppressWarnings("unused")
    private void updateCaches() {
        if (caches != null) {
            for (Cache cache : caches) {
                if (cache instanceof MemcacheCache) {
                    MemcacheCache memcacheCache = (MemcacheCache) cache;
                    memcacheCache.setClient(client);
                }
            }
        }
    }

    public Collection<Cache> getCaches() {
        return caches;
    }

    public void setCaches(Collection<Cache> caches) {
        this.caches = caches;
    }

    public MemcachedClient getClient() {
        return client;
    }

    public void setClient(MemcachedClient client) {
        this.client = client;
    }


}
