package gui.brand;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import gui.ScreenManager;

public class BrandMainPanel extends JPanel {
    private final ScreenManager screenManager;

    public BrandMainPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        
        // 디자인 설정
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250)); // 연한 그레이 배경
        setBorder(new EmptyBorder(60, 150, 60, 150)); // 좌우 여백을 넓게 주어 슬림하게 배치

        // 상단 타이틀
        JLabel titleLabel = new JLabel("브랜드 관리 시스템", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        titleLabel.setForeground(new Color(45, 52, 71));
        titleLabel.setBorder(new EmptyBorder(0, 0, 40, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 중앙 버튼 패널
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 15)); // 세로로 나열, 간격 15px
        buttonPanel.setOpaque(false);

        // 버튼 생성 및 리스너 연결 (로직 유지)
        addStyledButton(buttonPanel, "내 브랜드 재고 조회", "BRAND_STOCK");
        addStyledButton(buttonPanel, "브랜드 상품 목록 조회", "BRAND_PRODUCT_LIST");
        addStyledButton(buttonPanel, "상품 등록 / 삭제", "BRAND_PRODUCT_MANAGE");
        addStyledButton(buttonPanel, "발주 요청 / 취소", "BRAND_PURCHASE");
        addStyledButton(buttonPanel, "발주 이력 조회", "BRAND_PURCHASE_HISTORY");
        addStyledButton(buttonPanel, "브랜드 판매 내역 조회", "BRAND_ORDER_HISTORY");

        add(buttonPanel, BorderLayout.CENTER);

        // 하단 로그아웃 버튼
        JButton logoutButton = new JButton("로그아웃");
        logoutButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        logoutButton.setBackground(new Color(231, 76, 60)); // 빨간색 계열
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setPreferredSize(new Dimension(0, 45));
        logoutButton.addActionListener(e -> {
            screenManager.clearBrandSystem();
            screenManager.show("LOGIN_SELECT");
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        bottomPanel.add(logoutButton, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addStyledButton(JPanel container, String text, String screenName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(52, 73, 94));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 마우스 호버 효과 (선택사항)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(236, 240, 241)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(Color.WHITE); }
        });

        btn.addActionListener(e -> screenManager.show(screenName));
        container.add(btn);
    }
}