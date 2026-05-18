-- =========================================================================
-- 1. 완벽한 쓰레기 청소 (자식 테이블부터 최상위 부모까지 역순으로 삭제)
-- =========================================================================
DELETE FROM Payment;
DELETE FROM OrderDetail;
DELETE FROM Pickup;
DELETE FROM Orders;
DELETE FROM FlightBook;
DELETE FROM Flight;
DELETE FROM ShoppingCart;
DELETE FROM StockPurchase;
DELETE FROM Stock;
DELETE FROM Event;
DELETE FROM Product;
DELETE FROM Brand;
DELETE FROM Category;
DELETE FROM Member;
DELETE FROM ExchangeRate;
DELETE FROM AirportManager;
DELETE FROM ShopManager;
DELETE FROM Manager;
DELETE FROM Membership;
DELETE FROM SystemLog;

-- =========================================================================
-- 2. 메타 데이터 세팅 (등급, 관리자, 카테고리, 브랜드, 상품, 환율)
-- =========================================================================

-- 멤버십
INSERT INTO Membership (grade, criteria, discountRate) VALUES ('SILVER', '기본', 0);
INSERT INTO Membership (grade, criteria, discountRate) VALUES ('GOLD', '골드', 5);
INSERT INTO Membership (grade, criteria, discountRate) VALUES ('BLACK', '블랙', 10);
INSERT INTO Membership (grade, criteria, discountRate) VALUES ('PRESTIGE', '프레스티지', 15);

-- 관리자 (브랜드관리자 1명, 인도장관리자 1명)
INSERT INTO Manager (managerId, managerName, managerType) VALUES (1, '김브랜드', 'BRAND');
INSERT INTO Manager (managerId, managerName, managerType) VALUES (100, '인도장담당자A', 'AIRPORT');
INSERT INTO ShopManager (managerId, annualLeaveCount) VALUES (1, 15);
INSERT INTO AirportManager (managerId, shiftTime) VALUES (100, TO_DATE('2026-05-01 08:00:00', 'YYYY-MM-DD HH24:MI:SS'));

-- 카테고리, 브랜드, 상품 (주문상세에 넣기 위해 최소 1개 상품 필요)
INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (1, NULL, '화장품', 1);
INSERT INTO Brand (brandId, brandName, managerId) VALUES (1, '테스트브랜드', 1);
INSERT INTO Product (productId, categoryId, brandId, productName, capacity, priceUsd, priceKrw, thresholdValue) VALUES (1, 1, 1, '더미 테스트 향수', 100, 10, 13000, 50);

-- 재고 및 이벤트 (참고용 더미 데이터)
INSERT INTO Stock (stockId, productId, manufacturedDate, amount) VALUES (1, 1, SYSDATE-30, 1000);
INSERT INTO Event (productId, discountRate) VALUES (1, 0);

-- 환율
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-04-30', 'YYYY-MM-DD'), 1350.5000, 'N');
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-01', 'YYYY-MM-DD'), 1348.2000, 'Y');


-- =========================================================================
-- 3. MLPQ 시뮬레이션용 데이터 10건 (회원 -> 비행기 -> 예약 -> 주문 -> 주문상세 -> 결제 -> 픽업)
-- =========================================================================

