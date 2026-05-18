package order.state;

import order.Order;
import order.OrderState;

public class VerifiedState implements OrderState {

    @Override
    public String name() {
        return "VERIFIED";
    }

    // VERIFIED → PAID
    @Override
    public void pay(Order order) {
        order.setState(new PaidState());
    }

    // VERIFIED → CANCELED (결제 전이므로 취소 가능)
    @Override
    public void cancel(Order order) {
        order.setState(new CanceledState());
    }
}