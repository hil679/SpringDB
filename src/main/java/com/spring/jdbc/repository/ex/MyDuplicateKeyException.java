package com.spring.jdbc.repository.ex;

public class MyDuplicateKeyException extends MyDbException{ //런타임예외 상속받아도 되는데 DB 예외임을 카테고리로 묶을 수 있음

    public MyDuplicateKeyException() {
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
