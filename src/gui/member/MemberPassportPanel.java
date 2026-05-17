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

public class MemberPassportPanel extends JPanel {

    private final ScreenManager screenManager;
    private final MemberService memberService = new MemberService();

    private JTextField passportNumberField;
    private JTextField passportExpiryDateField;

    // UI 컬러 테마 (기존과 통일)
    private static final Color BG_COLOR = new Color(0xF5F6FA);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(0x2D6CDF);
    private static final Color SECONDARY_COLOR = new Color(0x6B7280);

    public MemberPassportPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridBagLayout());
        setBackground(BG_COLOR);

        /* ── 여권 정보 등록 카드 ── */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCDDE1), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        // 타이틀 섹션
        JLabel titleLabel = new JLabel("PASSPORT INFO");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0x2F3640));

        JLabel subTitle = new JLabel("면세품 수령을 위해 여권 정보를 등록해주세요");
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        subTitle.setForeground(SECONDARY_COLOR);
        subTitle.setBorder(new EmptyBorder(5, 0, 25, 0));

        // 입력 폼 섹션
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(320, 160));

        passportNumberField = new JTextField();
        passportExpiryDateField = new JTextField();

        formPanel.add(createFieldLabel("여권번호"));
        formPanel.add(passportNumberField);
        formPanel.add(createFieldLabel("여권 만료일 (yyyy-MM-dd)"));
        formPanel.add(passportExpiryDateField);

        // 버튼 섹션
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
        buttonPanel.setMaximumSize(new Dimension(320, 50));

        JButton registerButton = createStyledButton("등록하기", PRIMARY_COLOR);
        JButton backButton = createStyledButton("취소", SECONDARY_COLOR);

        registerButton.addActionListener(e -> registerPassport());
        backButton.addActionListener(e -> screenManager.show("MEMBER_MAIN"));

        buttonPanel.add(backButton);
        buttonPanel.add(registerButton);

        // 카드에 컴포넌트 조립
        card.add(titleLabel);
        card.add(subTitle);
        card.add(formPanel);
        card.add(buttonPanel);

        add(card);
    }

    private void registerPassport() {
        try {
            Integer memberId = screenManager.getLoginMemberId();

            if (memberId == null) {
                JOptionPane.showMessageDialog(this, "세션이 만료되었습니다. 다시 로그인해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                screenManager.show("MEMBER_LOGIN");
                return;
            }

            String passportNum = passportNumberField.getText().trim();
            String expiryDateStr = passportExpiryDateField.getText().trim();

            // 유효성 검증
            if (passportNum.isEmpty()) {
                JOptionPane.showMessageDialog(this, "여권번호를 입력해주세요.");
                return;
            }

            LocalDate passportExpiredDate;
            try {
                passportExpiredDate = LocalDate.parse(expiryDateStr);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "날짜 형식이 올바르지 않습니다.\n예: 2030-12-31", "형식 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            memberService.registerPassport(memberId, passportNum, passportExpiredDate);

            JOptionPane.showMessageDialog(this, "여권 정보가 성공적으로 등록되었습니다.");

            if (screenManager.isPassportAfterSignup()) {
                screenManager.setPassportAfterSignup(false);
                screenManager.clearLoginMemberId();

                JOptionPane.showMessageDialog(this, "이제 로그인 후 서비스를 이용해주세요.");
                screenManager.show("MEMBER_LOGIN");
            } else {
                screenManager.show("MEMBER_MAIN");
            }

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "등록 실패", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "등록 중 오류가 발생했습니다. 입력값을 확인해주세요.");
            e.printStackTrace();
        }
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