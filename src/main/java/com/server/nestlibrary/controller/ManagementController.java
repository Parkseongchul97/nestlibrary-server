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
        Management management = managementService.subscribe(vo);
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
            return ResponseEntity.ok(managementService.check(channelCode));
    }

}
