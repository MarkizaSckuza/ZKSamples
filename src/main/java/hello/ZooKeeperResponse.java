package hello;

public class ZooKeeperResponse {

    private final String message;

    public ZooKeeperResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
