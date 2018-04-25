package com.dzy.learn.other;

import com.netflix.hystrix.HystrixCircuitBreaker;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.*;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2018/4/23
 * @since 1.0
 */
public class NormalTest {

    private static final Logger LOG = LoggerFactory.getLogger(NormalTest.class);

    private final BehaviorSubject<String> counterSubject = BehaviorSubject.create();


    @Test
    public void 测试观察者模式(){
        /**
         * 我是被观察者，我要执行3个操作
         */
        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("Hi");
                subscriber.onNext("Aloha");
                subscriber.onCompleted();
            }
        });

        /**
         * 我是订阅者
         */
        observable.subscribe(new Observer() {
            @Override
            public void onCompleted() {
                LOG.info("我被执行完毕了");
            }

            @Override
            public void onError(Throwable e) {
                LOG.info("我出现异常了");
            }

            @Override
            public void onNext(Object o) {
                LOG.info("我要继续调用");
            }
        });

        observable.subscribe(new Action1() {
            @Override
            public void call(Object o) {
                LOG.info("我是第二个消息订阅者....");
            }
        });
    }

    /**
     * 第一个参数是事件， 第二个参数 事件返回值  ，第三个参数是输出
     */
    public static final Func2<long[], String, long[]> appendRawEventToBucket = new Func2<long[], String, long[]>() {
        @Override
        public long[] call(long[] initialCountArray, String execution) {
            LOG.info("我是static的方法....");
            return initialCountArray;
        }
    };


    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch=new CountDownLatch(2);


        NormalTest normalTest = new NormalTest();
        normalTest.测试观察者模式_窗口模式();


        latch.await();
    }


    @Test
    public void 测试观察者模式_窗口模式(){
        /**
         * 我是被观察者，我要执行3个操作
         */
        Observable inputEventStream = Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            LOG.info("我是最开始的三次Event操作.....");
            subscriber.onNext("Hello");
            subscriber.onNext("Hi");
            subscriber.onNext("Aloha");
            //subscriber.onCompleted(); 这句话加了生产者就生产完了，生产者就不生产东西了，困扰了我2天
        });


        /**
         * 第一个参数是桶 ， 第二个参数是  output
         */
        Func1<Observable<String>,Observable<long[]>> reduceBucketToSummary = new Func1<Observable<String>, Observable<long[]>>() {
            @Override
            public Observable<long[]> call(Observable<String> observable) {
                LOG.info("我是被减的");
                return observable.reduce(new long[1], appendRawEventToBucket);
            }
        } ;


        /**
         * 被观察者
         */
        Observable<String> bucketedStream  = Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                LOG.info("我是源数据流，循环执行.....");
                return inputEventStream
                        .window(1000, TimeUnit.MILLISECONDS)
                        .flatMap(reduceBucketToSummary)
                        .startWith(new long[1]);
            }
        });

        Observable<Observable<String>> sourceStream = bucketedStream
                .window(10, 1)
                .share()
                .onBackpressureDrop();

        /**
         * 我是订阅者
         */
        sourceStream.subscribeOn(Schedulers.io()).subscribe(new Observer() {
            @Override
            public void onCompleted() {
                LOG.info("我被执行完毕了");
            }

            @Override
            public void onError(Throwable e) {
                LOG.info("我出现异常了");
            }

            @Override
            public void onNext(Object o) {
                LOG.info("我要继续调用");
            }
        });

        //Observable<Long> longObservable = Observable.interval(1, TimeUnit.SECONDS).observeOn(Schedulers.io());
        //longObservable.subscribe(i -> LOG.info("bufferTime:" + i));

    }


}
