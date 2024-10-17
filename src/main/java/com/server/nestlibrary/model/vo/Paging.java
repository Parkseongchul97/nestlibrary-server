package com.server.nestlibrary.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor
@Data @Builder
public class Paging {
    private int page = 1; // 현재 페이지
    private int offset = 0; // 시작위치
    private int limit = 10; // 레코드 수

    private int pageSize = 10; // 한페이지 당 페이지 버튼 개수
    private int endPage = this.pageSize; // 한 페이지의 마지막 페이지 버튼 수
    private int startPage = this.page; // 한 페이지의 첫 페이지 버튼 수
    private int totalPage;

    private boolean prev; // 이전 페이지 (단위수만큼)
    private boolean next; // 다음 페이지 ()

    public Paging(int page, int total) {

        /*
         * page : 1 ~ 10 -> endPage = 10
         * page : 11 ~ 20 -> endPage = 20
         * */
        this.page = page;
        this.endPage = (int) Math.ceil((double) this.page / this.pageSize) * this.pageSize;
        this.startPage = this.endPage - this.pageSize + 1;

        int lastPage = (int) Math.ceil((double) total / this.limit);
        if(lastPage < this.endPage) {
            this.endPage = lastPage;
        }
        // 전체 숫자를 통해서 마지막 페이지
        this.prev = this.startPage > 1;
        this.next = this.endPage < lastPage;

    }

}

