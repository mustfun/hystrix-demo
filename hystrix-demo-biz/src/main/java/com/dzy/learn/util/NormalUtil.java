package com.dzy.learn.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.dzy.learn.util.WindowUtils.INNER_BUCKET_SUM;
import static com.dzy.learn.util.WindowUtils.WINDOW_SUM;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2018/4/23
 * @since 1.0
 */
public class NormalUtil {

    private static final Logger LOG = LoggerFactory.getLogger(NormalUtil.class);

    private final BehaviorSubject<String> counterSubject = BehaviorSubject.create();


    public static void main(String[] args) throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);


        Observable inputEventStream = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                subscriber.onNext("我是生产者.........");
            }
        });

        BehaviorSubject<Integer> behaviorSubject = BehaviorSubject.create();

        /*behaviorSubject
                // 5秒作为一个基本块
                .window(1000, TimeUnit.MILLISECONDS)
                // 基本块内数据求和
                .flatMap(INNER_BUCKET_SUM)
                // 积累3个事件，这里的事件是数字， 步长为1 发送
                .window(3, 1)
                // 窗口数据求和
                .flatMap(WINDOW_SUM)
                .subscribe((Integer integer) ->
                        // 输出统计数据到日志
                        LOG.info("[{}] 我被唤醒 ...... {}",
                                Thread.currentThread().getName(), integer));
        */

        behaviorSubject
                // 5秒作为一个基本块
                .window(1000, TimeUnit.MILLISECONDS)
                // 积累3个事件，这里的事件是数字， 步长为1 发送
                .window(3, 1)
                .subscribe(new Action1<Observable<Observable<Integer>>>() {
                    @Override
                    public void call(Observable<Observable<Integer>> observableObservable) {
                        LOG.info("=======");
                    }
                });

        BehaviorSubject subject = BehaviorSubject.create();
        //发送一个数据流
        subject.onNext(1);
        //再发送一个数据流
        subject.onNext(2);

        subject.window(500,TimeUnit.MILLISECONDS).subscribe(o -> LOG.info("我在监控最新的值......."+o));

        subject.onNext(3);    //再发送一个数据流

        inputEventStream.window(500,TimeUnit.MILLISECONDS).subscribe(new Action1() {
            @Override
            public void call(Object o) {
                LOG.info("我是普通订阅这...."+o);
            }
        });

        countDownLatch.await();
    }


}
