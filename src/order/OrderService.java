package order;

import member.Member;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import brandSystem.dto.BrandOrderRequestDTO;
import common.Grade;
import exception.BusinessException;
import exception.ErrorCode;
import flight.FlightDAO;
import flight.FlightService;
import member.MemberDAO;
import membership.MembershipService;
import order.dto.OrderDTO;
import order.state.PendingState;
import payment.Payment;
import payment.PaymentDAO;
import payment.PaymentService;
import regulation.RegulationDAO;
import regulation.RegulationDTO;
import shoppingCart.dto.CartItemDTO;
import tax.AlcoholTaxStrategy;
import tax.GeneralTaxStrategy;
import tax.PerfumeTaxStrategy;
import tax.TaxCalculator;
import tax.TaxStrategy;

public class OrderService {

	private final OrderDAO orderDAO = new OrderDAO();
	private final RegulationDAO regulationDAO = new RegulationDAO();
	private final FlightDAO flightDAO = new FlightDAO();
	private final PaymentDAO paymentDAO = new PaymentDAO();
	private final FlightService flightService = new FlightService(flightDAO);
	private final PaymentService paymentService = new PaymentService();
	private final MembershipService membershipService = new MembershipService();

	public List<OrderDTO> getAllOrders() {
		return orderDAO.findAll();
	}

	public List<OrderDTO> getOrdersByMemberId(int memberId) {
		return orderDAO.findByLoginId(memberId);
	}

	public OrderDTO getOrder(int orderId, int productId) {
		OrderDTO order = orderDAO.findByorderIdAndProductId(orderId, productId);
		if (order == null)
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		return order;
	}

	public List<OrderDTO> getOrdersByOrderId(int orderId) {
		List<OrderDTO> orders = orderDAO.findByOrderId(orderId);
		if (orders == null || orders.isEmpty())
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		return orders;
	}

	public List<OrderDTO> getOrdersByBrandName(String brandName) {
		if (brandName == null || brandName.trim().isEmpty())
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		List<OrderDTO> orders = orderDAO.findOrdersByBrandName(brandName);
		if (orders == null || orders.isEmpty())
			throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
		return orders;
	}

	public int createOrder(int memberId, int reservationId, List<CartItemDTO> cartItems) {
		if (cartItems == null || cartItems.isEmpty())
			throw new BusinessException(ErrorCode.INVALID_INPUT);

		BigDecimal totalPrice = cartItems.stream().map(CartItemDTO::getDollarPrice).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		Order order = new Order();
		order.setMemberId(memberId);
		order.setReservationId(reservationId);
		order.setTotalPrice(totalPrice);

		List<OrderDTO> orderItems = cartItems.stream()
				.map(item -> OrderDTO.builder().productId(item.getProductId()).categoryId(item.getCategoryId())
						.capacity(item.getCapacity()).quantity(item.getQuantity())
						.dollarPrice(item.getDollarPrice().divide(BigDecimal.valueOf(item.getQuantity()), 2,
								BigDecimal.ROUND_HALF_UP))
						.discountPrice(BigDecimal.ZERO).build())
				.collect(Collectors.toList());

		return orderDAO.insertOrder(order, orderItems);
	}

	// 면세 한도 체크만 수행 - 호출부로 결과 반환
	public DutyCheckResult checkDuty(List<OrderDTO> orderItems) {
		RegulationDTO alcoholReg = regulationDAO.getByCategoryName("ALCOHOL");
		RegulationDTO perfumeReg = regulationDAO.getByCategoryName("PERFUME");
		return checkDutyFreeLimits(orderItems, alcoholReg, perfumeReg);
	}

	// 면세 초과 확인 후 사용자가 진행 의사 확인한 뒤 호출
	public List<BrandOrderRequestDTO> confirmOrder(int memberId, int reservationId, List<OrderDTO> orderItems,
			String cardNumber) {
		if (orderItems == null || orderItems.isEmpty())
			throw new BusinessException(ErrorCode.DATA_NOT_FOUND);

		RegulationDTO generalReg = regulationDAO.getByCategoryName("GENERAL");
		RegulationDTO alcoholReg = regulationDAO.getByCategoryName("ALCOHOL");
		RegulationDTO perfumeReg = regulationDAO.getByCategoryName("PERFUME");

		int orderId = orderItems.get(0).getOrderId();
		processPayment(orderId, generalReg, alcoholReg, perfumeReg, cardNumber);

		return orderItems.stream()
				.map(item -> BrandOrderRequestDTO.builder().brandName(item.getBrandName())
						.productName(item.getProductName()).orderAmount(item.getQuantity()).build())
				.collect(Collectors.toList());
	}

