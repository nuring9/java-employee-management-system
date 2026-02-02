package commuting.common;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DBUtil {

    private static final String URL =
            "jdbc:mysql://localhost:3306/commuting?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    /* ===============================
     * DB Connection 반환
     * =============================== */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}