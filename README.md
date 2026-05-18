# HD_DutyFlow

> 현대면세점 공항 픽업 예약 시스템

## 📌 프로젝트 소개

**HD_DutyFlow**는 현대면세점의 온라인 면세 상품 구매부터 공항 인도장 픽업까지의 흐름을 관리하는 Java 기반 시스템입니다.

회원은 상품 조회, 장바구니 담기, 주문, 결제, 픽업 예약까지 진행할 수 있으며, 브랜드 관리자는 본인 브랜드의 상품, 재고, 발주, 판매 내역을 관리할 수 있습니다.

본 프로젝트는 Java, Oracle DB, JDBC, Swing GUI, 디자인 패턴, Scheduler, Trigger 등을 활용하여 면세점 도메인의 주문/결제/픽업 흐름을 구현하는 것을 목표로 했습니다.

---

## 🛫 주요 기능

### 👤 회원 기능

- 회원가입 및 로그인
- 여권 정보 등록/갱신
- 상품 조회 및 상품 상세 조회
- 장바구니 담기
- 장바구니 수량 변경/삭제
- 카드번호 입력 후 주문 및 결제 처리
- 주문 내역 조회
- 주문 상세 조회
- 픽업 예약
- 환율 조회
- 회원 등급 조회

### 🏷️ 브랜드 관리자 기능

- 브랜드 관리자 로그인
- 내 브랜드 상품 목록 조회
- 내 브랜드 재고 조회
- 상품 등록 및 삭제
- 발주 요청 및 취소
- 발주 이력 조회
- 브랜드별 판매 내역 조회

### 🧾 주문 / 결제 / 픽업 기능

- 장바구니 기반 주문 생성
- 주문 Queue 처리
- 카드번호 Luhn 알고리즘 검증
- 결제 요청 처리
- 주문 상태 관리
- `PAID → PICKUP_RESERVED` 상태 변경 시 Pickup 자동 생성
- Oracle Trigger 기반 픽업 예약 row 자동 생성

---

## 🧩 시스템 흐름

```text
회원 로그인
→ 상품 조회
→ 장바구니 담기
→ 주문하기
→ 카드번호 입력
→ 결제 진행 중 로딩
→ 주문 생성
→ 주문 Queue 처리
→ 결제 처리
→ 주문 상태 PAID
→ 픽업 예약
→ 주문 상태 PICKUP_RESERVED
→ Pickup 데이터 자동 생성
```

---

## 🛠️ 기술 스택

### Language

- Java

### Database

- Oracle DB
- JDBC

### GUI

- Java Swing
- CardLayout
- JTable
- JDialog
- JProgressBar

### Architecture / Pattern

- DAO Pattern
- DTO Pattern
- Service Layer
- Facade Pattern
- State Pattern
- Scheduler
- Observer 개념 일부 활용
- Oracle Trigger

### Tools

- Eclipse
- SQL Developer
- Git / GitHub

---

## 🏗️ 아키텍처 구조

```text
GUI
 └── ScreenManager
      ├── DutyFlowSystem
      │    ├── MemberService
      │    ├── ProductService
      │    ├── ShoppingCartService
      │    ├── OrderService
      │    ├── PaymentService
      │    ├── FlightService
      │    └── ExchangeRateService
      │
      └── BrandSystem
           ├── ProductService
           ├── StockService
           ├── StockPurchaseService
           └── OrderService

Service
 └── DAO
      └── Oracle DB
```

---

## 🗂️ 주요 패키지 구조

```text
src
├── main
│   └── KooMain.java
│
├── gui
│   ├── ScreenManager.java
│   ├── auth
│   ├── member
│   ├── brand
│   ├── exchangerate
│   └── common
│
├── dutyFlowSystem
│   └── DutyFlowSystem.java
│
├── brandSystem
│   └── BrandSystem.java
│
├── member
├── product
├── shoppingCart
├── order
├── payment
├── pickup
├── stock
├── flight
├── exchangeRate
├── membership
├── regulation
├── exception
└── common
```

---

## 🧑‍💻 핵심 구현 내용

### 1. DutyFlowSystem

회원 기능의 중심 Facade 역할을 담당합니다.

GUI는 여러 Service를 직접 호출하지 않고 `DutyFlowSystem`을 통해 기능을 사용합니다.

주요 메서드:

```java
login()
logout()
getShoppingProducts()
getProductDetail()
addToCart()
printCart()
updateQuantity()
deleteFromCart()
makeOrder(String cardNumber)
processOrderQueue()
getMyOrders()
getOrderDetails()
reservePickup()
getTodayExchangeRate()
getMyGradeName()
```

---

### 2. BrandSystem

브랜드 관리자 기능의 중심 Facade 역할을 담당합니다.

브랜드 관리자는 로그인 후 본인 브랜드 기준으로 상품, 재고, 발주, 판매 내역을 조회할 수 있습니다.

주요 메서드:

```java
getMyBrandStocks()
getProductsByBrandName()
getOrdersByBrandName()
getPurchaseHistory()
```

---

### 3. Order State Pattern

주문 상태는 State Pattern을 활용하여 관리했습니다.

```text
PENDING
→ VERIFIED
→ PAID
→ PICKUP_RESERVED
→ PICKED_UP
```

취소 또는 미수령 상태도 관리합니다.

```text
CANCELED
NO_SHOW
```

상태별로 가능한 행위를 분리하여 잘못된 상태 전이를 방지했습니다.

