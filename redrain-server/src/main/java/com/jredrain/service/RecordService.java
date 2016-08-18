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
import com.jredrain.base.job.RedRain;
import com.jredrain.domain.User;
import com.jredrain.job.Globals;
import com.jredrain.tag.Page;
import com.jredrain.domain.Record;
import com.jredrain.vo.ChartVo;
import com.jredrain.vo.RecordVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.List;

import static com.jredrain.base.utils.CommonUtils.notEmpty;

@Service
public class RecordService {

    @Autowired
    private QueryDao queryDao;


    public Page query(HttpSession session, Page<RecordVo> page, RecordVo recordVo, String queryTime, boolean status) {
        String sql = "SELECT r.recordId,r.jobId,r.command,r.success,r.startTime,r.status,r.redoCount,r.category,r.flowGroup,CASE WHEN r.status IN (1,3) THEN r.endTime WHEN r.status IN (0,2) THEN NOW() END AS endTime,r.execType,t.jobName,t.agentId,d.name AS agentName,d.password,d.ip,t.cronExp,u.userName AS operateUname FROM record r INNER JOIN job t ON r.jobId = t.jobId "
                + " LEFT JOIN agent d ON t.agentId = d.agentId LEFT JOIN user AS u ON t.operateId = u.userId AND CASE r.category WHEN 1 THEN r.flowNum=0 WHEN 0 THEN r.parentId IS NULL END WHERE r.parentId is NULL AND r.status IN " + (status ? "(1,3)" : "(0,2)");
        if (recordVo != null) {
            if (notEmpty(recordVo.getSuccess())) {
                sql += " AND r.success = " + recordVo.getSuccess() + "";
            }
            if (notEmpty(recordVo.getAgentId())) {
                sql += " AND d.agentId = " + recordVo.getAgentId() + " ";
            }
            if (notEmpty(recordVo.getJobId())) {
                sql += " AND r.jobId = " + recordVo.getJobId() + " ";
            }
            if (notEmpty(queryTime)) {
                sql += " AND r.startTime like '" + queryTime + "%' ";
            }
            if (notEmpty(recordVo.getExecType())) {
                sql += " AND r.execType = " + recordVo.getExecType() + " ";
            }
            if (status) {
                sql += " AND IFNULL(r.flowNum,0) = 0 ";
            }
            if (!(Boolean) session.getAttribute("permission")) {
                sql += " AND t.operateId = " + ((User)session.getAttribute(Globals.LOGIN_USER)).getUserId();
            }
        }
        sql += " ORDER BY r.startTime DESC";
        queryDao.getPageBySql(page, RecordVo.class, sql);

        if (status) {
            //已完成任务的子任务及重跑记录查询
            queryChildrenAndRedo(page, sql);
        }
        return page;
    }

