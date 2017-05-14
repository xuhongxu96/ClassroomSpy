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

    private static ArrayList<HashMap<String, Room>> rooms = new ArrayList<>(12);

    static ReentrantLock lock = new ReentrantLock();

    private static HashMap<String, Room> fetchRoom(DateInfo info, int start, int end) {
        HashMap<String, Room> rooms = new HashMap<>();
        String p = "";
        for (int i = start; i <= end; ++i) {
            if (i < 10) {
                p += "0" + i;
            } else {
                p += i;
            }
            if (i != end) {
                p += ",";
            }
        }
        try {
            Document doc = Jsoup.connect("http://zyfw.prsc.bnu.edu.cn/public/dykb.kxjsi_data.gs1.jsp")
                    .timeout(timeout)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    .data("hidweeks", info.day)
                    .data("hidjcs", p)
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
                        Room room = new Room();
                        room.building = tr.child(2).text();
                        if (room.building.equals("教十楼(物理楼)")) {
                            room.building = "教十楼";
                        }
                        room.rooms = tr.child(3).text();
                        rooms.put(room.building, room);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
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
        ArrayList<HashMap<String, Room>> tempRooms = new ArrayList<>(12);
        tempRooms.clear();
        for (int i = 1; i <= 12; ++i) {
            tempRooms.add(fetchRoom(fetchDate(), i, i));
        }
        lock.lock();
        try {
            rooms = tempRooms;
        } finally {
            lock.unlock();
            System.out.println(new Date().toString() + "  Finished: Spy on Room");
        }
    }

    private static String extractRoomName(String room) {
        return room.substring(1, room.indexOf("("));
    }

    public static ArrayList<HashSet<String>> getRoom(String buildingName) {
        lock.lock();
        try {
            if (rooms.size() == 12 && rooms.get(0) != null && rooms.get(0).containsKey(buildingName)) {
                ArrayList<HashSet<String>> res = new ArrayList<>();
                for (int i = 0; i < 12; ++i) {
                    HashSet<String> roomSet = new HashSet<>();
                    for (String roomName : rooms.get(i).get(buildingName).getRoomList()) {
                        roomSet.add(extractRoomName(roomName));
                    }
                    res.add(roomSet);
                }
                return res;
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }
}
