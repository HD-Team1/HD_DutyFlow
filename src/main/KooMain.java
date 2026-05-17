package main;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import brandSystem.BrandSystem;
import dutyFlowSystem.DutyFlowSystem;
import gui.MainFrame;
import stock.scheduler.StockCleanupScheduler;
import stock.scheduler.StockPurchaseScheduler;
import stock.service.StockPurchaseService;
import stock.service.StockService;

public class KooMain {

    public static void main(String[] args) {

        StockService stockService = new StockService();
        StockPurchaseService purchaseService = new StockPurchaseService();

        StockPurchaseScheduler purchaseScheduler =
                new StockPurchaseScheduler(purchaseService);

        StockCleanupScheduler cleanupScheduler =
                new StockCleanupScheduler(stockService);

        purchaseScheduler.start();
        cleanupScheduler.start();

        // Brand_01 ~ Brand_10 생성
        List<BrandSystem> brandSystems = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String brandName = String.format("Brand_%02d", i);
            brandSystems.add(new BrandSystem(brandName));
        }

        // DutyFlowSystem 생성
        DutyFlowSystem dutyFlowSystem = new DutyFlowSystem(brandSystems);

        // 결제 Worker / 환율 Scheduler 시작
        dutyFlowSystem.startPaymentWorker();
        dutyFlowSystem.startExchangeRateScheduler();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            purchaseScheduler.stop();
            cleanupScheduler.stop();

            dutyFlowSystem.stopPaymentWorker();
            dutyFlowSystem.stopExchangeRateScheduler();

            System.out.println("===== 스케줄러 종료 =====");
        }));

        SwingUtilities.invokeLater(() -> new MainFrame(dutyFlowSystem));
    }
}