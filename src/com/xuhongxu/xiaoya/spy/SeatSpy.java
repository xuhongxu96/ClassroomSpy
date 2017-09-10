package com.xuhongxu.xiaoya.spy;

import com.xuhongxu.xiaoya.model.Building;
import com.xuhongxu.xiaoya.model.DateInfo;
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

        /*
        HashMap<String, Building> buildings = new HashMap<>();


        */

        HashMap<String, Building> buildings = new HashMap<>();
        HashMap<String, Building> buildingName = new HashMap<>();

        try {
            Connection.Response res = Jsoup.connect("http://zyfw.prsc.bnu.edu.cn/frame/droplist/getDropLists.action")
                    .timeout(timeout)
                    .method(Connection.Method.POST)
                    .data("comboBoxName", "MsSchoolArea_PUBLIC_LF")
                    .data("paramValue", "ssxq=0&sybm_m=00")
                    .ignoreContentType(true)
                    .execute();

            JSONArray jsonArray = new JSONArray(res.body());
            for (Object obj : jsonArray) {
                JSONObject buildingObject = (JSONObject) obj;
                Building building = new Building(
                        buildingObject.getString("code"),
                        buildingObject.getString("name"));
                buildings.put(building.id, building);
                buildingName.put(building.name, building);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

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

                String name = buildingObject.getString("buildName");
                String id = buildingObject.getString("buildId");
                int roomNum = Integer.valueOf(buildingObject.getString("roomNum"));

                if (buildingName.containsKey(name)) {
                    buildingName.get(name).peopleCountId = id;
                    buildingName.get(name).roomNum = roomNum;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buildings;
    }

    public static String extractName(String name)
    {
        String[] names = name.split("-");
        if (names.length == 2) {
            return names[1];
        }
        return name;
    }

    private static ArrayList<Seat> fetchSeats(String buildingId) {
        ArrayList<Seat> seats = new ArrayList<>();

        Building building = buildings.get(buildingId);

        HashMap<String, Seat> seatName = new HashMap<>();

        DateInfo info = new DateInfo();
        try {

            Connection.Response res = Jsoup.connect("http://zyfw.prsc.bnu.edu.cn/jw/common/showYearTerm.action")
                    .timeout(timeout)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();

            JSONObject obj = new JSONObject(res.body());
            info.xn = obj.getString("xn");
            info.xq = obj.getString("xqM");

            res = Jsoup.connect("http://zyfw.prsc.bnu.edu.cn/frame/droplist/getDropLists.action")
                    .timeout(timeout)
                    .method(Connection.Method.POST)
                    .data("comboBoxName", "MsSchoolArea_PUBLIC_LF_JS")
                    .data("paramValue", "xq_m=" + info.xq + "&ssjzw_m=" + buildingId + "&jslx_m=&sybm_m=00")
                    .ignoreContentType(true)
                    .execute();

            JSONArray jsonArray = new JSONArray(res.body());
            for (Object obj2 : jsonArray) {
                JSONObject buildingObject = (JSONObject) obj2;
                Seat seat = new Seat(
                        buildingId,
                        building.name,
                        buildingObject.getString("code"),
                        buildingObject.getString("name"));
                seats.add(seat);
                seatName.put(seat.roomName, seat);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Connection.Response res = Jsoup.connect("http://219.224.19.121:8086/magus/findseatapi/loadClassRoomList")
                    .timeout(timeout)
                    .data("params", "{ \"buildId\": \"" + building.peopleCountId + "\", \"searchType\": \"0\" }")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();

            JSONObject jsonObject = new JSONObject(res.body());
            jsonObject = jsonObject.getJSONObject("result");
            JSONArray jsonArray = jsonObject.getJSONArray("roomSeatCountList");
            for (Object obj : jsonArray) {
                JSONObject o = (JSONObject) obj;
                String name = extractBuildingPrefix(building.name) + extractName(o.getString("roomName"));
                if (seatName.containsKey(name)) {
                    seatName.get(name).remainingSeats = o.getInt("lastSeats");
                    seatName.get(name).totalSeats = o.getInt("totalSeats");
                    seatName.get(name).peopleNum = o.getInt("personQty");
                    seatName.get(name).txTime = o.getString("txTime");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return seats;
    }

    public static String extractBuildingPrefix(String name) {
        if (name.equals("电子楼")) {
            return "电";
        } else if (name.startsWith("教")) {
            return name.substring(1, 2);
        }
        return "";
    }

    public static void spy() {
        System.out.println(new Date().toString() + "  Started: Spy on Seats");
        lock.lock();
        try {
            buildings = fetchBuildings();
            for (Building building : buildings.values()) {
                seats.put(building.id, fetchSeats(building.id));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
