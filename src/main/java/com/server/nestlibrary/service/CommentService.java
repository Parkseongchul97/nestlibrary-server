package com.server.nestlibrary.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.vo.Comment;
import com.server.nestlibrary.model.vo.QComment;
import com.server.nestlibrary.model.vo.QPost;
import com.server.nestlibrary.model.vo.QPostLike;
import com.server.nestlibrary.repo.CommentDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CommentService {

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QComment qComment = QComment.comment;
    private final QPost qPost = QPost.post;
    // 댓글 추가
    public Comment addComment(Comment vo) {
        return commentDAO.save(vo);
    }

    public Comment updateComment(Comment vo) {
        Comment comment = commentDAO.findById(vo.getCommentCode()).get();
        // 기존값들
        comment.setCommentContent(vo.getCommentContent());
        return commentDAO.save(comment);
    }

    // 자식댓글 확인
    public List<Comment> findChildComment(int commentCode){
        return queryFactory.selectFrom(qComment)
                .where(qComment.commentParentsCode.eq(commentCode)) // 해당 댓글을 부모로 가지고 있는 댓글이 없는
                .where(qComment.commentContent.isNotNull()) // 삭제된 댓글이거나
                .fetch();
    }
    // 자식댓글 삭제용
    public List<Comment> findRemoveChildComment(int commentCode){
        return queryFactory.selectFrom(qComment)
                .where(qComment.commentParentsCode.eq(commentCode)) // 해당 댓글을 부모로 가진
                .fetch();
    }
    // 부모 삭제 처리
    private void removeParentComment(int commentParentsCode) {
        if (commentParentsCode > 0) { // 부모 댓글이 있을때
            Comment parentComment = commentDAO.findById(commentParentsCode).orElse(null); // 부모댓글을 가져옴
            if (parentComment != null) { // 부모댓글이 진짜 있으면
                int brotherCount = findChildComment(commentParentsCode).size(); // 해당 댓글의 형제 댓글 숫자
                if (brotherCount == 0 && parentComment.getCommentContent() == null) { // 형제 댓글이 0(나만 남았고), 부모의 내용이 없으면
                    commentDAO.deleteById(commentParentsCode); // 부모 댓글을 지우고
                    removeParentComment(parentComment.getCommentParentsCode()); // 부모의 부모 댓글 로직 다시불러옴
                }
            }
        }
    }

    public void removeComment(int commentCode) {
        int childCount = findChildComment(commentCode).size(); // 자식댓글의 숫자
        Comment comment = commentDAO.findById(commentCode).get(); // 지우려는 당사자
        if(childCount > 0){ // 대댓글이 살아있음
            log.info("딜리트 변경");
            comment.setCommentContent(null); // 댓글 내용을 삭제
            commentDAO.save(comment);
        }else { // 살아있는 대댓글이 없음 (그냥 삭제)
            commentDAO.deleteById(commentCode); // 나 삭제
            removeParentComment(comment.getCommentParentsCode());  // 부모댓글 삭제 시작
            List<Comment> list = findChildComment(commentCode); // 내 남아있는 자식 댓글(삭제된 거일수도 있는)
            if(list.size() > 0){ // 내용 다날아간 자식 댓글이 1개이상 남아있다면
                for(Comment c : list){
                    removeComment(c.getCommentCode()); // 자식 댓글들도 다시 로직 불러옴
                }
            }
        }


    }

    public List<Comment> getTopComment(int postCode) {
        return queryFactory
                .selectFrom(qComment)
                .where(qComment.postCode.eq(postCode))
                .where(qComment.commentParentsCode.eq(0)) // 부모가 없는 댓글(대댓글 X 일반)
                .orderBy(qComment.commentCreatedAt.desc()) // 작성시간따라 정렬
//                .limit(20) // 페이징 관련된거 추가
                .fetch();
    }

    public List<Comment> getBottomComment(int commentParentsCode) {
        return queryFactory
                .selectFrom(qComment)
                .where(qComment.commentParentsCode.eq(commentParentsCode))
                .orderBy(qComment.commentCreatedAt.asc())
                .fetch();
    }
}