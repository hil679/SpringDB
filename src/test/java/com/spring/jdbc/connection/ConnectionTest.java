package com.spring.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.spring.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {
    //커넥션 직접 연결 (계속 새로운 커넥션)
    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
        Connection con2  = DriverManager.getConnection(URL, USER_NAME, PASSWORD); //커넥션 얻을 때마다 파라메터 넘김
        //여기까지 실제 커넥션 2개 얻게 됨
        log.info("connectin={}, class={}", con1, con1.getClass());
        log.info("connectin={}, class={}", con2, con2.getClass());// 실제 db에서 커넥션을 가져오게 되고 그것에 대한 구현 클래스 org.h2.jdbc.JdbcConnection가 나옴
    }

    //스프링이 제공하는 데이터 소스가 적용된 드라이버 매니저 사용_드라이버 매니저 데이터 소스
    @Test
    void dataSoureDriverManager() throws SQLException {
        //DriverManagerDataSource - 항상 새로운 커넥션을 획득한다.
        //DriverManagerDataSource는 DataSource 구현하고 있다. 따라서 DataSource로 받을 수 있다.
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USER_NAME, PASSWORD); // 생성 시에만 파라메터 넘김
        // Spring 프래임워크의 JDBC 데이터 소스라고 되어있다. -> 즉 스프링에서 제공한다.
        useDataSource(dataSource);
    }

    private void useDataSource(DataSource dataSource) throws SQLException { //driverManager 테스ㅌ, 코드와 차이점은 DataSource라는 인터페이스를 통해서 가져온다는 점이다.
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection(); //close안하면 활성화된거로 뜬다.
        log.info("connectin={}, class={}", con1, con1.getClass());
        log.info("connectin={}, class={}", con2, con2.getClass());
    }
}
