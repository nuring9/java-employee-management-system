package commuting.service;

import commuting.dao.AttendanceDAO;
import commuting.dto.Attendance;

import java.time.LocalDate;
import java.util.List;

public class AdminAttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    /* ===============================
     * 날짜별 전체 출퇴근 조회
     * =============================== */
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceDAO.findAllByDate(date);
    }

    /* ===============================
     * 오늘 출근자 수
     * =============================== */
    public int getTodayCheckInCount() {
        return attendanceDAO.countTodayCheckIn();
    }

    /* ===============================
     * 오늘 미출근자 수
     * =============================== */
    public int getTodayAbsentCount() {
        return attendanceDAO.countTodayAbsent();
    }

    /* ===============================
     * 관리자 대시보드 요약
     * =============================== */
    public AdminDashboard getTodayDashboard() {
        int checkIn = attendanceDAO.countTodayCheckIn();
        int absent = attendanceDAO.countTodayAbsent();

        return new AdminDashboard(checkIn, absent);
    }

    /* ===============================
     * 관리자 강제 퇴근 처리
     * =============================== */
    public boolean forceCheckOut(String userId) {
        return attendanceDAO.updateCheckOut(userId, LocalDate.now());
    }

    /* ===============================
     * 내부 DTO (대시보드 전용)
     * =============================== */
    public static class AdminDashboard {
        private final int checkInCount;
        private final int absentCount;

        public AdminDashboard(int checkInCount, int absentCount) {
            this.checkInCount = checkInCount;
            this.absentCount = absentCount;
        }

        public int getCheckInCount() {
            return checkInCount;
        }

        public int getAbsentCount() {
            return absentCount;
        }
    }
}
