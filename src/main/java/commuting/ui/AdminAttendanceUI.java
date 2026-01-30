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

    private JLabel lblCheckIn;
    private JLabel lblAbsent;

    public AdminAttendanceUI(User admin) {
        setTitle("관리자 출퇴근 관리");
        setSize(920, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
        loadToday();

        setVisible(true);
    }

    private void initUI() {

        //  프레임 전체 여백
        ((JComponent) getContentPane())
                .setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // 상단
        lblCheckIn = new JLabel("출근자: 0명");
        lblAbsent = new JLabel("미출근자: 0명");

        JButton todayBtn = new JButton("오늘 조회");
        todayBtn.addActionListener(e -> loadToday());

        JButton forceOutBtn = new JButton("강제 퇴근");
        forceOutBtn.addActionListener(e -> forceCheckOut());

        todayBtn.setPreferredSize(new Dimension(90, 30));
        forceOutBtn.setPreferredSize(new Dimension(100, 30));


        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        top.add(todayBtn);
        top.add(lblCheckIn);
        top.add(lblAbsent);
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
    }

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
        AdminAttendanceService.AdminDashboard dash = service.getTodayDashboard();

        lblCheckIn.setText("출근자: " + dash.getCheckInCount() + "명");
        lblAbsent.setText("미출근자: " + dash.getAbsentCount() + "명");
    }

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