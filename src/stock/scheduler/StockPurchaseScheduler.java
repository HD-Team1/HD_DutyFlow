package stock.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import stock.service.StockPurchaseService;

public class StockPurchaseScheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("stock-purchase-auto-receive-daemon");
                return thread;
            });

    private final StockPurchaseService stockPurchaseService;

    public StockPurchaseScheduler(StockPurchaseService stockPurchaseService) {
        this.stockPurchaseService = stockPurchaseService;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                stockPurchaseService.completeReceivablePurchases();
            } catch (Exception e) {
                System.out.println("[자동 입고 처리 오류]");
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}