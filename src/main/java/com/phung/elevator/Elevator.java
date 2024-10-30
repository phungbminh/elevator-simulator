package com.phung.elevator;
import java.util.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

class Elevator {
    private int position; // Tầng hiện tại của thang máy
    private String direction; // Hướng di chuyển của thang máy
    private Queue<Request> requestQueue; // Queue lưu trữ yêu cầu gọi thang máy

    // Lớp nội bộ để lưu trữ yêu cầu
    private static class Request {
        int fromFloor; // Tầng yêu cầu
        int toFloor; // Tầng đích
        String direction; // Hướng di chuyển

        Request(int fromFloor, int toFloor, String direction) {
            this.fromFloor = fromFloor;
            this.toFloor = toFloor;
            this.direction = direction;
        }
    }

    public Elevator() {
        this.position = 1; // Khởi tạo tầng hiện tại là 1
        this.direction = "Idle"; // Thang máy bắt đầu ở trạng thái rảnh
        this.requestQueue = new LinkedList<>();
    }

    // Phương thức để gọi thang máy từ tầng hiện tại đến tầng đích với hướng di chuyển
    public void callElevator(int fromFloor, int toFloor, String direction) {
        requestQueue.add(new Request(fromFloor, toFloor, direction)); // Thêm yêu cầu vào Queue
        processRequest(); // Xử lý yêu cầu
    }

    // Phương thức xử lý yêu cầu gọi thang máy
    private void processRequest() {
        if (direction.equals("Idle") && !requestQueue.isEmpty()) {
            Request nextRequest = requestQueue.poll(); // Lấy yêu cầu đầu tiên từ Queue
            direction = nextRequest.direction; // Lấy hướng di chuyển từ yêu cầu
            moveElevator(nextRequest.toFloor); // Di chuyển thang máy đến tầng yêu cầu
        }
    }

    // Phương thức di chuyển thang máy
    private void moveElevator(int nextFloor) {
        System.out.println("Thang máy đang di chuyển từ tầng " + position + " đến tầng " + nextFloor);
        position = nextFloor; // Cập nhật vị trí thang máy
        System.out.println("Thang máy đã đến tầng " + position);
        // Đặt lại trạng thái thang máy về rảnh
        direction = "Idle";
        removeCompletedRequest(nextFloor); // Xóa yêu cầu đã hoàn thành
    }

    // Phương thức xóa yêu cầu đã hoàn thành
    private void removeCompletedRequest(int floor) {
        System.out.println("Yêu cầu đi từ tầng " + (floor) + " đã được hoàn thành."); // Giả sử tầng là floor
        processRequest(); // Kiểm tra và xử lý yêu cầu tiếp theo
    }

    public static void main(String[] args) {
        Elevator elevator = new Elevator();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Nhập tầng bạn muốn gọi thang máy (hoặc nhập -1 để thoát): ");
            int fromFloor = scanner.nextInt();
            if (fromFloor == -1) {
                break; // Thoát khỏi vòng lặp
            }

            System.out.println("Nhập tầng bạn muốn đến: ");
            int toFloor = scanner.nextInt();

            // Xác định hướng di chuyển
            String direction = fromFloor < toFloor ? "Up" : "Down";
            elevator.callElevator(fromFloor, toFloor, direction); // Gọi thang máy
        }

        scanner.close(); // Đóng scanner
    }
}