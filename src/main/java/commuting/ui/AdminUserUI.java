package commuting.ui;


import commuting.dto.User;
import commuting.service.AdminUserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminUserUI extends JFrame {
    private User loginUser;
    private AdminUserService adminUserService = new AdminUserService();

    private JTable table;
    private DefaultTableModel model;

    public AdminUserUI(User loginUser) {

        this.loginUser = loginUser;

        setTitle("관리자 - 직원관리");
        setSize(740, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        loadUsers();

        setVisible(true);
    }

    public void initUI() {

        // 프레임 전체 여백
        ((JComponent) getContentPane())
                .setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));


        // 테이블
        String[] cols = {"ID", "이름", "부서", "역할", "전화번호"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));

        JButton btnAdd = new JButton("추가");
        JButton btnUpdate = new JButton("수정");
        JButton btnDelete = new JButton("삭제");
        JButton btnAttendance = new JButton("출퇴근 관리");
        JButton btnRefresh = new JButton("새로고침");

        Dimension btnSize = new Dimension(90, 32);
        btnAdd.setPreferredSize(btnSize);
        btnUpdate.setPreferredSize(btnSize);
        btnDelete.setPreferredSize(btnSize);
        btnAttendance.setPreferredSize(btnSize);
        btnRefresh.setPreferredSize(btnSize);


        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnAttendance);
        btnPanel.add(btnRefresh);

        add(btnPanel, BorderLayout.SOUTH);


        // 이벤트
        btnRefresh.addActionListener(e -> loadUsers());
        btnDelete.addActionListener(e -> deleteSelectedUser());
        btnAdd.addActionListener(e -> openAddUserDialog());
        btnAttendance.addActionListener(e -> {
            new AdminAttendanceUI(loginUser); // loginUser = 관리자
        });
        btnUpdate.addActionListener(e -> openUpdateUserDialog());
    }

    public void loadUsers() {
        model.setRowCount(0);

        List<User> users = adminUserService.getAllUsers(loginUser);
        for (User u : users) {
            model.addRow(new Object[]{
                    u.getUser_id(),
                    u.getName(),
                    u.getDepartment(),
                    u.getRole(),  // admin  , employee 들어옴
                    u.getPhone()
            });
        }
    }

    public void openAddUserDialog() {
        JTextField tfId = new JTextField();
        JTextField tfPw = new JTextField();
        JTextField tfName = new JTextField();
        JTextField tfDept = new JTextField();
        JTextField tfRole = new JTextField("EMPLOYEE");
        JTextField tfPhone = new JTextField();

        Object[] fields = {
                "아이디", tfId,
                "비밀번호", tfPw,
                "이름", tfName,
                "부서", tfDept,
                "역할(ADMIN/EMPLOYEE)", tfRole,
                "전화번호", tfPhone
        };

        int result = JOptionPane.showConfirmDialog(
                this, fields, "직원추가", JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            User newUser = new User();
            newUser.setUser_id(tfId.getText());
            newUser.setPassword(tfPw.getText());
            newUser.setName(tfName.getText());
            newUser.setDepartment(tfDept.getText());
            newUser.setRole(tfRole.getText());
            newUser.setPhone(tfPhone.getText());

            adminUserService.addUser(loginUser, newUser);
            loadUsers();
        }

    }

    // 수정
    public void openUpdateUserDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "수정할 직원을 선택하세요.");
            return;
        }

        // 기존 값 가져오기
        String userId = model.getValueAt(row, 0).toString();
        String name = model.getValueAt(row, 1).toString();
        String dept = model.getValueAt(row, 2).toString();
        String role = model.getValueAt(row, 3).toString();
        String phone = model.getValueAt(row, 4).toString();

        JTextField tfName = new JTextField(name);
        JTextField tfDept = new JTextField(dept);
        JTextField tfRole = new JTextField(role);
        JTextField tfPhone = new JTextField(phone);

        Object[] fields = {
                "이름", tfName,
                "부서", tfDept,
                "역할(ADMIN/EMPLOYEE)", tfRole,
                "전화번호", tfPhone
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                fields,
                "직원 정보 수정",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            User updateUser = new User();
            updateUser.setUser_id(userId);
            updateUser.setName(tfName.getText());
            updateUser.setDepartment(tfDept.getText());
            updateUser.setRole(tfRole.getText());
            updateUser.setPhone(tfPhone.getText());

            adminUserService.updateUser(loginUser, updateUser);
            loadUsers();
        }
    }



    public void deleteSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 직원을 선택하세요.");
            return;
        }
        String userId = model.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this, "정말 삭제하시겠습니까?", "삭제확인", JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            adminUserService.deleteUser(loginUser, userId);
            loadUsers();
        }
    }
}
