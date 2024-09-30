package com.phung.elevator.view.elevator;

import com.phung.elevator.utils.ConstantInfo;
import com.phung.elevator.controller.ElevatorsController;
import com.phung.elevator.utils.EleState;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

@Getter
@Setter
public class Elevator extends Thread {

    private boolean[][] taskState = new boolean[ConstantInfo.MaxFloor + 1][2];
    private ElevatorsController controller;
    private ElevatorView elevatorView;
    JLabel curPos;
    private int floor = 1;
    private EleState eLeState = EleState.STALL;// -1 -> down, 0 -> stop, 1 -> up
    private ArrayList<Boolean> dests = new ArrayList<Boolean>(Collections.nCopies(ConstantInfo.MaxFloor + 1, Boolean.FALSE));

    public void setBounds(int x, int y, int width, int height) {
        elevatorView.setBounds(x, y, width, height);
    }

    public Elevator(int buttonStarty, JFrame MainView) {
        elevatorView = new ElevatorView(buttonStarty);
        curPos = elevatorView.curPos;
        MainView.add(elevatorView);

        ArrayList<InnerNumButton> innerButtons = elevatorView.innerButtons;

        innerButtons.get(ConstantInfo.MaxFloor).addActionListener(event -> closeDoor());
        innerButtons.get(ConstantInfo.MaxFloor + 1).addActionListener(event -> new Thread(this::openDoorThenClose).start());
        innerButtons.get(ConstantInfo.MaxFloor + 2).addActionListener(event -> alert());
        for (int i = 0; i < ConstantInfo.MaxFloor; i++) {
            InnerNumButton buttonRef = innerButtons.get(i);
            buttonRef.addActionListener(this::InnerBtnListener);
        }
        for (int i = 0; i < ConstantInfo.MaxFloor; i++) {
            taskState[i][0] = taskState[i][1] = false;
        }
    }

    @Override
    public void run() {
        while (true) {
            if (eLeState == EleState.STALL) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Cant not sleep in run!");
                }
                continue;
            }
            moveOneFloor(eLeState == EleState.UP);
        }
    }

    synchronized void moveOneFloor(boolean up) {
        int distance = ConstantInfo.floorButtonHeight + ConstantInfo.floorButtonSpace;

        for (int i = 0; i < distance; i++) {
            try {
                sleep(ConstantInfo.ElevatorMsPerGrid);
            } catch (InterruptedException e) {
            }
            curPos.setLocation(curPos.getLocation().x, curPos.getLocation().y + (up ? -1 : 1));
        }
        //Tầng++--
        floor += (up ? 1 : -1);
        //Nếu ở tầng mục tiêu
        if (dests.get(floor)) {
            dests.set(floor, Boolean.FALSE);
            //Tắt đèn
            turnoffLight(floor);
            //Bạn có cần tiếp tục đi lên/xuống không?
            int limit = eLeState == EleState.UP ? (ConstantInfo.MaxFloor + 1) : 0;
            int step = eLeState == EleState.UP ? 1 : -1;
            boolean needToContinue = search(limit, step);

            //Không cần tiếp tục, kiểm tra ngược xem có task hay không
            if (!needToContinue) {
                needToContinue = search(ConstantInfo.MaxFloor + 1 - limit, -step);//Đảo ngược -=-
                if (!needToContinue) {
                    eLeState = EleState.STALL;
                } else {
                    assert eLeState != EleState.STALL;
                    eLeState = (eLeState == EleState.UP ? EleState.DOWN : EleState.UP);
                }
            }
            EleState oldState = eLeState;
            eLeState = EleState.PAUSE;
            openDoorThenClose();
            eLeState = oldState;
        }
    }

    //Kiểm tra xem có công việc theo một hướng nhất định không--
    private boolean search(int limit, int step) {
        boolean needToContinue = false;
        for (int i = floor; i != limit; i += step) {
            if (dests.get(i)) {
                needToContinue = true;
                break;
            }
        }
        return needToContinue;
    }

    //mở và đóng cửa
    private void openDoorThenClose() {
        if (eLeState != EleState.STALL && eLeState != EleState.PAUSE) return;
        openDoor();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
        }
        closeDoor();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void openDoor() {
        curPos.setText("|   |");
    }

    private void closeDoor() {
        curPos.setText(" =||= ");
    }

    private void alert() {
        Thread thread = new Thread(() -> {
            while (true) {
                curPos.setBackground(Color.RED);
                try {
                    sleep(500);
                } catch (InterruptedException e) { }
                curPos.setBackground(Color.GRAY);
                try {
                    sleep(500);
                } catch (InterruptedException e) {}
            }
        });
        thread.start();
    }

    //Commit Task
    //synchronized
    private void InnerBtnListener(ActionEvent event) {
        InnerNumButton button = (InnerNumButton) event.getSource();
        button.turnon();
        commitTask(button.floor);
    }

    void commitTask(int requiredFloor) {
        //Nếu thang máy đang trong tình cho thi acctive
        if (EleState.STALL == eLeState) {
            if (floor == requiredFloor) {
                turnoffLight(floor);
                return;
            }
            eLeState = (floor < requiredFloor ? EleState.UP : EleState.DOWN);
        }
        dests.set(requiredFloor, Boolean.TRUE);
    }


    public void commitOuterTask(int requiredFloor, EleState dir) {
        int index = dir == EleState.UP ? 1 : 0;
        taskState[requiredFloor][index] = true;
        commitTask(requiredFloor);
    }

    void turnoffLight(int floor) {
        InnerNumButton button = elevatorView.innerButtons.get(floor - 1);
        button.turnoff();
        for (int i = 0; i < 2; i++) {
            if (taskState[floor][i]) {
                taskState[floor][i] = false;
                controller.turnoffLight(floor, i);
            }
        }
    }


}