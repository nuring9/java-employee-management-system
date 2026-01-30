package commuting.ui;

import commuting.dto.Attendance;
import commuting.dto.User;
import commuting.service.EmployeeService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EmployeeUI extends JFrame {
    private User loginUser;
    private EmployeeService employeeService = new EmployeeService();

    private JTextArea textArea;

    public EmployeeUI(User user) {
        this.loginUser = user;

        setTitle("직원 화면 _ " + user.getName());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        JLabel lblWelcome = new JLabel(
                loginUser.getName() + "님, 근무 관리 화면입니다.",
                SwingConstants.CENTER
        );
        add(lblWelcome, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEnabled(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();

        JButton btnCheckIn = new JButton("출근");
        JButton btnCheckOut = new JButton("퇴근");
        JButton btnMyLog = new JButton("내 출근 기록");

        btnPanel.add(btnCheckIn);
        btnPanel.add(btnCheckOut);
        btnPanel.add(btnMyLog);

        add(btnPanel, BorderLayout.SOUTH);

        btnCheckIn.addActionListener(e -> checkIn());
        btnCheckOut.addActionListener(e -> checkOut());
        btnMyLog.addActionListener(e -> loadMyAttendance());
    }

    private void checkIn() {
        boolean result = employeeService.checkIn(loginUser);

        if (result) {
            JOptionPane.showMessageDialog(this, "출근 처리되었습니다.");
        } else {
            JOptionPane.showMessageDialog(this, "이미 오늘 출근 처리되었습니다.");
        }
    }

    private void checkOut() {
        boolean result = employeeService.checkOut(loginUser);

        if (result) {
            JOptionPane.showMessageDialog(this, "퇴근 처리되었습니다.");
        } else {
            JOptionPane.showMessageDialog(this, "퇴근할 수 없습니다. (출근 후 10분 미경과 또는 미출근)");
        }
    }

    private void loadMyAttendance() {
        List<Attendance> list = employeeService.getMyAttendance(loginUser);

        textArea.setText(""); // 초기화

        for (Attendance a : list) {
            textArea.append(
                    a.getWork_date() + " | 출근: " + a.getCheck_in() + " | 퇴근: " + a.getCheck_out() + "\n"
            );
        }
    }
}
