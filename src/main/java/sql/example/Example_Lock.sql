set autocommit true;
delete from member;
insert into member(member_id, money) values ('memberA',10000);

set autocommit false;
update member set money=500 where member_id = 'memberA';



--session2
SET LOCK_TIMEOUT 60000;
set autocommit false;
update member set money=1000 where member_id = 'memberA';