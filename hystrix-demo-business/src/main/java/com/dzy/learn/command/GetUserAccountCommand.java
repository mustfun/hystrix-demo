package com.dzy.learn.command;

import com.dzy.learn.model.UserAccount;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import java.net.HttpCookie;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/8
 * @since 1.0
 */
public class GetUserAccountCommand extends HystrixCommand<UserAccount>{
    private final HttpCookie httpCookie;
    private final UserCookie userCookie;

    /**
     *
     * @param cookie
     * @throws IllegalArgumentException
     * 如果cookie不合法，表示用户没有鉴权过
     */
    public GetUserAccountCommand(HttpCookie cookie) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("User")));
        this.httpCookie = cookie;
        /* 会成功，或者抛出一个异常，有概率事件 */
        this.userCookie = UserCookie.parseCookie(httpCookie);
    }

    @Override
    protected UserAccount run() {
        /* 模拟执行网络请求来检索用户信息 */
        try {
            Thread.sleep((int) (Math.random() * 10) + 2);//停顿2-12ms
        } catch (InterruptedException e) {
            // 不处理异常情况
        }

        /* 5%的概率会去调用fallback */
        if (Math.random() > 0.95) {
            throw new RuntimeException("执行UserAccount请求时候随机失败啦----------");
        }

        /* 5%的概率导致超时情况发生 */
        if (Math.random() > 0.95) {
            // 随机触发
            try {
                Thread.sleep((int) (Math.random() * 300) + 25);
            } catch (InterruptedException e) {
                // 超时不作处理
            }
        }

        /* 成功啦，创建一个UserAccount,假装从远程弄过来的呀 */
        return new UserAccount(86975, "John James", 2, true, false, true);
    }

    /**
     * Use the HttpCookie value as the cacheKey so multiple executions
     * in the same HystrixRequestContext will respond from cache.
     */
    @Override
    protected String getCacheKey() {
        return httpCookie.getValue();
    }

    /**
     * Fallback that will use data from the UserCookie and stubbed defaults
     * to create a UserAccount if the network call failed.
     */
    @Override
    protected UserAccount getFallback() {
        /*
         * first 3 come from the HttpCookie
         * next 3 are stubbed defaults
         */
        return new UserAccount(userCookie.userId, userCookie.name, userCookie.accountType, true, true, true);
    }

    /**
     * 表示cookie里面包含的一些属性
     * <p>
     * 一个真实的cookie能够解密一个安全的https cookie
     */
    private static class UserCookie {
        /**
         *
         * 将httpCookie转化为UserCookie，如果不是合法的就抛出IllegalArgumentException
         * @param cookie
         * @return UserCookie
         * @throws IllegalArgumentException 如果cookie不合法
         *
         */
        private static UserCookie parseCookie(HttpCookie cookie) {
            /* 这里只是模拟一下解析cookie的情况 */
            if (Math.random() < 0.998) {
                /* 合法 cookie */
                return new UserCookie(12345, "Henry Peter", 1);
            } else {
                /* 无效 cookie */
                throw new IllegalArgumentException();
            }
        }

        public UserCookie(int userId, String name, int accountType) {
            this.userId = userId;
            this.name = name;
            this.accountType = accountType;
        }

        private final int userId;
        private final String name;
        private final int accountType;
    }
}
