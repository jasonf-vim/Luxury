package com.jasonf.goods.feign;

import com.jasonf.goods.pojo.Spu;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("goods")
public interface SpuFeign {
    @GetMapping("spu/findSpuById/{id}")
    Spu findSpuById(@PathVariable String id);
}
