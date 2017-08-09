package com.dzy.learn.command;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 */
import java.math.BigDecimal;
import java.net.HttpCookie;

import com.dzy.learn.model.CreditCardAuthorizationResult;
import com.dzy.learn.model.Order;
import com.dzy.learn.model.PaymentInformation;
import com.dzy.learn.model.UserAccount;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * This class was originally taken from a functional example using the Authorize.net API
 * but was modified for this example to use mock classes so that the real API does not need
 * to be depended upon and so that a backend account with Authorize.net is not needed.
 */
// import net.authorize.Environment;
// import net.authorize.TransactionType;
// import net.authorize.aim.Result;
// import net.authorize.aim.Transaction;

/**
 * 信用卡支出用的HystrixCommand.
 * <p>
 * 没有fallback信用卡故障的，回退必须导致错误，因为没有逻辑回退
 * <p>
 *
 *  这个实现源自于一个授权的函数HystrixCommand包装器。Authorize.net API
 * <p>
 * 使用了 Authorize.net '重复的窗口' 设置 确保Order能被提交多次
 * 而且它会表现出幂等性，所以这样不会导致重复的事务，每一次请求都能够得到正确的返回， 就像是第一次或者唯一一次执行
 * <p>
 * 这种幂等性 (在重复窗口时间框架 中设置为多个小时) 允许客户端经历超时，失败 和 重试 信用卡事务，不用担心信用卡多次充值
 *
 * <p>
 *
 * 这又允许将HystrixCommand配置为合理的超时和隔离，而不是让它运行10 +秒，希望在延迟发生时获得成功
 * <p>
 *     在本例中，超时设置为3000毫秒，正常行为通常在1300毫秒左右看到信用卡交易，在这种情况下，最好是等待更长时间，并尝试成功，因为结果是一个用户错误
 *
 * <p>
 *
 * 我们不想等待10-20sAuthorize.net可能在大容量流量下的严重资源饱和时，进行流量削峰
 *
 */
public class CreditCardCommand extends HystrixCommand<CreditCardAuthorizationResult> {
    private final static AuthorizeNetGateway DEFAULT_GATEWAY = new AuthorizeNetGateway();

    private final AuthorizeNetGateway gateway;
    private final Order order;
    private final PaymentInformation payment;
    private final BigDecimal amount;

    /**
     *
     * HystrixCommand 实现接受参数到构造函数，然后<code>run()</code>方法执行时候，就能够访问到了
     * @param order  用户账户
     * @param payment 信用卡信息
     * @param amount  支付金额
     */
    public CreditCardCommand(Order order, PaymentInformation payment, BigDecimal amount) {
        this(DEFAULT_GATEWAY, order, payment, amount);
    }

