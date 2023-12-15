package com.jasonf.web.gateway.utils;

public class UrlUtil {
    // 需要令牌的url
    public static String filterPath = "/api/wseckillorder,/api/seckill,/api/wxpay,/api/wxpay/**,/api/worder/**,/api/user/**,/api/address/**,/api/wcart/**,/api/cart/**,/api/categoryReport/**,/api/orderConfig/**,/api/order/**,/api/orderItem/**,/api/orderLog/**,/api/preferential/**,/api/returnCause/**,/api/returnOrder/**,/api/returnOrderItem/**";

    /**
     * 判断是否需要经过认证
     * @param url
     * @return
     */
    public static boolean requireAuthorize(String url){
        String[] split = filterPath.replace("**", "").split(",");
        for (String s : split) {
            if(url.startsWith(s)){
                return true;
            }
        }
        return false;
    }
}
