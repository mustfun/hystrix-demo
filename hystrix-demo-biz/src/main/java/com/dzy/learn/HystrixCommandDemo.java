package com.dzy.learn;

import com.dzy.learn.command.CreditCardCommand;
import com.dzy.learn.command.GetOrderCommand;
import com.dzy.learn.command.GetPaymentInformationCommand;
import com.dzy.learn.command.GetUserAccountCommand;
import com.dzy.learn.model.Order;
import com.dzy.learn.model.PaymentInformation;
import com.dzy.learn.model.UserAccount;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.math.BigDecimal;
import java.net.HttpCookie;
import java.util.concurrent.*;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 */
public class HystrixCommandDemo {

    /**
     * 使用调用方运行策略，这样我们就可以继续迭代并添加它，它将在满时阻塞
     * 达到5个线程的时候开始阻塞
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 5, 5, TimeUnit.DAYS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());


    /**
     * 监控线程
     */
    private final ThreadPoolTaskExecutor monitorPool = new ThreadPoolTaskExecutor();


    public static void main(String[] args) {
        new HystrixCommandDemo().startDemo();
    }

    public void startDemo() {
        startMetricsMonitor();
        while (true) {
            runSimulatedRequestOnThread();
        }
    }

    public HystrixCommandDemo() {
        /*
         * 我们不会使用注入的属性，而是通过Archaius来设置它们
         * 所以代码的其余部分从外部获取的属性就像在真实系统中一样
         *
         */
        ConfigurationManager.getConfigInstance().setProperty("hystrix.threadpool.default.coreSize", 8);
        //设置CreditCardCommand的超时时间，为300ms
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CreditCardCommand.execution.isolation.thread.timeoutInMilliseconds", 3000);
        //设置UserAccount这个command的超时时间为50ms
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.GetUserAccountCommand.execution.isolation.thread.timeoutInMilliseconds", 50);
        // 将滚动百分比设置为更细粒度，这样我们就可以看到数据每秒钟都在变化，而不是每10秒就发生默认值
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.metrics.rollingPercentile.numBuckets", 60);
    }



    public void startMetricsMonitor(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //我们使用默认名称，所以可以使用 class.getSimpleName() 来派生键,HystrixCommandKey
                    //这样就可以找到那个group
                    HystrixCommandMetrics creditCardMetrics = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey(CreditCardCommand.class.getSimpleName()));
                    HystrixCommandMetrics orderMetrics = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey(GetOrderCommand.class.getSimpleName()));
                    HystrixCommandMetrics userAccountMetrics  = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey(GetUserAccountCommand.class.getSimpleName()));
                    HystrixCommandMetrics paymentInformationMetrics  = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey(GetPaymentInformationCommand.class.getSimpleName()));

                    // 打印出权值
                    StringBuilder out = new StringBuilder();
                    out.append("\n");
                    out.append("#####################################################################################").append("\n");
                    out.append("# CreditCardCommand: " + getStatsStringFromMetrics(creditCardMetrics)).append("\n");
                    out.append("# GetOrderCommand: " + getStatsStringFromMetrics(orderMetrics)).append("\n");
                    out.append("# GetUserAccountCommand: " + getStatsStringFromMetrics(userAccountMetrics)).append("\n");
                    out.append("# GetPaymentInformationCommand: " + getStatsStringFromMetrics(paymentInformationMetrics)).append("\n");
                    out.append("#####################################################################################").append("\n");
                    System.out.println(out.toString());



                }
            }


            /**
             * 每个线程都有一个getStatsStringFromMetrics
             * @param metrics
             * @return
             */
            private String getStatsStringFromMetrics(HystrixCommandMetrics metrics) {
                StringBuilder m = new StringBuilder();
                if (metrics != null) {
                    HystrixCommandMetrics.HealthCounts health = metrics.getHealthCounts();
                    m.append("Requests: ").append(health.getTotalRequests()).append(" ");
                    m.append("Errors: ").append(health.getErrorCount()).append(" (").append(health.getErrorPercentage()).append("%)   ");
                    m.append("Mean: ").append(metrics.getExecutionTimePercentile(50)).append(" ");
                    m.append("75th: ").append(metrics.getExecutionTimePercentile(75)).append(" ");
                    m.append("90th: ").append(metrics.getExecutionTimePercentile(90)).append(" ");
                    m.append("99th: ").append(metrics.getExecutionTimePercentile(99)).append(" ");
                }
                return m.toString();
            }

        };
        //设置为守护进程
        monitorPool.setDaemon(true);
        monitorPool.initialize();
        monitorPool.execute(runnable);
    }


    public void runSimulatedRequestOnThread() {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                HystrixRequestContext context = HystrixRequestContext.initializeContext();
                try {
                    executeSimulatedUserRequestForOrderConfirmationAndCreditCardPayment();

                    System.out.println("Request => " + HystrixRequestLog.getCurrentRequest().getExecutedCommandsAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    context.shutdown();
                }
            }

        });
    }


    public void executeSimulatedUserRequestForOrderConfirmationAndCreditCardPayment() throws InterruptedException, ExecutionException {
        /* fetch user object with http cookies */
        UserAccount user = new GetUserAccountCommand(new HttpCookie("mockKey", "mockValueFromHttpRequest")).execute();

        /* 为用户获取支付信息(异步)，这样就可以进行信用卡支付 */
        Future<PaymentInformation> paymentInformation = new GetPaymentInformationCommand(user).queue();

        /* 获取我们为用户处理的订单,获取订单信息 */
        long orderIdFromRequestArgument = 13579L;
        Order previouslySavedOrder = new GetOrderCommand(orderIdFromRequestArgument).execute();

        CreditCardCommand credit = new CreditCardCommand(previouslySavedOrder, paymentInformation.get(), new BigDecimal(123.45));
        credit.execute();
    }




}
