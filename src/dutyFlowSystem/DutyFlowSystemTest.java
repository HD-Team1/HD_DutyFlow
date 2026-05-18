//package dutyFlowSystem;
//
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.List;
//
//import brandSystem.BrandSystem;
//import member.MemberSignupDTO;
//import order.dto.OrderDTO;
//import product.Product;
//import product.ProductService;
//
//public class DutyFlowSystemTest {
//
//	public static void main(String[] args) throws InterruptedException {
//
//		System.out.println("========== TEST START ==========");
//
//		// =====================================================
//		// 1. 브랜드 시스템 Mock
//		// =====================================================
//		System.out.println("[1] 브랜드 시스템 생성");
//
//		BrandSystem johnnieWalker = new BrandSystem("Johnnie Walker");
//		BrandSystem ballantines = new BrandSystem("Ballantines");
//		BrandSystem chanel = new BrandSystem("Chanel");
//		BrandSystem iqosBrand = new BrandSystem("IQOS");
//
//		List<BrandSystem> brandList =
//		        Arrays.asList(
//		                johnnieWalker,
//		                ballantines,
//		                chanel,
//		                iqosBrand
//		        );
//
//		System.out.println("브랜드 개수 = " + brandList.size());
//
//		// =====================================================
//		// 2. 시스템 생성
//		// =====================================================
//		System.out.println("[2] DutyFlowSystem 생성");
//
//		DutyFlowSystem system = new DutyFlowSystem(brandList);
//		ProductService productService = new ProductService();
//
//		// =====================================================
//		// 3. Worker 시작
//		// =====================================================
//		System.out.println("[3] Worker 시작");
//
//		system.startPaymentWorker();
//		system.startExchangeRateScheduler();
//
//		// =====================================================
//		// 4. 회원가입
//		// =====================================================
//		System.out.println("[4] 회원가입 시작");
//
////		try {
////
////			MemberSignupDTO signupDTO = new MemberSignupDTO(
////					"testuser01",
////					"1234",
////					"김민준",
////					LocalDate.of(1998, 5, 10),
////					"01012345678"
////			);
////
////			System.out.println("signupDTO 생성 완료");
////
////			system.signup(signupDTO);
////
////			System.out.println("회원가입 성공");
////
////		} catch (Exception e) {
////
////			System.out.println("회원가입 실패");
////			System.out.println("exception class = " + e.getClass().getName());
////			System.out.println("message = " + e.getMessage());
////
////			e.printStackTrace();
////		}
//
//		// =====================================================
//		// 5. 로그인
//		// =====================================================
//		System.out.println("[5] 로그인 시작");
//
//		try {
//
//			system.login("user01", "pass01");
//
//			System.out.println("로그인 성공");
//
//		} catch (Exception e) {
//
//			System.out.println("로그인 실패");
//			System.out.println("message = " + e.getMessage());
//
//			e.printStackTrace();
//		}
//
//		// =====================================================
//		// 6. 상품 조회
//		// =====================================================
//		System.out.println("[6] 상품 조회");
//
//		List<Product> products = productService.getAllProducts();
//
//		System.out.println("조회된 상품 개수 = " + products.size());
//
//		for (Product p : products) {
//			System.out.println(
//					"상품 = " +
//					p.getProductName()
//			);
//		}
//
//		Product p1 = products.get(0);
//		Product p2 = products.get(1);
//		Product p3 = products.get(2);
//
//		// =====================================================
//		// 7. 장바구니 테스트
//		// =====================================================
//		System.out.println("[7] 장바구니 테스트");
//
//		system.addToCart(p1, 1);
//		System.out.println("p1 추가 완료");
//
//		system.addToCart(p2, 2);
//		System.out.println("p2 추가 완료");
//
//		system.addToCart(p3, 1);
//		System.out.println("p3 추가 완료");
//
//		system.updateQuantity(p2, 1);
//
//		System.out.println("수량 수정 완료");
//
//		System.out.println("========== CART ==========");
//		System.out.println(system.printCart());
//
//		// =====================================================
//		// 8. 주문 생성
//		// =====================================================
//		System.out.println("[8] 주문 생성");
//
//		int orderId = system.makeOrder();
//
//		System.out.println("생성된 orderId = " + orderId);
//
//		// =====================================================
//		// 9. 주문 처리
//		// =====================================================
//		System.out.println("[9] 주문 큐 처리");
//
//		system.processOrderQueue();
//
//		System.out.println("processOrderQueue 호출 완료");
//
//		Thread.sleep(2000);
//
//		// =====================================================
//		// 10. 주문 조회
//		// =====================================================
//		System.out.println("[10] 주문 조회");
//
//		List<OrderDTO> myOrders = system.getMyOrders();
//
//		System.out.println("주문 개수 = " + myOrders.size());
//
//		System.out.println(myOrders);
//
//		// =====================================================
//		// 11. 픽업 예약
//		// =====================================================
//		System.out.println("[11] 픽업 예약");
//
//		system.reservePickup(orderId);
//
//		System.out.println("픽업 예약 완료");
//
//		// =====================================================
//		// 12. 픽업 완료
//		// =====================================================
//		System.out.println("[12] 픽업 완료");
//
//		system.completePickup(orderId);
//
//		System.out.println("픽업 완료 처리 성공");
//
//		// =====================================================
//		// 13. 환율 테스트
//		// =====================================================
//		System.out.println("[13] 환율 조회");
//
//		System.out.println("오늘 환율 = " + system.getTodayExchangeRate());
//
//		System.out.println("주간 환율 = ");
//		System.out.println(system.getWeeklyExchangeRates());
//
//		System.out.println("월간 환율 = ");
//		System.out.println(system.getMonthlyExchangeRates());
//
//		// =====================================================
//		// 14. 시스템 종료
//		// =====================================================
//		System.out.println("[14] 시스템 종료");
//
//		system.stopPaymentWorker();
//		system.stopExchangeRateScheduler();
//
//		System.out.println("========== SYSTEM END ==========");
//	}
//}
