package com.spring.jdbc.service;

import com.spring.jdbc.domain.Member;
import com.spring.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static com.spring.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - @Transactional AOP
 */
@SpringBootTest // 여기서 스프링을 하나 띄움, 필요한 스프링 빈 다 등록함, 스프링 빈 의존관계 주입도 다 함
@Slf4j
class MemberServiceV3_3Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired // 의존 관계 주입 -> 하지만 스프링 빈 등록하지 않으면 의존관계 주입 안 됨, 주입해줘야 함 -> 따라서 @Before에 있는 거 그 전에 빈 등록이 먼저!
    private MemberRepositoryV3 memberRepository;
    @Autowired
    private MemberServiceV3_3 memberService;

    @TestConfiguration // for 빈 등록 -> 안 할 시 트랜잭션 적용 안 되는 오류 (A: 8000. Ex: 10000 -> 기대(테스트 성공)는 둘 다 10000)
    static class TestConfig {  // 프록시에서 가져다가 사용
        // 트랜잭션 AOP 쓰려면 dataSource, transactionManager 모두 필요
        @Bean
        DataSource dataSource() { // 스프링 생태계(컨테이너) 안에서 스프링 빈에 등록됨
            //dataSource는 transactionManager과 Repository에서 필요하기 때문에 등록해줘야 함
            return new DriverManagerDataSource(URL, USER_NAME, PASSWORD); // datasource 다 주입 받아서 사용 가능
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            // proxy에서는 트랜잭션 시작하려 트랜잭션 매니저 불러서 사용해야 함
            // 따라서 프록시가 transactionManager 주입받아서 쓸거임
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource());
        }
        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

//    @BeforeEach
//    void before() {
//        // 스프링 컨테이너를 쓰고있지 않음, DriverManagerDataSource를 직접 만들어 사용중
//        // 스프링 컨테이너에 스프링 빈을 쓰는게 아니고, 내가 원하는 것들만 넣어서 test하는게 됨
//        // 트렌젝션 AOP 사용하려면 스프링 AOP등 스프링이 제공하는 모든게 제공이 돼야 함
//        // 트랜잭션 쓰고 AOP 적용하려면 스프링 컨테이너에 스프링 빈이 다 등록해야 할 수 있다.
//        //그래야 스프링빈이 등록된거를 보고 원하는 것을 수행가능
//        //memberRepository, memberService를 직접 넣었지만 이제는 스프링 빈에 등록해서 사용하겠다.
//
//        // 스프링 빈 등록 방법
//        // 1. class에 @SpringBootTest
//
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USER_NAME, PASSWORD);
//        memberRepository = new MemberRepositoryV3(dataSource);
//
//        memberService = new MemberServiceV3_3(memberRepository);
//    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test // AOP프록시 생성 확인
    void AopCheck() {
        log.info("memberService class = {}", memberService.getClass());
        log.info("memberRepository class = {}", memberRepository.getClass());
        assertThat(AopUtils.isAopProxy(memberService)).isTrue(); // spring이 memberService코드 다 뒤저서 method나 class에 @Transactional있는지 모두 확인
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
