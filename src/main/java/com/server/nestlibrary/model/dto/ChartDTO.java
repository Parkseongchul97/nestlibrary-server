package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor@AllArgsConstructor@Data@Builder
public class ChartDTO {
    private int postCount;
    private int CommentCount;
    private LocalDateTime date;

}
