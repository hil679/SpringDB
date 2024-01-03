package com.spring.jdbc.repository;

import com.spring.jdbc.connection.DBConnectionUtil;
import com.spring.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용(굉장히 Low level)
 * 나중에는 실제로 쓸 일이 없다.
 */
@Slf4j // log를 남기기 위해
public class MemberRepositoryVO {
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try { // SQLException이 올라오기에 try-catch로 묶어두어야 한다.
            con = getConnection(); // 커넥션 획득
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());//파라메터 바인딩 해주기
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); // 위의 준비된 쿼리가 실제 데이터베이스에 실행된다. 영향 받은 row의 수가 반환된다.
            return member;
        } catch (SQLException e) {
            log.error("db error", e); // 로그 남기고
            throw e; //cathc한 예외를 다시 던짐
        } finally { // 이게 중요
            close(con, pstmt, null); // 꼭 finally에서 close를 해주어야 한다.
            // 왜냐하면 try에서 예외 발생시 예외가 아래 catch로 넘어가 버리기 때문
            // 그럼 close가 호출이 안 된다.
            //옛날이나 순서가 중요하지 지금은 다른 기술 사용

            /*
            pstmt.close()에서 문제가 생겨서 안 닫히고 exception이 터지면 밖으로 나가버리게 될 것이다.
            그러면 con.close() 이게 호출이 안되는 문제가 발생할 수 있다.
            따라서 if문 없이
            pstmt.close(); // 역순으로 닫기
            con.close();
            이렇게 나열하고 쓰면 안된다.
             */
        }
    }

    public Member findById(String memberId) throws SQLException{
        String sql = "select * from member where member_id = ?";

        Connection con = null; // try catch 후 finally에서 사용해야해서 밖에서 이렇게 선언할 수 밖에 없다.
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DBConnectionUtil.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();//executeUpdate: data 변경 시, executeQuery: select할 때

            //rs가 처음에는 아무데도 가리키지 않는다.
            //next를 호출하면 데이터가 있는지 없는지 확인
            //있으면 true, 없으면 false
            if(rs.next()) { // 처음 rs.next해줘야 rs내부의 커서가 실제 데이터가 있는 곳으로 이동됨
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else { //data 없으면
                throw new NoSuchElementException("member not found memberId = " + memberId); // key값 남기는게 좋다.
            }
        } catch (SQLException e) {
            log.info("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }

    }

    /**
     * Statement: sql을 바로 넣는 것이다.
     * PreparedStatement: 파라미터(위의 ?)를 바인딩 할 수 있는 것이다.
     * 따라서 기능이 더 많고, Statement를 상속받았다.
     */
    private void close(Connection con, Statement stmt, ResultSet rs) { // 사용 리소스 모두 닫기
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("error", e);            }
        }
        if (stmt != null) {//코드의 안정성을 위해
            try {
                stmt.close();//null이 아니면 close 해야하는데, close시 checkedException을 또 날려서 try catch가 필요
            } catch (SQLException e) {
                log.error("error", e); // 여기서 문제 시 닫을 때 오류가 터진 것이기에 크게 할 수 있는 것은 없기에 log남김
            }
        }

        if (con != null) { // 위의 stmt의 예외 발생이 아래 con을 close하는 코드에 더이상 영향을 주지 않는다.
            try {
                con.close(); // 외부 리소스를 쓰는 것(실제 TCP, IP를 걸려 쓰는 것)이기 때문에 안 닫아주면 계속 떠다닌다.
            } catch (SQLException e) {
                log.error("error", e);
            }
        }
    }

    private static Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}

/*
option + cmd + m -> 메소드 추출
클래스명 위에서 cmd + shift + t -> 테스트 코드 구조 추가됨
 */