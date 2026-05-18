package gui.fakedata;


import java.math.BigDecimal;

public class FakeCartItem {

    private final FakeProduct product;
    private int quantity;

    public FakeCartItem(FakeProduct product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public FakeProduct getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    public BigDecimal getTotalUsd() {
        return product.getFinalPriceUsd().multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getTotalKrw() {
        return product.getFinalPriceKrw().multiply(BigDecimal.valueOf(quantity));
    }
}