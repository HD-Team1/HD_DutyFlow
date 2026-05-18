package exchangeRate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import exception.SystemException;

// 매일 자정마다 환율을 자동 갱신하는 스케줄러
public class ExchangeRateScheduler {

    // 단일 스레드 기반 스케줄러 생성
    // daemon thread로 설정하여 프로그램 종료 시 함께 종료되도록 함
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("exchange-rate-update-daemon");
                return thread;
            });

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateScheduler(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    public void start() {
    	// 프로그램 시작 시 최신 환율 정보를 Provider에 초기화
        exchangeRateService.initializeExchangeRate();

        // 다음 자정까지 남은 시간 계산
        long initialDelay = calculateInitialDelayUntilMidnight();

        // 매일 자정마다 환율 자동 갱신 실행
        scheduler.scheduleAtFixedRate(() -> {
            try {
                exchangeRateService.updateDailyExchangeRate();
            } catch (Exception e) {
                System.out.println("[환율 자동 갱신 오류] " + e.getMessage());
            }
        }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown(); // 스케줄러 종료
    }

    // 현재 시각 기준 다음 자정까지 남은 시간을 초 단위로 계산
    // 프로그램 시작 시간이 아니라 매일 자정 기준으로 실행하기 위해 
    // 남은 시간만큼 대기시킨 후 스케줄러를 실행시킴, 이후 24시간마다 반복
    private long calculateInitialDelayUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight =
                now.toLocalDate().plusDays(1).atStartOfDay();

        return Duration.between(now, nextMidnight).getSeconds();
    }
}