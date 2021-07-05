package slive.nsfly.transport.inter.exception;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/10 8:51 上午
 */
public class TransportException extends Exception {

    public TransportException() {
    }

    public TransportException(String message) {
        super(message);
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportException(Throwable cause) {
        super(cause);
    }

    public TransportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
