package com.spring.jdbc.service;

import com.spring.jdbc.connection.ConnectionConst;
import com.spring.jdbc.domain.Member;
import com.spring.jdbc.repository.MemberRepositoryV1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static com.spring.jdbc.connection.ConnectionConst.URL;
import static com.spring.jdbc.connection.ConnectionConst.USER_NAME;
import static com.spring.jdbc.connection.ConnectionConst.PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 기본동작, 트렌젝션 없어서 문제 발생
 */
class MemberServiceV1Test { //테스트는 public 없어도 됨
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV1 memberRepositoryV1;
    private MemberServiceV1 memberServiceV1;

    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USER_NAME, PASSWORD);
        memberRepositoryV1 = new MemberRepositoryV1(dataSource);
        memberServiceV1 = new MemberServiceV1(memberRepositoryV1);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepositoryV1.delete(MEMBER_A);
        memberRepositoryV1.delete(MEMBER_B);
        memberRepositoryV1.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepositoryV1.save(memberA);
        memberRepositoryV1.save(memberB);

        //when
        memberServiceV1.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then
        Member findMemberA = memberRepositoryV1.findById(memberA.getMemberId());
        Member findMemberB = memberRepositoryV1.findById(memberB.getMemberId());

        assertThat(memberA.getMoney()).isEqualTo(8000); //assertj꺼가 편함
        assertThat(memberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000); //AfterEach로 실행마다 delete하지 않으면 pk 때문에 같은 member에 대해, 즉,  같은 test 2번 실행 못 함
        Member memberEX = new Member(MEMBER_EX, 10000);
        memberRepositoryV1.save(memberA);
        memberRepositoryV1.save(memberEX);

        //when
        assertThatThrownBy(()
                -> memberServiceV1.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepositoryV1.findById(memberA.getMemberId());
        Member findMemberB = memberRepositoryV1.findById(memberEX.getMemberId());

        assertThat(memberA.getMoney()).isEqualTo(8000);
        //memberServiceV1 accountTransfer에서 autocommit 모드라 memberA만 바뀜
        assertThat(memberEX.getMoney()).isEqualTo(10000);
    }
}
