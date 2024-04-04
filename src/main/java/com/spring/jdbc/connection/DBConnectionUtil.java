package com.spring.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.spring.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {
    public static Connection getConnection()  {//jdbc표준 인터페이스를 제공하는 커넥션이다.
        try {
            /*
            gradle h2폴더 안의 Driver class를 통해 db에 들어가게 된다.
            근데 아래 DriverManager가 자기들의 규칙에 의해서 Driver를 찾는다.

            DriverManager는 JDBC가 제공하는 것이다.
            실제 구현체 Driver를 통해서 찾아 실제 커넥션을 가져오게 된다.
             */
            Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            log.info("get connection = {}, class = {}", connection, connection.getClass()); // 잘 받아 왔는지 확인
            return connection;
        }catch (SQLException e) { // DriverManager.getConnection 잘 안되면 에러 캐치
//            e.printStackTrace();
            throw new IllegalArgumentException(e); //trace print(체크 입셉션)대신
        }
    }
}

/* [단축키]
    f2 -> 오류 난 곳 이동
    option cmd v -> 변수로 추출
 */
