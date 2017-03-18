package com.zhenlaidian.engine;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.zhenlaidian.bean.FreeOrder;
//<content>
//<message>优惠成功!</message> 
//<info>success</info> 
//</content>

public class FreeOrderParser {

	public static FreeOrder getFreeOrder(InputStream is) throws Exception {
		XmlPullParser parser = XmlPullParserFactory. newInstance().newPullParser();
		FreeOrder freeOrder = new FreeOrder();
		parser.setInput(is, "GB2312");
		int type = parser.getEventType();

		while (type != XmlPullParser.END_DOCUMENT) {
			switch (type) {
			case XmlPullParser.START_TAG:
				if ("message".equals(parser.getName())) {
					String message = parser.nextText();
					System.out.println("解析优惠信息为" + message);
					freeOrder.setMessage(message);
				} else if ("info".equals(parser.getName())) {
					String info = parser.nextText();
					freeOrder.setInfo(info);
				}
				break;
			}
			type = parser.next();
		}
		is.close();
		return freeOrder;
	}
}
