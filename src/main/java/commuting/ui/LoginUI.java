package commuting.ui;

import commuting.dto.User;
import commuting.service.LoginService;

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

public class LoginUI extends JFrame {
    private JTextField txtUserId;
    private JPasswordField txtPassword;
    private LoginService loginService = new LoginService();     // 로그인 성공, 실패 처리 불러옴

    public LoginUI() {
        setTitle("출퇴근 관리 시스템 - 로그인");
        setSize(360, 240);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        // 프레임 전체 여백
        ((JComponent) getContentPane())
                .setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 12));
        // rows, cols, 가로간격, 세로간격

        panel.add(new JLabel("아이디"));
        txtUserId = new JTextField();
        txtUserId.setPreferredSize(new Dimension(200, 26));
        panel.add(txtUserId);

        panel.add(new JLabel("비밀번호"));
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(200, 26));
        panel.add(txtPassword);

        panel.add(new JLabel()); // 빈 칸
        JButton btnLogin = new JButton("로그인");
        btnLogin.setPreferredSize(new Dimension(200, 30));
        panel.add(btnLogin);

        add(panel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> login());
        // 버튼 클릭 이벤트 -> 로그인 호출

    }

    private void login() {
        String userId = txtUserId.getText();
        String password = new String(txtPassword.getPassword());

        User user = loginService.login(userId, password);

        if (user == null) {
            JOptionPane.showMessageDialog(
                    this, "아이디 또는 비밀번호가 올바르지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE
            );
        }else{
            JOptionPane.showMessageDialog(
                    this, user.getName() + "님 환영합니다.", "로그인 성공", JOptionPane.INFORMATION_MESSAGE
            );
            dispose(); // 로그인 창 닫기

            if("EMPLOYEE".equals(user.getRole())){
                new EmployeeUI(user);
            } else if("ADMIN".equals(user.getRole())) {
                new AdminUserUI(user);
            }
        }
    }


}
