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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JavaType;

import java.util.List;

public class JSON {

    public static JsonMapper nonEmptyMapper = new JsonMapper(Include.NON_EMPTY);


    public static String decode(Object object) {
        return nonEmptyMapper.toJson(object);
    }


    public static <T> T encode(String jsonString, Class<T> clazz) {
        return nonEmptyMapper.fromJson(jsonString, clazz);
    }

    /****
     * json转list
     * @param <T>
     * @param jsonString
     * @param clazz
     * @return
     */
    public static <T> List<T> jsonToList(String jsonString, Class<T> clazz) {
        JavaType javat = nonEmptyMapper.contructCollectionType(List.class, clazz);
        return nonEmptyMapper.fromJson(jsonString, javat);
    }
}
