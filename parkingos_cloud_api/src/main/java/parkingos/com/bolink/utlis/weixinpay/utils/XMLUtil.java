package parkingos.com.bolink.utlis.weixinpay.utils;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * xml工具类
 * @author miklchen
 *
 */
public class XMLUtil {

	/**
	 * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
	 * @param strxml
	 * @return
	 * @throws IOException
	 */
	public static Map doXMLParse(String strxml) throws JDOMException, IOException {
		strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");

		if(null == strxml || "".equals(strxml)) {
			return null;
		}

		Map m = new HashMap();

		InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);
		Element root = doc.getRootElement();
		List list = root.getChildren();
		Iterator it = list.iterator();
		while(it.hasNext()) {
			Element e = (Element) it.next();
			String k = e.getName();
			String v = "";
			List children = e.getChildren();
			if(children.isEmpty()) {
				v = e.getTextNormalize();
			} else {
				v = XMLUtil.getChildrenText(children);
			}

			m.put(k, v);
		}

		//关闭流
		in.close();

		return m;
	}

	public static void main(String[] args) {
		String str ="<xml><appid><![CDATA[wx485c58b62cbb4dd0]]></appid><attach><![CDATA[15801482643_1_1022_3_20140815]]></attach><bank_type><![CDATA[WX]]></bank_type><body><![CDATA[test]]></body><fee_type><![CDATA[1]]></fee_type><input_charset><![CDATA[UTF-8]]></input_charset><mch_id><![CDATA[1332937401]]></mch_id><nonce_str><![CDATA[f75b757d3459c3e93e98ddab7b903938]]></nonce_str><notify_url><![CDATA[http://s.tingchebao.com/zld/weixihandle]]></notify_url><out_trade_no><![CDATA[7a88e7aa4a8076688152285096b144b7]]></out_trade_no><spbill_create_ip><![CDATA[192.168.0.188]]></spbill_create_ip><total_fee><![CDATA[10000]]></total_fee><trade_type><![CDATA[APP]]></trade_type><sign><![CDATA[7A5459162DC4120A614FBF7A04AFD4CC]]></sign></xml>";
		try {
			System.out.println(doXMLParse(str));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 获取子结点的xml
	 * @param children
	 * @return String
	 */
	public static String getChildrenText(List children) {
		StringBuffer sb = new StringBuffer();
		if(!children.isEmpty()) {
			Iterator it = children.iterator();
			while(it.hasNext()) {
				Element e = (Element) it.next();
				String name = e.getName();
				String value = e.getTextNormalize();
				List list = e.getChildren();
				sb.append("<" + name + ">");
				if(!list.isEmpty()) {
					sb.append(XMLUtil.getChildrenText(list));
				}
				sb.append(value);
				sb.append("</" + name + ">");
			}
		}

		return sb.toString();
	}

	/**
	 * 获取xml编码字符集
	 * @param strxml
	 * @return
	 * @throws IOException
	 */
	public static String getXMLEncoding(String strxml) throws JDOMException, IOException {
		InputStream in = HttpClientUtil.String2Inputstream(strxml);
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);
		in.close();
		return (String)doc.getProperty("encoding");
	}


}
