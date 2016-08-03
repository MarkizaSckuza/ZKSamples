package exception;

public class ZooException extends Exception {

    private int code;

    public ZooException(int code) {
        if (code < 0) {
            code *= -1;
        }
        this.code = code;
    }

    public ZooException(String message) {
        super(message);
    }

    public ZooException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZooException(Throwable cause) {
        super(cause);
    }

    protected ZooException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getCode() {
        return code;
    }
}
