package gui;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import brandSystem.BrandSystem;
import dutyFlowSystem.DutyFlowSystem;
import gui.common.Refreshable;
import lombok.Getter;
import lombok.Setter;
import product.dto.ProductDTO;

@Getter
@Setter
public class ScreenManager {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Map<String, JPanel> screens = new HashMap<>();

    // 회원가입 직후 여권정보 입력 화면으로 보낼지 여부
    private boolean passportAfterSignup;

    // 브랜드 관리자 로그인 세션
    private BrandSystem brandSystem;

    // 회원 기능 Facade
    private DutyFlowSystem dutyFlowSystem;

    // 회원 로그인 상태
    private Integer loginMemberId;

    // 회원 상품 상세 화면용 선택 상품
    private ProductDTO selectedProduct;

    // 주문 상세 / 픽업 예약 / 주문 취소 화면용 선택 주문 ID
    private Integer selectedOrderId;

    public ScreenManager(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
    }

    public void addScreen(String name, JPanel panel) {
        screens.put(name, panel);
        mainPanel.add(panel, name);
    }

    public void show(String name) {
        JPanel panel = screens.get(name);

        if (panel == null) {
            System.out.println("[ScreenManager] 등록되지 않은 화면입니다: " + name);
            return;
        }

        if (panel instanceof Refreshable) {
            ((Refreshable) panel).refresh();
        }

        cardLayout.show(mainPanel, name);
    }

    public void clearBrandSystem() {
        this.brandSystem = null;
    }

    public void clearLoginMemberId() {
        this.loginMemberId = null;
    }

    public boolean isMemberLoggedIn() {
        return loginMemberId != null;
    }

    public void clearSelectedProduct() {
        this.selectedProduct = null;
    }

    public void clearSelectedOrderId() {
        this.selectedOrderId = null;
    }

    public void clearMemberSession() {
        this.loginMemberId = null;
        this.selectedProduct = null;
        this.selectedOrderId = null;
        this.passportAfterSignup = false;

        if (this.dutyFlowSystem != null) {
            this.dutyFlowSystem.logout();
        }
    }

    public void clearBrandSession() {
        this.brandSystem = null;
    }

    public void clearAllSession() {
        clearMemberSession();
        clearBrandSession();
    }
}