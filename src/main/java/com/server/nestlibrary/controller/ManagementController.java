package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.*;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping ("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ManagementController {

    @Autowired
    private ManagementService managementService;
    @Autowired
    private PostService postService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private PushService pushService;

     // 구독하기
    @PostMapping("private/subscribe")
    public ResponseEntity subscribe(@RequestBody Management vo){
        Management management = managementService.subscribe(vo);
        log.info("구독버튼 누름 " + vo);
        return ResponseEntity.ok(management );
    }

    // 구독취소
    @DeleteMapping("private/subscribe/{managementCode}")
    public ResponseEntity removeSubscribe (@PathVariable(name = "managementCode") int managementCode){
        managementService.remove(managementCode);
        return  ResponseEntity.status(HttpStatus.OK).build();
    }


     //  구독중인지
    @GetMapping("/private/subscribe/{channelCode}")
    public ResponseEntity check(@PathVariable  (name="channelCode") int channelCode) {
        log.info("구독 체크 왔다");
            return ResponseEntity.ok(managementService.check(channelCode));
    }
    // 내가 구독중인 채널
    @GetMapping("/private/subscribe/channel")
    public ResponseEntity myChannel() {
        List<SubscribeChannelDTO> list = managementService.mySubscribe();
        log.info("유저가 구독중인 채널 : " + list);
        if(list == null){

            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(list);
    }


   @PostMapping("/private/role")
   public  ResponseEntity addRole(@RequestBody ManagementDTO dto){

           dto.setManagementDeleteAt(LocalDateTime.now().plusDays(dto.getBanDate()-1));
           Management vo  = userGrade( dto.getChannelCode(), dto.getUserEmail());
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
       Channel chan = channelService.findChannel(dto.getChannelCode());
       if( vo != null){ // 이미 관리 정보가 있음
           // 관리자를 벤
           if (dto.getManagementUserStatus().equals("ban") && vo.getManagementUserStatus().equals("admin")) {

               vo.setManagementUserStatus("ban");
               vo.setManagementDeleteAt(dto.getManagementDeleteAt());
               managementService.setRole(vo);
               pushService.savePush(Push.builder()
                       .pushCreatedAt(LocalDateTime.now()) // 알림 하루지나면 삭제?
                       .pushMassage(chan.getChannelName() + "채널에서 "+ dto.getManagementDeleteAt().format(formatter) +"까지 차단 되었습니다")
                       .userEmail(dto.getUserEmail()) // 대상유저
                       .channelCode(chan.getChannelCode())
                       .build());
               return ResponseEntity.ok(vo);
               // 2번 벤
           } else if (dto.getManagementUserStatus().equals("ban") && vo.getManagementUserStatus().equals("ban") &&dto.getBanDate() != -1) {
               vo.setManagementDeleteAt(vo.getManagementDeleteAt().plusDays(dto.getBanDate()-1));
               managementService.setRole(vo);
               pushService.savePush(Push.builder()
                       .pushCreatedAt(LocalDateTime.now()) // 알림 하루지나면 삭제?
                       .pushMassage(chan.getChannelName() + "채널에서 "+ vo.getManagementDeleteAt().format(formatter) +"까지 차단 되었습니다")
                       .userEmail(dto.getUserEmail()) // 대상유저
                       .channelCode(chan.getChannelCode())
                       .build());

               return ResponseEntity.ok(vo);
           }else{// 호스트 이양
               //예전 호스트 정보
             UserDTO oldHost = managementService.findAdmin(dto.getChannelCode()).get(0);
              Management oldVo = userGrade(dto.getChannelCode(), oldHost.getUserEmail());
              // 예전 호스트를 어드민으로 바꾸고
               oldVo.setManagementUserStatus("admin");
               managementService.setRole(oldVo);
              // 신규 호스트
              vo.setManagementUserStatus("host");
              managementService.setRole(vo);
               pushService.savePush(Push.builder()
                       .pushCreatedAt(LocalDateTime.now()) // 알림 하루지나면 삭제?
                       .pushMassage(chan.getChannelName() + "채널의 호스트로 등록되었습니다")
                       .userEmail(dto.getUserEmail()) // 대상유저
                       .channelCode(chan.getChannelCode())
                       .build());


               return ResponseEntity.ok(null);
           }
       }else {// 그냥 추가
                if(dto.getManagementUserStatus().equals("admin")){

                    pushService.savePush(Push.builder()
                            .pushCreatedAt(LocalDateTime.now()) // 알림 하루지나면 삭제?
                            .pushMassage(chan.getChannelName() + "채널의 관리자로 등록되었습니다.")
                            .userEmail(dto.getUserEmail()) // 대상유저
                            .channelCode(chan.getChannelCode())
                            .build());
                }else{

                    ZoneId zoneId = ZoneId.systemDefault(); // 시스템 기본 시간대
                    Date date = Date.from(dto.getManagementDeleteAt().atZone(zoneId).toInstant());
                    pushService.savePush(Push.builder()
                            .pushCreatedAt(LocalDateTime.now()) // 알림 하루지나면 삭제?
                            .pushMassage(chan.getChannelName() + "채널에서 "+ dto.getManagementDeleteAt().format(formatter) +"까지 차단 되었습니다")
                            .userEmail(dto.getUserEmail()) // 대상유저
                            .channelCode(chan.getChannelCode())
                            .build());
                }
               return  ResponseEntity.ok(managementService.setRole(Management.builder()
                       .channel(chan)
                       .managementDeleteAt(dto.getBanDate() != 0 ? dto.getManagementDeleteAt() : null)
                       .managementUserStatus(dto.getManagementUserStatus())
                       .userEmail(dto.getUserEmail())
                       .build()));

       }


   }

   @DeleteMapping("/private/role/{managementCode}")
   public ResponseEntity removeRole(@PathVariable(name = "managementCode") int managementCode) {
        Management vo = managementService.findManagement(managementCode);
        if(vo.getManagementUserStatus().equals("ban")){
            Channel chan = channelService.findChannel(vo.getChannel().getChannelCode());
            pushService.savePush(Push.builder()
                    .pushCreatedAt(LocalDateTime.now()) // 알림 하루지나면 삭제?
                    .pushMassage(chan.getChannelName() + "채널에서 차단 해제 되었습니다")
                    .userEmail(vo.getUserEmail()) // 대상유저
                    .channelCode(chan.getChannelCode())
                    .build());
        }
        managementService.remove(managementCode);

      return ResponseEntity.ok(null);
   }

    @GetMapping("/grade/{channelCode}/{userEmail}")
    public ResponseEntity targetUserGrade(@PathVariable(name = "channelCode") int channelCode, @PathVariable(name = "userEmail")String userEmail) {

        return  ResponseEntity.ok(userGrade(channelCode,userEmail));
    }
    // 포스트 코드로 해당 채널의 나의 등급
    @GetMapping("/private/grade/{channelCode}")
    public ResponseEntity findUserGrade(@PathVariable(name = "channelCode") int channelCode) {
        // 포스트 코드를 받아서 무슨 채널인지 찾아야함

        // 없으면 null 반환 있으면 등급반환
        return ResponseEntity.ok(userGrade(channelCode ,channelService.getLoginUser()) );
    }

    @GetMapping("private/management/user")
    public ResponseEntity userManagement(@RequestParam(name = "channelCode" , required = false)Integer channelCode,
                                         @RequestParam(name = "userNickname" ,required = false)String userNickname,
                                         @RequestParam(name = "managementUserStatus" , required = false)String managementUserStatus){
        // 채널내의 등급
        // 클라이언트에서 채널코드와 등급을 주거나 채널코드와 닉네임을 주거나인데
        // 1. 서비스의 모든 유저중 검색된 유저  리스트
        log.info("여기까지오나? " + channelCode + userNickname + managementUserStatus);

          if(userNickname != null){
              List<User> userList = userService.findByNicknameUserList(userNickname);
              List<UserRoleDTO> userRoleDTOList = new ArrayList<>();

             if(userList.size() == 0) {

                 return ResponseEntity.ok(null);
             }
              for(int i=0; i<userList.size(); i++) {
                  Management vo = userGrade(channelCode, userList.get(i).getUserEmail());
                  UserRoleDTO userRoleDTO = UserRoleDTO.builder()
                          .userEmail(userList.get(i).getUserEmail())
                          .userNickname(userList.get(i).getUserNickname())
                          .managementUserStatus(vo != null ? vo.getManagementUserStatus() : null)
                          .managementDeleteAt(vo != null ? vo.getManagementDeleteAt() : null)
                          .managementCode(vo != null ? vo.getManagementCode() :  0)
                          .commentCount( commentService.userCommentCount(channelCode,userList.get(i).getUserEmail()) )
                          .postCount(postService.userPostCount(channelCode,userList.get(i).getUserEmail()))
                          .build();

                  userRoleDTOList.add(userRoleDTO);
              }
              return  ResponseEntity.ok(userRoleDTOList);




          }else if(managementUserStatus.equals("admin")){

            log.info("admin으로 오냐?");
              List<UserDTO> adminList =    managementService.findAdmin(channelCode);
              List<UserRoleDTO> userRoleDTOList = new ArrayList<>();

              for(int i=0; i<adminList.size(); i++) {
                  Management vo = userGrade(channelCode, adminList.get(i).getUserEmail());
                  UserRoleDTO userRoleDTO = UserRoleDTO.builder()
                          .userEmail(adminList.get(i).getUserEmail())
                          .userNickname(adminList.get(i).getUserNickname())
                          .managementUserStatus(vo.getManagementUserStatus())
                          .managementDeleteAt(vo.getManagementDeleteAt())
                          .managementCode(vo.getManagementCode())
                          .commentCount( commentService.userCommentCount(channelCode,adminList.get(i).getUserEmail()) )
                          .postCount(postService.userPostCount(channelCode,adminList.get(i).getUserEmail()))
                          .build();

                  userRoleDTOList.add(userRoleDTO);
              }
              return  ResponseEntity.ok(userRoleDTOList);

          }else   if(managementUserStatus.equals("ban")){

              List<User> banList =    managementService.bans(channelCode);
              List<UserRoleDTO> userRoleDTOList = new ArrayList<>();

              for(int i=0; i<banList.size(); i++) {
                  Management vo = userGrade(channelCode, banList.get(i).getUserEmail());
                  UserRoleDTO userRoleDTO = UserRoleDTO.builder()
                          .userEmail(banList.get(i).getUserEmail())
                          .userNickname(banList.get(i).getUserNickname())
                          .managementUserStatus(vo.getManagementUserStatus())
                          .managementDeleteAt(vo.getManagementDeleteAt())
                          .managementCode(vo.getManagementCode())
                          .commentCount(commentService.userCommentCount(channelCode,banList.get(i).getUserEmail()))
                          .postCount(postService.userPostCount(channelCode,banList.get(i).getUserEmail()))
                          .build();

                  userRoleDTOList.add(userRoleDTO);
              }
              return  ResponseEntity.ok(userRoleDTOList);
          }





        return  ResponseEntity.ok(null);
    }

    //유저관리페이지에서 글 확인
    @GetMapping("/private/management/{channelCode}/{userNickname}")
    public  ResponseEntity channelPost(@PathVariable(name = "channelCode") int channelCode,
                                       @PathVariable(name = "userNickname") String userNickname
                                       ){

        log.info(userNickname);
        log.info("채널코드 " + channelCode);
        List<PostDTO> postDto = new ArrayList<>();
        if( !userNickname.equals("undefined")) {
          postDto = postService.channelCodeByAllPost(channelCode, null, "user", userNickname);
        } else {

            postDto = postService.channelCodeByAllPost(channelCode, null, null, null);
        }

        return  ResponseEntity.ok(postDto);
    }

    // 채널코드, 유저 이메일 받아서 메니지먼트 객체 반환
    public Management userGrade(int channelCode, String userEmail){
        List<Management> channelList = managementService.findChannelManagement(channelCode);
            List<Management> myList = new ArrayList<>();
                for(Management m : channelList){
                if (m.getUserEmail().equals(userEmail)){
                    myList.add(m);
                }
            }if(myList.size()== 0){
                return null;
            }
                for (Management m: myList){
                    if(m.getManagementUserStatus().equals("host")) return m;
                    else if(m.getManagementUserStatus().equals("ban")) return m;
                    else if(m.getManagementUserStatus().equals("admin")) return m;
                 }
                return null;
        }



}







