package exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	INVALID_INPUT("INVALID_INPUT", "잘못된 입력입니다."), 
	DATA_NOT_FOUND("DATA_NOT_FOUND", "데이터를 찾을 수 없습니다."),
	DUPLICATE_DATA("DUPLICATE_DATA", "이미 존재하는 데이터입니다."), 
	UNAUTHORIZED("UNAUTHORIZED", "로그인이 필요합니다."),
	ACCESS_DENIED("ACCESS_DENIED", "접근 권한이 없습니다."), 
	ILLEGAL_STATE("ILLEGAL_STATE", "시스템 검증 과정에 오류가 존재합니다."),
	// 이전에서 검증되었어야 할 조건을 만족하지 못했을 때

	// 장바구니
	INVALID_QUANTITY("INVALID_QUANTITY", "수량은 1개 이상이어야 합니다."),
	PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품 정보를 찾을 수 없습니다."),
	CART_PRODUCT_NOT_FOUND("CART_PRODUCT_NOT_FOUND", "장바구니에 존재하지 않는 상품입니다."),
	INVALID_PRODUCT_PRICE("INVALID_PRODUCT_PRICE", "상품 가격 정보가 올바르지 않습니다."),
	EMPTY_SELECTED_PRODUCTS("EMPTY_SELECTED_PRODUCTS", "선택된 상품이 없습니다."),

	// ORDER
	ORDER_INVALID_STATE("ORDER_INVALID_STATE", "현재 주문 상태에서는 수행할 수 없는 작업입니다."),
	ORDER_PAYMENT_FAILED("ORDER_PAYMENT_FAILED", "결제 처리 중 오류가 발생했습니다."),
	ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다"),
	NOT_FOUNT_BRAND("NOT_FOUNT_BRAND", "해당 브랜드 시스템이 존재하지 않습니다."),
 	
	
	// DB연결
	DB_CONNECTION("CONNECTION_ERROR", "DB 연결 실패"), 
	DB_DRIVER_NOT_FOUND("DRIVER_NOT_FOUND", "JDBC 드라이버를 찾을 수 없습니다"),

	// 규정
	DUTY_FREE_LIQUOR_EXCEEDED("DUTY_FREE_LIQUOR_EXCEEDED", "주류 면세 한도(용량/금액) 초과"),
	DUTY_FREE_PERFUME_EXCEEDED("DUTY_FREE_PERFUME_EXCEEDED", "향수 면세 한도(용량) 초과"),
	DUTY_FREE_TOTAL_LIMIT_EXCEEDED("DUTY_FREE_TOTAL_LIMIT_EXCEEDED", "전체 기본 면세 범위($800) 초과"),

	PAYMENT_FAILED("PAYMENT_FAILED", "주문 결제 실패"), 

	DENIED_PAY("DENIED_PAY", "결제 불가 상태"), 
	DENIED_CANCEL("DENIED_CANCEL", "취소 불가 상태"),
	DENIED_PICKUP("DENIED_PICKUP", "픽업 불가 상태"),

	// 결제EXCHANGE_RATE_API_FAILED
	INVALID_PAYMENT_REQUEST("INVALID_PAYMENT_REQUEST", "결제 요청 정보가 올바르지 않습니다."),
	INVALID_PAYMENT_AMOUNT("INVALID_PAYMENT_AMOUNT", "결제 금액이 올바르지 않습니다."),
	INVALID_CARD_NUMBER("INVALID_CARD_NUMBER", "카드번호 형식이 올바르지 않습니다."),
	EMPTY_PAYMENT_QUEUE("EMPTY_PAYMENT_QUEUE", "결제 대기열이 비어 있습니다."),
	PAYMENT_REQUEST_FAILED("PAYMENT_REQUEST_FAILED", "결제 요청 처리 중 오류가 발생했습니다."),
	INVALID_PAYMENT_STATUS("INVALID_PAYMENT_STATUS", "결제 상태가 올바르지 않습니다."),
	PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다."),
	PAYMENT_CANCEL_FAILED("PAYMENT_CANCEL_FAILED", "결제 취소 처리 중 오류가 발생했습니다."),
  
	// 멤버십
	MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
	INVALID_PURCHASE_AMOUNT("INVALID_PURCHASE_AMOUNT", "구매 금액이 올바르지 않습니다."),
	INVALID_MEMBERSHIP_GRADE("INVALID_MEMBERSHIP_GRADE", "회원 등급 정보가 올바르지 않습니다."),
	MEMBERSHIP_NOT_FOUND("MEMBERSHIP_NOT_FOUND", "멤버십 정보를 찾을 수 없습니다."),
	
 	// 인도장
 	QUEUE_EMPTY("QUEUE_EMPTY", "대기 중인 고객이 없습니다."),
 	NO_SHOW("NO_SHOW", "출국 시간이 경과하여 노쇼(No-Show) 처리된 예약입니다."),

 	// 인도장관리자
 	ALREADY_LOGGED_IN("ALREADY_LOGGED_IN", "이미 로그인된 상태입니다."),
 	NOT_LOGGED_IN("NOT_LOGGED_IN", "로그인 후 이용 가능합니다."),
 	INVALID_CREDENTIAL("INVALID_CREDENTIAL", "아이디 또는 비밀번호가 올바르지 않습니다."),

 	// 환율 외부 API
 	EXCHANGE_RATE_API_FAILED("EXCHANGE_RATE_API_FAILED", "외부 환율 API 호출에 실패했습니다."),
  
	// 재고
	STOCK_NOT_ENOUGH("S001", "재고가 부족합니다."),
	INVALID_STOCK_AMOUNT("S002", "재고 수량은 1개 이상이어야 합니다."),
	STOCK_NOT_FOUND("S003", "재고 정보를 찾을 수 없습니다."),
	STOCK_UPDATE_FAILED("S004", "재고 수정에 실패했습니다."),
	
	// 발주
	PURCHASE_NOT_FOUND("P001", "발주 정보를 찾을 수 없습니다."),
	INVALID_PURCHASE_STATUS("P003", "현재 상태에서는 발주를 처리할 수 없습니다."),
	PURCHASE_REQUEST_FAILED("P004", "발주 요청에 실패했습니다."),
	PURCHASE_CANCEL_FAILED("P005", "발주 취소에 실패했습니다."),
	PURCHASE_RECEIVE_FAILED("P006", "발주 입고 처리에 실패했습니다."),
	
	// 상품
	PRODUCT_ALREADY_EXISTS("PR002", "이미 등록된 상품입니다."),
	NOT_MY_BRAND_PRODUCT("PR003", "해당 브랜드의 상품이 아닙니다."),
	INVALID_PRODUCT_INPUT("PR004", "상품 입력값이 올바르지 않습니다."),
	// 파일
	FILE_SAVE_FAILED("FILE_SAVE_FAILED", "파일 저장 중 오류가 발생했습니다."),
	
	// 회원
	INVALID_LOGIN_ID("INVALID_LOGIN_ID", "아이디 입력값이 올바르지 않습니다."),
	INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호 입력값이 올바르지 않습니다."),
	INVALID_MEMBER_NAME("INVALID_MEMBER_NAME", "회원 이름 입력값이 올바르지 않습니다."),
	INVALID_BIRTH_DATE("INVALID_BIRTH_DATE", "생년월일 입력값이 올바르지 않습니다."),
	INVALID_PHONE_NUMBER("INVALID_PHONE_NUMBER", "전화번호 입력값이 올바르지 않습니다."),
	
	DUPLICATE_LOGIN_ID("DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다."),
	DUPLICATE_PHONE_NUMBER("DUPLICATE_PHONE_NUMBER", "이미 등록된 전화번호입니다."),	
	
	// 여권
	EMPTY_PASSPORT_NUMBER("EMPTY_PASSPORT_NUMBER", "여권번호를 입력해주세요."),
	INVALID_PASSPORT_FORMAT("INVALID_PASSPORT_FORMAT", "여권번호 형식이 올바르지 않습니다."),
	EMPTY_PASSPORT_EXPIRY_DATE("EMPTY_PASSPORT_EXPIRY_DATE", "여권 만료일을 입력해주세요."),
	EXPIRED_PASSPORT("EXPIRED_PASSPORT", "만료된 여권입니다."),
	DUPLICATE_PASSPORT("DUPLICATE_PASSPORT", "이미 등록된 여권번호입니다."),
	
	// 로그인
	INVALID_LOGIN_INPUT("INVALID_LOGIN_INPUT", "아이디와 비밀번호를 입력해주세요."),
	INVALID_LOGIN_CREDENTIAL("INVALID_LOGIN_CREDENTIAL", "아이디 또는 비밀번호가 올바르지 않습니다."),
	;
	
    private final String code;
    private final String message;
    
}