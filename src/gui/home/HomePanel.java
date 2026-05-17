package gui.home;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import gui.ScreenManager;

public class HomePanel extends JPanel {

    private final ScreenManager screenManager;

    public HomePanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        // 전체 레이아웃 및 여백 설정
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(50, 50, 50, 50)); 
        setBackground(UIManager.getColor("Panel.background")); // 테마 배경색 유지

        // 중앙 컨텐츠 패널 (타이틀 + 버튼을 수직으로 배치)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 메인 타이틀
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>"
                + "현대면세점 공항 인도장<br>픽업 예약 관리 시스템"
                + "</div></html>", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 36)); // 시원하게 키운 제목
        titleLabel.setForeground(UIManager.getColor("Component.accentColor")); // 테마 포인트 컬러 사용
        
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 40, 0); // 제목 아래 큰 여백
        contentPanel.add(titleLabel, gbc);

        // 2. 서브 설명 (옵션)
        JLabel subLabel = new JLabel("관리자 및 회원 로그인을 통해 시스템을 이용해 주세요.", SwingConstants.CENTER);
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        subLabel.setForeground(Color.GRAY);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 50, 0);
        contentPanel.add(subLabel, gbc);

        // 3. 시작하기(로그인) 버튼
        JButton loginButton = new JButton("시스템 시작하기");
        loginButton.setPreferredSize(new Dimension(250, 60)); // 버튼을 크고 신뢰감 있게
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        
        // FlatLaf 전용 스타일: 버튼을 강조색으로 칠하고 둥글게 만듭니다.
        loginButton.putClientProperty("JButton.buttonType", "roundRect");
        loginButton.putClientProperty("JButton.filled", true);
        
        loginButton.addActionListener(e -> screenManager.show("LOGIN_SELECT"));

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE; // 버튼은 가로로 꽉 차지 않게
        contentPanel.add(loginButton, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // 하단 카피라이트 (장식용)
        JLabel footerLabel = new JLabel("© 2024 HYUNDAI DUTY FREE. All Rights Reserved.", SwingConstants.CENTER);
        footerLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        footerLabel.setForeground(Color.LIGHT_GRAY);
        add(footerLabel, BorderLayout.SOUTH);
    }
}