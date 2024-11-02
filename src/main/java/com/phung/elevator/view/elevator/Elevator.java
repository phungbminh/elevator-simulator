package com.phung.elevator.view.elevator;

import com.phung.elevator.dto.TaskSubmit;
import com.phung.elevator.utils.ConstantInfo;
import com.phung.elevator.controller.ElevatorsController;
import com.phung.elevator.utils.EleState;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

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
    private Queue<TaskSubmit> requestQueue; // Queue lưu trữ các yêu cầu gọi thang máy
    private List<Integer> upList; // Danh sách các tầng đang chờ đi lên
    private List<Integer> downList; // Danh sách các tầng đang chờ đi xuống
    private boolean running; // Biến để kiểm soát vòng lặp

    private String openDoorText = "||____||";
    private String closeDoorText= "|==||==|";



    public void setBounds(int x, int y, int width, int height) {
        elevatorView.setBounds(x, y, width, height);
    }

    public Elevator(int buttonStarty, JFrame MainView) {
        elevatorView = new ElevatorView(buttonStarty);
        curPos = elevatorView.curPos;
        MainView.add(elevatorView);

        ArrayList<InnerNumButton> innerButtons = elevatorView.innerButtons;

        innerButtons.get(ConstantInfo.MaxFloor).setForeground(Color.RED);
        innerButtons.get(ConstantInfo.MaxFloor).setBackground(Color.BLACK);
        innerButtons.get(ConstantInfo.MaxFloor).setFont(new Font("Arial", Font.BOLD, 20));
        innerButtons.get(ConstantInfo.MaxFloor + 1).addActionListener(event -> closeDoor());
        innerButtons.get(ConstantInfo.MaxFloor + 2).addActionListener(event -> new Thread(this::openDoorThenClose).start());
        innerButtons.get(ConstantInfo.MaxFloor + 3).addActionListener(event -> alert());

        for (int i = 0; i < ConstantInfo.MaxFloor; i++) {
            InnerNumButton buttonRef = innerButtons.get(i);
            buttonRef.addActionListener(this::InnerBtnListener);
        }
        for (int i = 0; i < ConstantInfo.MaxFloor; i++) {
            taskState[i][0] = taskState[i][1] = false;
        }
        this.requestQueue = new LinkedList<>();
        this.upList = new ArrayList<>();
        this.downList = new ArrayList<>();
        this.running = true; // Bắt đầu vòng lặp
    }

    @Override
    public void run() {
        while (running) {
            processRequest();
        }
    }
    private void processRequest() {
        TaskSubmit requestedFloor = null;

        synchronized (requestQueue) {
            while (requestQueue.isEmpty()) {
                try {
                    requestQueue.wait(); // Chờ khi không có yêu cầu
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            requestedFloor = requestQueue.poll(); // Lấy yêu cầu đầu tiên từ Queue
        }


        if (requestedFloor != null) {
            Integer floor = requestedFloor.getFloor();
            EleState stage = requestedFloor.getStage();
            decideDirection(floor, stage); // Quyết định hướng di chuyển
            moveOneFloor(floor, stage); // Di chuyển đến tầng được yêu cầu
        }
    }
    public static <K, T> Optional<K> findKey(Map<K, T> mapOrNull, T value) {
        return Optional.ofNullable(mapOrNull).flatMap(map -> map.entrySet()
                .stream()
                .filter(e -> Objects.equals(e.getValue(), value))
                .map(Map.Entry::getKey)
                .findAny());
    }

    private void decideDirection(int requestedFloor, EleState requestStage) {
        if (requestedFloor > curentFloor) {
            upList.add(requestedFloor);
            currentElevatorState = EleState.UP;
        } else if (requestedFloor < curentFloor) {
            downList.add(requestedFloor);
            currentElevatorState = EleState.DOWN;
        }



    }
    void sliding (boolean up){
        curPos.setText( (up ? "↑_" : "↓_") + closeDoorText);
        elevatorView.innerButtons.get(ConstantInfo.MaxFloor).setText((up ? "↑ " : "↓ ") + curentFloor);
        int distance = ConstantInfo.floorButtonHeight + ConstantInfo.floorButtonSpace;
        for (int i = 0; i < distance; i++) {
            try {
                sleep(ConstantInfo.ElevatorMsPerGrid);
            } catch (InterruptedException e) {}
            curPos.setLocation(curPos.getLocation().x, curPos.getLocation().y + (up ? -1 : 1));
        }
    }

    private void moveOneFloor(int requestedFloor, EleState stage) {
        while (curentFloor != requestedFloor) {
            boolean up = currentElevatorState == EleState.UP;
            // Di chuyển thang máy
            if (up) {
                curentFloor++;
            } else {
                curentFloor--;
            }
            System.out.println("Moving " + curentFloor + " to floor " + curentFloor);

            sliding(up);

            // Kiểm tra yêu cầu mới trong quá trình di chuyển
            if (scanQueue(requestedFloor)) {
                return; // Nếu có yêu cầu mới, dừng lại
            }
        }
        System.out.println("Arrived at floor " + curentFloor);
        turnoffLight(curentFloor);
        EleState oldState = currentElevatorState;
        currentElevatorState = EleState.PAUSE;
        openDoorThenClose(); // Mở cửa
        elevatorView.innerButtons.get(ConstantInfo.MaxFloor).setText("_ " + curentFloor);
        currentElevatorState = oldState;
        // Xóa khỏi danh sách sau khi hoàn thành
        if (currentElevatorState == EleState.UP) {
            upList.remove(Integer.valueOf(requestedFloor));
        } else {
            downList.remove(Integer.valueOf(requestedFloor));
        }
        // Kiểm tra yêu cầu tiếp theo
        checkNextRequest();
    }
    private boolean scanQueue(int targetFloor) {
        synchronized (requestQueue) {
            if (!requestQueue.isEmpty()) {
                TaskSubmit newRequest = requestQueue.peek(); // Lấy yêu cầu mới mà không xóa
                if (newRequest != null) {
                    // Kiểm tra nếu yêu cầu mới cùng hướng và nhỏ hơn tầng mục tiêu
                    if ((currentElevatorState == EleState.UP && newRequest.getFloor() > curentFloor && newRequest.getFloor() < targetFloor) ||
                            (currentElevatorState == EleState.DOWN && newRequest.getFloor() < curentFloor  && newRequest.getFloor() > targetFloor)) {
                        if(newRequest.getStage() == currentElevatorState){
                            System.out.println("New request for floor " + newRequest + " received. Stopping at this floor.");
                            requestQueue.poll(); // Xóa yêu cầu mới khỏi hàng đợi
                            moveOneFloor(newRequest.getFloor(), newRequest.getStage()); // Dừng lại ở tầng mới
                            return true; // Đã dừng lại
                        }

                    }
                }
            }
        }
        return false; // Không có yêu cầu mới nào phù hợp
    }
    private void checkNextRequest() {
        if (!upList.isEmpty()) {
            // Xử lý yêu cầu tiếp theo từ danh sách upList
            int nextFloor = upList.get(0);
            moveOneFloor(nextFloor, null);
        } else if (!downList.isEmpty()) {
            // Xử lý yêu cầu tiếp theo từ danh sách downList
            int nextFloor = downList.get(0);
            moveOneFloor(nextFloor, null);
        } else {
            // Không còn yêu cầu, thang máy trở về trạng thái rảnh
            currentElevatorState = EleState.STALL;
        }
    }

    //mở và đóng cửa
    private void openDoorThenClose() {
        if (currentElevatorState != EleState.STALL && currentElevatorState != EleState.PAUSE) return;
        openDoor();
        try {
            sleep(200);
        } catch (InterruptedException e) {
        }
        closeDoor();
        try {
            sleep(200);
        } catch (InterruptedException e) {
        }
    }

    private void openDoor() {
        curPos.setText(openDoorText);
    }

    private void closeDoor() {
        curPos.setText(closeDoorText);
    }

    private void alert() {
        Thread thread = new Thread(() -> {
            while (true) {
                curPos.setBackground(Color.RED);
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                }
                curPos.setBackground(Color.GRAY);
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                }
            }
        });
        thread.start();
    }

    //Commit Task
    //synchronized
    private void InnerBtnListener(ActionEvent event) {
        InnerNumButton button = (InnerNumButton) event.getSource();
        button.turnon();
        commitTask(button.floor, null);
    }

    void commitTask(int requiredFloor, EleState eleState) {
        synchronized (requestQueue) {
            requestQueue.add(new TaskSubmit(requiredFloor, eleState)); // Thêm yêu cầu vào Queue
            requestQueue.notify(); // Thông báo cho luồng thang máy
        }

    }

    public void commitOuterTask(int requiredFloor, EleState dir) {
        int index = (dir == EleState.UP ? 1 : 0);
        taskState[requiredFloor][index] = true;
        commitTask(requiredFloor, dir);
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