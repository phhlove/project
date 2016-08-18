// JavaScript Document

function createMsgWindow(msgId,status){
	$("body").append("<div class='blackWrap' id='blackWrap'>" +
			"<div class='dialog-wrap'>" +
			"	<div class='btn-close' title='关闭' id='btnClose' onclick='modifyStatus("+msgId+","+status+")'></div>" +
			"		<div><iframe width='760' height='430' frameborder='no' border='0' src='/client/user/msg_window?msgID="+msgId+"'></iframe></div>" +
			"	</div>" +
			"</div>")
			
	/**兼容IE9***/
	var userAgent = window.navigator.userAgent.toLowerCase();
	var ms = $.browser.msie && /msie 9\.0/i.test(userAgent);
	if(ms){
		$("#blackWrap").css("background","none");
	}
}
    
/***关闭时修改弹窗状态
 * status用来判断此消息是否已读，如果未读就更改状态，否则关闭弹窗
 * ***/
function modifyStatus(msgId,status){
	if(status==0){
		$.ajax({
			type:"POST",
			url:"/client/user/modify",
			data:{id:msgId},
			success:function(data){
				$("#blackWrap").remove();
				checkNotice();
		  },
		  error:function(){
			  alert("网络异常")
		  }
		});
	}else{
		$("#blackWrap").remove();
	}
	
}

//引入css
var oLink=document.createElement('link');
oLink.rel='stylesheet';
oLink.type='text/css';
oLink.href='/css/Dialog.css';
var oHead=document.getElementsByTagName('head')[0];
oHead.appendChild(oLink);









