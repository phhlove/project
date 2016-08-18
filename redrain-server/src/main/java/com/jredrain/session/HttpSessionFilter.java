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

import com.jredrain.base.utils.CommonUtils;
import org.apache.commons.lang.StringUtils;
import com.jredrain.base.utils.CookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class HttpSessionFilter extends OncePerRequestFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpSessionFilter.class);

    private String sessionIdCookieName = "REDRAIN_SID";

    private SessionStore sessionStore;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        sessionStore = lookSessionStore();
    }

    protected SessionStore lookSessionStore() {
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        SessionStore store = wac.getBean("sessionStore", SessionStore.class);
        if (logger.isInfoEnabled()) {
            logger.info("Using '" + store.getClass().getSimpleName() + "' SessionStore for HttpSessionFilter");
        }
        return store;
    }

    public void setSessionIdCookieName(String sessionIdCookieName) {
        this.sessionIdCookieName = sessionIdCookieName;
    }

    public void setSessionStore(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String requestURL = request.getRequestURL().toString();
        String requestName = requestURL.substring(requestURL.lastIndexOf("/") + 1);
        requestName = requestName.toLowerCase();

        //过滤静态资源
        if (requestName.matches(".*\\.js$")
                || requestName.matches(".*\\.css$")
                || requestName.matches(".*\\.swf$")
                || requestName.matches(".*\\.jpg$")
                || requestName.matches(".*\\.png$")
                || requestName.matches(".*\\.jpeg$")
                || requestName.matches(".*\\.html$")
                || requestName.matches(".*\\.htm$")
                || requestName.matches(".*\\.xml$")
                || requestName.matches(".*\\.txt$")
                || requestName.matches(".*\\.ico$")
                ) {
            chain.doFilter(request, response);
            return;
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        Cookie sessionIdCookie = getOrGenerateSessionId(request, response);
        String sessionId = sessionIdCookie.getValue();

        HttpSession rawSession = request.getSession();

        Map sessionData = loadSessionData(sessionId, rawSession);
        try {
            HttpSession sessionWrapper = new HttpSessionStoreWrapper(rawSession, sessionStore, sessionId, sessionData);
            chain.doFilter(new HttpServletRequestSessionWrapper(request, sessionWrapper), response);
        } finally {
            try {
                String token = (String) sessionData.get("token");
                if (token != null) {
                    //登陆token
                    sessionId = token;
                    logger.info("login token=" + token);
                    sessionData.remove("token");
                }

                sessionStore.saveSession(sessionId, sessionData, rawSession.getMaxInactiveInterval());
            } catch (Exception e) {
                logger.warn("save session data error,cause:" + e, e);
            }
        }
    }

    private Map loadSessionData(String sessionId, HttpSession rawSession) {
        Map sessionData = null;
        try {
            sessionData = sessionStore.getSession(sessionId, rawSession.getMaxInactiveInterval());
        } catch (Exception e) {
            sessionData = new HashMap();
            logger.warn("load session data error,cause:" + e, e);
        }
        return sessionData;
    }

    private Cookie getOrGenerateSessionId(HttpServletRequest request,
                                          HttpServletResponse response) {
        Map<String, Cookie> cookieMap = CookieUtils.cookieToMap(request.getCookies());
        Cookie sessionIdCookie = cookieMap.get(sessionIdCookieName);
        if (sessionIdCookie == null || StringUtils.isEmpty(sessionIdCookie.getValue())) {
            sessionIdCookie = generateCookie(request, response);
        } else {
            //sessionIdCookie.setMaxAge(request.getSession().getMaxInactiveInterval() * 60 * 60 * 1000);
        }
        return sessionIdCookie;
    }

    private Cookie generateCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie sessionIdCookie;
        String sid = null;
        if (StringUtils.isBlank(sid)) {
            sid = CommonUtils.uuid();
        }
        sessionIdCookie = new Cookie(sessionIdCookieName, sid);

        String domain = request.getServerName();

        if (domain != null) {
            sessionIdCookie.setDomain(domain);
        }

        sessionIdCookie.setPath("/");
        response.addCookie(sessionIdCookie);
        return sessionIdCookie;
    }

}
