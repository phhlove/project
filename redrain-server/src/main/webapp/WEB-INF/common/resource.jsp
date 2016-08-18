<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String port = request.getServerPort() == 80 ? "" : (":"+request.getServerPort());
    String path = request.getContextPath().replaceAll("/$","");
    String contextPath = request.getScheme()+"://"+request.getServerName()+port+path;
    pageContext.setAttribute("contextPath",contextPath);
%>
<title>RedRain</title>
<meta name="format-detection" content="telephone=no">
<meta name="description" content="RedRain">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="keywords" content="redrain,crontab,a better crontab,Let's crontab easy">
<meta name="author" content="author:benjobs,wechat:wolfboys,Created by languang(http://u.languang.com) @ 2016" />

<!-- CSS -->
<link href="${contextPath}/css/bootstrap.min.css" rel="stylesheet">
<link href="${contextPath}/css/animate.min.css" rel="stylesheet">
<link href="${contextPath}/css/font-awesome.min.css" rel="stylesheet">
<link href="${contextPath}/css/font-awesome-ie7.min.css" rel="stylesheet">
<link href="${contextPath}/css/form.css" rel="stylesheet">
<link href="${contextPath}/css/calendar.css" rel="stylesheet">
<link href="${contextPath}/css/style.css" rel="stylesheet">
<link href="${contextPath}/css/icons.css" rel="stylesheet">
<link href="${contextPath}/css/generics.css" rel="stylesheet">
<link href='${contextPath}/css/sweetalert.css' rel='stylesheet'>
<link href='${contextPath}/css/redrain.css' rel='stylesheet'>
<link href='${contextPath}/css/loading.css' rel='stylesheet'>
<link href='${contextPath}/css/morris.css' rel='stylesheet'>
<link href='${contextPath}/css/prettify.min.css' rel='stylesheet'>
<link href="${contextPath}/img/favicon.ico" rel="shortcut icon" type="image/ico">

<!-- Javascript Libraries -->
<!-- jQuery -->
<script src="${contextPath}/js/jquery.min.js"></script> <!-- jQuery Library -->
<script src="${contextPath}/js/jquery-ui.min.js"></script> <!-- jQuery UI -->
<script src="${contextPath}/js/jquery.easing.1.3.js"></script> <!-- jQuery Easing - Requirred for Lightbox + Pie Charts-->

<!-- Bootstrap -->
<script src="${contextPath}/js/bootstrap.min.js"></script>
<script src="${contextPath}/js/easypiechart.js"></script> <!-- EasyPieChart - Animated Pie Charts -->

<!--  Form Related -->
<script src="${contextPath}/js/icheck.js"></script> <!-- Custom Checkbox + Radio -->
<script src="${contextPath}/js/select.min.js"></script> <!-- Custom Select -->

<!-- UX -->
<script src="${contextPath}/js/scroll.min.js"></script> <!-- Custom Scrollbar -->

<!-- Other -->
<script src="${contextPath}/js/calendar.min.js"></script> <!-- Calendar -->
<script src="${contextPath}/js/feeds.min.js"></script> <!-- News Feeds -->
<script src="${contextPath}/js/raphael.2.1.2-min.js"></script>
<script src="${contextPath}/js/prettify.min.js"></script>
<script src="${contextPath}/js/morris.min.js"></script>
<script src="${contextPath}/js/jquery.sparkline.min.js"></script>
<!-- All JS functions -->
<script id="themeFunctions" src="${contextPath}/js/functions.js?${contextPath}"></script>

<!--flot-->
<script src="${contextPath}/js/flot/jquery.flot.min.js"></script>
<script src="${contextPath}/js/flot/jquery.flot.resize.min.js"></script>
<script src="${contextPath}/js/flot/jquery.flot.spline.min.js"></script>

<script src="${contextPath}/js/testdevice.js"></script>

<!-- MD5 -->
<script src="${contextPath}/js/md5.js"></script>
<script src="${contextPath}/js/html5.js"></script>
<script src="${contextPath}/js/gauge.js"></script>
<script src="${contextPath}/js/jquery.cookie.js"></script>
<script src="${contextPath}/js/My97DatePicker/WdatePicker.js"></script>
<script src="${contextPath}/js/sweetalert.min.js"></script>
<script src="${contextPath}/js/redrain.js"></script>

<!--upfile-->
<link rel="stylesheet" href="${contextPath}/js/upload/css/jquery.Jcrop.min.css" type="text/css" />
<link rel="stylesheet" href="${contextPath}/css/Dialog.css" type="text/css" />

<script type="text/javascript" src="${contextPath}/js/upload/jquery.Jcrop.min.js" ></script>
<script type="text/javascript" src="${contextPath}/js/upload/jquery.uploadify.min.js"></script>
<script type="text/javascript" src="${contextPath}/js/Dialog.js"></script>


