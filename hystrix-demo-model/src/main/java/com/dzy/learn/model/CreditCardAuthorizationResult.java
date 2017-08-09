package com.dzy.learn.model;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 */
public class CreditCardAuthorizationResult {

    public static CreditCardAuthorizationResult createSuccessResponse(String transactionID, String authorizationCode) {
        return new CreditCardAuthorizationResult(true, transactionID, authorizationCode, false);
    }

    public static CreditCardAuthorizationResult createDuplicateSuccessResponse(String transactionID, String authorizationCode) {
        return new CreditCardAuthorizationResult(true, transactionID, authorizationCode, true);
    }

    public static CreditCardAuthorizationResult createFailedResponse(String message) {
        return new CreditCardAuthorizationResult(false, message, null, false);
    }

    private final boolean success;
    private final boolean isDuplicate;
    private final String authorizationCode;
    private final String transactionID;
    private final String errorMessage;

    /**
     * Private constructor that normally would be a horrible API as it re-uses different arguments for different state.
     * 私有构造函数通常是一个可怕的API，因为它对不同的状态使用不同的参数
     *
     * @param success
     * @param value
     * @param isResponseDuplicate
     *            boolean whether the response is the result of a duplicate transaction returning a previously submitted transaction result
     *            布尔是否该响应是重复事务返回先前提交的事务结果的结果
     *            <p>
     *            This is for handling the idempotent double-posting scenario, such as retries after timeouts.
     *            这是为了处理幂等的的双重发布场景，比如超时后的重试
     */
    private CreditCardAuthorizationResult(boolean success, String value, String value2, boolean isResponseDuplicate) {
        this.success = success;
        this.isDuplicate = isResponseDuplicate;
        if (success) {
            this.transactionID = value;
            this.authorizationCode = value2;
            this.errorMessage = null;
        } else {
            this.transactionID = null;
            this.errorMessage = value;
            this.authorizationCode = null;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * Whether this result was a duplicate transaction.
     *
     * @return boolean
     */
    public boolean isDuplicateTransaction() {
        return isDuplicate;
    }

    /**
     * If <code>isSuccess() == true</code> this will return the authorization code.
     * <p>
     * If <code>isSuccess() == false</code> this will return NULL.
     *
     * @return String
     */
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    /**
     * If <code>isSuccess() == true</code> this will return the transaction ID.
     * <p>
     * If <code>isSuccess() == false</code> this will return NULL.
     *
     * @return String
     */
    public String getTransactionID() {
        return transactionID;
    }

    /**
     * If <code>isSuccess() == false</code> this will return the error message.
     * <p>
     * If <code>isSuccess() == true</code> this will return NULL.
     *
     * @return String
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
