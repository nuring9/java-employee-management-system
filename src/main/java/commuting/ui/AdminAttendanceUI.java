package commuting.ui;

import commuting.dto.Attendance;
import commuting.dto.User;
import commuting.service.AdminAttendanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AdminAttendanceUI extends JFrame {

    private AdminAttendanceService service = new AdminAttendanceService();
    private DefaultTableModel tableModel;
    private JTable table;


//  상단 조회 상태 표시용
    private JLabel lblStatus;


    // 하단 대시보드
    private JLabel lblSummaryCheckIn;
    private JLabel lblSummaryAbsent;
    private JLabel lblSummaryCheckOut;

    // 날짜
    private JSpinner dateSpinner;

    public AdminAttendanceUI(User admin) {
        setTitle("관리자 출퇴근 관리");
        setSize(920, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadAll();
        updateDashboard(); // 하단 요약은 오늘 기준

        setVisible(true);
    }

    private void initUI() {

        //  프레임 전체 여백
        ((JComponent) getContentPane())
                .setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));


        lblStatus = new JLabel("전체 이력 조회 중");

        JButton allBtn = new JButton("전체 조회");
        allBtn.addActionListener(e -> loadAll());

        JButton todayBtn = new JButton("오늘 조회");
        todayBtn.addActionListener(e -> {
            dateSpinner.setValue(new java.util.Date());  // 날짜선택기가 오늘 날짜로 초기화
            loadToday();});

        // 날짜 선택 Spinner
       SpinnerDateModel dateModel = new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
       dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor =
                new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        dateSpinner.setPreferredSize(new Dimension(110, 30));

        // 날짜 조회 버튼
        JButton dateBtn = new JButton("날짜 조회");
        dateBtn.addActionListener(e -> loadByDate());

        JButton forceOutBtn = new JButton("강제 퇴근");
        forceOutBtn.addActionListener(e -> forceCheckOut());

        allBtn.setPreferredSize(new Dimension(90, 30));
        todayBtn.setPreferredSize(new Dimension(90, 30));
        forceOutBtn.setPreferredSize(new Dimension(100, 30));


        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        top.add(allBtn);
        top.add(todayBtn);
        top.add(dateSpinner);   // 날짜 선택
        top.add(dateBtn);       // 날짜 조회
        top.add(forceOutBtn);

        add(top, BorderLayout.NORTH);


        // 테이블
        String[] cols = {
                "사번", "이름", "부서", "날짜",
                "출근", "퇴근", "상태"
        };

        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));

        add(new JScrollPane(table), BorderLayout.CENTER);


        // 하단 대시보드 요약
        Font dashFont = UIManager.getFont("Label.font");

        lblSummaryCheckIn  = new JLabel("출근자: 0명", SwingConstants.CENTER);
        lblSummaryAbsent  = new JLabel("미출근자: 0명", SwingConstants.CENTER);
        lblSummaryCheckOut = new JLabel("퇴근자: 0명", SwingConstants.CENTER);

        lblSummaryCheckIn.setFont(dashFont);
        lblSummaryAbsent.setFont(dashFont);
        lblSummaryCheckOut.setFont(dashFont);

        //GridLayout
        JPanel bottom = new JPanel(new GridLayout(1, 3, 20, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        bottom.add(lblSummaryCheckIn);
        bottom.add(lblSummaryAbsent);
        bottom.add(lblSummaryCheckOut);

        add(bottom, BorderLayout.SOUTH);
    }

    // 전체(과거포함) 조회
    private void loadAll() {
        tableModel.setRowCount(0);

        List<Attendance> list = service.getAllAttendance();

        for(Attendance a : list){
            tableModel.addRow(new Object[]{
                    a.getUser_id(),
                    a.getName(),
                    a.getDepartment(),
                    a.getWork_date(),
                    a.getCheck_in(),
                    a.getCheck_out(),
                    a.getWork_status()
            });
        }
        lblStatus.setText("전체 이력 조회 중");
    }

    // 오늘 조회
    private void loadToday() {
        tableModel.setRowCount(0);

        List<Attendance> list =
                service.getAttendanceByDate(LocalDate.now());

        for (Attendance a : list) {
            tableModel.addRow(new Object[]{
                    a.getUser_id(),
                    a.getName(),
                    a.getDepartment(),
                    a.getWork_date(),
                    a.getCheck_in(),
                    a.getCheck_out(),
                    a.getWork_status()
            });
        }
        // 대시보드 요약
        lblStatus.setText("오늘 출퇴근 현황");
        updateDashboard(); // 오늘 기준 요약 갱신
    }


    // 날짜 조회
    private void loadByDate() {
        tableModel.setRowCount(0);

        java.util.Date selected =
                (java.util.Date) dateSpinner.getValue();

        LocalDate date = selected.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        List<Attendance> list =
                service.getAttendanceByDate(date);

        for (Attendance a : list) {
            tableModel.addRow(new Object[]{
                    a.getUser_id(),
                    a.getName(),
                    a.getDepartment(),
                    a.getWork_date(),
                    a.getCheck_in(),
                    a.getCheck_out(),
                    a.getWork_status()
            });
        }
        lblStatus.setText(date + " 조회 결과");
    }


    // 하단 대시보드 갱신 (오늘 기준)
    private void updateDashboard() {
        AdminAttendanceService.AdminDashboard dash = service.getTodayDashboard();

        lblSummaryCheckIn.setText("출근자: " + dash.getCheckInCount() + "명");
        lblSummaryAbsent.setText("미출근자: " + dash.getAbsentCount() + "명");
        lblSummaryCheckOut.setText("퇴근자: " + dash.getCheckOutCount() + "명");

        System.out.println("대시보드 값 → "
                + dash.getCheckInCount() + ", "
                + dash.getAbsentCount() + ", "
                + dash.getCheckOutCount());
    }


    // 관리자 강제 퇴근
    private void forceCheckOut() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "직원을 선택하세요");
            return;
        }

        String userId = tableModel.getValueAt(row, 0).toString();
        boolean ok = service.forceCheckOut(userId);

        if (ok) {
            JOptionPane.showMessageDialog(this, "강제 퇴근 처리 완료");
            loadToday();
        } else {
            JOptionPane.showMessageDialog(this, "이미 퇴근했거나 처리 불가");
        }
    }


}