//package main;
//
//import regulation.RegulationDTO;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
//import brandSystem.BrandSystem;
//import category.Category;
//import common.Currency;
//import dutyFlowSystem.DutyFlowSystem;
//import exception.BusinessException;
//import exception.DataNotFoundException;
//import exception.ErrorCode;
//import exception.SystemException;
//import member.Member;
//import member.MemberService;
//import member.MemberSignupDTO;
//import order.Order;
//import order.OrderService;
//import order.dto.OrderDTO;
//import product.ProductService;
//import product.dto.ProductDTO;
//
//public class ChaeyeonMain {
//
//	public static void main(String[] args) {
//
//		ProductService service = new ProductService();
//		OrderService orderService = new OrderService();
//		List<BrandSystem> brandList;
////		DutyFlowSystem dutyFlowSystem = new DutyFlowSystem(brandList);
//		/*
//		 * // product
//		 * -----------------------------------------------------------------------------
//		 * ----------------------------
//		 * 
//		 * 
//		 * // ========================= // 1. 전체 조회 테스트 // =========================
//		 * System.out.println("===== 전체 상품 조회 =====");
//		 * 
//		 * List<ProductDTO> all = service.printAllProducts();
//		 * 
//		 * for (ProductDTO p : all) { System.out.println(p); }
//		 * 
//		 * // ========================= // 2. 카테고리 조회 테스트 // =========================
//		 * System.out.println("===== 카테고리별 상품 조회 =====");
//		 * 
//		 * Category category = Category.builder() .categoryName("전자제품") // DB에 있는 값으로
//		 * 맞춰야 함 .build();
//		 * 
//		 * List<ProductDTO> byCategory = service.printAllProducts(category);
//		 * 
//		 * for (ProductDTO p : byCategory) { System.out.println(p); }
//		 * 
//		 * // ========================= // 3. 상품명으로 단건 조회 테스트 //
//		 * ========================= System.out.println("===== 상품명으로 상품 1개 조회 =====");
//		 * ProductDTO product = service.printProduct("조니워커 블루라벨");
//		 * System.out.println(product);
//		 * 
//		 * // ========================= //특정 금액 범위 테스트 // =========================
//		 * 
//		 * BigDecimal min = new BigDecimal("100"); BigDecimal max = new
//		 * BigDecimal("50000");
//		 * 
//		 * try { List<ProductDTO> list = service.printProduct(min, max, Currency.KRW);
//		 * 
//		 * System.out.println("===== 결과 ====="); for (ProductDTO p : list) {
//		 * System.out.println(p); }
//		 * 
//		 * } catch (Exception e) { e.printStackTrace(); }
//		 * 
//		 * 
//		 */
//
//		// order
//		// ---------------------------------------------------------------------------------------------------------
//
//		/*
//		 * System.out.println("\n\n===== [Order Service Test] =====");
//		 * 
//		 * 
//		 * //1. 전체 주문 내역 확인 System.out.println("===== [ADMIN] 전체 주문 내역 조회 ====="); try {
//		 * List<OrderDTO> allOrders = orderService.getAllOrders(); // DTO가 아닌 Order 도메인
//		 * 사용 권장 if (allOrders.isEmpty()) { System.out.println("등록된 주문 내역이 없습니다."); }
//		 * else { for (OrderDTO order : allOrders) { System.out.print("예약번호: " +
//		 * order.getReservationId()); System.out.print(" | 고객ID: " +
//		 * order.getMemberId()); System.out.print(" | 총액: " + order.getDollarPrice());
//		 * System.out.print(" | 상품명: " + order.getProductName());
//		 * System.out.print(" | 수량: " + order.getQuantity()); // 상태 패턴의 현재 클래스명을 출력하여 상태
//		 * 확인 System.out.print(" | 현재상태: " +
//		 * order.getOrderState().getClass().getSimpleName());
//		 * System.out.println(" | 주문일: " + order.getOrderedAt()); System.out.println(
//		 * "---------------------------------------------------------"); } } } catch
//		 * (Exception e) { throw new SystemException(ErrorCode.DATA_NOT_FOUND, e); }
//		 * 
//		 * // 2. 특정 사용자 주문 내역 확인 int memberId = 1;
//		 * System.out.println("\n===== [USER] 회원 번호(" + memberId + ") 주문 내역 조회 =====");
//		 * try { List<OrderDTO> myOrders = orderService.getOrdersByMemberId(memberId);
//		 * 
//		 * if (myOrders.isEmpty()) { System.out.println("해당 회원의 주문 내역이 없습니다."); } else {
//		 * for (OrderDTO order : myOrders) {
//		 * System.out.println(String.format("[주문번호: %d] 상태: %s | 결제금액(USD): %s",
//		 * order.getOrderId(), order.getOrderState().getClass().getSimpleName(),
//		 * order.getDollarPrice())); } } } catch (Exception e) { throw new
//		 * SystemException(ErrorCode.DATA_NOT_FOUND, e); }
//		 * 
//		 * // 3. 특정 주문 상품의 상세 내역 확인 int orderId = 1; // 예시용 예약번호 int productId = 1;
//		 * 
//		 * System.out.println("\n===== [USER] 내 주문 내역 조회 (예약번호: " + orderId + " 상품 번호" +
//		 * productId + ") ====="); try { OrderDTO myOrder =
//		 * orderService.getOrder(orderId, productId); System.out.println("주문 상태: " +
//		 * myOrder.getOrderState().getClass().getSimpleName());
//		 * System.out.println("주문 일자: " + myOrder.getOrderedAt());
//		 * 
//		 * } catch (Exception e) { System.out.println("해당 주문을 찾을 수 없습니다: " +
//		 * e.getMessage()); }
//		 * 
//		 * // 4. 한 번의 주문의 상품 리스트 보기 System.out.println("\n===== [USER] 주문 번호 " + orderId
//		 * + ") ====="); try { List<OrderDTO> myOrders =
//		 * orderService.getOrdersByOrderId(orderId);
//		 * 
//		 * for (OrderDTO order : myOrders) {
//		 * System.out.println(String.format("[주문번호: %d] 상태: %s | 결제금액(USD): %s",
//		 * order.getOrderId(), order.getOrderState().getClass().getSimpleName(),
//		 * order.getDollarPrice())); }
//		 * 
//		 * } catch (Exception e) { System.out.println("해당 주문을 찾을 수 없습니다: " +
//		 * e.getMessage()); }
//		 * 
//		 */
//
////    	OrderService orderService = new OrderService();
////        int testOrderId = 1; // DB에 존재하는 주문 번호
////
////        try {
////            System.out.println("=== 결제 및 검증 테스트 시작 ===");
////            orderService.order(testOrderId);
////            System.out.println("=== 테스트 종료: 성공 ===");
////        } catch (BusinessException e) {
////            System.err.println("❌ 검증 실패: " + e.getErrorCode().getMessage());
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//
////    	//==================================================
////    	// 5. 주문 생성 + 결제 테스트
////    	System.out.println("\n===== [USER] 주문 생성 및 결제 테스트 =====");
////    	OrderService orderService = new OrderService();
////    	
////    	try {
////
////    	    // 주문할 상품 리스트 생성
////    	    OrderDTO item1 = new OrderDTO();
////    	    item1.setProductId(1);
////    	    item1.setProductName("조니워커 블루라벨");
////    	    item1.setCategoryId(1);
////    	    item1.setCategoryName("위스키");
////    	    item1.setCapacity(750);
////    	    item1.setQuantity(1);
////    	    item1.setDollarPrice(new BigDecimal("220"));
////
////    	    OrderDTO item2 = new OrderDTO();
////    	    item2.setProductId(2);
////    	    item2.setProductName("샤넬 향수");
////    	    item2.setCategoryId(2);
////    	    item2.setCategoryName("향수");
////    	    item2.setCapacity(50);
////    	    item2.setQuantity(1);
////    	    item2.setDollarPrice(new BigDecimal("120"));
////
////    	    List<OrderDTO> cartItems = List.of(item1, item2);
////
////    	    int memberId = 1;
////    	    int reservationId = 1;
////    	    
////			// 주문 요청
////    	    orderService.placeOrder(memberId, reservationId, cartItems);
////
////    	    System.out.println("✅ 주문 및 결제 성공");
////
////    	} catch (BusinessException e) {
////
////    	    System.out.println("❌ 비즈니스 예외 발생");
////    	    System.out.println("에러 코드: " + e.getErrorCode());
////    	    System.out.println("메시지: " + e.getMessage());
////
////    	} catch (Exception e) {
////
////    	    System.out.println("❌ 시스템 오류 발생");
////    	    e.printStackTrace();
////    	}
//		// =====================================================
//
////    	 OrderService orderService = new OrderService();
////    	 
////         // ================================================================
////         // 케이스 1. 한도 이하 — 세금 없음
////         // 위스키 1000ml (한도 2000ml 이하), 화장품 50ml (한도 100ml 이하)
////         // 기대: 세금 $0, totalAmount = $220+$120 = $340
////         // ================================================================
////         System.out.println("===== 케이스 1: 한도 이하 (세금 없음) =====");
////         runOrder(orderService,
////                 whisky(750, 1, "220"),   // 750ml * 1 = 750ml  → 한도 이하 (2000)
////                 perfume(50, 1, "120")    // 50ml  * 1 = 50ml   → 한도 이하 (100)
////         );
////  
////         // ================================================================
////         // 케이스 2. 위스키만 한도 초과
////         // 위스키 3000ml (한도 2000ml 초과), 화장품 없음
////         // 기대: 위스키 세금 부과
////         // ================================================================
////         System.out.println("\n===== 케이스 2: 위스키만 한도 초과 =====");
////         runOrder(orderService,
////                 whisky(750, 4, "220")    // 750ml * 4 = 3000ml → 한도 초과 (2000)
////         );
////  
////         // ================================================================
////         // 케이스 3. 화장품만 한도 초과
////         // 화장품 200ml (한도 100ml 초과), 위스키 없음
////         // 기대: 화장품 세금 부과
////         // ================================================================
////         System.out.println("\n===== 케이스 3: 화장품만 한도 초과 =====");
////         runOrder(orderService,
////                 perfume(100, 2, "120")   // 100ml * 2 = 200ml  → 한도 초과 (100)
////         );
////  
////         // ================================================================
////         // 케이스 4. 둘 다 한도 초과 (복합)
////         // 위스키 3000ml + 화장품 200ml
////         // 기대: 위스키 + 화장품 세금 모두 부과
////         // ================================================================
////         System.out.println("\n===== 케이스 4: 위스키 + 화장품 모두 한도 초과 =====");
////         runOrder(orderService,
////                 whisky(750, 4, "220"),   // 3000ml → 초과
////                 perfume(100, 2, "120")   // 200ml  → 초과
////         );
////  
////         // ================================================================
////         // 케이스 5. 경계값 — 한도와 정확히 같음 (초과 아님)
////         // 위스키 2000ml, 화장품 100ml
////         // 기대: 세금 없음
////         // ================================================================
////	      // 케이스 5 — DB 실제 capacity=750 기준으로 경계값 맞추기
////	      // 750 * 2 = 1500ml (한도 2000 이하) → 세금 없음 ✅
////	      runOrder(orderService,
////	              whisky(750, 2, "220"),   // 1500ml → 한도 이하
////	              perfume(100, 1, "120")   // 100ml  → 경계
////	      );
////
////  
////         // ================================================================
////		   // 케이스 6 — 한도 초과용
////		   // 750 * 3 = 2250ml (한도 2000 초과) → 세금 있음 ✅
////		   runOrder(orderService,
////		           whisky(750, 3, "220"),   // 2250ml → 초과
////		           perfume(100, 2, "120")   // 200ml  → 초과
////		   );
////  
////         // ================================================================
////         // 케이스 7. 장바구니 비어있음 → 예외
////         // ================================================================
////         System.out.println("\n===== 케이스 7: 빈 장바구니 (예외) =====");
////         try {
////             orderService.placeOrder(1, 1, new ArrayList<>());
////         } catch (BusinessException e) {
////             System.out.println("✅ 예외 발생: " + e.getErrorCode() + " / " + e.getMessage());
////         }
////  
////         // ================================================================
////         // 케이스 8. 장바구니 null → 예외
////         // ================================================================
////         System.out.println("\n===== 케이스 8: null 장바구니 (예외) =====");
////         try {
////             orderService.placeOrder(1, 1, null);
////         } catch (BusinessException e) {
////             System.out.println("✅ 예외 발생: " + e.getErrorCode() + " / " + e.getMessage());
////         }
////  
////         // ================================================================
////         // 조회 테스트
////         // ================================================================
////  
////         System.out.println("\n===== 전체 주문 조회 =====");
////         orderService.getAllOrders().forEach(System.out::println);
////  
////         System.out.println("\n===== 회원(memberId=1) 주문 조회 =====");
////         orderService.getOrdersByMemberId(1).forEach(System.out::println);
////  
////         System.out.println("\n===== 주문 상세 조회 (orderId=1, productId=1) =====");
////         System.out.println(orderService.getOrder(1, 1));
////  
////         System.out.println("\n===== 주문번호 기준 조회 (orderId=1) =====");
////         orderService.getOrdersByOrderId(1).forEach(System.out::println);
////  
////         // ================================================================
////         // 케이스 9. 존재하지 않는 주문 조회 → 예외
////         // ================================================================
////         System.out.println("\n===== 케이스 9: 존재하지 않는 주문 조회 (예외) =====");
////         try {
////             orderService.getOrder(9999, 9999);
////         } catch (BusinessException e) {
////             System.out.println("✅ 예외 발생: " + e.getErrorCode() + " / " + e.getMessage());
////         }
////     }
////  
////     // ----------------------------------------------------------------
////     // 헬퍼 메서드
////     // ----------------------------------------------------------------
////  
////     /** 주문 실행 공통 처리 */
////     private static void runOrder(OrderService orderService, OrderDTO... items) {
////         try {
////             List<OrderDTO> cartItems = new ArrayList<>();
////             for (OrderDTO item : items) cartItems.add(item);
////  
////             orderService.placeOrder(1, 1, cartItems);
////             System.out.println("✅ 주문 완료");
////         } catch (BusinessException e) {
////             System.out.println("❌ 비즈니스 예외: " + e.getErrorCode() + " / " + e.getMessage());
////         } catch (Exception e) {
////             System.out.println("❌ 시스템 오류: " + e.getMessage());
////             e.printStackTrace();
////         }
////     }
////  
////     /** 위스키 아이템 생성 (categoryId=2) */
////     private static OrderDTO whisky(int capacity, int quantity, String price) {
////         return OrderDTO.builder()
////                 .productId(1)
////                 .productName("조니워커 블루라벨")
////                 .categoryId(2)               // DB 기준 위스키 = 2
////                 .categoryName("위스키")
////                 .capacity(capacity)
////                 .quantity(quantity)
////                 .dollarPrice(new BigDecimal(price))
////                 .discountPrice(BigDecimal.ZERO)
////                 .build();
////     }
////  
////     /** 화장품/향수 아이템 생성 (categoryId=4) */
////     private static OrderDTO perfume(int capacity, int quantity, String price) {
////         return OrderDTO.builder()
////                 .productId(3)
////                 .productName("샤넬 넘버5")
////                 .categoryId(4)               // DB 기준 화장품 = 4
////                 .categoryName("화장품")
////                 .capacity(capacity)
////                 .quantity(quantity)
////                 .dollarPrice(new BigDecimal(price))
////                 .discountPrice(BigDecimal.ZERO)
////                 .build();
////     }
//// }
//
////    	// ==============================
////    	// 상태 패턴 테스트
////    	// ==============================
////
////    	System.out.println("\n===== 상태 패턴 테스트 =====");
////    	
////    	
////
////    	// 케이스 1. 정상 흐름 — ORDERED → PAID → PICKUP_RESERVED → PICKED_UP
////    	System.out.println("\n--- 케이스 1: 정상 흐름 ---");
////    	try {
////    	    // 새 주문 생성
////    	    List<OrderDTO> cartItems = new ArrayList<>();
////    	    cartItems.add(whisky(750, 1, "220"));
////    	    int orderId = orderService.placeOrder(1, 1, cartItems);
////    	    
////    	    // 방금 생성된 주문 번호 확인 후 아래 orderId에 입력
////
////    	    orderService.reservePickup(orderId);  // PAID → PICKUP_RESERVED
////    	    orderService.completePickup(orderId); // PICKUP_RESERVED → PICKED_UP
////    	    System.out.println("✅ 정상 흐름 완료");
////
////    	} catch (BusinessException e) {
////    	    System.out.println("❌ " + e.getErrorCode() + " / " + e.getMessage());
////    	}
////
////    	// 케이스 2. ORDERED → 취소
////    	System.out.println("\n--- 케이스 2: 주문 취소 ---");
////    	try {
////    	    List<OrderDTO> cartItems = new ArrayList<>();
////    	    cartItems.add(whisky(750, 1, "220"));
////    	    orderService.placeOrder(1, 1, cartItems);
////
////    	    int orderId = orderService.placeOrder(1, 1, cartItems);
////    	    
////    	    orderService.cancelOrder(orderId); // PAID → CANCELED
////    	    System.out.println("✅ 취소 완료");
////
////    	} catch (BusinessException e) {
////    	    System.out.println("❌ " + e.getErrorCode() + " / " + e.getMessage());
////    	}
////
////    	// 케이스 3. PICKUP_RESERVED → 미수령
////    	System.out.println("\n--- 케이스 3: 미수령 처리 ---");
////    	try {
////    	    List<OrderDTO> cartItems = new ArrayList<>();
////    	    cartItems.add(whisky(750, 1, "220"));
////    	    int orderId = orderService.placeOrder(1, 1, cartItems);
////    	    orderService.reservePickup(orderId);
////    	    orderService.markNoShow(orderId); // PICKUP_RESERVED → NO_SHOW
////    	    System.out.println("✅ 미수령 처리 완료");
////
////    	} catch (BusinessException e) {
////    	    System.out.println("❌ " + e.getErrorCode() + " / " + e.getMessage());
////    	}
////
////    	// 케이스 1 바깥에 선언
////    	int case1OrderId = -1;
////
////    	System.out.println("\n--- 케이스 1: 정상 흐름 ---");
////    	try {
////    	    List<OrderDTO> cartItems = new ArrayList<>();
////    	    cartItems.add(whisky(750, 1, "220"));
////    	    int generatedId = orderService.placeOrder(1, 1, cartItems);
////    	    orderService.reservePickup(generatedId); // 방금 만든 ID로 진행
////    	    
////    	    orderService.reservePickup(case1OrderId);
////    	    orderService.completePickup(case1OrderId);
////    	    System.out.println("✅ 정상 흐름 완료");
////    	} catch (BusinessException e) {
////    	    System.out.println("❌ " + e.getErrorCode() + " / " + e.getMessage());
////    	}
////
////    	// 케이스 4
////    	System.out.println("\n--- 케이스 4: 불가능한 전이 (예외 기대) ---");
////    	try {
////    	    orderService.cancelOrder(case1OrderId); // PICKED_UP → CANCELED 불가
////    	} catch (BusinessException e) {
////    	    System.out.println("✅ 예외 발생: " + e.getErrorCode() + " / " + e.getMessage());
////    	}
////    	
////    	
////    }
////
////	private static OrderDTO whisky(int capacity, int quantity, String price) {
////		return OrderDTO.builder().productId(1).productName("조니워커 블루라벨").categoryId(2).categoryName("위스키")
////				.capacity(capacity).quantity(quantity).dollarPrice(new BigDecimal(price)).discountPrice(BigDecimal.ZERO)
////				.build();
////	}
//
//		/*
//		 * // ============================= // 비행기 예약 코드 검증 //
//		 * ============================= // 3. 테스트용 규정 데이터 생성 (컴파일 에러 방지) //
//		 * ChaeyeonMain.java의 테스트 데이터 생성 부분 수정 RegulationDTO generalReg =
//		 * RegulationDTO.builder() .regulationId(1) .categoryId(1) // "일반" 대신 ID 값 입력
//		 * .limitCapacity(0) .overageRate(10) .establishedDate(LocalDate.now())
//		 * .build();
//		 * 
//		 * RegulationDTO alcoholReg = RegulationDTO.builder() .regulationId(2)
//		 * .categoryId(2) .limitCapacity(2000) .overageRate(20)
//		 * .establishedDate(LocalDate.now()) .build(); RegulationDTO perfumeReg =
//		 * RegulationDTO.builder() .regulationId(3) .categoryId(4) .limitCapacity(60)
//		 * .overageRate(20) .establishedDate(LocalDate.now()) .build();
//		 * 
//		 * 
//		 * System.out.println("=== 항공편 예약 코드 검증 및 결제 테스트 ===\n");
//		 * 
//		 * try { // 시나리오: DB에 있는 orderId 1번을 대상으로 결제(order) 시도 // 이 과정에서 내부적으로
//		 * flightService.validateReservationCode()가 실행됨
//		 * System.out.println("[테스트 시작] 주문번호 1번에 대한 결제 승인 시도...");
//		 * 
//		 * orderService.order(1, generalReg, alcoholReg, perfumeReg);
//		 * 
//		 * System.out.println("\n✅ 테스트 결과: 성공 (결제 및 항공권 검증 완료)");
//		 * 
//		 * } catch (BusinessException e) {
//		 * System.err.println("\n❌ 테스트 결과: 실패 (비즈니스 로직 오류)");
//		 * System.err.println("에러 코드: " + e.getErrorCode()); System.err.println("메시지: "
//		 * + e.getMessage()); } catch (Exception e) {
//		 * System.err.println("\n❌ 테스트 결과: 시스템 오류 발생"); e.printStackTrace(); }
//		 * 
//		 */
//
////		// 1. 테스트용 면세 규정 설정 (DB 조회 대신 직접 생성)
////		RegulationDTO generalReg = new RegulationDTO(1, 1, 0, LocalDate.now(), 10); // 일반 10%
////		RegulationDTO alcoholReg = new RegulationDTO(2, 2, 2000, LocalDate.now(), 20); // 주류 2000ml 한도
////		RegulationDTO perfumeReg = new RegulationDTO(3, 4, 100, LocalDate.now(), 15); // 향수 100ml 한도
////
////		System.out.println("========= [면세점 주문 시스템 종합 테스트] =========\n");
////
////		// [시나리오 1] 정상 결제 (이미 DB에 있는 OrderId 1번 사용)
////		// 전제조건: DB orders 테이블 1번의 reservationId가 유효한 코드를 가져와야 함
////		try {
////			System.out.println("--- [CASE 1] 정상 항공권 & 한도 내 구매 ---");
////			orderService.order(1, generalReg, alcoholReg, perfumeReg, "4111-1111-1111-1111");
////			System.out.println("=> 결과: 성공 (PAID 업데이트 완료)");
////		} catch (BusinessException e) {
////			System.out.println("=> 결과: 실패 (" + e.getMessage() + ")");
////		}
////
////		System.out.println("\n-------------------------------------------");
////
////		// [시나리오 2] 항공권 예약 번호가 잘못된 경우 (정규식 위반 등)
////		// 테스트 방법: DB에서 특정 주문의 reservationId를 정규식에 안 맞는 값으로 바꾸고 실행
////		try {
////			System.out.println("--- [CASE 2] 유효하지 않은 예약 코드 (형식 오류) ---");
////			// 만약 999번 주문이 있고 코드가 'INVALID-123' 이라면
////			orderService.order(999, generalReg, alcoholReg, perfumeReg, "4111-1111-1111-1111");
////		} catch (BusinessException e) {
////			System.out.println("=> 결과: 예상된 실패 (" + e.getErrorCode().getMessage() + ")");
////		}
////
////		System.out.println("\n-------------------------------------------");
////
////		// [시나리오 3] 새 주문 생성부터 결제까지 전체 프로세스 (placeOrder 테스트)
////		try {
////			System.out.println("--- [CASE 3] 장바구니 생성 -> 주문 -> 결제 통합 ---");
////
////			List<OrderDTO> cart = new ArrayList<>();
////			// 주류 한도 초과 시뮬레이션 (750ml * 3병 = 2250ml > 2000ml)
////			cart.add(OrderDTO.builder().productId(1).productName("조니워커 블루").categoryId(2).capacity(750).quantity(3)
////					.dollarPrice(new BigDecimal("200")).discountPrice(new BigDecimal("0")) // 기본 상품 할인율 0%
////					.build());
////
////			// memberId: 1, reservationId: 1로 새 주문 생성
////			int newOrderId = orderService.placeOrder(1, 1, cart, "4111-1111-1111-1111");
////			System.out.println("=> 결과: 새 주문 생성 및 결제 완료 (ID: " + newOrderId + ")");
////
////		} catch (BusinessException e) {
////			System.out.println("=> 결과: 중단 (" + e.getMessage() + ")");
////		}
//
//
////        MemberService memberService = new MemberService();
////
////        System.out.println("===== 회원가입 테스트 시작 =====");
////        System.out.println();
////
////        // =========================
////        // 1. 정상 회원가입
////        // =========================
////        System.out.println("===== 정상 회원가입 =====");
////
////        try {
////
////            MemberSignupDTO dto = new MemberSignupDTO(
////                    "testuser01",
////                    "1234",
////                    "김민준",
////                    LocalDate.of(1998, 5, 10),
////                    "01012345678"
////            );
////
////            memberService.signup(dto);
////
////            System.out.println("회원가입 성공");
////
////        } catch (Exception e) {
////            System.out.println("회원가입 실패 | reason = " + e.getMessage());
////        }
////
////        System.out.println();
////        
////
////        System.out.println();
////
////        // =========================
////        // 6. 정상 여권 등록
////        // =========================
////        System.out.println("===== 정상 여권 등록 =====");
////
////        try {
////
////            memberService.registerPassport(
////                    1,
////                    "M123A4567",
////                    LocalDate.of(2030, 12, 31)
////            );
////
////            System.out.println("여권 등록 성공");
////
////        } catch (Exception e) {
////            System.out.println("여권 등록 실패 | reason = " + e.getMessage());
////        }
////
////        System.out.println();
////
////        // =========================
////        // 10. 정상 로그인
////        // =========================
////        System.out.println("===== 정상 로그인 =====");
////
////        try {
////
////            int memberId = memberService.login(
////                    "testuser01",
////                    "1234"
////            );
////
////            System.out.println("로그인 성공 | memberId = " + memberId);
////
////        } catch (Exception e) {
////            System.out.println("로그인 실패 | reason = " + e.getMessage());
////        }
////
////        System.out.println();
////
////
////		// 2. 브랜드 시스템 리스트 생성 및 구현체 추가
////
////		// 중요: DB의 brandName과 일치하도록 이름을 지정하여 객체 생성 및 추가
////		// 만약 BrandSystem이 인터페이스이고 개별 브랜드 클래스가 있다면 아래처럼 생성
////		brandList.add(new BrandSystem("Johnnie Walker")); 
////		brandList.add(new BrandSystem("Ballantines"));
////
////		// 3. DutyFlowSystem에 브랜드 리스트 주입
////		dutyFlowSystem.setBrandList(brandList);
////
////		// ------------------------------------------------
////		// 주문 상품 생성
////		// ------------------------------------------------
////		List<OrderDTO> cartItems = new ArrayList<>();
////
////		cartItems.add(OrderDTO.builder().productId(1).productName("조니워커 블루라벨").brandName("Johnnie Walker").categoryId(2)
////				.capacity(750).quantity(2).dollarPrice(new BigDecimal("220")).discountPrice(BigDecimal.ZERO).build());
////
////		cartItems.add(OrderDTO.builder().productId(2).productName("샤넬 넘버5").brandName("Chanel").categoryId(4)
////				.capacity(100).quantity(1).dollarPrice(new BigDecimal("150")).discountPrice(BigDecimal.ZERO).build());
////
////		// ------------------------------------------------
////		// Queue에 들어갈 Order 생성
////		// ------------------------------------------------
////		Order order = new Order();
////
////		order.setOrderId(1);
////		order.setMemberId(1);
////		order.setReservationId(1);
////
////		// ------------------------------------------------
////		// Queue 등록
////		// ------------------------------------------------
////		dutyFlowSystem.addOrderQueue(order);
////
////		// ------------------------------------------------
////		// Queue 처리
////		// ------------------------------------------------
////		try {
////
////			dutyFlowSystem.processOrderQueue();
////
////			System.out.println();
////			System.out.println("✅ 주문 Queue 처리 완료");
////
////		} catch (BusinessException e) {
////			throw new BusinessException(ErrorCode.DATA_NOT_FOUND,e);
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
//
//
//
//        // =====================================================
//        // 1. 테스트용 장바구니 생성
//        // =====================================================
//        List<OrderDTO> cartItems = new ArrayList<>();
//
//        // 주류 (ALCOHOL)
//        cartItems.add(createItem(
//                1, // productId
//                2, // categoryId (ALCOHOL)
//                "조니워커 블루라벨",
//                "Johnnie Walker",
//                750,
//                220,
//                10,
//                1
//        ));
//
//        // 향수 (PERFUME)
//        cartItems.add(createItem(
//                3,
//                4, // PERFUME
//                "샤넬 넘버5",
//                "Chanel",
//                100,
//                120,
//                5,
//                2
//        ));
//
//        // 일반상품 (GENERAL or ELECTRONICS 등)
//        cartItems.add(createItem(
//                5,
//                5,
//                "아이코스 일루마",
//                "IQOS",
//                1,
//                95,
//                0,
//                1
//        ));
//
//        // =====================================================
//        // 2. 주문 실행
//        // =====================================================
//        int memberId = 1;
//        int reservationId = 1;
//        String cardNumber = "4111-1111-1111-1111";
//
//        System.out.println("========== ORDER START ==========");
//
//        List<?> result = orderService.placeOrder(
//                memberId,
//                reservationId,
//                cartItems,
//                cardNumber
//        );
//
//        System.out.println("========== ORDER RESULT ==========");
//        System.out.println(result);
//
//        System.out.println("========== ORDER END ==========");
//    }
//
//    // =====================================================
//    // OrderDTO 생성 헬퍼
//    // =====================================================
//    private static OrderDTO createItem(
//            int productId,
//            int categoryId,
//            String productName,
//            String brandName,
//            int capacity,
//            double dollarPrice,
//            double discountRate,
//            int quantity
//    ) {
//        OrderDTO dto = new OrderDTO();
//
//        dto.setProductId(productId);
//        dto.setCategoryId(categoryId);
//        dto.setProductName(productName);
//        dto.setBrandName(brandName);
//        dto.setCapacity(capacity);
//        dto.setDollarPrice(java.math.BigDecimal.valueOf(dollarPrice));
//        dto.setDiscountPrice(java.math.BigDecimal.valueOf(discountRate));
//        dto.setQuantity(quantity);
//
//        return dto;
//    }
//		
//	
//
//}
