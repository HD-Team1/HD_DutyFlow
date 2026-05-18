package order.state;

import order.Order;
import order.OrderState;

public class PaidState implements OrderState {

    @Override
    public String name() {
        return "PAID";
    }

    // PAID → PICKUP_RESERVED
    @Override
    public void reservePickup(Order order) {
        order.setState(new PickupReservedState());
    }

    // PAID → CANCELED (환불 처리 필요 시 비즈니스 레이어에서 처리)
    @Override
    public void cancel(Order order) {
        order.setState(new CanceledState());
    }
}