package order.state;

import order.Order;
import order.OrderState;

public class CanceledState implements OrderState {

    @Override
    public String name() {
        return "CANCELED";
    }
    // 최종 상태
}