package commuting.service;

import commuting.dao.AttendanceDAO;
import commuting.dto.Attendance;

import java.time.LocalDate;
import java.util.List;

public class AdminAttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();


    // 전체 출퇴근 이력 조회 (과거 포함)
    public List<Attendance> getAllAttendance() {
        return attendanceDAO.findAll();
    }

    // 날짜별 전체 출퇴근 조회
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceDAO.findAllByDate(date);
    }

   // 관리자 강제 퇴근 처리
    public boolean forceCheckOut(String userId) {
        return attendanceDAO.updateCheckOut(userId, LocalDate.now());
    }

    // 관리자 대시보드 요약
    public AdminDashboard getTodayDashboard() {
        int checkIn = attendanceDAO.countTodayCheckIn();
        int absent = attendanceDAO.countTodayAbsent();
        int checkOut = attendanceDAO.countTodayCheckOut();

        return new AdminDashboard(checkIn, absent, checkOut);
    }

    //내부 DTO (대시보드 전용)
    public static class AdminDashboard {
        private final int checkInCount;
        private final int absentCount;
        private final int checkOutCount;   // ⭐ 추가

        public AdminDashboard(int checkInCount, int absentCount, int checkOutCount) {
            this.checkInCount = checkInCount;
            this.absentCount = absentCount;
            this.checkOutCount = checkOutCount;
        }

        public int getCheckInCount() {
            return checkInCount;
        }

        public int getAbsentCount() {
            return absentCount;
        }

        public int getCheckOutCount() {
            return checkOutCount;
        }
    }

}