package order.state;

import order.Order;
import order.OrderState;

public class PickupReservedState implements OrderState {

    @Override
    public String name() {
        return "PICKUP_RESERVED";
    }

    // PICKUP_RESERVED → PICKED_UP
    @Override
    public void pickup(Order order) {
        order.setState(new PickedUpState());
    }

    // PICKUP_RESERVED → NO_SHOW
    @Override
    public void noShow(Order order) {
        order.setState(new NoShowState());
    }
}