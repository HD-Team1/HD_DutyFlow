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

public class MemberOrderDetailPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JLabel orderInfoLabel;
    private DefaultTableModel tableModel;
    private JTable itemTable;

    private Integer currentOrderId;

    public MemberOrderDetailPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("주문 상세", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        orderInfoLabel = new JLabel();
        orderInfoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        orderInfoLabel.setBorder(new EmptyBorder(20, 0, 20, 0));

        String[] columns = {
                "상품명",
                "카테고리",
                "용량",
                "수량",
                "단가($)",
                "할인율",
                "할인 적용 단가($)",
                "합계($)"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(30);
        itemTable.setAutoCreateRowSorter(true);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(orderInfoLabel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        bottomPanel.setOpaque(false);

        JButton pickupButton = createButton("픽업 예약", new Color(46, 204, 113));
        JButton cancelButton = createButton("주문 취소", new Color(231, 76, 60));
        JButton backButton = createButton("뒤로가기", new Color(149, 165, 166));

        pickupButton.addActionListener(e -> goPickupReservation());
        cancelButton.addActionListener(e -> cancelOrder());
        backButton.addActionListener(e -> screenManager.show("MEMBER_ORDER_HISTORY"));

        bottomPanel.add(pickupButton);
        bottomPanel.add(cancelButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadOrderDetail() {
        try {
            currentOrderId = screenManager.getSelectedOrderId();

            if (currentOrderId == null) {
                JOptionPane.showMessageDialog(this, "선택된 주문이 없습니다.");
                screenManager.show("MEMBER_ORDER_HISTORY");
                return;
            }

            tableModel.setRowCount(0);

            List<OrderDTO> orderDetails =
                    screenManager.getDutyFlowSystem().getOrderDetails(currentOrderId);

            if (orderDetails == null || orderDetails.isEmpty()) {
                orderInfoLabel.setText("주문번호: " + currentOrderId + " / 상세 상품 정보가 없습니다.");
                return;
            }

            OrderDTO first = orderDetails.get(0);

            orderInfoLabel.setText(
                    "주문번호: " + first.getOrderId()
                            + " / 주문일시: " + first.getOrderedAt()
                            + " / 현재 상태: " + first.getOrderState()
                            + " / 결제 금액: " + first.getTotalAmount()
            );

            for (OrderDTO item : orderDetails) {
                tableModel.addRow(new Object[] {
                        item.getProductName(),
                        item.getCategoryName(),
                        item.getCapacity(),
                        item.getQuantity(),
                        item.getDollarPrice(),
                        item.getDiscountPrice(),
                        item.getDiscountedUnitPrice(),
                        item.getTotalLinePrice()
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
                    "주문 상세 정보를 불러오는 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void goPickupReservation() {
        if (currentOrderId == null) {
            JOptionPane.showMessageDialog(this, "픽업 예약할 주문이 없습니다.");
            return;
        }

        screenManager.setSelectedOrderId(currentOrderId);
        screenManager.show("MEMBER_PICKUP_RESERVATION");
    }

    private void cancelOrder() {
        try {
            if (currentOrderId == null) {
                JOptionPane.showMessageDialog(this, "취소할 주문이 없습니다.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "주문번호 " + currentOrderId + "번을 취소하시겠습니까?",
                    "주문 취소",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            screenManager.getDutyFlowSystem().cancelOrder(currentOrderId);

            JOptionPane.showMessageDialog(
                    this,
                    "주문이 취소되었습니다.",
                    "취소 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            screenManager.show("MEMBER_ORDER_HISTORY");

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
        button.setPreferredSize(new Dimension(110, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    public void refresh() {
        loadOrderDetail();
    }
}