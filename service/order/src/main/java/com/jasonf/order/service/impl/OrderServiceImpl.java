package com.jasonf.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jasonf.goods.feign.SkuFeign;
import com.jasonf.order.config.RabbitMQConfig;
import com.jasonf.order.dao.*;
import com.jasonf.order.pojo.*;
import com.jasonf.order.service.CartService;
import com.jasonf.order.service.OrderService;
import com.jasonf.pay.feign.PayFeign;
import com.jasonf.utils.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private CartService cartService;

    @Resource
    private IdWorker idWorker;

    @Resource
    private OrderItemMapper orderItemMapper;

    private static final String CART = "cart:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SkuFeign skuFeign;

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private OrderLogMapper orderLogMapper;

    @Resource
    private PayFeign payFeign;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private OrderConfigMapper orderConfigMapper;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }


    /**
     * 下订单
     *
     * @param order
     */
    @Override
    @Transactional  // 本地事务
    public String add(Order order) {
        // 购物车信息
        Map cartMap = cartService.list(order.getUsername());
        // tb_order表记录数据
        String orderId = Long.toString(idWorker.nextId());
        order.setId(orderId);
        order.setTotalNum((Integer) cartMap.get("totalNum"));
        order.setTotalMoney((Integer) cartMap.get("totalMoney"));
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setBuyerRate("1");
        order.setSourceType("1");
        order.setOrderStatus("0");  // 下单状态
        order.setPayStatus("0");    // 未支付
        order.setConsignStatus("0");    // 未发货
        order.setIsDelete("0"); // 未删除
        orderMapper.insertSelective(order);
        // tb_order_item表记录数据
        List<OrderItem> orderItems = (List<OrderItem>) cartMap.get("orderItems");
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderId);
            orderItemMapper.insertSelective(orderItem);
        }
        // 扣减库存
        skuFeign.decrCount(order.getUsername());

        // 删除购物车信息
        stringRedisTemplate.delete(CART + order.getUsername());

        // 添加积分 (分布式事务)
        Task task = new Task();
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setMqExchange(RabbitMQConfig.EX_BUYING_ADDPOINTUSER);
        task.setMqRoutingkey(RabbitMQConfig.CG_BUYING_ADDPOINT_KEY);
        Map<String, Object> request = new HashMap<>();
        request.put("order_id", orderId);
        request.put("user_id", order.getUsername());
        request.put("point", order.getTotalMoney());
        task.setRequestBody(JSON.toJSONString(request));
        taskMapper.insertSelective(task);

        // 往延迟队列放置消息, 校验支付结果
        rabbitTemplate.convertAndSend("", "queue.ordercreate", orderId);
        return orderId;
    }


    /**
     * 修改
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Order>) orderMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Order>) orderMapper.selectByExample(example);
    }

    @Override
    @Transactional
    public void updatePayStatus(String orderId, String transactionId) {
        // 查询订单状态
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order != null && "0".equals(order.getPayStatus())) {    // 订单存在且未支付
            order.setPayStatus("1");    // 已支付
            order.setOrderStatus("1");  // 订单到达已支付阶段
            order.setTransactionId(transactionId);
            order.setPayTime(new Date());
            order.setUpdateTime(new Date());
            orderMapper.insertSelective(order);
            // 记录订单变更历史
            OrderLog orderLog = new OrderLog();
            orderLog.setOrderId(Long.toString(idWorker.nextId()));
            orderLog.setOperator("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(orderId);
            orderLog.setOrderStatus("1");
            orderLog.setPayStatus("1");
            orderLog.setConsignStatus("0");     // 未发货
            orderLog.setRemarks("微信支付, 流水号：" + transactionId);
            orderLogMapper.insertSelective(orderLog);
        }
    }

    @Override
    @Transactional
    public void closeOrder(String orderId) {
        // 验证orderId
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new RuntimeException("订单: " + orderId + " 不存在");
        }
        if (!"0".equals(order.getOrderStatus())) {
            return;     // 无需处理
        }
        Map<String, String> queryMap = payFeign.queryOrder(orderId).getData();
        if (queryMap.get("trade_state") != null || "SUCCESS".equals(queryMap.get("trade_state"))) {    // 订单已支付
            updatePayStatus(orderId, queryMap.get("transaction_id"));
            return;
        }
        if ("NOTPAY".equals(queryMap.get("trade_state"))) {
            // 关闭微信订单
            payFeign.closeOrder(orderId);
            // 关闭用户订单
            order.setOrderStatus("9");      // 关闭订单
            order.setCloseTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            // 记录关闭日志
            OrderLog orderLog = new OrderLog();
            orderLog.setOrderId(Long.toString(idWorker.nextId()));
            orderLog.setOperator("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(orderId);
            orderLog.setOrderStatus("9");
            orderLog.setPayStatus("0");
            orderLog.setConsignStatus("0");     // 未发货
            orderLog.setRemarks("超时未支付");
            orderLogMapper.insertSelective(orderLog);
            // 回滚库存
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            for (OrderItem item : orderItemMapper.select(orderItem)) {
                skuFeign.rollback(item.getSkuId(), item.getNum());
            }
        }
    }

    @Override
    @Transactional
    public void batchSend(List<Order> orders) {
        // 物流信息校验
        for (Order order : orders) {
            if (order.getId() == null) {
                throw new RuntimeException("订单ID为空");
            } else if (order.getShippingName() == null || order.getShippingCode() == null) {
                throw new RuntimeException("物流信息为空");
            }
        }
        // 订单状态校验
        for (Order order : orders) {
            Order queryOrder = orderMapper.selectByPrimaryKey(order.getId());
            if (!"1".equals(queryOrder.getOrderStatus()) || !"0".equals(queryOrder.getConsignStatus())) {
                throw new RuntimeException("订单状态异常");
            }
        }
        // 批量发货
        for (Order order : orders) {
            order.setOrderStatus("2");  // 设为已发货
            order.setConsignStatus("1");
            order.setConsignTime(new Date());
            order.setUpdateTime(new Date());
            orderMapper.insertSelective(order);
            // 记录日志
            OrderLog orderLog = new OrderLog();
            orderLog.setOrderId(Long.toString(idWorker.nextId()));
            orderLog.setOperator("admin");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(order.getId());
            orderLog.setOrderStatus("2");
            orderLog.setPayStatus("1");
            orderLog.setConsignStatus("1");     // 已发货
            orderLog.setRemarks("订单已发货");
            orderLogMapper.insertSelective(orderLog);
        }
    }

    @Override
    @Transactional
    public void take(String orderId, String operator) {
        if (StringUtils.isBlank(orderId)) {
            throw new RuntimeException("订单号为空");
        }
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        // 修改order属性
        if (!"1".equals(order.getConsignStatus())) {
            throw new RuntimeException("物流信息异常");
        }
        order.setConsignStatus("2");
        order.setOrderStatus("3");
        order.setUpdateTime(new Date());
        order.setEndTime(new Date());
        orderMapper.insertSelective(order);
        // 记录日志
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(Long.toString(idWorker.nextId()));
        orderLog.setOperator(operator);
        orderLog.setOperateTime(new Date());
        orderLog.setOrderId(order.getId());
        orderLog.setOrderStatus("3");
        orderLog.setPayStatus("1");
        orderLog.setConsignStatus("2");     // 已收货
        orderLog.setRemarks("订单已收货");
        orderLogMapper.insertSelective(orderLog);
    }

    @Override
    public void autoTake() {
        // 筛选 发货 超过期限的订单
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey("1");
        Integer timeout = orderConfig.getTakeTimeout();
        LocalDate now = LocalDate.now();
        LocalDate date = now.plusDays(-timeout);    // 时间往前推
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderStatus", "2");
        criteria.andLessThanOrEqualTo("consignTime", date);
        List<Order> orders = orderMapper.selectByExample(example);
        // 遍历并自动收货
        orders.forEach(order -> take(order.getId(), "system"));
    }

    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 订单id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 支付类型，1、在线支付、0 货到付款
            if (searchMap.get("payType") != null && !"".equals(searchMap.get("payType"))) {
                criteria.andEqualTo("payType", searchMap.get("payType"));
            }
            // 物流名称
            if (searchMap.get("shippingName") != null && !"".equals(searchMap.get("shippingName"))) {
                criteria.andLike("shippingName", "%" + searchMap.get("shippingName") + "%");
            }
            // 物流单号
            if (searchMap.get("shippingCode") != null && !"".equals(searchMap.get("shippingCode"))) {
                criteria.andLike("shippingCode", "%" + searchMap.get("shippingCode") + "%");
            }
            // 用户名称
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
            // 买家留言
            if (searchMap.get("buyerMessage") != null && !"".equals(searchMap.get("buyerMessage"))) {
                criteria.andLike("buyerMessage", "%" + searchMap.get("buyerMessage") + "%");
            }
            // 是否评价
            if (searchMap.get("buyerRate") != null && !"".equals(searchMap.get("buyerRate"))) {
                criteria.andLike("buyerRate", "%" + searchMap.get("buyerRate") + "%");
            }
            // 收货人
            if (searchMap.get("receiverContact") != null && !"".equals(searchMap.get("receiverContact"))) {
                criteria.andLike("receiverContact", "%" + searchMap.get("receiverContact") + "%");
            }
            // 收货人手机
            if (searchMap.get("receiverMobile") != null && !"".equals(searchMap.get("receiverMobile"))) {
                criteria.andLike("receiverMobile", "%" + searchMap.get("receiverMobile") + "%");
            }
            // 收货人地址
            if (searchMap.get("receiverAddress") != null && !"".equals(searchMap.get("receiverAddress"))) {
                criteria.andLike("receiverAddress", "%" + searchMap.get("receiverAddress") + "%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andEqualTo("sourceType", searchMap.get("sourceType"));
            }
            // 交易流水号
            if (searchMap.get("transactionId") != null && !"".equals(searchMap.get("transactionId"))) {
                criteria.andLike("transactionId", "%" + searchMap.get("transactionId") + "%");
            }
            // 订单状态
            if (searchMap.get("orderStatus") != null && !"".equals(searchMap.get("orderStatus"))) {
                criteria.andEqualTo("orderStatus", searchMap.get("orderStatus"));
            }
            // 支付状态
            if (searchMap.get("payStatus") != null && !"".equals(searchMap.get("payStatus"))) {
                criteria.andEqualTo("payStatus", searchMap.get("payStatus"));
            }
            // 发货状态
            if (searchMap.get("consignStatus") != null && !"".equals(searchMap.get("consignStatus"))) {
                criteria.andEqualTo("consignStatus", searchMap.get("consignStatus"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }

            // 数量合计
            if (searchMap.get("totalNum") != null) {
                criteria.andEqualTo("totalNum", searchMap.get("totalNum"));
            }
            // 金额合计
            if (searchMap.get("totalMoney") != null) {
                criteria.andEqualTo("totalMoney", searchMap.get("totalMoney"));
            }
            // 优惠金额
            if (searchMap.get("preMoney") != null) {
                criteria.andEqualTo("preMoney", searchMap.get("preMoney"));
            }
            // 邮费
            if (searchMap.get("postFee") != null) {
                criteria.andEqualTo("postFee", searchMap.get("postFee"));
            }
            // 实付金额
            if (searchMap.get("payMoney") != null) {
                criteria.andEqualTo("payMoney", searchMap.get("payMoney"));
            }

        }
        return example;
    }
}
