package gui.brand;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import order.dto.OrderDTO;

public class BrandOrderHistoryPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private DefaultTableModel tableModel;
    private JTable orderTable;

    public BrandOrderHistoryPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("브랜드 판매 내역 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(new Color(45, 52, 71));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                "주문ID",
                "회원ID",
                "예약ID",
                "상품ID",
                "상품명",
                "카테고리",
                "용량",
                "수량",
                "달러 단가",
                "할인율",
                "할인 적용가",
                "라인 금액",
                "주문일시",
                "상태"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: // 주문ID
                    case 1: // 회원ID
                    case 2: // 예약ID
                    case 3: // 상품ID
                    case 6: // 용량
                    case 7: // 수량
                        return Integer.class;

                    case 8:  // 달러 단가
                    case 9:  // 할인율
                    case 10: // 할인 적용가
                    case 11: // 라인 금액
                        return BigDecimal.class;

                    case 12: // 주문일시
                        return LocalDateTime.class;

                    default:
                        return String.class;
                }
            }
        };

        orderTable = new JTable(tableModel);

        // 테이블 헤더 클릭 정렬 기능
        orderTable.setAutoCreateRowSorter(true);

        setupTableUI();

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton refreshButton = createStyledButton("새로고침", new Color(46, 204, 113));
        JButton backButton = createStyledButton("뒤로가기", new Color(149, 165, 166));

        refreshButton.addActionListener(e -> loadOrders());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupTableUI() {
        orderTable.setRowHeight(30);
        orderTable.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        orderTable.setSelectionBackground(new Color(232, 240, 254));
        orderTable.setGridColor(new Color(230, 230, 230));

        JTableHeader header = orderTable.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        for (int i = 0; i < orderTable.getColumnCount(); i++) {
            orderTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        int[] moneyColumns = {8, 9, 10, 11};

        for (int columnIndex : moneyColumns) {
            orderTable.getColumnModel().getColumn(columnIndex).setCellRenderer(rightRenderer);
        }

        orderTable.getColumnModel().getColumn(4).setPreferredWidth(180); // 상품명
        orderTable.getColumnModel().getColumn(12).setPreferredWidth(150); // 주문일시
    }

    private void loadOrders() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            tableModel.setRowCount(0);

            List<OrderDTO> orders = brandSystem.getOrdersByBrandName();

            for (OrderDTO order : orders) {
                tableModel.addRow(new Object[] {
                        order.getOrderId(),
                        order.getMemberId(),
                        order.getReservationId(),
                        order.getProductId(),
                        order.getProductName(),
                        order.getCategoryName(),
                        order.getCapacity(),
                        order.getQuantity(),
                        order.getDollarPrice(),
                        order.getDiscountPrice(),
                        order.getDiscountedUnitPrice(),
                        order.getTotalLinePrice(),
                        order.getOrderedAt(),
                        order.getOrderState()
                });
            }

        } catch (DutyFreeException e) {
            showError(e.getErrorCode().getMessage());

        } catch (Exception e) {
            showError("판매 내역을 불러오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private BrandSystem getLoginBrandSystem() {
        BrandSystem brandSystem = screenManager.getBrandSystem();

        if (brandSystem == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "브랜드 관리자 로그인이 필요합니다.",
                    "알림",
                    JOptionPane.WARNING_MESSAGE
            );

            screenManager.show("BRAND_MANAGER_LOGIN");
            return null;
        }

        return brandSystem;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);

        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void refresh() {
        loadOrders();
    }
}