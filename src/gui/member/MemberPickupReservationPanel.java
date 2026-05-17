package gui.member;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import order.dto.OrderDTO;

public class MemberPickupReservationPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JLabel orderIdLabel;
    private JLabel flightLabel;
    private JLabel departureTimeLabel;
    private JLabel pickupAvailableTimeLabel;
    private JLabel guideLabel;
    private JLabel statusLabel;

    private Integer currentOrderId;
    private String currentOrderState = "-";

    private static final Color BG_COLOR = new Color(245, 246, 250);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color SECONDARY_COLOR = new Color(149, 165, 166);
    private static final Color TITLE_COLOR = new Color(45, 52, 71);

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MemberPickupReservationPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("픽업 예약", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));

        add(titleLabel, BorderLayout.NORTH);

        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createCenterPanel() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 25, 0));
        wrapper.setOpaque(false);

        JPanel infoPanel = createCardPanel("예약 정보");
        JPanel guidePanel = createCardPanel("안내 및 상태");

        JPanel infoContent = new JPanel(new GridLayout(4, 2, 10, 20));
        infoContent.setOpaque(false);

        orderIdLabel = createValueLabel();
        flightLabel = createValueLabel();
        departureTimeLabel = createValueLabel();
        pickupAvailableTimeLabel = createValueLabel();

        addInfoRow(infoContent, "주문번호", orderIdLabel);
        addInfoRow(infoContent, "항공편", flightLabel);
        addInfoRow(infoContent, "출국시간", departureTimeLabel);
        addInfoRow(infoContent, "픽업 가능 시간", pickupAvailableTimeLabel);

        infoPanel.add(infoContent, BorderLayout.CENTER);

        JPanel guideContent = new JPanel(new GridLayout(3, 1, 10, 20));
        guideContent.setOpaque(false);

        guideLabel = new JLabel(
                "<html><div style='font-size:14px;'>"
                        + "출국 2시간 전부터 픽업 가능합니다.<br>"
                        + "픽업 예약 완료 시 주문 상태가 변경됩니다."
                        + "</div></html>"
        );

        guideLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 15));

        statusLabel = new JLabel("상태 변경: PAID → PICKUP_RESERVED", SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(235, 247, 239));
        statusLabel.setForeground(new Color(39, 174, 96));

        JLabel noticeLabel = new JLabel("※ 실제 예약 로직은 DutyFlowSystem.reservePickup(orderId)와 연결됩니다.", SwingConstants.CENTER);
        noticeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        noticeLabel.setForeground(Color.GRAY);

        guideContent.add(guideLabel);
        guideContent.add(statusLabel);
        guideContent.add(noticeLabel);

        guidePanel.add(guideContent, BorderLayout.CENTER);

        wrapper.add(infoPanel);
        wrapper.add(guidePanel);

        return wrapper;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20));
        bottomPanel.setOpaque(false);

        JButton reserveButton = createButton("픽업 예약", SUCCESS_COLOR);
        JButton backButton = createButton("뒤로가기", SECONDARY_COLOR);

        reserveButton.addActionListener(e -> reservePickup());
        backButton.addActionListener(e -> screenManager.show("MEMBER_ORDER_DETAIL"));

        bottomPanel.add(reserveButton);
        bottomPanel.add(backButton);

        return bottomPanel;
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(215, 215, 215)),
                        title,
                        0,
                        0,
                        new Font("맑은 고딕", Font.BOLD, 18),
                        TITLE_COLOR
                ),
                new EmptyBorder(25, 25, 25, 25)
        ));

        return panel;
    }

    private void addInfoRow(JPanel panel, String title, JLabel valueLabel) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));

        panel.add(titleLabel);
        panel.add(valueLabel);
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        return label;
    }

    private void loadReservationInfo() {
        try {
            currentOrderId = screenManager.getSelectedOrderId();

            if (currentOrderId == null) {
                JOptionPane.showMessageDialog(this, "선택된 주문이 없습니다.");
                screenManager.show("MEMBER_ORDER_HISTORY");
                return;
            }

            List<OrderDTO> orderDetails =
                    screenManager.getDutyFlowSystem().getOrderDetails(currentOrderId);

            if (orderDetails != null && !orderDetails.isEmpty()) {
                currentOrderState = orderDetails.get(0).getOrderState();
            } else {
                currentOrderState = "-";
            }

            LocalDateTime departureTime = LocalDateTime.now().plusHours(5);
            LocalDateTime pickupAvailableTime = departureTime.minusHours(2);

            orderIdLabel.setText(String.valueOf(currentOrderId));
            flightLabel.setText("KE903 / ICN → CDG");
            departureTimeLabel.setText(departureTime.format(formatter));
            pickupAvailableTimeLabel.setText(pickupAvailableTime.format(formatter));

            statusLabel.setText("현재 상태: " + currentOrderState + " → PICKUP_RESERVED");

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
                    "픽업 예약 정보를 불러오는 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void reservePickup() {
        try {
            if (currentOrderId == null) {
                JOptionPane.showMessageDialog(this, "픽업 예약할 주문이 없습니다.");
                return;
            }

            screenManager.getDutyFlowSystem().reservePickup(currentOrderId);

            JOptionPane.showMessageDialog(
                    this,
                    "픽업 예약이 완료되었습니다.\n주문 상태가 PICKUP_RESERVED로 변경되었습니다.",
                    "픽업 예약 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            screenManager.show("MEMBER_ORDER_HISTORY");

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
                    "픽업 예약 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(130, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    public void refresh() {
        loadReservationInfo();
    }
}