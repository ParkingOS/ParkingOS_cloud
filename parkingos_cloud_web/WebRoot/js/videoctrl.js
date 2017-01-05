var isLogin = 0; //是否登录
var gWndId = 0; //播放器控件自有的ID
var curGridCount = 4;
//加载默认服务器
function loadDefaultControl(objID,gridCount){
	return loadControl(objID, "172.16.210.4", "9000", "system", "123456", gridCount);
}
//加载控件，并登录服务器。返回登陆结果状态码。参数：DOM中的控件ID、视频管理服务器IP、端口、登录用户名、密码、控件窗格个数
function loadControl(objID,serverIP, serverPort, username, password, gridCount) {
    var obj = document.getElementById(objID);
    curGridCount = gridCount;
    gWndId = obj.DPSDK_CreateSmartWnd(0, 0, 100, 100); //控件的上下左右四个坐标
    obj.DPSDK_SetWndCount(gWndId, gridCount); //设定4个窗格
    obj.DPSDK_SetSelWnd(gWndId, 0); //选中第0个格子
    //隐藏播放器自带的按钮
    for (var i = 1; i <= 4; i++)
        obj.DPSDK_SetToolBtnVisible(i, false);
    obj.DPSDK_SetToolBtnVisible(7, false);
    obj.DPSDK_SetToolBtnVisible(9, false);
    obj.DPSDK_SetControlButtonShowMode(1, 0);
    obj.DPSDK_SetControlButtonShowMode(2, 0);
    //登录服务器。成功返回0，失败返回错误码
    var result = obj.DPSDK_Login(serverIP, serverPort, username, password);
    if (result == 0) {
        isLogin = 1; //登录
    }
    return result;
}

//在objID对应的播放器控件中，播放channelID对应的视频监控。返回状态码
function playVideo(objID, channelID) {
    var obj = document.getElementById(objID);
    var gridNo = obj.DPSDK_GetSelWnd(gWndId);
    var result = obj.DPSDK_StartRealplayByWndNo(gWndId, gridNo, channelID, 1, 1, 1);
    return result;
}

//按列表中的channelID播放视频。
function playVideos(objID, channelIDList) {
    var obj = document.getElementById(objID);
    var vaildCount = channelIDList.length;
    if (curGridCount < channelIDList.length) {
        vaildCount = curGridCount;
    }
    for (var i = 0; i < vaildCount; i++) {
        obj.DPSDK_StartRealplayByWndNo(gWndId, i, channelIDList[i], 1, 1, 1);
    }
}