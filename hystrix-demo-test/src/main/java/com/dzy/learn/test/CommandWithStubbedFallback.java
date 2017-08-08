package com.dzy.learn.test;

import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/4
 * @since 1.0
 */
public class CommandWithStubbedFallback extends HystrixCommand<CommandWithStubbedFallback.UserAccount>{

    private final int customerId;
    private final String countryCodeFromGeoLookup;


    protected CommandWithStubbedFallback( int customerId, String countryCodeFromGeoLookup) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.customerId = customerId;
        this.countryCodeFromGeoLookup = countryCodeFromGeoLookup;
    }

    @Override
    protected UserAccount run() throws Exception {
        throw new RuntimeException("我就想让这里抛个异常，咋地~");
    }

    /**
     * 这里的customerId，countryCodeFromGeoLookup是从上一个请求拿到的
     * 当发生错误的时候能够从上一个请求拿到值并返回，其它的为默认的
     * @return
     */
    @Override
    protected UserAccount getFallback() {
        return new UserAccount(customerId, "Unknown Name",
                countryCodeFromGeoLookup, true, true, false);
    }

    public static class UnitTest {

        @Test
        public void test() {
            CommandWithStubbedFallback command = new CommandWithStubbedFallback(1234, "ca");
            UserAccount account = command.execute();
            Assert.assertTrue(command.isFailedExecution());
            Assert.assertTrue(command.isResponseFromFallback());
            Assert.assertEquals(1234, account.customerId);
            Assert.assertEquals("ca", account.countryCode);
            Assert.assertEquals(true, account.isFeatureXPermitted);
            Assert.assertEquals(true, account.isFeatureYPermitted);
            Assert.assertEquals(false, account.isFeatureZPermitted);

            CommandWithStubbedFallback command2 = new CommandWithStubbedFallback(1111, "aaaaa");

            UserAccount execute = command2.execute();
            System.out.println(JSON.toJSONString(execute));
        }
    }


    public static class UserAccount{
        private final int customerId;
        private final String name;
        private final String countryCode;
        private final boolean isFeatureXPermitted;
        private final boolean isFeatureYPermitted;
        private final boolean isFeatureZPermitted;

        public UserAccount(int customerId, String name, String countryCode, boolean isFeatureXPermitted, boolean isFeatureYPermitted, boolean isFeatureZPermitted) {
            this.customerId = customerId;
            this.name = name;
            this.countryCode = countryCode;
            this.isFeatureXPermitted = isFeatureXPermitted;
            this.isFeatureYPermitted = isFeatureYPermitted;
            this.isFeatureZPermitted = isFeatureZPermitted;
        }

        public int getCustomerId() {
            return customerId;
        }

        public String getName() {
            return name;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public boolean isFeatureXPermitted() {
            return isFeatureXPermitted;
        }

        public boolean isFeatureYPermitted() {
            return isFeatureYPermitted;
        }

        public boolean isFeatureZPermitted() {
            return isFeatureZPermitted;
        }
    }
}