    private void queryChildrenAndRedo(Page<RecordVo> page, String sql) {
        List<RecordVo> parentRecords = page.getResult();
        for (RecordVo parentRecord : parentRecords) {
            //单一任务有重跑记录的，查出后并把最后一条重跑记录的执行结果记作整个任务的成功、失败状态
            if (parentRecord.getCategory() == 0 && parentRecord.getRedoCount() > 0) {
                sql = "SELECT r.recordId,r.jobId,r.category,r.startTime,r.endTime,r.execType,r.status,r.redoCount,r.command,r.success,t.jobName,d.name AS agentName,d.password,d.ip,t.cronExp,u.userName AS operateUname FROM record r INNER JOIN job t ON r.jobId = t.jobId LEFT JOIN agent d ON t.agentId = d.agentId LEFT JOIN user AS u ON t.operateId = u.userId WHERE r.parentId = ? ORDER BY r.startTime ASC ";
                List<RecordVo> records = queryDao.sqlQuery(RecordVo.class, sql, parentRecord.getRecordId());
                parentRecord.setSuccess(records.get(records.size() - 1).getSuccess());
                parentRecord.setChildRecord(records);
            }
            //流程任务，先查出父任务的重跑记录，再查出各个子任务，最后查询子任务的重跑记录，并以最后一条记录的执行结果记作整个流程任务的成功、失败状态
            if (parentRecord.getCategory() == 1) {
                if (parentRecord.getRedoCount() != 0) {
                    List<RecordVo> records = queryDao.sqlQuery(RecordVo.class, sql, parentRecord.getRecordId());
                    //流程任务不能保证子任务也有记录，先给父任务一个成功、失败状态
                    parentRecord.setSuccess(RedRain.ResultStatus.FAILED.getStatus());
                    parentRecord.setChildRecord(records);
                }
                sql = "SELECT r.recordId,r.jobId,r.category,r.startTime,r.endTime,r.execType,r.status,r.redoCount,r.command,r.success,r.flowGroup,t.jobName,t.lastFlag,d.name as agentName,d.password,d.ip,t.cronExp,u.userName AS operateUname FROM record r INNER JOIN job t ON r.jobId = t.jobId LEFT JOIN agent d ON t.agentId = d.agentId LEFT JOIN user AS u on t.operateId = u.userId WHERE r.parentId IS NULL AND r.flowGroup = ? AND r.flowNum > 0 ORDER BY r.flowNum ASC ";
                List<RecordVo> childJobs = queryDao.sqlQuery(RecordVo.class, sql, parentRecord.getFlowGroup());
                if (notEmpty(childJobs)) {
                    parentRecord.setChildJob(childJobs);
                    for (RecordVo childJob : parentRecord.getChildJob()) {
                        if (childJob.getRedoCount() > 0) {
                            sql = "SELECT r.recordId,r.jobId,r.category,r.startTime,r.endTime,r.execType,r.status,r.redoCount,r.command,r.success,r.parentId,t.jobName,d.name AS agentName,d.password,d.ip,t.cronExp,u.userName AS operateUname FROM record r INNER JOIN job t ON r.jobId = t.jobId LEFT JOIN agent d ON t.agentId = d.agentId LEFT JOIN user AS u ON t.operateId = u.userId WHERE r.parentId = ?  ORDER BY r.startTime ASC ";
                            List<RecordVo> childRedo = queryDao.sqlQuery(RecordVo.class, sql, childJob.getRecordId());
                            childJob.setChildRecord(childRedo);
                        }

                    }
                    //判断整个流程任务最终执行的成功、失败状态
                    RecordVo lastJob = childJobs.get(childJobs.size() - 1);
                    if (lastJob.getLastFlag()) {
                        if (notEmpty(lastJob.getChildRecord())) {
                            parentRecord.setSuccess(lastJob.getChildRecord().get(lastJob.getChildRecord().size() - 1).getSuccess());
                        } else {
                            parentRecord.setSuccess(lastJob.getSuccess());
                        }
                    } else {
                        parentRecord.setSuccess(RedRain.ResultStatus.FAILED.getStatus());
                    }
                }
            }
        }
        page.setResult(parentRecords);
    }

    public RecordVo getDetailById(Long id) {
        return queryDao.sqlUniqueQuery(RecordVo.class, "SELECT r.recordId,r.category,r.jobId,r.startTime,r.endTime,r.execType,r.returnCode,r.message,r.redoCount,r.command,r.success,t.jobName,t.agentId,d.name AS agentName,d.password,d.ip,t.cronExp,t.operateId,u.userName AS operateUname FROM record r LEFT JOIN job t ON r.jobId = t.jobId LEFT JOIN agent d ON t.agentId = d.agentId LEFT JOIN user AS u ON t.operateId = u.userId WHERE r.recordId = ?", id);
    }

    public void update(Record record) {
        queryDao.save(record);
    }

    public Record save(Record record) {
        return (Record) queryDao.save(record);
    }

    public Record get(Long recordId) {
        return queryDao.get(Record.class, recordId);
    }

    public List<Record> getReExecuteRecord() {
        String sql = "SELECT r.*,t.cronExp,d.ip,d.`name` AS agentName,d.password FROM record r INNER JOIN job t ON r.`success`=0 AND r.category=0 AND r.status = 1 AND r.parentId IS NULL AND r.jobId = t.jobId AND t.redo=1 AND r.redoCount<t.runCount INNER JOIN agent d ON t.agentId = d.agentId AND t.status=1";
        return queryDao.sqlQuery(Record.class, sql);
    }

