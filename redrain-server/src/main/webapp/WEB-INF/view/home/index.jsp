<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ben" uri="ben-taglib" %>
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

    <script src="${contextPath}/js/echarts.min.js"></script>
    <script src="${contextPath}/js/highcharts/js/highcharts.js"></script>
    <script src="${contextPath}/js/highcharts/js/highcharts-more.js"></script>
    <script src="${contextPath}/js/highcharts/js/highcharts-3d.js"></script>
    <script src="${contextPath}/js/highcharts/js/modules/exporting.js"></script>
    <script src="${contextPath}/js/socket/socket.io.js"></script>
    <script src="${contextPath}/js/socket/websocket.js"></script>
    <script src="${contextPath}/js/home.js?${contextPath}" id="homejs"></script>

    <style type="text/css">

        #config-view h6 {
            margin-bottom: 10px;
            margin-top: 4px;
            color: rgba(255, 255, 255, 0.85);
            font-weight: bold;
        }

        .side-border small {
            color: rgba(255, 255, 255, 0.85);
            font-size: 12px;
            font-weight: bold;
        }

        .pie-title {
            color: rgba(235, 235, 235, 0.85);
            font-size: 12px;
            font-weight: bold;
        }

        .labact{
            color:rgb(255,255,255);
            font-weight: lighter;
        }

        .block-area{
            margin-top: -15px;
        }

        .disk-item{
            font-weight: lighter;
        }

        #config-view counts{
            font-weight: lighter;
        }

        #config-view h6{
            font-weight: lighter;
        }

        .main-chart {
            font-weight: lighter;
        }

        .report_detail {
            margin-top: 5px;
            margin-bottom: 15px;
        }


        .noborder{
            font-family: "Roboto","Arial",sans-serif;
            color: rgba(192, 192, 192,0.9);
            font-weight: lighter;
        }

        .pull-left i {
            color: rgba(225,225,225,0.9);
        }

    </style>

    <script type="text/javascript">
        $(document).ready(function () {
            var agent_number = (parseFloat("${success}")/parseFloat("${fn:length(agents)}")*100).toFixed(2);
            if( isNaN(agent_number) ){
                $("#agent_number").text(0).attr("data-value",0);
                $("#agent_number_prop").attr("data-percentage","0%").css("width","0%");
            }else {
                $("#agent_number").text(agent_number).attr("data-value",agent_number);
                $("#agent_number_prop").attr("data-percentage",agent_number+"%").css("width",agent_number+"%");
            }

            var job_number = (parseFloat("${singleton}")/parseFloat("${job}")*100).toFixed(2);
            if(isNaN(job_number)){
                $("#job_number").text(0).attr("data-value",0);
                $("#job_number_prop").attr("data-percentage","0%").css("width","0%");
            }else {
                $("#job_number").text(job_number).attr("data-value",job_number);
                $("#job_number_prop").attr("data-percentage",job_number+"%").css("width",job_number+"%");
            }

            var ok_number = (parseFloat("${successAutoRecord}")/parseFloat("${successRecord}")*100).toFixed(2);
            if(isNaN(ok_number)){
                $("#ok_number").text(0).attr("data-value",0);
                $("#ok_number_prop").attr("data-percentage","0%").css("width","0%");
            }else {
                $("#ok_number").text(ok_number).attr("data-value",ok_number);
                $("#ok_number_prop").attr("data-percentage",ok_number+"%").css("width",ok_number+"%");
            }

            var no_number = (parseFloat("${failedAutoRecord}")/parseFloat("${failedRecord}")*100).toFixed(2);
            if(isNaN(no_number)){
                $("#no_number").text(0).attr("data-value",0);
                $("#no_number_prop").attr("data-percentage","0%").css("width"+"0%");
            }else {
                $("#no_number").text(no_number).attr("data-value",no_number);
                $("#no_number_prop").attr("data-percentage",no_number+"%").css("width",no_number+"%");
            }

            if($.isMobile()){
                $("#startTime").css("width","80px").removeClass("Wdate").addClass("mWdate");
                $("#endTime").css("width","80px").removeClass("Wdate").addClass("mWdate");
            }

            $(window).resize(function(){
                redrainChart.resizeChart();
                $("#cpu-chart").find("div").first().css("width","100%").find("canvas").first().css("width","100%");
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
        <li><a href="">首页</a></li>
    </ol>

    <h4 class="page-title" ><i class="fa fa-tachometer" aria-hidden="true" style="font-style: 30px;"></i>&nbsp;作业报告</h4>
    <!-- Quick Stats -->
    <div class="block-area" id="overview" style="margin-top: 0px">
        <!-- cards -->
        <div class="row cards">
            <div class="card-container col-lg-3 col-sm-6 col-sm-12">
                <div class="card hover">
                    <div class="front">
                        <div class="media">
                            <span class="pull-left"><i style="font-size: 60px;margin-top: 0px;" aria-hidden="true" class="fa fa-desktop"></i></span>
                            <div class="media-body">
                                <small>执行器</small>
                                <h2 data-animation-duration="1500" data-value="0" class="media-heading animate-number">${fn:length(agents)}</h2>
                            </div>
                        </div>

                        <div class="progress-list">
                            <div class="details">
                                <div class="title">通信状态(正常机器/失联机器)</div>
                            </div>
                            <div class="status pull-right bg-transparent-black-1">
                                <span data-animation-duration="1500" data-value="" class="animate-number" id="agent_number" ></span>%
                            </div>
                            <div class="progress progress-sm progress-transparent-black">
                                <div data-percentage="0%" class="progress-bar animate-progress-bar" id="agent_number_prop"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card-container col-lg-3 col-sm-6 col-sm-12">
                <div class="card hover">
                    <div class="front">
                        <div class="media">
                            <span class="pull-left"><i style="font-size: 60px;margin-top: 1px;" aria-hidden="true" class="fa fa-tasks"></i></span>
                            <div class="media-body">
                                <small>作业数</small>
                                <h2 data-animation-duration="1500" data-value="0" class="media-heading animate-number">${job}</h2>
                            </div>
                        </div>

                        <div class="progress-list">
                            <div class="details">
                                <div class="title">作业类型(单一任务/流程任务)</div>
                            </div>
                            <div class="status pull-right bg-transparent-black-1">
                                <span data-animation-duration="1500" data-value="" class="animate-number" id="job_number"></span>%
                            </div>
                            <div class="progress progress-sm progress-transparent-black">
                                <div data-percentage="0%" class="progress-bar animate-progress-bar" id="job_number_prop"></div>
                            </div>
                        </div>
                    </div>

                </div>
            </div>

            <div class="card-container col-lg-3 col-sm-6 col-sm-12">
                <div class="card hover">
                    <div class="front">
                        <div class="media">
                            <span class="pull-left"><i style="font-size: 60px;margin-top: 0px;" class="fa fa-thumbs-o-up" aria-hidden="true"></i></span>
                            <div class="media-body">
                                <small>成功作业</small>
                                <h2 data-animation-duration="1500" data-value="0" class="media-heading animate-number">${successRecord}</h2>
                            </div>
                        </div>

                        <div class="progress-list">
                            <div class="details">
                                <div class="title">执行类型(自动执行/手动执行)</div>
                            </div>
                            <div class="status pull-right bg-transparent-black-1">
                                <span data-animation-duration="1500" data-value="" class="animate-number" id="ok_number"></span>%
                            </div>
                            <div class="progress progress-sm progress-transparent-black">
                                <div data-percentage="0%" class="progress-bar animate-progress-bar" id="ok_number_prop"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card-container col-lg-3 col-sm-6 col-sm-12">
                <div class="card hover">
                    <div class="front">
                        <div class="media">
                            <span class="pull-left"><i style="font-size: 60px;margin-top: -3px;" class="fa fa-thumbs-o-down" aria-hidden="true"></i></span>
                            <div class="media-body">
                                <small>失败作业</small>
                                <h2 data-animation-duration="1500" data-value="0" class="media-heading animate-number">${failedRecord}</h2>
                            </div>
                        </div>

                        <div class="progress-list">
                            <div class="details">
                                <div class="title">执行类型(自动执行/手动执行)</div>
                            </div>
                            <div class="status pull-right bg-transparent-black-1">
                                <span data-animation-duration="1500" data-value="" class="animate-number" id="no_number"></span>%
                            </div>
                            <div class="progress progress-sm progress-transparent-black">
                                <div data-percentage="0%" class="progress-bar animate-progress-bar" id="no_number_prop"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>
        <!-- /cards -->

    </div>

    <div class="block-area col-xs-12" id="monitor" style="margin-bottom: 15px;">
        <div class="tile" style="border-bottom-left-radius:0px;border-bottom-right-radius: 0px;">

            <h2 class="tile-title" >
                <i aria-hidden="true" class="fa fa-bar-chart"></i>&nbsp;执行报告
            </h2>

            <div class="col-xs-12" style="background-color: rgba(0,0,0,0.3);">
                <div style="float: right;margin-bottom: 0px;margin-top: 0px;margin-right:10px;">
                    <label for="startTime" class="label-self">时间&nbsp;: </label>
                    <input type="text" style="border-radius: 2px;width: 90px" id="startTime" name="startTime" value="${startTime}" onfocus="WdatePicker({onpicked:function(){},dateFmt:'yyyy-MM-dd'})" class="Wdate select-self"/>
                    <label for="endTime" class="label-self">&nbsp;至&nbsp;</label>
                    <input type="text" style="border-radius: 2px;width: 90px" id="endTime" name="endTime" value="${endTime}" onfocus="WdatePicker({onpicked:function(){},dateFmt:'yyyy-MM-dd'})" class="Wdate select-self"/>&nbsp;
                    <button onclick="redrainChart.executeChart()" class="btn btn-default btn-sm" style="vertical-align:top;height: 25px;" type="button"><i class="glyphicon glyphicon-search"></i>查询</button>
                </div>
            </div>

           <div class="col-xs-7" id="overview_report_div" style="background-color: rgba(0,0,0,0.3);display: none">
               <div id="overview_report" style="height: 300px;" class="main-chart" ></div>
           </div>

           <div id="report_detail" class="col-xs-2" style="background-color: rgba(0,0,0,0.3);height: 300px;padding-top:15px;display: none">
               <h5 class="subtitle mb5" style="font-size: 20px;">报告明细</h5>
               <div class="clearfix"></div>

               <span class="sublabel">运行模式(自动/手动)</span>
               <div class="progress progress-sm report_detail">
                   <div class="progress-bar progress-bar-primary" role="progressbar" id="job_type" aria-valuenow="" aria-valuemin="0" aria-valuemax="100"></div>
               </div><!-- progress -->

               <span class="sublabel">作业类型(单一/流程）</span>
               <div class="progress progress-sm report_detail">
                   <div class="progress-bar progress-bar-success" role="progressbar" id="job_category" aria-valuenow="" aria-valuemin="0" aria-valuemax="100"></div>
               </div><!-- progress -->

               <span class="sublabel">规则类型(crontab/quartz)</span>
               <div class="progress progress-sm report_detail">
                   <div class="progress-bar progress-bar-danger" role="progressbar"  id="job_model" aria-valuenow="" aria-valuemin="0" aria-valuemax="100"></div>
               </div><!-- progress -->

               <span class="sublabel">重跑状态 (非重跑/重跑)</span>
               <div class="progress progress-sm report_detail">
                   <div class="progress-bar progress-bar-warning" role="progressbar"  id="job_rerun"  aria-valuenow="" aria-valuemin="0" aria-valuemax="100"></div>
               </div><!-- progress -->

               <span class="sublabel">执行状态(成功/失败)</span>
               <div class="progress progress-sm report_detail">
                   <div class="progress-bar progress-bar-success" role="progressbar" id="job_status" aria-valuenow="" aria-valuemin="0" aria-valuemax="100"></div>
               </div><!-- progress -->
           </div>

            <div class="col-xs-3 " id="overview_pie_div" style="background-color: rgba(0,0,0,0.3);display: none">
                 <div id="overview_pie" class="main-chart" style="height: 300px;" ></div>
            </div>

            <div class="col-xs-12" id="overview_loader" style="background-color: rgba(0,0,0,0.3);height: 300px;">
                 <div class="loader">
                     <div class="loader-inner">
                         <div class="loader-line-wrap">
                             <div class="loader-line"></div>
                         </div>
                         <div class="loader-line-wrap">
                             <div class="loader-line"></div>
                         </div>
                         <div class="loader-line-wrap">
                             <div class="loader-line"></div>
                         </div>
                         <div class="loader-line-wrap">
                             <div class="loader-line"></div>
                         </div>
                         <div class="loader-line-wrap">
                             <div class="loader-line"></div>
                         </div>
                     </div>
                 </div>
            </div>


        </div>
    </div>

    <h4 class="page-title" ><i class="icon">&#61881;</i> &nbsp;监控概况</h4>
    <!-- Main Widgets -->
    <div class="block-area" id="monitor" style="margin-top: 0px">

        <div class="row">
            <div class="col-md-8">
                <!-- overview -->
                <div class="tile" style="background: none">
                    <h2 class="tile-title" style="background:rgba(0,0,0,0.40);border-top-left-radius:2px;border-top-right-radius:2px;"><i aria-hidden="true" class="fa fa-area-chart"></i>&nbsp;系统概况</h2>
                    <div class="tile-config dropdown" style="float: right;">
                        <select class="form-control input-sm m-b-10" style="width: 120px;border-radius: 2px;"ps  id="agentId">
                            <c:forEach var="w" items="${agents}">
                                <option value="${w.agentId}" ${w.agentId eq agentId ? 'selected' : ''}>${w.name}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div id="overview-chart" style="background:rgba(0,0,0,0.40);border-bottom-left-radius:2px;border-bottom-right-radius:2px;height: 192px;" class="p-10 text-center">

                        <div class="loader">
                            <div class="loader-inner">
                                <div class="loader-line-wrap">
                                    <div class="loader-line"></div>
                                </div>
                                <div class="loader-line-wrap">
                                    <div class="loader-line"></div>
                                </div>
                                <div class="loader-line-wrap">
                                    <div class="loader-line"></div>
                                </div>
                                <div class="loader-line-wrap">
                                    <div class="loader-line"></div>
                                </div>
                                <div class="loader-line-wrap">
                                    <div class="loader-line"></div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>

                <div class="tile" id="top" style="min-height: 250px">
                    <h2 class="tile-title"><i aria-hidden="true" class="fa fa-server"></i>&nbsp;进程监控</h2>
                    <div style="margin-left: 15px;margin-right: 15px;">
                    <table class="table tile table-custom table-sortable " style="font-size: 13px;background-color: rgba(0,0,0,0);">
                        <tbody id="topbody" style="color: #fafafa;font-size:12px;">
                            <div class="loader" >
                                <div class="loader-inner">
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                </div>
                            </div>
                        </tbody>
                    </table>
                    </div>
                </div>


                <!-- CPU -->
                <div class="tile" id="cpu">
                    <h2 class="tile-title"><i aria-hidden="true" class="fa fa-line-chart"></i>&nbsp;CPU使用率</h2>
                   <%-- <p style="margin-left: 10px;margin-right: 10px;color: rgb(222,222,222)">
                        &nbsp;&nbsp;&nbsp;&nbsp;CPU利用率分为<span class="labact">用户态</span>，<span class="labact">系统态</span>和<span class="labact">空闲态</span>
                        分别表示CPU处于<span class="labact">用户态执行的时间</span>，<span class="labact">系统内核执行的时间</span>，和<span class="labact">空闲系统进程执行的时间</span>
                        这里的CPU利用率是指：CPU执行非系统空闲进程的时间 / CPU总的执行时间<br>
                        服务器的CPU利用率高,则表明服务器很繁忙。如果前台响应时间越来越大，而后台CPU利用率始终上不去，说明在某个地方有瓶颈了,系统需要调优
                    </p>--%>
                    <div class="p-t-10 p-r-5 p-b-5">
                        <div style="height: 200px; padding: 0px; position: relative;" id="cpu-chart">
                            <div class="loader">
                                <div class="loader-inner">
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                    <div class="loader-line-wrap">
                                        <div class="loader-line"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <!--config-->
            <div class="col-md-4" id="info">

                <div class="tile">
                    <h2 class="tile-title"><i aria-hidden="true" class="fa fa-pie-chart"></i>&nbsp;机器信息</h2>
                    <div class="loader">
                        <div class="loader-inner">
                            <div class="loader-line-wrap">
                                <div class="loader-line"></div>
                            </div>
                            <div class="loader-line-wrap">
                                <div class="loader-line"></div>
                            </div>
                            <div class="loader-line-wrap">
                                <div class="loader-line"></div>
                            </div>
                            <div class="loader-line-wrap">
                                <div class="loader-line"></div>
                            </div>
                            <div class="loader-line-wrap">
                                <div class="loader-line"></div>
                            </div>
                        </div>
                    </div>

                    <div class="p-t-10 p-r-5 p-b-5">
                        <div id="disk-view" class="main-chart" style="height: 250px;margin-top: 10px;"></div>
                    </div>
                    <div class="s-widget-body" id="disk-item"></div>
                    <div class="listview narrow" id="config-view" style="margin-top: -17px;display: none;">

                        <div class="media" id="view-hostname">
                            <div class="pull-right">
                                <div class="counts" id="config-hostname"></div>
                            </div>
                            <div class="media-body">
                                <h6><i class="glyphicon glyphicon-leaf"></i>&nbsp;&nbsp;主&nbsp;机&nbsp;名</h6>
                            </div>
                        </div>

                        <div class="media" id="view-os">
                            <div class="pull-right">
                                <div class="counts" id="config-os"></div>
                            </div>
                            <div class="media-body">
                                <h6><i class="glyphicon glyphicon-globe"></i>&nbsp;&nbsp;系统名称</h6>
                            </div>
                        </div>

                        <div class="media" id="view-kernel">
                            <div class="pull-right">
                                <div class="counts" id="config-kernel"></div>
                            </div>
                            <div class="media-body">
                                <h6><i class="glyphicon glyphicon-info-sign"></i>&nbsp;&nbsp;内核版本</h6>
                            </div>
                        </div>

                        <div class="media" id="view-name">
                            <div class="pull-right">
                                <div class="counts" id="config-cpuinfo-name"></div>
                            </div>
                            <div class="media-body">
                                <h6><i class="glyphicon glyphicon-star-empty"></i>&nbsp;&nbsp;CPU名称</h6>
                            </div>
                        </div>

                        <div class="media" id="view-machine">
                            <div class="pull-right">
                                <div class="counts" id="config-machine"></div>
                            </div>
                            <div class="media-body">
                                <h6><i class="glyphicon glyphicon-list-alt"></i>&nbsp;&nbsp;CPU架构</h6>
                            </div>
                        </div>

                        <div class="media" id="view-cpuinfo-count">
                            <div class="pull-right">
                                <div class="counts" id="config-cpuinfo-count"></div>
                            </div>
                            <div class="media-body">
                                <h6><i class="glyphicon glyphicon-certificate"></i>&nbsp;&nbsp;CPU核数</h6>
                            </div>
                        </div>

                        <div class="media" id="view-cpuinfo-info">
                            <div class="pull-right">
                                <div class="counts" id="config-cpuinfo-info"></div>
                            </div>
                            <div class="media-body">
                                <h6><i class="glyphicon glyphicon-fire"></i>&nbsp;&nbsp;CPU频率</h6>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

        </div>
    </div>

</section>

<jsp:include page="/WEB-INF/common/footer.jsp"/>

