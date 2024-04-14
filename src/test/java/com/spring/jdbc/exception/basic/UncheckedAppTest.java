package com.spring.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UncheckedAppTest {
    @Test
    void checked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(RuntimeSqlException .class); // Exception.class 여도 test 성공
    }

    @Test
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
//            e.printStackTrace(); // 할 수는 있지만 좋지 않은 방법!!!!!
            log.info("ex", e);
        }
    }

    static class Controller {
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }

    static class Service {
        NetworkClient networkClient = new NetworkClient();
        Repository repository = new Repository();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }
    static class NetworkClient {
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }
    }
    static class Repository {
        public void call() {
            try {
                runSql();
            } catch (SQLException e) {
                // throw new RuntimeSqlException(); // 기 존 예외 trace 안 나옴, 안 넘겨줬기 때문
                throw new RuntimeSqlException(e); //예외를 던질 땐 기존 예외를 던져줘야 기존 트레이스까지 확인 가능
            }
        }

        public void runSql() throws SQLException {
            throw new SQLException("ex"); //jdbc쓰면 SQLException 무조건 터짐
        }

    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSqlException extends RuntimeException {
        public RuntimeSqlException() {
        }

        public RuntimeSqlException(Throwable cause) { //cause -> 왜 발생했는지 이전 예외를 같이 넣을 수 있다.
            super(cause);
        }
    }
}
