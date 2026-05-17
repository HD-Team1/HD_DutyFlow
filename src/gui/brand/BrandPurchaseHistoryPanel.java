package gui.brand;

import java.awt.*;
import java.io.File;
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
import stock.dto.StockPurchaseHistoryDto;

public class BrandPurchaseHistoryPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private DefaultTableModel tableModel;
    private JTable purchaseTable;

    public BrandPurchaseHistoryPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("브랜드 발주 이력 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(new Color(45, 52, 71));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                "발주ID",
                "상품ID",
                "상품명",
                "카테고리",
                "가격($)",
                "가격(원)",
                "임계값",
                "발주일시",
                "수량",
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
                    case 0: // 발주ID
                    case 1: // 상품ID
                    case 6: // 임계값
                    case 8: // 수량
                        return Integer.class;

                    case 4: // 가격($)
                    case 5: // 가격(원)
                        return BigDecimal.class;

                    case 7: // 발주일시
                        return LocalDateTime.class;

                    default:
                        return String.class;
                }
            }
        };

        purchaseTable = new JTable(tableModel);

        // 테이블 헤더 클릭 정렬 기능
        purchaseTable.setAutoCreateRowSorter(true);

        setupTableUI();

        JScrollPane scrollPane = new JScrollPane(purchaseTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton refreshButton = createStyledButton("새로고침", new Color(46, 204, 113));
        JButton exportButton = createStyledButton("CSV 저장", new Color(52, 152, 219));
        JButton backButton = createStyledButton("뒤로가기", new Color(149, 165, 166));

        refreshButton.addActionListener(e -> loadPurchaseHistory());
        exportButton.addActionListener(e -> exportPurchaseHistory());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        bottomPanel.add(refreshButton);
        bottomPanel.add(exportButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupTableUI() {
        purchaseTable.setRowHeight(30);
        purchaseTable.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        purchaseTable.setSelectionBackground(new Color(232, 240, 254));
        purchaseTable.setGridColor(new Color(230, 230, 230));

        JTableHeader header = purchaseTable.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer usdRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(JLabel.RIGHT);

                if (value == null) {
                    setText("-");
                    return;
                }

                if (value instanceof BigDecimal) {
                    setText(String.format("$%,.2f", value));
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
                    setText(String.format("%,.0f원", value));
                    return;
                }

                setText(value.toString());
            }
        };

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        for (int i = 0; i < purchaseTable.getColumnCount(); i++) {
            purchaseTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        purchaseTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        purchaseTable.getColumnModel().getColumn(7).setPreferredWidth(150);

        purchaseTable.getColumnModel().getColumn(4).setCellRenderer(usdRenderer);
        purchaseTable.getColumnModel().getColumn(5).setCellRenderer(krwRenderer);
        purchaseTable.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        purchaseTable.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
    }

    private void loadPurchaseHistory() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            tableModel.setRowCount(0);

            List<StockPurchaseHistoryDto> purchases =
                    brandSystem.getMyBrandPurchaseHistory();

            if (purchases == null || purchases.isEmpty()) {
                return;
            }

            for (StockPurchaseHistoryDto purchase : purchases) {
                tableModel.addRow(new Object[] {
                        purchase.getPurchaseId(),
                        purchase.getProductId(),
                        purchase.getProductName(),
                        purchase.getCategoryName(),
                        purchase.getPriceUsd(),
                        purchase.getPriceKrw(),
                        purchase.getThresholdValue(),
                        purchase.getPurchaseDate(),
                        purchase.getAmount(),
                        purchase.getStatus() != null ? purchase.getStatus().name() : "-"
                });
            }

        } catch (DutyFreeException e) {
            showWarning(e.getErrorCode().getMessage());

        } catch (Exception e) {
            showError("발주 이력을 불러오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void exportPurchaseHistory() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("발주 이력 저장 위치 선택");

            String defaultFileName =
                    "purchase_history_"
                            + brandSystem.getBrandName().replaceAll("\\s+", "_")
                            + ".csv";

            fileChooser.setSelectedFile(new File(defaultFileName));

            int result = fileChooser.showSaveDialog(this);

            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File selectedFile = fileChooser.getSelectedFile();

            if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
            }

            brandSystem.exportPurchaseHistoryToFile(selectedFile);

            JOptionPane.showMessageDialog(
                    this,
                    "발주 이력이 성공적으로 저장되었습니다.\n경로: " + selectedFile.getAbsolutePath(),
                    "저장 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (DutyFreeException e) {
            showWarning(e.getErrorCode().getMessage());

        } catch (Exception e) {
            showError("파일 저장 중 오류가 발생했습니다.");
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
        loadPurchaseHistory();
    }
}