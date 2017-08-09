package com.dzy.learn.command;

import com.dzy.learn.model.PaymentInformation;
import com.dzy.learn.model.UserAccount;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 */
public class GetPaymentInformationCommand extends HystrixCommand<PaymentInformation> {

    private final UserAccount user;

    public GetPaymentInformationCommand(UserAccount user) {
        super(HystrixCommandGroupKey.Factory.asKey("PaymentInformation"));
        this.user = user;
    }

    @Override
    protected PaymentInformation run() {
        /* simulate performing network call to retrieve order */
        try {
            Thread.sleep((int) (Math.random() * 20) + 5);
        } catch (InterruptedException e) {
            // do nothing
        }

        /* fail rarely ... but allow failure */
        if (Math.random() > 0.9999) {
            throw new RuntimeException("random failure loading payment information over network");
        }

        /* latency spike 2% of the time */
        if (Math.random() > 0.98) {
            // random latency spike
            try {
                Thread.sleep((int) (Math.random() * 100) + 25);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        /* success ... create (a very insecure) PaymentInformation with data "from" the remote service response */
        return new PaymentInformation(user, "4444888833337777", 12, 15);
    }

}
