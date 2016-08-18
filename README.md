a better crontab job...

redrain(红雨):
    redrain是强大的管理linux crontab任务的系统，基于JAVA开发，可以方便快捷的管理各种复杂的任务计划,包括单一任务,流程任务,各种复制场景下的并行,串行任务,同事有服务器实时监控模块...
    
redrain分为两个redrain-server端和redrain-agent端，redrain-server端即为一个web可视化的中央管理调度平台,redrain-agent为要管理的任务的机器,每个要纳入中央统一管理的机器都必须安装redrain-agent,redrain-agent在要管理的服务器中安装执行完后，可以直接在redrain-server添加当前的机器，


#redrain-agent 安装步骤:
    1)下载编译源码，在redrain-agent的target下会生成一个在redrain-agent-${version}.tar.gz的包
    
    2)部署agent，将redrain-agent-${version}.tar.gz包拷贝到要管理任务的目标服务器,解包,会看到以下目录
     ---bin/
         |  startup.sh         #agent的启动脚本,调用的是redrain.sh来完成
         |  shutdown.sh        #agent停止脚本，调用的是redrain.sh来完成
         |  redrain.sh         #agent控制启动|停止的脚本
         |  monitor.sh         #实时监控获取数据需要的脚本,由系统调度
         |  kill.sh            #kill任务时需要的脚本,由系统调度
     ---conf/
         | log4j.properties    #log4j配置文件
     ---lib/
         | *.jar               #agent运行需要的jar文件
     ---temp/
         | *.sh                #用于存放项目生成的零时文件的目录
     ---logs
         | redrain.out         #项目启动会产生的Log文件
     
     > tar -xzvf redrain-agent-${version}.tar.gz
    3)启动redrain-agent 进入redrain-agent/bin
     > cd redrain-agent/bin
     > sh startup.sh
     这里可以接受两个参数，分别是服务启动的端口和密码，默认端口是:1577,默认密码:redrain
     如要指定参数启动命令如下:
     > sh startup.sh -P10001 -p123456
     参数说明:
      -P (大写的p)为agent启动的端口，选填，如果不输入默认启动端口是1577
      -p (小写的p)为当前agent的连接密码,选填，如果不输入默认连接该机器的密码是redrain
    启动完后会看到有一个redrain.pid，这个文件记录了当前agent进程的pid.更多详细的启动，运行日志请查看logs/redrain.out
       
    4)停止redrain-agent 进入redrain-agent/bin 执行：
      > cd redrain-agent/bin
      > sh shutdown.sh
     
     
#redrain-server 部署步骤:
     1):导入初始化sql,在mysql里导入setup.sql文件
     >mysql -uroot -proot   --进入mysql
     >source /usr/local/setup.sql;
     没啥意外就导入成功了,导入完毕会看到多了一个redrain的库
     2):更改项目的mysql连接信息和memcached连接，配置文件有两套，一个是pord一个是test,
     默认读取的是test下的配置文件,prod下的资源文件maven编译是以"-Ponline"激活，
     mysql和memcached配置在config.properties里，更改即可:
     如:
     --mysql
     jdbc.driverClass=com.mysql.jdbc.Driver
     jdbc.url=jdbc:mysql://${you_mysql_host}:3306/redrain?useUnicode=true&characterEncoding=UTF-8&useCursorFetch=true&autoReconnect=true&failOverReadOnly=false
     jdbc.username=${user}
     jdbc.password=${password}
    
     --memcached
      memcached.server=${memcached_host}:${memcached_port}
     3)maven编译完项目在redrain-server模块下有个redrain-server-${version}.war
     将这个war包发布到tomcat或者其他的web服务器启动即可.
     
     默认初始用户名redrain,密码redrain
     进入到redrain的管理端第一件要做的事情就是添加要管理的执行器.
     在菜单的第二栏点击"执行器管理"->添加执行器
     执行器ip，就是上面你部署的redrain-agent的机器ip，端口号是要连接的redrain-agent的启动端口，密码也是redrain-agent端的连接密码
     输入ip,端口和密码后点击"检查通信",如果成功则server和agnet端已经成功通信，server可以管理agent了,添加保持即可.如果连接失败，
     可能有一下即可原因:
     1):agent端启动失败,检查logs,查看详情
     2):检查agent端口是否开放(如很多云服务器得开放端口才能访问)
  
#注意:
      redrain-agent端编译后如果bin/下的脚本不能成功执行。如 startup.sh脚本不能执行,则:
      >vi startup.sh
      然后查看当前脚本的字符集
      :set ff 回车 （命令模式下,会显示:fileformat=unix或者其他,出问题都是非unix的字符集，然后将字符集改成unix即可)
      :set ff=unix 回车 (保存退出即可)
    
  以上是简单的安装部署,更多任务的管理使用后续会出详细的文档.
  
  redrain交流群156429713,欢迎大家加入
    
    
    
    
