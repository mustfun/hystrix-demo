package com.dzy.learn.test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/4
 * @since 1.0
 */
public class CommandThatFaildSilently extends HystrixCommand<String> {

    private final boolean throwException;

    protected CommandThatFaildSilently( boolean throwException) {
        super(HystrixCommandGroupKey.Factory.asKey("SilentlyCommand"));
        this.throwException = throwException;
    }

    @Override
    protected String run() throws Exception {
        if (throwException){
            throw  new RuntimeException("我就喜欢抛个异常，咋地");
        }else{
            return "success";
        }
    }

    @Override
    protected String getFallback() {
        //return "success";
        return null;
    }

    public static class UnitTest{

        @Test
        public void testSliently(){
            Assert.assertEquals("success", new CommandThatFaildSilently(false).execute());
            Assert.assertEquals(null, new CommandThatFaildSilently(true).execute());
        }


        @Test
        public void testFailure() {
            try {
                Assert.assertEquals(null, new CommandThatFaildSilently(true).execute());
            } catch (HystrixRuntimeException e) {
                Assert.fail("we should not get an exception as we fail silently with a fallback");
            }
        }
    }
}
