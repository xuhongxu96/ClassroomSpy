package com.xuhongxu.xiaoya.spy;

import com.xuhongxu.xiaoya.model.Building;
import com.xuhongxu.xiaoya.model.Seat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Hongxu Xu on 4/17/2017.
 */
public class SeatSpy {
    private static final int timeout = 10000;

    private static HashMap<String, Building> buildings;
    private static HashMap<String, ArrayList<Seat>> seats = new HashMap<>();

    static ReentrantLock lock = new ReentrantLock();

    private static HashMap<String, Building> fetchBuildings() {

        HashMap<String, Building> buildings = new HashMap<>();

        try {
            Connection.Response res = Jsoup.connect("http://219.224.19.121:8086/magus/findseatapi/loadBuildingsList?")
                    .timeout(timeout)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();

            JSONObject jsonObject = new JSONObject(res.body());
            jsonObject = jsonObject.getJSONObject("result");
            JSONArray jsonArray = jsonObject.getJSONArray("buildingList");
            for (Object obj : jsonArray) {
                JSONObject buildingObject = (JSONObject) obj;
                buildings.put(buildingObject.getString("buildId"), new Building(
                        buildingObject.getString("buildId"),
                        buildingObject.getString("buildName"),
                        Integer.valueOf(buildingObject.getString("roomNum"))
                ));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return buildings;
    }

    private static ArrayList<Seat> fetchSeats(String buildingId) {
        ArrayList<Seat> seats = new ArrayList<>();

        try {
            Connection.Response res = Jsoup.connect("http://219.224.19.121:8086/magus/findseatapi/loadClassRoomList")
                    .timeout(timeout)
                    .data("params", "{ \"buildId\": \"" + buildingId + "\", \"searchType\": \"0\" }")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();

            JSONObject jsonObject = new JSONObject(res.body());
            jsonObject = jsonObject.getJSONObject("result");
            JSONArray jsonArray = jsonObject.getJSONArray("roomSeatCountList");
            for (Object obj : jsonArray) {
                JSONObject o = (JSONObject) obj;
                seats.add(new Seat(
                        o.getString("buildId"),
                        o.getString("buildName"),
                        o.getInt("lastSeats"),
                        o.getInt("totalSeats"),
                        o.getInt("personQty"),
                        o.getString("roomId"),
                        o.getString("roomName"),
                        o.getString("txTime")
                        ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return seats;
    }

    public static void spy() {
        System.out.println(new Date().toString() + "  Started: Spy on Seats");
        lock.lock();
        try {
            buildings = fetchBuildings();
            for (Building building : buildings.values()) {
                seats.put(building.id, fetchSeats(building.id));
            }
        } finally {
            lock.unlock();
            System.out.println(new Date().toString() + "  Finished: Spy on Seats");
        }
    }

    public static Building getBuilding(String id) {
        lock.lock();
        try {
            if (buildings != null && buildings.containsKey(id))
                return buildings.get(id);
            return null;
        } finally {
            lock.unlock();
        }
    }

    public static ArrayList<Building> getBuildings() {
        lock.lock();
        try {
            if (buildings != null)
                return new ArrayList<>(buildings.values());
            return null;
        } finally {
            lock.unlock();
        }
    }

    public static ArrayList<Seat> getSeats(String buildingId) {
        lock.lock();
        try {
            if (seats != null && seats.containsKey(buildingId))
                return seats.get(buildingId);
            return null;
        } finally {
            lock.unlock();
        }
    }
}
