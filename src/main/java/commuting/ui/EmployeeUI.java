package commuting.ui;

import commuting.dto.Attendance;
import commuting.dto.User;
import commuting.service.EmployeeService;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployeeUI extends JFrame {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private User loginUser;
    private EmployeeService employeeService = new EmployeeService();
    private JTextArea textArea;


    // 하단
    private JLabel lblStatus;
    private JLabel lblWorkTime;


    public EmployeeUI(User user) {
        this.loginUser = user;

        setTitle("직원 화면 _ " + user.getName());
        setSize(640, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        updateTodayStatus(); // 초기 상태 갱신
        loadInitialView(); // 초기화면
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
        textArea.setEditable(false);   // 수정 불가
        textArea.setEnabled(true);     // 활성 상태 유지
        textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼 영역
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        JButton btnCheckIn = new JButton("출근");
        JButton btnCheckOut = new JButton("퇴근");
        JButton btnMyLog = new JButton("전체 출근 조회");
        JButton btnDateLog = new JButton("날짜별 조회");
        JButton btnLogout = new JButton("로그아웃");

        btnCheckIn.setPreferredSize(new Dimension(80, 32));
        btnCheckOut.setPreferredSize(new Dimension(80, 32));
        btnMyLog.setPreferredSize(new Dimension(120, 32));
        btnDateLog.setPreferredSize(new Dimension(100, 32));
        btnLogout.setPreferredSize(new Dimension(100, 32));

        btnPanel.add(btnCheckIn);
        btnPanel.add(btnCheckOut);
        btnPanel.add(btnMyLog);
        btnPanel.add(btnDateLog);
        btnPanel.add(btnLogout);

        btnCheckIn.addActionListener(e -> checkIn());
        btnCheckOut.addActionListener(e -> checkOut());
        btnMyLog.addActionListener(e -> loadMyAttendance());          // 전체 이력
        btnDateLog.addActionListener(e -> loadMyAttendanceByDate()); // 날짜별
        btnLogout.addActionListener(e -> logout()); // 로그아웃

        // 하단 상태 패널
        lblStatus = new JLabel("오늘 상태: 미출근");
        lblWorkTime = new JLabel("오늘 근무시간: 0시간 0분");

        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblWorkTime.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        statusPanel.add(lblStatus);
        statusPanel.add(lblWorkTime);

// 하단 전체 패널
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(btnPanel, BorderLayout.NORTH);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void checkIn() {
        boolean result = employeeService.checkIn(loginUser);

        if (result) {
            JOptionPane.showMessageDialog(this, "출근 처리되었습니다.");
            updateTodayStatus();
            loadInitialView();
        } else {
            JOptionPane.showMessageDialog(this, "이미 오늘 출근 처리되었습니다.");
        }
    }

    private void checkOut() {
        boolean result = employeeService.checkOut(loginUser);

        if (result) {
            JOptionPane.showMessageDialog(this, "퇴근 처리되었습니다.");
            updateTodayStatus();
            loadInitialView();
        } else {
            JOptionPane.showMessageDialog(this, "퇴근할 수 없습니다. (출근 후 10분 미경과 또는 미출근)");
        }
    }

    // 초기 화면
    private void loadInitialView() {
        textArea.setText("");

        Attendance today = employeeService.getTodayAttendance(loginUser);


        textArea.append("▶ 오늘 근무 요약\n");

        if (today == null) {
            textArea.append(" - 아직 출근하지 않았습니다.\n\n");
        } else {
            String inTime = today.getCheck_in() == null ? "-" : today.getCheck_in().format(TIME_FMT);
            String outTime = today.getCheck_out() == null ? "-" : today.getCheck_out().format(TIME_FMT);

            textArea.append(" - 출근: " + inTime + "\n");
            textArea.append(" - 퇴근: " + outTime + "\n\n");
        }

        textArea.append("▶ 최근 출근 기록\n");

        List<Attendance> list = employeeService.getMyAttendance(loginUser);

        int count = 0;
        for (Attendance a : list) {
            if (count == 5) break;

            textArea.append(
                    a.getWork_date()
                            + " | 출근: " +
                            (a.getCheck_in() == null ? "-" : a.getCheck_in().format(TIME_FMT))
                            + " | 퇴근: " +
                            (a.getCheck_out() == null ? "-" : a.getCheck_out().format(TIME_FMT))
                            + "\n"
            );
            count++;
        }
    }


    // 로그아웃
    private void logout() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "로그아웃 하시겠습니까?",
                "로그아웃",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            dispose();          // 직원 화면 닫기
            new LoginUI();      // 로그인 화면으로 이동
        }
    }


    private void loadMyAttendanceByDate() {

        // 날짜 선택 Spinner
        SpinnerDateModel model =
                new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(model);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        int result = JOptionPane.showConfirmDialog(
                this,
                dateSpinner,
                "조회할 날짜 선택",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        java.util.Date selected = (java.util.Date) dateSpinner.getValue();
        java.time.LocalDate date = selected.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        List<Attendance> list =
                employeeService.getMyAttendance(loginUser);

        textArea.setText("");

        boolean found = false;

        for (Attendance a : list) {
            if (a.getWork_date().equals(date)) {
                found = true;

                String inTime = a.getCheck_in() == null ? "-" : a.getCheck_in().format(TIME_FMT);
                String outTime = a.getCheck_out() == null ? "-" : a.getCheck_out().format(TIME_FMT);

                textArea.append(
                        a.getWork_date()
                                + " | 출근: " + inTime
                                + " | 퇴근: " + outTime
                                + "\n"
                );
            }
        }

        if (!found) {
            textArea.setText(date + " 출근 기록이 없습니다.");
        }
    }


    private void updateTodayStatus() {
        Attendance today = employeeService.getTodayAttendance(loginUser);

        if (today == null) {
            lblStatus.setText("오늘 상태: 미출근");
            lblWorkTime.setText("오늘 근무시간: 0시간 0분");
            return;
        }

        if (today.getCheck_out() == null) {
            lblStatus.setText("오늘 상태: 근무중");
        } else {
            lblStatus.setText("오늘 상태: 퇴근완료");
        }

        long minutes = employeeService.getTodayWorkMinutes(loginUser);
        long hours = minutes / 60;
        long mins = minutes % 60;

        lblWorkTime.setText("오늘 근무시간: " + hours + "시간 " + mins + "분");
    }


    private void loadMyAttendance() {
        List<Attendance> list = employeeService.getMyAttendance(loginUser);

        textArea.setText("");

        for (Attendance a : list) {
            String inTime = (a.getCheck_in() == null)
                    ? "-"
                    : a.getCheck_in().format(TIME_FMT);

            String outTime = (a.getCheck_out() == null)
                    ? "-"
                    : a.getCheck_out().format(TIME_FMT);

            textArea.append(
                    a.getWork_date()
                            + " | 출근: " + inTime
                            + " | 퇴근: " + outTime
                            + "\n"
            );
        }
    }

}
