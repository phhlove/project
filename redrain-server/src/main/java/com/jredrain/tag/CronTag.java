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


package com.jredrain.tag;

import org.apache.commons.lang3.StringUtils;
import com.jredrain.base.utils.DateUtils;
import com.jredrain.base.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Administrator on 14-3-25.
 */
public class CronTag {

    public static String currDayPrevDay(int day) {
        return DateUtils.getCurrDayPrevDay(day);
    }

    /**
     * 获取指定日期的后N天
     * @param date
     * @param n
     * @return
     */
    public static String getNextDay(String date, int n) {
        return DateUtils.getNextDay(date, n);
    }

    /**
     * 获取指定日期的前N天
     * @param date
     * @param n
     * @return
     */
    public static String getPrevDay(String date, int n) {
        return DateUtils.getPrevDay(date, n);
    }

    public static String substr(Object obj, int index, int end) {
        return substr(obj, index, end, "");
    }

    public static String substr(Object obj, int index, int end, String tempStr) {
        if (CommonUtils.isEmpty(obj))
            return "";
        String str = obj.toString();
        if (obj instanceof Date) {
            str = DateUtils.formatSimpleDate((Date) obj);
        } else {
            str = str + "";
        }
        int len = str.length();
        if (len > end && index < end) {
            return str.substring(index, end) + tempStr;
        }
        return str;
    }


    public static String subwithstr(Object obj, int start, String strx, int index) {
        if (CommonUtils.isEmpty(obj))
            return "";
        String str = obj.toString();
        if (obj instanceof Date) {
            str = DateUtils.formatSimpleDate((Date) obj);
        } else {
            str = str + "";
        }
        int pos = str.indexOf(strx);
        if (pos + index < 0) {
            return str;
        }
        return str.substring(start, str.indexOf(strx) + index);
    }

    public static String getDate(String date, String format) {
        // "%Y年%m月%s日";
        if (CommonUtils.isEmpty(date)) return "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            return simpleDateFormat.format(DateUtils.parseSimpleDate(date));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    public static String diffdate(Date date1, Date date2) {
        if (date1 == null || date2 == null) return "0";
        long durationMillisecond = Math.abs(date1.getTime() - date2.getTime());
        long day = durationMillisecond / (24 * 60 * 60 * 1000);
        long hour = (durationMillisecond / (60 * 60 * 1000) - day * 24);
        long min = ((durationMillisecond / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (durationMillisecond / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        StringBuilder ret = new StringBuilder();
        if (day > 0L) {
            ret.append(day).append("天");
        }
        if (hour > 0L) {
            ret.append(hour).append("时");
        }
        if (min > 0L) {
            ret.append(min).append("分");
        }
        if (second > 0L) {
            ret.append(second).append("秒");
        }
        if (ret.toString().equals("")) {
            ret.append("<1秒");
        }
        return ret.toString();
    }

    public static String repinx(Object obj, String str, String replaceStr, int count, int index) {
        if (CommonUtils.isEmpty(obj)) return "";
        if (CommonUtils.isEmpty(str) || count <= 0) return obj.toString();
        String args = obj.toString();
        if (!args.contains(str)) return args;
        int ops = args.indexOf(str);
        if (index < 0) {//符号前
            //fafdsafdsasd@qq.com
            if (count > ops) {
                String temp = "";
                for (int j = 0; j < ops; j++) {
                    temp += replaceStr;
                }
                return temp + args.substring(ops);
            } else {
                String str1 = args.substring(0, ops - count);
                String temp = "";
                for (int j = 0; j < count; j++) {
                    temp += replaceStr;
                }
                String str3 = args.substring(ops);
                return str1 + temp + str3;
            }
        } else {
            if (count > ops) {
                String temp = "";
                for (int j = 0; j < args.length() - ops - 1; j++) {
                    temp += replaceStr;
                }
                return args.substring(0, ops + 1) + temp;
            } else {
                //fdafdafsda@fdsafdsaf
                String str1 = args.substring(0, ops + 1);
                String temp = "";
                for (int j = 0; j < count; j++) {
                    temp += replaceStr;
                }
                String str3 = args.substring(ops + count + 1);
                return str1 + temp + str3;
            }
        }

    }

    public static String shiftEnter(String str) {
        String newStr = str.replaceAll("\r\n", "<br>"); // 替换回车换行符
        newStr = newStr.replaceAll(" ", "&nbsp;");// 替换空格符
        return newStr;
    }

    /**
     * 将lonng型time格式化
     * @param value
     * @return
     */
    public static String longtimeToDate(String value) {
        String s = "";
        if (StringUtils.isNotBlank(value)) {
            long time = Long.valueOf(value);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time);
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            s = dateformat.format(c.getTime());
        }

        return s;
    }

    /**
     * 替换手机号中间位数
     * @param str
     * @return
     */
    public static String StrReplaceAll(String str) {
        if (StringUtils.isNotBlank(str) && str.length() >= 11) {
            return str.substring(0, 3) + "****" + str.substring(7);
        } else {
            return str;
        }
    }

    /**
     * 将xxxx-xx字符串替换为xxxx年xx月
     * @param str
     * @return
     */
    public static String strToDateStr(String str) {
        if (StringUtils.isNotBlank(str)) {
            str = str.replace("-", "年");
            str += "月";
            return str;
        } else {
            return str;
        }
    }

    /**
     * 截取code前4位和后4位，中间用*代替
     * @param code
     * @param len（*号显示数量）
     * @return
     * @author yaohu
     */
    public static String strToCardCode(String code, int len) {
        String ret = code;
        if (StringUtils.isNotBlank(code) && code.length() > 8) {
            String str = "";
            if (len <= 0) {
                len = code.length() - 8;
            }
            for (int i = 0; i < len; i++) {
                str += "*";
            }
            ret = code.substring(0, 4) + str + code.substring(code.length() - 4, code.length());
        }
        return ret;
    }

    public static void main(String[] args) {
        System.out.println(substr("201511071755326105", 0, 12, "..."));
    }
}
