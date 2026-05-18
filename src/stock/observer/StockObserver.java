package stock.observer;

public interface StockObserver {
    void onStockShortageDetected(
            String brandName,
            String productName,
            int currentAmount,
            int thresholdValue
    );
}