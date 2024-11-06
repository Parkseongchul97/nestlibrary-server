package com.server.nestlibrary.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.BoardDTO;
import com.server.nestlibrary.model.dto.PostDTO;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class PostService {

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private PostLikeDAO likeDAO;

    @Autowired
    private UserDAO userDAO;
    @Lazy
    @Autowired
    private CommentService commentService;

    @Autowired
    private ChannelTagDAO tagDAO;

    @Autowired
    private ChannelDAO channelDAO;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QPostLike qPostLike = QPostLike.postLike;
    private final QPost qPost = QPost.post;
    private final QUser qUser = QUser.user;
    private final QComment qComment = QComment.comment;
    private final QChannelTag qChannelTag = QChannelTag.channelTag;

    public Post postCodeByPost(int postCode) {
        return postDAO.findById(postCode).orElse(null);
    }
    // 해당 채널의 태그 or 모든 게시글 출력, 카운트 쿼리
    public JPAQuery<Post> postQuery (int code, String target, String keyword, Paging paging, boolean isTag){
        JPAQuery<Post> query = queryFactory.selectFrom(qPost)
                .join(qUser).on(qPost.userEmail.eq(qUser.userEmail));
        if(isTag){ // 해당 태그 출력
            query.where(qPost.channelTag.channelTagCode.eq(code));

       }else{ // 채널 게시글 출력
            query.where(qPost.channel.channelCode.eq(code));
       }

        if (target != null && !target.equals("") && keyword != null && !keyword.equals("")) {
            if (target.equals("title")) { // 제목이 포함된게시글
                query.where(qPost.postTitle.containsIgnoreCase(keyword));
            } else if (target.equals("content")) {// 내용이 포함된게시글
                query.where(qPost.postContent.containsIgnoreCase(keyword));
            } else if (target.equals("user")) { // 작성자가
                query.where(qUser.userNickname.containsIgnoreCase(keyword));

            }
        }
        if (paging != null) { // 페이징이 있다면 게시글 반환할때
            query.orderBy(qPost.postCreatedAt.desc()) // 최신순으로
                    .offset(paging.getOffset()) //
                    .limit(paging.getLimit()); //10개씩

        } else { // 없다면 카운트 반환할때?
            query.orderBy(qPost.postCreatedAt.desc()); // 최신순으로

        }
        return query;
    }

    // 공지 출력
    public List<PostDTO> channelAnnouncement(int channelCode) {
        List<Post> voList = queryFactory.selectFrom(qPost)
                .join(qChannelTag).on(qPost.channelTag.channelTagCode.eq(qChannelTag.channelTagCode))
                .where(qPost.channelTag.channelTagName.eq("공지"))
                .where(qPost.channel.channelCode.eq(channelCode))
                .orderBy(qPost.postCreatedAt.desc())
                .limit(3).fetch();
        List<PostDTO> dtoList = new ArrayList<>();
        for (Post p : voList) {
            dtoList.add(postVoChangeDTO(p));
        }
        return dtoList;
    }

    // 해당 채널의 전체 글
    public List<PostDTO> channelCodeByAllPost(int channelCode, Paging paging, String target, String keyword) {
        List<PostDTO> dtoList = new ArrayList<>();
        List<Post> voList = postQuery(channelCode, target, keyword, paging,false).fetch();
        for (Post p : voList) {
            dtoList.add(postVoChangeDTO(p));
        }
        if (dtoList.size() == 0) {
            return null;
        }
        return dtoList;

    }
    // 채널 태그별 게시글
    public List<PostDTO> channelTagCodeByAllPost(int channelTagCode, Paging paging, String target, String keyword) {
        List<PostDTO> dtoList = new ArrayList<>();
        List<Post> voList = postQuery(channelTagCode, target, keyword, paging,true).fetch();
        for (Post p : voList) {
            dtoList.add(postVoChangeDTO(p));
        }
        if (dtoList.size() == 0) {
            return null;
        }
        return dtoList;

    }
    
    // 채널 전체 인기글
    public List<Post> bestPostVoList(int channelCode, String target, String keyword) {
        // 모든 게시글 다가져옴
        List<Post> voList = postQuery(channelCode,target,keyword,null,false).fetch();
        List<Post> bestList = new ArrayList<>();
        for (Post p : voList) { // 점수 계산
            int bestPoint = postViewCount(p.getPostCode()) + (postLikeCount(p.getPostCode()) * 5) + (postCommentCount(p.getPostCode()) * 2);
            if (bestPoint > 50) {
                bestList.add(p);
            }
        }
        return bestList;
    }
    // 채널 태그별 인기글
    public List<Post> bestTagPostVoList(int channelTagCode, String target, String keyword) {
        // 해당 태그 모든 게시글
        List<Post> voList = postQuery(channelTagCode,target,keyword,null,true).fetch();
        List<Post> bestList = new ArrayList<>();
        for (Post p : voList) { // 점수 계산
            int bestPoint = postViewCount(p.getPostCode()) + (postLikeCount(p.getPostCode()) * 5) + (postCommentCount(p.getPostCode()) * 2);
            if (bestPoint > 50) {
                bestList.add(p);
            }
        }
        return bestList;
    }
    // 인기글들 출력형식 맞춰주기
    public BoardDTO bestPostChangBoard(List<Post> voList ,int page){
        int totalCount = voList.size();
        if(totalCount == 0){
            return null;
        }
        Paging paging = new Paging(page, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));

        int extraCount = paging.getTotalPage() % paging.getPageSize(); // 나머지 숫자
        List<Post> pagingVoList = new ArrayList<>();
        if (paging.getPage() == paging.getEndPage()) { // 마지막 페이지이면서
            if (extraCount != 0) { // 딱떨어지지 않으면 마지막 페이지가
                for (int i = paging.getOffset(); i < paging.getOffset() + extraCount; i++) {
                    pagingVoList.add(voList.get(i));
                }
            } else {
                for (int i = paging.getOffset(); i < paging.getOffset() + paging.getPageSize(); i++) {
                    pagingVoList.add(voList.get(i));
                }
            }
        } else {
            for (int i = paging.getOffset(); i < paging.getOffset() + paging.getPageSize(); i++) {
                pagingVoList.add(voList.get(i));
            }
        }
        List<PostDTO> dtoList = new ArrayList<>();
        for (Post p : pagingVoList) {
            dtoList.add(postVoChangeDTO(p));
        }
        if (dtoList.size() == 0) dtoList = null;
        BoardDTO postBoard = BoardDTO.builder().postList(dtoList).paging(paging).build();
        return postBoard;

    }

    // 해당 채널의 인기 글
    public BoardDTO channelCodeByBestPost(int channelCode, int page, String target, String keyword) {
        List<Post> voList = bestPostVoList(channelCode, target, keyword);
        return bestPostChangBoard(voList, page);
    }
    public BoardDTO channelTagByBestPost(int channelTagCode, int page, String target, String keyword) {
        List<Post> voList = bestTagPostVoList(channelTagCode, target, keyword);
        return bestPostChangBoard(voList, page);
    }

    // 알림 포스트 페이지 알아내는 메서드
    public int postPage(int postCode) {
        Post vo = findByPostCode(postCode);
        // 삭제된 게시글이라면
        if (vo == null) return -1;
        List<Post> list = queryFactory
                .selectFrom(qPost)
                .where(qPost.channel.channelCode.eq(vo.getChannel().getChannelCode()))
                .orderBy(qPost.postCreatedAt.desc()).fetch();
        int index = list.indexOf(vo) + 1;
        // 인덱스 +1 = 해당 채널의 게시글중 N번째 게시글 /10 *10
        if (index == 0) {
            // 몬가 문제생긴경우
            return -1;
        }
        ;
        // 전체(25) % 10 = 0이면  전체 / 10 아니면 (전체 /10) + 1
        int pageNum = (int) Math.ceil(index / 10);
        if (index % 10 != 0) pageNum++;
        return pageNum;
    }


    public int postCommentCount(int postCode) {
        return queryFactory.selectFrom(qComment).where(qComment.postCode.eq(postCode)).fetch().size();
    }

    public int postViewCount(int postCode) {
        List<Integer> list = queryFactory.select(QPost.post.postViews).from(qPost).where(qPost.postCode.eq(postCode)).fetch();
        return list.get(0);
    }

    public int postLikeCount(int postCode) {
        return queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(postCode)).fetch().size();
    }



    public Post findByPostCode(int postCode) {

        return postDAO.findById(postCode).orElse(null);
    }

    // 게시글 조회
    @Transactional
    public PostDTO viewPost(int postCode) {
        // 조회수 증가
        queryFactory.update(qPost)
                .set(qPost.postViews, qPost.postViews.add(1))
                .where(qPost.postCode.eq(postCode))
                .execute();
        Post vo = postDAO.findById(postCode).orElse(null);
        if (vo == null) return null;
        return postVoChangeDTO(vo);
    }

    // 게시글 작성,수정
    public Post savePost(Post vo) {
        // 수정이아니라 작성의 경우에만
        if (vo.getPostCode() == 0) {
            vo.setPostCreatedAt(LocalDateTime.now());
            // 30분으로 도배글 채크
            if(spamCheck(vo.getChannel().getChannelCode(),vo.getPostTitle())){
            // 게시글 작성시 50포인트 추가
            User user = userDAO.findById(getEmail()).get();
            user.setUserPoint(user.getUserPoint() + 50);
            userDAO.save(user);
            return postDAO.save(vo);
            }else{
                // 도배글 방지! 알림
                return null;
            }
        } else { // 수정의 경우
            Post post = postDAO.findById(vo.getPostCode()).get();
            post.setPostTitle(vo.getPostTitle());
            post.setChannelTag(ChannelTag.builder()
                    .channelTagCode(vo.getChannelTag().getChannelTagCode())
                    .build());
            post.setPostContent(vo.getPostContent());

            return postDAO.save(post);
        }

    }
    // 도배글(해당 채널 30분 제목 같으면)
    public boolean spamCheck(int channelCode, String keyword) {
        List<PostDTO> dtoList = new ArrayList<>();
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        BooleanExpression timeCondition = qPost.postCreatedAt.after(thirtyMinutesAgo);
        List<Post> spamList = queryFactory.selectFrom(qPost)
                .join(qUser).on(qPost.userEmail.eq(qUser.userEmail))
                .where(qPost.channel.channelCode.eq(channelCode))
                .where(timeCondition)
                .where(qPost.postTitle.containsIgnoreCase(keyword)).fetch();
        if(spamList.size() != 0){
            return false;
        }return true;
    }
    
    // 게시글 삭제
    public void removePost(int postCode) {
        postDAO.deleteById(postCode);
    }

    // 게시글 좋아요
    public PostLike like(PostLike vo) {
        PostLike like = likeDAO.save(vo);
        // 좋아요 받은 게시글 작성자 포인트 + 10
        Post post = postDAO.findById(like.getPostCode()).get();
        User postAuthor = userDAO.findById(post.getUserEmail()).get();
        postAuthor.setUserPoint(postAuthor.getUserPoint() + 10);
        userDAO.save(postAuthor);
        return like;
    }

    // 좋아요 취소
    public void unLike(int postLikeCode) {
        Post post = postDAO.findById(likeDAO.findById(postLikeCode).get().getPostCode()).get();
        User postAuthor = userDAO.findById(post.getUserEmail()).get();
        // 작성자의 보유 포인트가 10보다 크면 좋아요 취소되면 다시 포인트 -10
        if (postAuthor.getUserPoint() > 10) {
            postAuthor.setUserPoint(postAuthor.getUserPoint() - 10);
            userDAO.save(postAuthor); // 포인트 감소
        }
        likeDAO.deleteById(postLikeCode); // 좋아요 취소
    }
    // 로그인한 사용자의 좋아요 여부 (내가 눌럿나 안눌럿나)
    public PostLike findLike(int postCode) {
        List<PostLike> list = queryFactory.selectFrom(qPostLike)
                .where(qPostLike.postCode.eq(postCode))
                .where(qPostLike.userEmail.eq(getEmail())).fetch();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
    // 로그인 유저의 email
    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();
            return user.getUserEmail();
        }
        return null;
    }

    // 포스트 코드로 채널 찾기
    public int postCodeByChannel(int postCode) {
        Post vo = postDAO.findById(postCode).get();
        int channelCode = vo.getChannel().getChannelCode();
        return channelCode;

    }

    //해당 채널 게시글 수
    public int userPostCount(int channelCode, String userEmail) {
        return postDAO.postCount(channelCode, userEmail);
    }
    //해당 유저의 최신글 10개
    public List<PostDTO> emailByPost(String userEmail) {
        List<Post> postList = postDAO.emailByPost(userEmail);
        List<PostDTO> dtoList = new ArrayList<>();
        User user = userDAO.findById(userEmail).orElse(null);
        if (postList.size() == 0) {
            return null;
        } else {
            for (int i = 0; i < postList.size(); i++) {
                dtoList.add(postVoChangeDTO(postList.get(i)));
            }
            return dtoList;
        }


    }
    // Post vo -> Post dto
    public PostDTO postVoChangeDTO(Post p) {
        User userVo = userDAO.findById(p.getUserEmail()).get();
        return PostDTO.builder()
                .postCreatedAt(p.getPostCreatedAt())
                .postTitle(p.getPostTitle())
                .postContent(p.getPostContent())
                .postCode(p.getPostCode())
                .channelTag(tagDAO.findById(p.getChannelTag().getChannelTagCode()).get())
                .channelCode(p.getChannel().getChannelCode())
                .channelName(p.getChannel().getChannelName())
                .postViews(p.getPostViews())

                .user(UserDTO.builder().userNickname(userVo.getUserNickname())
                        .userImgUrl(userVo.getUserImgUrl())
                        .userEmail(userVo.getUserEmail()).build())
                .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(p.getPostCode())).fetch().size())
                .commentCount(commentService.commentCount(p.getPostCode()))
                .bestPoint(postViewCount(p.getPostCode()) + (postLikeCount(p.getPostCode()) * 5) + (postCommentCount(p.getPostCode()) * 2))
                .build();


    }
    // ????
    public List<Integer> findChannelCode(String userEmail) {

        return postDAO.findChannelCode(userEmail);
    }
}
