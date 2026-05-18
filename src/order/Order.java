package order;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import order.state.PendingState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import product.Product;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Order {
	
    private int orderId;
    private int memberId;
    private int reservationId;
    private String cardNumber;
    


    private BigDecimal totalPrice;

    private OrderState state = new PendingState();
    
	private String loginId;
	private List<Map<Product, Integer>> products;
	private int totalPerfume;
	private int totalAlcohol;
	private LocalDateTime orderedAt;
	private BigDecimal discountPrice; // 등급 할인 금액
	
	public Order() {
		this.state = new PendingState(); //초기 객체는 검증 대기 객체
	}
	
    public Order(String cardNumber){
    	this.cardNumber= cardNumber;
    }
    
    public void pay() {
        state.pay(this);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void pickup() {
        state.pickup(this);
    }

	public Order(int orderId, OrderState state) {
        this.orderId = orderId;
        this.state = state;
    }

    // 결제 실패 시 호출할 직렬화 메서드 
    public void saveFailedOrder() {
        System.out.println("결제 실패: 현재 주문 상태를 직렬화하여 저장합니다...");
    }

    public void applyTax(BigDecimal taxAmount) {
        if (taxAmount != null && taxAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 기존 총액에 세금을 더함
            this.totalPrice = this.totalPrice.add(taxAmount);
        }
    }

    public String getStateName() {
        return state.name();
    }
    
    public void verify() {
        state.verify(this);
    }


    // 아래 3개 추가
    public void reservePickup() {
        state.reservePickup(this);
    }


    public void noShow() {
        state.noShow(this);
    }

	public void pending() {
		state.pending(this);
	}
    
}
