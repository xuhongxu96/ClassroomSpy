package com.xuhongxu.xiaoya;

import com.sun.net.httpserver.HttpServer;
import com.xuhongxu.xiaoya.model.Building;
import com.xuhongxu.xiaoya.spy.BuildingSpy;
import com.xuhongxu.xiaoya.spy.RoomSpy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    static private ScheduledExecutorService roomSpyService = Executors.newSingleThreadScheduledExecutor();
    static private ScheduledExecutorService buildingSpyService = Executors.newSingleThreadScheduledExecutor();

    static private HttpServer server;

    public static void main(String[] args) {

        try {

            int port = 9610;

            if (args.length > 0) {
                port = Integer.valueOf(args[0]);
            }

            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/buildings", httpExchange -> {

                System.out.println(new Date().toString() + "  GET /buildings  IP: " + httpExchange.getRemoteAddress().getHostString());
                String payload = "error";
                try {
                    payload = BuildingSpy.getBuildingHtml();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    httpExchange.getResponseHeaders().set("Content-Type", "text/text; charset=utf-8");
                    httpExchange.sendResponseHeaders(200, payload.getBytes().length);
                    final OutputStream output = httpExchange.getResponseBody();
                    output.write(payload.getBytes());
                    output.flush();
                    httpExchange.close();
                }

            });

            server.createContext("/building", httpExchange -> {
                String id = httpExchange.getRequestURI().getPath();

                System.out.println(new Date().toString() + "  GET " + id
                        + "  IP: " + httpExchange.getRemoteAddress().getHostString());

                String payload = "error";
                try {
                    id = id.substring(10);
                    Building building = BuildingSpy.getBuildings().getOrDefault(id, null);
                    if (building != null) {
                        payload = RoomSpy.getRoomHtml(building.name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    httpExchange.getResponseHeaders().set("Content-Type", "text/text; charset=utf-8");
                    httpExchange.sendResponseHeaders(200, payload.getBytes().length);
                    final OutputStream output = httpExchange.getResponseBody();
                    output.write(payload.getBytes());
                    output.flush();
                    httpExchange.close();
                }
            });

            server.start();

            roomSpyService.scheduleAtFixedRate(RoomSpy::spy, 0, 6, TimeUnit.HOURS);
            buildingSpyService.scheduleAtFixedRate(BuildingSpy::spy, 0, 6, TimeUnit.HOURS);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String simplifyName(String name) {

        if (name.startsWith("邱季端")) {
            name = name.substring(3);
            if (name.startsWith("-")) {
                name = name.substring(1);
            }
            return name;
        }

        if (name.length() > 3) {
            String last3 = name.substring(name.length() - 3);
            if (last3.matches("\\d\\d\\d")) {
                return last3;
            }
        }

        return name;
    }
}
