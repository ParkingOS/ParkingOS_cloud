package parkingos.com.bolink.actions;

import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import parkingos.com.bolink.netty.NettyChannelMap;
import parkingos.com.bolink.netty.SensorServer;
import parkingos.com.bolink.schedule.TimerManager;
import parkingos.com.bolink.service.impl.DoUploadImpl;
import parkingos.com.bolink.utlis.CommonUtils;
import parkingos.com.bolink.utlis.Defind;
import parkingos.com.bolink.utlis.ExecutorsUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.ExecutorService;


/**
 * 从listener里面启动tcp服务，避免shutdown服务器（tomcat）时tcp端口还在。
 * Created by waynelu on 2018/5/9.
 */
public class TcpServerListener implements ServletContextListener {
    Logger logger = Logger.getLogger(TcpServerListener.class);

    private SensorServer tcpServer;
    /*
        容器停止时停止tcp服务
    */
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            logger.info("before shutdown!");
            tcpServer.shutdown();
            logger.info("after shutdown!");
        }
        catch(Exception e){
            logger.info("tcpServer shutdown error",e);
        }

    }
    /*
        容器启动时启动tcp服务
     */
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ExecutorService pool = ExecutorsUtil.getExecutorPool();

            pool.execute(new Runnable() {
                @Override
                public void run(){
                    try {
                        logger.info("before startup!");
                        tcpServer = new SensorServer();
                        Integer port = Integer.valueOf(Defind.getProperty("TCPPORT"));
                        logger.info(">>>>>>1>>>>>>>>>>port:"+ port);
                        tcpServer.bind(port);
                        logger.info("after startup!");
                    } catch (Exception e) {
                        logger.info("tcpServer start error",e);

                    }
                }
            });
            ApplicationContext ctx = WebApplicationContextUtils
                    .getWebApplicationContext(sce.getServletContext());
            DoUploadImpl doUploadImpl = (DoUploadImpl) ctx.getBean("doUploadImpl");
            logger.info("doUploadImpl:"+doUploadImpl);
            NettyChannelMap.doUpload = doUploadImpl;

            //定时查询下发数据
            CommonDao commonDao = (CommonDao) ctx.getBean("commonDaoMybatisImpl");
            CommonUtils commonUtils = (CommonUtils)ctx.getBean("commonUtils");
            new TimerManager(commonDao,commonUtils);

        } catch (Exception e) {

            logger.info("tcpServer pool start error",e);

        }
    }
}

