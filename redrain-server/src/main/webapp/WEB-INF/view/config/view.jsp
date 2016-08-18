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
        <li><a href="">系统设置</a></li>
    </ol>
    <h4 class="page-title"><i class="glyphicon glyphicon-cog"></i>&nbsp;设置详情</h4>
    <div class="block-area" id="defaultStyle">

        <table class="table tile">
            <tbody id="tableContent">
            <tr>
                <td class="item"><i class="glyphicon glyphicon-envelope"></i>&nbsp;发件邮箱：</td>
                <td>${config.senderEmail}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <a class="green" href="${contextPath}/config/editpage" title="编辑"><i class="glyphicon glyphicon-pencil"></i></a>
                </td>
            </tr>

            <tr>
                <td class="item"><i class="glyphicon glyphicon-map-marker"></i>&nbsp;SMTP地址：</td>
                <td>${config.smtpHost}
                </td>
            </tr>

            <tr>
                <td class="item"><i class="glyphicon glyphicon-filter"></i>&nbsp;SMTP端口：</td>
                <td>${config.smtpPort} &nbsp;&nbsp;（SSL协议）
                </td>
            </tr>

            <tr>
                <td class="item"><i class="glyphicon glyphicon-lock"></i>&nbsp;邮箱密码：</td>
                <td>******</td>
            </tr>
            <tr>
                <td class="item"><i class="glyphicon glyphicon-font"></i>&nbsp;发信URL：</td>
                <td>${config.sendUrl}</td>
            </tr>
            <tr>
                <td class="item"><i class="glyphicon glyphicon-time"></i>&nbsp;发送间隔：</td>
                <td>
                    ${config.spaceTime} 分钟
                </td>
            </tr>
            <tr>
                <td class="item"><i class="glyphicon glyphicon-list-alt"></i>&nbsp;短信模板：</td>
                <td>
                    ${config.template}
                </td>
            </tr>
            </tbody>

        </table>
    </div>

</section>
<br/><br/>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
