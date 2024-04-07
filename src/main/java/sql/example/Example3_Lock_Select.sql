-- init
 set autocommit true;
 delete from member;
 insert into member(member_id, money) values ('memberA',10000);

--for update
set autocommit false; -- 자동커밋모드면 의미없음
select * from member where member_id='memberA' for update; -- memberA row 변경 불가, lock 가져

-- session2 do Before session1 commit -> ERROR
set autocommit false;
update member set money=500 where member_id = 'memberA';

-- session1 commit
commit;

-- session2 do After session1 commit -> SUCCESS
set autocommit false;
update member set money=500 where member_id = 'memberA';

-- session2 commit
commit;