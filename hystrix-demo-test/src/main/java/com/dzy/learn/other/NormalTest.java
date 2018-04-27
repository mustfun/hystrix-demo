package com.dzy.learn.other;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCircuitBreaker;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.*;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    }


    /**
     * 跟lambda里面的map很像，传入4个String ， 返回一个Observable对象
     */
    @Test
    public void flatMapTest() throws InterruptedException {


        List<Integer> list = Arrays.asList(10, 5, 3, 2, 1, 0);

        Observable.from(list).flatMap(new Func1<Integer, Observable<?>>() {
            @Override
            public Observable<?> call(Integer num) {
                List<String> strings = Arrays.asList("|" + num + "|", "|" + num * 10 + "|", "|" + num * 100 + "|");
                return Observable.from(strings);
            }
        }).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                LOG.info("新转化后的字符串是 = {}" , o);
            }
        });



        List<Object> flatMapcollect = list.stream().flatMap(new Function<Integer, Stream<?>>() {
            @Override
            public Stream<?> apply(Integer integer) {
                //通过list里面的元素创建一个list返回回去
                List<Integer> list1 = Arrays.asList(integer, integer + 10, integer + 100, integer + 1000);
                return list1.stream();
            }
        }).collect(Collectors.toList());

        flatMapcollect.forEach(s->LOG.info("complex integer  = {}" , s));

        Thread.sleep(5000);

    }

    /**
     * 上一次操作的结果传递给这一次 作为参数使用，有点类似于递归
     */
    @Test
    public void testReduce(){
        List<Integer> list = Arrays.asList(10, 5, 3, 2, 1, 0);



        Observable.from(list).reduce(new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer result, Integer num) {
                        LOG.info("开始前： result {}, num = {}" , result,num);
                        result+=num;
                        return result;
                    }
                }).subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        LOG.info("result = {}" , result);
                    }
                });

        Observable.from(list)
                .scan(new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer result, Integer num) {
                        LOG.info("开始前： result {}, num = {}" , result,num);
                        result+=num;
                        return result;
                    }
                }).subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        LOG.info("result = {}" , result);
                    }
                });


    }

    @Test
    public void testMap(){
        List<Integer> list = Arrays.asList(10, 5, 3, 2, 1, 0);

        Observable.from(list)
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        return integer+"变成str";
                    }
                })
                .subscribe(s -> LOG.info("s = {}" , s));
    }

    @Test
    public void testWindow() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Observable inputEventStream = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                subscriber.onNext("我是生产者.........");
            }
        });

        inputEventStream.window(1000,TimeUnit.MILLISECONDS).subscribe(new Action1() {
            @Override
            public void call(Object o) {
                Calendar calendar = Calendar.getInstance();
                int i = calendar.get(Calendar.SECOND);
                LOG.info("我会{}就被唤醒触发...",i);
            }
        });
        countDownLatch.await();



        /*Observable.from(list).window(2, 2).subscribe(new Action1<Observable<Integer>>() {
            @Override
            public void call(Observable<Integer> integerObservable) {
                integerObservable.reduce((sum, num) -> sum+=num).subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        LOG.info("我被2个打印一次 = {}" , integer);
                    }
                });
            }
        });*/
        countDownLatch.await();
    }


    /**
     * 两个数字相加，reduce，scan用
     */
    public static final Func2<Integer, Integer, Integer> PUBLIC_SUM =
            (integer, integer2) -> integer + integer2;

    public static final Func1<Observable<Integer>, Observable<Integer>> WINDOW_SUM =
            //跳过前3个数据
            window -> window.scan(0, PUBLIC_SUM).skip(1);

    public static final Func1<Observable<Integer>, Observable<Integer>> INNER_BUCKET_SUM =
            integerObservable -> integerObservable.reduce(0, PUBLIC_SUM);

    private Integer sum = 0;

    @Test
    public void testWindowSlide() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        BehaviorSubject<Integer> behaviorSubject = BehaviorSubject.create();
        behaviorSubject
                // 1秒作为一个基本块,横向移动
                .window(1000, TimeUnit.MILLISECONDS)
                //将flatMap汇总平铺成一个事件,然后累加成一个Observable<Integer>对象，比如说1s内有10个对象，被累加起来
                .flatMap(INNER_BUCKET_SUM)
                //对这个对象2个发送，步长为1
                .window(2,1)
                //对窗口里面的进行求和,用的scan, 每次累加都会打印出来
                .flatMap(WINDOW_SUM)
                .subscribe((Integer integer) ->
                        // 输出统计数据到日志
                        LOG.info("[{}] call ...... {}",
                                Thread.currentThread().getName(), integer));

        for (int i = 0; i < 1000; i++) {
            //200ms生产一个数据，
            behaviorSubject.onNext(i);
            LOG.info("i = {}" ,i);
            Thread.sleep(200);
        }
        countDownLatch.await();
    }
}
