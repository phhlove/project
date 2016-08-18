<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ben"  uri="ben-taglib"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%
    String port = request.getServerPort() == 80 ? "" : (":"+request.getServerPort());
    String path = request.getContextPath().replaceAll("/$","");
    String contextPath = request.getScheme()+"://"+request.getServerName()+port+path;
    pageContext.setAttribute("contextPath",contextPath);
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/common/resource.jsp"/>

    <script type="text/javascript">
        $(document).ready(function(){
            setInterval(function(){

                $("#highlight").fadeOut(3000,function(){
                    $(this).show();
                });

                $.ajax({
                    url:"${contextPath}/record/running",
                    data:{
                        "refresh":1,
                        "size":"${size}",
                        "queryTime":"${queryTime}",
                        "agentId":"${agentId}",
                        "jobId":"${jobId}",
                        "execType":"${execType}",
                        "pageNo":${page.pageNo},
                        "pageSize":${page.pageSize}
                    },
                    dataType:"html",
                    success:function(data){
                        //解决子页面登录失联,不能跳到登录页面的bug
                        if(data.indexOf("login")>-1){
                            window.location.href="${contextPath}";
                        }else {
                            $("#tableContent").html(data);
                        }
                    }
                });
            },5000);

            $("#size").change(function(){doUrl();});
            $("#agentId").change(function(){doUrl();});
            $("#jobId").change(function(){doUrl();});
            $("#execType").change(function(){doUrl();});
        });
        function doUrl() {
            var pageSize = $("#size").val();
            var queryTime = $("#queryTime").val();
            var agentId = $("#agentId").val();
            var jobId = $("#jobId").val();
            var execType = $("#execType").val();
            window.location.href = "${contextPath}/record/running?queryTime=" + queryTime + "&agentId=" + agentId + "&jobId=" + jobId + "&execType=" + execType + "&pageSize=" + pageSize;
        }

        function killJob(id){
            swal({
                title: "",
                text: "您确定要结束这个作业吗？",
                type: "warning",
                showCancelButton: true,
                closeOnConfirm: false,
                confirmButtonText: "结束",
            }, function() {
                $("#process_"+id).html("停止中");
                $.ajax({
                    url:"${contextPath}/record/kill",
                    data:{"recordId":id}
                });
                alertMsg("结束请求已发送");
                return;
            });

        }

        function restartJob(id,jobId){
            swal({
                title: "",
                text: "您确定要结束并重启这个作业吗？",
                type: "warning",
                showCancelButton: true,
                closeOnConfirm: false,
                confirmButtonText: "重启",
            }, function() {
                $("#process_"+id).html("停止中");
                $.ajax({
                    url:"${contextPath}/record/kill",
                    data:{"recordId":id},
                    success:function(result){
                        if (result == "true"){
                            $.ajax({
                                url:"${contextPath}/job/execute",
                                data:{"id":jobId}
                            });
                        }
                    }
                });
                alertMsg( "该作业已重启,正在执行中.");
                return;
            });

        }

    </script>
</head>
<jsp:include page="/WEB-INF/common/top.jsp"/>

<!-- Content -->
<section id="content" class="container">

    <!-- Messages Drawer -->
    <jsp:include page="/WEB-INF/common/message.jsp"/>

    <!-- Breadcrumb -->
    <ol class="breadcrumb hidden-xs">
        <li class="icon">&#61753;</li>
        当前位置：
        <li><a href="#">RedRain</a></li>
        <li><a href="#">调度记录</a></li>
        <li><a href="#">正在运行</a></li>
    </ol>
    <h4 class="page-title"><i aria-hidden="true" class="fa fa-play-circle"></i>&nbsp;正在运行&nbsp;&nbsp;<span id="highlight" style="font-size: 14px"><img src='${contextPath}/img/icon-loader.gif' style="width: 14px;height: 14px">&nbsp;调度作业持续进行中...</span></h4>
    <div class="block-area" id="defaultStyle">

        <div>
            <div style="float: left">
                <label>
                    每页 <select size="1" class="select-self" id="size" style="width: 50px;">
                    <option value="15">15</option>
                    <option value="30" ${page.pageSize eq 30 ? 'selected' : ''}>30</option>
                    <option value="50" ${page.pageSize eq 50 ? 'selected' : ''}>50</option>
                    <option value="100" ${page.pageSize eq 100 ? 'selected' : ''}>100</option>
                </select> 条记录
                </label>
            </div>

            <div style="float: right;margin-bottom: 10px">
                <label for="agentId">执行器：</label>
                <select id="agentId" name="agentId" class="select-self" style="width: 120px;">
                    <option value="">全部</option>
                    <c:forEach var="d" items="${agents}">
                        <option value="${d.agentId}" ${d.agentId eq agentId ? 'selected' : ''}>${d.name}</option>
                    </c:forEach>
                </select>
                &nbsp;&nbsp;&nbsp;
                <label for="jobId">作业名称：</label>
                <select id="jobId" name="jobId" class="select-self" style="width: 80px;">
                    <option value="">全部</option>
                    <c:forEach var="t" items="${jobs}">
                        <option value="${t.jobId}" ${t.jobId eq jobId ? 'selected' : ''}>${t.jobName}&nbsp;</option>
                    </c:forEach>
                </select>
                &nbsp;&nbsp;&nbsp;
                <label for="execType">执行方式：</label>
                <select id="execType" name="execType" class="select-self" style="width: 80px;">
                    <option value="">全部</option>
                    <option value="0" ${execType eq 0 ? 'selected' : ''}>自动</option>
                    <option value="1" ${execType eq 1 ? 'selected' : ''}>手动</option>
                    <option value="2" ${execType eq 2 ? 'selected' : ''}>重跑</option>
                </select>
                &nbsp;&nbsp;&nbsp;
                <label for="queryTime">开始时间：</label>
                <input type="text" id="queryTime" name="queryTime" value="${queryTime}" onfocus="WdatePicker({onpicked:function(){doUrl(); },dateFmt:'yyyy-MM-dd'})" class="Wdate select-self" style="width: 90px"/>
            </div>
        </div>

        <table class="table tile">
            <thead>
            <tr>
                <th>作业名称</th>
                <th>执行器</th>
                <th>运行状态</th>
                <th>执行方式</th>
                <th>执行命令</th>
                <th>开始时间</th>
                <th>运行时长</th>
                <th>作业类型</th>
                <th><center>操作</center></th>
            </tr>
            </thead>

            <tbody id="tableContent">

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
            </tbody>
        </table>

        <ben:pager href="${contextPath}/record/running?queryTime=${queryTime}&agentId=${agentId}&jobId=${jobId}&execType=${execType}" id="${page.pageNo}" size="${page.pageSize}" total="${page.totalCount}"/>

    </div>

</section>
<br/><br/>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
