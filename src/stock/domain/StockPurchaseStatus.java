package stock.domain;

public enum StockPurchaseStatus {
    REQUESTED,  // 발주 요청 -> 취소 가능
    RECEIVED,   // 입고 완료 -> 취소 불가능
    CANCELED    // 발주 취소
}
