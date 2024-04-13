package com.spring.jdbc.service;

import com.spring.jdbc.domain.Member;
import com.spring.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV3_2 {
    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepositoryV3; // 트랜잭션 동기화 매니저 사용

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepositoryV3) {
        //이렇게 해서 트렌젝션 매니저를 의존관계 주입받고, 내부에서는 트랜잭션 템플릿을 쓰는 것이다.
        //txTemplate이 transactionManager를 안에 가지고 있다.(=한 번 감싸고 있다.) 그래서 transactionManager와 관련된 일을 TransactionTemplate이ㅣ 수행한다고 보면 된다.
        this.txTemplate = new TransactionTemplate(transactionManager); // TransactionTemplate을 사용하려면, TransactionManager가 필요하다. 따라서 이렇게 주입받는 식으로 많이 사용한다.
        //TransactionTemplate을 밖에서 bean으로 등록해두고 TransactionTemplate을 주입받아서 사용해도 된다.
        //하지만 이렇게 쓰는 이유 중 하나는 TransactionTemplate은 class라서 유연성이 없다.
        // TransactionTemplate을 생성자 주입하면 유연성이 없지만, PlatformTransactionManager를 두면 어 유연성이 생김 -> 다른 거로 바꿀 수도 있음(관례로 굳어진 것도 있다.)

        this.memberRepositoryV3 = memberRepositoryV3;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //executeWithoutResult 실행 시 이 코드 안에서 트랜잭션 시작, 그 다음에 비즈니스 로직 수행
        // 이 코드 안에서 정상 동작 시 commit, 아니면 rollback
           // 여기 status가 TransactionStatus임
            //비즈니스 로직 시작
            try {
                bizLogic(fromId, toId, money); // 람다에서는 exception 던질 수 없으니까 try catch로 잡음
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
    }

    //commant + f6 -> 파라메터 순서 바꿀 수 있는 단축
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepositoryV3.findById(fromId);
        Member toMember = memberRepositoryV3.findById(toId);

        memberRepositoryV3.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepositoryV3.update(toId, toMember.getMoney() + money);
    }

    /* option + command + m -> 함수로 extract*/
    private static void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
