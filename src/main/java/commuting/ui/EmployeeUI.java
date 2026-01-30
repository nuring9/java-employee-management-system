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
        setSize(520, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        // 프레임 전체 여백
        ((JComponent) getContentPane())
                .setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // 상단 환영 문구
        JLabel lblWelcome = new JLabel(
                loginUser.getName() + "님, 근무 관리 화면입니다.",
                SwingConstants.CENTER
        );
        lblWelcome.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(lblWelcome, BorderLayout.NORTH);

       // 중앙 텍스트 영역
        textArea = new JTextArea();
        textArea.setEnabled(false);
        textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼 영역
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        JButton btnCheckIn = new JButton("출근");
        JButton btnCheckOut = new JButton("퇴근");
        JButton btnMyLog = new JButton("내 출근 기록");

        btnCheckIn.setPreferredSize(new Dimension(100, 32));
        btnCheckOut.setPreferredSize(new Dimension(100, 32));
        btnMyLog.setPreferredSize(new Dimension(120, 32));

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
