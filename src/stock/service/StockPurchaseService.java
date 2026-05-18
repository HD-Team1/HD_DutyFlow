package stock.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import exception.BusinessException;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;
import exception.ValidationException;
import stock.dao.StockDao;
import stock.dao.StockPurchaseDao;
import stock.domain.Stock;
import stock.domain.StockPurchase;
import stock.domain.StockPurchaseStatus;
import stock.dto.StockPurchaseHistoryDto;

public class StockPurchaseService {

    private final StockPurchaseDao stockPurchaseDao = new StockPurchaseDao();
    private final StockDao stockDao = new StockDao();

    public void requestPurchase(String productName, int amount) throws SystemException {
        validateRequiredText(productName);
        validateAmount(amount);

        int result = stockPurchaseDao.insertPurchaseByProductName(productName, amount);

        if (result != 1) {
            throw new DataNotFoundException(
                    ErrorCode.PRODUCT_NOT_FOUND,
                    new Exception("발주 요청 실패. 존재하지 않는 상품명일 수 있습니다. 상품명: " + productName)
            );
        }
    }

    public void cancelPurchase(int purchaseId) throws SystemException {
        validatePositiveId(purchaseId);

        StockPurchase purchase = stockPurchaseDao.findById(purchaseId);

        if (purchase == null) {
            throw new DataNotFoundException(
                    ErrorCode.PURCHASE_NOT_FOUND,
                    new Exception("존재하지 않는 발주입니다. purchaseId=" + purchaseId)
            );
        }

        if (purchase.getStatus() != StockPurchaseStatus.REQUESTED) {
            throw new BusinessException(
                    ErrorCode.PURCHASE_CANCEL_FAILED,
                    new Exception("REQUESTED 상태의 발주만 취소할 수 있습니다. 현재 상태: " + purchase.getStatus())
            );
        }

        int result = stockPurchaseDao.cancelPurchase(purchaseId);

        if (result != 1) {
            throw new BusinessException(
                    ErrorCode.PURCHASE_CANCEL_FAILED,
                    new Exception("이미 입고 완료되었거나 취소된 발주입니다. purchaseId=" + purchaseId)
            );
        }
    }

    public void receivePurchase(int purchaseId) throws SystemException {
        validatePositiveId(purchaseId);

        StockPurchase purchase = stockPurchaseDao.findById(purchaseId);

        if (purchase == null) {
            throw new DataNotFoundException(
                    ErrorCode.PURCHASE_NOT_FOUND,
                    new Exception("존재하지 않는 발주입니다. purchaseId=" + purchaseId)
            );
        }

        if (purchase.getStatus() != StockPurchaseStatus.REQUESTED) {
            throw new BusinessException(
                    ErrorCode.PURCHASE_RECEIVE_FAILED,
                    new Exception("REQUESTED 상태의 발주만 입고 처리할 수 있습니다. 현재 상태: " + purchase.getStatus())
            );
        }

        Stock stock = Stock.builder()
                .productId(purchase.getProductId())
                .manufacturedDate(LocalDate.now())
                .amount(purchase.getAmount())
                .build();

        int insertResult = stockDao.insertStock(stock);

        if (insertResult != 1) {
            throw new SystemException(
                    ErrorCode.STOCK_UPDATE_FAILED,
                    new Exception("발주 입고 중 재고 추가에 실패했습니다. purchaseId=" + purchaseId)
            );
        }

        int updateResult = stockPurchaseDao.markAsReceived(purchaseId);

        if (updateResult != 1) {
            throw new BusinessException(
                    ErrorCode.PURCHASE_RECEIVE_FAILED,
                    new Exception("발주 상태를 RECEIVED로 변경하지 못했습니다. purchaseId=" + purchaseId)
            );
        }
    }

    public void completeReceivablePurchases() throws SystemException {
        List<StockPurchase> purchases = stockPurchaseDao.findReceivablePurchases();

        for (StockPurchase purchase : purchases) {
            receivePurchase(purchase.getPurchaseId());
        }
    }

    public List<StockPurchase> getAllPurchaseHistory() throws SystemException {
        return stockPurchaseDao.findAll();
    }

    public List<StockPurchaseHistoryDto> getPurchaseHistoryByProductName(String productName) throws SystemException {
        validateRequiredText(productName);
        return stockPurchaseDao.findByProductName(productName);
    }

    public List<StockPurchaseHistoryDto> getPurchaseHistoryByStatus(StockPurchaseStatus status) throws SystemException {
        if (status == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_PURCHASE_STATUS,
                    new Exception("발주 상태는 null일 수 없습니다.")
            );
        }

        return stockPurchaseDao.findByStatus(status);
    }

    public StockPurchase getPurchaseById(int purchaseId) throws SystemException {
        validatePositiveId(purchaseId);

        StockPurchase purchase = stockPurchaseDao.findById(purchaseId);

        if (purchase == null) {
            throw new DataNotFoundException(
                    ErrorCode.PURCHASE_NOT_FOUND,
                    new Exception("존재하지 않는 발주입니다. purchaseId=" + purchaseId)
            );
        }

        return purchase;
    }

    public List<StockPurchaseHistoryDto> getRequestedPurchases() throws SystemException {
        return stockPurchaseDao.findRequestedPurchases();
    }

    public List<StockPurchaseHistoryDto> getReceivedPurchases() throws SystemException {
        return stockPurchaseDao.findReceivedPurchases();
    }

    public List<StockPurchaseHistoryDto> getPurchaseHistoryDtoByBrandName(String brandName) {
        validateRequiredText(brandName);
        return stockPurchaseDao.findStockPurchasesHistoryByBrandName(brandName);
    }

    public void exportPurchaseHistoryByBrandName(String brandName, File file) {
        validateRequiredText(brandName);

        if (file == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_INPUT,
                    new Exception("저장할 파일이 선택되지 않았습니다.")
            );
        }

        List<StockPurchaseHistoryDto> purchases =
                getPurchaseHistoryDtoByBrandName(brandName);

        try (
                BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            bw.write('\uFEFF');

            bw.write("purchaseId,productId,productName,brandName,categoryName,priceUsd,priceKrw,thresholdValue,purchaseDate,amount,status");
            bw.newLine();

            for (StockPurchaseHistoryDto purchase : purchases) {
                bw.write(toCsvLine(
                        String.valueOf(purchase.getPurchaseId()),
                        String.valueOf(purchase.getProductId()),
                        purchase.getProductName(),
                        purchase.getBrandName(),
                        purchase.getCategoryName(),
                        String.valueOf(purchase.getPriceUsd()),
                        String.valueOf(purchase.getPriceKrw()),
                        String.valueOf(purchase.getThresholdValue()),
                        String.valueOf(purchase.getPurchaseDate()),
                        String.valueOf(purchase.getAmount()),
                        String.valueOf(purchase.getStatus())
                ));
                bw.newLine();
            }

        } catch (IOException e) {
            throw new SystemException(ErrorCode.FILE_SAVE_FAILED, e);
        }
    }

    private void validateRequiredText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new ValidationException(ErrorCode.INVALID_PURCHASE_AMOUNT);
        }
    }

    private void validatePositiveId(int id) {
        if (id <= 0) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    }

    private String toCsvLine(String... values) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            sb.append(escapeCsv(values[i]));

            if (i < values.length - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}