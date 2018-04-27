package com.dzy.learn.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2018/4/25
 * @since 1.0
 */
public class WindowUtils {
    private static final Logger logger = LoggerFactory.getLogger(WindowUtils.class);

    public static final Func2<Integer, Integer, Integer> INTEGER_SUM =
            (integer, integer2) -> integer + integer2;

    public static final Func1<Observable<Integer>, Observable<Integer>> WINDOW_SUM =
            window -> window.scan(0, INTEGER_SUM).skip(3);

    public static final Func1<Observable<Integer>, Observable<Integer>> INNER_BUCKET_SUM =
            integerObservable -> integerObservable.reduce(0, INTEGER_SUM);

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        PublishSubject<Integer> publishSubject = PublishSubject.create();
        SerializedSubject<Integer, Integer> serializedSubject = publishSubject.toSerialized();

        serializedSubject
                // 1秒作为一个基本块
                .window(1000, TimeUnit.MILLISECONDS)
                // 基本块内数据求和
                .flatMap(INNER_BUCKET_SUM)
                // 积累3个事件，这里的事件是数字， 步长为1 发送
                .window(3, 1)
                // 窗口数据求和
                .flatMap(WINDOW_SUM)
                .subscribe((Integer integer) ->
                        // 输出统计数据到日志
                        logger.info("[{}] call ...... {}",
                                Thread.currentThread().getName(), integer));

        // 缓慢发送数据，观察效果
        serializedSubject.onNext(1);
        countDownLatch.await();
    }
}
