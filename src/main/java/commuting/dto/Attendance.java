package commuting.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Attendance {
    private int id;   // PK
    private String user_id;  // user.user_id (FK)
    private String name;  // 이름
    private String department;  // 부서
    private LocalDate work_date;   // 근무일 (YYYY-MM-DD)
    private LocalDateTime check_in;  // 출근시간
    private LocalDateTime check_out;  // 퇴근시간
    private String work_status;  // 그날 근무 상태

    public int getId(){
        return id;
    }

    public void setId(int id){
     this.id = id;
    }

    public String getUser_id(){
        return user_id;
    }

    public void setUser_id(String user_id){
        this.user_id = user_id;
    }


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getWork_date() {
        return work_date;
    }
    public void setWork_date(LocalDate work_date) {
        this.work_date = work_date;
    }

    public LocalDateTime getCheck_in() {
        return check_in;
    }
    public void setCheck_in(LocalDateTime check_in) {
        this.check_in = check_in;
    }

    public LocalDateTime getCheck_out() {
        return check_out;
    }
    public void setCheck_out(LocalDateTime check_out) {
        this.check_out = check_out;
    }
    public String getWork_status() { return work_status; }
    public void setWork_status(String work_status) {
        this.work_status = work_status;
    }

}
