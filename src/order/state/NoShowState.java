package order.state;

import order.Order;
import order.OrderState;

public class NoShowState implements OrderState {

    @Override
    public String name() {
        return "NO_SHOW";
    }
}