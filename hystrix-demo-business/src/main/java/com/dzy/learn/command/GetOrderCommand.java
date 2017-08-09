package com.dzy.learn.command;

import com.dzy.learn.model.Order;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 */
public class GetOrderCommand extends HystrixCommand<Order> {

    private final Long orderId;

    public GetOrderCommand(Long orderId) {
        super(HystrixCommandGroupKey.Factory.asKey(GetOrderCommand.class.getSimpleName()));
        this.orderId = orderId;
    }

    @Override
    protected Order run() {
        /* 模拟网络请求拿到order    50-200ms */
        try {
            Thread.sleep((int) (Math.random() * 200) + 50);
        } catch (InterruptedException e) {
            // 不作处理
        }

        /* 很小的几率会失败，但是允许失败，没有fallback */
        if (Math.random() > 0.9999) {
            throw new RuntimeException("随机失败，在加载order的时候，几率比较小");
        }

        /* 5%的概率出现延迟超时 */
        if (Math.random() > 0.95) {
            // random latency spike
            try {
                Thread.sleep((int) (Math.random() * 300) + 25);
            } catch (InterruptedException e) {
                // 不处理
            }
        }

        /* 成功过来啦，假装调用order soa里面拿到order */
        return new Order(orderId);
    }

}
