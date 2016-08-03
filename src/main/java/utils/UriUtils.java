package utils;

import java.net.URI;
import java.net.URISyntaxException;

public class UriUtils {

    public static URI createUri(String hostPort) throws URISyntaxException {
        return new URI("scheme://" + hostPort);
    }

    public static boolean isHostPortValid(String hostPort) {
        try {
            URI uri = createUri(hostPort);
            if (uri.getHost() == null || uri.getPort() == -1) {
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }
}
