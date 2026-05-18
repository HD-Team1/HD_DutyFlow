package gui.member;

import java.awt.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import order.dto.OrderDTO;

public class MemberOrderHistoryPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTable orderTable;
    private DefaultTableModel tableModel;
    private List<OrderDTO> orders;

    public MemberOrderHistoryPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("주문 내역 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                "주문번호", "주문일시", "금액", "상태"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(30);
        orderTable.setAutoCreateRowSorter(true);

        add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        bottomPanel.setOpaque(false);

        JButton detailButton = createButton("상세 조회", new Color(52, 152, 219));
        JButton pickupButton = createButton("픽업 예약", new Color(46, 204, 113));
        JButton cancelButton = createButton("주문 취소", new Color(231, 76, 60));
        JButton backButton = createButton("돌아가기", new Color(149, 165, 166));

        detailButton.addActionListener(e -> showDetail());
        pickupButton.addActionListener(e -> reservePickup());
        cancelButton.addActionListener(e -> cancelOrder());
        backButton.addActionListener(e -> screenManager.show("MEMBER_MAIN"));

        bottomPanel.add(detailButton);
        bottomPanel.add(pickupButton);
        bottomPanel.add(cancelButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadOrders() {
        try {
            tableModel.setRowCount(0);

            orders = screenManager.getDutyFlowSystem().getMyOrders();

            if (orders == null || orders.isEmpty()) {
                return;
            }

            for (OrderDTO order : orders) {
                tableModel.addRow(new Object[] {
                        order.getOrderId(),
                        order.getOrderedAt(),
                        order.getTotalAmount(),
                        order.getOrderState()
                });
            }

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getErrorCode().getMessage(),
                    "알림",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "주문 내역 조회 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private OrderDTO getSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();

        if (selectedRow == -1) {
            return null;
        }

        int modelRow = orderTable.convertRowIndexToModel(selectedRow);

        if (orders == null || modelRow < 0 || modelRow >= orders.size()) {
            return null;
        }

        return orders.get(modelRow);
    }

    private void showDetail() {
        OrderDTO order = getSelectedOrder();

        if (order == null) {
            JOptionPane.showMessageDialog(this, "주문을 선택해주세요.");
            return;
        }

        screenManager.setSelectedOrderId(order.getOrderId());
        screenManager.show("MEMBER_ORDER_DETAIL");
    }

    private void reservePickup() {
        OrderDTO order = getSelectedOrder();

        if (order == null) {
            JOptionPane.showMessageDialog(this, "픽업 예약할 주문을 선택해주세요.");
            return;
        }

        screenManager.setSelectedOrderId(order.getOrderId());
        screenManager.show("MEMBER_PICKUP_RESERVATION");
    }

    private void cancelOrder() {
        try {
            OrderDTO order = getSelectedOrder();

            if (order == null) {
                JOptionPane.showMessageDialog(this, "취소할 주문을 선택해주세요.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "주문번호 " + order.getOrderId() + "번을 취소하시겠습니까?",
                    "주문 취소",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            screenManager.getDutyFlowSystem().cancelOrder(order.getOrderId());

            JOptionPane.showMessageDialog(
                    this,
                    "주문이 취소되었습니다.",
                    "취소 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadOrders();

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getErrorCode().getMessage(),
                    "알림",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "주문 취소 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    public void refresh() {
        loadOrders();
    }
}