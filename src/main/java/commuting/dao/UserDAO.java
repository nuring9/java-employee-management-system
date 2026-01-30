package commuting.dao;

import commuting.common.DBUtil;
import commuting.dto.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    // 로그인용: user_id + password로 유저 1명 조회
    public User findByIdAndPassword(String user_id, String password) {
        String sql = "SELECT * FROM user WHERE user_id = ? AND password = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             // PreparedStatement = SQL을 “미리 준비”해 두고, 값만 꽂아서 실행하는 객체
        ) {
            pstmt.setString(1, user_id);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            // ResultSet = SELECT 결과를 담고 있는 “테이블 결과 상자”

            if (rs.next()) {
                User user = new User();

                user.setUser_id(rs.getString("user_id"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setDepartment(rs.getString("department"));
                user.setRole(rs.getString("role"));
                user.setPhone(rs.getString("phone"));
                user.setCreated_at(
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                // DB TIMESTAMP → Java LocalDateTime 변환
                return user; // 로그인 성공
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 로그인 실패
    }


    // 관리자 - 전체 직원 조회
    public List<User> selectAllUsers() {
        List<User> list = new ArrayList<>();

        String sql = "SELECT * FROM user ORDER BY created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setUser_id(rs.getString("user_id"));
                user.setName(rs.getString("name"));
                user.setDepartment(rs.getString("department"));
                user.setRole(rs.getString("role"));
                user.setPhone(rs.getString("phone"));
                user.setCreated_at(
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                list.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 관리자 - 직원 삭제
    public void deleteUser(String user_id) {
        String sql = "DELETE FROM user WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, user_id);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 관리자 - 직원 추가
    public void insertUser(User user) {
        String sql = "INSERT INTO user\n" +
                "(user_id, password, name, department, role, phone)\n" +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUser_id());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getDepartment());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getPhone());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 관리자 - 직원 수정
    public void updateUser(User user) {
        String sql = """
                    UPDATE user
                       SET name = ?, department = ?, role = ?, phone = ?
                     WHERE user_id = ?
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getDepartment());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getUser_id());

            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

