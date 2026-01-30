package commuting.dao;

import commuting.common.DBUtil;
import commuting.dto.Attendance;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    /* ===============================
     * 날짜별 전체 출퇴근 조회 (관리자)
     * =============================== */
    public List<Attendance> findAllByDate(LocalDate date) {
        List<Attendance> list = new ArrayList<>();

        String sql = """
            SELECT
                u.user_id,
                u.name,
                u.department,
                a.work_date,
                a.check_in,
                a.check_out,
                CASE
                    WHEN a.check_in IS NULL THEN '미출근'
                    WHEN a.check_in IS NOT NULL AND a.check_out IS NULL THEN '근무중'
                    ELSE '퇴근'
                END AS work_status
            FROM user u
            LEFT JOIN attendance a
                ON u.user_id = a.user_id
               AND a.work_date = ?
            WHERE u.role = 'EMPLOYEE'
            ORDER BY
                CASE
                    WHEN a.check_in IS NOT NULL AND a.check_out IS NULL THEN 1
                    WHEN a.check_in IS NOT NULL AND a.check_out IS NOT NULL THEN 2
                    ELSE 3
                END,
                u.department,
                u.name
        """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Attendance att = new Attendance();

                att.setUserId(rs.getString("user_id"));
                att.setUserName(rs.getString("name"));        // 관리자 화면용
                att.setDepartment(rs.getString("department"));// 관리자 화면용
                att.setWorkDate(rs.getDate("work_date") != null
                        ? rs.getDate("work_date").toLocalDate()
                        : null);
                att.setCheckIn(rs.getTime("check_in"));
                att.setCheckOut(rs.getTime("check_out"));
                att.setWorkStatus(rs.getString("work_status"));

                list.add(att);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /* ===============================
     * 오늘 출근자 수
     * =============================== */
    public int countTodayCheckIn() {
        int count = 0;

        String sql = """
            SELECT COUNT(DISTINCT user_id) AS cnt
            FROM attendance
            WHERE work_date = CURDATE()
              AND check_in IS NOT NULL
        """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            if (rs.next()) {
                count = rs.getInt("cnt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    /* ===============================
     * 오늘 미출근자 수
     * =============================== */
    public int countTodayAbsent() {
        int count = 0;

        String sql = """
            SELECT COUNT(*) AS cnt
            FROM user u
            WHERE u.role = 'EMPLOYEE'
              AND NOT EXISTS (
                  SELECT 1
                  FROM attendance a
                  WHERE a.user_id = u.user_id
                    AND a.work_date = CURDATE()
                    AND a.check_in IS NOT NULL
              )
        """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            if (rs.next()) {
                count = rs.getInt("cnt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    /* ===============================
     * (옵션) 관리자 강제 퇴근 처리
     * =============================== */
    public boolean updateCheckOut(String userId, LocalDate date) {
        String sql = """
            UPDATE attendance
            SET check_out = CURTIME()
            WHERE user_id = ?
              AND work_date = ?
              AND check_out IS NULL
        """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, userId);
            pstmt.setDate(2, Date.valueOf(date));

            return pstmt.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
