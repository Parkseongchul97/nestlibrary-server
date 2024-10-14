package com.server.nestlibrary.controller;


import com.server.nestlibrary.model.vo.Management;
import com.server.nestlibrary.service.ManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        System.out.println("구독컨트롤러 연결 " + vo);
        vo.setManagementUserStatus("sub");
        managementService.subscribe(vo);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 구독취소
    @DeleteMapping("private/subscribe/{managementCode}")
    public ResponseEntity removeSubscribe (@PathVariable(name = "managementCode") int managementCode){

        System.out.println("구독취소 컨트롤러 연결 " );
        managementService.remove(managementCode);

        return  ResponseEntity.status(HttpStatus.OK).build();
    }


     //  구독중인지
    @GetMapping("/private/sub/{channelCode}")
    public ResponseEntity check(@PathVariable  (name="channelCode") int channelCode) {

        System.out.println("구독체크 컨트롤러 연결");
        if (managementService.check(channelCode) != null) {
            return ResponseEntity.ok(managementService.check(channelCode));

        } else {

            return ResponseEntity.ok(null);
        }

    }
}
