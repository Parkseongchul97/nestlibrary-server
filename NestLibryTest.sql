select * from user;
update user
set user_point = 3000
where user_email = "asd3";
insert into user
values("asd123@naver.com", "123", "커피왕", "대충이미지주소.jpg" , "반가월");

select * from channel;
insert into channel(channel_name,  channel_img_url)
values("바리스타 채널",  "대충이미지주소.jpg" );


select * from channel_tag;
insert into channel_tag(channel_tag_name,  channel_code)
values("공지",  1),("일반",  1),("리뷰",  1);

select * from comment;
select * from management;
select * from messages -- 받은사람
where messages_to_user = "asd";

delete from messages
where messages_code = 35;
select * from messages -- 보낸사람
where messages_from_user = "asd";
-- 채널코드 6의 인기글
select * 
from post 
where channel_code = 6; -- 22개

-- 33 78
update messages
set messages_title = "숭배하라"
where messages_code = 55;
SELECT 
	post_title
    post_views, 
    post_like_code, 
    comment_code, 
    (post_views * 0.5 + count(post_like_code) * 0.3 + count(comment_code) * 0.2) AS best 
FROM post 
join post_like on (post.post_code = post_like.post_code)
join comment on (post.post_code = comment.post_code)
group by post_like_code, comment_code, post_views
ORDER BY best DESC ;

select  post.post_code, post_views, count(*),  count(post_like_code)
from post
left join post_like on (post.post_code = post_like.post_code)
left join comment on (post.post_code = comment.post_code)
where post.channel_code = 6
group by post.post_code, post_views, comment_code, post_like_code;

-- 채널 6의 댓글수 
select post_code, post_title, count(post_code) as "댓글수"
from comment
join post USING (post_code)
where channel_code = 6
group by post_code;

-- 채널 6의 게시글의 추천수
SELECT p.post_code,p.post_title, COUNT(pl.post_like_code) AS like_count
FROM post p
JOIN post_like pl ON p.post_code = pl.post_code
where channel_code = 6
GROUP BY p.post_code
ORDER BY like_count DESC;

SELECT p.post_code ,p.post_title,
	p.post_views as 조회수,
    COUNT(DISTINCT c.comment_code) AS 댓글수, 
    COUNT(DISTINCT pl.post_like_code) AS 추천수,
    (COUNT(DISTINCT c.comment_code) * 2 + COUNT(DISTINCT pl.post_like_code) *5 + p.post_views) 점수
FROM post p
LEFT JOIN comment c ON p.post_code = c.post_code
LEFT JOIN post_like pl ON p.post_code = pl.post_code
WHERE p.channel_code = 6
GROUP BY p.post_code, p.post_title, p.post_views
ORDER BY 6 desc;

select * 
from channel
left join post on(channel.channel_code = post.channel_code)
left join post_like on (post.post_code = post_like.post_code)
left join comment on (post.post_code = comment.post_code);


select * from post where channel_code = 15; -- 182개
select * from channel; -- 15

select * from post_like;
insert into post (post_title, post_content, user_email, channel_code, channel_tag_code)
values("제목1", "제목1", "asd",


 15, 40);
 
 select * from channel_tag; -- channel_code (얘는 전부)
 select * from post; -- channel_code --> 10개씩 최근 기준 (post_created_at) <--- 채널 태그가 필요한 것   
 
 select * from post 
 join channel_tag 
 using (channel_tag_code) 
 where post.channel_code = 15
 order by post_created_at desc
 limit 10;
 select * from post
 where post_code = 95;
 select * from channel; -- 6번 - 원피스, 15번 - 채널 테스트 8
 select * from management;
 select * from management where management_user_status = 'sub'; -- channel_code, management_user_status : 'sub' 인 애들 
 
 select * from channel left join management using(channel_code);
 select * from channel join management using(channel_code);
 
 SELECT 
    p.*,
    u.*,
    (COUNT(DISTINCT CASE WHEN c.comment_parents_code = 0 THEN c.comment_code END) * 2 + COUNT(DISTINCT pl.post_like_code) * 5 + p.post_views) AS best_score
FROM post p
LEFT JOIN comment c ON p.post_code = c.post_code
LEFT JOIN post_like pl ON p.post_code = pl.post_code
LEFT JOIN user u on(p.user_email = u.user_email)
WHERE p.channel_code = 6
GROUP BY p.post_code, p.post_title, p.post_views
HAVING best_score > 50
ORDER BY post_created_at DESC
limit 1 ;

 
 select * from user;
 update user 
 set user_point = user_point + 5000;
 
 
 
 
 
 
 
 
 
 
 