package gui.auth;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import exception.DutyFreeException;
import gui.ScreenManager;

public class MemberLoginPanel extends JPanel {

    private final ScreenManager screenManager;

    private JTextField loginIdField;
    private JPasswordField passwordField;

    private static final Color BG_COLOR = new Color(0xF5F6FA);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(0x2D6CDF);
    private static final Color SECONDARY_COLOR = new Color(0x6B7280);

    public MemberLoginPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridBagLayout());
        setBackground(BG_COLOR);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCDDE1), 1),
                new EmptyBorder(40, 40, 40, 40)
        ));

        JLabel titleLabel = new JLabel("MEMBER LOGIN");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0x2F3640));

        JLabel subTitle = new JLabel("현대면세점 회원 서비스");
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        subTitle.setForeground(SECONDARY_COLOR);
        subTitle.setBorder(new EmptyBorder(5, 0, 30, 0));

        JPanel inputPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        inputPanel.setOpaque(false);
        inputPanel.setMaximumSize(new Dimension(300, 150));

        loginIdField = new JTextField();
        passwordField = new JPasswordField();

        ActionListener loginAction = e -> login();
        loginIdField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        inputPanel.add(new JLabel("아이디"));
        inputPanel.add(loginIdField);
        inputPanel.add(new JLabel("비밀번호"));
        inputPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
        buttonPanel.setMaximumSize(new Dimension(300, 110));

        JButton loginButton = createStyledButton("로그인", PRIMARY_COLOR);
        JButton signupButton = createStyledButton("회원가입", new Color(0x487EB0));

        loginButton.addActionListener(loginAction);
        signupButton.addActionListener(e -> screenManager.show("MEMBER_SIGNUP"));

        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);

        JButton backButton = new JButton("<html><u>이전 화면으로 돌아가기</u></html>");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        backButton.setForeground(SECONDARY_COLOR);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> screenManager.show("LOGIN_SELECT"));

        card.add(titleLabel);
        card.add(subTitle);
        card.add(inputPanel);
        card.add(buttonPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(backButton);

        add(card);
    }

    private void login() {
        String loginId = loginIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (loginId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "아이디와 비밀번호를 모두 입력해주세요.",
                    "알림",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            if (screenManager.getDutyFlowSystem() == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "회원 시스템이 초기화되지 않았습니다.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            int memberId = screenManager.getDutyFlowSystem()
                    .login(loginId, password);

            screenManager.setLoginMemberId(memberId);

            JOptionPane.showMessageDialog(this, "반갑습니다! 로그인되었습니다.");
            clearFields();
            screenManager.show("MEMBER_MAIN");

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getErrorCode().getMessage(),
                    "로그인 실패",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "로그인 처리 중 예기치 못한 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        loginIdField.setText("");
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