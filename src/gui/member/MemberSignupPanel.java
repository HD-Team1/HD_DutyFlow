package gui.member;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import exception.DutyFreeException;
import gui.ScreenManager;
import member.MemberService;
import member.MemberSignupDTO;

public class MemberSignupPanel extends JPanel {

    private final ScreenManager screenManager;
    private final MemberService memberService = new MemberService();

    private JTextField loginIdField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JTextField birthDateField;
    private JTextField phoneNumberField;

    // UI 컬러 테마 (기존 테마와 유지)
    private static final Color BG_COLOR = new Color(0xF5F6FA);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(0x2D6CDF);
    private static final Color SECONDARY_COLOR = new Color(0x6B7280);

    public MemberSignupPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridBagLayout());
        setBackground(BG_COLOR);

        /* ── 회원가입 카드 컨테이너 ── */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCDDE1), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        // 타이틀 섹션
        JLabel titleLabel = new JLabel("CREATE ACCOUNT");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0x2F3640));

        JLabel subTitle = new JLabel("현대면세점의 새로운 회원이 되어보세요");
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        subTitle.setForeground(SECONDARY_COLOR);
        subTitle.setBorder(new EmptyBorder(5, 0, 25, 0));

        // 입력 폼 섹션
        JPanel formPanel = new JPanel(new GridLayout(10, 1, 0, 5));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(320, 400));

        loginIdField = new JTextField();
        passwordField = new JPasswordField();
        nameField = new JTextField();
        birthDateField = new JTextField();
        phoneNumberField = new JTextField();

        formPanel.add(createFieldLabel("아이디"));
        formPanel.add(loginIdField);
        formPanel.add(createFieldLabel("비밀번호"));
        formPanel.add(passwordField);
        formPanel.add(createFieldLabel("이름"));
        formPanel.add(nameField);
        formPanel.add(createFieldLabel("생년월일 (yyyy-MM-dd)"));
        formPanel.add(birthDateField);
        formPanel.add(createFieldLabel("전화번호 (010-xxxx-xxxx)"));
        formPanel.add(phoneNumberField);

        // 버튼 섹션
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(25, 0, 10, 0));
        buttonPanel.setMaximumSize(new Dimension(320, 50));

        JButton signupButton = createStyledButton("가입하기", PRIMARY_COLOR);
        JButton backButton = createStyledButton("취소", SECONDARY_COLOR);

        signupButton.addActionListener(e -> signup());
        backButton.addActionListener(e -> screenManager.show("LOGIN_SELECT"));

        buttonPanel.add(backButton);
        buttonPanel.add(signupButton);

        // 카드에 컴포넌트 추가
        card.add(titleLabel);
        card.add(subTitle);
        card.add(formPanel);
        card.add(buttonPanel);

        add(card);
    }

    private void signup() {
        try {
            String loginId = loginIdField.getText().trim();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText().trim();
            String birthStr = birthDateField.getText().trim();
            String phoneNumber = phoneNumberField.getText().trim();

            if (loginId.isEmpty() || password.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "필수 정보를 모두 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate birthDate;
            try {
                birthDate = LocalDate.parse(birthStr);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "생년월일 형식이 올바르지 않습니다. (예: 1999-10-22)", "형식 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MemberSignupDTO dto = new MemberSignupDTO(
                    loginId,
                    password,
                    name,
                    birthDate,
                    phoneNumber
            );

            memberService.signup(dto);

            int memberId = memberService.login(loginId, password);

            screenManager.setLoginMemberId(memberId);
            screenManager.setPassportAfterSignup(true);

            JOptionPane.showMessageDialog(
                    this,
                    "회원가입이 완료되었습니다!\n이어서 여권 정보를 등록해주세요."
            );

            clearFields();
            screenManager.show("MEMBER_PASSPORT");

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "가입 실패", JOptionPane.ERROR_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "회원가입 중 예상치 못한 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        loginIdField.setText("");
        passwordField.setText("");
        nameField.setText("");
        birthDateField.setText("");
        phoneNumberField.setText("");
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        label.setForeground(new Color(0x4B5563));
        return label;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
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