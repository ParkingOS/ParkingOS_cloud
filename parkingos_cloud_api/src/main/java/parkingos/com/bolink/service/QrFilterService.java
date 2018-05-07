package parkingos.com.bolink.service;

import parkingos.com.bolink.beans.FixCodeTb;
import parkingos.com.bolink.beans.QrCodeTb;

public interface QrFilterService {
    /**
     * 获取二维码信息
     * @param code
     * @return
     */
    QrCodeTb getQrCode(String code);

    FixCodeTb getFixCode(String code);
}