    private CreditCardCommand(AuthorizeNetGateway gateway, Order order, PaymentInformation payment, BigDecimal amount) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("CreditCard"))
                //默认为一个相当长的超时值，因为失败的信用卡交易是一个糟糕的用户体验和“昂贵的”重新尝试，3000ms
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(3000)));
        this.gateway = gateway;
        this.order = order;
        this.payment = payment;
        this.amount = amount;
    }

    /**
     * 实际的提交鉴权发生在<code>run()</code>方法里面
     */
    @Override
    protected CreditCardAuthorizationResult run() {

        // 模拟来自CreditCardCommand 到GetUserAccountCommand 的传递依赖
        // 可以将用户帐户注入这个命令作为参数,这会更加精确,这里是执行
        // 但通常在大型的代码库中，不会这样去做，因为每个库都会获取公共数据，userAccount显然是一个公共资源
        // 比如用户信息，比如这个例子
        UserAccount user = new GetUserAccountCommand(new HttpCookie("mockKey", "mockValueFromHttpRequest")).execute();
        if (user.getAccountType() == 1) {
            // do something
        } else {
            // do something else
        }

        // 进行信用卡交易，有可能失败或者超时
        Result<Transaction> result = gateway.submit(payment.getCreditCardNumber(),
                String.valueOf(payment.getExpirationMonth()),
                String.valueOf(payment.getExpirationYear()),
                TransactionType.AUTH_CAPTURE, amount, order);

        if (result.isApproved()) {
            return CreditCardAuthorizationResult.createSuccessResponse(result.getTarget().getTransactionId(), result.getTarget().getAuthorizationCode());
        } else if (result.isDeclined()) {
            //被拒绝
            return CreditCardAuthorizationResult.createFailedResponse(result.getReasonResponseCode() + " : " + result.getResponseText());
        } else {
            // 检查重复的交易
            if (result.getReasonResponseCode().getResponseReasonCode() == 11) {
                if (result.getTarget().getAuthorizationCode() != null) {
                    // 我们将把它视为成功，因为它告诉我们，我们有一个成功的授权代码
                    // 只是我们试图在“复制窗口”的时间段重新发布。
                    // 这是我们需要的幂等行为的一部分，这样我们就可以安全地超时和/或失败和允许
                    // 客户端应用程序重新尝试为相同的订单提交信用卡事务。
                    // 在这些情况下，如果客户看到失败，但是事务实际上成功了，这将捕获
                    // 重复的响应和对客户端的行为是成功的。
                    return CreditCardAuthorizationResult.createDuplicateSuccessResponse(result.getTarget().getTransactionId(), result.getTarget().getAuthorizationCode());
                }
            }
            // handle all other errors
            return CreditCardAuthorizationResult.createFailedResponse(result.getReasonResponseCode() + " : " + result.getResponseText());
            /**
             * NOTE that in this use case we do not throw an exception for an "error" as this type of error from the service is not a system error,
             * but a legitimate usage problem successfully delivered back from the service.
             *
             * Unexpected errors will be allowed to throw RuntimeExceptions.
             *
             * The HystrixBadRequestException could potentially be used here, but with such a complex set of errors and reason codes
             * it was chosen to stick with the response object approach rather than using an exception.
             */
        }
    }

    /*
     *
     * 下面的内部类都基于Authorize.net 的 API进行模拟
     * 在这个示例中，它们被静态地模拟，以演示在包装这种类型调用时，Hystrix可能会如何表现
     */

    public static class AuthorizeNetGateway {
        public AuthorizeNetGateway() {

        }

        public Result<Transaction> submit(String creditCardNumber, String expirationMonth, String expirationYear, TransactionType authCapture, BigDecimal amount, Order order) {
            /* 模拟不同长度的800 - 1500毫秒，这是典型的信用卡交易耗费的时间 */
            try {
                Thread.sleep((int) (Math.random() * 700) + 800);
                System.out.println("正在进行信用卡交易.....");
            } catch (InterruptedException e) {
                // 不做处理
            }

            /* 每隔一段时间，我们就会使它超过3000毫秒，这会导致命令超时 */
            if (Math.random() > 0.99) {
                try {
                    Thread.sleep(8000);
                    System.out.println("信用卡交易超时发生啦.......");
                } catch (InterruptedException e) {
                    // 不处理
                }
            }

            if (Math.random() < 0.8) {
                //80%的概率验证通过
                return new Result<Transaction>(true);
            } else {
                //20%的概率验证不通过
                return new Result<Transaction>(false);
            }

        }
    }

    public static class Result<T> {

        private final boolean approved;

        public Result(boolean approved) {
            this.approved = approved;
        }

        public boolean isApproved() {
            return approved;
        }

        public ResponseCode getResponseText() {
            return null;
        }

        public Target getTarget() {
            return new Target();
        }

        public ResponseCode getReasonResponseCode() {
            return new ResponseCode();
        }

        public boolean isDeclined() {
            return !approved;
        }

    }

    public static class ResponseCode {

        public int getResponseReasonCode() {
            return 0;
        }

    }

    public static class Target {

        public String getTransactionId() {
            return "transactionId";
        }

        public String getAuthorizationCode() {
            return "authorizedCode";
        }

    }

    public static class Transaction {

    }

    public static enum TransactionType {
        AUTH_CAPTURE
    }

}
