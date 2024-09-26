-- DROP SCHEMA testProject;
-- CREATE SCHEMA testProject;

CREATE TABLE user( -- 유저 테이블
	user_email VARCHAR(50) PRIMARY KEY, -- 유저 이메일아이디 
    user_password TEXT, -- 유저 비밀번호 (암호화)
    user_nickname VARCHAR(30) UNIQUE, -- 유저 닉네임 중복 X
    user_img_url TEXT, -- 유저 프로필 이미지 URL(서버에 저장할)
    user_info TEXT -- 유저 자기소개란
--  ,user_role VARCHAR(30) -- 유저 권한 (관리자, 일반회원) ? 일단 중요도 낮음
);

CREATE TABLE channel( --  유저가 생성한 채널
	channel_code INT AUTO_INCREMENT PRIMARY KEY, -- 채널 코드
    channel_name VARCHAR(50) UNIQUE, -- 채널 이름 
    channel_created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 채널 생성일
	channel_img_url text -- 채널 대문사진
);



CREATE TABLE channel_tag( --  유저가 생성한 채널
	channel_tag_code INT AUTO_INCREMENT PRIMARY KEY,
    channel_tag_name VARCHAR(50), -- 채널 게시판 이름 
	channel_code INT -- 채널 코드
);

ALTER TABLE channel_tag ADD  FOREIGN KEY (channel_code) REFERENCES channel(channel_code); -- 채널과 채널 태그 참조

CREATE TABLE management( --  채널 관리
	management_code INT AUTO_INCREMENT PRIMARY KEY, -- 관리 코드
    management_user_status VARCHAR(10) , -- 유저 상태 (host, admin ,ban, sub)
    management_delete_at DATETIME, -- 삭제 예정일(벤 관련)
	channel_img_url TEXT, -- 채널 대문사진
    user_email VARCHAR(50) -- 대상 유저
);
ALTER TABLE management ADD  FOREIGN KEY (user_email) REFERENCES user(user_email); -- 채널관리 -> 유저  참조
ALTER TABLE channel_tag ADD  FOREIGN KEY (channel_code) REFERENCES channel(channel_code); -- 채널태그 -> 채널 참조

CREATE TABLE post( -- 게시글 테이블 
	post_code INT AUTO_INCREMENT PRIMARY KEY, -- 게시판 코드
    post_title TEXT, -- 게시글 제목
    post_content LONGTEXT, -- 게시글 내용
    post_areated_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 게시글 작성 시간 
    post_views INT DEFAULT 0 ,-- 조회수
    user_email VARCHAR(50), -- 게시글 작성자
    channel_code INT, -- 채널 코드
    channel_tag_code INT -- 채널 세부 게시판태그
);

ALTER TABLE post ADD  FOREIGN KEY (user_email) REFERENCES user(user_email); -- 게시글 -> 유저  참조
--  ALTER TABLE post ADD  FOREIGN KEY (channel_code) REFERENCES channel(channel_code); -- 게시글 -> 채널 참조
ALTER TABLE post ADD  FOREIGN KEY (channel_tag_code) REFERENCES channel_tag(channel_tag_code); -- 게시글 -> 채널태그 참조

CREATE TABLE post_like(-- 게시글 추천 테이블
	post_like_code INT AUTO_INCREMENT PRIMARY KEY,  -- 추천 코드
    post_code INT,  -- 대상 게시판 코드
	user_email VARCHAR(50) -- 추천, 비추천을 누른사람
);

ALTER TABLE post_like ADD  FOREIGN KEY (user_email) REFERENCES user(user_email); -- 좋아용 -> 유저  참조
ALTER TABLE post_like ADD  FOREIGN KEY (post_code) REFERENCES post(post_code); -- 좋아용 -> 게시글 참조

CREATE TABLE comment( -- 댓글 테이블 
	commnet_code INT AUTO_INCREMENT PRIMARY KEY, -- 댓글 코드
    comment_content LONGTEXT, -- 댓글 내용
    comment_created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 댓글 작성시간
    comment_parents_code INT, -- 부모 댓글 코드 (있을수도 없을수도)
    post_code INT, -- 게시판 코드(어느 글에 달린 댓글인지)
    user_email VARCHAR(50) -- 댓글 작성자
);

ALTER TABLE comment ADD  FOREIGN KEY (user_email) REFERENCES user(user_email); -- 댓글 -> 유저  참조
ALTER TABLE comment ADD  FOREIGN KEY (post_code) REFERENCES post(post_code); -- 댓글 -> 게시글 참조
-- 대댓글도 관리상의 이유로 참조 X

CREATE TABLE messages( -- 쪽지
	messages_code INT AUTO_INCREMENT PRIMARY KEY, -- 쪽지 코드
    messages_title TEXT, -- 쪽지 제목
    messages_content LONGTEXT, -- 쪽지 내용
    messages_sent_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 쪽지를 보낸시간
    messages_read BOOLEAN, -- 쪽지 조회 여부 (알림기능용도)
    messages_from_user VARCHAR(50), -- 발신자 
    messages_to_user VARCHAR(50) -- 수신자
);

-- 쪽지는 관리 편하려고 참조 X  from , to 둘다 유저 eamil 참조
