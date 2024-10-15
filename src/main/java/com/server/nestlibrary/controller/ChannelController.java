package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.ChannelDTO;
import com.server.nestlibrary.model.dto.ChannelPostDTO;
import com.server.nestlibrary.model.vo.Channel;
import com.server.nestlibrary.model.vo.ChannelTag;
import com.server.nestlibrary.model.vo.Management;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.ManagementDAO;
import com.server.nestlibrary.service.ChannelService;
import com.server.nestlibrary.service.ManagementService;
import com.server.nestlibrary.service.UserService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ChannelController {

    @Autowired
   private ManagementService managementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;



    @GetMapping("/channel/main")
    public ResponseEntity allChannel(){
        List<Channel> list = channelService.allChannel();
        List<ChannelPostDTO> dtoList= new ArrayList<>();
        for(Channel c : list){
        dtoList.add(channelService.allChannelInfo(c.getChannelCode()));
        }
        return ResponseEntity.ok(dtoList);
    }

    // 채널 새부 정보 조회
    @GetMapping("/channel/{channelCode}")
    public ResponseEntity channelMain(@PathVariable(name = "channelCode")int channelCode){

        ChannelPostDTO chanDTO = channelService.allChannelInfo(channelCode);
        log.info("해당채널 모든 정보 : " + chanDTO);
        return ResponseEntity.ok(chanDTO);
    }

    @GetMapping("/channel/{channelCode}/{channelTagCode}")
    public ResponseEntity channelSub(@PathVariable(name = "channelCode")int channelCode,@PathVariable(name = "channelTagCode")int channelTagCode){
        Channel chan = channelService.findChannel(channelCode);
        log.info("해당 코드의 채널 : " + chan);
        // 채널 서브 게시판 창으로 보내야함. (채널코드 + 채널 태그코드로 찾는 )
        return ResponseEntity.ok(chan);
    }

    // 채널 이름 중복 확인
    @GetMapping("/channel/name")
    public ResponseEntity findByChannelName(@RequestParam(name = "channelName")String channelName,@RequestParam( required = false, name="channelCode") int channelCode){

        return ResponseEntity.ok(channelService.findByChannelName(Channel.builder().channelCode(channelCode).channelName(channelName).build()));
    }

    // 채널 생성(프라이빗 추가)
    @PostMapping("/private/channel/create")
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
    // 채널 태그 추가
    @PostMapping("/private/channel/tag")
    public ResponseEntity createChannelTag(@RequestBody ChannelTag vo) throws Exception {
       ChannelTag tag = channelService.createTag(vo);
       log.info("생성된 새부 게시판 : " + tag);
       // 태그 추가시 포인트 감소
        return ResponseEntity.ok(tag);
    }

    @DeleteMapping("/private/channel/tag/{channelTagCode}")
    public ResponseEntity createChannelTag(@PathVariable(name = "channelTagCode") int channelTagCode) throws Exception {
        channelService.removeTag(channelTagCode);
        log.info("생성된 새부 게시판 삭제");
        // 해당 태그 밑에 있던 게시글들 처리? 일반탭으로? 아님 삭제
        return ResponseEntity.ok(null);
    }
    
    
    // 파일 업로드
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
    // 삭제
    public void fileDelete(String file, int channelCode) throws IllegalStateException, Exception {
        if (file != null) {
            String decodedString = URLDecoder.decode(file, StandardCharsets.UTF_8.name()); // 한글 디코딩 처리
            File f = new File("\\\\192.168.10.51\\nest\\channel\\" +String.valueOf(channelCode) + "\\" + decodedString);
            f.delete();
        }
    }




}
