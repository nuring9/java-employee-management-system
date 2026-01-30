package commuting.service;

import commuting.dao.AttendanceDAO;
import commuting.dto.Attendance;
import commuting.dto.User;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EmployeeService {
    private AttendanceDAO attendanceDAO = new AttendanceDAO();

    // 출근
    public boolean checkIn(User user){
        List<Attendance> list = attendanceDAO.selectMyAttendance(user.getUser_id());
         // 조회 먼저
        LocalDate today = LocalDate.now();

        for(Attendance a : list){
            if (a.getWork_date().equals(today)){
                // 날짜가 오늘이면 이미 출근한 상태임.
                return false;
            }
        }

        attendanceDAO.insertCheckIn(user.getUser_id());
        return true;  // 출근 가능하므로, insert 메서드 호출해서 DB에 반영함.
    }

    // 퇴근
    public boolean checkOut(User user){
        List<Attendance> list = attendanceDAO.selectMyAttendance(user.getUser_id());
        // 조회 먼저
        LocalDate today = LocalDate.now(); // 오늘 날짜

        for(Attendance a : list){
            if(a.getWork_date().equals(today)){
                // 오늘 날짜랑 일치하면
                if(a.getCheck_out() == null){
                    // 체크아웃이 안되어있는 경우(아직 퇴근 안 한 상태인지 확인)
                    LocalDateTime checkInTime = a.getCheck_in();
                    // 오늘 출근한 시간 가져오기
                    LocalDateTime now = LocalDateTime.now();
                    // 현재 시간 (자바 기준, DB랑 날짜시간 쓰는방법이 다름)

                    if(Duration.between(checkInTime, now).toMillis() < 10){
                        return false;
                        // Duration.between(); = 두 사이의 시간의 경과시간 계산 해줌.
                    }
                    attendanceDAO.updateCheckOut(user.getUser_id());
                    return true;

                }

            }
        }
        return true;
    }

    // 출퇴근 기록 조회
    public List<Attendance> getMyAttendance(User user){
        return attendanceDAO.selectMyAttendance(user.getUser_id());
    }

}
