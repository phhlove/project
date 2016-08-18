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

<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/common/resource.jsp"/>
    <style type="text/css">
        .email{
            height: 100%;
            overflow: visible;
            position: relative;
            top: 20px;
            z-index: 50;
            height: 968px !important;
            margin: 0 auto;
            padding: 165px 115px 100px 100px;
            width: 1256px !important;
            margin-top: 5px;
            background: url(${contextPath}/img/bg-mob.png) no-repeat 0 -1249px;
        }
        .email-title{
            height: 126px;
            margin-left: 171px;
            margin-top: 140px;
            width: 864px;
            padding-top: 1px;
            padding-left: 20px;
        }
        .email-content{
            height: 126px;
            margin-left: 171px;
            margin-top: 10px;
            width: 850px;
            padding-top: 0px;
            padding-left: 20px;
        }
        .mobile{
            height: 100%;
            overflow: visible;
            position: relative;
            top: 20px;
            z-index: 50;
            height: 720px !important;
            width: 387px !important;
            margin: 0 auto;
            padding: 125px 25px 159px 25px;
            margin-top: 20px;
            background: url(${contextPath}/img/bg-mob.png) no-repeat 0 -2217px;
        }
        .mobile-in{
            background-color: rgba(255, 255, 255, 0.30);
            height: 463px;
            margin-left: -7px;
            margin-top: -31px;
            width: 351px;
        }

        .message-border{
            width:85%;
            background-color:rgba(0,0,0,0.1);
            margin-top: 20px;
            margin-left: 10px;
            padding: 8px 8px 8px 8px;
            -moz-border-radius: 10px !important;
            -webkit-border-radius: 10px !important;
            border-radius:10px !important;
        }
        ::selection {
            background:#d3d3d3;
            color:#555;
        }
        ::-moz-selection {
            background:#d3d3d3;
            color:#555;
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
        <li><a href="">REDRAIN</a></li>
        <li><a href="">告警日志</a></li>
    </ol>
    <h4 class="page-title"><i class="glyphicon glyphicon-eye-open"></i>&nbsp;日志详情</h4>
    <div class="block-area" id="defaultStyle">
        <button type="button" onclick="history.back()" class="btn btn-sm m-t-10" style="float: right;margin-bottom: 8px;"><i class="icon">&#61740;</i>&nbsp;返回</button>
        <c:if test="${log.type eq 0}">
                <div class="email">
                    <div class="email-title">
                        <h4 style="color: #000000;font-size: 12px;font-weight: 900">cronjob监控告警</h4>
                        <div>
                            <span style="color: #808080;font-size: 12px;">发件人：&nbsp;&nbsp;${sender}</span><br>
                            <span style="color: #808080;font-size: 12px;">时&nbsp;&nbsp;&nbsp;&nbsp;间：&nbsp;&nbsp;<fmt:formatDate value="${log.sendTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span><br>
                            <span style="color: #808080;font-size: 12px;">收件人：&nbsp;&nbsp;${log.receiver}</span><br>
                        </div>
                    </div>
                    <div class="email-content">
                        <span style="color: #0b0b0b;font-size: 15px;font-weight: 600">${log.message}</span>
                    </div>

                </div>
        </c:if>

        <c:if test="${log.type eq 1}">
            <div class="mobile">
                <div class="mobile-in">
                    <span style="color: #31b0d5;font-size: 20px;font-weight: 600"><i class="icon">&#61903;</i>信息</span>
                    <span style="color: #0b0b0b;font-size: 18px;font-weight:600;margin-left: 15px">106905705189615</span>
                    <hr>
                    <center style="color: #393939;font-size: 12px;margin-top: 10px">短信/彩信</center>
                    <center style="color: #393939;font-size: 12px;"><fmt:formatDate value="${log.sendTime}" pattern="yyyy-MM-dd HH:mm:ss"/></center>
                    <div class="message-border">
                        <span style="color: #0b0b0b;font-size: 15px;font-weight: 600">${log.message}</span>
                    </div>
                </div>
                <div>
                </div>
            </div>

        </c:if>
    </div>

</section>
<br/><br/>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
