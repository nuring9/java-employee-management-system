
-- User
CREATE TABLE user (
    user_id     VARCHAR(20) PRIMARY KEY,
    password    VARCHAR(100) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    department  VARCHAR(50) NOT NULL,
    role        ENUM('EMPLOYEE', 'ADMIN') NOT NULL,
    phone       VARCHAR(20),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Attendance
CREATE TABLE attendance (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    VARCHAR(20) NOT NULL,
    work_date  DATE NOT NULL,
    check_in   TIME,
    check_out  TIME,

    CONSTRAINT fk_attendance_user
        FOREIGN KEY (user_id)
        REFERENCES user(user_id)
        ON DELETE CASCADE,

    CONSTRAINT uq_user_workdate
        UNIQUE (user_id, work_date)
);
