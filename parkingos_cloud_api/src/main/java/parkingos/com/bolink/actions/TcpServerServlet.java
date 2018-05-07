package parkingos.com.bolink.actions;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import parkingos.com.bolink.netty.NettyChannelMap;
import parkingos.com.bolink.netty.SensorServer;
import parkingos.com.bolink.service.impl.DoUploadImpl;
import parkingos.com.bolink.utlis.Defind;
import parkingos.com.bolink.utlis.ExecutorsUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.concurrent.ExecutorService;


public class TcpServerServlet extends HttpServlet {


	public void destroy() { 
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}
	/**
	 * Initialization of the servlet.
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		try {
			ExecutorService pool = ExecutorsUtil.getExecutorPool();

			//需开两个线程，因为这两个方法都是阻塞的
			pool.execute(new Runnable() {
				@Override
				public void run(){
                try {
					Integer port = Integer.valueOf(Defind.getProperty("TCPPORT"));
                    System.out.println(">>>>>>1>>>>>>>>>>port:"+ port);
                    new SensorServer().bind(port);
                } catch (Exception e) {
                    System.out.println(">>>>>>2>>>>>>>>>>");
                    e.printStackTrace();
                }
            }
			});
		} catch (Exception e) {

			System.out.println(">>>>>>>>3>>>>>>>>");
			e.printStackTrace();
		}
		
		ApplicationContext ctx = WebApplicationContextUtils
				.getWebApplicationContext(getServletContext());
		DoUploadImpl doUploadImpl = (DoUploadImpl) ctx.getBean("doUploadImpl");
		System.err.println("doUploadImpl:"+doUploadImpl);
		NettyChannelMap.doUpload = doUploadImpl;
		//定时查询下发数据
//		CommonDao commonDao = (CommonDao) ctx.getBean("commonDaoMybatisImpl");
//		CommonUtils commonUtils = (CommonUtils)ctx.getBean("commonUtils");
//		new TimerManager(commonDao,commonUtils);
	}
}