-- 1) 회원 10명
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (1, 'GOLD', 'user_lee', 'pass1234', '이급박', TO_DATE('1992-05-15', 'YYYY-MM-DD'), '010-1111-2222', SYSDATE-100, 'Y', 'M11111111', SYSDATE+1000);
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (2, 'SILVER', 'user_park', 'pass1234', '박지각', TO_DATE('1995-08-20', 'YYYY-MM-DD'), '010-2222-3333', SYSDATE-150, 'Y', 'M22222222', SYSDATE+1000);
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (3, 'SILVER', 'user_kim', 'pass1234', '김철용', TO_DATE('1988-11-11', 'YYYY-MM-DD'), '010-3333-4444', SYSDATE-200, 'Y', 'M33333333', SYSDATE+1000);
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (4, 'BLACK', 'user_oh', 'pass1234', '오블랙', TO_DATE('1985-03-01', 'YYYY-MM-DD'), '010-4444-5555', SYSDATE-50, 'Y', 'M44444444', SYSDATE+1000);
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (5, 'GOLD', 'user_choi', 'pass1234', '최골드', TO_DATE('1990-07-07', 'YYYY-MM-DD'), '010-5555-6666', SYSDATE-120, 'Y', 'M55555555', SYSDATE+1000);
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (6, 'SILVER', 'user_yoo', 'pass1234', '유실버', TO_DATE('1998-12-25', 'YYYY-MM-DD'), '010-6666-7777', SYSDATE-10, 'Y', 'M66666666', SYSDATE+1000);
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (7, 'BLACK', 'user_yak', 'pass1234', '약블랙', TO_DATE('1982-04-14', 'YYYY-MM-DD'), '010-7777-8888', SYSDATE-300, 'Y', 'M77777777', SYSDATE+1000);
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (8, 'BLACK', 'user_joong', 'pass1234', '중블랙', TO_DATE('1979-09-09', 'YYYY-MM-DD'), '010-8888-9999', SYSDATE-250, 'Y', 'M88888888', SYSDATE+1000);
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (9, 'PRESTIGE', 'user_kang', 'pass1234', '강부자', TO_DATE('1975-01-01', 'YYYY-MM-DD'), '010-9999-0000', SYSDATE-500, 'Y', 'M99999999', SYSDATE+1000);
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (10, 'SILVER', 'user_gu', 'pass1234', '구민', TO_DATE('2000-10-10', 'YYYY-MM-DD'), '010-0000-1111', SYSDATE-5, 'Y', 'M00000000', SYSDATE+1000);

