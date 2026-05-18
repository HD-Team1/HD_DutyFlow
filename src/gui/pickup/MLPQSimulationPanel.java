package gui.pickup;

import java.awt.*;
import java.awt.event.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

import common.CurrentTime;
import common.Grade;
import gui.ScreenManager;
import gui.common.Refreshable;
import pickup.*;
import pickup.SimulationContext.TicketInfo;

public class MLPQSimulationPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;
    private final SimulationContext ctx;

    // ── 상태 ──
    private int lastScenario = -1;
    private boolean scenarioActionDone = false;
    private Integer selectedMemberId = null;   // 멤버 섹션 선택
    private boolean processingPickup = false;
    private Timer autoTimer, pickupTimer;
    private boolean autoRunning = false;
    private int pickupStepsRemaining = 0;

    // ── UI ──
    private JLabel clockLabel;
    private Timer clockRefresh;
    private TimelinePanel timelinePanel;
    private JButton pickupBtn;
    private JPanel orderPanel, memberPanel, aqBoxPanel, bqBoxPanel;
    private JTextArea logArea, descArea;
    private JSpinner pickupSpinner;

    // ── 색상 ──
    static final Color PRESTIGE_C = new Color(0x7C3AED);
    static final Color BLACK_C    = new Color(0x334155);
    static final Color GOLD_C     = new Color(0xD97706);
    static final Color SILVER_C   = new Color(0x94A3B8);
    static final Color BG         = new Color(0xF1F5F9);
    static final Color CARD       = Color.WHITE;
    static final Color BAR        = new Color(0x1E293B);
    static final Color AQ_C       = new Color(0xEF4444);
    static final Color BQ_C       = new Color(0x3B82F6);
    static final Color CALLED_BG  = new Color(0xFEE2E2);   // 연한 빨강 배경만
    static final Color HIGHLIGHT  = new Color(0xFEF9C3);   // 멤버 선택 노란색
    static final Color TD         = new Color(0x1E293B);
    static final Color TS         = new Color(0x64748B);
    static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("yyyy-MM-dd (E)  HH:mm:ss");
    static final DateTimeFormatter SF = DateTimeFormatter.ofPattern("HH:mm");

    /* ================================================================ */
    public MLPQSimulationPanel(ScreenManager sm) {
        this.screenManager = sm;
        this.ctx = new SimulationContext();
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        // NORTH: 탑바 + 타임라인(300px) + 수령버튼
        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setBackground(BG);
        north.add(buildTopBar());
        timelinePanel = new TimelinePanel();
        timelinePanel.setPreferredSize(new Dimension(0, 300));
        timelinePanel.setMinimumSize(new Dimension(0, 300));
        north.add(timelinePanel);
        north.add(buildPickupBar());
        add(north, BorderLayout.NORTH);

        // CENTER: 좌(주문+멤버) + 중(큐) + 우(시나리오+설명+컨트롤+로그)
        JPanel center = new JPanel(new BorderLayout(4, 0));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(4, 6, 6, 6));

        JPanel leftCols = new JPanel(new GridLayout(1, 2, 4, 0));
        leftCols.setBackground(BG);
        leftCols.setPreferredSize(new Dimension(370, 0));
        leftCols.add(buildOrderSection());
        leftCols.add(buildMemberSection());
        center.add(leftCols, BorderLayout.WEST);

        center.add(buildQueueSection(), BorderLayout.CENTER);
        center.add(buildRightPanel(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        bindKeys();
        clockRefresh = new Timer(300, e -> updateClock());
        clockRefresh.start();
    }

    public void setPickUpSystem(Object ps) { /* 호환 — 무시 */ }

    /* ================================================================
     *  상단 바
     * ================================================================ */
    private JPanel buildTopBar() {
        JPanel b = new JPanel(new BorderLayout());
        b.setBackground(BAR); b.setBorder(new EmptyBorder(5, 12, 5, 12));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel mode = new JLabel("◻ 시뮬레이션 모드");
        mode.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        mode.setForeground(new Color(0xFBBF24));
        b.add(mode, BorderLayout.WEST);
        clockLabel = new JLabel();
        clockLabel.setFont(new Font("D2Coding", Font.BOLD, 15));
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setHorizontalAlignment(SwingConstants.CENTER);
        b.add(clockLabel, BorderLayout.CENTER);
        JButton bk = flatBtn("← 메인", new Color(0x475569), Color.WHITE);
        bk.addActionListener(e -> goBack());
        b.add(bk, BorderLayout.EAST);
        return b;
    }

    private void updateClock() {
        if (clockLabel != null) clockLabel.setText(CurrentTime.curTime.format(TF));
    }

    /* ================================================================
     *  수령 버튼
     * ================================================================ */
    private JPanel buildPickupBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 3));
        bar.setBackground(BG);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pickupBtn = new JButton("수령 대기 중...");
        pickupBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        pickupBtn.setPreferredSize(new Dimension(400, 32));
        pickupBtn.setFocusPainted(false);
        pickupBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pickupBtn.setEnabled(false);
        pickupBtn.addActionListener(e -> startPickup());
        bar.add(pickupBtn);
        return bar;
    }

    /* ================================================================
     *  주문 섹션
     * ================================================================ */
    private JPanel buildOrderSection() {
        JPanel w = new JPanel(new BorderLayout(0, 4));
        w.setBackground(BG);
        w.add(title("주문 섹션"), BorderLayout.NORTH);
        orderPanel = new JPanel();
        orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
        orderPanel.setBackground(CARD);
        JScrollPane sp = new JScrollPane(orderPanel);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        sp.getVerticalScrollBar().setUnitIncrement(12);
        w.add(sp, BorderLayout.CENTER);
        return w;
    }

    /* ================================================================
     *  멤버 섹션 (각각이 버튼)
     * ================================================================ */
    private JPanel buildMemberSection() {
        JPanel w = new JPanel(new BorderLayout(0, 4));
        w.setBackground(BG);
        w.add(title("멤버 섹션"), BorderLayout.NORTH);
        memberPanel = new JPanel();
        memberPanel.setLayout(new BoxLayout(memberPanel, BoxLayout.Y_AXIS));
        memberPanel.setBackground(CARD);
        JScrollPane sp = new JScrollPane(memberPanel);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        sp.getVerticalScrollBar().setUnitIncrement(12);
        w.add(sp, BorderLayout.CENTER);
        return w;
    }

    /* ================================================================
     *  큐 섹션 (AQ 위, BQ 아래 — 컴팩트)
     * ================================================================ */
    private JPanel buildQueueSection() {
        JPanel w = new JPanel(new BorderLayout(0, 3));
        w.setBackground(BG);
        w.add(title("큐 섹션"), BorderLayout.NORTH);

        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 3));
        inner.setBackground(BG);

        aqBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        aqBoxPanel.setBackground(CARD);
        JPanel aqW = titled(aqBoxPanel, AQ_C, "AQ 우선큐");
        inner.add(aqW);

        bqBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        bqBoxPanel.setBackground(CARD);
        JPanel bqW = titled(bqBoxPanel, BQ_C, "BQ 일반큐");
        inner.add(bqW);

        w.add(inner, BorderLayout.CENTER);
        return w;
    }

    private JPanel titled(JPanel content, Color c, String t) {
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(CARD);
        w.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(c), " " + t + " ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 11), c));
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        w.add(sp, BorderLayout.CENTER);
        return w;
    }

    /* ================================================================
     *  우측 패널: 시나리오 선택 + 설명 + 컨트롤 + 로그
     * ================================================================ */
    private JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(600, 0));
        p.setBackground(BG);

     // ── 시나리오 선택 ──
        JPanel scen = boxed();
        scen.setLayout(new BoxLayout(scen, BoxLayout.Y_AXIS));
        scen.add(title("시나리오 선택"));
        scen.add(Box.createVerticalStrut(3));

        String[] names = {
            "1. 정상 호출", "2. 호출 타임아웃", "3. 노쇼 자동 처리", "4. 우선 등급 입장",
            "5. Starvation 에이징", "6. 항공 지연 재정렬", "7. 골든타임 보호"
        };

        // 버튼들을 담을 2x4 패널
        JPanel btnGrid = new JPanel(new GridLayout(2, 4, 4, 4));
        btnGrid.setOpaque(false);

        for (int i = 0; i < names.length; i++) {
            final int n = i + 1;
            JButton b = flatBtn(names[i], new Color(0xFFFDE7), TD);
            b.setFont(new Font("맑은 고딕", Font.BOLD, 10));
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xFDE68A)),
                    new EmptyBorder(5, 6, 5, 6)));
            b.addActionListener(e -> runScenario(n));
            btnGrid.add(b);
        }

        // 7개만 있으니 마지막 칸 1개를 비워두기 위한 더미 컴포넌트
        btnGrid.add(new JLabel());

        scen.add(btnGrid);
        p.add(scen);
        p.add(Box.createVerticalStrut(4));

        // ── 시나리오 설명 ──
        JPanel descW = boxed();
        descW.add(title("시나리오 설명"));
        descArea = new JTextArea("시나리오를 선택하세요.");
        descArea.setFont(new Font("D2Coding", Font.PLAIN, 12));
        descArea.setEditable(false); descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(0xFFFBEB));
        JScrollPane dsp = new JScrollPane(descArea);
        dsp.setPreferredSize(new Dimension(0, 220));
        dsp.setBorder(BorderFactory.createLineBorder(new Color(0xFDE68A)));
        descW.add(dsp);
        p.add(descW); p.add(Box.createVerticalStrut(4));

        // ── 컨트롤 ──
        JPanel ctrl = boxed();
        ctrl.add(title("컨트롤"));
        ctrl.add(Box.createVerticalStrut(3));
        JPanel r1 = new JPanel(new GridLayout(1, 2, 3, 0));
        r1.setBackground(CARD); r1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JButton bb = flatBtn("← 1분 전", new Color(0xE0E7FF), TD);
        bb.addActionListener(e -> stepBackward());
        JButton bf = flatBtn("1분 후 →", new Color(0xE0E7FF), TD);
        bf.addActionListener(e -> stepForward());
        r1.add(bb); r1.add(bf);
        ctrl.add(r1); ctrl.add(Box.createVerticalStrut(3));

        JPanel r2 = new JPanel(new GridLayout(1, 2, 3, 0));
        r2.setBackground(CARD); r2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JButton ba = flatBtn("▶ 자동실행", new Color(0xD1FAE5), TD);
        ba.addActionListener(e -> startAuto());
        JButton bp = flatBtn("⏸ 일시정지", new Color(0xFEE2E2), TD);
        bp.addActionListener(e -> pauseAuto());
        r2.add(ba); r2.add(bp);
        ctrl.add(r2); ctrl.add(Box.createVerticalStrut(4));

        JPanel r3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        r3.setBackground(CARD); r3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        r3.add(new JLabel("인도 시간:"));
        pickupSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        pickupSpinner.setFont(new Font("D2Coding", Font.BOLD, 13));
        pickupSpinner.setPreferredSize(new Dimension(50, 22));
        r3.add(pickupSpinner); r3.add(new JLabel("분"));
        ctrl.add(r3);
        p.add(ctrl); p.add(Box.createVerticalStrut(4));

        // ── 로그 ──
        JPanel logW = boxed();
        logW.add(title("실행 로그"));
        logArea = new JTextArea();
        logArea.setFont(new Font("D2Coding", Font.PLAIN, 12));
        logArea.setEditable(false); logArea.setLineWrap(true); logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(0xF8FAFC));
        JScrollPane lsp = new JScrollPane(logArea);
        lsp.setPreferredSize(new Dimension(0, 300));
        lsp.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        logW.add(lsp);
        p.add(logW);

        return p;
    }

    /* ================================================================
     *  키 바인딩
     * ================================================================ */
    private void bindKeys() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "fwd");
        am.put("fwd", act(e -> stepForward()));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "bck");
        am.put("bck", act(e -> stepBackward()));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "auto");
        am.put("auto", act(e -> { if (autoRunning) pauseAuto(); else startAuto(); }));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc");
        am.put("esc", act(e -> { if (autoRunning) pauseAuto(); else goBack(); }));
    }

    /* ================================================================
     *  시간 조작
     * ================================================================ */
    private void stepForward() {
        if (processingPickup) return;
        ctx.passTime();
        afterStep();
    }

    private void stepBackward() {
        if (processingPickup) return;
        if (!ctx.canUndo()) { appendLog("⚠ 더 이상 되돌릴 수 없습니다."); return; }
        ctx.undo();
        flushLog();
        refreshAll();
    }

    private void afterStep() {
        flushLog();
        for (String msg : ctx.drainPromotionAlerts()) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, msg,
                            "⬆️ BQ → AQ 승격!", JOptionPane.INFORMATION_MESSAGE));
        }
        refreshAll();
    }

    private void startAuto() {
        if (processingPickup || autoRunning) return;
        autoRunning = true;
        appendLog("▶ 자동실행 시작");
        autoTimer = new Timer(400, e -> stepForward());
        autoTimer.start();
    }

    private void pauseAuto() {
        autoRunning = false;
        if (autoTimer != null) { autoTimer.stop(); autoTimer = null; }
        appendLog("⏸ 자동실행 정지");
    }

    /* ================================================================
     *  수령 프로세스 (시간 소요, 중단 불가)
     * ================================================================ */
    private void startPickup() {
        if (ctx.getCurrentTicket() == null || processingPickup) return;
        if (autoRunning) pauseAuto();

        int duration = (int) pickupSpinner.getValue();
        processingPickup = true;
        ctx.setPickupInProgress(true);
        pickupStepsRemaining = duration;
        appendLog("📦 인도 시작 (" + duration + "분)...");

        pickupTimer = new Timer(350, e -> {
            ctx.passTime();
            flushLog();
            pickupStepsRemaining--;
            refreshAll();
            if (pickupStepsRemaining <= 0) {
                ((Timer) e.getSource()).stop();
                ctx.processPickUp();  // 내부에서 pickupInProgress=false + 다음호출
                flushLog();
                processingPickup = false;
                refreshAll();
            }
        });
        pickupTimer.start();
    }

    /* ================================================================
     *  시나리오 실행
     * ================================================================ */
    private void runScenario(int num) {
        if (processingPickup) return;
        if (autoRunning) pauseAuto();

        // 시나리오 6: 재클릭 시 지연 액션
        if (num == 6 && lastScenario == 6 && !scenarioActionDone) {
            appendLog("\n─── 항공 지연 발동 ───");
            ctx.delayFlight("KE305", CurrentTime.curTime.plusHours(3));
            ctx.rescheduledPq();
            scenarioActionDone = true;
            flushLog(); refreshAll();
            return;
        }

        ctx.loadScenario(num);
        lastScenario = num;
        scenarioActionDone = false;
        selectedMemberId = null;

        appendLog("\n═══ 시나리오 " + num + " ═══");
        flushLog();
        descArea.setText(SimulationContext.getDescription(num));
        descArea.setCaretPosition(0);
        refreshAll();
    }

    /* ================================================================
     *  멤버 클릭
     * ================================================================ */
    private void onMemberClick(int mid) {
        selectedMemberId = (selectedMemberId != null && selectedMemberId == mid) ? null : mid;
        refreshAll();
    }

    /* ================================================================
     *  전체 갱신
     * ================================================================ */
    private void refreshAll() {
        refreshMembers();
        refreshOrders();
        refreshQueue();
        refreshPickupBtn();
        timelinePanel.repaint();
        updateClock();
    }

    /* ── 멤버 섹션 ── */
    private void refreshMembers() {
        memberPanel.removeAll();
        for (TicketInfo ti : ctx.getTicketInfos()) {
            Color gc = gradeC(ti.member.getGrade());
            boolean sel = selectedMemberId != null && selectedMemberId == ti.member.getMemberId();
            JButton b = new JButton();
            b.setLayout(new BorderLayout(6, 0));
            b.setBackground(sel ? HIGHLIGHT : CARD);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(gc, sel ? 3 : 2),
                    new EmptyBorder(8, 10, 8, 10)));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel nm = new JLabel(ti.member.getName());
            nm.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            b.add(nm, BorderLayout.WEST);

            JLabel info = new JLabel(ti.airplane.getFlightCode() + " " +
                    ti.member.getGrade().name());
            info.setFont(new Font("D2Coding", Font.PLAIN, 11));
            info.setForeground(TS);
            b.add(info, BorderLayout.EAST);

            final int mid = ti.member.getMemberId();
            b.addActionListener(e -> onMemberClick(mid));
            memberPanel.add(b);
            memberPanel.add(Box.createVerticalStrut(3));
        }
        memberPanel.revalidate(); memberPanel.repaint();
    }

    /* ── 주문 섹션 ── */
    private void refreshOrders() {
        orderPanel.removeAll();
        List<TicketInfo> list = new ArrayList<>(ctx.getTicketInfos());
        list.sort(Comparator.comparing(ti -> ti.airplane.getDepartureAt()));

        for (TicketInfo ti : list) {
            boolean hl = selectedMemberId != null && selectedMemberId == ti.member.getMemberId();
            JPanel row = new JPanel(new BorderLayout(4, 0));
            row.setBackground(hl ? HIGHLIGHT : CARD);
            row.setBorder(new EmptyBorder(6, 8, 6, 8));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

            // 왼쪽: 시간+이름
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            left.setOpaque(false);
            left.add(lbl(ti.airplane.getDepartureAt().format(SF), "D2Coding", Font.BOLD, 12, TD));
            left.add(lbl(ti.member.getName(), "맑은 고딕", Font.BOLD, 12, TD));
            //left.add(gradeChip(ti.member.getGrade(), 10));
            row.add(left, BorderLayout.WEST);

            // 오른쪽: 상태
            Color sc = "NO_SHOW".equals(ti.status) ? AQ_C
                    : "PICKED_UP".equals(ti.status) ? new Color(0x059669)
                    : "CALLED".equals(ti.status) ? GOLD_C
                    : "TIMEOUT".equals(ti.status) ? new Color(0xEA580C)
                    : TS;
            JLabel st = lbl(ti.status, "D2Coding", Font.BOLD, 10, sc);
            row.add(st, BorderLayout.EAST);

            orderPanel.add(row);
        }
        orderPanel.revalidate(); orderPanel.repaint();
    }

    /* ── 큐 섹션 ── */
    private void refreshQueue() {
        aqBoxPanel.removeAll();
        bqBoxPanel.removeAll();
        PickUpTicket called = ctx.getCurrentTicket();

        // AQ 티켓
        List<PickUpTicket> aq = ctx.pq.getAllFromAq();
        aq.sort(Comparator.comparing((PickUpTicket t) -> t.getAirplane().getDepartureAt())
                .thenComparingInt(PickUpTicket::getTicketNum));

        // 호출된 티켓이 AQ 출신이면 AQ에 하이라이트로 표시
        if (called != null && ctx.isCalledFromAq()) {
            aqBoxPanel.add(ticketBox(called, true));
        }
        for (PickUpTicket t : aq) aqBoxPanel.add(ticketBox(t, false));

        // BQ 티켓
        List<PickUpTicket> bq = ctx.pq.getAllFromBq();
        bq.sort(Comparator.comparingInt((PickUpTicket t) -> t.getMember().getGrade().getPriority())
                .thenComparingInt(PickUpTicket::getTicketNum));

        // 호출된 티켓이 BQ 출신이면 BQ에 하이라이트로 표시
        if (called != null && !ctx.isCalledFromAq()) {
            bqBoxPanel.add(ticketBox(called, true));
        }
        for (PickUpTicket t : bq) bqBoxPanel.add(ticketBox(t, false));

        aqBoxPanel.revalidate(); aqBoxPanel.repaint();
        bqBoxPanel.revalidate(); bqBoxPanel.repaint();
    }

    /* ── 번호표 박스 ── */
    private JPanel ticketBox(PickUpTicket t, boolean isCalled) {
        Grade g = t.getMember().getGrade();
        Color gc = gradeC(g);
        boolean hl = selectedMemberId != null && selectedMemberId == t.getMember().getMemberId();

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setPreferredSize(new Dimension(123, 93));
        // 배경: 호출=연빨강, 선택=노랑, 기본=흰
        box.setBackground(isCalled ? CALLED_BG : hl ? HIGHLIGHT : CARD);
        // 테두리: 항상 등급색 (호출이어도!)
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(gc, 2),
                new EmptyBorder(3, 3, 3, 3)));

        JLabel nm = new JLabel(t.getMember().getName());
        nm.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        nm.setAlignmentX(CENTER_ALIGNMENT);
        box.add(nm);

        long ml = Duration.between(CurrentTime.curTime, t.getAirplane().getDepartureAt()).toMinutes();
        JLabel dep = new JLabel("출국~" + ml + "분");
        dep.setFont(new Font("D2Coding", Font.PLAIN, 12));
        dep.setForeground(ml < 30 ? AQ_C : TS);
        dep.setAlignmentX(CENTER_ALIGNMENT);
        box.add(dep);

        long wt = Duration.between(t.getTicketIssueTime(), CurrentTime.curTime).toMinutes();
        JLabel wl = new JLabel("대기+" + wt + "분");
        wl.setFont(new Font("D2Coding", Font.PLAIN, 12));
        wl.setForeground(wt >= 40 ? AQ_C : TS);
        wl.setAlignmentX(CENTER_ALIGNMENT);
        box.add(wl);

        if (isCalled) {
            JLabel tag = new JLabel("● 호출중");
            tag.setFont(new Font("맑은 고딕", Font.BOLD, 11));
            tag.setForeground(AQ_C);
            tag.setAlignmentX(CENTER_ALIGNMENT);
            box.add(tag);
        }
        return box;
    }

    /* ── 수령 버튼 ── */
    private void refreshPickupBtn() {
        PickUpTicket c = ctx.getCurrentTicket();
        if (processingPickup) {
            pickupBtn.setText("📦 인도 중... (잔여 " + pickupStepsRemaining + "분)");
            pickupBtn.setBackground(new Color(0xF59E0B));
            pickupBtn.setForeground(TD);
            pickupBtn.setEnabled(false);
        } else if (c != null) {
            pickupBtn.setText("▼ 수령: " + c.getMember().getName()
                    + " (" + c.getMember().getGrade().name() + ", "
                    + c.getAirplane().getFlightCode() + ") — 클릭하여 인도");
            pickupBtn.setBackground(new Color(0x059669));
            pickupBtn.setForeground(Color.WHITE);
            pickupBtn.setEnabled(true);
        } else {
            pickupBtn.setText("수령 대기 중...");
            pickupBtn.setBackground(new Color(0xE2E8F0));
            pickupBtn.setForeground(TS);
            pickupBtn.setEnabled(false);
        }
    }

    /* ================================================================
     *  타임라인 패널 (커스텀 페인팅)
     *  - 현재시각 = 정중앙 빨간선 (고정)
     *  - 정각(긴선), 10분(짧은선), 발권~출국 막대 = 시간에 따라 좌로 밀림
     *  - 막대: 출국 빠른 순 위에 표시, 등급 테두리색
     * ================================================================ */
    private class TimelinePanel extends JPanel {
        TimelinePanel() {
            setBackground(new Color(0xF8FAFC));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int cx = w / 2;
            double ppm = (double) w / 360.0; // ±3h = 360 min
            LocalDateTime now = CurrentTime.curTime;
            int barTop = 12, barBot = h - 32;
            int tickY = h - 30;

            // ── 눈금 ──
            LocalDateTime mark = now.minusHours(3).withMinute(
                    now.minusHours(3).getMinute() / 10 * 10).withSecond(0).withNano(0);
            while (mark.isBefore(now.plusHours(3).plusMinutes(1))) {
                int x = cx + (int)(Duration.between(now, mark).toMinutes() * ppm);
                boolean isHour = mark.getMinute() == 0;
                g2.setColor(new Color(isHour ? 0x94A3B8 : 0xCBD5E1));
                g2.setStroke(new BasicStroke(isHour ? 1.5f : 0.7f));
                int lh = isHour ? 20 : 10;
                g2.drawLine(x, tickY, x, tickY + lh);
                if (isHour) {
                    g2.setFont(new Font("D2Coding", Font.BOLD, 10));
                    g2.setColor(TS);
                    g2.drawString(mark.format(DateTimeFormatter.ofPattern("H:mm")), x - 10, h - 4);
                }
                mark = mark.plusMinutes(10);
            }

            // ── 발권~출국 막대 ──
            List<TicketInfo> active = ctx.getActiveTicketInfos();
            active.sort(Comparator.comparing(ti -> ti.airplane.getDepartureAt()));

            int barH = Math.max(12, Math.min(22, (barBot - barTop) / Math.max(active.size(), 1) - 2));
            int gap = 2;
            PickUpTicket called = ctx.getCurrentTicket();

            for (int i = 0; i < active.size(); i++) {
                TicketInfo ti = active.get(i);
                if (ti.ticket == null) continue;
                int x1 = cx + (int)(Duration.between(now, ti.ticket.getTicketIssueTime()).toMinutes() * ppm);
                int x2 = cx + (int)(Duration.between(now, ti.airplane.getDepartureAt()).toMinutes() * ppm);
                int y = barTop + i * (barH + gap);
                if (y + barH > barBot) break;

                Color gc = gradeC(ti.member.getGrade());
                boolean isCalled = called != null && called == ti.ticket;
                boolean hl = selectedMemberId != null && selectedMemberId == ti.member.getMemberId();

                // 막대 채우기
                g2.setColor(new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), hl ? 220 : 140));
                g2.fillRoundRect(Math.min(x1, x2), y, Math.abs(x2 - x1), barH, 4, 4);

                // 테두리
                g2.setColor(isCalled ? AQ_C : hl ? new Color(0xEAB308) : gc);
                g2.setStroke(new BasicStroke(isCalled ? 2.5f : hl ? 2f : 1f));
                g2.drawRoundRect(Math.min(x1, x2), y, Math.abs(x2 - x1), barH, 4, 4);

                // 이름
                g2.setFont(new Font("맑은 고딕", Font.BOLD, barH > 14 ? 10 : 9));
                g2.setColor(TD);
                g2.drawString(ti.member.getName(), Math.max(Math.min(x1, x2) + 3, 2), y + barH - 4);
            }

            // ── 현재시각 중앙선 (빨강, 고정) ──
            g2.setColor(AQ_C);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawLine(cx, 0, cx, tickY);
            g2.setFont(new Font("D2Coding", Font.BOLD, 10));
            g2.drawString("▼지금", cx - 14, tickY + 24);
        }
    }

    /* ================================================================
     *  유틸리티
     * ================================================================ */
    static Color gradeC(Grade g) {
        switch (g) {
            case PRESTIGE: return PRESTIGE_C;
            case BLACK: return BLACK_C;
            case GOLD: return GOLD_C;
            case SILVER: return SILVER_C;
            default: return SILVER_C;
        }
    }

    private JLabel gradeChip(Grade g, int sz) {
        JLabel c = new JLabel(" " + g.name() + " ");
        c.setFont(new Font("D2Coding", Font.BOLD, sz));
        c.setOpaque(true);
        c.setBackground(gradeC(g));
        c.setForeground(g == Grade.GOLD ? new Color(0x451A03)
                : g == Grade.SILVER ? TD : Color.WHITE);
        return c;
    }

    private JLabel title(String t) {
        JLabel l = new JLabel("  " + t);
        l.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        l.setForeground(TD); l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel lbl(String t, String f, int s, int sz, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(f, s, sz)); l.setForeground(c);
        return l;
    }

    private JButton flatBtn(String t, Color bg, Color fg) {
        JButton b = new JButton(t);
        b.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel boxed() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(6, 8, 6, 8)));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private AbstractAction act(java.util.function.Consumer<ActionEvent> fn) {
        return new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { fn.accept(e); }
        };
    }

    private void appendLog(String m) {
        logArea.append(m + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void flushLog() {
        for (String m : ctx.drainLog()) appendLog("  " + m);
    }

    private void goBack() {
        if (autoRunning) pauseAuto();
        screenManager.show("PICKUP_MAIN");
    }

    @Override
    public void refresh() {
        if (autoRunning) pauseAuto();
        refreshAll();
    }
}