package parkingos.com.bolink.actions;

import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.SyncInfoPoolTb;
import parkingos.com.bolink.beans.VisitorTb;
import parkingos.com.bolink.service.WeixinCurOrderService;
import parkingos.com.bolink.utlis.AjaxUtil;
import parkingos.com.bolink.utlis.RequestUtil;
import parkingos.com.bolink.utlis.TimeTools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 访客管理
 *
 * @author c
 */
@Controller
@RequestMapping("/visitor")
public class VistorAction {

    Logger logger = Logger.getLogger(VistorAction.class);
    @Autowired
    WeixinCurOrderService weixinCurOrderService;
    @Autowired
    CommonDao commonDao;

    @RequestMapping(value = "/tovisit")
    public String getState(HttpServletResponse response, HttpServletRequest request) throws Exception {
        Long comid = RequestUtil.getLong(request,"comid",-1L);
        request.setAttribute("comid",comid);
        return "tovisit";
    }

    @RequestMapping(value = "/sendcode")
    public String sendcode(HttpServletResponse response, HttpServletRequest request) throws Exception {

        String mobile = RequestUtil.getString(request,"mobile");
        int result = weixinCurOrderService.sendCode(null,mobile);
        AjaxUtil.ajaxOutputWithSnakeCase(response,result);
        return null;
    }

    @RequestMapping("validcode")
    public String validCode(HttpServletRequest request, HttpServletResponse response) {

        String code = RequestUtil.getString(request,"code");
        String mobile = RequestUtil.getString(request,"mobile");
        int result = weixinCurOrderService.validCode(code,mobile,null);
        AjaxUtil.ajaxOutputWithSnakeCase(response,result);
        return null;
    }

    @RequestMapping("addvistor")
    public String addvistor(HttpServletRequest request, HttpServletResponse response) {

        Long comid = RequestUtil.getLong(request,"comid",-1L);
        String remark = RequestUtil.getString(request,"remark");
        String mobile = RequestUtil.getString(request,"mobile");
        String carNumber = RequestUtil.getString(request,"car_number");
        Long beginTime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(RequestUtil.getString(request,"begin_time").replace("T"," "));
        Long endTime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(RequestUtil.getString(request,"end_time").replace("T"," "));
        Long createTime = System.currentTimeMillis()/1000;

        logger.info("访客申请时间:"+RequestUtil.getString(request,"begin_time")+"~~~"+RequestUtil.getString(request,"end_time"));
        logger.info("访客申请"+comid+"~~"+remark+"~~"+mobile+"~~"+carNumber+"~~"+beginTime+"~~"+endTime);

        Long id = commonDao.selectSequence(VisitorTb.class);


        VisitorTb visitorTb = new VisitorTb();
        visitorTb.setId(id);
        visitorTb.setRemark(remark);
        visitorTb.setBeginTime(beginTime);
        visitorTb.setEndTime(endTime);
        visitorTb.setCarNumber(carNumber);
        visitorTb.setComid(comid);
        visitorTb.setCreateTime(createTime);
        visitorTb.setMobile(mobile);

        int insert = commonDao.insert(visitorTb);

        if(insert==1){
            SyncInfoPoolTb syncInfoPoolTb = new SyncInfoPoolTb();
            syncInfoPoolTb.setComid(comid);
            syncInfoPoolTb.setTableId(id);
            syncInfoPoolTb.setTableName("visitor_tb");
            syncInfoPoolTb.setCreateTime(System.currentTimeMillis()/1000);
            syncInfoPoolTb.setOperate(0);
            commonDao.insert(syncInfoPoolTb);
        }

        return null;
    }

    @RequestMapping(value = "/success")
    public String success(HttpServletResponse response, HttpServletRequest request) throws Exception {
        Integer type = RequestUtil.getInteger(request,"type",0);
        logger.info("访客申请成功===>>>"+type);
        if(type==1){
            request.setAttribute("errmsg","提交成功，待审核");
            return "success";
        }else{
            request.setAttribute("errmsg","访客申请失败");
            return "success";
        }
    }
}