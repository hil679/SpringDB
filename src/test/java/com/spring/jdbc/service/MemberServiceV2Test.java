package com.spring.jdbc.service;

import com.spring.jdbc.domain.Member;
import com.spring.jdbc.repository.MemberRepositoryV1;
import com.spring.jdbc.repository.MemberRepositoryV2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static com.spring.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 기본동작, 트렌젝션 없어서 문제 발생
 */
@Slf4j
class MemberServiceV2Test { //테스트는 public 없어도 됨
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV2 memberRepositoryV2;
    private MemberServiceV2 memberServiceV2;

    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USER_NAME, PASSWORD);
        memberRepositoryV2 = new MemberRepositoryV2(dataSource);
        memberServiceV2 = new MemberServiceV2(dataSource, memberRepositoryV2);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepositoryV2.delete(MEMBER_A);
        memberRepositoryV2.delete(MEMBER_B);
        memberRepositoryV2.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepositoryV2.save(memberA);
        memberRepositoryV2.save(memberB);

        //when
        log.info("Start TX");
        memberServiceV2.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("end TX");

        //then
        Member findMemberA = memberRepositoryV2.findById(memberA.getMemberId());
        Member findMemberB = memberRepositoryV2.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000); //assertj꺼가 편함
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000); //AfterEach로 실행마다 delete하지 않으면 pk 때문에 같은 member에 대해, 즉,  같은 test 2번 실행 못 함
        Member memberEX = new Member(MEMBER_EX, 10000);
        memberRepositoryV2.save(memberA);
        memberRepositoryV2.save(memberEX);

        //when
        assertThatThrownBy(()
                -> memberServiceV2.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepositoryV2.findById(memberA.getMemberId());
        Member findMemberB = memberRepositoryV2.findById(memberEX.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        //memberServiceV2는 exception이 터져 rollback하니까 A의 돈이 바뀌지 않음
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}
