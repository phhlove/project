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

    <script type="text/javascript">

        function save(){
            var name = $("#name").val();
            if (!name){
                alert("请填写用户名!");
                return false;
            }
            var password = $("#password").val();
            if (!password){
                alert("请填密码!");
                return false;
            }
            if (password.length < 6 || password.length > 15){
                alert("密码长度请在6-15位之间!");
                return false;
            }
            var pwd = $("#pwd").val();
            if (!pwd){
                alert("请填写确认密码!");
                return false;
            }
            if (pwd.length < 6 || pwd.length > 15){
                alert("密码长度请在6-15位之间!");
                return false;
            }
            if (password != pwd){
                alert("两密码不一致!");
                return false;
            }
            var realName = $("#realName").val();
            if (!realName){
                alert("请填写用户的真实姓名!");
                return false;
            }
            if ($("#contact").val()){
                if(!redrain.testMobile($("#contact").val())){
                    alert("请填写正确的手机号码!");
                    return false;
                }
            }
            if ($("#email").val()){
                if(!redrain.testEmail($("#email").val())){
                    alert("请填写正确的邮箱地址!");
                    return false;
                }
            }
            if ($("#qq").val()){
                if(!redrain.testQq($("#qq").val())){
                    alert("请填写正确的QQ号码!");
                    return false;
                }
            }
            $("#password").val(calcMD5(password));
            $.ajax({
                url:"${contextPath}/user/checkname",
                data:{
                    "name":name
                },
                success:function(data){
                    if (data == "yes"){
                        $("#user").submit();
                        return false;
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

        $(document).ready(function(){

            $("#name").blur(function(){
                if(!$("#name").val()){
                    $("#checkname").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;请填写用户名' + "</font>");
                    return false;
                }
                $.ajax({
                    url:"${contextPath}/user/checkname",
                    data:{
                        "name":$("#name").val()
                    },
                    success:function(data){
                        if (data == "yes"){
                            $("#checkname").html("<font color='green'>" + '<i class="glyphicon glyphicon-ok-sign"></i>&nbsp;用户名可用' + "</font>");
                            return false;
                        }else {
                            $("#checkname").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;用户已存在' + "</font>");
                            return false;
                        }
                    },
                    error : function() {
                        alert("网络繁忙请刷新页面重试!");
                        return false;
                    }
                });
            });

            $("#name").focus(function(){
                $("#checkname").html('<b>*&nbsp;</b>用户名必填');
            });

            $("#pwd").focus(function(){
                $("#checkpwd").html('<b>*&nbsp;</b>必须与上密码一致');
            });

            $("#password").focus(function(){
                $("#checkpassword").html('<b>*&nbsp;</b>密码必填，长度6-20位');
            });

            $("#password").blur(function(){
                if ($("#password").val().length < 6 || $("#password").val().length > 15){
                    $("#checkpassword").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;密码长度请在6-15位之间' + "</font>");
                }
            });

            $("#pwd").change(function(){
                if ($("#pwd").val()==$("#password").val()){
                    $("#checkpwd").html("<font color='green'>" + '<i class="glyphicon glyphicon-ok-sign"></i>&nbsp;两密码一致' + "</font>");
                }else {
                    $("#checkpwd").html("<font color='red'>" + '<i class="glyphicon glyphicon-remove-sign"></i>&nbsp;密码不一致' + "</font>");
                }
            });
        });

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
        <li><a href="">RedRain</a></li>
        <li><a href="">用户管理</a></li>
        <li><a href="">添加用户</a></li>
    </ol>
    <h4 class="page-title"><i class="fa fa-plus" aria-hidden="true"></i>&nbsp;添加用户</h4>

    <div style="float: right;margin-top: 5px">
        <a onclick="goback();" class="btn btn-sm m-t-10" style="margin-right: 16px;margin-bottom: -4px"><i class="fa fa-mail-reply" aria-hidden="true"></i>&nbsp;返回</a>
    </div>

    <div class="block-area" id="basic">
        <div class="tile p-15">
            <form class="form-horizontal" role="form" id="user" action="${contextPath}/user/add" method="post"><br>
                <div class="form-group">
                    <label for="name" class="col-lab control-label"><i class="glyphicon glyphicon-user"></i>&nbsp;&nbsp;用&nbsp;&nbsp;户&nbsp;&nbsp;名：</label>
                    <div class="col-md-10">
                        <input type="text" class="form-control input-sm" id="name" name="userName">
                        <span class="tips" id="checkname"><b>*&nbsp;</b>用户名必填</span>
                    </div>
                </div><br>

                <div class="form-group">
                    <label for="role" class="col-lab control-label"><i class="glyphicon glyphicon-random"></i>&nbsp;&nbsp;用户角色：</label>
                    <div class="col-md-10">
                        <select id="role" name="roleId" class="form-control m-b-10 input-sm">
                            <c:forEach var="r" items="${role}">
                                <option value="${r.roleId}">${r.roleName}</option>
                            </c:forEach>
                        </select>
                    <span class="tips"><b>*&nbsp;</b>角色决定用户的操作权限</span>
                    </div>
                </div><br>

                <div class="form-group">
                    <label for="password" class="col-lab control-label"><i class="glyphicon glyphicon-lock"></i>&nbsp;&nbsp;密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码：</label>
                    <div class="col-md-10">
                        <input type="password" class="form-control input-sm" id="password" name="password">
                        <span class="tips" id="checkpassword"><b>*&nbsp;</b>密码必填，长度6-20位</span>
                    </div>
                </div><br>

                <div class="form-group">
                    <label for="pwd" class="col-lab control-label"><i class="glyphicon glyphicon-lock"></i>&nbsp;&nbsp;确认密码：</label>
                    <div class="col-md-10">
                        <input type="password" class="form-control input-sm" id="pwd">
                        <span class="tips" id="checkpwd"><b>*&nbsp;</b>必须与上密码一致</span>
                    </div>
                </div><br>

                <div class="form-group">
                    <label for="realName" class="col-lab control-label"><i class="glyphicon glyphicon-tag"></i>&nbsp;&nbsp;真实姓名：</label>
                    <div class="col-md-10">
                        <input type="text" class="form-control input-sm" id="realName" name="realName">
                        <span class="tips"><b>*&nbsp;</b>真实姓名必填</span>
                    </div>
                </div><br>


                <div class="form-group">
                    <label for="contact" class="col-lab control-label"><i class="glyphicon glyphicon-comment"></i>&nbsp;&nbsp;联系方式：</label>
                    <div class="col-md-10">
                        <input type="text" class="form-control input-sm" id="contact" name="contact">
                        <span class="tips">选填</span>
                    </div>
                </div><br>

                <div class="form-group">
                    <label for="email" class="col-lab control-label"><i class="glyphicon glyphicon-envelope"></i>&nbsp;&nbsp;电子邮箱：</label>
                    <div class="col-md-10">
                        <input type="text" class="form-control input-sm" id="email" name="email">
                        <span class="tips">选填</span>
                    </div>
                </div><br>

                <div class="form-group">
                    <label for="qq" class="col-lab control-label"><i class="glyphicon glyphicon-magnet"></i>&nbsp;&nbsp;QQ&nbsp;号&nbsp;码：</label>
                    <div class="col-md-10">
                        <input type="text" class="form-control input-sm" id="qq" name="qq">
                        <span class="tips">选填</span>
                    </div>
                </div><br>

                <div class="form-group">
                    <div class="col-md-offset-1 col-md-10">
                        <button type="button"  onclick="save()" class="btn btn-sm m-t-10"><i class="icon">&#61717;</i>&nbsp;保存</button>&nbsp;&nbsp;
                        <button type="button" onclick="history.back()" class="btn btn-sm m-t-10"><i class="icon">&#61740;</i>&nbsp;取消</button>
                    </div>
                </div>
            </form>
        </div>
    </div>

</section>
<br/><br/>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
