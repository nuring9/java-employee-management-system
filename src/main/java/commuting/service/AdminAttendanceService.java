package commuting.service;

import commuting.dao.AttendanceDAO;
import commuting.dto.Attendance;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
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

    // 날짜별
    public AdminDashboard getDashboardByDate(LocalDate date) {

        int total = attendanceDAO.getTotalEmployeeCount();
        int checkIn = attendanceDAO.countCheckInByDate(date);
        int checkOut = attendanceDAO.countCheckOutByDate(date);

        int absent = total - checkIn;
        if (absent < 0) absent = 0;

        return new AdminDashboard(checkIn, absent, checkOut);
    }


    // 전체 직원 수
    public int getTotalEmployeeCount() {
        return attendanceDAO.getTotalEmployeeCount();
    }


    public int getWorkingDays(YearMonth month) {
        int days = 0;

        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate d = month.atDay(i);
            if (!(d.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    d.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                days++;
            }
        }
        return days;
    }
    public double getMonthlyAttendanceRate(String userId, YearMonth month) {
        int attendedDays = attendanceDAO.countMonthlyAttendance(userId, month);
        int workingDays = getWorkingDays(month);

        if (workingDays == 0) return 0.0;

        return Math.round((attendedDays * 1000.0 / workingDays)) / 10.0;
    }// 월 전체 평일수 계산 (토/일제외)
    private int countWeekdays(YearMonth month) {
        int count = 0;

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            DayOfWeek dow = date.getDayOfWeek();

            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }
    // 월 출석률 계산 (주말 제외)
    public double getMonthlyAttendanceRateExcludeWeekend(
            String userId, YearMonth month) {

        int attendedDays =
                attendanceDAO.countMonthlyAttendanceExcludeWeekend(userId, month);

        int workDays = countWeekdays(month);

        if (workDays == 0) return 0;

        return Math.round((attendedDays * 100.0 / workDays) * 10) / 10.0;
    }


    // ?
    public List<Attendance> getAttendanceWithAbsentByDate(LocalDate date) {
        return attendanceDAO.findAllWithAbsentByDate(date);
    }


    // 관리자 강제 퇴근 처리
    public boolean forceCheckOut(String userId) {
        attendanceDAO.updateCheckOut(userId);
        return true;
    }

    // 출석률
    public List<Object[]> getDepartmentAttendanceRate(LocalDate date) {
        return attendanceDAO.getDepartmentAttendanceRate(date);
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