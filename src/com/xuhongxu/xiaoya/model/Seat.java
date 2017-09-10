package com.xuhongxu.xiaoya.model;

/**
 * Created by Hongxu Xu on 4/17/2017.
 */
public class Seat {
    public String buildId, buildName;
    public int remainingSeats, totalSeats, peopleNum;
    public String roomId, roomName;
    public String txTime;

    public Seat(String buildId, String buildName, String roomId, String roomName) {
        this.buildId = buildId;
        this.buildName = buildName;
        this.roomId = roomId;

        if (roomName.contains("]")) {
            roomName = roomName.substring(roomName.indexOf("]") + 1);
            if (roomName.contains("[")) {
                roomName = roomName.substring(0, roomName.indexOf("["));
            }
        }

        this.roomName = roomName;
    }

}
