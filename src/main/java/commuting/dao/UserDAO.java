package commuting.dao;

import commuting.dto.User;
import java.sql.*;
import java.util.List;

public class UserDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/your_db_name";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 1. 직원 추가
    public void insertUser(User user) {
        String sql = "INSERT INTO user (user_id, password, name, department, role) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getDepartment());
            pstmt.setString(5, user.getRole());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("직원 등록 실패", e);
        }
    }

    // 2. 직원 수정
    public void updateUser(User user) {
        throw new UnsupportedOperationException("User DTO 확정 후 구현 예정");
    }

    // 3. 직원 삭제
    public void deleteUser(String userId) {
        String sql = "DELETE FROM user WHERE user_id = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("직원 삭제 실패", e);
        }
    }

    // 4. 전체 직원 조회
    public List<User> selectAllUsers() {
        throw new UnsupportedOperationException("User DTO 확정 후 구현 예정");
    }

    // 5. 아이디 중복 체크
    public boolean existsByUserId(String userId) {
        String sql = "SELECT COUNT(*) FROM user WHERE user_id = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, userId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("아이디 중복 체크 실패", e);
        }
    }
}

