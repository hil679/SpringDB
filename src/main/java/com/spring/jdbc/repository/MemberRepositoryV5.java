package com.spring.jdbc.repository;

import com.spring.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC template 사용 -> connection 동기화도 알아서 다 해줌
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository{
    private final JdbcTemplate jdbcTemplate;

    public MemberRepositoryV5(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";
        jdbcTemplate.update(sql, member.getMemberId(), member.getMoney());

        return member;
/*
jdbcTemplate.update에서 아래 connection받고 executeUpdate같이 실행하고 예외 변환까지 다 해줌
 */
//        Connection con = null;
//        PreparedStatement pstmt = null;
//
//        try {
//            con = getConnection();
//            System.out.println("save");
//            pstmt = con.prepareStatement(sql);
//            pstmt.setString(1, member.getMemberId());
//            pstmt.setInt(2, member.getMoney());
//            pstmt.executeUpdate();
//            return member;
//        } catch (SQLException e) {
//            throw exTranslator.translate("save", sql ,e); // 이거 한 줄로 스프링이 알아서 필요한 예외로 바꿔줌, DataAccessException
//        } finally {
//            close(con, pstmt, null);
//        }
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        Member member = jdbcTemplate.queryForObject(sql, memberRowMapper(), memberId);
        return member;
        // 한 건 조회는 queryForObject 사용
        // memberRowMapper -> 쿼리의 결과를 어떻게 멤버로 만들건가에 대한 정보를 mapping정보로 넣어줘야한다.
        //마지막에 파라미터 넣어줌

    }

    private RowMapper<Member> memberRowMapper() {
        return (resultSet, rowNum) -> {
            Member member = new Member();
            member.setMemberId(resultSet.getString("member_id"));
            member.setMoney( resultSet.getInt("money"));
            return member;
        };
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money = ? where member_id = ?";
        jdbcTemplate.update(sql, money, memberId);
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id = ?";
        jdbcTemplate.update(sql, memberId);
    }

}
