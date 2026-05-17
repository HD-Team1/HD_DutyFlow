package gui.member;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import exception.DutyFreeException;
import exchangeRate.ExchangeRate;
import gui.ScreenManager;
import gui.common.Refreshable;
import order.dto.OrderDTO;

public class MemberMainPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JLabel exchangeRateLabel;
    private JLabel gradeLabel;
    private JLabel recentOrderLabel;
    private JLabel exchangeSummaryLabel;
    private JLabel pickupCountLabel;

    private static final Color BG_COLOR = new Color(245, 246, 250);
    private static final Color PRIMARY_COLOR = new Color(45, 108, 223);
    private static final Color TITLE_COLOR = new Color(45, 52, 71);

    public MemberMainPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(25, 35, 25, 35));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createLeftMenu(), BorderLayout.WEST);
        add(createDashboardPanel(), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("회원 면세 쇼핑 시스템");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        titleLabel.setForeground(TITLE_COLOR);

        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        infoPanel.setOpaque(false);

        exchangeRateLabel = createBadge("현재 환율 조회 중...", PRIMARY_COLOR);
        gradeLabel = createBadge("회원 등급 조회 중...", new Color(149, 165, 166));

        infoPanel.add(exchangeRateLabel);
        infoPanel.add(gradeLabel);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(infoPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createLeftMenu() {
        JPanel menuPanel = new JPanel(new GridLayout(7, 1, 0, 15));
        menuPanel.setOpaque(false);
        menuPanel.setPreferredSize(new Dimension(220, 0));
        menuPanel.setBorder(new EmptyBorder(10, 0, 10, 25));

        JButton passportButton = createMenuButton("① 여권 등록/갱신");
        JButton productButton = createMenuButton("② 상품 조회");
        JButton cartButton = createMenuButton("③ 장바구니");
        JButton orderButton = createMenuButton("④ 주문 내역 조회");
        JButton exchangeButton = createMenuButton("⑤ 환율 조회");
        JButton paymentQueueButton = createMenuButton("⑥ 결제 Queue 시연");
        JButton logoutButton = createMenuButton("⑦ 로그아웃");

        passportButton.addActionListener(e -> screenManager.show("MEMBER_PASSPORT"));
        productButton.addActionListener(e -> screenManager.show("MEMBER_PRODUCT_LIST"));
        cartButton.addActionListener(e -> screenManager.show("MEMBER_CART"));
        orderButton.addActionListener(e -> screenManager.show("MEMBER_ORDER_HISTORY"));
        exchangeButton.addActionListener(e -> screenManager.show("MEMBER_EXCHANGE_RATE"));
        paymentQueueButton.addActionListener(e -> screenManager.show("MEMBER_PAYMENT_QUEUE"));

        logoutButton.addActionListener(e -> {
            screenManager.clearMemberSession();
            screenManager.show("LOGIN_SELECT");
        });

        menuPanel.add(passportButton);
        menuPanel.add(productButton);
        menuPanel.add(cartButton);
        menuPanel.add(orderButton);
        menuPanel.add(exchangeButton);
        menuPanel.add(paymentQueueButton);
        menuPanel.add(logoutButton);

        return menuPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new GridLayout(3, 1, 0, 20));
        dashboardPanel.setOpaque(false);

        recentOrderLabel = createDashboardCard("최근 주문 내역", "최근 주문 내역이 없습니다.");
        exchangeSummaryLabel = createDashboardCard("최근 환율 변동", "환율 정보를 조회 중입니다.");
        pickupCountLabel = createDashboardCard("픽업 예정 건수", "픽업 예정: 0건");

        dashboardPanel.add(recentOrderLabel);
        dashboardPanel.add(exchangeSummaryLabel);
        dashboardPanel.add(pickupCountLabel);

        return dashboardPanel;
    }

    private void loadDashboardData() {
        try {
            if (screenManager.getDutyFlowSystem() == null) {
                exchangeRateLabel.setText("회원 시스템 미연결");
                gradeLabel.setText("회원 등급: -");
                return;
            }

            loadExchangeRate();
            loadMemberGrade();
            loadRecentOrders();
            loadExchangeSummary();

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getErrorCode().getMessage(),
                    "알림",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "회원 메인 정보를 불러오는 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void loadExchangeRate() {
        BigDecimal todayRate =
                screenManager.getDutyFlowSystem().getTodayExchangeRate();

        exchangeRateLabel.setText("현재 환율 1 USD = " + todayRate + " KRW");
    }

    private void loadMemberGrade() {
        String gradeName =
                screenManager.getDutyFlowSystem().getMyGradeName();

        gradeLabel.setText("회원 등급: " + gradeName);
        gradeLabel.setBackground(getGradeColor(gradeName));
    }

    private void loadRecentOrders() {
        List<OrderDTO> orders =
                screenManager.getDutyFlowSystem().getMyOrders();

        if (orders == null || orders.isEmpty()) {
            recentOrderLabel.setText(createDashboardText("최근 주문 내역", "최근 주문 내역이 없습니다."));
            pickupCountLabel.setText(createDashboardText("픽업 예정 건수", "픽업 예정: 0건"));
            return;
        }

        OrderDTO recentOrder = orders.get(0);

        recentOrderLabel.setText(
                createDashboardText(
                        "최근 주문 내역",
                        "주문번호: " + recentOrder.getOrderId()
                                + " / 상태: " + recentOrder.getOrderState()
                                + " / 금액: " + recentOrder.getTotalAmount()
                )
        );

        int pickupCount = 0;

        for (OrderDTO order : orders) {
            if ("PICKUP_RESERVED".equals(order.getOrderState())) {
                pickupCount++;
            }
        }

        pickupCountLabel.setText(
                createDashboardText(
                        "픽업 예정 건수",
                        "픽업 예정: " + pickupCount + "건"
                )
        );
    }

    private void loadExchangeSummary() {
        List<ExchangeRate> weeklyRates =
                screenManager.getDutyFlowSystem().getWeeklyExchangeRates();

        if (weeklyRates == null || weeklyRates.isEmpty()) {
            exchangeSummaryLabel.setText(
                    createDashboardText("최근 환율 변동", "최근 환율 데이터가 없습니다.")
            );
            return;
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = weeklyRates.get(0).getExchangeRate();
        BigDecimal min = weeklyRates.get(0).getExchangeRate();

        for (ExchangeRate rate : weeklyRates) {
            BigDecimal value = rate.getExchangeRate();

            sum = sum.add(value);

            if (value.compareTo(max) > 0) {
                max = value;
            }

            if (value.compareTo(min) < 0) {
                min = value;
            }
        }

        BigDecimal avg = sum.divide(
                BigDecimal.valueOf(weeklyRates.size()),
                2,
                BigDecimal.ROUND_HALF_UP
        );

        exchangeSummaryLabel.setText(
                createDashboardText(
                        "최근 환율 변동",
                        "1주일 평균: " + avg
                                + " KRW / 최고: " + max
                                + " / 최저: " + min
                )
        );
    }

    private Color getGradeColor(String gradeName) {
        if ("SILVER".equalsIgnoreCase(gradeName)) {
            return new Color(149, 165, 166);
        }

        if ("GOLD".equalsIgnoreCase(gradeName)) {
            return new Color(243, 156, 18);
        }

        if ("BLACK".equalsIgnoreCase(gradeName)) {
            return new Color(45, 52, 54);
        }

        if ("PRESTIGE".equalsIgnoreCase(gradeName)) {
            return new Color(142, 68, 173);
        }

        return new Color(149, 165, 166);
    }

    private JLabel createDashboardCard(String title, String content) {
        JLabel label = new JLabel(createDashboardText(title, content));

        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(20, 25, 20, 25)
        ));

        return label;
    }

    private String createDashboardText(String title, String content) {
        return "<html><div style='font-size:14px; font-weight:bold;'>"
                + title
                + "</div><br><div style='font-size:13px;'>"
                + content
                + "</div></html>";
    }

    private JLabel createBadge(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);

        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        label.setBorder(new EmptyBorder(8, 15, 8, 15));

        return label;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        button.setBackground(Color.WHITE);
        button.setForeground(TITLE_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    public void refresh() {
        loadDashboardData();
    }
}