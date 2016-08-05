package com.zk.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class ClusterInfoUtils {

    public static String getInfo(String hostPort) {
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        String result;

        try {
            URI uri = PathUtils.createUri(hostPort);
            socket = new Socket(uri.getHost(), uri.getPort());
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("stat");
            StringBuilder builder = new StringBuilder();
            String s = in.readLine();
            while (s != null) {
                builder.append(s).append("\n");
                s = in.readLine();
            }

            out.close();
            in.close();
            socket.close();

            result = builder.toString();

        } catch (URISyntaxException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        return result;
    }
}
