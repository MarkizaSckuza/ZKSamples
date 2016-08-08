package com.zk.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public class ZooUtils {

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

    public static String getInfo(String host, int port) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String result;

        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("stat");
            StringBuilder builder = new StringBuilder();
            String s = in.readLine();
            while (s != null) {
                builder.append(s).append("\n");
                s = in.readLine();
            }

            result = builder.toString();
        } catch (IOException e) {
            return null;
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        return result;
    }

    public static boolean isValidConnectionParameters(String host, int port1, int port2, int clientPort) {
        Pattern pattern = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
        Matcher matcher = pattern.matcher(host);
        return matcher.matches()
                && !(!isPortInAllowedRange(port1) || !isPortInAllowedRange(port2) || !isPortInAllowedRange(clientPort))
                && !(isTheSamePort(port1, port2) || isTheSamePort(port1, clientPort) || isTheSamePort(port2, clientPort));
    }

    public static String makeConnectionString(String sid, String host, String port1, String port2, String clientPort) {
        return !isNull(host) || !isNull(port1) || !isNull(port2) || !isNull(clientPort)
                ? "server." + sid + "=" + host + ":" + port1 + ":" + port2 + ";" + clientPort
                : null;
    }

    private static boolean isPortInAllowedRange(int port) {
        return port > 1023 && port < 65536;
    }

    private static boolean isTheSamePort(int port1, int port2) {
        return port1 == port2;
    }
}