	// checkDuty + confirmOrder 조합 - 면세 초과 없을 때 바로 진행
	public List<BrandOrderRequestDTO> placeOrder(int memberId, int reservationId, List<OrderDTO> orderItems,
			String cardNumber) {
		if (orderItems == null || orderItems.isEmpty())
			throw new BusinessException(ErrorCode.DATA_NOT_FOUND);

		DutyCheckResult dutyResult = checkDuty(orderItems);
		if (dutyResult.isExceeded()) {
			throw new BusinessException(ErrorCode.DUTY_FREE_TOTAL_LIMIT_EXCEEDED);
		}

		return confirmOrder(memberId, reservationId, orderItems, cardNumber);
	}
//    public List<BrandOrderRequestDTO> placeOrder(int memberId, int reservationId,
//                                                  List<OrderDTO> orderItems, String cardNumber) {
//        if (orderItems == null || orderItems.isEmpty()) throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
//
//        RegulationDTO generalReg = regulationDAO.getByCategoryName("GENERAL");
//        RegulationDTO alcoholReg = regulationDAO.getByCategoryName("ALCOHOL");
//        RegulationDTO cosmeticsReg = regulationDAO.getByCategoryName("COSMETICS");
//
//        DutyCheckResult dutyResult = checkDutyFreeLimits(orderItems, alcoholReg, cosmeticsReg);
//
//        if (dutyResult.isExceeded()) {
//            System.out.println("⚠ 면세 한도 초과");
//            System.out.println(dutyResult.getMessage());
//            System.out.println("예상 세금 = $" + dutyResult.getEstimatedTax());
//            System.out.println("그래도 구매하시겠습니까? (Y/N)");
//
//            Scanner sc = new Scanner(System.in);
//            String answer = sc.nextLine();
//
//            if (!answer.equalsIgnoreCase("Y")) {
//                System.out.println("주문이 취소되었습니다.");
//                return Collections.emptyList();
//            }
//        }
//
//        int orderId = orderItems.get(0).getOrderId();
//        processPayment(orderId, generalReg, alcoholReg, cosmeticsReg, cardNumber);
//
//        return orderItems.stream()
//                .map(item -> BrandOrderRequestDTO.builder()
//                        .brandName(item.getBrandName())
//                        .productName(item.getProductName())
//                        .orderAmount(item.getQuantity())
//                        .build())
//                .collect(Collectors.toList());
//    }

	public void processPayment(int orderId, RegulationDTO generalReg, RegulationDTO alcoholReg,
			RegulationDTO perfumeReg, String cardNumber) {
		System.out.println("in Order");

		List<OrderDTO> orderItems = getOrdersByOrderId(orderId);
		Order order = orderDAO.findOneOrderByOrderId(orderId);

		if (order == null)
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		if (!(order.getState() instanceof PendingState))
			throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);

		String reservationCode = orderDAO.findReservationCodeByOrderId(orderId);
		flightService.validateReservationCode(reservationCode);

