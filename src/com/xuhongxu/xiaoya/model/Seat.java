package com.xuhongxu.xiaoya.model;

/**
 * Created by Hongxu Xu on 4/17/2017.
 */
public class Seat {
    public String buildId, buildName;
    public int remainingSeats, totalSeats, peopleNum;
    public String roomId, roomName;
    public String txTime;

    public Seat(String buildId, String buildName, int remainingSeats, int totalSeats, int peopleNum,
                String roomId, String roomName, String txTime) {
        this.buildId = buildId;
        this.buildName = buildName;
        this.remainingSeats = remainingSeats;
        this.totalSeats = totalSeats;
        this.peopleNum = peopleNum;
        this.roomId = roomId;
        this.roomName = roomName;
        this.txTime = txTime;
    }

    public String getRoomName() {
        String[] names = roomName.split("-");
        if (names.length == 2) {
            return names[1];
        }
        return roomName;
    }
}
