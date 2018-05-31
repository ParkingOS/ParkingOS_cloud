package parkingos.com.bolink.service;

public interface OpenService {

    boolean checkSign(String data, String sign, Object o) throws Exception;
}