		MemberDAO memberDAO = new MemberDAO();
		Member member = memberDAO.findById(order.getMemberId());
		if (member == null)
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);

		Grade grade = member.getGrade();
		BigDecimal membershipDiscount = order.getTotalPrice().multiply(grade.getDiscountRate())
				.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);

		order.setDiscountPrice(membershipDiscount);
		order.setTotalPrice(order.getTotalPrice().subtract(membershipDiscount));
		order.verify();

		BigDecimal totalTax = calculateTax(orderItems, generalReg, alcoholReg, perfumeReg);
		BigDecimal finalAmount = order.getTotalPrice().add(totalTax);

		try {
			boolean paySuccess = paymentService.payment(orderId, finalAmount, cardNumber);
			if (!paySuccess) {
				order.pending();
				throw new BusinessException(ErrorCode.PAYMENT_FAILED);
			}
				

			if (totalTax.compareTo(BigDecimal.ZERO) > 0) {
				order.applyTax(totalTax);
			}

			order.pay();
			orderDAO.update(order);
			membershipService.updateMembershipGrade(order.getMemberId());

			System.out.println("✅ 결제 및 DB 반영 완료");

		} catch (BusinessException e) {
			throw new BusinessException(ErrorCode.PAYMENT_FAILED);
		} catch (Exception e) {
			System.err.println("❌ [시스템 에러 디버그] 결제 처리 중 내부 예외 발생:");
			e.printStackTrace();
		}
	}

	private DutyCheckResult checkDutyFreeLimits(List<OrderDTO> items, RegulationDTO alcoholReg,
			RegulationDTO perfumeReg) {

		int totalAlcohol = alcoholReg != null ? aggregateCapacity(items, alcoholReg.getCategoryId()) : 0;
		int totalPerfume = perfumeReg != null ? aggregateCapacity(items, perfumeReg.getCategoryId()) : 0;

		boolean exceeded = false;
		StringBuilder message = new StringBuilder();

		// 주류 용량 초과 체크
		if (alcoholReg != null && totalAlcohol > alcoholReg.getLimitCapacity()) {
			exceeded = true;
			message.append("- 주류 면세 한도(").append(alcoholReg.getLimitCapacity()).append("ml)를 초과했습니다.\n");
		}

		// 향수 용량 초과 체크
		if (perfumeReg != null && totalPerfume > perfumeReg.getLimitCapacity()) {
			exceeded = true;
			message.append("- 향수 면세 한도(").append(perfumeReg.getLimitCapacity()).append("ml)를 초과했습니다.\n");
		}

		RegulationDTO generalReg = regulationDAO.getByCategoryName("GENERAL");
		BigDecimal estimatedTax = calculateTax(items, generalReg, alcoholReg, perfumeReg);
		return new DutyCheckResult(exceeded, message.toString(), estimatedTax);
	}

	private BigDecimal calculateTax(List<OrderDTO> items, RegulationDTO generalReg, RegulationDTO alcoholReg,
			RegulationDTO perfumeReg) {

		Order order = buildOrderForTax(items, alcoholReg, perfumeReg);

		System.out.println("GENERAL 대상 총 금액 = $" + order.getTotalPrice());
		System.out.println("주류 총 용량 = " + order.getTotalAlcohol() + "ml");
		System.out.println("향수 총 용량 = " + order.getTotalPerfume() + "ml");

		return calculate(order, generalReg, alcoholReg, perfumeReg);
	}

	private BigDecimal calculate(Order order, RegulationDTO generalReg, RegulationDTO alcoholReg,
			RegulationDTO perfumeReg) {

		List<TaxStrategy> strategies = new ArrayList<>();

		// GENERAL
		// 주류/향수 제외 상품 총액이 800달러 초과일 때만 적용
		if (generalReg != null
				&& order.getTotalPrice().compareTo(BigDecimal.valueOf(generalReg.getLimitCapacity())) > 0) {
			strategies.add(new GeneralTaxStrategy(generalReg));
		}

		// ALCOHOL
		if (alcoholReg != null && order.getTotalAlcohol() > alcoholReg.getLimitCapacity()) {
			strategies.add(new AlcoholTaxStrategy(alcoholReg));
		}

		// PERFUME
		if (perfumeReg != null && order.getTotalPerfume() > perfumeReg.getLimitCapacity()) {
			strategies.add(new PerfumeTaxStrategy(perfumeReg));
		}

		if (strategies.isEmpty()) {
			return BigDecimal.ZERO;
		}

		return new TaxCalculator(strategies).calculateTax(order);
	}

	private Order buildOrderForTax(List<OrderDTO> items, RegulationDTO alcoholReg, RegulationDTO perfumeReg) {

		// GENERAL 대상 금액
		// = 주류/향수 제외 상품 금액
		BigDecimal totalGeneralPrice = BigDecimal.ZERO;

		int totalAlcohol = 0;
		int totalPerfume = 0;

		int alcoholCategoryId = alcoholReg != null ? alcoholReg.getCategoryId() : -1;
		int perfumeCategoryId = perfumeReg != null ? perfumeReg.getCategoryId() : -1;

		for (OrderDTO item : items) {

			int categoryId = item.getCategoryId();
			BigDecimal itemPrice = item.getDollarPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
			int totalCapacity = item.getCapacity() * item.getQuantity();

			// ALCOHOL
			if (categoryId == alcoholCategoryId) {
				totalAlcohol += totalCapacity;
			}

			// PERFUME
			else if (categoryId == perfumeCategoryId) {
				totalPerfume += totalCapacity;
			}

			// GENERAL
			// 주류/향수 제외 상품
			else {

				totalGeneralPrice = totalGeneralPrice.add(itemPrice);
			}
		}

		Order order = new Order();

		order.setTotalPrice(totalGeneralPrice);
		order.setTotalAlcohol(totalAlcohol);
		order.setTotalPerfume(totalPerfume);

		return order;
	}

	private int aggregateCapacity(List<OrderDTO> items, int categoryId) {
		int total = 0;
		for (OrderDTO item : items) {
			if (item.getCategoryId() == categoryId) {
				total += item.getCapacity() * item.getQuantity();
			}
		}
		return total;
	}

	public void reservePickup(int orderId) {
		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null)
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		order.reservePickup();
		orderDAO.update(order);
		System.out.println("✅ 픽업 예약 완료 orderId = " + orderId);
	}

	public void completePickup(int orderId) {
		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null)
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		order.pickup();
		orderDAO.update(order);
		System.out.println("✅ 픽업 완료 orderId = " + orderId);
	}

	private void cancelPay(Order order) {
		try {
			Payment payment = paymentDAO.findSuccessByOrderId(order.getOrderId());
			paymentService.cancelPayment(payment.getPaymentId());
			System.out.println("✅ 주문 취소 완료 orderId = " + order.getOrderId());
			membershipService.updateMembershipGrade(order.getMemberId());
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAILED, e);
		}
	}

	public void cancelOrder(int orderId) {
		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null)
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		order.cancel();
		orderDAO.update(order);
		cancelPay(order);
	}

	public void markNoShow(int orderId) {
		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null)
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		order.noShow();
		orderDAO.update(order);
		cancelPay(order);
		System.out.println("✅ 미수령 처리 완료 orderId = " + orderId);
	}
}