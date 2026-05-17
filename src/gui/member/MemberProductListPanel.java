package gui.member;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import dutyFlowSystem.DutyFlowSystem;
import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import product.dto.ProductDTO;

public class MemberProductListPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTable productTable;
    private DefaultTableModel tableModel;

    private List<ProductDTO> products;

    private JLabel detailNameLabel;
    private JLabel detailBrandLabel;
    private JLabel detailDescriptionLabel;
    private JLabel detailStockLabel;
    private JLabel detailFinalPriceLabel;
    private JSpinner quantitySpinner;

    private JComboBox<String> categoryCombo;
    private JComboBox<String> brandCombo;
    private JCheckBox eventOnlyCheck;

    private static final Color BG_COLOR = new Color(245, 246, 250);
    private static final Color PRIMARY_COLOR = new Color(45, 108, 223);

    public MemberProductListPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel titleLabel = new JLabel("상품 조회");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        contentPanel.add(createProductTablePanel(), BorderLayout.CENTER);
        contentPanel.add(createDetailPanel(), BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createProductTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setOpaque(false);

        categoryCombo = new JComboBox<>(new String[]{"전체", "주류", "향수", "화장품", "건강식품"});
        brandCombo = new JComboBox<>(new String[]{"전체", "Johnnie Walker", "CHANEL", "DIOR", "정관장", "Ballantine's"});
        eventOnlyCheck = new JCheckBox("행사 상품만 보기");
        eventOnlyCheck.setOpaque(false);

        JButton filterButton = createStyledButton("필터 적용", new Color(52, 152, 219));
        filterButton.addActionListener(e -> applyFilter());

        filterPanel.add(new JLabel("카테고리"));
        filterPanel.add(categoryCombo);
        filterPanel.add(new JLabel("브랜드"));
        filterPanel.add(brandCombo);
        filterPanel.add(eventOnlyCheck);
        filterPanel.add(filterButton);

        String[] columns = {
                "상품ID",
                "상품명",
                "브랜드",
                "카테고리",
                "가격($)",
                "원화가",
                "할인율",
                "재고상태"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 4:
                    case 5:
                    case 6:
                        return BigDecimal.class;
                    default:
                        return String.class;
                }
            }
        };

        productTable = new JTable(tableModel);
        productTable.setRowHeight(30);
        productTable.setAutoCreateRowSorter(true);

        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailPanel();
            }
        });

        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    goDetailScreen();
                }
            }
        });

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel imageLabel = new JLabel("상품 이미지", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(235, 237, 240));
        imageLabel.setPreferredSize(new Dimension(0, 150));

        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 0, 10));
        infoPanel.setOpaque(false);

        detailNameLabel = new JLabel("상품을 선택하세요");
        detailBrandLabel = new JLabel("-");
        detailDescriptionLabel = new JLabel("-");
        detailStockLabel = new JLabel("-");
        detailFinalPriceLabel = new JLabel("-");

        infoPanel.add(detailNameLabel);
        infoPanel.add(detailBrandLabel);
        infoPanel.add(detailDescriptionLabel);
        infoPanel.add(detailStockLabel);
        infoPanel.add(detailFinalPriceLabel);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

        JButton addCartButton = createStyledButton("장바구니 담기", PRIMARY_COLOR);
        JButton cartButton = createStyledButton("장바구니 이동", new Color(46, 204, 113));

        addCartButton.addActionListener(e -> addToCart());
        cartButton.addActionListener(e -> screenManager.show("MEMBER_CART"));

        bottomPanel.add(new JLabel("수량"));
        bottomPanel.add(quantitySpinner);
        bottomPanel.add(addCartButton);
        bottomPanel.add(cartButton);

        panel.add(imageLabel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setOpaque(false);

        JButton detailButton = createStyledButton("상세 화면 보기", PRIMARY_COLOR);
        JButton backButton = createStyledButton("돌아가기", new Color(149, 165, 166));

        detailButton.addActionListener(e -> goDetailScreen());
        backButton.addActionListener(e -> screenManager.show("MEMBER_MAIN"));

        panel.add(detailButton);
        panel.add(backButton);

        return panel;
    }

    private void loadProducts() {
        try {
            DutyFlowSystem dutyFlowSystem = screenManager.getDutyFlowSystem();

            if (dutyFlowSystem == null) {
                JOptionPane.showMessageDialog(this, "회원 시스템이 연결되지 않았습니다.");
                return;
            }

            tableModel.setRowCount(0);

            products = dutyFlowSystem.getShoppingProducts();

            if (products == null || products.isEmpty()) {
                clearDetailPanel();
                return;
            }

            for (ProductDTO product : products) {
                addProductRow(product);
            }

            clearDetailPanel();

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
                    "상품 목록을 불러오는 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void applyFilter() {
        if (products == null) {
            return;
        }

        tableModel.setRowCount(0);

        String selectedCategory = String.valueOf(categoryCombo.getSelectedItem());
        String selectedBrand = String.valueOf(brandCombo.getSelectedItem());
        boolean eventOnly = eventOnlyCheck.isSelected();

        for (ProductDTO product : products) {
            String categoryName = product.getCategory() != null
                    ? product.getCategory().getCategoryName()
                    : "-";

            boolean categoryMatched =
                    "전체".equals(selectedCategory) || selectedCategory.equals(categoryName);

            boolean brandMatched =
                    "전체".equals(selectedBrand) || selectedBrand.equals(product.getBrandName());

            boolean eventMatched =
                    !eventOnly || product.isHasEvent();

            if (categoryMatched && brandMatched && eventMatched) {
                addProductRow(product);
            }
        }

        clearDetailPanel();
    }

    private void addProductRow(ProductDTO product) {
        tableModel.addRow(new Object[]{
                product.getProductId(),
                product.getProductName(),
                product.getBrandName(),
                product.getCategory() != null
                        ? product.getCategory().getCategoryName()
                        : "-",
                product.getPriceUsd(),
                product.getPriceKrw(),
                BigDecimal.valueOf(product.getDiscountRate()),
                getStockStatus(product)
        });
    }

    private ProductDTO getSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();

        if (selectedRow == -1) {
            return null;
        }

        int modelRow = productTable.convertRowIndexToModel(selectedRow);

        int productId = (int) tableModel.getValueAt(modelRow, 0);

        if (products == null) {
            return null;
        }

        for (ProductDTO product : products) {
            if (product.getProductId() == productId) {
                return product;
            }
        }

        return null;
    }

    private void updateDetailPanel() {
        ProductDTO product = getSelectedProduct();

        if (product == null) {
            clearDetailPanel();
            return;
        }

        detailNameLabel.setText("상품명: " + product.getProductName());
        detailBrandLabel.setText("브랜드: " + product.getBrandName());
        detailDescriptionLabel.setText(
                "<html>설명: "
                        + product.getProductName()
                        + "은(는) "
                        + product.getBrandName()
                        + " 브랜드의 면세 상품입니다."
                        + "</html>"
        );

        detailStockLabel.setText("상태: " + getStockStatus(product));

        detailFinalPriceLabel.setText(
                "최종가: $"
                        + product.getFinalPriceUsd()
                        + " / "
                        + product.getFinalPriceKrw()
                        + "원"
        );
    }

    private void goDetailScreen() {
        ProductDTO product = getSelectedProduct();

        if (product == null) {
            JOptionPane.showMessageDialog(this, "상품을 선택해주세요.");
            return;
        }

        screenManager.setSelectedProduct(product);
        screenManager.show("MEMBER_PRODUCT_DETAIL");
    }

    private void addToCart() {
        try {
            ProductDTO product = getSelectedProduct();

            if (product == null) {
                JOptionPane.showMessageDialog(this, "상품을 선택해주세요.");
                return;
            }

            int quantity = (int) quantitySpinner.getValue();

            screenManager.getDutyFlowSystem()
                    .addToCart(product.getProductId(), quantity);

            JOptionPane.showMessageDialog(
                    this,
                    "장바구니에 담았습니다.",
                    "장바구니 담기 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

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
                    "장바구니 담기 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private String getStockStatus(ProductDTO product) {
        /*
         * ProductDTO에 stockAmount 필드가 있다면 아래 방식 추천:
         *
         * if (product.getStockAmount() <= 0) {
         *     return "품절";
         * }
         */

        if (product.isHasEvent()) {
            return "행사중";
        }

        return "판매중";
    }

    private void clearDetailPanel() {
        detailNameLabel.setText("상품을 선택하세요");
        detailBrandLabel.setText("-");
        detailDescriptionLabel.setText("-");
        detailStockLabel.setText("-");
        detailFinalPriceLabel.setText("-");
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    public void refresh() {
        loadProducts();
    }
}