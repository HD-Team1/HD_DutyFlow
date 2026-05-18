package gui.brand;

import java.awt.*;
import java.math.BigDecimal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;

public class BrandProductManagePanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTextField categoryNameField;
    private JTextField productNameField;
    private JTextField capacityField;
    private JTextField priceUsdField;
    private JTextField priceKrwField;
    private JTextField thresholdField;
    private JTextField purchaseAmountField;
    private JTextField deleteProductNameField;

    private static final Color BG_COLOR = new Color(245, 246, 250);
    private static final Color TITLE_COLOR = new Color(45, 52, 71);
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color SECONDARY_COLOR = new Color(149, 165, 166);

    public BrandProductManagePanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("상품 등록 및 관리", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));

        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        centerPanel.setOpaque(false);

        JPanel registerPanel = createSectionPanel("신규 상품 등록");
        JPanel deletePanel = createSectionPanel("상품 삭제");

        buildRegisterPanel(registerPanel);
        buildDeletePanel(deletePanel);

        centerPanel.add(registerPanel);
        centerPanel.add(deletePanel);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton backButton = createStyledButton("뒤로가기", SECONDARY_COLOR);
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void buildRegisterPanel(JPanel registerPanel) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        categoryNameField = new JTextField();
        productNameField = new JTextField();
        capacityField = new JTextField();
        priceUsdField = new JTextField();
        priceKrwField = new JTextField();
        thresholdField = new JTextField();
        purchaseAmountField = new JTextField();

        addFormField(formPanel, "카테고리명", categoryNameField, 0);
        addFormField(formPanel, "상품명", productNameField, 1);
        addFormField(formPanel, "용량(ml)", capacityField, 2);
        addFormField(formPanel, "달러 가격($)", priceUsdField, 3);
        addFormField(formPanel, "원화 가격(원)", priceKrwField, 4);
        addFormField(formPanel, "재고 임계값", thresholdField, 5);
        addFormField(formPanel, "초기 발주 수량", purchaseAmountField, 6);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton registerButton = createStyledButton("상품 등록", PRIMARY_COLOR);
        JButton registerAndPurchaseButton = createStyledButton("등록 + 즉시 발주", SUCCESS_COLOR);

        registerButton.addActionListener(e -> registerNewProduct());
        registerAndPurchaseButton.addActionListener(e -> registerNewProductAndPurchase());

        buttonPanel.add(registerButton);
        buttonPanel.add(registerAndPurchaseButton);

        registerPanel.add(formPanel, BorderLayout.CENTER);
        registerPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void buildDeletePanel(JPanel deletePanel) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        deleteProductNameField = new JTextField();

        addFormField(formPanel, "삭제할 상품명", deleteProductNameField, 0);

        JLabel helpLabel = new JLabel("<html><font color='gray'>* 로그인한 브랜드의 상품만 삭제할 수 있습니다.</font></html>");
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

        JButton deleteButton = createStyledButton("상품 삭제", DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteProduct());

        buttonPanel.add(deleteButton);

        deletePanel.add(formPanel, BorderLayout.CENTER);
        deletePanel.add(buttonPanel, BorderLayout.SOUTH);
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

    private void registerNewProduct() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            validateRegistrationFields(false);

            brandSystem.registerNewProduct(
                    categoryNameField.getText().trim(),
                    productNameField.getText().trim(),
                    Integer.parseInt(capacityField.getText().trim()),
                    new BigDecimal(priceUsdField.getText().trim()),
                    new BigDecimal(priceKrwField.getText().trim()),
                    Integer.parseInt(thresholdField.getText().trim())
            );

            JOptionPane.showMessageDialog(
                    this,
                    "신규 상품이 성공적으로 등록되었습니다.",
                    "등록 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetForm();

        } catch (NumberFormatException e) {
            showWarning("용량, 가격, 임계값은 숫자로 입력해주세요.");

        } catch (DutyFreeException e) {
            showWarning(e.getErrorCode().getMessage());

        } catch (Exception e) {
            showError("상품 등록 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void registerNewProductAndPurchase() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            validateRegistrationFields(true);

            brandSystem.registerNewProductAndPurchase(
                    categoryNameField.getText().trim(),
                    productNameField.getText().trim(),
                    Integer.parseInt(capacityField.getText().trim()),
                    new BigDecimal(priceUsdField.getText().trim()),
                    new BigDecimal(priceKrwField.getText().trim()),
                    Integer.parseInt(thresholdField.getText().trim()),
                    Integer.parseInt(purchaseAmountField.getText().trim())
            );

            JOptionPane.showMessageDialog(
                    this,
                    "상품 등록 및 발주 요청이 완료되었습니다.",
                    "처리 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetForm();

        } catch (NumberFormatException e) {
            showWarning("용량, 가격, 임계값, 발주 수량은 숫자로 입력해주세요.");

        } catch (DutyFreeException e) {
            showWarning(e.getErrorCode().getMessage());

        } catch (Exception e) {
            showError("상품 등록 및 발주 요청 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void deleteProduct() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            String productName = deleteProductNameField.getText().trim();

            if (productName.isEmpty()) {
                showWarning("삭제할 상품명을 입력하세요.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "'" + productName + "' 상품을 삭제하시겠습니까?",
                    "삭제 확인",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            brandSystem.deleteProduct(productName);

            JOptionPane.showMessageDialog(
                    this,
                    "상품 삭제가 완료되었습니다.",
                    "삭제 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetForm();

        } catch (DutyFreeException e) {
            showWarning(e.getErrorCode().getMessage());

        } catch (Exception e) {
            showError("상품 삭제 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void validateRegistrationFields(boolean requirePurchaseAmount) {
        if (isBlank(categoryNameField) ||
                isBlank(productNameField) ||
                isBlank(capacityField) ||
                isBlank(priceUsdField) ||
                isBlank(priceKrwField) ||
                isBlank(thresholdField)) {

            throw new IllegalArgumentException("신규 상품 등록 정보를 모두 입력해주세요.");
        }

        if (requirePurchaseAmount && isBlank(purchaseAmountField)) {
            throw new IllegalArgumentException("발주 수량을 입력해주세요.");
        }
    }

    private boolean isBlank(JTextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "알림", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);
    }

    private void resetForm() {
        categoryNameField.setText("");
        productNameField.setText("");
        capacityField.setText("");
        priceUsdField.setText("");
        priceKrwField.setText("");
        thresholdField.setText("");
        purchaseAmountField.setText("");
        deleteProductNameField.setText("");
    }

    @Override
    public void refresh() {
        resetForm();
    }
}