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

package com.jredrain.base.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

/**
 * @Package: cn.damai.notify.util
 * @Description: TODO
 * @author: Wanghuajie
 * @date: 13-5-22 - 下午2:39
 * @version: V1.0
 * @company: damai
 */
public class PropertyPlaceholder extends PropertyPlaceholderConfigurer {

    private static Properties properties = new Properties();

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            String value = props.getProperty(keyStr);
            properties.put(keyStr, value);
        }
    }

    public static Object getProperty(String name) {
        return properties.get(name);
    }

    public static String get(String name) {
        return (String) properties.get(name);
    }

    public static <T> T getProperty(String name, Class<T> clazz) {
        return (T) properties.get(name);
    }

    public static Properties getProperties() {
        return properties;
    }
}