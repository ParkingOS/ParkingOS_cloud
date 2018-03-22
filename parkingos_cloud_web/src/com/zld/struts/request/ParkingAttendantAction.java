package com.zld.struts.request;

import com.zld.impl.MongoDbUtils;
import com.zld.utils.RequestUtil;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Calendar;


public class ParkingAttendantAction extends Action{

	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(ParkingAttendantAction.class);


	/**
	 *泊车
	 */

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		logger.error(">>>ParkingAttendantAction action:"+action);
		if(action.equals("getpic")){//取泊车点照片
			String fname = RequestUtil.getString(request, "id");
			String dbName = RequestUtil.getString(request, "db");
			if(dbName.equals(""))
				dbName = "park_pics";
			byte [] content = mongoDbUtils.getParkPic(fname,dbName);
			if(content!=null){
				response.setDateHeader("Expires", System.currentTimeMillis()+4*60*60*1000);
				//response.setStatus(httpc);
				Calendar c = Calendar.getInstance();
				c.set(1970, 1, 1, 1, 1, 1);
				response.setHeader("Last-Modified", c.getTime().toString());
				response.setContentLength(content.length);
				response.setContentType("image/jpeg");
				//System.err.println(content.length);
				OutputStream o = response.getOutputStream();
				o.write(content);
				o.flush();
				o.close();
				response.flushBuffer();
			}else {
				response.sendRedirect("images/nopic.jpg");
			}
		}
		/////////============车主请求=======================////////////////////
		return null;
	}

}