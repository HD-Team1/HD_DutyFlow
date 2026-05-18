package main;

import java.time.LocalDateTime;
import java.util.Scanner;

import admin.airportmanager.AirportManagerDao;
import admin.airportmanager.AirportManagerService;
import common.CurrentTime;

import common.Grade;
import flight.FlightDAO;
import flight.FlightService;
import member.Member;
import pickup.PickUpSystem;
import admin.airportmanager.AirportManagerDao;
import admin.airportmanager.AirportManagerService;

public class CheolyongMain {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("==========================================");
		System.out.print("📦 물품 인도에 걸리는 시간을 설정해주세요 (분 단위, 예: 3) : ");
		int procTime = sc.nextInt();
		System.out.println("==========================================");

		try {
			LocalDateTime baseTime = LocalDateTime.of(2026, 5, 1, 9, 30, 0, 0);
			CurrentTime.curTime = baseTime;

			
			FlightDAO flightDAO = new FlightDAO();
	        AirportManagerDao airportManagerDao = new AirportManagerDao();
	        
	        FlightService flightService = new FlightService(flightDAO);
	        AirportManagerService airportManagerService = new AirportManagerService(airportManagerDao);
	        
	        PickUpSystem ps = new PickUpSystem(airportManagerService,flightService);

			
			ps.loadOrders();

			// ==========================================================
			// 번호표 발급 (타임워프)
			// ==========================================================
			System.out.println("\nSYSTEM: (창구 오픈 전) 타임워프를 통해 고객들이 순차적으로 번호표를 뽑습니다...");
			CurrentTime.curTime = baseTime.minusMinutes(39); ps.appendQueue("M33333333", 3); // 김철용
			CurrentTime.curTime = baseTime.minusMinutes(25); ps.appendQueue("M44444444", 4); // 오블랙
			CurrentTime.curTime = baseTime.minusMinutes(10); ps.appendQueue("M77777777", 7); // 약블랙
			CurrentTime.curTime = baseTime.minusMinutes(5);  ps.appendQueue("M88888888", 8); // 중블랙
			CurrentTime.curTime = baseTime.minusMinutes(2);  ps.appendQueue("M99999999", 9); // 강부자
			CurrentTime.curTime = baseTime.minusMinutes(1);  ps.appendQueue("M55555555", 5); // 최골드
			CurrentTime.curTime = baseTime;                  ps.appendQueue("M66666666", 6); // 유실버
			ps.appendQueue("M11111111", 1); // 이급박 (AQ, 25분 남음)
			ps.appendQueue("M22222222", 2); // 박지각 (AQ, 15분 남음, AQ 1순위)

			// ==========================================================
			// 🎬 [시나리오 1] 항공편 지연 — AQ에 있는 박지각이 BQ로 강등
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 1] AQ 사람의 항공편 지연 ---");
			System.out.println("[ PickUpSystem ] 지연 전 대기열 상태 (박지각이 AQ 1순위):");
			ps.printCurrentQueue();

			System.out.println("\n[ PickUpSystem ] 알림: 박지각의 OZ1015 항공편이 14:00으로 지연되었습니다.");
			ps.delayFlight("OZ1015", LocalDateTime.of(2026, 5, 1, 14, 0, 0));

			System.out.println("\n[ PickUpSystem ] 지연 후 대기열 상태 (박지각이 AQ → BQ 맨 뒤로 강등!):");
			ps.printCurrentQueue();
			System.out.println("💡 분석: 출국 임박이라 AQ에 있던 박지각이 14:00으로 밀리면서 더 이상 긴급하지 않게 됨.");
			System.out.println("   AQ에서 탈출해 BQ의 일반 룰(등급>번호표)을 따라 SILVER 맨 뒤로 재배치.");

			ps.openCounter(); // 박지각 강등됐으니 AQ 1순위 = 이급박 자동호출

