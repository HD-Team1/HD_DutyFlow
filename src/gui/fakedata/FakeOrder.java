package gui.fakedata;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FakeOrder {

    private final int orderId;
    private final LocalDateTime orderedAt;
    private final List<FakeCartItem> items;
    private final BigDecimal totalUsd;
    private final BigDecimal totalKrw;
    private String status;

    public FakeOrder(
            int orderId,
            LocalDateTime orderedAt,
            List<FakeCartItem> items,
            BigDecimal totalUsd,
            BigDecimal totalKrw,
            String status
    ) {
        this.orderId = orderId;
        this.orderedAt = orderedAt;
        this.items = items;
        this.totalUsd = totalUsd;
        this.totalKrw = totalKrw;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public List<FakeCartItem> getItems() {
        return items;
    }

    public BigDecimal getTotalUsd() {
        return totalUsd;
    }

    public BigDecimal getTotalKrw() {
        return totalKrw;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
