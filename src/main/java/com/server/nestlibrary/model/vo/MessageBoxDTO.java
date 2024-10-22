package com.server.nestlibrary.model.vo;

import com.server.nestlibrary.model.dto.MessagesDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor @Data @Builder
public class MessageBoxDTO {
    private List<MessagesDTO> messagesDTOList = new ArrayList<>();
    private Paging paging;
}
