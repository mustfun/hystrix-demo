package com.dzy.learn.test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import static org.junit.Assert.*;
import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/7/20
 * @since 1.0
 */
public class CommandHelloWorld extends HystrixCommand<String> {

    private String name;

    protected CommandHelloWorld(String name) {
        //貌似构造方法都需要飞一个groupkey
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name=name;
    }

    @Override
    protected String run() throws Exception {
        return "hello "+name+"!";
    }

    public static class testHello{
        //测试同步

        /**
         * HystrixCommand里面的execute方法能够让方法同步执行
         */
        @Test
        public void testSynchronous(){
            assertEquals("hello world!",new CommandHelloWorld("world").execute());
            //下面会报错，不相等
            assertEquals("hello Bob!",new CommandHelloWorld("Bobc").execute());
        }

        /**
         * 通过得到queue，然后异步执行，方法
         * @throws ExecutionException
         * @throws InterruptedException
         */
        @Test
        public void testSynchronousQuene() throws ExecutionException, InterruptedException {
            assertEquals("hello world!",new CommandHelloWorld("world").queue().get());
            assertEquals("hello Bob!",new CommandHelloWorld("Bob").queue().get());
        }

        @Test
        public void testSynchronousFuture() throws ExecutionException, InterruptedException {
            Future<String> world = new CommandHelloWorld("world").queue();
            Future<String> bob = new CommandHelloWorld("Bob").queue();
            assertEquals("hello world!",world.get());
            assertEquals("hello Bob!",bob.get());
        }

        @Test
        public void testSynchronousObserve() throws ExecutionException, InterruptedException {
            //返回一个热启动的Observable，可以立即执行该命令，虽然由于Observable可以通过ReplaySubject进行过滤，但是在您有机会订阅之前，您没有丢失它发出的任何物品的危险
            Observable<String> world = new CommandHelloWorld("world").observe();
            Observable<String> bob = new CommandHelloWorld("Bob").observe();

            //返回一个冷启动的Observable
            //Observable<String> co = new CommandHelloWorld("World").toObservable();

            //转变为阻塞观察者
            assertEquals("hello world!",world.toBlocking().single());
            assertEquals("hello Bob!",bob.toBlocking().single());
            //非阻塞的
            world.subscribe(new Observer<String>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onNext(String s) {
                    System.out.println("do next: "+s);
                }
            });

            world.subscribe(s-> System.out.println("lambda do next: "+s), Throwable::printStackTrace);

            /*bob.subscribe(new Action1<String>() {
                @Override
                public void call(String s) {
                    System.out.println("on next: " +s);
                }
            });*/
            bob.subscribe(s -> System.out.println("on next: " +s));

        }




    }


}
