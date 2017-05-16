package com.xuhongxu.xiaoya;

import com.sun.net.httpserver.HttpServer;
import com.xuhongxu.xiaoya.model.Building;
import com.xuhongxu.xiaoya.model.Seat;
import com.xuhongxu.xiaoya.spy.RoomSpy;
import com.xuhongxu.xiaoya.spy.SeatSpy;

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
    static private ScheduledExecutorService seatSpyService = Executors.newSingleThreadScheduledExecutor();

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
                    ArrayList<Building> buildings = SeatSpy.getBuildings();
                    if (buildings != null) {
                        payload = "";
                        for (Building building : buildings) {
                            payload += building.id + "," + building.name + ",";
                        }
                        payload = payload.substring(0, payload.length() - 1);
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

            server.createContext("/building", httpExchange -> {
                String id = httpExchange.getRequestURI().getPath();

                System.out.println(new Date().toString() + "  GET " + id
                        + "  IP: " + httpExchange.getRemoteAddress().getHostString());

                String payload = "error";
                try {
                    id = id.substring(10);

                    ArrayList<Seat> seats = SeatSpy.getSeats(id);
                    if (seats != null) {
                        Building building = SeatSpy.getBuilding(id);
                        if (building != null) {
                            ArrayList<HashSet<String>> rooms = RoomSpy.getRoom(building.name);
                            if (rooms != null) {
                                payload = "";

                                for (Seat seat : seats) {
                                    String name = seat.getRoomName();
                                    payload += name + "," +
                                            seat.txTime + "," +
                                            seat.peopleNum + "," +
                                            seat.remainingSeats + "," +
                                            seat.totalSeats;
                                    for (int i = 0; i < 12; ++i) {
                                        if (rooms.get(i).contains(name)) {
                                            payload += "," + "1";
                                        } else {
                                            payload += "," + "0";
                                        }
                                    }
                                    payload += ";\n";
                                }
                            }
                        }
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
            seatSpyService.scheduleAtFixedRate(SeatSpy::spy, 0, 8, TimeUnit.HOURS);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
