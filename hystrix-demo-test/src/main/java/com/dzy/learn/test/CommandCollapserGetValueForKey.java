package com.dzy.learn.test;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/3
 * @since 1.0
 */
public class CommandCollapserGetValueForKey extends HystrixCollapser<List<String>,String,Integer> {

    private final Integer key;

    public CommandCollapserGetValueForKey(Integer key) {
        this.key = key;
    }

    @Override
    public Integer getRequestArgument() {
        return key;
    }

    @Override
    protected HystrixCommand<List<String>> createCommand(Collection<CollapsedRequest<String, Integer>> collection) {
        return new BatchCommond(collection);
    }

    /**
     *
     * @param batchResponse 这里batchResponse指的是批处理执行的response
     * @param collection
     */
    @Override
    protected void mapResponseToRequests(List<String> batchResponse, Collection<CollapsedRequest<String, Integer>> collection) {
        int count=0;
        for (CollapsedRequest<String, Integer> stringIntegerCollapsedRequest : collection) {
            stringIntegerCollapsedRequest.setResponse(batchResponse.get(count++));
        }
    }


    /**
     * 返回一个List<String>string</string></String>的commond
     */
    private static final class BatchCommond extends HystrixCommand<List<String>>{

        private final Collection<CollapsedRequest<String,Integer>> requests;

        private BatchCommond(Collection<CollapsedRequest<String, Integer>> requests) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("commandGroupKey"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("getValueForKey")));
            this.requests = requests;
        }

        @Override
        protected List<String> run() throws Exception {
            //把批次请求的参数放到一个list里面
            List<String> response=new ArrayList<>();
            for (CollapsedRequest<String, Integer> request : requests) {
                response.add("ValueForKey"+request.getArgument());
            }
            return response;
        }
    }

    public static class unitTest{
        @Test
        public void testCollapaser() throws Exception{

            HystrixRequestContext requestContext=HystrixRequestContext.initializeContext();

            try{
                Future<String> queue1 = new CommandCollapserGetValueForKey(1).queue();
                Future<String> queue2 = new CommandCollapserGetValueForKey(2).queue();
                Future<String> queue3 = new CommandCollapserGetValueForKey(3).queue();
                Future<String> queue4 = new CommandCollapserGetValueForKey(4).queue();

                String s = queue1.get();
                System.out.println(s);
                Assert.assertEquals("ValueForKey1",s);
                Assert.assertEquals("ValueForKey2",queue2.get());
                Assert.assertEquals("ValueForKey3",queue3.get());
                Assert.assertEquals("ValueForKey4",queue4.get());

                int numExecuted = HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().size();

                System.out.println("numExecuted = "+numExecuted);

                if (numExecuted>2){
                    Assert.fail("有一些请求没有被合并，囧");
                }

                System.err.println("HystrixRequestLog.getCurrentRequest().getAllExecutedCommands() = "+HystrixRequestLog.getCurrentRequest().getAllExecutedCommands());

                //下面这个方法已经废弃
                //HystrixCommand<?> command = HystrixRequestLog.getCurrentRequest().getExecutedCommands().toArray(new HystrixCommand<?>[1])[0];

                for (HystrixInvokableInfo<?> command : HystrixRequestLog.getCurrentRequest().getAllExecutedCommands()) {

                    System.out.println("command key的名称 = " + command.getCommandKey().name());

                    System.err.println(command.getCommandKey().name() + " => command.getExecutionEvents(): " + command.getExecutionEvents());

                    Assert.assertTrue(command.getExecutionEvents().contains(HystrixEventType.SUCCESS));
                    Assert.assertTrue(command.getExecutionEvents().contains(HystrixEventType.COLLAPSED));
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                requestContext.shutdown();
            }

        }
    }
}
