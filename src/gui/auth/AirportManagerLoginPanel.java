package gui.auth;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import exception.DutyFreeException;
import gui.ScreenManager;
import pickup.PickUpSystem;

/**
 * 인도장 관리자 로그인 패널
 * - PickUpSystem.login(managerId, password) 호출
 *   → AirportManagerService.login()
 *   → AirportManagerDao.findById() + authenticate()
 */
public class AirportManagerLoginPanel extends JPanel {

    private final ScreenManager screenManager;
    private PickUpSystem pickUpSystem;

    private JTextField managerIdField;
    private JPasswordField passwordField;

    private static final Color BG             = new Color(0xF5F6FA);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color PRIMARY        = new Color(0x2D6CDF);
    private static final Color PRIMARY_HOVER  = new Color(0x1B4FAF);
    private static final Color BACK_COLOR     = new Color(0x6B7280);

    public AirportManagerLoginPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridBagLayout());
        setBackground(BG);

        /* ── 카드 ── */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(40, 55, 40, 55)
        ));

        // 아이콘 + 타이틀
        JLabel icon = new JLabel("🛫");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("인도장 관리자 로그인");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(28));

        // 폼
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setMaximumSize(new Dimension(340, 120));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        managerIdField = new JTextField(15);
        passwordField  = new JPasswordField(15);
        managerIdField.setFont(new Font("D2Coding", Font.PLAIN, 15));
        passwordField.setFont(new Font("D2Coding", Font.PLAIN, 15));

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(formLabel("관리자 ID"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(managerIdField, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(formLabel("비밀번호"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(passwordField, gc);

        card.add(form);
        card.add(Box.createVerticalStrut(24));

        // 로그인 버튼
        JButton loginBtn = styledButton("로그인", PRIMARY);
        loginBtn.setMaximumSize(new Dimension(340, 44));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addMouseListener(hoverEffect(loginBtn, PRIMARY, PRIMARY_HOVER));
        loginBtn.addActionListener(e -> doLogin());

        // Enter 키로도 로그인
        passwordField.addActionListener(e -> doLogin());
        managerIdField.addActionListener(e -> passwordField.requestFocus());

        // 뒤로가기
        JButton backBtn = new JButton("← 돌아가기");
        backBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        backBtn.setForeground(BACK_COLOR);
        backBtn.setBackground(CARD_BG);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> {
            clearFields();
            screenManager.show("LOGIN_SELECT");
        });

        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(backBtn);

        add(card);

        // ESC 키
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "goBack");
        getActionMap().put("goBack", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                clearFields();
                screenManager.show("LOGIN_SELECT");
            }
        });
    }

    /* ── PickUpSystem 주입 ── */
    public void setPickUpSystem(PickUpSystem ps) {
        this.pickUpSystem = ps;
    }

    /* ── 로그인 처리 ── */
    private void doLogin() {
        String idText  = managerIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "관리자 ID를 입력하세요.");
            managerIdField.requestFocus();
            return;
        }
        if (password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "비밀번호를 입력하세요.");
            passwordField.requestFocus();
            return;
        }

        int managerId;
        try {
            managerId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "관리자 ID는 숫자로 입력해야 합니다.");
            managerIdField.requestFocus();
            return;
        }

        if (pickUpSystem == null) {
            JOptionPane.showMessageDialog(this,
                    "시스템이 초기화되지 않았습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // PickUpSystem → AirportManagerService → DAO.findById + authenticate
            pickUpSystem.login(managerId, password);

            JOptionPane.showMessageDialog(this,
                    "로그인 성공!\n인도장 관리 시스템에 입장합니다.");

            clearFields();
            screenManager.show("PICKUP_MAIN");

        } catch (DutyFreeException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getErrorCode().getMessage(),
                    "로그인 실패", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "로그인 중 오류가 발생했습니다.\n" + ex.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        managerIdField.setText("");
        passwordField.setText("");
    }

    /* ── UI 헬퍼 ── */
    private JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        return lbl;
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private MouseAdapter hoverEffect(JButton btn, Color normal, Color hover) {
        return new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        };
    }
}