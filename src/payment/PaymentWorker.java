package payment;

//백그라운드에서 실행되는 결제 처리 Worker
public class PaymentWorker implements Runnable { 

    private final PaymentQueue paymentQueue = PaymentQueue.getInstance();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    
    private boolean running = true;

    // Queue에 쌓인 결제 요청 1건을 꺼내서 처리
    public void processOne() {
         // Queue에 결제 요청이 남아있는 동안 반복 처리
        int paymentId = paymentQueue.dequeue();
        
        System.out.println("[WORKER] 결제 처리 시작 | paymentId = " + paymentId);

        Payment payment = paymentDAO.findById(paymentId);

        if (payment == null) {
            System.out.println("[WORKER] 결제 정보 없음 | paymentId = " + paymentId);
            paymentDAO.updateStatusToFailed(paymentId, "PAYMENT_NOT_FOUND");
            return;
        }

        paymentDAO.updateStatusToProcessing(paymentId);
        Payment processingPayment = paymentDAO.findById(paymentId);
        
        System.out.println("[PAYMENT] 상태 변경 완료 | paymentId = " + paymentId + " | currentStatus = " + processingPayment.getPaymentStatus());
        
        try {
            // 카드번호 검증은 requestPayment 단계에서 이미 완료됨
            // Worker에서는 실제 결제 승인 처리만 Mock으로 수행
            paymentDAO.updateStatusToSuccess(paymentId);
            Payment successPayment = paymentDAO.findById(paymentId);
            System.out.println("[PAYMENT] 상태 변경 완료 | paymentId = " + paymentId + " | currentStatus = " + successPayment.getPaymentStatus());

            // 추후 OrderStatus 로직과 연동 예정
        } catch (Exception e) {
            paymentDAO.updateStatusToFailed(paymentId, "PAYMENT_PROCESSING_ERROR");
            System.out.println("결제 실패: paymentId = " + paymentId);
            
            Payment failedPayment = paymentDAO.findById(paymentId);
            System.out.println("[PAYMENT] 상태 변경 완료 | paymentId = " + paymentId + " | currentStatus = " + failedPayment.getPaymentStatus());
        }
        
        System.out.println();
    }

	@Override
	public void run() {
        while (running) {
            if (!paymentQueue.isEmpty()) {
                processOne();
            } else {
                sleep();
            }
        }
	}
	
    // Queue가 비어 있을 때 CPU 과점유 방지
	// 1초 대기 후 다시 Queue 확인
    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            running = false;
            Thread.currentThread().interrupt();
        }
    }
    
    // Worker 종료
    public void stop() {
        running = false;
    }
}