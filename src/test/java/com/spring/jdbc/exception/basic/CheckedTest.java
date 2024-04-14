package com.spring.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class CheckedTest {
    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_throw() { // 그냥 throws MyCheckedException하면 exception 던져지면서 test 실패
        Service service = new Service();

        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyCheckedException.class);
    }

    static class MyCheckedException extends Exception { // Exception 상속받으면 Check예외가 된다.
        /**
         * exception을 상속받은 예외는 체크 예외가 된다. (컴파일러가 체크)
         */
        public MyCheckedException(String message) { // 여러 생성자 존재, 그 중 메세지만 넘기는 거
            super(message);
        }
    }

    /*
    * Checked 예외는
    * 예외를 잡아서 처리하거나, 던지거나 둘중 하를 필수로 선택해야 한다.
     */
    //service와 repository 가상의 코드 작성
    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyCheckedException e) {
                log.info("예외 처리, message = {}", e.getMessage(), e); // exception자체를 출력 시 {} 필요 없음
            }
        }

        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지않고 밖으로 던지려면 throws 예외를 메서드에 필수로 선언해야 한다.
         */
        public void callThrow() throws MyCheckedException{
            repository.call();
        }
    }

    static class Repository {
        public void call() throws MyCheckedException { // throws MyCheckedException를 compiler가 check
            throw new MyCheckedException("ex");
        }
    }
}
