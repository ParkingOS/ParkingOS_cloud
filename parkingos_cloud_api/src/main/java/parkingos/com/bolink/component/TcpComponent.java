package parkingos.com.bolink.component;

public interface TcpComponent {

    /**
     * 向SDK发送消息
     * @param comId 云平台车场编号
     * @param data 要发送的数据,&和=的键值对
     * @return 0系统错误;1发送成功;2发送失败;3车场离线
     */
    public int sendMessageToSDK(Long comId, String data);

}
