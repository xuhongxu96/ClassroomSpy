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
            Connection.Response res = Jsoup.connect("http://zyfw.prsc.bnu.edu.cn/frame/droplist/getDropLists.action")
                    .timeout(timeout)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    .data("comboBoxName", "MsSchoolArea_PUBLIC_LF")
                    .data("paramValue", "ssxq=0&sybm_m=00")
                    .method(Connection.Method.POST)
                    .execute();

            JSONArray arr = new JSONArray(res.body());

            for(int i = 0; i < arr.length(); ++i) {
                JSONObject o = arr.getJSONObject(i);
                String code = o.getString("code");
                String name = o.getString("name");
                buildings.put(code, new Building(code, name, 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildings;
    }

    private static ArrayList<Seat> fetchSeats(String buildingId) {
        ArrayList<Seat> seats = new ArrayList<>();

        try {
            Connection.Response res = Jsoup.connect("http://zyfw.prsc.bnu.edu.cn/frame/droplist/getDropLists.action")
                    .timeout(timeout)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    .data("comboBoxName", "MsSchoolArea_PUBLIC_LF_JS")
                    .data("paramValue", "xq_m=0&ssjzw_m=" + buildingId + "&jslx_m=&sybm_m=00")
                    .method(Connection.Method.POST)
                    .execute();

            JSONArray arr = new JSONArray(res.body());

            for(int i = 0; i < arr.length(); ++i) {
                JSONObject o = arr.getJSONObject(i);
                String code = o.getString("code");
                String name = o.getString("name");
                name = name.substring(name.indexOf(']') + 1);
                name = name.substring(0, name.indexOf('['));
                seats.add(new Seat(buildingId, buildings.get(buildingId).name,
                        0, 0, 0,
                        code, name, ""));
            }
        } catch (Exception e) {
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
