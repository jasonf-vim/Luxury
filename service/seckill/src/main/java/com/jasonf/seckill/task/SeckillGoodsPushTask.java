package com.jasonf.seckill.task;

import com.jasonf.seckill.dao.SeckillGoodsMapper;
import com.jasonf.seckill.pojo.SeckillGoods;
import com.jasonf.utils.DateUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillGoodsPushTask {
    private static final String SECKILL_GOODS_KEY = "seckill:goods:";

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    @Resource
    private SimpleDateFormat simpleDateFormat;

    @Resource
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/10 * * * * *")
    public void pushGoodsToRedis() {
        List<Date> dates = DateUtil.getDateMenus();
        for (Date date : dates) {
            // 构建redis key
            String suffix = DateUtil.date2Str(date);    // yyyyMMddHH
            String key = SECKILL_GOODS_KEY + suffix;
            // 筛选合格商品
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status", "1"); // 审核通过
            criteria.andGreaterThan("stockCount", 0);   // 库存数量
            criteria.andGreaterThanOrEqualTo("startTime", simpleDateFormat.format(date));   // 时间限制
            criteria.andLessThan("endTime", simpleDateFormat.format(DateUtil.addDateHour(date, 2)));
            Set keys = redisTemplate.opsForHash().keys(key);
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);  // 排除已上架商品
            }
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);
            for (SeckillGoods seckillGood : seckillGoods) {
                redisTemplate.opsForHash().put(key, seckillGood.getId(), seckillGood);
            }
        }
    }
}
