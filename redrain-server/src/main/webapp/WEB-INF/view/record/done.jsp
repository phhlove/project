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

    <style type="text/css">
        .last-child td {
            border-bottom-color: #0b0b0b;
            color: RED;
            border-bottom: 0px solid black;
        }
        .down{
            height: 12px;
            margin-bottom: 5px;
            margin-top: 0;
            width: 30px
        }
        .div-circle{
            width:15px;
            height:15px;
            background-color:#66c2a5;
            margin-top: 1px;
            margin-bottom: 1px;
            -moz-border-radius: 25px !important;
            -webkit-border-radius: 25px !important;
            border-radius:25px !important;
        }
        .span-circle{
            height:15px;
            line-height:15px;
            display:block;
            color:#FFF;
            text-align:center;
            font-size: 10px;
        }
    </style>

    <script type="text/javascript">
        $(document).ready(function(){
            $("#size").change(function(){doUrl();});
            $("#success").change(function(){doUrl();});
            $("#agentId").change(function(){doUrl();});
            $("#jobId").change(function(){doUrl();});
            $("#execType").change(function(){doUrl();});
        });
        function doUrl() {
            var pageSize = $("#size").val();
            var queryTime = $("#queryTime").val();
            var success = $("#success").val();
            var agentId = $("#agentId").val();
            var jobId = $("#jobId").val();
            var execType = $("#execType").val();
            window.location.href = "${contextPath}/record/done?queryTime=" + queryTime + "&success=" + success + "&agentId=" + agentId + "&jobId=" + jobId + "&execType=" + execType + "&pageSize=" + pageSize;
        }

        function showChild(id){
            var open = $("#record_"+id).attr("childOpen");
            if (open == "off"){
                $("#icon"+id).removeClass("fa-chevron-down").addClass("fa-chevron-up");
                $(".redoGroup"+id).css("background-color","rgba(0,0,0,0.25)");
                $(".child"+id).show();
                $("#record_"+id).attr("childOpen","on");
                $(".name_"+id+"_1").hide();
                $(".name_"+id+"_2").show();
            }else {
                $("#icon"+id).removeClass("fa-chevron-up").addClass("fa-chevron-down");
                $(".redoGroup"+id).css("background-color","");
                $(".child"+id).hide();
                $("#record_"+id).attr("childOpen","off");
                $(".name_"+id+"_1").show();
                $(".name_"+id+"_2").hide();
            }
        }

        function showFatherRedo(id,flowGroup,length){
            var open = $("#record_"+id).attr("childOpen");
            if (open == "off"){
                $("#icon"+id).removeClass("fa-chevron-down").addClass("fa-chevron-up");
                $(".redoGroup"+id).css("background-color","rgba(0,0,0,0.25)");
                $(".child"+id).show();
                $("#record_"+id).attr("childOpen","on");
                var row = $("#row_"+flowGroup).attr("rowspan");
                $("#row_"+flowGroup).attr("rowspan",parseInt(row) + parseInt(length));
                $(".redoIndex_"+id).show();
                $(".name_"+id+"_1").hide();
                $(".name_"+id+"_2").show();
            }else {
                $("#icon"+id).removeClass("fa-chevron-up").addClass("fa-chevron-down");
                $(".redoGroup"+id).css("background-color","");
                $(".child"+id).hide();
                $("#record_"+id).attr("childOpen","off");
                var row = $("#row_"+flowGroup).attr("rowspan");
                $("#row_"+flowGroup).attr("rowspan",parseInt(row) - parseInt(length));
                $(".redoIndex_"+id).hide();
                $(".name_"+id+"_1").show();
                $(".name_"+id+"_2").hide();
            }
        }

        function showChildRedo(id,flowGroup,length){
            var open = $("#record_"+id).attr("childOpen");
            if (open == "off"){
                $("#icon"+id).removeClass("fa-chevron-down").addClass("fa-chevron-up");
                $(".redoGroup"+id).css("background-color","rgba(0,0,0,0.25)");
                $(".child"+id).show();
                $("#record_"+id).attr("childOpen","on");
                var row = $("#row_"+flowGroup).attr("rowspan");
                $("#row_"+flowGroup).attr("rowspan",parseInt(row) + parseInt(length));
                $(".redoIndex_"+id).show();
                $(".name_"+id+"_1").hide();
                $(".name_"+id+"_2").show();
            }else {
                $("#icon"+id).removeClass("fa-chevron-up").addClass("fa-chevron-down");
                $(".redoGroup"+id).css("background-color","rgba(0,0,0,0.1)");
                $(".child"+id).hide();
                $("#record_"+id).attr("childOpen","off");
                var row = $("#row_"+flowGroup).attr("rowspan");
                $("#row_"+flowGroup).attr("rowspan",parseInt(row) - parseInt(length));
                $(".redoIndex_"+id).hide();
                $(".name_"+id+"_1").show();
                $(".name_"+id+"_2").hide();
            }
        }

        function showChildFlow(id,flowId){
            var open = $("#recordFlow_"+id).attr("childOpen");
            if (open == "off"){
                $("#iconFlow"+id).removeClass("fa-angle-double-down").addClass("fa-angle-double-up");
                $(".childFlow"+id).show();
                $(".flowRecord"+flowId).css("background-color","rgba(0,0,0,0.1)");
                $("#recordFlow_"+id).attr("childOpen","on");
                $(".name_"+id+"_1").hide();
                $(".name_"+id+"_2").show();
            }else {
                $(".flowRecord"+flowId).css("background-color","");
                $("#iconFlow"+id).removeClass("fa-angle-double-up").addClass("fa-angle-double-down");
                $(".iconChild"+id).removeClass("fa-chevron-up").addClass("fa-chevron-down");
                $(".openChild"+id).attr("childOpen","off");
                $(".childFlow"+id).hide();
                $(".redoIndex_"+id).hide();
                $(".childFlowRedo"+id).hide();
                $("#row_"+flowId).attr("rowspan",$("#row_"+flowId).attr("constant"));
                $(".name_"+id+"_1").show();
                $(".name_"+id+"_2").hide();
                $("#recordFlow_"+id).attr("childOpen","off");
            }
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
        <li><a href="#">已完成</a></li>
    </ol>
    <h4 class="page-title"><i class="fa fa-check-circle" aria-hidden="true"></i>&nbsp;已完成</h4>
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
                <select id="agentId" name="agentId" class="select-self" style="width: 110px;">
                    <option value="">全部</option>
                    <c:forEach var="d" items="${agents}">
                        <option value="${d.agentId}" ${d.agentId eq agentId ? 'selected' : ''}>${d.name}</option>
                    </c:forEach>
                </select>
                &nbsp;&nbsp;&nbsp;
                <label for="jobId">任务名称：</label>
                <select id="jobId" name="jobId" class="select-self" style="width: 110px;">
                    <option value="">全部</option>
                    <c:forEach var="t" items="${jobs}">
                        <option value="${t.jobId}" ${t.jobId eq jobId ? 'selected' : ''}>${t.jobName}&nbsp;</option>
                    </c:forEach>
                </select>
                &nbsp;&nbsp;&nbsp;
                <label for="success">执行状态：</label>
                <select id="success" name="success" class="select-self" style="width: 80px;">
                    <option value="">全部</option>
                    <option value="1" ${success eq 1 ? 'selected' : ''}>成功</option>
                    <option value="0" ${success eq 0 ? 'selected' : ''}>失败</option>
                    <option value="0" ${success eq 2 ? 'selected' : ''}>被杀</option>
                </select>
                &nbsp;&nbsp;&nbsp;
                <label for="execType">执行方式：</label>
                <select id="execType" name="execType" class="select-self" style="width: 80px;">
                    <option value="">全部</option>
                    <option value="0" ${execType eq 0 ? 'selected' : ''}>自动</option>
                    <option value="1" ${execType eq 1 ? 'selected' : ''}>手动</option>
                    <option value="3" ${execType eq 3 ? 'selected' : ''}>重跑</option>
                </select>
                &nbsp;&nbsp;&nbsp;
                <label for="queryTime">开始时间：</label>
                <input type="text" id="queryTime" name="queryTime" value="${queryTime}" onfocus="WdatePicker({onpicked:function(){doUrl(); },dateFmt:'yyyy-MM-dd'})" class="Wdate select-self" style="width: 90px"/>
            </div>
        </div>

        <table class="table tile">
            <thead>
            <tr>
                <th>任务名称</th>
                <th>执行器</th>
                <th>运行状态</th>
                <th>执行方式</th>
                <th>执行命令</th>
                <th>开始时间</th>
                <th>运行时长</th>
                <th>任务类型</th>
                <th><center>操作</center></th>
            </tr>
            </thead>

            <tbody>
            <%--父记录--%>
            <c:forEach var="r" items="${page.result}" varStatus="index">
                <tr <c:if test="${r.category eq 1}">class="flowRecord${r.flowGroup} redoGroup${r.recordId}"</c:if>
                    <c:if test="${r.category eq 0}">class="redoGroup${r.recordId}"</c:if>>
                    <c:if test="${r.category eq 0}">
                        <td  class="name_${r.recordId}_1"> <center>${r.jobName}</center></td>
                        <td style="display: none;" class="name_${r.recordId}_2" rowspan="${fn:length(r.childRecord)+1}">
                            <center>
                                    ${r.jobName}
                                <c:forEach var="c" items="${r.childRecord}" varStatus="index">
                                    <div>
                                        <div class="div-circle"><span class="span-circle">${index.count}</span></div>${c.jobName}
                                    </div>
                                </c:forEach>
                            </center>
                        </td>
                    </c:if>
                    <c:if test="${r.category eq 1}">
                        <td  class="name_${r.recordId}_1"> <center>${r.jobName}</center></td>
                        <td style="display: none;" class="name_${r.recordId}_2" id="row_${r.flowGroup}" constant="${fn:length(r.childJob)+1}" rowspan="${fn:length(r.childJob)+1}">
                            <center>
                                    ${r.jobName}
                                <c:if test="${r.redoCount ne 0}">
                                    <c:forEach var="rc" items="${r.childRecord}" varStatus="index">
                                        <div class="redoIndex_${rc.parentId} redoIndex_${r.recordId}" style="display: none">
                                            <div class="div-circle"><span class="span-circle">${index.count}</span></div>${rc.jobName}
                                        </div>
                                    </c:forEach>
                                </c:if>
                                <c:forEach var="t" items="${r.childJob}" varStatus="index">
                                    <div class="down"><i class="fa fa-arrow-down" style="font-size:14px" aria-hidden="true"></i></div>${t.jobName}
                                    <c:if test="${t.redoCount ne 0}">
                                        <c:forEach var="tc" items="${t.childRecord}" varStatus="count">
                                            <div class="redoIndex_${tc.parentId} redoIndex_${r.recordId}" style="display: none">
                                                <div class="div-circle"><span class="span-circle">${count.count}</span></div>${tc.jobName}
                                            </div>
                                        </c:forEach>
                                    </c:if>
                                </c:forEach>
                            </center>
                        </td>
                    </c:if>
                    <td>${r.agentName}</td>
                    <td>
                        <c:if test="${r.success eq 1}">
                            <span class="label label-success">&nbsp;&nbsp;成&nbsp;功&nbsp;&nbsp;</span>
                        </c:if>
                        <c:if test="${r.success eq 0}">
                            <span class="label label-danger">&nbsp;&nbsp;失&nbsp;败&nbsp;&nbsp;</span>
                        </c:if>
                        <c:if test="${r.success eq 2}">
                            <span class="label label-warning">&nbsp;&nbsp;被&nbsp;杀&nbsp;&nbsp;</span>
                        </c:if>
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
                        <c:if test="${r.category eq 1}">流程任务</c:if>
                        <c:if test="${r.category eq 0}">单一任务</c:if>
                    </td>
                    <td>
                        <center>
                            <div class="visible-md visible-lg hidden-sm hidden-xs action-buttons">
                                <c:if test="${r.category eq 1 and r.childJob ne null}">
                                    <a href="#" title="流程任务" id="recordFlow_${r.recordId}" childOpen="off" onclick="showChildFlow(${r.recordId},'${r.flowGroup}')">
                                        <i aria-hidden="true" class="fa fa-angle-double-down" style="font-size:15px;" id="iconFlow${r.recordId}"></i>
                                    </a>&nbsp;&nbsp;
                                </c:if>
                                <c:if test="${r.redoCount ne 0}">
                                    <a href="#" title="重跑记录" id="record_${r.recordId}" childOpen="off" onclick="showFatherRedo('${r.recordId}','${r.flowGroup}','${fn:length(r.childRecord)}')">
                                        <i aria-hidden="true" class="fa fa-chevron-down" id="icon${r.recordId}"></i>
                                    </a>&nbsp;&nbsp;
                                </c:if>
                                <a href="${contextPath}/record/detail?id=${r.recordId}" title="查看详情">
                                    <i class="glyphicon glyphicon-eye-open"></i>
                                </a>&nbsp;&nbsp;
                            </div>
                        </center>
                    </td>
                </tr>
                <%--父记录重跑记录--%>
                <c:if test="${r.redoCount ne 0}">
                    <c:forEach var="rc" items="${r.childRecord}" varStatus="index">
                        <tr class="child${r.recordId} redoGroup${r.recordId}" style="display: none;">
                            <td>${rc.agentName}</td>
                            <td>
                                <c:if test="${rc.success eq 1}">
                                    <span class="label label-success">&nbsp;&nbsp;成&nbsp;功&nbsp;&nbsp;</span>
                                </c:if>
                                <c:if test="${rc.success eq 0}">
                                    <span class="label label-danger">&nbsp;&nbsp;失&nbsp;败&nbsp;&nbsp;</span>
                                </c:if>
                                <c:if test="${rc.success eq 2}">
                                    <span class="label label-warning">&nbsp;&nbsp;被&nbsp;杀&nbsp;&nbsp;</span>
                                </c:if>
                            </td>
                            <td><span class="label label-warning">&nbsp;&nbsp;重&nbsp;跑&nbsp;&nbsp;</span></td>
                            <td title="${rc.command}">${ben:substr(rc.command,0 ,30 ,"..." )}</td>
                            <td><fmt:formatDate value="${rc.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td>${ben:diffdate(rc.startTime,c.endTime)}</td>
                            <td>
                                <c:if test="${rc.category eq 1}">流程任务</c:if>
                                <c:if test="${rc.category eq 0}">单一任务</c:if>
                            </td>
                            <td>
                                <center>
                                    <div class="visible-md visible-lg hidden-sm hidden-xs action-buttons">
                                        <a href="${contextPath}/record/detail?id=${rc.recordId}" title="查看详情">
                                            <i class="glyphicon glyphicon-eye-open"></i>
                                        </a>&nbsp;&nbsp;
                                    </div>
                                </center>
                            </td>
                        </tr>
                    </c:forEach>
                </c:if>
                <%--流程子任务--%>
                <c:if test="${r.category eq 1}">
                    <c:forEach var="t" items="${r.childJob}" varStatus="index">

                        <tr class="childFlow${r.recordId} flowRecord${r.flowGroup} redoGroup${t.recordId}" style="display: none;">
                            <td>${t.agentName}</td>
                            <td>
                                <c:if test="${t.success eq 1}">
                                    <span class="label label-success">&nbsp;&nbsp;成&nbsp;功&nbsp;&nbsp;</span>
                                </c:if>
                                <c:if test="${t.success eq 0}">
                                    <span class="label label-danger">&nbsp;&nbsp;失&nbsp;败&nbsp;&nbsp;</span>
                                </c:if>
                                <c:if test="${t.success eq 2}">
                                    <span class="label label-warning">&nbsp;&nbsp;被&nbsp;杀&nbsp;&nbsp;</span>
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${t.execType eq 0}"><span class="label label-default">&nbsp;&nbsp;自&nbsp;动&nbsp;&nbsp;</span></c:if>
                                <c:if test="${t.execType eq 1}"><span class="label label-info">&nbsp;&nbsp;手&nbsp;动&nbsp;&nbsp;</span></c:if>
                                <c:if test="${t.execType >= 2}"><span class="label label-warning">&nbsp;&nbsp;重&nbsp;跑&nbsp;&nbsp;</span></c:if>
                            </td>
                            <td title="${t.command}">${ben:substr(t.command,0 ,30 ,"..." )}</td>
                            <td><fmt:formatDate value="${t.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td>${ben:diffdate(t.startTime,t.endTime)}</td>
                            <td>流程任务</td>
                            <td>
                                <center>
                                    <div class="visible-md visible-lg hidden-sm hidden-xs action-buttons">
                                        <c:if test="${t.redoCount ne 0}">
                                            <a class="openChild${r.recordId}" href="#" title="重跑记录" id="record_${t.recordId}" childOpen="off" onclick="showChildRedo('${t.recordId}','${t.flowGroup}','${fn:length(t.childRecord)}')">
                                                <i aria-hidden="true" class="fa fa-chevron-down iconChild${r.recordId}" id="icon${t.recordId}"></i>
                                            </a>&nbsp;&nbsp;
                                        </c:if>
                                        <a href="${contextPath}/record/detail?id=${t.recordId}" title="查看详情">
                                            <i class="glyphicon glyphicon-eye-open"></i>
                                        </a>&nbsp;&nbsp;
                                    </div>
                                </center>
                            </td>
                        </tr>
                        <%--流程子任务的重跑记录--%>
                        <c:if test="${t.redoCount ne 0}">
                            <c:forEach var="tc" items="${t.childRecord}" varStatus="index">
                                <tr class="child${t.recordId} redoGroup${t.recordId} childFlowRedo${r.recordId}" style="display: none;">
                                    <td>${tc.agentName}</td>
                                    <td>
                                        <c:if test="${tc.success eq 1}">
                                            <span class="label label-success">&nbsp;&nbsp;成&nbsp;功&nbsp;&nbsp;</span>
                                        </c:if>
                                        <c:if test="${tc.success eq 0}">
                                            <span class="label label-danger">&nbsp;&nbsp;失&nbsp;败&nbsp;&nbsp;</span>
                                        </c:if>
                                        <c:if test="${tc.success eq 2}">
                                            <span class="label label-warning">&nbsp;&nbsp;被&nbsp;杀&nbsp;&nbsp;</span>
                                        </c:if>
                                    </td>
                                    <td><span class="label label-warning">&nbsp;&nbsp;重&nbsp;跑&nbsp;&nbsp;</span></td>
                                    <td title="${tc.command}">${ben:substr(tc.command,0 ,30 ,"..." )}</td>
                                    <td><fmt:formatDate value="${tc.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                    <td>${ben:diffdate(tc.startTime,tc.endTime)}</td>
                                    <td>流程任务</td>
                                    <td>
                                        <center>
                                            <div class="visible-md visible-lg hidden-sm hidden-xs action-buttons">
                                                <a href="${contextPath}/record/detail?id=${tc.recordId}" title="查看详情">
                                                    <i class="glyphicon glyphicon-eye-open"></i>
                                                </a>&nbsp;&nbsp;
                                            </div>
                                        </center>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:if>
                    </c:forEach>
                </c:if>
            </c:forEach>
            </tbody>
        </table>
        <ben:pager href="${contextPath}/record/done?queryTime=${queryTime}&success=${success}&agentId=${agentId}&jobId=${jobId}&execType=${execType}" id="${page.pageNo}" size="${page.pageSize}" total="${page.totalCount}"/>
    </div>

</section>
<br/><br/>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
