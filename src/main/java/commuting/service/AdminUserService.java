package commuting.service;

import commuting.dao.UserDAO;
import commuting.dto.User;
import java.util.List;

public class AdminUserService {

    private UserDAO userDAO = new UserDAO();

    public void addUser(User loginUser, User newUser) {
        checkAdmin(loginUser);
        userDAO.insertUser(newUser);
    }


    public void updateUser(User loginUser, User user) {
        checkAdmin(loginUser);
        userDAO.updateUser(user);
    }

    public void deleteUser(User loginUser, String userId) {
        checkAdmin(loginUser);
        userDAO.deleteUser(userId);
    }

    public List<User> getAllUsers(User loginUser) {
        checkAdmin(loginUser);
        return userDAO.selectAllUsers();
    }

    private void checkAdmin(User loginUser) {
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }
    }
}
