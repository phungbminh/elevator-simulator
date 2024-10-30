package com.phung.elevator.controller;

import com.phung.elevator.utils.ConstantInfo;
import com.phung.elevator.utils.EleState;
import com.phung.elevator.view.elevator.Elevator;
import com.phung.elevator.view.floors.Floors;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
public class ElevatorsController extends Thread{
    private Elevator[] elevators;
    private Queue<ElevatorsTask> elevatorsTasks = new LinkedList<>();

    private int lastElevator = 0;
    private Floors floors;

    public ElevatorsController(Elevator[] elevators){
        this.elevators = elevators;
        for(int i = 0; i < ConstantInfo.ElevatorNum; i++){
            this.elevators[i].setController(this);
        }
    }
    //Chọn thang máy tốt nhất dựa trên nhiệm vụ được giao
    @Override
    public void run(){
        //Bỏ phiếu, ngủ khi không có nhiệm vụ
        while (true){
            if(elevatorsTasks.isEmpty()){
                try{
                    sleep(1000);
                }catch (InterruptedException e){
                    System.out.println("Controller!");
                }
                continue;
            }
            //Sắp xếp Task vào thang máy phù hợp
            while (!elevatorsTasks.isEmpty()){
                ElevatorsTask elevatorsTask = elevatorsTasks.remove();
                schedule(elevatorsTask);
            }
        }
    }
    //
    public synchronized void commitTask(ElevatorsTask elevatorsTask){
        elevatorsTasks.add(elevatorsTask);
        printTasks();
    }
    public void printTasks() {
        if (elevatorsTasks.isEmpty()) {
            System.out.println("No tasks in the queue.");
            return;
        }

        System.out.println("Elevator Tasks:");
        for (ElevatorsTask task : elevatorsTasks) {
            String directionString = (task.direction == EleState.UP) ? "Up" : "Down";
            System.out.println("=>>> Queue<ElevatorsTask> : Floor: " + task.floor + ", Direction: " + directionString);
        }
    }

    //Lập kế hoạch logic

    void schedule(ElevatorsTask elevatorsTask){
        //System.out.println("Dieu phoi thang may");
        int start = lastElevator;
        int distance = ConstantInfo.MaxFloor + 1;//Khoảng cách giữa thang máy ứng viên và hành khách được đón

        for(int i = 0; i < ConstantInfo.ElevatorNum; i++){
            start = (start + 1)  % ConstantInfo.ElevatorNum;
            System.out.println("So luong thang may(start): " + start + 1);
            //System.out.println("Task direction: " +  elevatorsTask.direction);
            if(elevators[start].getCurrentElevatorState() == elevatorsTask.direction){
                if(elevatorsTask.direction == EleState.UP && elevatorsTask.floor >= elevators[start].getCurentFloor() || elevatorsTask.direction == EleState.DOWN && elevatorsTask.floor<=elevators[start].getCurentFloor()
                ){
                    lastElevator = start;
                    break;
                }
            }
            if(elevators[start].getCurrentElevatorState() == EleState.STALL){
                int ndistance = Math.abs(elevators[start].getCurentFloor() - elevatorsTask.floor);
                if(distance > ndistance){
                    lastElevator = start;
                    distance = ndistance;
                }
            }
        }
//        lastElevator = start;
        elevators[lastElevator].commitOuterTask(elevatorsTask.floor, elevatorsTask.direction);
    }

    //Tắt đèn
    public void turnoffLight(int floor, int up){
        floors.turnoffLight(floor, up);
    }
}
