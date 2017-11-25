package parkingos.com.bolink.utlis;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Component
public class HttpsProxy {
	private final String METHOD_POST = "POST";
    private final String DEFAULT_CHARSET = "utf-8";
    Logger logger = Logger.getLogger(HttpsProxy.class);
      
    public String doPost(String url, String params, int connectTimeout, int readTimeout) throws Exception {
        String ctype = "application/json;charset=" + DEFAULT_CHARSET;
        byte[] content = {};  
        if(params != null){  
            content = params.getBytes(DEFAULT_CHARSET);
        }  
        return doPost(url, ctype, content, connectTimeout, readTimeout);
    }

    public String doPost(String url, String ctype, byte[] content,
                         int connectTimeout, int readTimeout) throws Exception {
        HttpsURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;
        try {  
            try{  
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()},
                		new SecureRandom());
                SSLContext.setDefault(ctx);
  
                conn = getConnection(new URL(url), METHOD_POST, ctype);
                conn.setHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				}); 
                conn.setConnectTimeout(connectTimeout);  
                conn.setReadTimeout(readTimeout);  
            }catch(Exception e){
                logger.error("GET_CONNECTOIN_ERROR, URL = " + url, e);  
                throw e;  
            }  
            try{  
                out = conn.getOutputStream();  
                out.write(content);  
                rsp = getResponseAsString(conn);  
            }catch(IOException e){
            	logger.error("REQUEST_RESPONSE_ERROR, URL = " + url, e);  
                throw e;  
            }  
              
        }finally {  
            if (out != null) {  
                out.close();  
            }  
            if (conn != null) {  
                conn.disconnect();  
            }  
        }  
          
        return rsp;  
    }

    private class DefaultTrustManager implements X509TrustManager {
    	  
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
  
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
  
        public X509Certificate[] getAcceptedIssuers() {
            return null;  
        }  
  
    }  
      
    private HttpsURLConnection getConnection(URL url, String method, String ctype)
            throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod(method);  
        conn.setDoInput(true);  
        conn.setDoOutput(true);  
        conn.setRequestProperty("Accept", "text/xml,text/javascript,text/html");  
        conn.setRequestProperty("User-Agent", "stargate");  
        conn.setRequestProperty("Content-Type", ctype);  
        return conn;  
    }  
  
    protected String getResponseAsString(HttpURLConnection conn) throws IOException {
        String charset = getResponseCharset(conn.getContentType());
        InputStream es = conn.getErrorStream();
        if (es == null) {  
            return getStreamAsString(conn.getInputStream(), charset);  
        } else {  
            String msg = getStreamAsString(es, charset);
            if (msg==null||"".equals(msg)) {  
                throw new IOException(conn.getResponseCode() + ":" + conn.getResponseMessage());
            } else {  
                throw new IOException(msg);
            }  
        }  
    }  
  
    private String getStreamAsString(InputStream stream, String charset) throws IOException {
        try {  
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
            StringWriter writer = new StringWriter();
  
            char[] chars = new char[256];  
            int count = 0;  
            while ((count = reader.read(chars)) > 0) {  
                writer.write(chars, 0, count);  
            }  
  
            return writer.toString();  
        } finally {  
            if (stream != null) {  
                stream.close();  
            }  
        }  
    }  
  
    private String getResponseCharset(String ctype) {
        String charset = DEFAULT_CHARSET;
  
        if (ctype!=null&&!"".equals(ctype)) {  
            String[] params = ctype.split(";");
            for (String param : params) {
                param = param.trim();  
                if (param.startsWith("charset")) {  
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {  
                        if (pair[1]!=null&&!"".equals(pair[1])) {  
                            charset = pair[1].trim();  
                        }  
                    }  
                    break;  
                }  
            }  
        }  
  
        return charset;  
    }

}
