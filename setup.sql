CREATE DATABASE IF NOT EXISTS redrain;

USE redrain;

DROP TABLE IF EXISTS config;
CREATE TABLE config (
  configId tinyint(2) NOT NULL PRIMARY KEY,
  senderEmail varchar(200) DEFAULT NULL COMMENT '发件人的邮箱地址',
  smtpHost varchar(255) DEFAULT NULL,
  smtpPort int(10) DEFAULT NULL,
  password varchar(50) DEFAULT NULL COMMENT '发件人邮箱密码',
  sendUrl varchar(1000) DEFAULT NULL COMMENT '发送短信的URL',
  spaceTime int(11) DEFAULT NULL COMMENT '警告发送的处理间隔(分钟)',
  template text COMMENT '短信模板',
  aeskey varchar(16) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

LOCK TABLES config WRITE;
INSERT INTO config VALUES (1,'you_mail_name','smtp.exmail.qq.com',465,'your_mail_pwd','http://your_url',30,'',NULL);
UNLOCK TABLES;

DROP TABLE IF EXISTS job;
CREATE TABLE job (
  jobId bigint(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  agentId bigint(20) NOT NULL COMMENT '执行器的id',
  jobName varchar(50) NOT NULL COMMENT '作业名称',
  category smallint(10) DEFAULT '0' COMMENT '作业类型,0:单作业,1:流程作业',
  cronType smallint(10) DEFAULT '0' COMMENT '表达式类型',
  cronExp varchar(16) DEFAULT NULL COMMENT 'crontab表达式',
  command varchar(1000) DEFAULT NULL COMMENT '执行时运行的命令',
  execType tinyint(1) NOT NULL COMMENT '0-自动模式,由系统自动调用,1-手动模式(手动执行)',
  comment text DEFAULT NULL COMMENT '简介',
  operateId bigint(20) DEFAULT '-1' COMMENT '操作人的id号',
  updateTime datetime DEFAULT NULL COMMENT '修改日期',
  redo tinyint(1) NOT NULL DEFAULT '0' COMMENT '0--不重新执行此作业,1--重新执行此作业',
  runCount int(11) DEFAULT '0' COMMENT '失败后重跑的次数',
  flowId bigint(10) DEFAULT NULL COMMENT '流程作业的组Id',
  flowNum smallint(10) DEFAULT NULL,
  status smallint(2) DEFAULT '1' COMMENT '1:有效,0:无效,2:',
  lastFlag smallint(2) DEFAULT '0' COMMENT '是否为流程作业的最后一个子作业',
  runModel smallint(2) DEFAULT '0' COMMENT '运行模式,0:串行,1:并行(针对流程作业)',
  KEY INX_AGENT (agentId),
  KEY INX_QUERY (category,cronType,execType,status)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;


DROP TABLE IF EXISTS log;
CREATE TABLE log (
  logId bigint(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  agentId bigint(10) NOT NULL,
  receiverId bigint(20) DEFAULT NULL,
  type smallint(1) NOT NULL COMMENT '0:邮件,1:短信',
  receiver varchar(500) NOT NULL COMMENT '收件人',
  message varchar(1000) DEFAULT NULL COMMENT '发送信息',
  result varchar(1000) DEFAULT NULL,
  sendTime datetime NOT NULL,
  isread int(10) NOT NULL COMMENT '消息是否已读取,0:未读,1:已读(只针对站内信)'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;


DROP TABLE IF EXISTS monitor;
CREATE TABLE monitor (
  monitorId int(10) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  agentId int(10) DEFAULT NULL,
  cpuUs float(10,2) DEFAULT NULL,
  cpuSy float(10,2) DEFAULT NULL,
  cpuId float(10,2) DEFAULT NULL,
  memUsed bigint(10) DEFAULT NULL,
  memFree bigint(10) DEFAULT NULL,
  monitTime datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;


DROP TABLE IF EXISTS record;
CREATE TABLE record (
  recordId bigint(20) unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT,
  parentId bigint(20) DEFAULT NULL COMMENT '重复记录需要记录跑的是哪条父记录',
  jobId bigint(20) NOT NULL COMMENT '该作业对应的作业Id',
  command text NOT NULL COMMENT '执行的命令',
  returnCode int(10) DEFAULT NULL COMMENT '完成的返回值。0--成功，其他都--失败',
  success tinyint(4) DEFAULT NULL COMMENT '完成的返回状态。1--成功，0--失败',
  startTime datetime NOT NULL COMMENT '作业开始时间(如果是自动重执行时,每次执行不修改起始时间)',
  endTime datetime DEFAULT NULL COMMENT '作业结束时间',
  execType int(10) NOT NULL COMMENT '执行类型,0--crontab执行的记录，1--手动执行执行的记录,2--出错后自动重执行执行的记录,3--表示重复执行完的记录',
  message longtext COMMENT '执行调度作业后返回的结果。',
  redoCount int(11) DEFAULT NULL COMMENT '当前第几次自动重试执行',
  status tinyint(1) NOT NULL DEFAULT '0' COMMENT '完成状态 0:正在运行 1:运行完毕 2:正在停止 3:停止完毕',
  pid varchar(50) DEFAULT NULL COMMENT '用于查询进程号的uuid',
  flowGroup bigint(10) DEFAULT NULL,
  flowNum bigint(10) DEFAULT NULL,
  category smallint(2) DEFAULT '0' COMMENT '0:单作业,1:流程作业'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;


DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  roleId int(11) NOT NULL PRIMARY KEY COMMENT '角色ID',
  roleName varchar(50) NOT NULL COMMENT '角色名称',
  description varchar(255) DEFAULT NULL COMMENT '角色描述'
) ENGINE=MyISAM AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

LOCK TABLES `role` WRITE;
INSERT INTO `role` VALUES (1,'管理员','仅具有查看权限'),(999,'超级管理员','具有所有操作权限');
UNLOCK TABLES;

DROP TABLE IF EXISTS term;
CREATE TABLE term (
  termId int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  userId int(11) DEFAULT NULL,
  host varchar(255) DEFAULT NULL,
  user varchar(50) DEFAULT NULL,
  password varchar(50) DEFAULT NULL,
  port int(10) DEFAULT NULL,
  privatekey varchar(255) DEFAULT NULL,
  status smallint(6) DEFAULT '1' COMMENT '连接状态(1:成功,0:失败)',
  logintime datetime DEFAULT NULL,
  UNIQUE KEY UNQ_INX (userId,host)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;


DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  userId bigint(20) NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  roleId int(11) DEFAULT NULL COMMENT '角色ID',
  userName varchar(50) NOT NULL COMMENT '用户名',
  password varchar(50) NOT NULL COMMENT '登录密码',
  headerpic blob DEFAULT NULL COMMENT '头像',
  picExtName varchar(10) DEFAULT NULL COMMENT '图片后缀名',
  salt varchar(16) NOT NULL COMMENT '校验码',
  realName varchar(50) DEFAULT NULL COMMENT '真实姓名',
  contact varchar(200) NOT NULL COMMENT '联系方式',
  email varchar(200) NOT NULL COMMENT '邮箱',
  qq varchar(50) DEFAULT NULL COMMENT 'qq号码',
  createTime datetime NOT NULL COMMENT '创建时间',
  modifyTime timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;


LOCK TABLES `user` WRITE;
INSERT INTO `user`(roleId,userName,password,salt,realName,contact,email,qq,createTime,modifyTime)
VALUES (999,'redrain','016b85818bdc68ba65d6a41e3d8054e693778dee','ece2bae9d384582b','jredrain','13800138000','benjobs@qq.com','123322242','2016-02-17 12:17:19','2016-03-07 03:05:28');
UNLOCK TABLES;


DROP TABLE IF EXISTS `agent`;
CREATE TABLE `agent` (
  agentId bigint(20) unsigned PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  proxyAgent  bigint(20) DEFAULT NULL COMMENT '代理执行器的Id,非代理为空',
  status tinyint(1) DEFAULT NULL COMMENT '通信状态:0通讯异常，1通信正常',
  ip varchar(16) NOT NULL COMMENT '机器ip',
  port int(4) NOT NULL COMMENT '机器端口号',
  password varchar(50) DEFAULT NULL,
  failTime datetime DEFAULT NULL COMMENT '检查通信上一次失败的时间',
  name varchar(100) NOT NULL COMMENT '执行器名字',
  warning smallint(1) DEFAULT '0' COMMENT 'bool.失联后是否通知email报警',
  mobiles varchar(255) DEFAULT NULL COMMENT '接收通知的手机号',
  emailAddress varchar(1000) DEFAULT NULL COMMENT '失联后接受报警的email',
  comment text DEFAULT NULL COMMENT '简介',
  proxy tinyint(1) DEFAULT '0' COMMENT '0:直连,1:代理',
  updateTime timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
