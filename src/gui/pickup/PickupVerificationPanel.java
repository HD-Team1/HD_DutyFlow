package gui.pickup;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import admin.airportmanager.dto.PickUpListDTO;
import common.CurrentTime;
import common.OrderStatus;
import gui.ScreenManager;
import gui.common.Refreshable;
import order.dto.OrderUpdateDTO;
import pickup.PickUpDAO;
import pickup.PickUpSystem;
import pickup.dto.PickUpDTO;

/**
 * 인도물품 수령확인 화면 (화면설계서 섹션 3 — 시나리오 7)
 *
 * [Step 1] 고객 식별 입력 (여권번호 + 예약번호)
 * [Step 2] 신원 검증 결과 (초록/빨강/보라 분기)
 * [Step 3] 전달 확정 → 수령 완료
 */
public class PickupVerificationPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    /* ── 참조할 시스템 (MainFrame에서 set해줘야 함) ── */
    private PickUpSystem pickUpSystem;

    /* ── UI 컴포넌트 ── */
    private final CardLayout stepLayout;
    private final JPanel     stepContainer;

    // Step 1
    private JTextField passportField;
    private JTextField reservationField;

    // Step 2
    private JPanel     verifyResultPanel;
    private JLabel     verifyIcon;
    private JLabel     verifyMessage;
    private JButton    confirmBtn;
    private JButton    step2BackBtn;

    // Step 3
    private DefaultTableModel orderTableModel;
    private JTable            orderTable;

    // 현재 검증된 정보 저장
    private String verifiedPassport;
    private int    verifiedReservationId;
    private List<PickUpDTO> verifiedPickUpList;

    /* ── 시계 ── */
    private final JLabel clockLabel;
    private final Timer  clockTimer;

    /* ── 색상 ── */
    private static final Color BG          = new Color(0xF5F6FA);
    private static final Color BAR_BG      = new Color(0x1E293B);
    private static final Color PRIMARY     = new Color(0x2D6CDF);
    private static final Color SUCCESS     = new Color(0x16A34A);
    private static final Color DANGER      = new Color(0xEF4444);
    private static final Color PURPLE      = new Color(0x7C3AED);
    private static final Color SUCCESS_BG  = new Color(0xDCFCE7);
    private static final Color DANGER_BG   = new Color(0xFEE2E2);
    private static final Color PURPLE_BG   = new Color(0xEDE9FE);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd (E)  HH:mm:ss");

    /* ============================================================ */

    public PickupVerificationPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG);

        /* ── 상단 시계 ── */
        clockLabel = new JLabel();
        clockLabel.setFont(new Font("D2Coding", Font.BOLD, 15));
        clockLabel.setForeground(Color.WHITE);
        updateClock();

        JPanel clockBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        clockBar.setBackground(BAR_BG);
        clockBar.add(clockLabel);
        add(clockBar, BorderLayout.NORTH);

        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        /* ── 중앙: 3단계 CardLayout ── */
        stepLayout    = new CardLayout();
        stepContainer = new JPanel(stepLayout);
        stepContainer.setBackground(BG);

        stepContainer.add(createStep1(), "STEP1");
        stepContainer.add(createStep2(), "STEP2");
        stepContainer.add(createStep3(), "STEP3");

        add(stepContainer, BorderLayout.CENTER);

        /* ── ESC ── */
        bindEscKey();
    }

    /* ── PickUpSystem 주입 (MainFrame에서 호출) ── */
    public void setPickUpSystem(PickUpSystem ps) {
        this.pickUpSystem = ps;
    }

    /* ═══════════════════ STEP 1 — 고객 식별 입력 ═══════════════════ */

    private JPanel createStep1() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(35, 50, 35, 50)
        ));

        // 타이틀
        JLabel title = new JLabel("🔍  인도물품 수령확인");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel desc = new JLabel("여권번호와 예약번호를 입력하세요");
        desc.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        desc.setForeground(new Color(0x6B7280));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(24));

        // 폼
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 12));
        form.setBackground(Color.WHITE);
        form.setMaximumSize(new Dimension(400, 80));

        passportField     = new JTextField();
        reservationField  = new JTextField();
        passportField.setFont(new Font("D2Coding", Font.PLAIN, 15));
        reservationField.setFont(new Font("D2Coding", Font.PLAIN, 15));

        form.add(label("여권번호"));
        form.add(passportField);
        form.add(label("예약번호 (숫자)"));
        form.add(reservationField);

        card.add(form);
        card.add(Box.createVerticalStrut(20));

        // 버튼 행
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setBackground(Color.WHITE);

        JButton searchBtn = styledButton("조회", PRIMARY);
        searchBtn.addActionListener(e -> doSearch());

        JButton backBtn = styledButton("← 메인 메뉴", new Color(0x6B7280));
        backBtn.addActionListener(e -> screenManager.show("PICKUP_MAIN"));

        btnRow.add(searchBtn);
        btnRow.add(backBtn);
        card.add(btnRow);

        wrapper.add(card);
        return wrapper;
    }

    /* ═══════════════════ STEP 2 — 신원 검증 결과 ═══════════════════ */

    private JPanel createStep2() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG);

        verifyResultPanel = new JPanel();
        verifyResultPanel.setLayout(new BoxLayout(verifyResultPanel, BoxLayout.Y_AXIS));
        verifyResultPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(40, 60, 40, 60)
        ));

        verifyIcon = new JLabel();
        verifyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        verifyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        verifyMessage = new JLabel();
        verifyMessage.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        verifyMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        verifyResultPanel.add(verifyIcon);
        verifyResultPanel.add(Box.createVerticalStrut(16));
        verifyResultPanel.add(verifyMessage);
        verifyResultPanel.add(Box.createVerticalStrut(24));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);

        confirmBtn  = styledButton("전달 확정 →", SUCCESS);
        confirmBtn.addActionListener(e -> goToStep3());

        step2BackBtn = styledButton("← 돌아가기", new Color(0x6B7280));
        step2BackBtn.addActionListener(e -> {
            resetFields();
            stepLayout.show(stepContainer, "STEP1");
        });

        btnRow.add(confirmBtn);
        btnRow.add(step2BackBtn);
        verifyResultPanel.add(btnRow);

        wrapper.add(verifyResultPanel);
        return wrapper;
    }

    /* ═══════════════════ STEP 3 — 전달 확정 ═══════════════════ */

    private JPanel createStep3() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("📦  물품 전달 확정");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        panel.add(title, BorderLayout.NORTH);

        // 주문 목록 테이블
        String[] cols = {"선택", "주문ID", "상품", "픽업가능시각", "상태"};
        orderTableModel = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
        };

        orderTable = new JTable(orderTableModel);
        orderTable.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        orderTable.setRowHeight(30);
        orderTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        orderTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        orderTable.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane sp = new JScrollPane(orderTable);
        sp.setBorder(new LineBorder(new Color(0xE2E8F0)));
        panel.add(sp, BorderLayout.CENTER);

        // 버튼
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        btnRow.setBackground(BG);

        JButton pickUpBtn = styledButton("✔ 수령 완료", SUCCESS);
        pickUpBtn.setPreferredSize(new Dimension(160, 40));
        pickUpBtn.addActionListener(e -> doPickUp());

        JButton cancelBtn = styledButton("← 돌아가기", new Color(0x6B7280));
        cancelBtn.addActionListener(e -> {
            resetFields();
            stepLayout.show(stepContainer, "STEP1");
        });

        btnRow.add(pickUpBtn);
        btnRow.add(cancelBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    /* ═══════════════════ 비즈니스 로직 ═══════════════════ */

    /** Step 1 → Step 2: 조회 + 검증 */
    private void doSearch() {
        String passport = passportField.getText().trim();
        String resText  = reservationField.getText().trim();

        if (passport.isEmpty() || resText.isEmpty()) {
            showError("여권번호와 예약번호를 모두 입력하세요.");
            return;
        }

        int reservationId;
        try {
            reservationId = Integer.parseInt(resText);
        } catch (NumberFormatException ex) {
            showError("예약번호는 숫자로 입력하세요.");
            return;
        }

        try {
            PickUpDAO pickUpDAO = new PickUpDAO();
            List<PickUpDTO> pickUpList = pickUpDAO.getAllPickUp(passport, reservationId);

            // 조회 성공 → 여권·예약 모두 일치 (초록)
            verifiedPassport       = passport;
            verifiedReservationId  = reservationId;
            verifiedPickUpList     = pickUpList;

            showVerifyResult("SUCCESS",
                    "🟢", "여권 · 예약 정보 모두 일치합니다.",
                    SUCCESS, SUCCESS_BG);

        } catch (exception.DataNotFoundException ex) {
            // 예약 불일치 (보라)
            showVerifyResult("RESERVATION_MISMATCH",
                    "🟣", "예약 정보가 일치하지 않습니다.\n" + ex.getMessage(),
                    PURPLE, PURPLE_BG);
        } catch (Exception ex) {
            // 여권 불일치 또는 기타 오류 (빨강)
            showVerifyResult("PASSPORT_MISMATCH",
                    "🔴", "검증 실패: " + ex.getMessage(),
                    DANGER, DANGER_BG);
        }

        stepLayout.show(stepContainer, "STEP2");
    }

    private void showVerifyResult(String type, String icon, String msg, Color fg, Color bg) {
        verifyResultPanel.setBackground(bg);
        verifyIcon.setText(icon);
        verifyMessage.setText("<html><center>" + msg.replace("\n", "<br>") + "</center></html>");
        verifyMessage.setForeground(fg);

        // 전달 확정 버튼: 일치할 때만 활성
        boolean success = "SUCCESS".equals(type);
        confirmBtn.setEnabled(success);
        confirmBtn.setVisible(success);
    }

    /** Step 2 → Step 3 */
    private void goToStep3() {
        orderTableModel.setRowCount(0);

        if (verifiedPickUpList != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            for (PickUpDTO dto : verifiedPickUpList) {
                orderTableModel.addRow(new Object[]{
                        Boolean.TRUE,
                        dto.getReservationCode(),
                        "주문 상품",  // 실제로는 주문 상세 조회 필요
                        dto.getPickupAvailableAt() != null ? dto.getPickupAvailableAt().format(fmt) : "-",
                        "PICKUP_RESERVED"
                });
            }
        }

        stepLayout.show(stepContainer, "STEP3");
    }

    /** Step 3: 수령 완료 처리 */
    private void doPickUp() {
        if (pickUpSystem == null) {
            showError("PickUpSystem이 연결되지 않았습니다.");
            return;
        }

        int checkedCount = 0;
        for (int i = 0; i < orderTableModel.getRowCount(); i++) {
            Boolean checked = (Boolean) orderTableModel.getValueAt(i, 0);
            if (Boolean.TRUE.equals(checked)) {
                checkedCount++;
            }
        }

        if (checkedCount == 0) {
            showError("수령할 항목을 선택하세요.");
            return;
        }

        try {
            // 주문 상태 업데이트: PICKUP_RESERVED → PICKED_UP
            // processPickUp 호출 또는 직접 updateOrderState 호출
            pickUpSystem.processPickUp(verifiedPassport, verifiedReservationId, 3);

            // 테이블 상태 갱신
            for (int i = 0; i < orderTableModel.getRowCount(); i++) {
                Boolean checked = (Boolean) orderTableModel.getValueAt(i, 0);
                if (Boolean.TRUE.equals(checked)) {
                    orderTableModel.setValueAt("PICKED_UP", i, 4);
                }
            }

            // 완료 토스트
            JOptionPane.showMessageDialog(this,
                    "✅ 수령 완료 처리되었습니다.\n총 " + checkedCount + "건",
                    "수령 완료", JOptionPane.INFORMATION_MESSAGE);

            resetFields();
            stepLayout.show(stepContainer, "STEP1");

        } catch (Exception ex) {
            showError("수령 처리 중 오류: " + ex.getMessage());
        }
    }

    /* ─────────────────── 유틸 ─────────────────── */

    private void resetFields() {
        if (passportField != null)     passportField.setText("");
        if (reservationField != null)  reservationField.setText("");
        verifiedPassport      = null;
        verifiedReservationId = 0;
        verifiedPickUpList    = null;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        return lbl;
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 36));
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE);
    }

    private void updateClock() {
        clockLabel.setText(CurrentTime.curTime.format(TIME_FMT));
    }

    private void bindEscKey() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "goBack");
        getActionMap().put("goBack", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                resetFields();
                stepLayout.show(stepContainer, "STEP1");
            }
        });
    }

    @Override
    public void refresh() {
        resetFields();
        stepLayout.show(stepContainer, "STEP1");
    }
}
