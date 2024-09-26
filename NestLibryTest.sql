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
select * from post;
select * from post_like;