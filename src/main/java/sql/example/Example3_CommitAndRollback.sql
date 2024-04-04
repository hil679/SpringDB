-- // commit
-- 데이터 초기화
set autocommit true;
delete from member;
insert into member(member_id, money) values ('oldId',10000);

-- 신규 데이터 추가 - commit 전
-- 트랜잭션 시작
set autocommit false; -- 수동 커밋 모드
insert into member(member_id, money) values ('newId1',10000);
insert into member(member_id, money) values ('newId2',10000); -- commit 전임

-- 커밋
commit;


-- //rollback
-- // commit
-- 데이터 초기화
set autocommit true;
delete from member;
insert into member(member_id, money) values ('oldId',10000);

-- 신규 데이터 추가 - commit 전
-- 트랜잭션 시작
set autocommit false; -- 수동 커밋 모드
insert into member(member_id, money) values ('newId1',10000);
insert into member(member_id, money) values ('newId2',10000); -- commit 전임

-- 여기서 세션1이 commit하지 않고 rollback을 한다.
rollback -- update 도 마찬가지로 다시 원래 데이터로 돌아가있다.

