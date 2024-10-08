package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.ChannelDTO;
import com.server.nestlibrary.model.vo.Channel;
import com.server.nestlibrary.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/channel/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    // 채널 이름 중복 확인
    @GetMapping("/name")
    public ResponseEntity findByChannelName(@RequestParam(name = "channelName")String channelName,@RequestParam( required = false, name="channelCode") int channelCode){
        if(channelCode != 0){
            // 이름만 중복확인
        }else{
            // 코드 받은거랑 같은 이름인지 확인
        }
        return ResponseEntity.ok(null);
    }

    // 채널 생성
    @PostMapping("/create")
    public ResponseEntity createChannel(ChannelDTO dto) throws Exception {
        Channel channel = channelService.createChannel(Channel
                .builder()
                .channelName(dto.getChannelName())
                .channelInfo(dto.getChannelInfo())
                .channelCreatedAt(LocalDateTime.now())
                .build());
        Path directoryPath = Paths.get("\\\\\\\\192.168.10.51\\\\nest\\\\channel\\" + String.valueOf(channel.getChannelCode())  + "\\");
        Files.createDirectories(directoryPath); 
        channel.setChannelImgUrl(fileUpload(dto.getChannelImgUrl(), channel.getChannelCode())); // 이미지 추가
        Channel result = channelService.createChannel(channel);
        log.info("message : " + channel);
        // 채널 생성후에 로그인한 회원의 포인트 -3000

        // 채널에 기본 채널태그로 공지, 일반 , 인기글 탭 추가
        return ResponseEntity.ok(result);
    }
    public String fileUpload(MultipartFile file, int channelCode) throws IllegalStateException, Exception {
        if (file == null || file.getOriginalFilename() == "") {
            return null;
        }
        UUID uuid = UUID.randomUUID(); // 랜덤 파일명 부여
        String fileName = uuid.toString() + "_" + file.getOriginalFilename();

        File copyFile = new File("\\\\192.168.10.51\\nest\\channel\\" +String.valueOf(channelCode) + "\\" + fileName);
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
