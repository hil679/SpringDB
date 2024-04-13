package com.spring.jdbc.service;

import com.spring.jdbc.domain.Member;
import com.spring.jdbc.repository.MemberRepositoryV2;
import com.spring.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

//이렇게 datasource직접 가져다 쓰는게 문제! -> JDBC관련된 거 사용, 즉, JDBC에 종속적이다.
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {
//    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager; // = new DataSourceTransactionManager();
    // 이렇게 직접 JDBC 트랜잭션 매니저 구현체로 대입 시 DI제대로 안 됨(OCP 잘 안지켜짐) -> 따라서 외부에서 주입받는다.

    private final MemberRepositoryV3 memberRepositoryV3; // 트랜잭션 동기화 매니저 사용

    public void accountTransfer(String fromId, String toId, int money) throws SQLException { //원래 예외 잘 던지지 않음, 지금은 간편히 던짐
        //트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());//파라미터로 transaction definition이라는 트랜잭션 관련된 속성 넣어줘야함
        try{

            //비즈니스 로직 시작
            bizLogic(fromId, toId, money);
            transactionManager.commit(status); //성공 시 커밋

        } catch (Exception e) {
            transactionManager.rollback(status); //실패 시 롤백
            throw new IllegalStateException(e);
        }
//       release 필요 없음, commit or rollback 시(더 이상 트랜잭션 필요 없음)에 알아서 트랜잭션 매니저가 release함

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
