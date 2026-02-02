package commuting.ui;

import commuting.dto.Attendance;
import commuting.dto.User;
import commuting.service.AdminAttendanceService;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class AdminAttendanceUI extends JFrame {

    private AdminAttendanceService service = new AdminAttendanceService();
    private DefaultTableModel tableModel;
    private JTable table;

    private JLabel lblStatus;
    private JDatePickerImpl datePicker;

    // 하단 요약
    private JLabel lblSummaryCheckIn;
    private JLabel lblSummaryAbsent;
    private JLabel lblSummaryCheckOut;

    // 부서 카드
    private JPanel deptCardPanel;

    public AdminAttendanceUI(User admin) {
        setTitle("관리자 출퇴근 관리");
        setSize(920, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadToday();
        updateDashboard();

        setVisible(true);
    }

    private void initUI() {

        ((JComponent) getContentPane())
                .setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        lblStatus = new JLabel("전체 이력 조회 중");

        // ===== 날짜 선택기 =====
        Properties p = new Properties();
        p.put("text.today", "오늘");
        p.put("text.month", "월");
        p.put("text.year", "년");

        UtilDateModel model = new UtilDateModel();
        model.setSelected(true);

        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        JButton dateBtn = new JButton("날짜 조회");
        dateBtn.addActionListener(e -> loadByDate());

        JButton allBtn = new JButton("전체 조회");
        allBtn.addActionListener(e -> loadAll());

        JButton todayBtn = new JButton("오늘 조회");
        todayBtn.addActionListener(e -> loadToday());


        JButton forceOutBtn = new JButton("강제 퇴근");
        forceOutBtn.addActionListener(e -> forceCheckOut());

        JPanel top = new JPanel(new BorderLayout());

// 왼쪽 패널 (날짜 선택 + 날짜 조회)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        leftPanel.add(datePicker);
        leftPanel.add(dateBtn);

// 오른쪽 패널 (전체 / 오늘 / 강제퇴근)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        rightPanel.add(allBtn);
        rightPanel.add(todayBtn);
        rightPanel.add(forceOutBtn);

        top.add(leftPanel, BorderLayout.WEST);
        top.add(rightPanel, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ===== 테이블 =====
        String[] cols = {
                "사번", "이름", "부서", "날짜",
                "출근", "퇴근", "상태", "월 출근률(%)"
        };

        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== 하단 메인 패널 (SOUTH 단일) =====
        JPanel mainBottomPanel = new JPanel(new BorderLayout());
        mainBottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // 부서 출근율 카드 (왼쪽)
        deptCardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        mainBottomPanel.add(deptCardPanel, BorderLayout.CENTER);

        // 요약 (오른쪽)
        Font dashFont = UIManager.getFont("Label.font");

        lblSummaryCheckIn = new JLabel("출근자: 0명");
        lblSummaryAbsent = new JLabel("미출근자: 0명");
        lblSummaryCheckOut = new JLabel("퇴근자: 0명");

        lblSummaryCheckIn.setFont(dashFont);
        lblSummaryAbsent.setFont(dashFont);
        lblSummaryCheckOut.setFont(dashFont);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.add(lblSummaryCheckIn);
        summaryPanel.add(Box.createVerticalStrut(6));
        summaryPanel.add(lblSummaryAbsent);
        summaryPanel.add(Box.createVerticalStrut(6));
        summaryPanel.add(lblSummaryCheckOut);

        mainBottomPanel.add(summaryPanel, BorderLayout.EAST);

        add(mainBottomPanel, BorderLayout.SOUTH);
    }

    // ===== 부서 출근율 카드 =====
    private void loadDepartmentCards(LocalDate date) {
        deptCardPanel.removeAll();

        List<Object[]> list = service.getDepartmentAttendanceRate(date);

        for (Object[] row : list) {
            String dept = (String) row[0];
            int checked = (int) row[1];
            int total = (int) row[2];

            int rate = total == 0 ? 0 : checked * 100 / total;

            JPanel card = new JPanel(new BorderLayout());
            card.setPreferredSize(new Dimension(140, 80));
            card.setBorder(BorderFactory.createLineBorder(Color.GRAY));

            card.add(new JLabel(dept, SwingConstants.CENTER), BorderLayout.NORTH);
            card.add(new JLabel(rate + "%", SwingConstants.CENTER), BorderLayout.CENTER);
            card.add(new JLabel("(" + checked + " / " + total + ")", SwingConstants.CENTER),
                    BorderLayout.SOUTH);

            deptCardPanel.add(card);
        }

        deptCardPanel.revalidate();
        deptCardPanel.repaint();
    }

    // ===== 전체 조회 =====
    private void loadAll() {
        tableModel.setRowCount(0);

        for (Attendance a : service.getAllAttendance()) {

            String status;
            if (a.getCheck_in() == null) {
                status = "미출근";
            } else if (a.getCheck_out() == null) {
                status = "출근";
            } else {
                status = "퇴근";
            }

            // 월 출근율 계산 (날짜 기준: 오늘 달)
            YearMonth ym = YearMonth.now();
            double rate =
                    service.getMonthlyAttendanceRateExcludeWeekend(a.getUser_id(), ym);

            String rateText = rate == 0 ? "-" : rate + "%";

            tableModel.addRow(new Object[]{
                    a.getUser_id(),
                    a.getName(),
                    a.getDepartment(),
                    a.getWork_date(),
                    a.getCheck_in(),
                    a.getCheck_out(),
                    status,
                    rateText
            });

        }

        // 전체 조회 시에도 카드 유지 (오늘 기준)
        loadDepartmentCards(LocalDate.now());
    }


    // ===== 오늘 조회 =====
    private void loadToday() {
        LocalDate today = LocalDate.now();
        loadAttendance(today);     // ← 카드 + 테이블 같이 갱신
        updateDashboard();
    }

    // ===== 날짜 조회 =====
    private void loadByDate() {
        Date selected = (Date) datePicker.getModel().getValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "날짜를 선택하세요");
            return;
        }

        LocalDate date = selected.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        loadAttendance(date);
    }

    private void loadAttendance(LocalDate date) {
        tableModel.setRowCount(0);

        List<Attendance> list =
                service.getAttendanceByDate(date);

        YearMonth ym = YearMonth.from(date);

        for (Attendance a : list) {

            String status;
            if (a.getCheck_in() == null) {
                status = "미출근";
            } else if (a.getCheck_out() == null) {
                status = "출근";
            } else {
                status = "퇴근";
            }

            double rate =
                    service.getMonthlyAttendanceRateExcludeWeekend(a.getUser_id(), ym);

            String rateText = rate == 0 ? "-" : rate + "%";

            tableModel.addRow(new Object[]{
                    a.getUser_id(),
                    a.getName(),
                    a.getDepartment(),
                    a.getWork_date(),
                    a.getCheck_in(),
                    a.getCheck_out(),
                    status,
                    rateText
            });

        }

        loadDepartmentCards(date);
    }

    // ===== 하단 요약 (오늘 기준) =====
    private void updateDashboard() {
        AdminAttendanceService.AdminDashboard dash =
                service.getTodayDashboard();

        lblSummaryCheckIn.setText("출근자: " + dash.getCheckInCount() + "명");
        lblSummaryAbsent.setText("미출근자: " + dash.getAbsentCount() + "명");
        lblSummaryCheckOut.setText("퇴근자: " + dash.getCheckOutCount() + "명");
    }

//    // ===== 하단 요약 (선택 날짜 기준) =====
//    private void updateDashboardByDate(LocalDate date) {
//        AdminAttendanceService.AdminDashboard dash =
//                service.getDashboardByDate(date);
//
//        lblSummaryCheckIn.setText("출근자: " + dash.getCheckInCount() + "명");
//        lblSummaryAbsent.setText("미출근자: " + dash.getAbsentCount() + "명");
//        lblSummaryCheckOut.setText("퇴근자: " + dash.getCheckOutCount() + "명");
//    }


    // ===== 강제 퇴근 =====
    private void forceCheckOut() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "직원을 선택하세요");
            return;
        }

        String userId = tableModel.getValueAt(row, 0).toString();
        service.forceCheckOut(userId);
        loadToday();
    }

    // ===== 날짜 포맷 =====
    class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final java.text.SimpleDateFormat formatter =
                new java.text.SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) throws java.text.ParseException {
            return formatter.parse(text);
        }

        @Override
        public String valueToString(Object value) {
            if (value != null) {
                java.util.Calendar cal = (java.util.Calendar) value;
                return formatter.format(cal.getTime());
            }
            return "";
        }
    }
}
