package com.dzy.learn.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixRequestCache;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategyDefault;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/4
 * @since 1.0
 */


/**
 * Example {@link HystrixCommand} implementation for handling the get-set-get use case within
 * a single request context so that the "set" can invalidate the cached "get".
 */
public class CommandUsingRequestCacheInvalidation {

    /* 远程数据存储 */
    private static volatile String prefixStoredOnRemoteDataStore = "ValueBeforeSet_";

    public static class GetterCommand extends HystrixCommand<String> {

        private static final HystrixCommandKey GETTER_KEY = HystrixCommandKey.Factory.asKey("GetterCommand");
        private final int id;

        public GetterCommand(int id) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetSetGet"))
                    .andCommandKey(GETTER_KEY));
            this.id = id;
        }

        @Override
        protected String run() {
            return prefixStoredOnRemoteDataStore + id;
        }

        @Override
        protected String getCacheKey() {
            return String.valueOf(id);
        }

        /**
         * 允许清空缓存
         *
         * @param id
         *
         */
        public static void flushCache(int id) {
            HystrixRequestCache.getInstance(GETTER_KEY,
                    HystrixConcurrencyStrategyDefault.getInstance()).clear(String.valueOf(id));
        }

    }

    public static class SetterCommand extends HystrixCommand<Void> {

        private final int id;
        private final String prefix;

        public SetterCommand(int id, String prefix) {
            super(HystrixCommandGroupKey.Factory.asKey("GetSetGet"));
            this.id = id;
            this.prefix = prefix;
        }

        @Override
        protected Void run() {
            // prefix参数给 prefixStoredOnRemoteDataStore
            prefixStoredOnRemoteDataStore = prefix;
            // 清空缓存
            GetterCommand.flushCache(id);
            // 不返回任何东西
            return null;
        }
    }

    public static class UnitTest {

        @Test
        public void getGetSetGet() {
            HystrixRequestContext context = HystrixRequestContext.initializeContext();
            try {
                assertEquals("ValueBeforeSet_1", new GetterCommand(1).execute());
                GetterCommand commandAgainstCache = new GetterCommand(1);
                assertEquals("ValueBeforeSet_1", commandAgainstCache.execute());
                // 第二遍是从缓存里面拿的
                assertTrue(commandAgainstCache.isResponseFromCache());
                // 设置新值
                new SetterCommand(1, "ValueAfterSet_").execute();
                // 重新拿取
                GetterCommand commandAfterSet = new GetterCommand(1);
                // getter应该返回一个新的前缀，不应该从缓存里面拿，设置为了ValueAfterSet_
                assertFalse(commandAfterSet.isResponseFromCache());
                assertEquals("ValueAfterSet_1", commandAfterSet.execute());
            } finally {
                context.shutdown();
            }
        }
    }

}
