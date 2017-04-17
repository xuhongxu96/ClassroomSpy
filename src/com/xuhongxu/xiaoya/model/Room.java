package com.xuhongxu.xiaoya.model;

/**
 * Created by Hongxu Xu on 4/17/2017.
 */
public class Room {
    public String building;
    public String rooms;

    public String[] getRoomList() {
        return rooms.split("; ");
    }
}
