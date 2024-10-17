package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.*;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.ManagementDAO;
import com.server.nestlibrary.service.ChannelService;
import com.server.nestlibrary.service.ManagementService;
import com.server.nestlibrary.service.PostService;
import com.server.nestlibrary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.Proxy;
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

    @Autowired
    private PostService postService;



    @GetMapping("/channel/main")
    public ResponseEntity allChannel(){
        List<Channel> list = channelService.allChannel();
        List<ChannelPostDTO> dtoList= new ArrayList<>();
        for(Channel c : list){
        dtoList.add(channelService.allChannelInfo(c.getChannelCode()));
        }
        return ResponseEntity.ok(dtoList);
    }

    // 채널정보 조회
    @GetMapping("/channel/{channelCode}")
    public ResponseEntity channelSub(@PathVariable(name = "channelCode")int channelCode){
        Channel vo = channelService.findChannel(channelCode);
        ChannelDTO dto = ChannelDTO.builder()
                .channelCode(channelCode)
                .channelImg(vo.getChannelImgUrl())
                .channelCreatedAt(vo.getChannelCreatedAt())
                .channelName(vo.getChannelName())
                .channelInfo(vo.getChannelInfo())
                .channelTag(channelService.tagList(channelCode))
                .host(managementService.findAdmin(channelCode).get(0))
                .favoriteCount(managementService.count(channelCode))
                .build();
        log.info("채널정보 : " + dto);
        return ResponseEntity.ok(dto);
    }
    // 채널의 전체 게시판 조회
    @GetMapping("/{channelCode}")
    public ResponseEntity allPost(@PathVariable(name = "channelCode")int channelCode,
                                  @RequestParam(name = "page",defaultValue = "1")int page,
                                  @RequestParam(name = "target",defaultValue = "" ,required = false)String target,
                                  @RequestParam(name = "keyword",defaultValue = "" ,required = false)String keyword
                                  ){
        log.info("page : " + page);
        log.info("target : " + target);
        log.info("keyword : " + keyword);
        int totalCount = postService.allPostCount(channelCode, target , keyword);
        Paging paging = new Paging(page, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage()-1));
            // 전체 게시글 파라미터로 ?p=1~10%target=유저or제목,내용%search=검색어
            List<PostDTO>  postList = postService.channelCodeByAllPost(channelCode,paging,target,keyword);
        BoradDTO postBorad = BoradDTO.builder().postList(postList).paging(paging).build();
        // 페이징도 같이 담긴걸로?
            return ResponseEntity.ok(postBorad);
    }
    // 채널의 세부탭 게시판 조회
    @GetMapping("/{channelCode}/{channelTagCode}")
    public ResponseEntity tagPost(@PathVariable(name = "channelCode")int channelCode,@PathVariable(required = false, name = "channelTagCode")int channelTagCode,
                                  @RequestParam(name = "page",defaultValue = "1")int page,
                                  @RequestParam(name = "target",defaultValue = "" ,required = false)String target,
                                  @RequestParam(name = "keyword",defaultValue = "" ,required = false)String keyword){
             // 게시글 파라미터로 ?p=1~10%target=유저or제목,내용%search=검색어
        int totalCount = postService.tagPostCount(channelTagCode,target,keyword);
        Paging paging = new Paging(page, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage()-1));
        List<PostDTO>  postList = postService.channelTagCodeByAllPost(channelTagCode,paging,target,keyword);
        BoradDTO postBorad = BoradDTO.builder().postList(postList).paging(paging).build();
            return ResponseEntity.ok(postBorad);
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
        if(channel == null){
            return ResponseEntity.ok(null);
        }
        Path directoryPath = Paths.get("\\\\\\\\192.168.10.51\\\\nest\\\\channel\\" + String.valueOf(channel.getChannelCode())  + "\\");
        Files.createDirectories(directoryPath);
        channel.setChannelImgUrl(fileUpload(dto.getChannelImgUrl(), channel.getChannelCode())); // 이미지 추가
        Channel result = channelService.createChannel(channel);
        log.info("message : " + channel);
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
