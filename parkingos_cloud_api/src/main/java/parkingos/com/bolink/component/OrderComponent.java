package parkingos.com.bolink.component;


import parkingos.com.bolink.dto.CurOrderPrice;

public interface OrderComponent {

    CurOrderPrice getCurOrderPrice(Long unionId, Long comId, String carNumber, String orderId);
}
