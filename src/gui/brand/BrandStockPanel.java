package gui.brand;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import stock.dto.StockProductDto;

public class BrandStockPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTable stockTable;
    private DefaultTableModel tableModel;

    private JLabel totalCountLabel;
    private JLabel lowStockLabel;
    private JLabel outOfStockLabel;

    private static final Color PRIMARY_COLOR = new Color(0x2D6CDF);
    private static final Color DANGER_COLOR = new Color(0xE74C3C);
    private static final Color WARNING_COLOR = new Color(0xF39C12);
    private static final Color BG_COLOR = new Color(0xF5F6FA);
    private static final Color TITLE_COLOR = new Color(0x2F3640);

    public BrandStockPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("내 브랜드 재고 현황");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        northPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(new Dimension(0, 100));

        totalCountLabel = createSummaryCard(summaryPanel, "전체 상품", new Color(0x7F8C8D));
        lowStockLabel = createSummaryCard(summaryPanel, "재고 부족", WARNING_COLOR);
        outOfStockLabel = createSummaryCard(summaryPanel, "품절 항목", DANGER_COLOR);

        northPanel.add(summaryPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        String[] columnNames = {
                "상품명",
                "카테고리",
                "용량",
                "가격($)",
                "가격(원)",
                "제조일자",
                "재고수량",
                "임계값",
                "상태"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: // 용량
                    case 6: // 재고수량
                    case 7: // 임계값
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };

        stockTable = new JTable(tableModel);

        // 테이블 헤더 클릭 정렬 기능
        stockTable.setAutoCreateRowSorter(true);

        setupTableUI();

        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xDCDDE1)));

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        bottomPanel.setOpaque(false);

        JButton refreshBtn = createStyledButton("새로고침", PRIMARY_COLOR);
        JButton backBtn = createStyledButton("뒤로가기", new Color(0x6B7280));

        refreshBtn.addActionListener(e -> loadStockData());
        backBtn.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        bottomPanel.add(refreshBtn);
        bottomPanel.add(backBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupTableUI() {
        stockTable.setRowHeight(35);
        stockTable.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        stockTable.setGridColor(new Color(0xF1F2F6));
        stockTable.setSelectionBackground(new Color(0xEBEDF0));
        stockTable.setSelectionForeground(Color.BLACK);

        JTableHeader header = stockTable.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setBackground(new Color(0x2F3640));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        header.setReorderingAllowed(false);

        applyCustomRenderer();

        stockTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        stockTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        stockTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        stockTable.getColumnModel().getColumn(8).setPreferredWidth(100);
    }

    private JLabel createSummaryCard(JPanel parent, String title, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCDDE1), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        titleLbl.setForeground(color);

        JLabel valueLbl = new JLabel("0");
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.SOUTH);

        parent.add(card);

        return valueLbl;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);

        btn.setPreferredSize(new Dimension(130, 45));
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    private void applyCustomRenderer() {
        stockTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int col
            ) {
                Component c = super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        col
                );

                setHorizontalAlignment(SwingConstants.CENTER);

                int modelRow = table.convertRowIndexToModel(row);
                String status = String.valueOf(table.getModel().getValueAt(modelRow, 8));

                if (!isSelected) {
                    if ("품절".equals(status)) {
                        c.setBackground(new Color(0xFFF0F0));
                    } else if ("재고 부족".equals(status)) {
                        c.setBackground(new Color(0xFFFAF0));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });
    }

    private void loadStockData() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            tableModel.setRowCount(0);

            List<StockProductDto> stockList = brandSystem.getMyBrandStocks();

            int lowStock = 0;
            int outOfStock = 0;

            for (StockProductDto stock : stockList) {
                int amount = stock.getAmount();
                int thresholdValue = stock.getThresholdValue();

                String status = "판매중";

                if (amount == 0) {
                    status = "품절";
                    outOfStock++;
                } else if (amount <= thresholdValue) {
                    status = "재고 부족";
                    lowStock++;
                }

                tableModel.addRow(new Object[] {
                        stock.getProductName(),
                        stock.getCategory() != null
                                ? stock.getCategory().getCategoryName()
                                : "-",
                        stock.getCapacity(),
                        formatUsd(stock.getPriceUsd()),
                        formatKrw(stock.getPriceKrw()),
                        stock.getManufacturedDate() != null
                                ? stock.getManufacturedDate().toString()
                                : "-",
                        stock.getAmount(),
                        stock.getThresholdValue(),
                        status
                });
            }

            totalCountLabel.setText(String.valueOf(stockList.size()));
            lowStockLabel.setText(String.valueOf(lowStock));
            outOfStockLabel.setText(String.valueOf(outOfStock));

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
                    "재고 정보를 불러오는 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
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

    private String formatUsd(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        return String.format("$%,.2f", value);
    }

    private String formatKrw(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        return String.format("%,.0f원", value);
    }

    @Override
    public void refresh() {
        loadStockData();
    }
}