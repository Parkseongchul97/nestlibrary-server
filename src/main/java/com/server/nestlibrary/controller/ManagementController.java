package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.ChannelDTO;
import com.server.nestlibrary.model.dto.SubscribeChannelDTO;
import com.server.nestlibrary.model.dto.UserRoleDTO;
import com.server.nestlibrary.model.vo.Management;
import com.server.nestlibrary.service.ManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping ("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ManagementController {

    @Autowired
    private ManagementService managementService;

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

    @PutMapping("/private/subscribe/role")
      public ResponseEntity changeGrade (@RequestBody UserRoleDTO userRoleDTO){





        int days = userRoleDTO.getBanDate();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newDate = now.plusDays(days);

        userRoleDTO.setManagementDeleteAt(newDate);



        log.info("등급바꾸기 컨트롤러 " + userRoleDTO);

        // 경우
        //  sub or 아무것도 아닌 사람이 벤  => 그냥 management에 추가만 하면됨
        // admin인 사람이 벤 => 그 사람이 어드민인 기록 삭제 후  ban 추가
        //  ban인 사람이 벤 => 음 그냥 save하면 될거같은데 dao찾아서
        // 일단 여기서 구한 값을 service로 넘겨주면?





        return  ResponseEntity.ok(null);
    }




}
