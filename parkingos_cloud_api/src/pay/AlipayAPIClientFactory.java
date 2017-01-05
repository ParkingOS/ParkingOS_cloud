/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package pay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;


/**
 * API调用客户端工厂
 * 
 * @author taixu.zqq
 * @version $Id: AlipayAPIClientFactory.java, v 0.1 2014年7月23日 下午5:07:45 taixu.zqq Exp $
 */
public class AlipayAPIClientFactory {

    /** API调用客户端 */
    private static AlipayClient alipayClient;
    
    /**
     * 获得API调用客户端
     * 
     * @return
     */
    public static AlipayClient getAlipayClient(){
        
        if(null == alipayClient){
            alipayClient = new DefaultAlipayClient(AlipayConfig.ALIPAY_GATEWAY, AlipayConfig.APP_ID, 
            		AlipayConfig.private_key, "json", AlipayConfig.input_charset,AlipayConfig.ALIPUBLICKEY4QR);
        }
        return alipayClient;
    }
}
