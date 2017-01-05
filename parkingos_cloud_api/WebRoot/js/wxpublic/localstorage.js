
var hxmsg = window.localStorage.getItem("hxmsg");

function checkMsg(){
	var readsize = 0;
	var unreadsize = 0;
	if(hxmsg != null){
		var allinfo = eval("(" + hxmsg + ")");
		for(var i=0; i<allinfo.length;i++){
			var content = allinfo[i].content;
			for(var j=0; j<content.length; j++){
				var msgobj = content[j];
				var flag = msgobj.flag;
				var msgsize = msgobj.msg.length * 2;
				if(parseInt(flag) == 0){
					unreadsize += msgsize;
				}else{
					readsize += msgsize;
				}
			}
		}
	}
	
	if(readsize > 1 *1024 *1024){//已读消息大于1M，清除已读消息
		cleanMsg(1);
	}
	
	if(unreadsize > 2*1024*1024){//未读消息大于2M，清除消息
		cleanMsg(1);
		cleanMsg(0);
	}
}

Array.prototype.remove=function(dx) 
{ 
    if(isNaN(dx) || dx > this.length){
    	return false;
    } 
    for(var i=0,n=0;i<this.length;i++) 
    { 
        if(this[i] != this[dx])
	        { 
	            this[n++]=this[i];
	        } 
    } 
    this.length-=1;
};

function cleanMsg(flag){
	if(hxmsg != null){
		var allinfo = eval("(" + hxmsg + ")");
		for(var i=0; i<allinfo.length;i++){
			var content = allinfo[i].content;
			for(var j=content.length -1; j>=0; j--){//倒着排序删除，否则下标错误
				var msgobj = content[j];
				var f = msgobj.flag;
				if(f == flag){
					content.remove(j);
				}
			}
		}
		
		window.localStorage.removeItem("hxmsg");//Iphone有可能报错，所以加这一行
		window.localStorage.setItem("hxmsg",JSON.stringify(allinfo));
	}
}

checkMsg();