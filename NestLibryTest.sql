select * from user;
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
select * from messages;
select * 
from post 
where channel_code = 6; -- 22개

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
 
 select * from channel; -- 6번 - 원피스, 15번 - 채널 테스트 8
 select * from management;
 select * from management where management_user_status = 'sub'; -- channel_code, management_user_status : 'sub' 인 애들 
 
 select * from channel left join management using(channel_code);
 select * from channel join management using(channel_code);