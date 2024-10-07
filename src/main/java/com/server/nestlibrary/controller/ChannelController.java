package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.ChannelDTO;
import com.server.nestlibrary.model.vo.Channel;
import com.server.nestlibrary.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/channel/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @PostMapping("/create")
    public ResponseEntity createChannel( ChannelDTO dto) throws Exception {

        Channel channel = channelService.createChannel(Channel
                .builder()
                        .channelName(dto.getChannelName())
                        .channelInfo(dto.getChannelInfo())
                        .channelImgUrl(fileUpload(dto.getChannelImgUrl(), 0))
                .build());
        log.info("채널 : " + channel);
        return ResponseEntity.ok(channel);
    }
    public String fileUpload(MultipartFile file, int channelCode) throws IllegalStateException, Exception {
        if (file == null || file.getOriginalFilename() == "") {
            return null;
        }
        UUID uuid = UUID.randomUUID(); // 랜덤 파일명 부여
        String fileName = uuid.toString() + "_" + file.getOriginalFilename();

        File copyFile = new File("\\\\192.168.10.51\\upload\\nestlibrary" + "\\" + fileName);
        // \channel\+ String.valueOf(channelCode)
//        File copyFile = new File("\\\\http://192.168.10.51:8082/\\nestlibrary\\user\\" + email + "\\" + fileName);
        file.transferTo(copyFile);
        return fileName;
    }

    public void fileDelete(String file, String email) throws IllegalStateException, Exception {
        if (file != null) {
            String decodedString = URLDecoder.decode(file, StandardCharsets.UTF_8.name()); // 한글 디코딩 처리
            File f = new File("링크주소");
            f.delete();
        }
    }
}
