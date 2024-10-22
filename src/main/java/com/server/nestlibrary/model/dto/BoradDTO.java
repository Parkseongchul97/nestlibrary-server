package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.Paging;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BoradDTO {
    private List<PostDTO> postList = new ArrayList<>();
    private Paging paging;
}