package gui.brand;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
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
import product.Product;
import product.dto.ProductDTO;

public class BrandProductListPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private DefaultTableModel tableModel;
    private JTable productTable;

    private final DecimalFormat krwFormat = new DecimalFormat("#,###원");
    private final DecimalFormat usdFormat = new DecimalFormat("$#,##0.00");

    public BrandProductListPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("브랜드 상품 목록 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(new Color(45, 52, 71));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                "상품명",
                "브랜드",
                "카테고리",
                "용량",
                "가격($)",
                "가격(원)",
                "할인율",
                "행사",
                "최종가($)",
                "최종가(원)",
                "임계값"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 3:  // 용량
                    case 10: // 임계값
                        return Integer.class;

                    case 4: // 가격($)
                    case 5: // 가격(원)
                    case 6: // 할인율
                    case 8: // 최종가($)
                    case 9: // 최종가(원)
                        return BigDecimal.class;

                    default:
                        return String.class;
                }
            }
        };

        productTable = new JTable(tableModel);

        // 테이블 헤더 클릭 정렬 기능
        productTable.setAutoCreateRowSorter(true);

        setupTableUI();

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton refreshButton = createStyledButton("새로고침", new Color(46, 204, 113));
        JButton backButton = createStyledButton("뒤로가기", new Color(149, 165, 166));

        refreshButton.addActionListener(e -> loadProducts());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupTableUI() {
        productTable.setRowHeight(30);
        productTable.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        productTable.setSelectionBackground(new Color(232, 240, 254));
        productTable.setGridColor(new Color(230, 230, 230));

        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(JLabel.RIGHT);

                if (value == null) {
                    setText("-");
                    return;
                }

                if (value instanceof BigDecimal) {
                    setText(((BigDecimal) value).stripTrailingZeros().toPlainString());
                    return;
                }

                setText(value.toString());
            }
        };

        DefaultTableCellRenderer usdRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(JLabel.RIGHT);

                if (value == null) {
                    setText("-");
                    return;
                }

                if (value instanceof BigDecimal) {
                    setText(usdFormat.format(value));
                    return;
                }

                setText(value.toString());
            }
        };

        DefaultTableCellRenderer krwRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(JLabel.RIGHT);

                if (value == null) {
                    setText("-");
                    return;
                }

                if (value instanceof BigDecimal) {
                    setText(krwFormat.format(value));
                    return;
                }

                setText(value.toString());
            }
        };

        DefaultTableCellRenderer discountRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(JLabel.RIGHT);

                if (value == null) {
                    setText("-");
                    return;
                }

                if (value instanceof BigDecimal) {
                    setText(((BigDecimal) value).stripTrailingZeros().toPlainString() + "%");
                    return;
                }

                setText(value + "%");
            }
        };

        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        productTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        productTable.getColumnModel().getColumn(4).setCellRenderer(usdRenderer);
        productTable.getColumnModel().getColumn(5).setCellRenderer(krwRenderer);
        productTable.getColumnModel().getColumn(6).setCellRenderer(discountRenderer);
        productTable.getColumnModel().getColumn(8).setCellRenderer(usdRenderer);
        productTable.getColumnModel().getColumn(9).setCellRenderer(krwRenderer);
        productTable.getColumnModel().getColumn(10).setCellRenderer(rightRenderer);
    }

    private void loadProducts() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            tableModel.setRowCount(0);

            List<Product> products = brandSystem.getProductsByBrandName();


            if (products == null || products.isEmpty()) {
                return;
            }
            for (Product product : products) {
                tableModel.addRow(new Object[] {
                        product.getProductName(),
                        product.getBrandName(),
                        product.getCategory() != null
                                ? product.getCategory().getCategoryName()
                                : "-",
                        product.getCapacity(),
                        product.getPriceUsd(),
                        product.getPriceKrw(),
                        BigDecimal.valueOf(product.getDiscountRate()),
//                        product.isHasEvent() ? "Y" : "N",
                        product.getThresholdValue()
                });
            }

        } catch (DutyFreeException e) {
            showWarning(e.getErrorCode().getMessage());

        } catch (Exception e) {
            showError("브랜드 상품 목록을 불러오는 중 오류가 발생했습니다.");
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

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "알림", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void refresh() {
        loadProducts();
    }
}