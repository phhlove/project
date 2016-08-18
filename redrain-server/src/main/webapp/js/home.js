var redrainChart = {
    path: "/",
    intervalId: null,
    intervalTime: 2000,
    gauge: null,
    gaugeOption: null,
    screen: 1,//1小屏,2:中屏,3大屏
    data: null,
    socket: null,
    resizeChartData: {},
    diskLoad:false,
    cpuLoad:false,
    configLoad:false,
    cpuChartObj:{},
    cpuX:[],
    cpuY:[],
    config:[],

    overviewDataArr: [
        {"key": "us", "title": "用户占用", "color": "rgba(221,68,255,0.90)"},
        {"key": "sy", "title": "系统占用", "color": "rgba(255,255,255,0.90)"},
        {"key": "memUsage", "title": "内存使用率", "color": ""},
        {"key": "id", "title": "Cpu空闲", "color": "rgba(92,184,92,0.90)"},
        {"key": "swap", "title": "Swap空闲", "color": "rgba(240,173,78,0.90)"}
    ],

    monitorData: function () {

        if ( (arguments[0]||0) ==1 && redrainChart.intervalId==null ) {
            return;
        }

        /**
         * 没有执行器
         */
        if (!$("#agentId").val()) {
            window.setTimeout(function () {
                $(".loader").remove();
            }, 1000);
            return;
        }
        /**
         * 关闭上一个websocket
         */
        if (redrainChart.socket) {
            redrainChart.socket.close();
            redrainChart.socket = null;
        }

        $.ajax({
            url: redrainChart.path + "/monitor",
            data: "agentId=" + $("#agentId").val(),
            dataType: "html",
            success: function (dataResult) {

                if (dataResult.toString().indexOf("login") > -1) {
                    window.location.href = redrainChart.path;
                }
                //remobe loader...
                $(".loader").remove();
                var connType = dataResult.substring(0,1);
                var data =  dataResult.substring(2);
                //代理
                if (connType == 1) {
                    redrainChart.data = $.parseJSON(data);
                    if ( redrainChart.intervalId == null ) {
                        /**
                         * 第一个轮询不显示,等下一个轮询开始渲染...
                         * @type {number}
                         */
                        redrainChart.intervalId = window.setInterval(function () {
                            redrainChart.monitorData(connType);
                        }, redrainChart.intervalTime);
                    }else {
                        redrainChart.doRender();
                    }
                }else {//直联-->发送websocket...
                    if ( redrainChart.intervalId!=null ) {
                        window.clearInterval( redrainChart.intervalId );
                        redrainChart.intervalId = null;
                        redrainChart.clearData();
                    }

                    redrainChart.socket = new io.connect(data, {'reconnect': false, 'auto connect': false});
                    redrainChart.socket.on("monitor", function (data) {
                        redrainChart.data = data;
                        redrainChart.doRender();
                    });
                    //when close then clear data...
                    redrainChart.socket.on("disconnect", function () {
                        console.log('close');
                        redrainChart.clearData();
                    });
                }

            }
        });
    },

    clearData:function () {
        if (redrainChart.socket!=null) {
            redrainChart.socket.close();
            redrainChart.socket = null;
        }
        redrainChart.data = [];
        redrainChart.diskLoad = false;
        redrainChart.cpuLoad = false;
        redrainChart.configLoad = false;
        redrainChart.config = [];
        redrainChart.cpuChartObj = null;
        redrainChart.cpuX = [];
        redrainChart.cpuY = [];
    },

    doRender:function () {
        $(".loader").remove();
        //解决子页面登录失联,不能跳到登录页面的bug
        if (!redrainChart.diskLoad) {
            redrainChart.diskLoad = true;
            redrainChart.diskChart();
        }

        if (!redrainChart.configLoad){
            redrainChart.config = $.parseJSON(redrainChart.data.config);
            redrainChart.configLoad = true;
            redrainChart.configData();
        }

        if (!redrainChart.cpuLoad) {
            redrainChart.cpuLoad = true;
            redrainChart.createItemCpu();
            redrainChart.cpuChartObj = echarts.init(document.getElementById('cpu-chart'));
            var option = {};
            redrainChart.cpuChartObj.setOption(option);
        } else {
            var opt = redrainChart.cpuChart(redrainChart.cpuX, redrainChart.cpuY);
            redrainChart.cpuChartObj.setOption(opt);
            redrainChart.gaugeOption.series[0].data[0].value = parseFloat(redrainChart.data.memUsage);
            redrainChart.gauge.setOption(redrainChart.gaugeOption, true);
        }
        redrainChart.topData();
    },

    diskChart: function () {
        $("#overview-chart").html("").css("height", "auto");
        $("#disk-view").html("");
        $("#disk-item").html("");

        var diskArr = $.parseJSON(redrainChart.data.diskUsage);
        var freeTotal, usedTotal;

        for (var i in diskArr) {
            var disk = diskArr[i].disk;
            var used = parseFloat(diskArr[i].used);
            var free = parseFloat(diskArr[i].free);
            var val = parseInt((used / (used + free)) * 100);

            if (disk == "usage") {
                freeTotal = free;
                usedTotal = used;
                continue;
            }

            var colorCss = "";
            if (val < 60) {
                colorCss = "progress-bar-success";
            } else if (val < 80) {
                colorCss = "progress-bar-warning";
            } else {
                colorCss = "progress-bar-danger";
            }

            var html = '<div class="side-border"><h6><small style="font-weight: lighter"><i class="glyphicon glyphicon-hdd"></i>&nbsp;&nbsp;' + disk + '&nbsp;&nbsp;(已用:' + used + 'G/空闲:' + free + 'G)</small><div class="progress progress-small"><a href="#" data-toggle="tooltip" title="" class="progress-bar tooltips ' + colorCss + '" style="width: ' + val + '%;" data-original-title="' + val + '%"><span class="sr-only">' + val + '%</span></a></div></h6></div>';
            $("#disk-item").append(html);
        }

        $('#disk-view').highcharts({
            chart: {
                type: 'pie',
                backgroundColor: 'rgba(0,0,0,0)',
                options3d: {
                    enabled: true,
                    alpha: 45,
                    beta: 0
                }
            },
            colors: ['rgba(76,224,105,0.45)', 'rgba(237,56,46,0.45)'],
            title: {
                text: ''
            },
            tooltip: {
                pointFormat: '{series.name}:{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    depth: 30,
                    dataLabels: {
                        enabled: false,
                        format: '{point.name}'
                    }
                }
            },
            series: [{
                type: 'pie',
                name: '占比',
                data: [
                    ['空闲磁盘', parseFloat(freeTotal)],
                    ['已用磁盘', parseFloat(usedTotal)]
                ]
            }]
        });
    },

    configData:function () {
        //config...
        $.each( redrainChart.config, function (name, value) {
            var css = {
                "font-size": "15px",
                "font-weight": "900",
                "color": "rgba(255,255,255,0.8)",
                "margin-top": "3px"
            };

            if (typeof(value) == 'string') {
                if (!value) {
                    $("#view-" + name).remove();
                } else {
                    $("#config-" + name).css(css).html(value);
                }
            } else {
                $.each(value, function (k, v) {
                    if (!v) {
                        $("#view-" + name + "-" + k).remove();
                    } else {
                        $("#config-" + name + "-" + k).css(css).html(v);
                    }
                });
            }
        });
        $("#config-view").fadeIn(1000);
    },

    createItemCpu: function () {
        var overdata = [];
        $.each(redrainChart.overviewDataArr, function (i, elem) {
            $.each(redrainChart.data, function (name, obj) {
                if (elem.key == name) {
                    if (name == "swap") {
                        overdata.push([elem.key, 100.00 - parseFloat(obj)]);
                    } else {
                        overdata.push([elem.key, parseFloat(obj)]);
                    }

                }
            });
            var _cpuData = $.parseJSON( redrainChart.data.cpuData );
            for (var k in _cpuData) {
                if (elem.key == k) {
                    overdata.push([k, parseFloat(_cpuData[k])]);
                }
            }
        });

        $.each(overdata, function (i, obj) {
            var key = obj[0];
            var val = obj[1];
            var title = "";

            $.each(redrainChart.overviewDataArr, function (k, v) {
                if (i == k) {
                    title = v.title;
                }
            });

            if (i == 2) {
                if (redrainChart.screen == 2) {
                    var html = "<div style='display: inline-block;position: relative; margin: 3px -25px 0;'><div id=\"gauge-cpu\" class=\"gaugeChart_m\"></div><span class='gaugeChartTitle' ><i class=\"icon\" >&#61881;</i>&nbsp;" + title + "</span></div>";
                    $("#overview-chart").append(html);
                } else {
                    var html = "<div style='display: inline-block;position: relative; margin: 3px -25px 0;'><div id=\"gauge-cpu\" class=\"gaugeChart\"></div><span class='gaugeChartTitle' ><i class=\"icon\" >&#61881;</i>&nbsp;" + title + "</span></div>";
                    $("#overview-chart").append(html);
                }
                redrainChart.gauge = echarts.init(document.getElementById('gauge-cpu'));
                redrainChart.gaugeOption = {
                    series: [
                        {
                            name: '内存使用率',
                            type: 'gauge',
                            startAngle: 180,
                            endAngle: 0,
                            center: ['50%', '90%'],
                            radius: 115,
                            axisLine: {
                                lineStyle: {
                                    color: [[0.2, 'rgb(92,184,92)'], [0.8, 'rgb(240,173,78)'], [1, 'rgb(237,26,26)']],
                                    width: 6
                                }
                            },
                            axisTick: {
                                show: true,
                                splitNumber: 4,
                                length: 8,
                                lineStyle: {
                                    color: 'auto'
                                }
                            },
                            splitLine: {
                                show: true,
                                length: 10,
                                lineStyle: {
                                    color: 'auto'
                                }
                            },
                            pointer: {
                                width: 4,
                                length: '90%',
                                color: 'rgba(255, 255, 255, 0.8)'
                            },
                            title: {
                                show: false
                            },
                            detail: {
                                show: true,
                                backgroundColor: 'rgba(0,0,0,0)',
                                borderWidth: 0,
                                borderColor: '#ccc',
                                offsetCenter: [0, -30],
                                formatter: '{value}%',
                                textStyle: {
                                    fontSize: 20
                                }
                            },
                            data: [{value: 50}]
                        }
                    ]
                };
                redrainChart.gaugeOption.series[0].data[0].value = parseFloat(val);
                redrainChart.gauge.setOption(redrainChart.gaugeOption, true);

            } else {
                var html = $("<div class=\"pie-chart-tiny\" id=\"cpu_" + key + "\"><span class=\"percent\"></span><span style='font-weight: lighter' class=\"pie-title\" title='" + title + "(" + key + ")'><i class=\"icon\" >&#61881;</i>&nbsp;" + title + "</span></div>&nbsp;&nbsp;");
                html.easyPieChart({
                    easing: 'easeOutBounce',
                    barColor: redrainChart.overviewDataArr[i].color,
                    trackColor: 'rgba(0,0,0,0.3)',
                    scaleColor: 'rgba(255,255,255,0.85)',
                    lineCap: 'square',
                    lineWidth: 4,
                    animate: 3000,
                    size: redrainChart.screen == 3 ? ((i == 1 || i == 3) ? 90 : 80) : (redrainChart.screen == 1 ? 120 : 135),
                    onStep: function (from, to, percent) {
                        $(this.el).find('.percent').text(Math.round(percent));
                    }
                });
                html.data('easyPieChart').update(val);
                $("#overview-chart").append(html);
                if ($.isMobile()) {
                    $(".percent").css("margin-top", "35px");
                }
            }
        });

    },

    cpuChart: function (x, y) {
        $.each(redrainChart.overviewDataArr, function (i, elem) {
            var overelem = $("#cpu_" + elem.key);
            if (overelem.length > 0) {
                $.each(redrainChart.data, function (name, obj) {
                    if (elem.key == name) {
                        if (name == "swap") {
                            overelem.data('easyPieChart').update(100.00 - parseFloat(obj));
                        } else {
                            overelem.data('easyPieChart').update(parseFloat(obj));
                        }
                    }
                });
                var _cpuData = $.parseJSON( redrainChart.data.cpuData );
                for (var k in _cpuData) {
                    if (elem.key == k) {
                        overelem.data('easyPieChart').update(parseFloat(_cpuData[k]));
                    }
                }
            }
        });

        //添加新的
        x.push(redrainChart.data.time);
        y.push(parseFloat($.parseJSON( redrainChart.data.cpuData )["usage"]));

        if (y.length == 60 * 10) {
            x.shift();
            y.shift();
        }

        var max = parseInt(Math.max.apply({}, y)) + 1;
        if (max > 100) {
            max = 100;
        }
        return cpuOption = {
            tooltip: {
                trigger: 'axis',
                formatter: "监&nbsp;控&nbsp;时&nbsp;间&nbsp;&nbsp;: {b} <br />CPU使用率 : {c}%",
                textStyle: {
                    fontSize: 12
                }
            },
            title: {
                left: 'center',
                text: ''
            },
            grid: {
                top: '9%',
                left: '0%',
                right: '2%',
                bottom: '8%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                show: false,
                boundaryGap: false,
                splitLine: {show: false},
                data: x
            },
            yAxis: {
                type: 'value',
                boundaryGap: [0, '100%'],
                splitLine: {show: false},
                max: max,
                axisLabel: {
                    show: true,
                    textStyle: {color: 'rgba(255,255,255,0.80)'}
                },
                axisLine: {
                    lineStyle: {color: 'rgba(220,220,255,0.6)'}
                }
            },
            dataZoom: [{
                type: 'inside',
                start: 0,
            }, {
                start: 0,
                backgroundColor: 'rgba(0,0,0,0.05)',
                dataBackgroundColor: 'rgba(0,0,0,0.2)',
                fillerColor: 'rgba(0,0,0,0.3)',
                handleColor: 'rgba(0,0,0,0.9)',
                textStyle: {color: '#aaa'}
            }],
            series: [
                {
                    type: 'line',
                    smooth: false,
                    symbol: 'none',
                    sampling: 'average',
                    itemStyle: {
                        normal: {
                            color: 'rgba(255, 255, 255,0.3)'
                        }
                    },
                    areaStyle: {
                        normal: {
                            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                                offset: 0,
                                color: 'rgba(255, 255, 255,0.3)'
                            }, {
                                offset: 1,

                                color: 'rgba(20, 70, 155,0.2)'
                            }])
                        }
                    },
                    data: y
                }
            ]
        };
    },

    networkChart: function () {
        var network = $.parseJSON( redrainChart.data.network );
        var read = network.read;
        var write = network.write;
        return new Highcharts.Chart({
            chart: {
                type: 'spline',
                backgroundColor: 'rgba(0,0,0,0)',
                plotBorderColor: null,
                plotBackgroundColor: null,
                plotBackgroundImage: '',
                plotBorderWidth: null,
                height: 200,
                renderTo: "network-view"
            },
            title: {
                text: "",
                align: 'left',
                style: {
                    color: 'rgba(255,255,255,0.6)'
                }
            },
            subtitle: {
                text: ''
            },
            xAxis: {
                categories: [redrainChart.data.time],
                boundaryGap: false,
                borderRadius: 2,
                labels: {
                    maxStaggerLines: 1,
                    style: {
                        color: 'rgba(220,220,255,0.8)',
                        fontSize: 0
                    }
                },
                splitLine: {show: false},
                tickLength: 0,
            },
            yAxis: {
                title: {text: ''},
                min: 0,
                minorGridLineWidth: 0,
                alternateGridColor: null,
                gridLineWidth: 0,
                labels: {
                    style: {
                        color: 'rgba(220,220,255,0.8)'
                    }
                },
                lineWidth: 1,
                lineColor: 'rgba(220,220,255,0.8)'
            },
            tooltip: {
                shared: true,
                borderColor: 'rgba(45,45,45,0)',
                backgroundColor: 'rgba(45,45,45,0.75)',
                borderRadius: 5,
                borderWidth: 2,
                shadow: true,
                animation: true,
                crosshairs: {
                    width: 1,
                    color: "rgba(255,255,255,0.5)"
                },
                formatter: function () {
                    var tipHtml = '<b style="color:rgba(255,255,255,0.85);font-size: 12px;">监控时间 :&nbsp;' + redrainChart.data.time + '</b>';
                    $.each(this.points, function () {
                        tipHtml += '<div class="tooltip-item" style="color: rgba(255,255,255,0.85)"> <span> ' + this.series.name + '&nbsp;:&nbsp;</span>' + Highcharts.numberFormat(this.y, 0) + " kb/s</div>";
                    });
                    return tipHtml;
                },
                useHTML: true
            },

            plotOptions: {
                spline: {
                    lineWidth: 2,
                    states: {
                        hover: {
                            lineWidth: 3
                        }
                    },
                    marker: {
                        enabled: false
                    }
                }
            },

            series: [{
                name: '读取速度',
                data: [read],
                color: 'rgba(128,128,255,0.6)'
            }, {
                name: '写入速度',
                data: [write],
                color: 'rgba(255,205,205,0.7)'
            }]
            ,
            navigation: {
                menuItemStyle: {
                    fontSize: '10px'
                }
            }
        });
    },

    topData: function () {
        //title
        //@JSONType(orders={"pid","user","virt","res","cpu","mem","time","command"})
        var shtml = '<tr>' +
            '<td class="noborder" style="width: 20%" title="CPU使用占比">CPU</td>' +
            '<td class="noborder" style="width: 20%" title="内存使用占比">MEM</td>' +
            '<td class="noborder" title="持续时长">TIME</td>' +
            '<td class="noborder" title="所执行的命令">COMMAND</td>' +
            '</tr>';

        var mhtml = '<tr>' +
            '<td class="noborder" title="进程ID">PID</td>' +
            '<td class="noborder" title="进程所属的用户">USER</td>' +
            '<td class="noborder" style="width: 20%" title="CPU使用占比">CPU</td>' +
            '<td class="noborder" style="width: 20%" title="内存使用占比">MEM</td>' +
            '<td class="noborder" title="持续时长">TIME</td>' +
            '<td class="noborder" title="所执行的命令">COMMAND</td>' +
            '</tr>';

        var lhtml = '<tr>' +
            '<td class="noborder" title="进程ID">PID</td>' +
            '<td class="noborder" title="进程所属的用户">USER</td>' +
            '<td class="noborder" title="虚拟内存">VIRI</td>' +
            '<td class="noborder" title="常驻内存">RES</td>' +
            '<td class="noborder" style="width: 20%" title="CPU使用占比">CPU</td>' +
            '<td class="noborder" style="width: 20%" title="内存使用占比">MEM</td>' +
            '<td class="noborder" title="持续时长">TIME</td>' +
            '<td class="noborder" title="所执行的命令">COMMAND</td>' +
            '</tr>';

        $.each($.parseJSON( redrainChart.data.top ), function (i, data) {
            var text = "<tr>";
            var obj = $.parseJSON( data );
            switch (redrainChart.screen) {
                case 1:
                    //小屏去掉pid,user,virt,res...
                    for (var k in obj) {
                        if ('pid' === k || 'user' === k || 'virt' === k || 'res' === k) {
                            continue;
                        }
                        if ('cpu' === k || 'mem' === k) {
                            text += ("<td>" + obj[k] + "%</td>");
                        } else {
                            text += ("<td>" + obj[k] + "</td>");
                        }
                    }
                    break
                case 2:
                    //中屏去掉virt,res
                    for (var k in obj) {
                        if ('virt' === k || 'res' === k) {
                            continue;
                        }
                        if ('cpu' === k || 'mem' === k) {
                            text += ("<td>" + obj[k] + "%</td>");
                        } else {
                            text += ("<td>" + obj[k] + "</td>");
                        }
                    }
                    break
                case 3:
                    //大屏,全部字段显示。。。
                    for (var k in obj) {
                        if ('cpu' === k || 'mem' === k) {
                            var val = obj[k];
                            var colorCss = "";
                            if (val < 60) {
                                colorCss = "progress-bar-success";
                            } else if (val < 80) {
                                colorCss = "progress-bar-warning";
                            } else {
                                colorCss = "progress-bar-danger";
                            }
                            var cpu = '<td> <div class="status pull-right bg-transparent-black-1" style="margin-left: 5px;font-size: 10px;">' +
                                '<span id="agent_number" class="animate-number" data-value="100.00" data-animation-duration="1500">' + val + '</span>%' +
                                '</div>' +
                                '<div class="progress progress-small progress-white">' +
                                '<div class="progress-bar ' + colorCss + '" role="progressbar" data-percentage="' + val + '%" style="width:' + val + '%" aria-valuemin="0" aria-valuemax="100"></div>' +
                                '</div></td>';
                            text += cpu;
                        } else {
                            text += ("<td>" + obj[k] + "</td>");
                        }
                    }
                    break;
            }
            text += '</tr>';
            shtml += text;
            mhtml += text;
            lhtml += text;
        });

        switch (redrainChart.screen) {
            case 1:
                $("#topbody").html(shtml);
                break;
            case 2:
                $("#topbody").html(mhtml);
                break
            case 3:
                $("#topbody").html(lhtml);
                break
        }

    },

    resizeChart: function () {
        if ($.isMobile()) {
            $("#overview_pie_div").remove();
            $("#report_detail").remove();
            $("#overview_report_div").removeClass("col-xs-7").addClass("col-xs-12")
        } else {
            if( $( window).width() < 1024 ){
                $("#overview_pie_div").removeClass("col-xs-3").removeClass("col-xs-4").hide();
                $("#report_detail").removeClass("col-xs-2").hide();
                $("#overview_report_div").removeClass("col-xs-7").removeClass("col-xs-8").addClass("col-xs-12")
            }else if ( $(window).width() < 1280 ) {//1024 ~ 1280
                $("#report_detail").removeClass("col-xs-2").hide();
                $("#overview_report_div").removeClass("col-xs-7").addClass("col-xs-8");
                $("#overview_pie_div").removeClass("col-xs-3").addClass("col-xs-4");
            }else {//>1280
                $("#overview_report_div").removeClass("col-xs-8").addClass("col-xs-7");
                $("#overview_pie_div").removeClass("col-xs-4").addClass("col-xs-3");
                $("#report_detail").addClass("col-xs-2").show();
            }
        }

        //屏幕大于1024显示饼状图
        if($(window).width()>=1024){
            $("#overview_pie").html('');
            $("#overview_pie_div").show();
            $('#overview_pie').highcharts({
                chart: {
                    backgroundColor: 'rgba(0,0,0,0)',
                    plotBackgroundColor: null,
                    plotBorderWidth: null,
                    plotShadow: true,
                    options3d: {
                        enabled: true,
                        alpha: 20,
                        beta: 0
                    }
                },
                colors: ['rgba(110,186,249,0.45)', 'rgba(252,80,76,0.45)', 'rgba(222,222,222,0.45)'],
                title: {text: ''},
                tooltip: {
                    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                },
                plotOptions: {
                    pie: {
                        allowPointSelect: true,
                        cursor: 'pointer',
                        dataLabels: {
                            enabled: false
                        },
                        showInLegend: true,
                        depth: 25
                    }
                },
                series: [{
                    type: 'pie',
                    name: '占比',

                    data: [
                        ['成功', redrainChart.resizeChartData.success],
                        ['失败', redrainChart.resizeChartData.failure],
                        ['被杀', redrainChart.resizeChartData.killed]
                    ]

                }]
            });
        }

        //屏幕大于1280显示占比进度图
        if ($(window).width() >= 1280){

            var job_type = parseFloat(redrainChart.resizeChartData.auto / (redrainChart.resizeChartData.auto + redrainChart.resizeChartData.operator)) * 100;
            if (isNaN(job_type)) {
                $("#job_type").attr("aria-valuenow", 0).css("width", "0%");
            } else {
                $("#job_type").attr("aria-valuenow", job_type).css("width", job_type + "%");
            }

            var job_category = parseFloat(redrainChart.resizeChartData.singleton / (redrainChart.resizeChartData.singleton + redrainChart.resizeChartData.flow)) * 100;
            if (isNaN(job_category)) {
                $("#job_category").attr("aria-valuenow", 0).css("width", "0%");
            } else {
                $("#job_category").attr("aria-valuenow", job_category).css("width", job_category + "%");
            }

            var job_model = parseFloat(redrainChart.resizeChartData.crontab / (redrainChart.resizeChartData.crontab + redrainChart.resizeChartData.quartz)) * 100;
            if (isNaN(job_model)) {
                $("#job_model").attr("aria-valuenow", 0).css("width", "0%");
            } else {
                $("#job_model").attr("aria-valuenow", job_model).css("width", job_model + "%");
            }

            var job_rerun = parseFloat((redrainChart.resizeChartData.success + redrainChart.resizeChartData.failure + redrainChart.resizeChartData.killed - redrainChart.resizeChartData.rerun) / (redrainChart.resizeChartData.success + redrainChart.resizeChartData.failure + redrainChart.resizeChartData.killed)) * 100;
            if (isNaN(job_rerun)) {
                $("#job_rerun").attr("aria-valuenow", 0).css("width", "0%");
            } else {
                $("#job_rerun").attr("aria-valuenow", job_rerun).css("width", job_rerun + "%");
            }

            var job_status = parseFloat(redrainChart.resizeChartData.success / (redrainChart.resizeChartData.success + redrainChart.resizeChartData.failure + redrainChart.resizeChartData.killed)) * 100;
            if (isNaN(job_status)) {
                $("#job_status").attr("aria-valuenow", 0).css("width", "0%");
            } else {
                $("#job_status").attr("aria-valuenow", job_status).css("width", job_status + "%");
            }
        }

        //线型报表
        $("#overview_report_div").show();
        $("#overview_report").html('');
        Morris.Line({
            element: 'overview_report',
            data: redrainChart.resizeChartData.dataArea,
            grid: true,
            axes: true,
            xkey: 'date',
            ykeys: ['success', 'failure', 'killed'],
            labels: ['成功', '失败', '被杀'],
            lineColors: ['rgba(205,224,255,0.5)', 'rgba(237,26,26,0.5)', 'rgba(0,0,0,0.5)'],
            hoverFillColor: 'rgb(45,45,45)',
            lineWidth: 4,
            pointSize: 5,
            hideHover: 'auto',
            smooth: false,
            resize: true
        });
    },

    executeChart: function () {
        $.ajax({
            url: redrainChart.path + "/diffchart",
            data: {
                "startTime": $("#startTime").val(),
                "endTime": $("#endTime").val()
            },
            dataType: "json",
            success: function (data) {
                $("#overview_loader").hide();
                if (data != null) {
                    //折线图
                    var dataArea = [];
                    var successSum = 0;
                    var failureSum = 0;
                    var killedSum = 0;
                    var singleton = 0;
                    var flow = 0;
                    var crontab = 0;
                    var quartz = 0;
                    var rerun = 0;
                    var auto = 0;
                    var operator = 0;

                    for (var i in data) {
                        dataArea.push({
                            date: data[i].date,
                            success: data[i].success,
                            failure: data[i].failure,
                            killed: data[i].killed
                        });
                        successSum += parseInt(data[i].success);
                        failureSum += parseInt(data[i].failure);
                        killedSum += parseInt(data[i].killed);
                        singleton += parseInt(data[i].singleton);
                        flow += parseInt(data[i].flow);
                        crontab += parseInt(data[i].crontab);
                        quartz += parseInt(data[i].quartz);
                        rerun += parseInt(data[i].rerun);
                        auto += parseInt(data[i].auto);
                        operator += parseInt(data[i].operator);
                    }

                    redrainChart.resizeChartData = {
                        "dataArea":dataArea,
                        "success":successSum,
                        "failure":failureSum,
                        "killed":killedSum,
                        "singleton":singleton,
                        "flow":flow,
                        "crontab":crontab,
                        "quartz":quartz,
                        "rerun":rerun,
                        "auto":auto,
                        "operator":operator
                    };
                    redrainChart.resizeChart();
                }
            }
        });
    }
}

$(document).ready(function () {
    var homejs = document.getElementById('homejs').src;
    redrainChart.path = homejs.substr(homejs.indexOf("?") + 1);

    var width = window.screen.width;
    //iphone6以下屏幕大小
    if (width < 375) {
        redrainChart.screen = 1;
    } else if (width >= 375 && width < 1280) {
        redrainChart.screen = 2;
    } else {
        redrainChart.screen = 3;
    }
    redrainChart.monitorData();
    redrainChart.executeChart();

    $("#agentId").change(
        function () {
            //清理上一个轮询...
            if ( redrainChart.intervalId!=null ) {
                window.clearInterval( redrainChart.intervalId );
                redrainChart.intervalId = null;
                redrainChart.clearData();
            }
            redrainChart.monitorData();
        }
    );
});

