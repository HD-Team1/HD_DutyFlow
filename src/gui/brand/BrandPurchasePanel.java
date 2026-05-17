package gui.brand;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;

public class BrandPurchasePanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTextField productNameField;
    private JTextField amountField;
    private JTextField cancelPurchaseIdField;

    private static final Color BG_COLOR = new Color(245, 246, 250);
    private static final Color TITLE_COLOR = new Color(45, 52, 71);
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color SECONDARY_COLOR = new Color(149, 165, 166);

    public BrandPurchasePanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("브랜드 발주 요청 및 취소", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));

        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        centerPanel.setOpaque(false);

        JPanel requestPanel = createSectionPanel("새 발주 요청");
        JPanel cancelPanel = createSectionPanel("발주 취소");

        buildRequestPanel(requestPanel);
        buildCancelPanel(cancelPanel);

        centerPanel.add(requestPanel);
        centerPanel.add(cancelPanel);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton backButton = createStyledButton("뒤로가기", SECONDARY_COLOR);
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void buildRequestPanel(JPanel requestPanel) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        productNameField = new JTextField();
        amountField = new JTextField();

        addFormField(formPanel, "상품명", productNameField, 0);
        addFormField(formPanel, "발주 수량", amountField, 1);

        JLabel helpLabel = new JLabel("<html><font color='gray'>* 등록된 상품명 기준으로 발주 요청됩니다.</font></html>");
        helpLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        GridBagConstraints helpGbc = new GridBagConstraints();
        helpGbc.gridx = 0;
        helpGbc.gridy = 2;
        helpGbc.gridwidth = 2;
        helpGbc.insets = new Insets(8, 10, 8, 10);
        helpGbc.anchor = GridBagConstraints.WEST;
        formPanel.add(helpLabel, helpGbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton purchaseButton = createStyledButton("발주 요청", PRIMARY_COLOR);
        purchaseButton.addActionListener(e -> requestPurchase());

        buttonPanel.add(purchaseButton);

        requestPanel.add(formPanel, BorderLayout.CENTER);
        requestPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void buildCancelPanel(JPanel cancelPanel) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        cancelPurchaseIdField = new JTextField();

        addFormField(formPanel, "발주 ID", cancelPurchaseIdField, 0);

        JLabel helpLabel = new JLabel("<html><font color='gray'>* 발주 이력 화면에서 발주 ID를 확인하세요.</font></html>");
        helpLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        GridBagConstraints helpGbc = new GridBagConstraints();
        helpGbc.gridx = 0;
        helpGbc.gridy = 1;
        helpGbc.gridwidth = 2;
        helpGbc.insets = new Insets(8, 10, 8, 10);
        helpGbc.anchor = GridBagConstraints.WEST;
        formPanel.add(helpLabel, helpGbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton cancelButton = createStyledButton("발주 취소", DANGER_COLOR);
        cancelButton.addActionListener(e -> cancelPurchase());

        buttonPanel.add(cancelButton);

        cancelPanel.add(formPanel, BorderLayout.CENTER);
        cancelPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 18),
                TITLE_COLOR
        );

        panel.setBorder(BorderFactory.createCompoundBorder(
                titledBorder,
                new EmptyBorder(25, 25, 25, 25)
        ));

        return panel;
    }

    private void addFormField(JPanel panel, String labelText, JTextField field, int row) {
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 10, 8, 10);

        panel.add(label, gbc);

        field.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(180, 32));

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(field, gbc);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);

        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(150, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void requestPurchase() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            String productName = productNameField.getText().trim();
            String amountText = amountField.getText().trim();

            if (productName.isEmpty() || amountText.isEmpty()) {
                showWarning("상품명과 발주 수량을 모두 입력하세요.");
                return;
            }

            int amount = Integer.parseInt(amountText);

            brandSystem.makePurchase(productName, amount);

            JOptionPane.showMessageDialog(
                    this,
                    "발주 요청이 완료되었습니다.",
                    "요청 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            productNameField.setText("");
            amountField.setText("");

        } catch (NumberFormatException e) {
            showWarning("발주 수량은 숫자로 입력해야 합니다.");

        } catch (DutyFreeException e) {
            showWarning(e.getErrorCode().getMessage());

        } catch (Exception e) {
            showError("발주 요청 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void cancelPurchase() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            String idText = cancelPurchaseIdField.getText().trim();

            if (idText.isEmpty()) {
                showWarning("취소할 발주 ID를 입력하세요.");
                return;
            }

            int purchaseId = Integer.parseInt(idText);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "발주 ID " + purchaseId + "번을 취소하시겠습니까?",
                    "발주 취소 확인",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            brandSystem.cancelPurchase(purchaseId);

            JOptionPane.showMessageDialog(
                    this,
                    "발주 취소 요청이 완료되었습니다.",
                    "취소 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            cancelPurchaseIdField.setText("");

        } catch (NumberFormatException e) {
            showWarning("발주 ID는 숫자로 입력해야 합니다.");

        } catch (DutyFreeException e) {
            showWarning(e.getErrorCode().getMessage());

        } catch (Exception e) {
            showError("발주 취소 중 오류가 발생했습니다.");
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

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "알림", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);
    }

    private void resetForm() {
        productNameField.setText("");
        amountField.setText("");
        cancelPurchaseIdField.setText("");
    }

    @Override
    public void refresh() {
        resetForm();
    }
}