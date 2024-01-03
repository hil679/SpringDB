package com.spring.jdbc.connection;

public abstract class ConnectionConst { // 상수를 모아놓았기에 객체 생성하면 안됨. 따라서 생성 못하게 추상 클래스
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USER_NAME = "sa";
    public static final String PASSWORD = "";
}
