package payment;

import java.util.LinkedList;
import java.util.Queue;

import exception.ErrorCode;
import exception.QueueException;

public class PaymentQueue {

	// 결제 요청을 하나의 공용 Queue에서 관리하기 위해 싱글톤으로 구현
	// PaymentService가 enqueue, PaymentWorker가 dequeue할 때 동일한 Queue를 공유해야 하기 때문
	private static final PaymentQueue INSTANCE = new PaymentQueue();

	// Queue 대기열 - paymentId만 저장
	// FIFO로 처리하기 위해 LinkedList로 구현
    private final Queue<Integer> queue = new LinkedList<>();

    private PaymentQueue() {}

    public static PaymentQueue getInstance() {
        return INSTANCE;
    }

    // 결제 요청을 Queue의 맨 뒤에 추가
    public void enqueue(int paymentId) {
        queue.offer(paymentId);
        System.out.println("[QUEUE] enqueue 완료 | paymentId = " + paymentId + " | queueSize = " + queue.size());
    }

    // Queue의 맨 앞에 있는 결제 요청을 제거하면서 paymentId 반환
    public int dequeue() {
        if (queue.isEmpty()) { // Queue가 비어있는 경우 에러
            throw new QueueException(ErrorCode.EMPTY_PAYMENT_QUEUE);
        }

        int paymentId = queue.poll();

        System.out.println("[QUEUE] dequeue 완료 | paymentId = " + paymentId + " | remainingQueueSize = " + queue.size());
        
        return paymentId;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}