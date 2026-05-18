package brandSystem;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;

import exception.BusinessException;
import exception.ErrorCode;
import exception.ValidationException;
import product.Product;
import product.ProductService;
import stock.domain.StockPurchaseStatus;
import stock.dto.StockProductDto;
import stock.dto.StockPurchaseHistoryDto;
import stock.service.StockPurchaseService;
import stock.service.StockService;
import stock.observer.StockObserver;
import order.OrderService;
import order.dto.OrderDTO;
import product.dto.ProductDTO;

@Getter
public class BrandSystem implements StockObserver {

    private final String brandName;
    private final StockService stockService;
    private final StockPurchaseService purchaseService;
    private final ProductService productService;
    private final OrderService orderService;

    public BrandSystem(String brandName) {
        validateRequiredText(brandName);

        this.brandName = brandName;
        this.stockService = new StockService();
        this.purchaseService = new StockPurchaseService();
        this.productService = new ProductService();
        this.orderService = new OrderService();

        this.stockService.registerObserver(this);
    }

    public void receiveOrder(String productName, int amount) {
        validateMyBrandProduct(productName);
        stockService.deductStockFIFO(productName, amount);
    }

    public void makePurchase(String productName, int amount) {
        validateMyBrandProduct(productName);
        purchaseService.requestPurchase(productName, amount);
    }

    public void cancelPurchase(int purchaseId) {
        purchaseService.cancelPurchase(purchaseId);
    }

    public void receivePurchase(int purchaseId) {
        purchaseService.receivePurchase(purchaseId);
    }

    public void registerNewProduct(
            String categoryName,
            String productName,
            int capacity,
            BigDecimal priceUsd,
            BigDecimal priceKrw,
            int thresholdValue
    ) {
        productService.registerNewProduct(
                brandName,
                categoryName,
                productName,
                capacity,
                priceUsd,
                priceKrw,
                thresholdValue
        );
    }

    public void registerNewProductAndPurchase(
            String categoryName,
            String productName,
            int capacity,
            BigDecimal priceUsd,
            BigDecimal priceKrw,
            int thresholdValue,
            int purchaseAmount
    ) {
        productService.registerNewProduct(
                brandName,
                categoryName,
                productName,
                capacity,
                priceUsd,
                priceKrw,
                thresholdValue
        );

        purchaseService.requestPurchase(productName, purchaseAmount);
    }

    public void deleteProduct(String productName) {
        validateRequiredText(productName);
        productService.deleteProduct(brandName, productName);
    }

    public List<StockProductDto> getMyBrandStocks() {
        return stockService.getAllStockByBrandName(brandName);
    }
    
    public List<StockPurchaseHistoryDto> getMyBrandPurchaseHistory() {
        return purchaseService.getPurchaseHistoryDtoByBrandName(brandName);
    }
    
    public List<ProductDTO> getProductsByBrandName() {
        return productService.getProductsByBrandName(brandName);
    }

    public List<OrderDTO> getOrdersByBrandName() {
        return orderService.getOrdersByBrandName(brandName);
    }
    
    
    public void exportPurchaseHistoryToFile(File file) {
        purchaseService.exportPurchaseHistoryByBrandName(brandName, file);
    }

    public int getStockAmount(String productName) {
        validateMyBrandProduct(productName);
        return stockService.getTotalAmountByProductName(productName);
    }

//    public void printStockStatus(String productName) {
//        validateMyBrandProduct(productName);
//
//        int totalAmount = stockService.getTotalAmountByProductName(productName);
//
//        System.out.println("===== [" + brandName + "] 재고 조회 =====");
//        System.out.println("상품명: " + productName);
//        System.out.println("총 재고: " + totalAmount);
//    }
//
//    public void printMyBrandStocks() {
//        System.out.println("===== [" + brandName + "] 전체 재고 목록 =====");
//        stockService.printAllStockByBrandName(brandName);
//    }
//
//    public void printPurchaseHistory() {
//        System.out.println("===== [" + brandName + "] 브랜드 발주 이력 =====");
//
//        List<StockPurchaseHistoryDto> purchases =
//                purchaseService.getPurchaseHistoryDtoByBrandName(brandName);
//
//        printPurchaseHistory(purchases);
//    }
//
//    public void printPurchaseHistoryByProductName(String productName) {
//        validateMyBrandProduct(productName);
//
//        System.out.println("===== [" + brandName + "] 상품별 발주 이력 =====");
//        System.out.println("상품명: " + productName);
//
//        List<StockPurchaseHistoryDto> purchases =
//                purchaseService.getPurchaseHistoryByProductName(productName);
//
//        printPurchaseHistory(purchases);
//    }
//
//    public void printPurchaseHistoryByStatus(StockPurchaseStatus status) {
//        System.out.println("===== [" + brandName + "] 상태별 발주 이력 =====");
//        System.out.println("상태: " + status);
//
//        List<StockPurchaseHistoryDto> purchases =
//                purchaseService.getPurchaseHistoryByStatus(status);
//
//        printPurchaseHistory(purchases);
//    }
//
//    private void printPurchaseHistory(List<StockPurchaseHistoryDto> purchases) {
//        if (purchases == null || purchases.isEmpty()) {
//            System.out.println("조회된 발주 이력이 없습니다.");
//            return;
//        }
//
//        for (StockPurchaseHistoryDto purchase : purchases) {
//            System.out.println(purchase);
//        }
//    }
//    
//    public void printProductsByBrandName() {
//        List<Product> products = productService.getProductsByBrandName(brandName);
//
//        System.out.println("===== [" + brandName + "] 브랜드 상품 목록 =====");
//
//        for (Product product : products) {
//            System.out.println(product);
//        }
//    }
//
//    public void printOrdersByBrandName() {
//        List<OrderDTO> orders = orderService.getOrdersByBrandName(brandName);
//
//        System.out.println("===== [" + brandName + "] 브랜드 판매 내역 =====");
//
//        for (OrderDTO order : orders) {
//            System.out.println(order);
//        }
//    }


    @Override
    public void onStockShortageDetected(
            String brandName,
            String productName,
            int currentAmount,
            int thresholdValue
    ) {
        if (!this.brandName.equals(brandName)) {
            return;
        }

        System.out.println();
        System.out.println("====================================");
        System.out.println("[브랜드 시스템 재고 부족 알림]");
        System.out.println("브랜드명: " + brandName);
        System.out.println("상품명: " + productName);
        System.out.println("현재 총 재고: " + currentAmount);
        System.out.println("기준 수량: " + thresholdValue);
        System.out.println("재고가 기준 수량 이하입니다. 발주 검토가 필요합니다.");
        System.out.println("====================================");
        System.out.println();
    }

    private void validateMyBrandProduct(String productName) {
        validateRequiredText(productName);

        if (!stockService.isBrandProduct(brandName, productName)) {
            throw new BusinessException(
                    ErrorCode.NOT_MY_BRAND_PRODUCT,
                    new Exception("해당 브랜드의 상품이 아닙니다. 브랜드=" + brandName + ", 상품명=" + productName)
            );
        }
    }

    private void validateRequiredText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    }
}