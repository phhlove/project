<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ben"  uri="ben-taglib"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

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

    <script type="text/javascript" src="${contextPath}/js/socket/socket.io.js"></script>

    <script type="text/javascript">

    function showContact(){$(".contact").show()}
    function hideContact(){$(".contact").hide()}
    function showProxy(){
        $(".proxy").show();
        $("#proxy1").prop("checked",true);
        $("#proxy").val(1);
        $("#proxy1").parent().removeClass("checked").addClass("checked");
        $("#proxy1").parent().attr("aria-checked",true);
        $("#proxy1").parent().prop("onclick","showContact()");
        $("#proxy0").parent().removeClass("checked");
        $("#proxy0").parent().attr("aria-checked",false);
    }
    function hideProxy(){
        $(".proxy").hide();
        $("#proxy").val(0);
        $("#proxy0").prop("checked",true);
        $("#proxy0").parent().removeClass("checked").addClass("checked");
        $("#proxy0").parent().attr("aria-checked",true);
        $("#proxy1").parent().removeClass("checked");
        $("#proxy1").parent().attr("aria-checked",false);
    }

    $(document).ready(function(){
        $("#size").change(function(){
            var pageSize = $("#size").val();
            window.location.href = "${contextPath}/agent/view?pageSize="+pageSize;
        });

        setInterval(function(){

            $("#highlight").fadeOut(3000,function(){
                $(this).show();
            });

            $.ajax({
                url:"${contextPath}/agent/view",
                data:{
                    "refresh":1,
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

        $("#name").focus(function(){
            $("#checkName").html("");
        });

        $("#pwd0").focus(function(){
            $("#oldpwd").html("");
        });

        $("#pwd2").focus(function(){
            $("#checkpwd").html("");
        });

        $("#name").blur(function(){
            if(!$("#name").val()){
                $("#checkName").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;请填写执行器名' + "</font>");
                return false;
            }
            $.ajax({
                url:"${contextPath}/agent/checkname",
                data:{
                    "id":$("#id").val(),
                    "name":$("#name").val()
                },
                success:function(data){
                    if (data == "yes"){
                        $("#checkName").html("<font color='green'>" + '<i class="glyphicon glyphicon-ok-sign"></i>&nbsp;执行器名可用' + "</font>");
                        return false;
                    }else {
                        $("#checkName").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;执行器名已存在' + "</font>");
                        return false;
                    }
                },
                error : function() {
                    alert("网络繁忙请刷新页面重试!");
                    return false;
                }
            });
        });

        $("#pwd0").blur(function(){
            if (!$("#pwd0").val()){
                $("#oldpwd").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;请输入原密码' + "</font>");
            }
        });

        $("#pwd2").change(function(){
            if ($("#pwd1").val()==$("#pwd2").val()){
                $("#checkpwd").html("<font color='green'>" + '<i class="glyphicon glyphicon-ok-sign"></i>&nbsp;两密码一致' + "</font>");
            }else {
                $("#checkpwd").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;密码不一致' + "</font>");
            }
        });

        $("#proxy1").attr("onclick","showProxy()").next().attr("onclick","showProxy()");
        $("#proxy0").attr("onclick","hideProxy()").next().attr("onclick","hideProxy()");

    });

    function edit(id){
        $.ajax({
            url:"${contextPath}/agent/editpage",
            data:{"id":id},
            success : function(obj) {
                $("#agentform")[0].reset();
                if(obj!=null){
                    $("#checkName").html("");
                    $("#pingResult").html("");
                    $("#id").val(obj.agentId);
                    $("#password").val(obj.password);
                    if (obj.status==true){
                        $("#status").val("1");
                    }else {
                        $("#status").val("0");
                    }
                    $("#name").val(obj.name);
                    $("#ip").val(obj.ip);
                    $("#port").val(obj.port);
                    if(obj.proxy==1){
                        showProxy();
                        $("#proxyAgent").val(obj.proxyAgent);

                    }else {
                        hideProxy();

                    }

                    $("#warning1").next().attr("onclick","showContact()");
                    $("#warning0").next().attr("onclick","hideContact()");
                    if(obj.warning==true){
                        showContact();
                        $("#warning1").prop("checked",true);
                        $("#warning1").parent().removeClass("checked").addClass("checked");
                        $("#warning1").parent().attr("aria-checked",true);
                        $("#warning1").parent().prop("onclick","showContact()");
                        $("#warning0").parent().removeClass("checked");
                        $("#warning0").parent().attr("aria-checked",false);
                    }else {
                        hideContact();
                        $("#warning0").prop("checked",true);
                        $("#warning0").parent().removeClass("checked").addClass("checked");
                        $("#warning0").parent().attr("aria-checked",true);
                        $("#warning1").parent().removeClass("checked");
                        $("#warning1").parent().attr("aria-checked",false);
                    }
                    $("#mobiles").val(obj.mobiles);
                    $("#email").val(obj.emailAddress);
                    $("#agentModal").modal("show");
                    return;
                }


            },
            error : function() {
                alert("网络繁忙请刷新页面重试!");
            }
        });
    }

    function save(){
        var id = $("#id").val();
        if (!id){
            alert("页面异常，请刷新重试！");
            return false;
        }
        var password = $("#password").val();
        if (!password){
            alert("页面异常，请刷新重试！");
            return false;
        }
        var name = $("#name").val();
        if (!name){
            alert("请填写执行器名称!");
            return false;
        }
        var ip = $("#ip").val();
        if (!ip){
            alert("请填写机器IP!");
            return false;
        }
        if (!redrain.testIp(ip)){
            alert("请填写正确的IP地址!");
            return false;
        }
        var port = $("#port").val();
        if (!port){
            alert("请填写端口号!");
            return false;
        }
        if (!redrain.testPort(port)){
            alert("请填写正确的端口号!");
            return false;
        }
        var warning = $('input[type="radio"][name="warning"]:checked').val();
        if (!warning){
            alert("页面错误，请刷新重试!");
            return false;
        }
        if (warning == 1){
            var mobiles = $("#mobiles").val();
            if (!mobiles){
                alert("请填写手机号码!");
                return false;
            }
            if(!redrain.testMobile(mobiles)){
                alert("请填写正确的手机号码!");
                return false;
            }
            var email = $("#email").val();
            if (!email){
                alert("请填写邮箱地址!");
                return false;
            }
            if(!redrain.testEmail(email)){
                alert("请填写正确的邮箱地址!");
                return false;
            }
        }
        var status = $("#status").val();
        if (!status){
            alert("页面异常，请刷新重试！");
            return false;
        }
        $.ajax({
            url:"${contextPath}/agent/checkname",
            data:{
                "id":id,
                "name":name
            },
            success:function(data){
                if (data == "yes"){
                    if (status == 1){
                        $.ajax({
                            url:"${contextPath}/verify/ping",
                            data:{
                                "proxy":$("#proxy").val(),
                                "proxyId":$("#proxyAgent").val(),
                                "ip":ip,
                                "port":port,
                                "password":password
                            },
                            success:function(data){
                                if (data == "success"){
                                    canSave(id,name,port,warning,mobiles,email);
                                    return false;
                                }else {
                                    alert("通信失败!请检查IP和端口号");
                                }
                            },
                            error : function() {
                                alert("网络繁忙请刷新页面重试!");
                            }
                        });
                    }else {
                        canSave(id,name,port,warning,mobiles,email);
                        return false;
                    }
                }else {
                    alert("用户已存在!");
                    return false;
                }
            },
            error : function() {
                alert("网络繁忙请刷新页面重试!");
                return false;
            }
        });
    }

    function canSave(id,name,port,warning,mobiles,email){
        $.ajax({
            url:"${contextPath}/agent/edit",
            data:{
                "proxy":$("#proxy").val(),
                "proxyAgent":$("#proxyAgent").val(),
                "agentId":id,
                "name":name,
                "port":port,
                "warning":warning,
                "mobiles":mobiles,
                "emailAddress":email
            },
            success:function(data){
                if (data == "success"){
                    $('#agentModal').modal('hide');
                    alertMsg("修改成功");
                    $("#name_"+id).html(name);
                    $("#port_"+id).html(port);
                    if(warning == "0"){
                        $("#warning_"+id).html('<span class="label label-default" style="color: red;font-weight:bold">&nbsp;&nbsp;否&nbsp;&nbsp;</span>');
                    }else {
                        $("#warning_"+id).html('<span class="label label-warning" style="color: white;font-weight:bold">&nbsp;&nbsp;是&nbsp;&nbsp;</span>');
                    }
                    return false;
                }else {
                    alert("修改失败");
                }
            },
            error : function() {
                alert("网络繁忙请刷新页面重试!");
                return false;
            }
        });
    }

    function editPwd(id){
        $.ajax({
            url:"${contextPath}/agent/pwdpage",
            data:{"id":id},
            success : function(obj) {
                $("#pwdform")[0].reset();
                if(obj!=null){
                    $("#oldpwd").html("");
                    $("#checkpwd").html("");
                    $("#agentId").val(obj.agentId);
                    $("#pwdModal").modal("show");
                    return;
                }
            },
            error : function() {
                alert("网络繁忙请刷新页面重试!11");
            }
        });
    }

    function savePwd(){
        var id = $("#agentId").val();
        if (!id){
            alert("页面异常，请刷新重试!");
            return false;
        }
        var pwd0 = $("#pwd0").val();
        if (!pwd0){
            alert("请填原密码!");
            return false;
        }
        var pwd1 = $("#pwd1").val();
        if (!pwd1){
            alert("请填新密码!");
            return false;
        }
        var pwd2 = $("#pwd2").val();
        if (!pwd2){
            alert("请填写确认密码!");
            return false;
        }
        if (pwd1 != pwd2){
            alert("两密码不一致!");
            return false;
        }
        $.ajax({
            url:"${contextPath}/agent/editpwd",
            data:{
                "id":id,
                "pwd0":pwd0,
                "pwd1":pwd1,
                "pwd2":pwd2
            },
            success:function(data){
                if (data == "success"){
                    $('#pwdModal').modal('hide');
                    alertMsg("修改成功");
                    return false;
                }
                if (data == "failure"){
                    alert("Client密码存在异常!");
                    return false;
                }
                if(data == "one"){
                    $("#oldpwd").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;密码不正确' + "</font>");
                    return false;
                }
                if(data == "two"){
                    $("#checkpwd").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;密码不一致' + "</font>");
                    return false;
                }

            },
            error : function() {
                alert("网络繁忙请刷新页面重试!");
                return false;
            }
        });
    }

    function pingCheck(){

        var ip = $("#ip").val();
        if (!ip){
            alert("请填写机器IP!");
            return false;
        }
        var password = $("#password").val();
        if (!password){
            alert("页面异常，请刷新重试！");
            return false;
        }
        if (!redrain.testIp(ip)){
            alert("请填写正确的IP地址!");
            return false;
        }
        var port = $("#port").val();
        if (!port){
            alert("请填写端口号!");
            return false;
        }
        if (!redrain.testPort(port)){
            alert("请填写正确的端口号!");
            return false;
        }

        $("#pingResult").html("<img src='${contextPath}/img/icon-loader.gif'> <font color='#2fa4e7'>检测中...</font>");

        $.ajax({
            url:"${contextPath}/verify/ping",
            data:{
                "agentId":$("#agentId").val(),
                "ip":ip,
                "port":port,
                "password":password
            },
            success:function(data){
                if (data == "success"){
                    $("#pingResult").html("<font color='green'>" + '<i class="glyphicon glyphicon-ok-sign"></i>&nbsp;通信正常' + "</font>");
                    return;
                }else {
                    $("#pingResult").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;通信失败' + "</font>");
                    return;
                }
            },
            error : function() {
                alert("网络繁忙请刷新页面重试!");
            }
        });

    }

    function ssh(agentId,ip,type) {
        $.ajax({
            url:"${contextPath}/term/ssh",
            data:"agentId="+agentId+"&ip="+ip,
            dataType: "html",
            success:function (url) {
                if (url == "null") {
                    $("#sship").val(ip);
                    $("#sshagent").val(agentId);
                    $("#sshModal").modal("show");
                }else {
                    var socketUrl = url.split("?")[0];
                    var term =  url.split("?")[1];
                    var socket = io.connect(socketUrl);
                    socket.emit('login', term ,function(data) {
                        if( data == "authfail" ) {
                            if(type==2) {
                                alert("登录失败,请确认登录口令的正确性");
                            }else {
                                $("#sship").val(ip);
                                $("#sshagent").val(agentId);
                                $("#sshModal").modal("show");
                            }
                            socket.disconnect();
                        }else if( data == "timeout" ) {
                            alert("连接到远端主机超时");
                            socket.disconnect();
                        }
                    });

                    socket.on("console",function(data, ackServerCallback) {
                        alert(data);
                        if (ackServerCallback) {
                            ackServerCallback();
                        }
                    });

                    socket.on("disconnect",function () {
                        console.log("close");
                    });
                }
            }
        });

    }
        
    function saveSsh() {
        var user = $("#sshuser").val();
        var pwd = $("#sshpwd").val();
        var port = $("#sshport").val();
        var ip = $("#sship").val();
        var agent = $("#sshagent").val();
        $.ajax({
            url:"${contextPath}/term/save",
            type:"post",
            data:{
                "user":user,
                "password":pwd,
                "port":port,
                "host":ip
            },
            dataType:"html",
            success:function (status) {
                $("#sshModal").modal("hide");
                $("#sshform")[0].reset();
                if( status == "success" ){
                    ssh(agent,ip,2);
                }else {
                    alert("登录失败,请确认登录口令的正确性");
                }
            }
        });
    }

</script>

    <style type="text/css">
        .visible-md i{
            font-size: 15px;
        }
    </style>

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
        <li><a href="">RedRain</a></li>
        <li><a href="">执行器管理</a></li>
    </ol>
    <h4 class="page-title"><i class="fa fa-desktop" aria-hidden="true"></i>&nbsp;执行器管理&nbsp;&nbsp;<span id="highlight" style="font-size: 14px"><img src='${contextPath}/img/icon-loader.gif' style="width: 14px;height: 14px">&nbsp;通信监测持续进行中...</span></h4>
    <div class="block-area" id="defaultStyle">
        <div>
            <div style="float: left">
                <label>
                    每页 <select size="1" class="select-self" id="size" style="width: 50px;margin-bottom: 8px">
                    <option value="15">15</option>
                    <option value="30" ${page.pageSize eq 30 ? 'selected' : ''}>30</option>
                    <option value="50" ${page.pageSize eq 50 ? 'selected' : ''}>50</option>
                    <option value="100" ${page.pageSize eq 100 ? 'selected' : ''}>100</option>
                </select> 条记录
                </label>
            </div>
            <c:if test="${permission eq true}">
            <div style="float: right;margin-top: -10px">
                <a href="${contextPath}/agent/addpage" class="btn btn-sm m-t-10" style="margin-left: 50px;margin-bottom: 8px"><i class="icon">&#61943;</i>添加</a>
            </div>
            </c:if>
        </div>

        <table class="table tile">
            <thead>
            <tr>
                <th>执行器</th>
                <th>ip</th>
                <th>端口号</th>
                <th>通信状态</th>
                <th>失联报警</th>
                <th><center>操作</center></th>
            </tr>
            </thead>

            <tbody id="tableContent">

            <c:forEach var="w" items="${page.result}" varStatus="index">
                <tr>
                    <td id="name_${w.agentId}">${w.name}</td>
                    <td>${w.ip}</td>
                    <td id="port_${w.agentId}">${w.port}</td>
                    <td id="agent_${d.agentId}">
                        <c:if test="${w.status eq false}">
                            <span class="label label-danger">&nbsp;&nbsp;失&nbsp;败&nbsp;&nbsp;</span>
                        </c:if>
                        <c:if test="${w.status eq true}">
                            <span class="label label-success">&nbsp;&nbsp;成&nbsp;功&nbsp;&nbsp;</span>
                        </c:if>
                    </td>
                    <td id="warning_${w.agentId}">
                        <c:if test="${w.warning eq false}"><span class="label label-default" style="color: red;font-weight:bold">&nbsp;&nbsp;否&nbsp;&nbsp;</span>  </c:if>
                        <c:if test="${w.warning eq true}"><span class="label label-warning" style="color: white;font-weight:bold">&nbsp;&nbsp;是&nbsp;&nbsp;</span> </c:if>
                    </td>
                    <td>
                        <center>
                            <div class="visible-md visible-lg hidden-sm hidden-xs action-buttons">
                                <a href="javascript:ssh('${w.agentId}','${w.ip}',1)" title="登录">
                                    <i aria-hidden="true" class="fa fa-desktop"></i>
                                </a>&nbsp;&nbsp;

                                <a href="${contextPath}/job/addpage?id=${w.agentId}" title="新任务">
                                    <i aria-hidden="true" class="fa fa-plus-square"></i>
                                </a>&nbsp;&nbsp;
                                <c:if test="${permission eq true}">
                                    <a href="#" onclick="edit('${w.agentId}')" title="编辑">
                                        <i aria-hidden="true" class="fa fa-edit"></i>
                                    </a>&nbsp;&nbsp;
                                    <a href="#" onclick="editPwd('${w.agentId}')" title="修改密码">
                                        <i aria-hidden="true" class="fa fa-lock"></i>
                                    </a>&nbsp;&nbsp;
                                </c:if>
                                <a href="${contextPath}/agent/detail?id=${w.agentId}" title="查看详情">
                                    <i aria-hidden="true" class="fa fa-eye"></i>
                                </a>
                            </div>
                        </center>
                    </td>
                </tr>
            </c:forEach>

            </tbody>
        </table>

        <ben:pager href="${contextPath}/agent/view" id="${page.pageNo}" size="${page.pageSize}" total="${page.totalCount}"/>

    </div>

    <!-- 修改执行器弹窗 -->
    <div class="modal fade" id="agentModal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4>修改执行器</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" role="form" id="agentform">
                        <input type="hidden" id="id" name="id"><input type="hidden" id="password" name="password"><input type="hidden" id="status" name="status">
                        <div class="form-group" style="margin-bottom: 4px;">
                            <label for="ip" class="col-lab control-label" title="执行器IP地址只能为点分十进制方式表示">机&nbsp;&nbsp;器&nbsp;&nbsp;IP：</label>
                            <div class="col-md-9">
                                <input type="text" class="form-control " id="ip" readonly>&nbsp;
                            </div>
                        </div>

                        <div class="form-group" style="">
                            <label for="name" class="col-lab control-label" title="执行器名称必填">执行器名：</label>
                            <div class="col-md-9">
                                <input type="text" class="form-control " id="name">&nbsp;&nbsp;<label id="checkName"></label>
                            </div>
                        </div>

                        <c:if test="${empty page.result}">
                            <!--默认为直连-->
                            <input type="hidden" name="proxy" id="proxy" value="0">
                        </c:if>
                        <c:if test="${!empty page.result}">
                            <div class="form-group">
                                <label class="col-lab control-label" title="执行器通信不正常时是否发信息报警">连接类型：</label>&nbsp;&nbsp;
                                <input type="hidden" id="proxy"/>
                                <label  onclick="hideProxy()" for="proxy0" class="radio-label"><input type="radio"  onclick="hideProxy()" name="proxy" value="0" id="proxy0">直连</label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                <label  onclick="showProxy()" for="proxy1" class="radio-label"><input type="radio"  onclick="showProxy()" name="proxy" value="1" id="proxy1">代理&nbsp;&nbsp;&nbsp;</label>
                            </div>

                            <div class="form-group proxy" style="display: none;margin-top: 20px;">
                                <label for="proxyAgent" class="col-lab control-label">代&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;理：</label>
                                <div class="col-md-9">
                                <select id="proxyAgent" name="proxyAgent" class="form-control">
                                    <c:forEach var="d" items="${page.result}">
                                        <c:if test="${d.proxy eq 0}">
                                            <option value="${d.agentId}">${d.ip}&nbsp;(${d.name})</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                                </div>
                            </div><br>
                        </c:if>

                        <div class="form-group">
                            <label for="port" class="col-lab control-label" title="执行器端口号只能是数字,且不超过4位">端&nbsp;&nbsp;口&nbsp;&nbsp;号：</label>
                            <div class="col-md-9">
                                <input type="text" class="form-control " id="port" style="margin-bottom: 5px;"/>&nbsp;&nbsp;<a href="#" onclick="pingCheck()">
                                <i class="glyphicon glyphicon-signal"></i>&nbsp;检测通信</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<label id="pingResult"></label>
                            </div>
                        </div>
                        <div class="form-group" style="margin-top: 15px;margin-bottom: 20px">
                            <label class="col-lab control-label" title="执行器通信不正常时是否发信息报警">失联报警：</label>&nbsp;&nbsp;
                            <label  onclick="showContact()" for="warning1" class="radio-label"><input type="radio" name="warning" value="1" id="warning1">是&nbsp;&nbsp;&nbsp;</label>
                            <label  onclick="hideContact()" for="warning0" class="radio-label"><input type="radio" name="warning" value="0" id="warning0">否</label>
                        </div>
                        <div class="form-group contact">
                            <label for="mobiles" class="col-lab control-label" title="执行器通信不正常时将发送短信给此手机">报警手机：</label>
                            <div class="col-md-9">
                                <input type="text" class="form-control " id="mobiles"/>&nbsp;
                            </div>
                        </div>
                        <div class="form-group contact">
                            <label for="email" class="col-lab control-label" title="执行器通信不正常时将发送报告给此邮箱">报警邮箱：</label>
                            <div class="col-md-9">
                                <input type="text" class="form-control " id="email"/>&nbsp;
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <center>
                        <button type="button" class="btn btn-sm"  onclick="save()">保存</button>&nbsp;&nbsp;
                        <button type="button" class="btn btn-sm"  data-dismiss="modal">关闭</button>
                    </center>
                </div>
            </div>
        </div>
    </div>

    <!-- 修改密码弹窗 -->
    <div class="modal fade" id="pwdModal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4>修改密码</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" role="form" id="pwdform">
                        <input type="hidden" id="agentId">
                        <div class="form-group" style="margin-bottom: 4px;">
                            <label for="pwd0" class="col-lab control-label"><i class="glyphicon glyphicon-lock"></i>&nbsp;&nbsp;原&nbsp;&nbsp;密&nbsp;&nbsp;码：</label>
                            <div class="col-md-9">
                                <input type="password" class="form-control " id="pwd0" placeholder="请输入原密码">&nbsp;&nbsp;<label id="oldpwd"></label>
                            </div>
                        </div>
                        <div class="form-group" style="margin-bottom: 20px;">
                            <label for="pwd1" class="col-lab control-label"><i class="glyphicon glyphicon-lock"></i>&nbsp;&nbsp;新&nbsp;&nbsp;密&nbsp;&nbsp;码：</label>
                            <div class="col-md-9">
                                <input type="password" class="form-control " id="pwd1" placeholder="请输入新密码">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="pwd2" class="col-lab control-label"><i class="glyphicon glyphicon-lock"></i>&nbsp;&nbsp;确认密码：</label>
                            <div class="col-md-9">
                                <input type="password" class="form-control " id="pwd2" placeholder="请输入确认密码"/>&nbsp;&nbsp;<label id="checkpwd"></label>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <center>
                    <button type="button" class="btn btn-sm"  onclick="savePwd()">保存</button>&nbsp;&nbsp;
                    <button type="button" class="btn btn-sm"  data-dismiss="modal">关闭</button>
                    </center>
                </div>
            </div>
        </div>
    </div>


    <!-- 修改密码弹窗 -->
    <div class="modal fade" id="sshModal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4>SSH登录</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" role="form" id="sshform">
                        <input type="hidden" id="sship"/>
                        <input type="hidden" id="sshagent"/>
                        <div class="form-group" style="margin-bottom: 4px;">
                            <label for="sshuser" class="col-lab control-label"><i class="glyphicon glyphicon-lock"></i>&nbsp;&nbsp;帐&nbsp;&nbsp;号&nbsp;&nbsp;：</label>
                            <div class="col-md-9">
                                <input type="text" class="form-control " id="sshuser" placeholder="请输入账户">&nbsp;&nbsp;<label id="sshuser_lab"></label>
                            </div>
                        </div>

                        <div class="form-group" style="margin-bottom: 4px;">
                            <label for="sshport" class="col-lab control-label"><i class="glyphicon glyphicon-lock"></i>&nbsp;&nbsp;端&nbsp;&nbsp;口&nbsp;&nbsp;：</label>
                            <div class="col-md-9">
                                <input type="text" class="form-control " id="sshport" placeholder="请输入端口">&nbsp;&nbsp;<label id="sshport_lab"></label>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="sshpwd" class="col-lab control-label"><i class="glyphicon glyphicon-lock"></i>&nbsp;&nbsp;密&nbsp;&nbsp;码&nbsp;&nbsp;：</label>
                            <div class="col-md-9">
                                <input type="password" class="form-control " id="sshpwd" placeholder="请输入密码"/>&nbsp;&nbsp;<label id="sshpwd_lab"></label>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <center>
                        <button type="button" class="btn btn-sm"  onclick="saveSsh()">保存</button>&nbsp;&nbsp;
                        <button type="button" class="btn btn-sm"  data-dismiss="modal">关闭</button>
                    </center>
                </div>
            </div>
        </div>
    </div>


</section>
<br/><br/>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
