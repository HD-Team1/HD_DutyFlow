package gui;

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Color;
import javax.swing.*;

import com.formdev.flatlaf.FlatLightLaf;

import admin.airportmanager.AirportManagerDao;
import admin.airportmanager.AirportManagerService;
import dutyFlowSystem.DutyFlowSystem;
import flight.FlightDAO;
import flight.FlightService;
import gui.auth.*;
import gui.brand.*;
import gui.exchangerate.ExchangeRatePanel;
import gui.home.HomePanel;
import gui.member.*;
import gui.pickup.*;
import pickup.PickUpSystem;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ScreenManager screenManager;
    private PickUpSystem pickUpSystem;

    public MainFrame(DutyFlowSystem dutyFlowSystem) {
        setTitle("현대면세점 공항 인도장 픽업 예약 관리 시스템");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        screenManager = new ScreenManager(cardLayout, mainPanel);
        screenManager.setDutyFlowSystem(dutyFlowSystem);

        initScreens();

        add(mainPanel);

        screenManager.show("HOME");

        // 화면 초기 렌더링 강제
        mainPanel.revalidate();
        mainPanel.repaint();

        setVisible(true);
    }

    private void applyGlobalStyle() {
        try {
            // 모던한 Light 테마 적용
            FlatLightLaf.setup();
            
            // 전역 폰트 설정 (이 설정으로 모든 컴포넌트의 폰트가 통일됩니다)
            Font globalFont = new Font("맑은 고딕", Font.PLAIN, 14);
            UIManager.put("defaultFont", globalFont);
            
            // 버튼, 라벨 등 특정 컴포넌트 여백 및 디자인 디테일 조정
            UIManager.put("Button.arc", 8); // 버튼 모서리 둥글게
            UIManager.put("Component.arc", 8); // 입력창 모서리 둥글게
            UIManager.put("Table.rowHeight", 30); // 테이블 행 높이 조절
            UIManager.put("TableHeader.font", new Font("맑은 고딕", Font.BOLD, 14));
            
        } catch (Exception ex) {
            System.err.println("테마를 적용할 수 없습니다. 기본 UI로 실행합니다.");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initScreens() {
        // 기존 패널 등록 로직 (동일)
        screenManager.addScreen("HOME", new HomePanel(screenManager));
        screenManager.addScreen("LOGIN_SELECT", new LoginSelectPanel(screenManager));
        screenManager.addScreen("BRAND_MANAGER_LOGIN", new BrandManagerLoginPanel(screenManager));

        screenManager.addScreen("BRAND_MAIN", new BrandMainPanel(screenManager));
        screenManager.addScreen("BRAND_STOCK", new BrandStockPanel(screenManager));
        screenManager.addScreen("BRAND_PRODUCT_LIST", new BrandProductListPanel(screenManager));
        screenManager.addScreen("BRAND_ORDER_HISTORY", new BrandOrderHistoryPanel(screenManager));
        screenManager.addScreen("BRAND_PRODUCT_MANAGE", new BrandProductManagePanel(screenManager));
        screenManager.addScreen("BRAND_PURCHASE", new BrandPurchasePanel(screenManager));
        screenManager.addScreen("BRAND_PURCHASE_HISTORY", new BrandPurchaseHistoryPanel(screenManager));
        
        screenManager.addScreen("MEMBER_LOGIN", new MemberLoginPanel(screenManager));
        screenManager.addScreen("MEMBER_SIGNUP", new MemberSignupPanel(screenManager));
        screenManager.addScreen("MEMBER_MAIN", new MemberMainPanel(screenManager));
        screenManager.addScreen("MEMBER_PASSPORT", new MemberPassportPanel(screenManager));
        screenManager.addScreen("MEMBER_PRODUCT_LIST", new MemberProductListPanel(screenManager));
        screenManager.addScreen("MEMBER_PRODUCT_DETAIL", new MemberProductDetailPanel(screenManager));
        screenManager.addScreen("MEMBER_CART", new MemberCartPanel(screenManager));
        screenManager.addScreen("MEMBER_ORDER_HISTORY", new MemberOrderHistoryPanel(screenManager));
        screenManager.addScreen("MEMBER_ORDER_DETAIL", new MemberOrderDetailPanel(screenManager));
        screenManager.addScreen(
                "MEMBER_EXCHANGE_RATE",
                new ExchangeRatePanel(screenManager)
        );        
        screenManager.addScreen("MEMBER_PAYMENT_QUEUE", new MemberPaymentQueuePanel(screenManager));
        screenManager.addScreen("MEMBER_PICKUP_RESERVATION", new MemberPickupReservationPanel(screenManager));
        
        
        // ── 인도장 시스템 의존성 설정 ──
        FlightDAO flightDAO = new FlightDAO();
        AirportManagerDao airportManagerDao = new AirportManagerDao();
        FlightService flightService = new FlightService(flightDAO);
        AirportManagerService airportManagerService = new AirportManagerService(airportManagerDao);
        pickUpSystem = new PickUpSystem(airportManagerService, flightService);
      
        // -- 인도장 화면 등록 --
        AirportManagerLoginPanel loginPanel = new AirportManagerLoginPanel(screenManager);
        loginPanel.setPickUpSystem(pickUpSystem);
        screenManager.addScreen("AIRPORT_MANAGER_LOGIN", loginPanel);

        PickupMainPanel pMainPanel = new PickupMainPanel(screenManager);
        pMainPanel.setPickUpSystem(pickUpSystem);
        screenManager.addScreen("PICKUP_MAIN", pMainPanel);
        
        screenManager.addScreen("PICKUP_LIST", new PickupListPanel(screenManager));
        
        PickupVerificationPanel verifyPanel = new PickupVerificationPanel(screenManager);
        verifyPanel.setPickUpSystem(pickUpSystem);

        screenManager.addScreen("PICKUP_VERIFY", verifyPanel);
        
        MLPQSimulationPanel simPanel = new MLPQSimulationPanel(screenManager);
        screenManager.addScreen("PICKUP_SIMULATION", simPanel);
    }
}