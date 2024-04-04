package com.spring.jdbc.repository;

import com.spring.jdbc.domain.Member;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static com.spring.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach
    void beforeEach() {
        //기본 DriverManager - 항상 새로운 커넥션을 획득
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USER_NAME, PASSWORD);

        //커넥션 풀링 - Hikari 이용
        HikariDataSource dataSource = new HikariDataSource();
        // HikariDataSource 대신 DataSource dataSource를 적으면
        // DataSource에는 setJdbcUrl이런 인터페이스가 없기 때문에 설정이 안돼서 오류남
        // 따라서 객체 생성 시에는 구체적인 타입을 받아야하고, 의존관계 주입에서는 DataSource로 받으면 된다.
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USER_NAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource);
    }

    @Test
    void crud() throws SQLException {
        //save
        Member member = new Member("memberV100", 10000);
        repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember = {}", findMember);
        assertThat(member).isEqualTo(findMember); // 내부에서 equals를 써서 비교

        //update: money 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updateMember = repository.findById(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(20000);

        //delete
        repository.delete(member.getMemberId());
        // 테스트 데이터를 지우고 다시 테스트가 가능하도록 하는 것이 중요하지만 이건 좋은 방법은 아니다.
        // 위에서 예외가 터지면 결국 지워지지 않고 디비에는 남아있기 때문이다.
        // 또한 내가 사용한 데이터로 인해 다른 테스트에 영향을 줄 수 있기 때문이다. (내가 사용한 데이터를 다른사람도 똑같이 테스트하려고 할 수 있다.)
        assertThatThrownBy(() -> repository.findById(member.getMemberId())) //findById에서 없으면 NoSuchElementException에러 터지게 작업했음
                .isInstanceOf(NoSuchElementException.class);

        try {
            Thread.sleep(1000); // log보려고
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}