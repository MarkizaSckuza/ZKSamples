package com.zk.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {

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

    public static boolean isValidConnectionParameters(String host, int port1, int port2, int clientPort) {
        Pattern pattern = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
        Matcher matcher = pattern.matcher(host);
        if (!matcher.matches()) {
            return false;
        }
        if (!isPortInAllowedRange(port1) || !isPortInAllowedRange(port2) || !isPortInAllowedRange(clientPort)) {
            return false;
        }
        if (isTheSamePort(port1, port2) || isTheSamePort(port1, clientPort) || isTheSamePort(port2, clientPort)) {
            return false;
        }
        return true;
    }

    public static String makeConnectionString(String sid, String host, String port1, String port2, String clientPort) {
        return !isNull(host) || !isNull(port1) || !isNull(port2) || !isNull(clientPort)
                ? new StringBuilder()
                    .append("server.")
                    .append(sid)
                    .append("=")
                    .append(host)
                    .append(":")
                    .append(port1)
                    .append(":")
                    .append(port2)
                    .append(";")
                    .append(clientPort)
                    .toString()
                : null;
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    private static boolean isPortInAllowedRange(int port) {
        return port > 1023 && port < 65536;
    }

    private static boolean isTheSamePort(int port1, int port2) {
        return port1 == port2;
    }
}
