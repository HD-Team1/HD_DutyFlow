package stock.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import stock.service.StockService;

public class StockCleanupScheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("stock-cleanup-daemon");
                return thread;
            });

    private final StockService stockService;

    public StockCleanupScheduler(StockService stockService) {
        this.stockService = stockService;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int deletedCount = stockService.deleteEmptyStocks();

                if (deletedCount > 0) {
                    System.out.println("[재고 정리 완료] amount가 0인 재고 "
                            + deletedCount
                            + "건 삭제");
                }

            } catch (Exception e) {
                System.out.println("[재고 정리 스케줄러 오류]");
                e.printStackTrace();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}