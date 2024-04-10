package com.spring.jdbc.service;

import com.spring.jdbc.domain.Member;
import com.spring.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {
    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepositoryV2;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException { //원래 예외 잘 던지지 않음, 지금은 간편히 던짐
        //비즈니스 로직 제외하고는 커넥션 처리를 위한 코드임
        //순수 비즈니스 로직과 커넥션 처리 코드가 섞여있음, 한 군데 섞여있으면 어지럽
        //순수 비즈니스 로직은 bizlogic으로 뺀다.
        Connection con = dataSource.getConnection();
        try{
            con.setAutoCommit(false); //트랜잭션 시작, set autocommit false 명령어를 DB에 날림

            //비즈니스 로직 시작
            bizLogic(con, fromId, toId, money);
            //커밋 또는 롤백
            // commit 명령어가 connection을 통해서 정상적으로 db에 반영, 세션이 전달하고 세션이 commit실행하게 됨
            con.commit(); // 성공 시 커밋

        } catch (Exception e) {
            con.rollback();
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }
    }

    //commant + f6 -> 파라메터 순서 바꿀 수 있는 단축
    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepositoryV2.findById(con, fromId);
        Member toMember = memberRepositoryV2.findById(con, toId);

        memberRepositoryV2.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepositoryV2.update(con, toId, toMember.getMoney() + money);
    }

    private static void release(Connection con) {
        //JdbcUtils써도 되지만 한 가지 고려할게 있음 -> release!
        if(con != null) {
            try {
                //con.close() -> cloase하게 되면 풀에서 돌아감,
                // 즉, close만 하면 누군가 풀에서 이 커넥션을 가지게 되면 autocommit이 false인 상태를 가져감,
                // 원래 기본값은 true, 그런데 보통 autocommit true로 가정하기 때문에 문제가 발생할 가능성이 너무 높음
                // 따라서 autocommit도 true로 만들고 close 하자

                con.setAutoCommit(true); //커넥션 풀 고려
                con.close();
            } catch (Exception e) {
                log.info("error", e); // 그냥 error만 잡을 땐 "error error={}"이런거 안 하고 그냥 뒤에 붙이면 됨
            }
        }
    }

    /* option + command + m -> 함수로 extract*/
    private static void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
