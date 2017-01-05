package com.zld.utils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

// 动态构造调用串，灵活性更大
public class DynamicHttpclientCall {

    private String namespace="http://tempuri.org/";
    private String methodName;
    private String wsdlLocation="http://218.93.6.98:6017/PYZBike.dll/wsdl/IYZBikeInterFace";
    private String soapResponseData;

    public DynamicHttpclientCall(String methodName) {

        this.methodName = methodName;
    }

    private int invoke(Map<String, String> patameterMap) throws Exception {
        PostMethod postMethod = new PostMethod(wsdlLocation);
        String soapRequestData = buildRequestData(patameterMap);

        byte[] bytes = soapRequestData.getBytes("utf-8");
        InputStream inputStream = new ByteArrayInputStream(bytes, 0,
                bytes.length);
        RequestEntity requestEntity = new InputStreamRequestEntity(inputStream,
                bytes.length, "application/soap+xml; charset=utf-8");
        postMethod.setRequestEntity(requestEntity);

        HttpClient httpClient = new HttpClient();
        int statusCode = httpClient.executeMethod(postMethod);
        soapResponseData = postMethod.getResponseBodyAsString();

        return statusCode;
    }

    private String buildRequestData(Map<String, String> patameterMap) {
        StringBuffer soapRequestData = new StringBuffer();
        soapRequestData.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        soapRequestData
                .append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema\""
                        + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                        + " xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">");
        soapRequestData.append("<soap12:Body>");
        soapRequestData.append("<" + methodName + " xmlns=\"" + namespace
                + "\">");
        soapRequestData.append("<" + methodName + "Request>");

        Set<String> nameSet = patameterMap.keySet();
        for (String name : nameSet) {
            soapRequestData.append("<" + name + ">" + patameterMap.get(name)
                    + "</" + name + ">");
        }
        
        soapRequestData.append("</" + methodName + "Request>");
        soapRequestData.append("</" + methodName + ">");
        soapRequestData.append("</soap12:Body>");
        soapRequestData.append("</soap12:Envelope>");

        return soapRequestData.toString();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub

        DynamicHttpclientCall dynamicHttpclientCall = new DynamicHttpclientCall("YZBikeInterFaceIntf-IYZBikeInterFace#QueryLeaseRecXML");

        Map<String, String> patameterMap = new HashMap<String, String>();
//"<ROOT><ACTNO>6222621410000535967</ACTNO><CERTNO>13579370660</CERTNO><TELNUM>65290119880312044X</TELNUM><BEGINTIME>20160304000000</BEGINTIME>
        //<ENDTIME>20160330235959</ENDTIME><CURRPAGE>1</CURRPAGE><PAGENO>25</PAGENO></ROOT>";

        patameterMap.put("ACTNO", "6222621410000535967");
        patameterMap.put("CERTNO", "13579370660");
        patameterMap.put("TELNUM", "65290119880312044X");
        patameterMap.put("BEGINTIME", "20160304000000");
        patameterMap.put("ENDTIME", "20160330235959");
        patameterMap.put("CURRPAGE", "1");
        patameterMap.put("PAGENO", "25");

        String soapRequestData = dynamicHttpclientCall.buildRequestData(patameterMap);
        
        
        System.out.println(soapRequestData);

        int statusCode = dynamicHttpclientCall.invoke(patameterMap);
        if(statusCode == 200) {
           System.out.println("调用成功！");
           System.out.println(dynamicHttpclientCall.soapResponseData);
       }
       else {
           System.out.println("调用失败！错误码：" + statusCode);
       }
       
   }

}