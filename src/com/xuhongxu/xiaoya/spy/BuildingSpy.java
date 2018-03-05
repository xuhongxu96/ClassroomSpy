package com.xuhongxu.xiaoya.spy;

import com.xuhongxu.xiaoya.model.Building;
import com.xuhongxu.xiaoya.model.Room;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BuildingSpy {

    private static ReentrantLock lock = new ReentrantLock();
    private final static int timeout = 10000;
    private static HashMap<String, Building> buildings = new HashMap<>();
    private static String buildingHtml = "";

    private static HashMap<String, Building> fetchBuildings() {

        HashMap<String, Building> buildings = new HashMap<>();

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
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buildings;
    }

    public static void spy() {
        System.out.println(new Date().toString() + "  Started: Spy on Building");
        HashMap<String, Building> tempBuildings = fetchBuildings();
        lock.lock();
        try {
            buildings = tempBuildings;
            ArrayList<String> stringList = new ArrayList<>();
            for (Building building : buildings.values()){
                stringList.add(building.id);
                stringList.add(building.name);
            }
            buildingHtml = String.join(",", stringList);
        } finally {
            lock.unlock();
            System.out.println(new Date().toString() + "  Finished: Spy on Building");
        }
    }

    public static HashMap<String, Building> getBuildings() {
        lock.lock();
        try {
            return buildings;
        } finally {
            lock.unlock();
        }
    }

    public static String getBuildingHtml() {
        lock.lock();
        try {
            return buildingHtml;
        } finally {
            lock.unlock();
        }
    }


}
