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
    private int curentFloor = 1;
    private EleState currentElevatorState = EleState.STALL;// -1 -> down, 0 -> stop, 1 -> up
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
            //System.out.println("floor: " + floor);
            if (currentElevatorState == EleState.STALL) {
                try {
                    //System.out.println("eLeState: " + eLeState);
                    sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Cant not sleep in run!");
                }
                continue;
            }
            moveOneFloor(currentElevatorState == EleState.UP);
        }
    }

    void sliding (boolean up){
        int distance = ConstantInfo.floorButtonHeight + ConstantInfo.floorButtonSpace;
        for (int i = 0; i < distance; i++) {
            try {
                sleep(ConstantInfo.ElevatorMsPerGrid);
            } catch (InterruptedException e) {}
            curPos.setLocation(curPos.getLocation().x, curPos.getLocation().y + (up ? -1 : 1));
        }
    }
    synchronized void moveOneFloor(boolean up) {
        sliding(up);
        // Cập nhật tầng hiện tại
        curentFloor += (up ? 1 : -1);

        if (dests.get(curentFloor)) { // Nếu đến tầng có yêu cầu
            //kiem tra con task nao o tren khong

            dests.set(curentFloor, Boolean.FALSE);

            turnoffLight(curentFloor);
            // Kiểm tra xem còn công việc nào cùng hướng yêu cầu không
            boolean needToContinue = checkForRemainingTasksSameDirection();

            // Nếu không có công việc nào, kiểm tra ngược lại
            if (!needToContinue) {
                int limit = (currentElevatorState == EleState.UP ? (ConstantInfo.MaxFloor + 1) : 0);
                needToContinue = search(limit, (currentElevatorState == EleState.UP ? 1 : -1)); // Kiểm tra ngược
                if (!needToContinue) {
                    currentElevatorState = EleState.STALL; // Không có công việc nào, dừng thôi
                } else {
                    currentElevatorState = (currentElevatorState == EleState.UP ? EleState.DOWN : EleState.UP);
                }
            }

            EleState oldState = currentElevatorState;
            currentElevatorState = EleState.PAUSE;
            openDoorThenClose(); // Mở cửa
            currentElevatorState = oldState; // Trở lại trạng thái cũ
        }
    }
    //    synchronized void moveOneFloor(boolean up) {
//        int distance = ConstantInfo.floorButtonHeight + ConstantInfo.floorButtonSpace;
//        for (int i = 0; i < distance; i++) {
//            try {
//                sleep(ConstantInfo.ElevatorMsPerGrid);
//            } catch (InterruptedException e) {}
//            //System.out.println("elevator position: x=" + curPos.getLocation().x + "; y=" + curPos.getLocation().y + (up ? -1 : 1));
//            curPos.setLocation(curPos.getLocation().x, curPos.getLocation().y + (up ? -1 : 1));
//        }
//        //Tầng++--
//        curentFloor += (up ? 1 : -1);
//        //Nếu ở tầng mục tiêu
//        for (int i = 1; i < dests.size(); i++) { // Bắt đầu từ 1 nếu tầng 0 không được sử dụng
//            System.out.print(i + ":" + (dests.get(i) ? "True" : "False") + " , ");
//        }
//        System.out.println("");
//
//        if (dests.get(curentFloor)) {
//            //if(!checkForRemainingTasksSameDirection()){
//            dests.set(curentFloor, Boolean.FALSE);
//            //Tắt đèn
//            turnoffLight(curentFloor);
//            //Bạn có cần tiếp tục đi lên/xuống không?
//            int limit = (eLeState == EleState.UP ? (ConstantInfo.MaxFloor + 1) : 0);
//            int step = eLeState == EleState.UP ? 1 : -1;
//            boolean needToContinue = search(limit, step);
//
//            //Không cần tiếp tục, kiểm tra ngược xem có task hay không
//            if (!needToContinue) {
//                needToContinue = search(ConstantInfo.MaxFloor + 1 - limit, -step);//Đảo ngược -=-
//                if (!needToContinue) {
//                    eLeState = EleState.STALL;
//                } else {
//                    assert eLeState != EleState.STALL;
//                    eLeState = (eLeState == EleState.UP ? EleState.DOWN : EleState.UP);
//                }
//            }
//            EleState oldState = eLeState;
//            eLeState = EleState.PAUSE;
//            openDoorThenClose();
//            eLeState = oldState;
//        }
//
//    }
   // }
    // Kiểm tra xem còn công việc nào yêu cầu cùng một hướng không
    private boolean checkForRemainingTasksSameDirection() {
        int limit = (currentElevatorState == EleState.UP ? ConstantInfo.MaxFloor + 1 : 0);
        int step = (currentElevatorState == EleState.UP ? 1 : -1);
        for (int i = curentFloor + step; (currentElevatorState == currentElevatorState.UP ? i < limit : i > limit); i += step) {
            if (dests.get(i)) {
                return true; // Còn công việc yêu cầu ở trên đường đi
            }
        }
        return false;
    }

    //Kiểm tra xem có công việc theo một hướng nhất định không--
    private boolean search(int limit, int step) {
        boolean needToContinue = false;
        for (int i = curentFloor; i != limit; i += step) {
            if (dests.get(i)) {
                needToContinue = true;
                break;
            }
        }
        return needToContinue;
    }
    // Phương thức tìm kiếm để xác định tầng ưu tiên cao nhất
//    private boolean search(int limit, int step) {
//        boolean needToContinue = false;
//        int priorityFloor = -1; // Biến để theo dõi tầng ưu tiên cao nhất
//        for (int i = curentFloor; i != limit; i += step) {
//            if (dests.get(i)) {
//                priorityFloor = i; // Cập nhật tầng ưu tiên
//            }
//        }
//        if (priorityFloor != -1) {
//            // Di chuyển trực tiếp đến tầng ưu tiên cao nhất
//            curentFloor = priorityFloor;
//            needToContinue = true;
//        }
//        return needToContinue;
//    }

    //mở và đóng cửa
    private void openDoorThenClose() {
        if (currentElevatorState != EleState.STALL && currentElevatorState != EleState.PAUSE) return;
        openDoor();
        try {
            sleep(300);
        } catch (InterruptedException e) {
        }
        closeDoor();
        try {
            sleep(300);
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
        Thread thread = new Thread(()-> {
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
        //Nếu thang máy đang trong tình trạng dừng
        if (EleState.STALL == currentElevatorState) {
            if (curentFloor == requiredFloor) {
                turnoffLight(curentFloor);
                return;
            }
            currentElevatorState = (curentFloor < requiredFloor ? EleState.UP : EleState.DOWN);
        }
        System.out.println("commitTask: eLeState -> " + currentElevatorState + "; requiredFloor: " + requiredFloor);
        dests.set(requiredFloor, Boolean.TRUE);


    }
    public void commitOuterTask(int requiredFloor, EleState dir) {
        int index = (dir == EleState.UP ? 1 : 0);
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