package gui.member;


import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import gui.ScreenManager;
import gui.common.Refreshable;

public class MemberPaymentQueuePanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTable queueTable;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JLabel queueSizeLabel;
    private JLabel flowLabel;

    private final Queue<Integer> paymentQueue = new LinkedList<>();
    private int paymentSeq = 1;

    private Timer workerTimer;
    private boolean isProcessing = false;

    private static final Color BG_COLOR = new Color(245, 246, 250);
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color SECONDARY_COLOR = new Color(149, 165, 166);

    public MemberPaymentQueuePanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("결제 Queue 처리 화면", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        centerPanel.setOpaque(false);

        centerPanel.add(createQueuePanel());
        centerPanel.add(createLogPanel());

        add(centerPanel, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        workerTimer = new Timer(1800, e -> processNextPayment());
    }

    private JPanel createQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 215)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel subTitle = new JLabel("실시간 Queue 상태");
        subTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        String[] columns = {
                "paymentId", "상태", "처리시간"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        queueTable = new JTable(tableModel);
        queueTable.setRowHeight(30);
        queueTable.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        flowLabel = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "PENDING<br>↓<br>PROCESSING<br>↓<br>SUCCESS / FAIL"
                        + "</div></html>",
                SwingConstants.CENTER
        );
        flowLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        flowLabel.setOpaque(true);
        flowLabel.setBackground(new Color(245, 246, 250));
        flowLabel.setBorder(new EmptyBorder(15, 15, 15, 15));

        panel.add(subTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(queueTable), BorderLayout.CENTER);
        panel.add(flowLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 215)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel subTitle = new JLabel("실시간 로그 패널");
        subTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        logArea.setText("[INFO] 결제 Queue Worker 대기 중...\n");

        panel.add(subTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        queueSizeLabel = new JLabel("현재 Queue Size: 0");
        queueSizeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton enqueueButton = createButton("결제 요청 추가", PRIMARY_COLOR);
        JButton startButton = createButton("Worker 시작", SUCCESS_COLOR);
        JButton stopButton = createButton("Worker 중지", DANGER_COLOR);
        JButton backButton = createButton("뒤로가기", SECONDARY_COLOR);

        enqueueButton.addActionListener(e -> enqueuePayment());
        startButton.addActionListener(e -> startWorker());
        stopButton.addActionListener(e -> stopWorker());
        backButton.addActionListener(e -> screenManager.show("MEMBER_MAIN"));

        buttonPanel.add(enqueueButton);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(backButton);

        bottomPanel.add(queueSizeLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        return bottomPanel;
    }

    private void enqueuePayment() {
        int paymentId = paymentSeq++;
        paymentQueue.offer(paymentId);

        tableModel.addRow(new Object[] {
                paymentId,
                "PENDING",
                "-"
        });

        appendLog("[QUEUE] paymentId=" + paymentId + " enqueue 완료");
        updateQueueSize();
    }

    private void startWorker() {
        if (!workerTimer.isRunning()) {
            workerTimer.start();
            appendLog("[WORKER] 결제 Worker 시작");
        }
    }

    private void stopWorker() {
        if (workerTimer.isRunning()) {
            workerTimer.stop();
            appendLog("[WORKER] 결제 Worker 중지");
        }
    }

    private void processNextPayment() {
        if (isProcessing || paymentQueue.isEmpty()) {
            return;
        }

        isProcessing = true;

        int paymentId = paymentQueue.poll();
        int rowIndex = findRowByPaymentId(paymentId);

        if (rowIndex == -1) {
            isProcessing = false;
            updateQueueSize();
            return;
        }

        tableModel.setValueAt("PROCESSING", rowIndex, 1);
        tableModel.setValueAt(now(), rowIndex, 2);

        appendLog("[WORKER] paymentId=" + paymentId + " 결제 처리 시작");

        Timer finishTimer = new Timer(1200, e -> {
            boolean success = paymentId % 5 != 0;

            tableModel.setValueAt(success ? "SUCCESS" : "FAIL", rowIndex, 1);
            tableModel.setValueAt(now(), rowIndex, 2);

            if (success) {
                appendLog("[SUCCESS] paymentId=" + paymentId + " 결제 승인 완료");
            } else {
                appendLog("[FAIL] paymentId=" + paymentId + " 결제 승인 실패");
            }

            isProcessing = false;
            updateQueueSize();

            ((Timer) e.getSource()).stop();
        });

        finishTimer.setRepeats(false);
        finishTimer.start();

        updateQueueSize();
    }

    private int findRowByPaymentId(int paymentId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object value = tableModel.getValueAt(i, 0);

            if (value instanceof Integer && ((Integer) value) == paymentId) {
                return i;
            }
        }

        return -1;
    }

    private void updateQueueSize() {
        queueSizeLabel.setText("현재 Queue Size: " + paymentQueue.size());
    }

    private void appendLog(String message) {
        logArea.append(now() + " " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String now() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    public void refresh() {
        updateQueueSize();
    }
}
