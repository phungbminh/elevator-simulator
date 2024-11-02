package com.phung.elevator.utils;


public class ConstantInfo {
    public final static int MaxFloor = 10;
    public final static int ElevatorNum = 3;
    public final static int ElevatorMsPerGrid = 30;
    public final static int WindowWidth = 800;
    public final static int WindowHeight = 750;
    //floor Buttons
    public final static int floorButtonWidth = 60;
    public final static int floorButtonHeight = 25;
    public final static int floorButtonSpace = 10;
    public final static int floorFlagStart = 5;

    //Configs of InnerButton of Elevator
    public final static int innerButtonWidth = 25;
    public final static int innerButtonHeight = 25;
    public final static int innerSpace = 8;//Khoảng cách nút thang máy
    public final static int buttonsPerLine = 4;

    public final static int elevatorSpace = 70;
    public final static int elevatorStart = floorFlagStart + 3 * (floorButtonSpace+floorButtonWidth);
    public final static int innerTotalButtonx = 5 *(innerButtonWidth+innerSpace);
    public final static int innerTotalButtony = 2 * innerSpace + 5 * (innerButtonHeight+innerSpace);
}
