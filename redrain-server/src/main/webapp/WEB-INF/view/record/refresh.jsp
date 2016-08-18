<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ben"  uri="ben-taglib"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%
    String port = request.getServerPort() == 80 ? "" : (":"+request.getServerPort());
    String path = request.getContextPath().replaceAll("/$","");
    String contextPath = request.getScheme()+"://"+request.getServerName()+port+path;
    pageContext.setAttribute("contextPath",contextPath);
%>

<c:forEach var="r" items="${page.result}" varStatus="index">
    <tr>
        <td><a href="${contextPath}/job/detail?id=${r.jobId}">${r.jobName}</a></td>
        <td><a href="${contextPath}/agent/detail?id=${r.agentId}">${r.agentName}</a></td>
        <td>
            <div class="progress progress-striped progress-success active" style="margin-top:3px;width: 80%;height: 14px;" >
                <div style="width:100%;height: 100%;" class="progress-bar">
                    &nbsp;&nbsp;
                                <span id="process_${r.recordId}">
                                    <c:if test="${r.status eq 0}">运行中</c:if>
                                    <c:if test="${r.status eq 2}">停止中</c:if>
                                </span>
                    ...&nbsp;&nbsp;
                </div>
            </div>
        </td>
        <td>
            <c:if test="${r.execType eq 0}"><span class="label label-default">&nbsp;&nbsp;自&nbsp;动&nbsp;&nbsp;</span></c:if>
            <c:if test="${r.execType eq 1}"><span class="label label-info">&nbsp;&nbsp;手&nbsp;动&nbsp;&nbsp;</span></c:if>
            <c:if test="${r.execType >= 2}"><span class="label label-warning">&nbsp;&nbsp;重&nbsp;跑&nbsp;&nbsp;</span></c:if>
        </td>
        <td title="${r.command}">${ben:substr(r.command,0 ,30 ,"..." )}</td>
        <td><fmt:formatDate value="${r.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
        <td>${ben:diffdate(r.startTime,r.endTime)}</td>
        <td>
            <c:if test="${r.category eq 1}">流程作业</c:if>
            <c:if test="${r.category eq 0}">单一作业</c:if>
        </td>
        <td><center>
            <div class="visible-md visible-lg hidden-sm hidden-xs action-buttons">
                <a href="#" onclick="killJob('${r.recordId}')" title="kill">
                    <i class="glyphicon glyphicon-stop"></i>
                </a>&nbsp;&nbsp;

                <a href="#" onclick="restartJob('${r.recordId}','${r.jobId}')" title="结束并重启">
                    <i class="glyphicon glyphicon-refresh"></i>
                </a>&nbsp;&nbsp;

            </div>
        </center>
        </td>
    </tr>
</c:forEach>