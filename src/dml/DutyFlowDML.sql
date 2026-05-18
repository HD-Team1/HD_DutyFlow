INSERT INTO Membership (grade, criteria, discountRate) VALUES ('SILVER','누적 구매 0달러 이상',0);
INSERT INTO Membership (grade, criteria, discountRate) VALUES ('GOLD','누적 구매 3000달러 이상',5);
INSERT INTO Membership (grade, criteria, discountRate) VALUES ('BLACK','누적 구매 10000달러 이상',10);
INSERT INTO Membership (grade, criteria, discountRate) VALUES ('PRESTIGE','누적 구매 30000달러 이상',15);

INSERT INTO Manager (managerId, managerName, managerType) VALUES (1,'김브랜드','BRAND');
INSERT INTO Manager (managerId, managerName, managerType) VALUES (100,'인도장담당자A','AIRPORT');

INSERT INTO ShopManager (managerId, annualLeaveCount) VALUES (1,15);
INSERT INTO AirportManager (managerId, shiftTime) VALUES (100,TO_DATE('2026-05-01 08:00:00','YYYY-MM-DD HH24:MI:SS'));

INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (1,NULL,'주류',1);
INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (2,1,'위스키',2);
INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (3,1,'와인',2);
INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (4,NULL,'화장품',1);
INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (5,NULL,'전자제품',1);

INSERT INTO Brand (brandId, brandName, managerId) VALUES (1,'Johnnie Walker',1);
INSERT INTO Brand (brandId, brandName, managerId) VALUES (2,'Ballantines',1);
INSERT INTO Brand (brandId, brandName, managerId) VALUES (3,'Chanel',1);
INSERT INTO Brand (brandId, brandName, managerId) VALUES (4,'Dior',1);
INSERT INTO Brand (brandId, brandName, managerId) VALUES (5,'IQOS',1);

INSERT INTO Product (productId, categoryId, brandId, productName, capacity, priceUsd, priceKrw, thresholdValue) VALUES (1,2,1,'조니워커 블루라벨',750,220,298000,10);
INSERT INTO Product (productId, categoryId, brandId, productName, capacity, priceUsd, priceKrw, thresholdValue) VALUES (2,2,2,'발렌타인 21년',700,180,243000,10);
INSERT INTO Product (productId, categoryId, brandId, productName, capacity, priceUsd, priceKrw, thresholdValue) VALUES (3,4,3,'샤넬 넘버5 오드퍼퓸',100,120,162000,10);
INSERT INTO Product (productId, categoryId, brandId, productName, capacity, priceUsd, priceKrw, thresholdValue) VALUES (4,4,4,'디올 립글로우',15,45,61000,10);
INSERT INTO Product (productId, categoryId, brandId, productName, capacity, priceUsd, priceKrw, thresholdValue) VALUES (5,5,5,'아이코스 일루마',1,95,128000,10);

INSERT INTO Event (productId, discountRate) VALUES (1,10);
INSERT INTO Event (productId, discountRate) VALUES (2,15);
INSERT INTO Event (productId, discountRate) VALUES (3,5);
INSERT INTO Event (productId, discountRate) VALUES (4,20);
INSERT INTO Event (productId, discountRate) VALUES (5,12);

INSERT INTO Stock (stockId, productId, manufacturedDate, amount) VALUES (1,1,SYSDATE-30,1000);

INSERT INTO Regulation (regulationId, categoryId, limitCapacity, establishedDate, overageRate) VALUES (1,1,800,SYSDATE,15);
INSERT INTO Regulation (regulationId, categoryId, limitCapacity, establishedDate, overageRate) VALUES (2,2,2,SYSDATE,70);
INSERT INTO Regulation (regulationId, categoryId, limitCapacity, establishedDate, overageRate) VALUES (3,4,100,SYSDATE,15);

INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-10','YYYY-MM-DD'),1466.7800,'N');
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-11','YYYY-MM-DD'),1472.9000,'N');
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-12','YYYY-MM-DD'),1488.6700,'N');
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-13','YYYY-MM-DD'),1488.5800,'Y');
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-14','YYYY-MM-DD'),1490.1200,'N');

INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (1,'GOLD','user01','pass01','김철수',TO_DATE('1995-03-12','YYYY-MM-DD'),'010-1111-1111',TO_DATE('2025-01-01','YYYY-MM-DD'),'Y','M12345678',TO_DATE('2030-05-01','YYYY-MM-DD'));
INSERT INTO Member (memberId, grade, loginId, password, name, birthDate, phoneNumber, gradeSelectionDate, adult, passportNumber, passportExpiryDate) VALUES (2,'BLACK','user02','pass02','이영희',TO_DATE('1988-07-21','YYYY-MM-DD'),'010-2222-2222',TO_DATE('2024-06-15','YYYY-MM-DD'),'Y','M87654321',TO_DATE('2031-09-15','YYYY-MM-DD'));

INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (1,'KE101',TO_DATE('2026-05-20 09:30','YYYY-MM-DD HH24:MI'),0);
INSERT INTO Flight (flightId, flightCode, departureAt, isDelayed) VALUES (2,'OZ202',TO_DATE('2026-05-21 14:10','YYYY-MM-DD HH24:MI'),1);

INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (1,1,1,'RESV-KE101-001');
INSERT INTO FlightBook (reservationId, memberId, flightId, reservationCode) VALUES (2,2,2,'RESV-OZ202-001');

INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (1,1,1,TO_DATE('2026-05-14','YYYY-MM-DD'),TO_DATE('2026-05-14 10:30','YYYY-MM-DD HH24:MI'),'ORDERED',378.00);
INSERT INTO Orders (orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) VALUES (2,2,2,TO_DATE('2026-05-14','YYYY-MM-DD'),TO_DATE('2026-05-14 11:00','YYYY-MM-DD HH24:MI'),'ORDERED',165.00);

INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (1,1,1,22.00,198.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (2,1,1,18.00,162.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (3,2,1,5.00,115.00);
INSERT INTO OrderDetail (productId, orderId, quantity, discountPrice, dollarPrice) VALUES (4,2,1,5.00,40.00);

INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason) VALUES (1,1,'CARD','SUCCESS',378.00,'1234-****-****-1111',SYSDATE,SYSDATE,NULL);
INSERT INTO Payment (paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt,failReason) VALUES (2,2,'CARD','SUCCESS',165.00,'1234-****-****-2222',SYSDATE,SYSDATE,NULL);

INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (1,1,100,TO_DATE('2026-05-20 07:00','YYYY-MM-DD HH24:MI'),NULL);
INSERT INTO Pickup (pickupId, orderId, managerId, pickupAvailableAt, pickedUpAt) VALUES (2,2,100,TO_DATE('2026-05-21 12:00','YYYY-MM-DD HH24:MI'),NULL);

COMMIT;