---

### 4. Oracle Trigger

주문 상태가 `PAID`에서 `PICKUP_RESERVED`로 변경되면 `Pickup` 테이블에 row를 자동 생성합니다.

```sql
CREATE OR REPLACE TRIGGER trg_create_pickup_after_reserved
AFTER UPDATE OF orderState ON Orders
FOR EACH ROW
WHEN (
    OLD.orderState = 'PAID'
    AND NEW.orderState = 'PICKUP_RESERVED'
)
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM Pickup
    WHERE orderId = :NEW.orderId;

    IF v_count = 0 THEN
        INSERT INTO Pickup (
            pickupId,
            orderId,
            managerId,
            pickupAvailableAt,
            pickedUpAt
        )
        VALUES (
            pickup_seq.NEXTVAL,
            :NEW.orderId,
            NULL,
            SYSDATE,
            NULL
        );
    END IF;
END;
/
```
```
create or replace Trigger TRG_STOCK_AFTER_NO_SHOW
AFTER UPDATE ON Orders
FOR EACH ROW

DECLARE
    -- OrderDetail 순회용 커서
    CURSOR cur_order_detail IS
        SELECT
            productId,
            quantity
        FROM OrderDetail
        WHERE orderId = :NEW.orderId;

    -- 가장 오래된 재고 Row의 PK 저장용
    v_stock_id Stock.stockId%TYPE;

BEGIN
    /*
      상태 변화 조건:
      PICKUP_RESERVED -> NO_SHOW
    */
    IF :OLD.orderState = 'PICKUP_RESERVED'
       AND :NEW.orderState = 'NO_SHOW'
    THEN
        -- 주문 상세 반복
        FOR od IN cur_order_detail LOOP
            /*
              해당 상품(productId)의
              가장 오래된 manufacturedDate 재고 찾기
            */
            SELECT stockId
            INTO v_stock_id
            FROM (
                SELECT stockId
                FROM Stock
                WHERE productId = od.productId
                ORDER BY manufacturedDate ASC
            )
            WHERE ROWNUM = 1;

            /*
              재고 복구
            */
            UPDATE Stock
            SET amount = amount + od.quantity
            WHERE stockId = v_stock_id;
        END LOOP;
    END IF;
END;
```

---

## 🧾 ERD 주요 설계

주요 테이블은 다음과 같습니다.

```text
Member
Membership
Manager
ShopManager
AirportManager
Brand
Category
Product
Stock
StockPurchase
ShoppingCart
Orders
BuyDetail
Payment
Pickup
Flight
FlightBook
ExchangeRate
SystemLog
Regulation
```

### 설계 포인트

- `Orders`와 `BuyDetail`을 분리하여 1:N 주문 상세 구조 설계
- `Product`와 `Stock`을 분리하여 상품 정보와 재고 수량 관리 분리
- `Manager`를 기준으로 브랜드 관리자와 공항 관리자 역할 분리
- `Payment`와 `Orders` 상태를 분리하여 결제 상태와 주문 상태를 별도 관리
- `Pickup`은 주문 상태 변경에 따라 자동 생성되도록 설계

---

## 💳 결제 처리

카드번호는 `PaymentService`에서 검증합니다.

검증 기준:

- 숫자가 아닌 문자는 제거
- 16자리 숫자인지 확인
- Luhn 알고리즘 검증

테스트 카드번호 예시:

```text
4111-1111-1111-1111
```

---

## 📈 환율 조회

환율 조회 화면에서는 실제 환율 데이터를 기반으로 다음 정보를 제공합니다.

- 오늘 환율
- 최근 1주일 환율
- 최근 1개월 환율
- 최고 환율
- 최저 환율
- 평균 환율
- Swing 기반 선 그래프 차트

---


## 🚀 실행 방법

### 1. Oracle DB 실행

Oracle DB가 실행 중이어야 합니다.

### 2. DB 연결 정보 확인

`OracleConnection`에서 DB 연결 정보를 확인합니다.

```java
jdbc:oracle:thin:@localhost:1521/FREEPDB1
```

환경에 따라 service name은 다를 수 있습니다.

### 3. Java Application 실행

`main.KooMain`을 실행합니다.

```java
public class KooMain {
    public static void main(String[] args) {
        ...
    }
}
```

---

## 🔮 개선 예정 사항

- 주문/결제/재고 차감 트랜잭션 통합
- PaymentQueue 실제 GUI 연동
- 카드번호 토큰화 또는 마스킹 처리
- 비밀번호 암호화
- 상품 검색 및 페이징 처리
- 재고 차감 동시성 제어
- Pickup 담당 관리자 배정 기능 추가
- Swing GUI를 Web UI로 확장
- Spring 기반 구조로 전환

---

## 👨‍💻 프로젝트 회고

이번 프로젝트를 통해 단순 CRUD를 넘어 주문, 결제, 재고, 픽업, 환율 등 여러 도메인이 연결되는 흐름을 경험했습니다.

특히 DB 설계, DAO/Service 분리, Facade 구조, State Pattern, Oracle Trigger, Swing GUI 연동을 직접 구현하면서 전체 시스템 흐름을 이해할 수 있었습니다.

또한 문제 발생 시 Java 코드뿐 아니라 DB 제약조건, Trigger 상태, SQL alias, 세션 상태, 빌드 캐시까지 함께 확인하는 디버깅 관점을 기를 수 있었습니다.
