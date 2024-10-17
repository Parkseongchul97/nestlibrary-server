package com.server.nestlibrary.controller;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.server.nestlibrary.model.dto.ChannelDTO;
import com.server.nestlibrary.model.dto.ChannelManagementDTO;
import com.server.nestlibrary.model.dto.ChannelPostDTO;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.ManagementDAO;
import com.server.nestlibrary.service.ChannelService;
import com.server.nestlibrary.service.ManagementService;
import com.server.nestlibrary.service.PostService;
import com.server.nestlibrary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.*;

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
    public ResponseEntity allChannel(@RequestParam(name = "page", defaultValue = "1")int page , @RequestParam(name = "keyword", required = false) String keyword){
        /*
        List<Channel> list = channelService.allChannel();
        List<ChannelPostDTO> dtoList= new ArrayList<>();
        for(Channel c : list){
            dtoList.add(channelService.allChannelInfo(c.getChannelCode()));
        }*/


        // 정렬 조건 필요시 Sort 객체 // 여기서 구독자 수 많은 순으로 보여주기
      // Sort sort = Sort.by("")
        BooleanBuilder builder = new BooleanBuilder();
        Pageable pageable = PageRequest.of(page-1, 4);

        // 동적 처리를 하려면 Q도메인 클래스 가져오기

        // q 도메인 클래스를 이용하면 Entity 클래스에 선언된 필드들을 변수로 사용 가능
        QChannel qChannel = QChannel.channel;
        // WHERE channel_title LIKE CONCAT ('%',keyword,'%');
if(keyword != null && keyword != ""){
    log.info("키워드 컨트롤러 : " + keyword);
    // 원하는 조건은 필드값과 같이 결합해서 생성
    BooleanExpression expression = qChannel.channelName.like("%"+keyword+"%");
    // 만들어진 조건은 WHERE문에 and 나 or 같은 키워드와 결합해서 추가
    builder.and(expression);


    Page<Channel> list = channelService.allChannelPage(builder, pageable);
    List<ChannelPostDTO> dtoList= new ArrayList<>();

    for(int i=0; i<list.getSize(); i++){


        dtoList.add(channelService.allChannelInfo(list.getContent().get(i).getChannelCode()));

    }


    System.out.println(dtoList);

return ResponseEntity.ok(dtoList);
}




        Page<Channel> list = channelService.allChannelPage(builder, pageable);


        List<ChannelPostDTO> dtoList= new ArrayList<>();

        HashMap<Integer,Integer> map2 = new HashMap<>();


        // 모든 채널 코드
     List<Integer> codeList = channelService.allCode();

        // 각 채널코드의 인원수 배열
       List<Integer> countList = new ArrayList<>();

        for(int i =0; i<channelService.allCode().size(); i++){
            map2.put(codeList.get(i), managementService.count(codeList.get(i)));

        }
        List<Map.Entry<Integer, Integer>> entryList = new LinkedList<>(map2.entrySet());
        entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        System.out.println(entryList);


     for(int i=0; i < (page*4); i++) {


         if (i < entryList.size()) {
             dtoList.add(channelService.allChannelInfo(entryList.get(i).getKey()));
         }

     }

/*
        for(Channel c : list){
            if(c.getChannelCode() > 0) {
                dtoList.add(channelService.allChannelInfo(c.getChannelCode()));
            }
        }
*/



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
        int totalCount = postService.allPostCount(channelCode);
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
        int totalCount = postService.tagPostCount(channelTagCode);
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

    // 채널 태그 삭제
    @DeleteMapping("/private/channel/tag/{channelTagCode}")
    public ResponseEntity createChannelTag(@PathVariable(name = "channelTagCode") int channelTagCode ) throws Exception {
        channelService.removeTag(channelTagCode );
        log.info("생성된 새부 게시판 삭제");
        // 해당 태그 밑에 있던 게시글들 처리? 일반탭으로? 아님 삭제
        return ResponseEntity.ok(null);
    }

    // 채널 수정 페이지
    @GetMapping("/private/channel/update/{channelCode}")
    public ResponseEntity updatePage (@PathVariable(name = "channelCode") int channelCode){

          ChannelManagementDTO dto =    channelService.update(channelCode);
        log.info("채널 수정 페이지 정보 :  "  + dto);

        return ResponseEntity.ok(dto);
    }


    // 채널 소개 수정
    @PutMapping("/private/channel/update")
    public ResponseEntity updateInfo (@RequestBody Channel vo ) {

      channelService.updateInfo(vo.getChannelInfo(), vo.getChannelCode());

        return ResponseEntity.ok(null);


    }

     // 채널 이미지 수정
    @PutMapping ("/private/channel/channelImg")
    public ResponseEntity imgUpdate (ChannelDTO dto) throws Exception{



        log.info("이미지 수정 dto " + dto);

    //    채널코드로 db가서 이미지 url 추출
 // 이미지가 안왔을때 기본 이미지 인지 , 기존 이미지 인지 구분을 해줘야함
        // 기본이미지라면  파일 삭제만 해주고 null로
        // 기존 이미지라면 아무것도 안함
        // 업로드라면 이전 이미지가 있을 경우 없을 경우 체크 해서 삭제 업로드

        // 기존 이미지 url
      String imgUrl =   channelService.getUrl(dto.getChannelCode());
      log.info("기존 url " + imgUrl);
      // 기본 프사 사용시
       if(dto.getChange() == -1) {
           // 기존 이미지가 있는 경우
           if (imgUrl != null) {
               fileDelete(imgUrl, dto.getChannelCode());
               channelService.imgUpdate(null, dto.getChannelCode());

               //   // 기존 이미지가 있는 경우  없는경우
           } else {
               channelService.imgUpdate(null, dto.getChannelCode());
           }


           // 기존 프사 사용시


           // 사진 변경시
       }  else if(dto.getChange() == 1) {

           if(imgUrl != null) {
               fileDelete(imgUrl, dto.getChannelCode());
               channelService.imgUpdate(fileUpload(dto.getChannelImgUrl(),dto.getChannelCode()),dto.getChannelCode());

           } else {
               channelService.imgUpdate(fileUpload(dto.getChannelImgUrl(),dto.getChannelCode()),dto.getChannelCode());
           }

       }






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
