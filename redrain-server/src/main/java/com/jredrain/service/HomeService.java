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

import com.jredrain.dao.QueryDao;
import com.jredrain.base.utils.Digests;
import com.jredrain.base.utils.Encodes;
import com.jredrain.domain.Log;
import com.jredrain.domain.User;
import com.jredrain.job.Globals;
import com.jredrain.tag.Page;
import com.jredrain.vo.LogVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;

import java.util.List;

import static com.jredrain.base.utils.CommonUtils.notEmpty;

/**
 * Created by ChenHui on 2016/2/17.
 */
@Service
public class HomeService {

    @Autowired
    private QueryDao queryDao;

    public int checkLogin(HttpSession httpSession, String username, String password) {
        //将session置为无效
        if (!httpSession.isNew()) {
            httpSession.invalidate();
            httpSession.removeAttribute(Globals.LOGIN_USER);
        }

        User user = queryDao.hqlUniqueQuery("FROM User WHERE userName = ?", username);
        if (user == null) return 500;

        byte[] salt = Encodes.decodeHex(user.getSalt());
        byte[] hashPassword = Digests.sha1(password.getBytes(), salt, 1024);
        password = Encodes.encodeHex(hashPassword);

        String sql = "SELECT COUNT(1) FROM user WHERE userName=? AND password=?";
        Long count = queryDao.getCountBySql(sql, username, password);

        if (count == 1L) {
            httpSession.setAttribute(Globals.LOGIN_USER, user);
            if (user.getRoleId() == 999L) {
                httpSession.setAttribute("permission", true);
            } else {
                httpSession.setAttribute("permission", false);
            }
            return 200;
        } else {
            return 500;
        }
    }

    public Page<LogVo> getLog(HttpSession session, Page page, Long agentId, String sendTime) {
        String sql = "SELECT L.*,w.name AS agentName FROM log L LEFT JOIN agent w ON L.agentId = w.agentId WHERE 1=1 ";
        if (notEmpty(agentId)) {
            sql += " AND L.agentId = " + agentId;
        }
        if (notEmpty(sendTime)) {
            sql += " AND L.sendTime like '" + sendTime + "%' ";
        }
        if (!(Boolean) session.getAttribute("permission")) {
            sql += " AND L.receiverId = " + ((User)session.getAttribute(Globals.LOGIN_USER)).getUserId();
        }
        sql += " ORDER BY L.sendTime DESC";
        queryDao.getPageBySql(page, LogVo.class, sql);
        return page;
    }

    public List<LogVo> getUnReadMessage(HttpSession session) {
        String sql = "SELECT * FROM log L WHERE isread=0 and type=2 ";
        if (!(Boolean) session.getAttribute("permission")) {
            sql += " and L.receiverId = " + ((User)session.getAttribute(Globals.LOGIN_USER)).getUserId();
        }
        sql += " ORDER BY L.sendTime DESC LIMIT 5";
        return queryDao.sqlQuery(LogVo.class,sql);
    }

    public Long getUnReadCount(HttpSession session) {
        String sql = "SELECT count(1) FROM log L WHERE isread=0 and type=2 ";
        if (!(Boolean) session.getAttribute("permission")) {
            sql += " and L.receiverId = " + ((User)session.getAttribute(Globals.LOGIN_USER)).getUserId();
        }
        return queryDao.getCountBySql(sql);
    }


    public void saveLog(Log log) {
        queryDao.save(log);
    }

    public Log getLogDetail(Long logId) {
        return queryDao.get(Log.class,logId);
    }
}
