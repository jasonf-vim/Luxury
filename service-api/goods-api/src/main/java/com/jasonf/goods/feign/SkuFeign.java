package com.jasonf.goods.feign;

import com.jasonf.entity.Result;
import com.jasonf.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("goods")
public interface SkuFeign {
    @GetMapping("sku/spu/{spuId}")
    List<Sku> findSkuListBySpuId(@PathVariable("spuId") String spuId);

    @GetMapping("sku/find/{id}")
    Sku find(@PathVariable String id);

    @PutMapping("sku/decr/count")
    Result decrCount(@RequestParam("username") String username);
}
