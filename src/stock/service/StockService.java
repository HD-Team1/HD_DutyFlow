package stock.service;

import java.util.ArrayList;
import java.util.List;

import exception.BusinessException;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;
import exception.ValidationException;
import stock.dao.StockDao;
import stock.domain.Stock;
import stock.dto.StockProductDto;
import stock.observer.StockObserver;

public class StockService {

    private final StockDao stockDao = new StockDao();
    private final List<StockObserver> listObservers = new ArrayList<>();

    public void registerObserver(StockObserver observer) {
        if (observer == null) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }

        listObservers.add(observer);
    }

    public void deleteObserver(StockObserver observer) {
        if (observer == null) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }

        listObservers.remove(observer);
    }

    private void notifyObservers(
            String brandName,
            String productName,
            int currentAmount,
            int thresholdValue
    ) {
        for (StockObserver observer : listObservers) {
            observer.onStockShortageDetected(
                    brandName,
                    productName,
                    currentAmount,
                    thresholdValue
            );
        }
    }

    public int getTotalAmountByProductName(String productName) throws SystemException {
        validateRequiredText(productName);
        return stockDao.getTotalAmountByProductName(productName);
    }

    public int getTotalAmountByProductId(int productId) throws SystemException {
        validatePositiveId(productId);
        return stockDao.getTotalAmountByProductId(productId);
    }

    public boolean canSell(String productName, int amount) throws SystemException {
        validateRequiredText(productName);
        validateAmount(amount);

        int totalAmount = stockDao.getTotalAmountByProductName(productName);
        return totalAmount >= amount;
    }

    public boolean canSell(int productId, int amount) throws SystemException {
        validatePositiveId(productId);
        validateAmount(amount);

        int totalAmount = stockDao.getTotalAmountByProductId(productId);
        return totalAmount >= amount;
    }

    public void deductStockFIFO(String productName, int orderAmount) throws SystemException {
        validateRequiredText(productName);
        validateAmount(orderAmount);

        int productId = stockDao.findProductIdByProductName(productName);

        List<Stock> stockList = stockDao.findByProductIdOrderByManufacturedDate(productId);

        if (stockList == null || stockList.isEmpty()) {
            throw new DataNotFoundException(
                    ErrorCode.STOCK_NOT_FOUND,
                    new Exception("재고 정보를 찾을 수 없습니다. 상품명: " + productName)
            );
        }

        int totalAmount = stockDao.getTotalAmountByProductId(productId);

        if (totalAmount < orderAmount) {
            throw new BusinessException(
                    ErrorCode.STOCK_NOT_ENOUGH,
                    new Exception("재고가 부족합니다. 상품명: " + productName
                            + ", 현재 재고: " + totalAmount
                            + ", 요청 수량: " + orderAmount)
            );
        }

        int remainAmount = orderAmount;

        for (Stock stock : stockList) {
            if (remainAmount == 0) {
                break;
            }

            int currentAmount = stock.getAmount();

            if (currentAmount >= remainAmount) {
                int newAmount = currentAmount - remainAmount;
                updateStockAmountSafely(stock.getStockId(), newAmount);
                remainAmount = 0;
            } else {
                updateStockAmountSafely(stock.getStockId(), 0);
                remainAmount -= currentAmount;
            }
        }

        checkThreshold(productName);
    }

    public void checkThreshold(String productName) throws SystemException {
        validateRequiredText(productName);

        int productId = stockDao.findProductIdByProductName(productName);

        int currentAmount = stockDao.getTotalAmountByProductId(productId);
        int thresholdValue = stockDao.getThresholdValueByProductId(productId);
        String brandName = stockDao.getBrandNameByProductId(productId);

        if (currentAmount <= thresholdValue) {
            notifyObservers(brandName, productName, currentAmount, thresholdValue);
        }
    }

    public int deleteEmptyStocks() throws SystemException {
        return stockDao.deleteZeroAmountStocks();
    }

    public boolean isBrandProduct(String brandName, String productName) throws SystemException {
        validateRequiredText(brandName);
        validateRequiredText(productName);

        String actualBrandName = stockDao.getBrandNameByProductName(productName);
        return brandName.equals(actualBrandName);
    }

    public void printAllStockByBrandName(String brandName) throws SystemException {
        validateRequiredText(brandName);

        stockDao.getAllStockByBrandName(brandName)
                .forEach(System.out::println);
    }

    public List<StockProductDto> getAllStockByBrandName(String brandName) throws SystemException {
        validateRequiredText(brandName);

        List<StockProductDto> stocks = stockDao.getAllStockByBrandName(brandName);

        if (stocks == null || stocks.isEmpty()) {
            throw new DataNotFoundException(
                    ErrorCode.STOCK_NOT_FOUND,
                    new Exception("조회된 재고가 없습니다. 브랜드명: " + brandName)
            );
        }

        return stocks;
    }

    private void updateStockAmountSafely(int stockId, int newAmount) throws SystemException {
        int result = stockDao.updateAmount(stockId, newAmount);

        if (result != 1) {
            throw new SystemException(
                    ErrorCode.STOCK_UPDATE_FAILED,
                    new Exception("재고 수정에 실패했습니다. stockId=" + stockId)
            );
        }
    }

    private void validateRequiredText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new ValidationException(ErrorCode.INVALID_STOCK_AMOUNT);
        }
    }

    private void validatePositiveId(int id) {
        if (id <= 0) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    }
}