    public ChartVo getExecTypePieData() {
        String sql = "SELECT a.count AS crontab,b.count AS operator,c.count AS rerun FROM (SELECT COUNT(1) count FROM record WHERE execType = 0)a,(SELECT COUNT(1) count FROM record WHERE execType = 1)b,(SELECT COUNT(1) count FROM record WHERE execType IN (2,3))c ";
        return queryDao.sqlUniqueQuery(ChartVo.class, sql);
    }

    public Object getStatusDonutData() {
        String sql = "SELECT a.count AS success,b.count AS failure,c.count AS killed FROM (SELECT COUNT(1) count FROM record WHERE success = 1)a,(SELECT COUNT(1) count FROM record WHERE success = 0)b,(SELECT COUNT(1) count FROM record WHERE success = 2)c ";
        return queryDao.sqlUniqueQuery(ChartVo.class, sql);
    }

    public Boolean isRunning(Long id) {
        return queryDao.getCountBySql("SELECT COUNT(1) FROM record r LEFT JOIN job t ON r.jobId = t.jobId  WHERE (r.jobId = ? OR t.flowId = ?) AND r.status = 0 ", id, id) > 0L;
    }

    public List<ChartVo> getDiffData(String startTime, String endTime) {
        String sql = "SELECT DATE_FORMAT(r.startTime,'%Y-%m-%d') AS date, " +
                " sum(CASE r.success WHEN 0 THEN 1 ELSE 0 END) failure," +
                " sum(CASE r.success WHEN 1 THEN 1 ELSE 0 END) success," +
                " sum(CASE r.success WHEN 2 THEN 1 ELSE 0 END) killed, " +
                " sum(CASE r.category WHEN 0 THEN 1 ELSE 0 END) singleton,"+
                " sum(CASE r.category WHEN 1 THEN 1 ELSE 0 END) flow,"+
                " sum(CASE j.cronType WHEN 0 THEN 1 ELSE 0 END) crontab,"+
                " sum(CASE j.cronType WHEN 1 THEN 1 ELSE 0 END) quartz,"+
                " sum(CASE r.execType WHEN 0 THEN 1 ELSE 0 END) auto,"+
                " sum(CASE r.execType WHEN 1 THEN 1 ELSE 0 END) operator,"+
                " sum(CASE r.redoCount>0 WHEN 1 THEN 1 ELSE 0 END) rerun"+
                " FROM record r left join job j ON r.jobid=j.jobid "+
                " WHERE DATE_FORMAT(r.startTime,'%Y-%m-%d') BETWEEN '" + startTime + "' AND '" + endTime + "' GROUP BY DATE_FORMAT(r.startTime,'%Y-%m-%d') ORDER BY DATE_FORMAT(r.startTime,'%Y-%m-%d') ASC";
        return queryDao.sqlQuery(ChartVo.class, sql);
    }

    @Transactional(readOnly = false)
    public void flowJobDone(Record record) {
        String sql = "update record set status=? where flowGroup=?";
        queryDao.createSQLQuery(sql, RedRain.RunStatus.DONE.getStatus(), record.getFlowGroup()).executeUpdate();
    }

    public List<Record> getRunningFlowJob(Long recordId) {
        String sql = "SELECT r.* FROM record r INNER JOIN (SELECT flowGroup FROM record WHERE recordId=?) AS t WHERE r.flowGroup = t.flowGroup";
        return queryDao.sqlQuery(Record.class, sql, recordId);
    }

    public Long getRecords(int status, RedRain.ExecType execType) {
        if(status==1) {
            String sql = "SELECT COUNT(1) FROM record WHERE success=? AND execType=? AND (FLOWNUM IS NULL OR flowNum=1)";
            return queryDao.getCountBySql(sql,1,execType.getStatus());
        }else {
            String sql = "SELECT COUNT(1) FROM record WHERE success<>? AND execType=? AND (FLOWNUM IS NULL OR flowNum=1)";
            return queryDao.getCountBySql(sql,1,execType.getStatus());
        }
    }
}
