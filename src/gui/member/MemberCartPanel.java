package gui.member;

import java.awt.*;
import java.math.BigDecimal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import shoppingCart.dto.CartItemDTO;
import shoppingCart.dto.TotalCartDTO;

public class MemberCartPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel totalQuantityLabel;
    private JLabel totalUsdLabel;
    private JLabel totalKrwLabel;

    public MemberCartPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("장바구니", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                "상품명", "용량", "수량", "단가($)", "단가(원)", "합계($)", "합계(원)"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(tableModel);
        cartTable.setRowHeight(30);
        cartTable.setAutoCreateRowSorter(true);

        add(new JScrollPane(cartTable), BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createBottomPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);

        totalQuantityLabel = createSummaryLabel("총 상품 수량: 0개");
        totalUsdLabel = createSummaryLabel("총 달러 금액: $0.00");
        totalKrwLabel = createSummaryLabel("총 원화 금액: 0원");

        summaryPanel.add(totalQuantityLabel);
        summaryPanel.add(totalUsdLabel);
        summaryPanel.add(totalKrwLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setOpaque(false);

        JButton updateButton = createButton("수량 변경", new Color(52, 152, 219));
        JButton deleteButton = createButton("선택 삭제", new Color(231, 76, 60));
        JButton clearButton = createButton("전체 삭제", new Color(192, 57, 43));
        JButton orderButton = createButton("주문하기", new Color(46, 204, 113));
        JButton backButton = createButton("돌아가기", new Color(149, 165, 166));

        updateButton.addActionListener(e -> updateQuantity());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        clearButton.addActionListener(e -> clearCart());
        orderButton.addActionListener(e -> order());
        backButton.addActionListener(e -> screenManager.show("MEMBER_MAIN"));

        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(orderButton);
        buttonPanel.add(backButton);

        wrapper.add(summaryPanel, BorderLayout.NORTH);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private JLabel createSummaryLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);

        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        label.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        return label;
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

    private void loadCart() {
        try {
            tableModel.setRowCount(0);

            TotalCartDTO cart = screenManager.getDutyFlowSystem().printCart();

            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                totalQuantityLabel.setText("총 상품 수량: 0개");
                totalUsdLabel.setText("총 달러 금액: $0.00");
                totalKrwLabel.setText("총 원화 금액: 0원");
                return;
            }

            for (CartItemDTO item : cart.getItems()) {
                BigDecimal dollarPrice = item.getDollarPrice();
                BigDecimal wonPrice = item.getWonPrice();
                int quantity = item.getQuantity();

                BigDecimal totalDollarPrice =
                        dollarPrice.multiply(BigDecimal.valueOf(quantity));

                BigDecimal totalWonPrice =
                        wonPrice.multiply(BigDecimal.valueOf(quantity));

                tableModel.addRow(new Object[] {
                        item.getProductName(),
                        item.getCapacity(),
                        quantity,
                        dollarPrice,
                        wonPrice,
                        totalDollarPrice,
                        totalWonPrice
                });
            }

            totalQuantityLabel.setText("총 상품 수량: " + cart.getTotalQuantity() + "개");
            totalUsdLabel.setText("총 달러 금액: $" + cart.getTotalDollarPrice());
            totalKrwLabel.setText("총 원화 금액: " + cart.getTotalWonPrice() + "원");

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
                    "장바구니 조회 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void updateQuantity() {
        int selectedRow = cartTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수량을 변경할 상품을 선택해주세요.");
            return;
        }

        int modelRow = cartTable.convertRowIndexToModel(selectedRow);

        String productName = String.valueOf(tableModel.getValueAt(modelRow, 0));
        int currentQuantity = (int) tableModel.getValueAt(modelRow, 2);

        String input = JOptionPane.showInputDialog(
                this,
                "변경할 수량을 입력하세요.",
                currentQuantity
        );

        if (input == null) {
            return;
        }

        try {
            int newQuantity = Integer.parseInt(input.trim());

            if (newQuantity <= 0) {
                JOptionPane.showMessageDialog(this, "수량은 1개 이상이어야 합니다.");
                return;
            }

            screenManager.getDutyFlowSystem()
                    .updateQuantity(productName, newQuantity);

            JOptionPane.showMessageDialog(this, "수량이 변경되었습니다.");
            loadCart();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "수량은 숫자로 입력해야 합니다.");

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
                    "수량 변경 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void deleteSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 상품을 선택해주세요.");
            return;
        }

        int modelRow = cartTable.convertRowIndexToModel(selectedRow);
        String productName = String.valueOf(tableModel.getValueAt(modelRow, 0));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "'" + productName + "' 상품을 장바구니에서 삭제하시겠습니까?",
                "선택 삭제",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            screenManager.getDutyFlowSystem()
                    .deleteFromCart(productName);

            JOptionPane.showMessageDialog(this, "선택한 상품이 삭제되었습니다.");
            loadCart();

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
                    "선택 삭제 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void clearCart() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "장바구니를 전체 삭제하시겠습니까?",
                "전체 삭제",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            screenManager.getDutyFlowSystem().deleteFromCart();

            JOptionPane.showMessageDialog(this, "장바구니가 비워졌습니다.");
            loadCart();

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
                    "전체 삭제 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

	private void order() {
	    String cardNumber = JOptionPane.showInputDialog(
	            this,
	            "카드번호를 입력하세요.\n예: 4111-1111-1111-1111",
	            "카드 결제",
	            JOptionPane.PLAIN_MESSAGE
	    );
	
	    if (cardNumber == null) {
	        return;
	    }
	
	    cardNumber = cardNumber.trim();
	
	    if (cardNumber.isEmpty()) {
	        JOptionPane.showMessageDialog(
	                this,
	                "카드번호를 입력해주세요.",
	                "알림",
	                JOptionPane.WARNING_MESSAGE
	        );
	        return;
	    }
	
	    showPaymentLoading(cardNumber);
	}
	
	private void showPaymentLoading(String cardNumber) {
	    JDialog loadingDialog = new JDialog(
	            SwingUtilities.getWindowAncestor(this),
	            "결제 진행 중",
	            Dialog.ModalityType.APPLICATION_MODAL
	    );

	    JPanel panel = new JPanel(new BorderLayout(15, 15));
	    panel.setBorder(new EmptyBorder(30, 40, 30, 40));
	    panel.setBackground(Color.WHITE);

	    JLabel messageLabel = new JLabel("결제 처리 중입니다...", SwingConstants.CENTER);
	    messageLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));

	    JLabel subMessageLabel = new JLabel("잠시만 기다려주세요.", SwingConstants.CENTER);
	    subMessageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
	    subMessageLabel.setForeground(Color.GRAY);

	    JProgressBar progressBar = new JProgressBar();
	    progressBar.setIndeterminate(true);

	    panel.add(messageLabel, BorderLayout.NORTH);
	    panel.add(progressBar, BorderLayout.CENTER);
	    panel.add(subMessageLabel, BorderLayout.SOUTH);

	    loadingDialog.setContentPane(panel);
	    loadingDialog.setSize(350, 180);
	    loadingDialog.setLocationRelativeTo(this);
	    loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

	    Timer timer = new Timer(2000, e -> {
	        loadingDialog.dispose();
	        processOrderAfterPaymentLoading(cardNumber);
	        ((Timer) e.getSource()).stop();
	    });

	    timer.setRepeats(false);
	    timer.start();

	    loadingDialog.setVisible(true);
	}
	
	private void processOrderAfterPaymentLoading(String cardNumber) {
	    try {
	        int orderId = screenManager.getDutyFlowSystem()
	                .makeOrder(cardNumber);

	        screenManager.getDutyFlowSystem()
	                .processOrderQueue();

	        JOptionPane.showMessageDialog(
	                this,
	                "주문 처리가 완료되었습니다.\n주문번호: " + orderId,
	                "주문 완료",
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
	                "주문 처리 중 오류가 발생했습니다.",
	                "오류",
	                JOptionPane.ERROR_MESSAGE
	        );
	        e.printStackTrace();
	    }
	}

    @Override
    public void refresh() {
        loadCart();
    }
}