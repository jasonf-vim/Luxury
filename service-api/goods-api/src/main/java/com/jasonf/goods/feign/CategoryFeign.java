package com.jasonf.goods.feign;

import com.jasonf.goods.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("goods")
public interface CategoryFeign {
    @GetMapping("category/findCategoryById/{id}")
    Category findCategoryById(@PathVariable Integer id);
}
