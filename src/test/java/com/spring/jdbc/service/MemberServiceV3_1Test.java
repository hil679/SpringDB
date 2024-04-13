package com.spring.jdbc.service;

import com.spring.jdbc.domain.Member;
import com.spring.jdbc.repository.MemberRepositoryV2;
import com.spring.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

import static com.spring.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 기본동작, 트렌젝션 없어서 문제 발생
 */
@Slf4j
class MemberServiceV3_1Test { //테스트는 public 없어도 됨
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_1 memberService;

    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USER_NAME, PASSWORD);
        memberRepository = new MemberRepositoryV3(dataSource);
        //serveceV3_1은 추상화된 플랫폼 트랜잭션 매니저를 쓰기 때문에 transactionManager을 넣어줘야 한다.
        //주의! 여기 파라미터로 datasource넘겨줘야 함

        // -> why?? 트랜잭션 시작 시 트랜잭션 매니저가 데이터 소스를 통해서 커넥션을 생성한다. ( 이 일도 트랜잭션 매니저가 함)
        // 그러면 데이터 소스가 트랜잭션 매니저에게 없으면 커넥션을 트랜잭션 매니저가 못 만든다.
        // 트랜잭션 매니저가  커넥션을 만들어야 트랜잭션을 시작하는데 애초애 datasource가 없어서 커넥션을 못만들고 있음,
        
        //따라서 datasource를 파라미터로 넘겨주지 않으면 오류 -> No DataSource set
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource); //JDBC와 관련!
        memberService = new MemberServiceV3_1(transactionManager, memberRepository);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        log.info("Start TX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("end TX");

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000); //assertj꺼가 편함
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000); //AfterEach로 실행마다 delete하지 않으면 pk 때문에 같은 member에 대해, 즉,  같은 test 2번 실행 못 함
        Member memberEX = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEX);

        //when
        assertThatThrownBy(()
                -> memberService.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEX.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        //memberServiceV2는 exception이 터져 rollback하니까 A의 돈이 바뀌지 않음
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}
