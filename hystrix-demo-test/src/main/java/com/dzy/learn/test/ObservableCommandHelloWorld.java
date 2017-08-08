package com.dzy.learn.test;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.concurrent.ExecutionException;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/7/20
 * @since 1.0
 */
public class ObservableCommandHelloWorld extends HystrixObservableCommand<String> {

    private final String name;

    public ObservableCommandHelloWorld(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name = name;
    }

    @Override
    protected Observable<String> construct() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> observer) {
                try {
                    if (!observer.isUnsubscribed()) {
                        // 不要在这里做网络请求
                        //不要调用2次onNext方法
                        //observer.onNext("Hello");
                        observer.onNext("Hello "+name + "!");
                        observer.onCompleted();
                    }
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
        } ).subscribeOn(Schedulers.io());
    }

    public static class testObserve{


        /**
         * 没有execute方法提供执行，需要拿到观察者，然后使用阻塞模式
         * 转化为一个BlockingObserve，然后 执行单个节点，如果当前有多节点就抛出异常
         * @throws ExecutionException
         * @throws InterruptedException
         */
        @Test
        public void testObserve11() throws ExecutionException, InterruptedException {
            Observable<String> world = new ObservableCommandHelloWorld("world").observe();
            String single = world.toBlocking().single();
            System.out.println(single);
        }
    }
}
