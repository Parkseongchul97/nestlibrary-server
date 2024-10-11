package com.server.nestlibrary.service;


import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.ManagementDAO;
import com.server.nestlibrary.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagementService {


    @Autowired
    private ManagementDAO managementDAO;


    @Autowired
    private UserDAO userDAO;

    public User findHost (int channelCode){
        return  userDAO.findById(managementDAO.findHost(channelCode).getUserEmail()).orElse(null);
    }
}
