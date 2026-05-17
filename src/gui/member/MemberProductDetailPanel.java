package gui.member;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import product.dto.ProductDTO;

public class MemberProductDetailPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JLabel nameLabel;
    private JLabel brandLabel;
    private JLabel categoryLabel;
    private JLabel capacityLabel;
    private JLabel priceLabel;
    private JLabel eventLabel;
    private JLabel descriptionLabel;
    private JSpinner quantitySpinner;

    private static final Color BG_COLOR = new Color(245, 246, 250);
    private static final Color PRIMARY_COLOR = new Color(45, 108, 223);

    public MemberProductDetailPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("상품 상세 정보", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));

        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(25, 0));
        centerPanel.setOpaque(false);

        JLabel imageLabel = new JLabel("상품 이미지", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(235, 237, 240));
        imageLabel.setPreferredSize(new Dimension(320, 0));

        JPanel infoPanel = new JPanel(new GridLayout(7, 2, 10, 15));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        nameLabel = new JLabel();
        brandLabel = new JLabel();
        categoryLabel = new JLabel();
        capacityLabel = new JLabel();
        priceLabel = new JLabel();
        eventLabel = new JLabel();
        descriptionLabel = new JLabel();

        addInfoRow(infoPanel, "상품명", nameLabel);
        addInfoRow(infoPanel, "브랜드", brandLabel);
        addInfoRow(infoPanel, "카테고리", categoryLabel);
        addInfoRow(infoPanel, "용량", capacityLabel);
        addInfoRow(infoPanel, "최종 가격", priceLabel);
        addInfoRow(infoPanel, "행사 여부", eventLabel);
        addInfoRow(infoPanel, "상품 설명", descriptionLabel);

        centerPanel.add(imageLabel, BorderLayout.WEST);
        centerPanel.add(infoPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20));
        bottomPanel.setOpaque(false);

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

        JButton addCartButton = createStyledButton("장바구니 담기", PRIMARY_COLOR);
        JButton cartButton = createStyledButton("장바구니 이동", new Color(46, 204, 113));
        JButton backButton = createStyledButton("목록으로", new Color(149, 165, 166));

        addCartButton.addActionListener(e -> addToCart());
        cartButton.addActionListener(e -> screenManager.show("MEMBER_CART"));
        backButton.addActionListener(e -> screenManager.show("MEMBER_PRODUCT_LIST"));

        bottomPanel.add(new JLabel("수량"));
        bottomPanel.add(quantitySpinner);
        bottomPanel.add(addCartButton);
        bottomPanel.add(cartButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addInfoRow(JPanel panel, String title, JLabel valueLabel) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));

        valueLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 15));

        panel.add(titleLabel);
        panel.add(valueLabel);
    }

    private void loadProduct() {
        try {
            ProductDTO selectedProduct = screenManager.getSelectedProduct();

            if (selectedProduct == null) {
                JOptionPane.showMessageDialog(this, "선택된 상품이 없습니다.");
                screenManager.show("MEMBER_PRODUCT_LIST");
                return;
            }

            ProductDTO product = screenManager.getDutyFlowSystem()
                    .getProductDetail(selectedProduct.getProductId());

            nameLabel.setText(product.getProductName());
            brandLabel.setText(product.getBrandName());
            categoryLabel.setText(
                    product.getCategory() != null
                            ? product.getCategory().getCategoryName()
                            : "-"
            );
            capacityLabel.setText(product.getCapacity() + "ml");
            priceLabel.setText(
                    "$" + product.getFinalPriceUsd()
                            + " / "
                            + product.getFinalPriceKrw()
                            + "원"
            );
            eventLabel.setText(product.isHasEvent()
                    ? "행사중 (" + product.getDiscountRate() + "% 할인)"
                    : "일반 상품"
            );
            descriptionLabel.setText(
                    "<html>"
                            + product.getProductName()
                            + "은(는) "
                            + product.getBrandName()
                            + " 브랜드의 면세 상품입니다."
                            + "</html>"
            );

            screenManager.setSelectedProduct(product);
            quantitySpinner.setValue(1);

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
                    "상품 상세 정보를 불러오는 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void addToCart() {
        try {
            ProductDTO product = screenManager.getSelectedProduct();

            if (product == null) {
                JOptionPane.showMessageDialog(this, "선택된 상품이 없습니다.");
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

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(130, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    public void refresh() {
        loadProduct();
    }
}