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
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/channel/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @GetMapping("/main")
    public ResponseEntity allChannel(){
        List<Channel> list = channelService.allChannel();
        log.info("전부 : " + list);
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{channelCode}")
    public ResponseEntity channelMain(@PathVariable(name = "channelCode")int channelCode){
       Channel chan = channelService.findChannel(channelCode);
       log.info("해당 코드의 채널 : " + chan);
       // 일단 기본 채널정보만
        // 추후 DTO로 커다란거로 포장해서 보내야함...
        return ResponseEntity.ok(chan);
    }

    @GetMapping("/{channelCode}/{channelTagCode}")
    public ResponseEntity channelSub(@PathVariable(name = "channelCode")int channelCode,@PathVariable(name = "channelTagCode")int channelTagCode){
        Channel chan = channelService.findChannel(channelCode);
        log.info("해당 코드의 채널 : " + chan);
        // 채널 서브 게시판 창으로 보내야함. (채널코드 + 채널 태그코드로 찾는 )
        return ResponseEntity.ok(chan);
    }

    // 채널 이름 중복 확인
    @GetMapping("/name")
    public ResponseEntity findByChannelName(@RequestParam(name = "channelName")String channelName,@RequestParam( required = false, name="channelCode") int channelCode){

        return ResponseEntity.ok(channelService.findByChannelName(Channel.builder().channelCode(channelCode).channelName(channelName).build()));
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

    public void fileDelete(String file, int channelCode) throws IllegalStateException, Exception {
        if (file != null) {
            String decodedString = URLDecoder.decode(file, StandardCharsets.UTF_8.name()); // 한글 디코딩 처리
            File f = new File("\\\\192.168.10.51\\nest\\channel\\" +String.valueOf(channelCode) + "\\" + decodedString);
            f.delete();
        }
    }
}
