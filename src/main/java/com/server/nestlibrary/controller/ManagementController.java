package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.vo.Management;
import com.server.nestlibrary.service.ManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ManagementController {
    @Autowired
    private ManagementService managementService;
}
