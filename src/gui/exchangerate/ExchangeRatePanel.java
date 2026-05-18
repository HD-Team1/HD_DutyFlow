package gui.exchangerate;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import exception.DutyFreeException;
import exchangeRate.ExchangeRate;
import gui.ScreenManager;
import gui.common.Refreshable;

public class ExchangeRatePanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JLabel todayLabel;

    private ExchangeRateChartPanel weeklyChartPanel;
    private ExchangeRateChartPanel monthlyChartPanel;

    private JLabel weeklySummaryLabel;
    private JLabel monthlySummaryLabel;

    private static final Color BG_COLOR = new Color(245, 246, 250);
    private static final Color PRIMARY_COLOR = new Color(45, 108, 223);
    private static final Color TITLE_COLOR = new Color(45, 52, 71);

    public ExchangeRatePanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("환율 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("오늘 환율", createTodayPanel());
        tabbedPane.addTab("최근 1주일", createWeeklyPanel());
        tabbedPane.addTab("최근 1개월", createMonthlyPanel());

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        bottomPanel.setOpaque(false);

        JButton refreshButton = createButton("새로고침", PRIMARY_COLOR);
        JButton backButton = createButton("돌아가기", new Color(149, 165, 166));

        refreshButton.addActionListener(e -> loadExchangeRates());
        backButton.addActionListener(e -> screenManager.show("MEMBER_MAIN"));

        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTodayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        todayLabel = new JLabel("오늘 환율 조회 중...", SwingConstants.CENTER);
        todayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        todayLabel.setForeground(TITLE_COLOR);

        JLabel guideLabel = new JLabel(
                "기준: 1 USD 대비 KRW 환율",
                SwingConstants.CENTER
        );
        guideLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        guideLabel.setForeground(Color.GRAY);

        panel.add(todayLabel, BorderLayout.CENTER);
        panel.add(guideLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createWeeklyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("최근 1주일 환율 변화", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        weeklyChartPanel = new ExchangeRateChartPanel();
        weeklySummaryLabel = createSummaryLabel();

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(weeklyChartPanel, BorderLayout.CENTER);
        panel.add(weeklySummaryLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMonthlyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("최근 1개월 환율 변화", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        monthlyChartPanel = new ExchangeRateChartPanel();
        monthlySummaryLabel = createSummaryLabel();

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(monthlyChartPanel, BorderLayout.CENTER);
        panel.add(monthlySummaryLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createSummaryLabel() {
        JLabel label = new JLabel("환율 데이터 조회 중...", SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        label.setBorder(new EmptyBorder(15, 0, 0, 0));
        return label;
    }

    private void loadExchangeRates() {
        try {
            if (screenManager.getDutyFlowSystem() == null) {
                todayLabel.setText("회원 시스템이 연결되지 않았습니다.");
                weeklySummaryLabel.setText("회원 시스템이 연결되지 않았습니다.");
                monthlySummaryLabel.setText("회원 시스템이 연결되지 않았습니다.");
                return;
            }

            loadTodayRate();
            loadWeeklyRates();
            loadMonthlyRates();

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
                    "환율 정보를 불러오는 중 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void loadTodayRate() {
        BigDecimal todayRate =
                screenManager.getDutyFlowSystem().getTodayExchangeRate();

        if (todayRate == null) {
            todayLabel.setText("오늘 환율 데이터가 없습니다.");
            return;
        }

        todayLabel.setText("오늘 환율: 1 USD = " + formatRate(todayRate) + " KRW");
    }

    private void loadWeeklyRates() {
        List<ExchangeRate> weeklyRates =
                screenManager.getDutyFlowSystem().getWeeklyExchangeRates();

        List<ChartPoint> points = convertToChartPoints(weeklyRates);

        weeklyChartPanel.setPoints(points);
        weeklySummaryLabel.setText(createSummaryText(points));
    }

    private void loadMonthlyRates() {
        List<ExchangeRate> monthlyRates =
                screenManager.getDutyFlowSystem().getMonthlyExchangeRates();

        List<ChartPoint> points = convertToChartPoints(monthlyRates);

        monthlyChartPanel.setPoints(points);
        monthlySummaryLabel.setText(createSummaryText(points));
    }

    private List<ChartPoint> convertToChartPoints(List<ExchangeRate> rates) {
        List<ChartPoint> points = new ArrayList<>();

        if (rates == null || rates.isEmpty()) {
            return points;
        }

        rates.sort((r1, r2) -> {
            if (r1.getExchangeDate() == null && r2.getExchangeDate() == null) {
                return 0;
            }
            if (r1.getExchangeDate() == null) {
                return 1;
            }
            if (r2.getExchangeDate() == null) {
                return -1;
            }

            return r1.getExchangeDate().compareTo(r2.getExchangeDate());
        });

        for (ExchangeRate rate : rates) {
            if (rate == null || rate.getExchangeRate() == null) {
                continue;
            }

            String label = String.valueOf(rate.getExchangeDate());
            BigDecimal value = rate.getExchangeRate();

            points.add(new ChartPoint(label, value));
        }

        return points;
    }

    private String createSummaryText(List<ChartPoint> points) {
        if (points == null || points.isEmpty()) {
            return "환율 데이터가 없습니다.";
        }

        BigDecimal max = points.get(0).value;
        BigDecimal min = points.get(0).value;
        BigDecimal sum = BigDecimal.ZERO;

        for (ChartPoint point : points) {
            BigDecimal value = point.value;

            sum = sum.add(value);

            if (value.compareTo(max) > 0) {
                max = value;
            }

            if (value.compareTo(min) < 0) {
                min = value;
            }
        }

        BigDecimal avg = sum.divide(
                BigDecimal.valueOf(points.size()),
                2,
                RoundingMode.HALF_UP
        );

        return "최고: "
                + formatRate(max)
                + " / 최저: "
                + formatRate(min)
                + " / 평균: "
                + formatRate(avg);
    }

    private String formatRate(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(110, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    public void refresh() {
        loadExchangeRates();
    }

    private static class ChartPoint {
        private final String label;
        private final BigDecimal value;

        private ChartPoint(String label, BigDecimal value) {
            this.label = label;
            this.value = value;
        }
    }

    private static class ExchangeRateChartPanel extends JPanel {

        private List<ChartPoint> points = new ArrayList<>();

        private ExchangeRateChartPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(700, 360));
        }

        private void setPoints(List<ChartPoint> points) {
            this.points = points != null ? points : new ArrayList<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            int leftPadding = 70;
            int rightPadding = 30;
            int topPadding = 30;
            int bottomPadding = 60;

            int chartWidth = width - leftPadding - rightPadding;
            int chartHeight = height - topPadding - bottomPadding;

            g2.setColor(new Color(245, 246, 250));
            g2.fillRect(leftPadding, topPadding, chartWidth, chartHeight);

            g2.setColor(new Color(180, 180, 180));
            g2.drawLine(leftPadding, topPadding, leftPadding, topPadding + chartHeight);
            g2.drawLine(leftPadding, topPadding + chartHeight, leftPadding + chartWidth, topPadding + chartHeight);

            if (points == null || points.isEmpty()) {
                g2.setFont(new Font("맑은 고딕", Font.BOLD, 16));
                g2.setColor(Color.GRAY);
                g2.drawString("환율 데이터가 없습니다.", leftPadding + 220, topPadding + 150);
                g2.dispose();
                return;
            }

            BigDecimal minValue = points.get(0).value;
            BigDecimal maxValue = points.get(0).value;

            for (ChartPoint point : points) {
                if (point.value.compareTo(minValue) < 0) {
                    minValue = point.value;
                }

                if (point.value.compareTo(maxValue) > 0) {
                    maxValue = point.value;
                }
            }

            if (maxValue.compareTo(minValue) == 0) {
                maxValue = maxValue.add(BigDecimal.ONE);
                minValue = minValue.subtract(BigDecimal.ONE);
            }

            BigDecimal range = maxValue.subtract(minValue);

            int count = points.size();

            int[] xPoints = new int[count];
            int[] yPoints = new int[count];

            for (int i = 0; i < count; i++) {
                ChartPoint point = points.get(i);

                int x;

                if (count == 1) {
                    x = leftPadding + chartWidth / 2;
                } else {
                    x = leftPadding + (i * chartWidth / (count - 1));
                }

                BigDecimal normalized =
                        point.value.subtract(minValue)
                                .divide(range, 6, RoundingMode.HALF_UP);

                int y = topPadding + chartHeight
                        - normalized.multiply(BigDecimal.valueOf(chartHeight)).intValue();

                xPoints[i] = x;
                yPoints[i] = y;
            }

            drawHorizontalGrid(g2, leftPadding, topPadding, chartWidth, chartHeight, minValue, maxValue);
            drawLineChart(g2, xPoints, yPoints);
            drawPoints(g2, xPoints, yPoints);
            drawLabels(g2, xPoints, yPoints);

            g2.dispose();
        }

        private void drawHorizontalGrid(
                Graphics2D g2,
                int leftPadding,
                int topPadding,
                int chartWidth,
                int chartHeight,
                BigDecimal minValue,
                BigDecimal maxValue
        ) {
            g2.setFont(new Font("맑은 고딕", Font.PLAIN, 11));

            int gridCount = 5;

            for (int i = 0; i <= gridCount; i++) {
                int y = topPadding + (chartHeight * i / gridCount);

                g2.setColor(new Color(225, 225, 225));
                g2.drawLine(leftPadding, y, leftPadding + chartWidth, y);

                BigDecimal rate = maxValue.subtract(
                        maxValue.subtract(minValue)
                                .multiply(BigDecimal.valueOf(i))
                                .divide(BigDecimal.valueOf(gridCount), 2, RoundingMode.HALF_UP)
                );

                g2.setColor(Color.GRAY);
                g2.drawString(rate.setScale(2, RoundingMode.HALF_UP).toPlainString(), 10, y + 4);
            }
        }

        private void drawLineChart(Graphics2D g2, int[] xPoints, int[] yPoints) {
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(45, 108, 223));

            for (int i = 0; i < xPoints.length - 1; i++) {
                g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            }
        }

        private void drawPoints(Graphics2D g2, int[] xPoints, int[] yPoints) {
            g2.setColor(new Color(231, 76, 60));

            for (int i = 0; i < xPoints.length; i++) {
                g2.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
            }
        }

        private void drawLabels(Graphics2D g2, int[] xPoints, int[] yPoints) {
            g2.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            g2.setColor(new Color(45, 52, 71));

            int step = Math.max(1, points.size() / 7);

            for (int i = 0; i < points.size(); i += step) {
                String dateLabel = points.get(i).label;
                BigDecimal value = points.get(i).value;

                if (dateLabel.length() > 10) {
                    dateLabel = dateLabel.substring(0, 10);
                }

                g2.drawString(dateLabel, xPoints[i] - 30, getHeight() - 30);
                g2.drawString(
                        value.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        xPoints[i] - 25,
                        yPoints[i] - 10
                );
            }
        }
    }
}