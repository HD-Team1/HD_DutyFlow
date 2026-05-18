package gui.fakedata;


import java.math.BigDecimal;

public class FakeProduct {

    private final int productId;
    private final String productName;
    private final String brandName;
    private final String categoryName;
    private final int capacity;
    private final BigDecimal priceUsd;
    private final BigDecimal priceKrw;
    private final BigDecimal discountRate;
    private final int stockAmount;
    private final boolean hasEvent;
    private final String description;

    public FakeProduct(
            int productId,
            String productName,
            String brandName,
            String categoryName,
            int capacity,
            BigDecimal priceUsd,
            BigDecimal priceKrw,
            BigDecimal discountRate,
            int stockAmount,
            boolean hasEvent,
            String description
    ) {
        this.productId = productId;
        this.productName = productName;
        this.brandName = brandName;
        this.categoryName = categoryName;
        this.capacity = capacity;
        this.priceUsd = priceUsd;
        this.priceKrw = priceKrw;
        this.discountRate = discountRate;
        this.stockAmount = stockAmount;
        this.hasEvent = hasEvent;
        this.description = description;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getCapacity() {
        return capacity;
    }

    public BigDecimal getPriceUsd() {
        return priceUsd;
    }

    public BigDecimal getPriceKrw() {
        return priceKrw;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public int getStockAmount() {
        return stockAmount;
    }

    public boolean isHasEvent() {
        return hasEvent;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getFinalPriceUsd() {
        return priceUsd.subtract(
                priceUsd.multiply(discountRate).divide(BigDecimal.valueOf(100))
        );
    }

    public BigDecimal getFinalPriceKrw() {
        return priceKrw.subtract(
                priceKrw.multiply(discountRate).divide(BigDecimal.valueOf(100))
        );
    }

    public String getStockStatus() {
        if (stockAmount <= 0) {
            return "품절";
        }

        if (hasEvent) {
            return "행사중";
        }

        return "판매중";
    }
}
