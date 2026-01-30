package commuting.dao;

import commuting.common.DBUtil;
import commuting.dto.Attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    // 출근 처리 (INSERT)
    public void insertCheckIn(String user_id) {
        String sql = "INSERT INTO attendance (user_id, work_date, check_in) VALUES (?, CURDATE(), CURTIME())";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, user_id);
            pstmt.executeUpdate(); // INSERT
            // executeUpdate() = DB의 “데이터를 바꾸는 SQL”을 실행하는 메서드
            // INSERT, UPDATE, DELETE 할때 씀, (SELECT 말고 전부)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 퇴근 (UPDATE)
    public void updateCheckOut(String user_id) {
        String sql = "UPDATE attendance SET check_out = CURTIME() WHERE user_id = ? AND work_date = CURDATE()";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, user_id);
            pstmt.executeUpdate(); // UPDATE
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 내 출퇴근 기록 조회 (SELECT)
    public List<Attendance> selectMyAttendance(String user_id) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE user_id = ? ORDER BY work_date DESC";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, user_id);
            ResultSet rs = pstmt.executeQuery(); // SELECT
            // ResultSet = DB에서 SELECT한 결과를 담고 있는 “자바 객체”

            while (rs.next()) {
                Attendance a = new Attendance();
                a.setId(rs.getInt("id"));
                a.setUser_id(rs.getString("user_id"));
                a.setWork_date(rs.getDate("work_date").toLocalDate());

                LocalTime in = rs.getTime("check_in").toLocalTime();
                a.setCheck_in(LocalDateTime.of(a.getWork_date(), in));
                // 날짜 (LocalDate)+ 시간 (LocalTime) 둘을 합쳐서 LocalDateTime 생성

                if (rs.getTime("check_out") != null) {
                    // 출근만 하고 퇴근 안 했을 수도 있어서 if문
                    LocalTime out = rs.getTime("check_out").toLocalTime();
                    a.setCheck_out(LocalDateTime.of(a.getWork_date(), out));
                }

                list.add(a);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
