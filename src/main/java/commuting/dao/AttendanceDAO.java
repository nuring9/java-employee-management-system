package commuting.dao;

import commuting.common.DBUtil;
import commuting.dto.Attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static commuting.common.DBUtil.getConnection;

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


    // 내 출퇴근 기록 조회 (SELECT) user가 접속 시
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


    // 이제부터 관리자 기능 추가

    // 과거 포함 전체 출퇴근 이력 조회
    public List<Attendance> findAll() {
        List<Attendance> list = new ArrayList<>();

        String sql =
                "SELECT a.user_id, u.name, u.department, a.work_date, a.check_in, a.check_out " +
                        "FROM attendance a " +
                        "JOIN user u ON a.user_id = u.user_id " +
                        "ORDER BY a.work_date DESC, u.department, u.name";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                Attendance a = new Attendance();
                a.setUser_id(rs.getString("user_id"));
                a.setName(rs.getString("name"));
                a.setDepartment(rs.getString("department"));
                a.setWork_date(rs.getDate("work_date").toLocalDate());

                if (rs.getTime("check_in") != null) {
                    a.setCheck_in(LocalDateTime.of(
                            a.getWork_date(),
                            rs.getTime("check_in").toLocalTime()
                    ));
                }

                if (rs.getTime("check_out") != null) {
                    a.setCheck_out(LocalDateTime.of(
                            a.getWork_date(),
                            rs.getTime("check_out").toLocalTime()
                    ));
                }

                list.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    // 출근 평일만 계산
    public int countMonthlyAttendanceExcludeWeekend(String userId, YearMonth month) {

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        String sql =
                "SELECT COUNT(*) " +
                        "FROM attendance " +
                        "WHERE user_id = ? " +
                        "AND check_in IS NOT NULL " +
                        "AND work_date BETWEEN ? AND ? " +
                        "AND DAYOFWEEK(work_date) NOT IN (1, 7)"; // 일(1), 토(7)

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 월 출근일수
    public int countMonthlyAttendance(String userId, YearMonth month) {

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        String sql =
                "SELECT COUNT(*) " +
                        "FROM attendance " +
                        "WHERE user_id = ? " +
                        "AND check_in IS NOT NULL " +
                        "AND work_date BETWEEN ? AND ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 날짜별 전체 출퇴근 조회
    public List<Attendance> findAllByDate(LocalDate date) {
        List<Attendance> list = new ArrayList<>();

        String sql = "SELECT a.user_id, u.name, u.department, a.work_date, a.check_in, a.check_out " +
                "FROM attendance a " +
                "JOIN user u ON a.user_id = u.user_id " +
                "WHERE a.work_date = ? " +
                "ORDER BY u.department, u.name";
        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Attendance a = new Attendance();
                a.setUser_id(rs.getString("user_id"));
                a.setName(rs.getString("name"));
                a.setDepartment(rs.getString("department"));
                a.setWork_date(rs.getDate("work_date").toLocalDate());

                if (rs.getTime("check_in") != null) {
                    LocalTime in = rs.getTime("check_in").toLocalTime();
                    a.setCheck_in(LocalDateTime.of(a.getWork_date(), in));
                }

                if (rs.getTime("check_out") != null) {
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

    // 전체 직원 수
    public int getTotalEmployeeCount() {
        String sql = "SELECT COUNT(*) FROM user WHERE role = 'EMPLOYEE'";
        int count = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    // 오늘 출근자 수
    public int countTodayCheckIn() {
        String sql = "SELECT COUNT(*) FROM attendance WHERE work_date = CURDATE() AND check_in IS NOT NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 오늘 미출근자 수
    public int countTodayAbsent() {
        String sql =
                "SELECT COUNT(*) " +
                        "FROM user u " +
                        "LEFT JOIN attendance a " +
                        "  ON u.user_id = a.user_id " +
                        " AND a.work_date = CURDATE() " +
                        "WHERE u.role = 'EMPLOYEE' " +
                        "AND a.check_in IS NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 오늘 퇴근자 수
    public int countTodayCheckOut() {
        String sql =
                "SELECT COUNT(*) " +
                        "FROM attendance " +
                        "WHERE work_date = CURDATE() " +
                        "AND check_out IS NOT NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 날짜별 출근자 수
    public int countCheckInByDate(LocalDate date) {
        String sql =
                "SELECT COUNT(*) FROM attendance " +
                        "WHERE work_date = ? AND check_in IS NOT NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 날짜별 퇴근자 수
    public int countCheckOutByDate(LocalDate date) {
        String sql =
                "SELECT COUNT(*) FROM attendance " +
                        "WHERE work_date = ? AND check_out IS NOT NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    // 출석을했던안했던 모든직원
    public List<Attendance> findAllWithAbsentByDate(LocalDate date) {
        List<Attendance> list = new ArrayList<>();

        String sql =
                "SELECT u.user_id, u.name, u.department, ? AS work_date, " +
                        "       a.check_in, a.check_out " +
                        "FROM user u " +
                        "LEFT JOIN attendance a " +
                        "  ON u.user_id = a.user_id AND a.work_date = ? " +
                        "WHERE u.role = 'EMPLOYEE' " +
                        "ORDER BY u.department, u.name";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setDate(2, java.sql.Date.valueOf(date));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Attendance a = new Attendance();
                a.setUser_id(rs.getString("user_id"));
                a.setName(rs.getString("name"));
                a.setDepartment(rs.getString("department"));
                a.setWork_date(date);

                if (rs.getTime("check_in") != null) {
                    a.setCheck_in(
                            LocalDateTime.of(date, rs.getTime("check_in").toLocalTime())
                    );
                }

                if (rs.getTime("check_out") != null) {
                    a.setCheck_out(
                            LocalDateTime.of(date, rs.getTime("check_out").toLocalTime())
                    );
                }

                list.add(a);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

// 부서별 출근
    public List<Object[]> getDepartmentAttendanceRate(LocalDate date) {
        List<Object[]> list = new ArrayList<>();

        String sql =
                "SELECT u.department, " +
                        "COUNT(u.user_id) AS total, " +
                        "SUM(CASE WHEN a.check_in IS NOT NULL THEN 1 ELSE 0 END) AS checked " +
                        "FROM user u " +
                        "LEFT JOIN attendance a " +
                        "ON u.user_id = a.user_id AND a.work_date = ? " +
                        "WHERE u.role = 'EMPLOYEE' " +
                        "GROUP BY u.department";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String dept = rs.getString("department");
                int total = rs.getInt("total");
                int checked = rs.getInt("checked");

                list.add(new Object[]{dept, checked, total});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

//        // 관리자 강제 퇴근 처리
//    public boolean updateCheckOut(String user_id, LocalDate date) {
//        String sql =
//                "UPDATE attendance " +
//                        "SET check_out = CURTIME() " +
//                        "WHERE user_id = ? AND work_date = ? AND check_out IS NULL";
//
//        try (Connection conn = DBUtil.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, user_id);
//            pstmt.setDate(2, java.sql.Date.valueOf(date));
//
//            return pstmt.executeUpdate() > 0;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
}
