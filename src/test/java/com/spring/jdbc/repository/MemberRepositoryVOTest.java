package com.spring.jdbc.repository;

import com.spring.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryVOTest {

    MemberRepositoryVO repository = new MemberRepositoryVO();

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
    }
}