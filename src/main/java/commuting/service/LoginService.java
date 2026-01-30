package commuting.service;

import commuting.dao.UserDAO;
import commuting.dto.User;

public class LoginService {
    private UserDAO userDAO = new UserDAO();

    // 로그인처리
    public User login(String user_id, String password) {

        // UserDAO의 메서드 호출
        User user = userDAO.findByIdAndPassword(user_id, password);

        if (user == null) {
            return null;  // 로그인 실패
        }
        return user; // 로그인 성공
    }
}
