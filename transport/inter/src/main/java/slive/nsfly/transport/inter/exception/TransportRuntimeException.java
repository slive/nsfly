package slive.nsfly.transport.inter.exception;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/10 8:52 上午
 */
public class TransportRuntimeException extends RuntimeException {

    public TransportRuntimeException() {
    }

    public TransportRuntimeException(String message) {
        super(message);
    }

    public TransportRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportRuntimeException(Throwable cause) {
        super(cause);
    }

    public TransportRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
