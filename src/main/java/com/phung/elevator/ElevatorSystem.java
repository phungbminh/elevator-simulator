package com.phung.elevator;
import java.util.*;

class ElevatorSystem extends Thread {
    private int position; // Tầng hiện tại của thang máy
    private String direction; // Hướng di chuyển của thang máy
    private Queue<Integer> requestQueue; // Queue lưu trữ các yêu cầu gọi thang máy
    private List<Integer> upList; // Danh sách các tầng đang chờ đi lên
    private List<Integer> downList; // Danh sách các tầng đang chờ đi xuống
    private boolean running; // Biến để kiểm soát vòng lặp

    public ElevatorSystem() {
        this.position = 1; // Khởi tạo tầng hiện tại là 1
        this.direction = "STILL"; // Thang máy đang ở trạng thái không di chuyển
        this.requestQueue = new LinkedList<>();
        this.upList = new ArrayList<>();
        this.downList = new ArrayList<>();
        this.running = true; // Bắt đầu vòng lặp
    }

    public void callElevator(int floor) {
        synchronized (requestQueue) {
            requestQueue.add(floor); // Thêm yêu cầu vào Queue
            requestQueue.notify(); // Thông báo cho luồng thang máy
        }
    }

    @Override
    public void run() {
        while (running) {
            processRequest();
        }
    }

    private void processRequest() {
        Integer requestedFloor = null;

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
            decideDirection(requestedFloor); // Quyết định hướng di chuyển
            moveOneFloor(requestedFloor); // Di chuyển đến tầng được yêu cầu
        }
    }

    private void decideDirection(int requestedFloor) {
        if (requestedFloor > position) {
            direction = "UP";
            upList.add(requestedFloor);
        } else if (requestedFloor < position) {
            direction = "DOWN";
            downList.add(requestedFloor);
        }
    }

    private void moveOneFloor(int requestedFloor) {
        while (position != requestedFloor) {
            // Di chuyển thang máy
            if (direction.equals("UP")) {
                position++;
            } else {
                position--;
            }
            System.out.println("Moving " + direction + " to floor " + position);
            try {
                Thread.sleep(1000); // Mô phỏng thời gian di chuyển giữa các tầng
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Kiểm tra yêu cầu mới trong quá trình di chuyển
            if (checkForNewRequests(requestedFloor)) {
                return; // Nếu có yêu cầu mới, dừng lại
            }
        }
        System.out.println("Arrived at floor " + position);

        // Xóa khỏi danh sách sau khi hoàn thành
        if (direction.equals("UP")) {
            upList.remove(Integer.valueOf(requestedFloor));
        } else {
            downList.remove(Integer.valueOf(requestedFloor));
        }

        // Kiểm tra yêu cầu tiếp theo
        checkNextRequest();
    }

    private boolean checkForNewRequests(int targetFloor) {
        synchronized (requestQueue) {
            if (!requestQueue.isEmpty()) {
                Integer newRequest = requestQueue.peek(); // Lấy yêu cầu mới mà không xóa
                if (newRequest != null) {
                    // Kiểm tra nếu yêu cầu mới cùng hướng và nhỏ hơn tầng mục tiêu
                    if ((direction.equals("UP") && newRequest > position && newRequest < targetFloor) ||
                            (direction.equals("DOWN") && newRequest < position  && newRequest > targetFloor)) {
                        System.out.println("New request for floor " + newRequest + " received. Stopping at this floor.");
                        requestQueue.poll(); // Xóa yêu cầu mới khỏi hàng đợi
                        moveOneFloor(newRequest); // Dừng lại ở tầng mới
                        return true; // Đã dừng lại
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
            moveOneFloor(nextFloor);
        } else if (!downList.isEmpty()) {
            // Xử lý yêu cầu tiếp theo từ danh sách downList
            int nextFloor = downList.get(0);
            moveOneFloor(nextFloor);
        } else {
            // Không còn yêu cầu, thang máy trở về trạng thái rảnh
            direction = "STILL";
        }
    }

    public int getPosition() {
        return position;
    }

    public String getDirection() {
        return direction;
    }

    public void stopElevator() {
        running = false; // Dừng vòng lặp
        this.interrupt(); // Ngắt luồng nếu đang chờ
    }

    public static void main(String[] args) {
        ElevatorSystem elevator = new ElevatorSystem();
        elevator.start(); // Bắt đầu luồng thang máy

        Scanner scanner = new Scanner(System.in);
        System.out.println("Nhập tầng gọi thang máy (hoặc -1 để dừng):");

        while (true) {
            int floor = scanner.nextInt();
            if (floor == -1) {
                break;
            }
            elevator.callElevator(floor); // Gọi thang máy từ tầng nhập vào
        }

        elevator.stopElevator(); // Dừng thang máy
        scanner.close();
    }
}