			// ==========================================================
			// 🎬 [시나리오 2] 이급박 10분 노쇼 타임아웃
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 2] 이급박 10분 노쇼 대기 ---");
			for (int i = 0; i < 10; i++) {
				ps.passTime();
			}
			// 09:40에 이급박 호출 취소 → pop → handleStarvation에서 김철용(49분) Starvation 가로채기

			// ==========================================================
			// 🎬 [시나리오 3] 김철용 에이징(Starvation) 자동 호출
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 3] 노쇼 후 김철용 에이징 자동 호출 ---");
			ps.processPickUp("M33333333", 3, procTime); // 김철용

			// 강부자(PRESTIGE) 자동 호출 → 처리
			ps.processPickUp("M99999999", 9, procTime); // 강부자

			// ==========================================================
			// 🎬 [시나리오 4] 오블랙 에이징(Starvation) 가로채기
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 4] 오블랙 에이징 가로채기 ---");
			ps.processPickUp("M44444444", 4, procTime); // 오블랙

			// ==========================================================
			// 🎬 [시나리오 5] BQ 평시 룰 순서대로 처리
			// BLACK(약블랙→중블랙) → GOLD(최골드) → SILVER(유실버)
			// ⚠️ 박지각은 NO_SHOW 검증을 위해 처리하지 않음!
			// ==========================================================
			ps.processPickUp("M77777777", 7, procTime); // 약블랙
			ps.processPickUp("M88888888", 8, procTime); // 중블랙
			ps.processPickUp("M55555555", 5, procTime); // 최골드
			ps.processPickUp("M66666666", 6, procTime); // 유실버
			// → 유실버 처리 완료 후 tryCallNextCustomer() → 박지각이 currentTicket으로 자동 호출됨

			// ==========================================================
			// 🎬 [시나리오 6] NO_SHOW 자동 처리 검증
			// ==========================================================
			System.out.println("\n==========================================================");
			System.out.println("🎬 [시나리오 6] NO_SHOW 자동 처리 검증");
			System.out.println("==========================================================");
			System.out.println("💡 박지각(OZ1015, 지연 후 14:00 출국)이 호출되었지만 창구에 오지 않은 상태입니다.");
			System.out.println("   시간을 14:00으로 점프한 뒤 passTime() 1회를 호출합니다.");
			System.out.println("   → updateNoShowState()가 currentTicket의 출국 시간 경과를 감지하여");
			System.out.println("     자동으로 NO_SHOW 처리해야 합니다.");
			System.out.println();

			System.out.println("📋 [ 점프 전 상태 ]");
			System.out.println("   현재 시각: " + CurrentTime.curTime.toLocalTime());
			System.out.println("   currentTicket: 박지각 (OZ1015, 출국 14:00)");
			System.out.println("   대기열 잔여:");
			ps.printCurrentQueue(); // 비어있어야 정상 (박지각은 currentTicket이므로 큐에 없음)

			// 시간을 14:00으로 직접 세팅 → passTime() 호출 시 14:01이 되면서 NO_SHOW 감지
			CurrentTime.curTime = LocalDateTime.of(2026, 5, 1, 14, 0, 0);
			System.out.println("\n⏩ 시간 점프 완료: " + CurrentTime.curTime.toLocalTime());
			System.out.println("   passTime() 호출 → 14:01에 updateNoShowState() 발동 예정...\n");

			ps.passTime(); // 14:01 → currentTicket(박지각) 출국 14:00 경과 → NO_SHOW 자동 처리

			System.out.println("\n📋 [ NO_SHOW 처리 후 상태 ]");
			System.out.println("   현재 시각: " + CurrentTime.curTime.toLocalTime());
			System.out.println("   대기열 잔여:");
			ps.printCurrentQueue(); // 완전히 비어있어야 함

			System.out.println("\n==========================================================");
			System.out.println("🎉 [테스트 완료] 모든 시나리오가 예외 없이 종료되었습니다.");
			System.out.println("==========================================================");
			System.out.println("\n📊 검증된 시나리오 요약:");
			System.out.println("  1️⃣  항공편 지연 → AQ→BQ 강등 + DB Flight 테이블 동기화");
			System.out.println("  2️⃣  호출 타임아웃 (이급박 10분 미방문)");
			System.out.println("  3️⃣  에이징/Starvation 가로채기 (김철용 49분 대기)");
			System.out.println("  4️⃣  에이징/Starvation 가로채기 (오블랙 41분+ 대기)");
			System.out.println("  5️⃣  BQ 평시 룰 (등급 > 번호표 순)");
			System.out.println("  6️⃣  NO_SHOW 자동 처리 (출국 시간 경과 시 currentTicket 자동 전이)");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}
}
