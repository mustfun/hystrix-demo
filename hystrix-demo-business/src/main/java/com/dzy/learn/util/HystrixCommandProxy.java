package com.dzy.learn.util;

import com.dzy.learn.annotation.Hystrix;
import com.dzy.learn.model.HystrixCommandConfig;
import com.dzy.learn.model.HystrixConfig;
import com.netflix.hystrix.HystrixCommand;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * cglib代理回调函数，如果实现了cglib代理，就会执行itercept方法
 * <p><a href='http://shensy.iteye.com/blog/1873155'>一篇讲cglib的博客</a></p>
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 */
public class HystrixCommandProxy<T> implements MethodInterceptor {

    private final static Logger LOGGER = Logger.getLogger(HystrixCommandProxy.class);

    private Enhancer enhancer = new Enhancer();

    protected List<HystrixConfig> set = new ArrayList<>();

    private T target;

    @SuppressWarnings("unchecked")
    public T getProxy(T target, List<HystrixConfig> set) {
        this.set = set;
        this.target = target;

        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(this);
        return (T) enhancer.create();
    }

    @Override
    public Object intercept(Object arg0, Method arg1, Object[] arg2, MethodProxy arg3) throws Throwable {
        for (HystrixConfig entry : set) {
            // 执行方法
            Method method = entry.getMethod();
            // 降级方法
            Method failMethod = entry.getFailMethod();
            String config = entry.getConfig();

            if (arg1.getName().equals(method.getName()) && arg1.getParameterCount() == method.getParameterCount()) {
                try {
                    HystrixCommandConfig hystrixConfig = (HystrixCommandConfig) ApplicationContextHolder.getApplicationContext().getBean(config);
                    if (hystrixConfig != null) {
                        HystrixFatory hystrixFatory = new HystrixFatory(hystrixConfig);
                        HystrixCommand<Object> command = hystrixFatory.create(arg3, arg0, arg2, failMethod, target);
                        Object result = command.execute();
                        return result;
                    } else {
                        LOGGER.error("断路器配置：" + config + "没有被找到");
                    }

                    break;
                } catch (Exception e) {
                    LOGGER.error("断路器：" + config + "在执行方法：" + method.getName() + "时发生异常");
                    e.printStackTrace();
                }
            }
        }

        return arg3.invokeSuper(arg0, arg2);
    }
}
