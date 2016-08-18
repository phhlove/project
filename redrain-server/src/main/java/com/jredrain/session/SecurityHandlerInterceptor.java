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


import com.jredrain.domain.User;
import com.jredrain.job.Globals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登陆权限拦截器
 */
@Repository
public class SecurityHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityHandlerInterceptor.class);

    public boolean preHandle(HttpServletRequest request,HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        String requestURI = request.getContextPath() + request.getServletPath();

        //静态资源,页面
        if ( requestURI.contains("/css/")
                || requestURI.contains("/fonts/")
                || requestURI.contains("/img/")
                || requestURI.contains("/js/")
                || requestURI.contains("/WEB-INF") ) {
            return super.preHandle(request, response, handler);
        }

        //登陆|上传
        if (requestURI.contains("/login")||requestURI.contains("/upload")) {
            return super.preHandle(request, response, handler);
        }

        User user = (User) session.getAttribute(Globals.LOGIN_USER);
        if (user == null) {
            logger.info(request.getRequestURL().toString());
            //跳到登陆页面
            response.sendRedirect("/");
            logger.info("session is null,redirect to login page");
            return false;
        }
        //普通管理员不可访问的资源
        if (!(Boolean) session.getAttribute("permission") &&
                (requestURI.contains("/config/")
                        || requestURI.contains("/user/view")
                        || requestURI.contains("/user/add")
                        || requestURI.contains("/agent/add")
                        || requestURI.contains("/agent/edit"))) {
            logger.info("illegal or limited access");
            return false;
        }
        return super.preHandle(request, response, handler);
    }

}
