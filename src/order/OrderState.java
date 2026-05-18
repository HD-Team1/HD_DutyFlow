package order;

import exception.BusinessException;
import exception.ErrorCode;

public interface OrderState {

    String name();

    default void verify(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    default void pay(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    default void reservePickup(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    default void pickup(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    default void cancel(Order order) {
        throw new BusinessException(ErrorCode.DENIED_CANCEL);
    }
    default void noShow(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    default void pending(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
}