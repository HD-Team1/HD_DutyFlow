package gui.pickup;

import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import common.CurrentTime;
import gui.ScreenManager;
import pickup.PickUpSystem;

/**
 * 인도장 관리 시스템 — 메인 메뉴 (화면설계서 섹션 1)
 *
 * [상단] 현재 시각 (상시)
 * [본문] ① 픽업 리스트 조회  ② 인도물품 수령확인  ③ MLPQ 시뮬레이션
 * [하단] 로그아웃
 */
public class PickupMainPanel extends JPanel {

    private final ScreenManager screenManager;
    private PickUpSystem pickUpSystem;
    private final JLabel clockLabel;
    private final Timer clockTimer;

    public void setPickUpSystem(PickUpSystem ps) {
        this.pickUpSystem = ps;
    }

    /* ── 색상 상수 ── */
    private static final Color BG          = new Color(0xF5F6FA);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color PRIMARY     = new Color(0x2D6CDF);
    private static final Color PRIMARY_HOVER = new Color(0x1B4FAF);
    private static final Color TEXT_DARK   = new Color(0x1E1E2F);
    private static final Color TEXT_SUB    = new Color(0x6B7280);
    private static final Color LOGOUT_BG   = new Color(0xEF4444);
    private static final Color LOGOUT_HOVER = new Color(0xDC2626);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd (E)  HH:mm:ss");

    public PickupMainPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG);

        /* ====== 상단 — 시계 ====== */
        clockLabel = createClockLabel();
        JPanel clockBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        clockBar.setBackground(new Color(0x1E293B));
        clockBar.add(clockLabel);
        add(clockBar, BorderLayout.NORTH);

        /* ====== 중앙 — 메뉴 카드 ====== */
        add(createCenterPanel(), BorderLayout.CENTER);

        /* ====== 하단 — 로그아웃 ====== */
        add(createBottomPanel(), BorderLayout.SOUTH);

        /* ====== 시계 타이머 (1초 갱신) ====== */
        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        /* ====== ESC 키 바인딩 ====== */
        bindEscKey();
    }

    /* ─────────────────── UI 생성 헬퍼 ─────────────────── */

    private JLabel createClockLabel() {
        JLabel lbl = new JLabel(CurrentTime.curTime.format(TIME_FMT));
        lbl.setFont(new Font("D2Coding", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private void updateClock() {
        clockLabel.setText(CurrentTime.curTime.format(TIME_FMT));
    }

    private JPanel createCenterPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(40, 60, 40, 60)
        ));

        // 타이틀
        JLabel title = new JLabel("인도장 관리 시스템");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("현대면세점 인천공항점");
        subtitle.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SUB);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));

        // 메뉴 버튼들
        String[][] menus = {
                {"① 픽업 리스트 조회",   "PICKUP_LIST"},
                {"② 인도물품 수령확인",   "PICKUP_VERIFY"},
                {"③ MLPQ 시뮬레이션",    "PICKUP_SIMULATION"},
        };

        for (String[] menu : menus) {
            JButton btn = createMenuButton(menu[0]);
            String target = menu[1];
            btn.addActionListener(e -> screenManager.show(target));
            card.add(btn);
            card.add(Box.createVerticalStrut(12));
        }

        wrapper.add(card);
        return wrapper;
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 17));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(360, 50));
        btn.setPreferredSize(new Dimension(360, 50));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY_HOVER); }
            @Override
            public void mouseExited(MouseEvent e)  { btn.setBackground(PRIMARY); }
        });

        return btn;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        panel.setBackground(BG);

        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(LOGOUT_BG);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(160, 38));

        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { logoutBtn.setBackground(LOGOUT_HOVER); }
            @Override
            public void mouseExited(MouseEvent e)  { logoutBtn.setBackground(LOGOUT_BG); }
        });

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this, "로그아웃 하시겠습니까?", "로그아웃",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (pickUpSystem != null) {
                        pickUpSystem.logout();
                    }
                } catch (Exception ex) {
                    // 로그아웃 실패해도 화면 전환은 진행
                    System.out.println("[로그아웃 오류] " + ex.getMessage());
                }
                screenManager.show("LOGIN_SELECT");
            }
        });

        panel.add(logoutBtn);
        return panel;
    }

    /* ─────────────────── ESC 키 바인딩 ─────────────────── */
    private void bindEscKey() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "goBack");
        getActionMap().put("goBack", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                screenManager.show("LOGIN_SELECT");
            }
        });
    }
}