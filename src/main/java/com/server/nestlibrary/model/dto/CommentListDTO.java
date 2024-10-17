package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.Paging;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CommentListDTO {
    private List<CommentDTO> commentList = new ArrayList<>();
    private Paging paging;
}