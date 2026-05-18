package gui.auth;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import gui.ScreenManager;

public class LoginSelectPanel extends JPanel {

    private final ScreenManager screenManager;

    // UI 컬러 테마
    private static final Color BG_COLOR = new Color(0xF5F6FA);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color MEMBER_COLOR = new Color(0x2D6CDF); // 회원용 블루
    private static final Color ADMIN_COLOR = new Color(0x2F3640);  // 관리자용 다크그레이
    private static final Color BACK_COLOR = new Color(0x6B7280);   // 그레이

    public LoginSelectPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridBagLayout()); // 화면 정중앙 배치
        setBackground(BG_COLOR);

        /* ── 유형 선택 카드 ── */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCDDE1), 1),
                new EmptyBorder(50, 60, 50, 60)
        ));

        // 타이틀 섹션
        JLabel titleLabel = new JLabel("HYUNDAI DUTY FREE");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(new Color(0x2F3640));

        JLabel subTitle = new JLabel("원하시는 서비스를 선택해주세요");
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        subTitle.setForeground(BACK_COLOR);
        subTitle.setBorder(new EmptyBorder(5, 0, 40, 0));

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 0, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(320, 260));

        JButton memberLoginButton = createStyledButton("회원 로그인", MEMBER_COLOR);
        JButton memberSignupButton = createStyledButton("신규 회원가입", new Color(0x487EB0));
        JButton brandLoginButton = createStyledButton("브랜드 관리자 로그인", ADMIN_COLOR);
        JButton airportLoginButton = createStyledButton("면세 시스템 관리자 로그인", ADMIN_COLOR);

        // 이벤트 연결
        memberLoginButton.addActionListener(e -> screenManager.show("MEMBER_LOGIN"));
        memberSignupButton.addActionListener(e -> screenManager.show("MEMBER_SIGNUP"));
        brandLoginButton.addActionListener(e -> screenManager.show("BRAND_MANAGER_LOGIN"));
        airportLoginButton.addActionListener(e -> screenManager.show("AIRPORT_MANAGER_LOGIN"));

        buttonPanel.add(memberLoginButton);
        buttonPanel.add(memberSignupButton);
        buttonPanel.add(brandLoginButton);
        buttonPanel.add(airportLoginButton);

        // 하단 이전 화면 링크
        JButton backButton = new JButton("<html><u>메인 화면으로 돌아가기</u></html>");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        backButton.setForeground(BACK_COLOR);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setBorder(new EmptyBorder(30, 0, 0, 0));
        backButton.addActionListener(e -> screenManager.show("HOME"));

        // 카드 컴포넌트 구성
        card.add(titleLabel);
        card.add(subTitle);
        card.add(buttonPanel);
        card.add(backButton);

        add(card);
    }

    /**
     * 스타일이 적용된 버튼 생성 헬퍼 메서드
     */
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 마우스 호버 효과
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }
}