-- 2) 비행기 10대
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (1, 'KE1025', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (25 / 1440), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (2, 'OZ1015', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (15 / 1440), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (3, '7C1050', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (50 / 1440), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (4, 'KE1090', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (90 / 1440), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (5, 'OZ1100', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (100 / 1440), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (6, 'LJ1080', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (80 / 1440), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (7, 'KE1120', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (120 / 1440), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (8, 'OZ1160', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (160 / 1440), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (9, 'KE1150', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (150 / 1440), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (10, 'TW1030', TO_DATE('2026-05-01 09:30:00', 'YYYY-MM-DD HH24:MI:SS') + (30 / 1440), 0);

-- 3) 예약 내역 10건
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (1, 1, 1, 'RES-A001');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (2, 2, 2, 'RES-A002');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (3, 3, 3, 'RES-A003');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (4, 4, 4, 'RES-A004');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (5, 5, 5, 'RES-A005');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (6, 6, 6, 'RES-A006');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (7, 7, 7, 'RES-A007');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (8, 8, 8, 'RES-A008');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (9, 9, 9, 'RES-A009');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (10, 10, 10, 'RES-A010');

-- 4) 주문 내역 10건
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (1, 1, 1, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 150.00);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (2, 2, 2, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:05:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 85.50);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (3, 3, 3, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:10:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 210.00);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (4, 4, 4, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:15:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 55.00);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (5, 5, 5, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:20:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 320.00);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (6, 6, 6, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:25:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 12.50);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (7, 7, 7, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 450.00);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (8, 8, 8, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:35:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 89.00);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (9, 9, 9, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:40:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 1250.00);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (10, 10, 10, TO_DATE('2026-04-30', 'YYYY-MM-DD'), TO_DATE('2026-04-30 15:45:00', 'YYYY-MM-DD HH24:MI:SS'), 'PICKUP_RESERVED', 75.00);

-- 5) 주문 상세 내역 10건 (새로 추가됨!)
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 1, 1, 0, 150.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 2, 1, 0, 85.50);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 3, 1, 0, 210.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 4, 1, 0, 55.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 5, 1, 0, 320.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 6, 1, 0, 12.50);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 7, 1, 0, 450.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 8, 1, 0, 89.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 9, 1, 0, 1250.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1, 10, 1, 0, 75.00);

-- 6) 결제 내역 10건 (새로 추가됨!)
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (1, 1, 'CARD', 'SUCCESS', 150.00, '1234-****-****-1111', TO_DATE('2026-04-30 15:01:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:01:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (2, 2, 'CARD', 'SUCCESS', 85.50, '1234-****-****-2222', TO_DATE('2026-04-30 15:06:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:06:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (3, 3, 'CARD', 'SUCCESS', 210.00, '1234-****-****-3333', TO_DATE('2026-04-30 15:11:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:11:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (4, 4, 'CARD', 'SUCCESS', 55.00, '1234-****-****-4444', TO_DATE('2026-04-30 15:16:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:16:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (5, 5, 'CARD', 'SUCCESS', 320.00, '1234-****-****-5555', TO_DATE('2026-04-30 15:21:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:21:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (6, 6, 'CARD', 'SUCCESS', 12.50, '1234-****-****-6666', TO_DATE('2026-04-30 15:26:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:26:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (7, 7, 'CARD', 'SUCCESS', 450.00, '1234-****-****-7777', TO_DATE('2026-04-30 15:31:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:31:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (8, 8, 'CARD', 'SUCCESS', 89.00, '1234-****-****-8888', TO_DATE('2026-04-30 15:36:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:36:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (9, 9, 'CARD', 'SUCCESS', 1250.00, '1234-****-****-9999', TO_DATE('2026-04-30 15:41:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:41:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (10, 10, 'CARD', 'SUCCESS', 75.00, '1234-****-****-0000', TO_DATE('2026-04-30 15:46:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2026-04-30 15:46:05', 'YYYY-MM-DD HH24:MI:SS'), NULL);

-- 7) 픽업예약 내역 10건
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (1, 1, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (2, 2, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (3, 3, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (4, 4, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (5, 5, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (6, 6, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (7, 7, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (8, 8, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (9, 9, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (10, 10, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);

COMMIT;

-- 지연 시나리오용
-- 시나리오 4용 추가 임박 데이터
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) 
VALUES (11, 'SILVER', 'user_soon1', 'pass1234', '임박1', SYSDATE-10000, '010-1212-1212', SYSDATE-10, 'Y', 'M11110001', SYSDATE+1000);

INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) 
VALUES (12, 'GOLD', 'user_soon2', 'pass1234', '임박2', SYSDATE-10000, '010-1313-1313', SYSDATE-10, 'Y', 'M11110002', SYSDATE+1000);

-- 비행기 (현재 09:45 기준 약 20~25분 남은 비행기)
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (11, 'KE_SOON1', TO_DATE('2026-05-01 10:05:00', 'YYYY-MM-DD HH24:MI:SS'), 0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (12, 'KE_SOON2', TO_DATE('2026-05-01 10:10:00', 'YYYY-MM-DD HH24:MI:SS'), 0);

INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (11, 11, 11, 'RES-SOON-1');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (12, 12, 12, 'RES-SOON-2');

INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) 
VALUES (11, 11, 11, TO_DATE('2026-04-30', 'YYYY-MM-DD'), SYSDATE, 'PICKUP_RESERVED', 100.00);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) 
VALUES (12, 12, 12, TO_DATE('2026-04-30', 'YYYY-MM-DD'), SYSDATE, 'PICKUP_RESERVED', 100.00);

INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt) VALUES (11, 11, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt) VALUES (12, 12, 100, TO_DATE('2026-05-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

COMMIT;

-----------------



-- Category (일반상품=1, 주류=2, 향수=3)
INSERT INTO Category VALUES (category_seq.NEXTVAL, NULL, '일반상품', 1);
INSERT INTO Category VALUES (category_seq.NEXTVAL, NULL, '주류', 1);
INSERT INTO Category VALUES (category_seq.NEXTVAL, NULL, '향수', 1);

-- Regulation (rate는 정수로 저장: 15 = 15%)
-- 일반상품: 800달러 한도, 15% 초과세율
INSERT INTO Regulation VALUES (regulation_seq.NEXTVAL, 1, 800, SYSDATE, 15);
-- 주류: 2리터 한도, 70% 초과세율
INSERT INTO Regulation VALUES (regulation_seq.NEXTVAL, 2, 2, SYSDATE, 70);
-- 향수: 100ml 한도, 15% 초과세율
INSERT INTO Regulation VALUES (regulation_seq.NEXTVAL, 3, 100, SYSDATE, 15);

COMMIT;

