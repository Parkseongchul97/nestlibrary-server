package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Management;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManagementDAO extends JpaRepository<Management, Integer> {


}
