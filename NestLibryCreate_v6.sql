-- DROP SCHEMA testProject;
 
-- CREATE SCHEMA testProject;

CREATE TABLE user( -- 유저 테이블
	user_email VARCHAR(50) PRIMARY KEY, -- 유저 이메일아이디 
    user_password TEXT, -- 유저 비밀번호 (암호화)
    user_nickname VARCHAR(50) UNIQUE, -- 유저 닉네임 중복 X
    user_img_url TEXT, -- 유저 프로필 이미지 URL(서버에 저장할)
    user_info TEXT, -- 유저 자기소개란
    user_point INT DEFAULT 0 -- 유저 포인트 (채널 생성 등에 사용)
);

CREATE TABLE channel( --  유저가 생성한 채널
	channel_code INT AUTO_INCREMENT PRIMARY KEY, -- 채널 코드
    channel_name VARCHAR(50) UNIQUE, -- 채널 이름 
    channel_created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 채널 생성일
	channel_img_url TEXT, -- 채널 대문사진
    channel_info TEXT -- 채널 소개문
);


CREATE TABLE channel_tag( --  유저가 생성한 채널
	channel_tag_code INT AUTO_INCREMENT PRIMARY KEY,
    channel_tag_name VARCHAR(50), -- 채널 게시판 이름
	channel_code INT -- 채널 코드
);

ALTER TABLE channel_tag ADD  FOREIGN KEY (channel_code) REFERENCES channel(channel_code)
 ON DELETE CASCADE
 ON UPDATE CASCADE; -- 채널과 채널 태그 참조

CREATE TABLE management( --  채널 관리
	management_code INT AUTO_INCREMENT PRIMARY KEY, -- 관리 코드
    management_user_status VARCHAR(10) , -- 유저 상태 (host, admin ,ban, sub)
    management_delete_at DATETIME, -- 삭제 예정일(벤 관련)
	channel_code INT,
    user_email VARCHAR(50) -- 대상 유저
);
ALTER TABLE management ADD  FOREIGN KEY (user_email) REFERENCES user(user_email)
 ON DELETE CASCADE
 ON UPDATE CASCADE; -- 유저 삭제시 자동삭제
 
 ALTER TABLE management ADD  FOREIGN KEY (channel_code) REFERENCES channel(channel_code)
 ON DELETE CASCADE
 ON UPDATE CASCADE; -- 채널 삭제시 자동삭제
 
ALTER TABLE channel_tag ADD  FOREIGN KEY (channel_code) REFERENCES channel(channel_code)
 ON DELETE CASCADE
 ON UPDATE CASCADE; -- 채널 삭제시 자동삭제

CREATE TABLE post( -- 게시글 테이블 
	post_code INT AUTO_INCREMENT PRIMARY KEY, -- 게시판 코드
    post_title TEXT, -- 게시글 제목
    post_content LONGTEXT, -- 게시글 내용
    post_created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 게시글 작성 시간
    post_views INT DEFAULT 0 ,-- 조회수

    user_email VARCHAR(50), -- 게시글 작성자
    channel_code INT, -- 채널 코드
    channel_tag_code INT -- 채널 세부 게시판태그
);

ALTER TABLE post ADD  FOREIGN KEY (user_email) REFERENCES user(user_email)
 ON DELETE CASCADE
 ON UPDATE CASCADE; -- 게시글 -> 유저  참조
--  ALTER TABLE post ADD  FOREIGN KEY (channel_code) REFERENCES channel(channel_code); -- 게시글 -> 채널 참조
ALTER TABLE post ADD  FOREIGN KEY (channel_tag_code) REFERENCES channel_tag(channel_tag_code)
 ON DELETE CASCADE
 ON UPDATE CASCADE;-- 게시글 -> 채널태그 참조
-- CASCADE 옵션 추가인데 잘 돌아갈지 모르겠음

CREATE TABLE post_like(-- 게시글 추천 테이블
	post_like_code INT AUTO_INCREMENT PRIMARY KEY,  -- 추천 코드

    post_code INT,  -- 대상 게시판 코드
	user_email VARCHAR(50) -- 추천, 비추천을 누른사람
);

ALTER TABLE post_like ADD  FOREIGN KEY (user_email) REFERENCES user(user_email)
 ON DELETE SET NULL; -- 좋아용 -> 유저  참조
ALTER TABLE post_like ADD  FOREIGN KEY (post_code) REFERENCES post(post_code)
 ON DELETE CASCADE
 ON UPDATE CASCADE; -- 좋아용 -> 게시글 참조

CREATE TABLE comment( -- 댓글 테이블 
	comment_code INT AUTO_INCREMENT PRIMARY KEY, -- 댓글 코드
    comment_content LONGTEXT, -- 댓글 내용
    comment_created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 댓글 작성시간

    comment_parents_code INT, -- 부모 댓글 코드 (있을수도 없을수도)
    post_code INT, -- 게시판 코드(어느 글에 달린 댓글인지)
    user_email VARCHAR(50) -- 댓글 작성자
);

ALTER TABLE comment ADD  FOREIGN KEY (user_email) REFERENCES user(user_email)
ON DELETE SET NULL; -- 댓글 -> 유저  참조 유저 삭제시 유저 널처리
ALTER TABLE comment ADD  FOREIGN KEY (post_code) REFERENCES post(post_code)
 ON DELETE CASCADE
 ON UPDATE CASCADE; -- 댓글 -> 게시글 참조
-- 대댓글도 관리상의 이유로 참조 X

CREATE TABLE messages( -- 쪽지
	messages_code INT AUTO_INCREMENT PRIMARY KEY, -- 쪽지 코드
    messages_title TEXT, -- 쪽지 제목
    messages_content LONGTEXT, -- 쪽지 내용
    messages_sent_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 쪽지를 보낸시간
    messages_read BOOLEAN, -- 쪽지 조회 여부 (알림기능용도)
    messages_from_user VARCHAR(50), -- 발신자 
    messages_to_user VARCHAR(50), -- 수신자
    messages_from_delete INT default(0), -- 삭제여부 수신자 삭제, 발신자 삭제
	messages_to_delete INT default(0) -- 삭제여부 수신자 삭제, 발신자 삭제
);

CREATE TABLE push( -- 알림
	push_code INT AUTO_INCREMENT PRIMARY KEY, -- 쪽지 코드
	user_email VARCHAR(50), -- 알림 대상자
    post_code int, -- 링크용 글코드
    push_massage TEXT, -- 푸쉬알람 메시지
     channel_code INT default(0),
    push_created_at DATETIME DEFAULT CURRENT_TIMESTAMP -- 알림 생성 시간
    -- isRead boolean default(false)   -- // 읽었냐?
);

-- select * from information_schema.table_constraints
-- where CONSTRAINT_SCHEMA = 'nest';
--  ALTER TABLE push drop column channelCode;

 alter table push add column  channel_code INT default(0);
-- 쪽지는 관리 편하려고 참조 X  from , to 둘다 유저 eamil 참조
select * from push;
select * from user;

-- 알림 삭제
CREATE EVENT remove_push
ON SCHEDULE EVERY 5 minute
DO
  DELETE FROM push
  WHERE push_created_at IS NOT NULL
    AND push_created_at < NOW() - INTERVAL 3 day;
    
-- 벤 삭제    
CREATE EVENT remove_ban
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_DATE + INTERVAL 1 DAY
DO
  DELETE FROM management
  WHERE management_delete_at IS NOT NULL
    AND management_delete_at <= NOW()
    AND management_user_status = "ban";
    
    SHOW EVENTS ;  

