package com.phung.elevator.controller;

import com.phung.elevator.utils.EleState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ElevatorsTask {
    public int floor;
    public EleState direction;

    public ElevatorsTask(int floor, int direction){
        this.floor = floor;
        this.direction = direction == 1 ? EleState.UP : EleState.DOWN;
    }
}
