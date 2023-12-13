package com.jasonf.goods.feign;

import com.jasonf.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("goods")
public interface SkuFeign {
    @GetMapping("sku/spu/{spuId}")
    List<Sku> findSkuListBySpuId(@PathVariable("spuId") String spuId);
}
