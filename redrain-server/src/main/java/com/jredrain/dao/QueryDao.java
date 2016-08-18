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


package com.jredrain.dao;

import com.jredrain.tag.Page;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by benjobs on 15/10/18.
 */
@Repository
public class QueryDao extends BaseDao {

    /**
     * 获取分页结果,
     * @param beanClass 支持任意Bean，按结果集映射
     * @return
     */
    public <E> Page<E> getPageBySql(Page<E> page, Class<E> beanClass, String sql, Object... parameters) {
        Query query = createSQLQuery(sql, parameters).setResultTransformer(BeanResultTransFormer.get(beanClass));
        pageQuery(query, page);

        //总记录数
        sql = preparedCount(sql);
        Long count = this.getCountBySql(sql, parameters);
        if (count == null) {
            count = 0L;
        }
        page.setTotalCount(count);
        return page;
    }

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    public static Page pageQuery(Query query, Page page) {
        page.setResult(pageQuery(query, page.getPageNo(), page.getPageSize()));
        return page;
    }

    private static String preparedCount(String sql) {

        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(sql);

        String tmpSql = sql.toLowerCase();
        while (matcher.find()) {
            String strFinded = matcher.group(1);
            String strReplace = strFinded.replace("from", "####");
            tmpSql = tmpSql.replace(strFinded, strReplace);
        }

        Pattern groupPattern = Pattern.compile(".from.*group\\s+{1,}by\\s+{1,}.*");
        Matcher groupMatcher = groupPattern.matcher(sql.toLowerCase());

        if (groupMatcher.find()) {
            sql = "select count(1) as total from ( " + sql + " ) as t ";
        } else {
            int startIndex = tmpSql.indexOf("select");
            int endIndex = tmpSql.indexOf(" from ");
            String repaceSql = sql.substring(startIndex + 6, endIndex);
            sql = sql.replace(repaceSql, " count(1) as total ");
        }
        return sql;
    }


}
