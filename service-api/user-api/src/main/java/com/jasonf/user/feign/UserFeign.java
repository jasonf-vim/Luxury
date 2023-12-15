package com.jasonf.user.feign;

import com.jasonf.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("user")
public interface UserFeign {
    @GetMapping("/user/login/{username}")
    User findUser(@PathVariable("username") String username);
}
