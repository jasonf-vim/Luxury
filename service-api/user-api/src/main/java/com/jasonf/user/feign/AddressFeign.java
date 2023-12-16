package com.jasonf.user.feign;

import com.jasonf.user.pojo.Address;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("user")
public interface AddressFeign {
    @GetMapping("/address/list")
    List<Address> list();
}
