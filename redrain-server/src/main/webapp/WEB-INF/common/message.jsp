<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String port = request.getServerPort() == 80 ? "" : (":"+request.getServerPort());
    String path = request.getContextPath().replaceAll("/$","");
    String contextPath = request.getScheme()+"://"+request.getServerName()+port+path;
    pageContext.setAttribute("contextPath",contextPath);
%>

<style type="text/css">
    .media a:hover {
        color: yellow;
    }
</style>
<div id="messages" class="tile drawer animated">
    <div class="listview narrow">
        <div class="media">
            <i class="glyphicon glyphicon-bell"></i>&nbsp;<a href="#">通知&nbsp;&&nbsp;消息&nbsp;&&nbsp;短信</a>
            <span class="drawer-close">&times;</span>

        </div>
        <div class="overflow" id="msgList">

        </div>
        <div class="media text-center whiter l-100">
            <i class="glyphicon glyphicon-eye-open"></i>&nbsp;<a href="${contextPath}/notice/view">查看全部</a>
        </div>
    </div>
</div>