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

    public AdminAttendanceUI(User admin) {
        setTitle("관리자 출퇴근 관리");
        setSize(900, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
        loadToday();

        setVisible(true);
    }

    private void initUI() {
        String[] cols = {
                "사번", "이름", "부서", "날짜",
                "출근", "퇴근", "상태"
        };

        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);

        JButton todayBtn = new JButton("오늘 조회");
        todayBtn.addActionListener(e -> loadToday());

        JPanel top = new JPanel();
        top.add(todayBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadToday() {
        tableModel.setRowCount(0);

        List<Attendance> list =
                service.getAttendanceByDate(LocalDate.now());

        for (Attendance a : list) {
            tableModel.addRow(new Object[]{
                    a.getUserId(),
                    a.getUserName(),
                    a.getDepartment(),
                    a.getWorkDate(),
                    a.getCheckIn(),
                    a.getCheckOut(),
                    a.getWorkStatus()
            });
        }
    }
}
