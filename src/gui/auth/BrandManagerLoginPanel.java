package gui.auth;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import admin.brandmanager.dto.BrandManager;
import admin.brandmanager.service.BrandManagerService;
import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;

public class BrandManagerLoginPanel extends JPanel {

    private final ScreenManager screenManager;
    private final BrandManagerService brandManagerService = new BrandManagerService();

    private JTextField managerIdField;
    private JPasswordField passwordField;

    // UI 컬러 테마 (MemberLoginPanel과 통일)
    private static final Color BG_COLOR = new Color(0xF5F6FA);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(0x2D6CDF); 
    private static final Color SECONDARY_COLOR = new Color(0x6B7280);

    public BrandManagerLoginPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridBagLayout()); // 중앙 배치를 위해 GridBagLayout 사용
        setBackground(BG_COLOR);

        /* ── 브랜드 관리자 로그인 카드 ── */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCDDE1), 1),
                new EmptyBorder(40, 40, 40, 40)
        ));

        // 타이틀 섹션
        JLabel titleLabel = new JLabel("BRAND ADMIN LOGIN");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0x2F3640));

        JLabel subTitle = new JLabel("브랜드 관리 시스템 접속");
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        subTitle.setForeground(SECONDARY_COLOR);
        subTitle.setBorder(new EmptyBorder(5, 0, 30, 0));

        // 입력 필드 섹션 (GridLayout 사용)
        JPanel inputPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        inputPanel.setOpaque(false);
        inputPanel.setMaximumSize(new Dimension(300, 150));

        managerIdField = new JTextField();
        passwordField = new JPasswordField();
        
        // 엔터 키 입력 시 로그인 시도
        ActionListener loginAction = e -> handleLogin();
        managerIdField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        inputPanel.add(new JLabel("관리자 ID"));
        inputPanel.add(managerIdField);
        inputPanel.add(new JLabel("비밀번호"));
        inputPanel.add(passwordField);

        // 버튼 섹션
        JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 0, 10)); // 브랜드 로그인은 가입 버튼이 없으므로 1열
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
        buttonPanel.setMaximumSize(new Dimension(300, 75));

        JButton loginButton = createStyledButton("로그인", PRIMARY_COLOR);
        loginButton.addActionListener(loginAction);
        buttonPanel.add(loginButton);

        // 뒤로가기 링크 (텍스트 버튼 형태)
        JButton backButton = new JButton("<html><u>이전 화면으로 돌아가기</u></html>");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        backButton.setForeground(SECONDARY_COLOR);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            clearFields();
            screenManager.show("LOGIN_SELECT");
        });

        // 카드에 컴포넌트 추가
        card.add(titleLabel);
        card.add(subTitle);
        card.add(inputPanel);
        card.add(buttonPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(backButton);

        add(card);
    }

    private void handleLogin() {
        String managerIdText = managerIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (managerIdText.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID와 비밀번호를 입력하세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int managerId = Integer.parseInt(managerIdText);
            BrandManager brandManager = brandManagerService.login(managerId, password);
            
            BrandSystem brandSystem = new BrandSystem(brandManager.getBrandName());
            screenManager.setBrandSystem(brandSystem);
            
            JOptionPane.showMessageDialog(this, brandManager.getManagerName() + "님 환영합니다.");
            clearFields();
            screenManager.show("BRAND_MAIN");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "관리자 ID는 숫자로 입력해야 합니다.", "형식 오류", JOptionPane.ERROR_MESSAGE);
        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "로그인 실패", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "로그인 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        managerIdField.setText("");
        passwordField.setText("");
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });

        return btn;
    }
}