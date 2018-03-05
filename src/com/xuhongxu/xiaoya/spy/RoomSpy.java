package com.xuhongxu.xiaoya.spy;

import com.xuhongxu.xiaoya.model.DateInfo;
import com.xuhongxu.xiaoya.model.Room;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Hongxu Xu on 4/17/2017.
 */

public class RoomSpy {
    private final static int timeout = 10000;

    private static HashMap<String, boolean[]> roomsIsEmpty = new HashMap<>();
    private static HashMap<String, HashSet<String>> roomsInBuilding = new HashMap<>();
    private static HashMap<String, String> roomHtml = new HashMap<>();

    private static ReentrantLock lock = new ReentrantLock();

    private static void fetchRoom(DateInfo info, int start, int end,
                                  HashMap<String, boolean[]> roomsIsEmpty,
                                  HashMap<String, HashSet<String>> roomsInBuilding) {

        StringBuilder p = new StringBuilder();
        for (int i = start; i <= end; ++i) {
            if (i < 10) {
                p.append("0").append(i);
            } else {
                p.append(i);
            }
            if (i != end) {
                p.append(",");
            }
        }
        try {
            Document doc = Jsoup.connect("http://zyfw.prsc.bnu.edu.cn/public/dykb.kxjsi_data.gs1.jsp")
                    .timeout(timeout)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    .data("hidweeks", info.day)
                    .data("hidjcs", p.toString())
                    .data("hidMIN", "0")
                    .data("hidMAX", "800")
                    .data("sybm_m", "00")
                    .data("xn", info.xn)
                    .data("xn1", String.valueOf(Integer.valueOf(info.xn) + 1))
                    .data("xq_m", info.xq)
                    .data("sel_zc", info.week)
                    .data("selXQ", "0")
                    .data("selGS", "1")
                    .ignoreContentType(true)
                    .post();

            Element table = doc.getElementsByTag("tbody").first();
            if (table != null) {
                for (Element tr : table.getElementsByTag("tr")) {
                    try {
                        String building = tr.child(2).text();
                        if (building.equals("教十楼(物理楼)")) {
                            building = "教十楼";
                        }
                        List<String> roomList = Arrays.asList(tr.child(3).text().split("; "));

                        for (String roomRawName : roomList) {
                            String roomName = building + roomRawName;
                            if (!roomsIsEmpty.containsKey(roomName)) {
                                roomsIsEmpty.put(roomName, new boolean[12]);
                            }
                            for (int i = start; i <= end; ++i) {
                                roomsIsEmpty.get(roomName)[i - 1] = true;
                            }
                        }

                        if (roomsInBuilding.containsKey(building)) {
                            roomsInBuilding.get(building).addAll(roomList);
                        } else {
                            roomsInBuilding.put(building, new HashSet<>(roomList));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static DateInfo fetchDate() {
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

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            res = Jsoup.connect("http://zyfw.prsc.bnu.edu.cn/public/getTeachingWeekByDate.action")
                    .timeout(timeout)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    .data("xn", info.xn)
                    .data("xq_m", info.xq)
                    .data("hidOption", "getWeek")
                    .data("hdrq", df.format(new Date()))
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();
            String[] date = res.body().split("@");
            info.week = date[0];
            info.day = date[1];
            if ("0".equals(info.day)) info.day = "7";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public static void spy() {
        System.out.println(new Date().toString() + "  Started: Spy on Room");
        HashMap<String, boolean[]> tempRoomsIsEmpty = new HashMap<>();
        HashMap<String, HashSet<String>> tempRoomsInBuilding = new HashMap<>();

        tempRoomsIsEmpty.clear();
        tempRoomsInBuilding.clear();

        for (int i = 1; i <= 12; ++i) {
            fetchRoom(fetchDate(), i, i, tempRoomsIsEmpty, tempRoomsInBuilding);
        }

        lock.lock();
        try {
            roomsIsEmpty = tempRoomsIsEmpty;
            roomsInBuilding = tempRoomsInBuilding;

            for (String buildingName : roomsInBuilding.keySet()) {
                StringBuilder html = new StringBuilder();
                List<String> roomList = new ArrayList<>(roomsInBuilding.get(buildingName));
                Collections.sort(roomList);
                for (String roomRawNameInBuilding : roomList) {

                    String roomInBuilding = roomRawNameInBuilding;
                    int roomCapacity = 0;

                    int tempPos = roomRawNameInBuilding.indexOf("(");
                    if (tempPos != -1) {
                        roomInBuilding = roomRawNameInBuilding.substring(0, tempPos);

                        if (roomInBuilding.length() == 4
                                && (roomInBuilding.startsWith("二")
                                || roomInBuilding.startsWith("八")
                                || roomInBuilding.startsWith("七")
                                || roomInBuilding.startsWith("九")
                                || roomInBuilding.startsWith("十")
                                || roomInBuilding.startsWith("四")
                                || roomInBuilding.startsWith("电")
                                || roomInBuilding.startsWith("艺")
                        )) {
                            roomInBuilding = roomInBuilding.substring(1);
                        } else if (roomInBuilding.startsWith("邱季端")) {
                            roomInBuilding = roomInBuilding.substring(3);
                        } else if (roomInBuilding.startsWith("科技楼C区")) {
                            roomInBuilding = roomInBuilding.substring(5);
                        }

                        roomCapacity = Integer.valueOf(roomRawNameInBuilding.substring(tempPos + 1,
                                roomRawNameInBuilding.length() - 1));
                    }

                    boolean[] emptyStatus = roomsIsEmpty.getOrDefault(buildingName + roomRawNameInBuilding, new boolean[12]);
                    html.append(roomInBuilding).append(",1996-10-31 10:00:00,0,0,").append(roomCapacity);
                    for (int i = 0; i < 12; ++i) {
                        html.append(",").append(emptyStatus[i] ? "1" : "0");
                    }
                    html.append(";\n");
                }
                roomHtml.put(buildingName, html.toString());
            }

        } finally {
            lock.unlock();
            System.out.println(new Date().toString() + "  Finished: Spy on Room");
        }
    }

    public static String getRoomHtml(String buildingName) {
        lock.lock();
        try {
            return roomHtml.getOrDefault(buildingName, "");
        } finally {
            lock.unlock();
        }
    }
}
