package parkingos.com.bolink.service.impl;

import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.beans.QrCodeTb;
import parkingos.com.bolink.service.QrFilterService;

import java.util.Random;

@Service
public class QrFilterServiceImpl implements QrFilterService {
    Logger logger = Logger.getLogger(QrFilterServiceImpl.class);
    @Autowired
    CommonDao<QrCodeTb> qrCodeCommonDao;


    public static void main(String[] args) {
        System.out.println(new Random().nextDouble());
    }


    @Override
    public QrCodeTb getQrCode(String code) {
        QrCodeTb qrCodeConditions = new QrCodeTb();
        qrCodeConditions.setCode(code);
        QrCodeTb qrCodeTb = qrCodeCommonDao.selectObjectByConditions(qrCodeConditions);
        return qrCodeTb;
    }
}
