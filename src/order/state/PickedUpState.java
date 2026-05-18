package order.state;

import order.Order;
import order.OrderState;

public class PickedUpState implements OrderState {

    @Override
    public String name() {
        return "PICKED_UP";
    }
    // 최종 상태 — 모든 전이 불가 (인터페이스 default가 예외 던짐)
}