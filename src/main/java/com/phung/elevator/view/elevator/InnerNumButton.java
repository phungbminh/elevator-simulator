package com.phung.elevator.view.elevator;

import com.phung.elevator.utils.BaseButton;
import lombok.Getter;

@Getter
public class InnerNumButton extends BaseButton {
    int floor = 0;

    public InnerNumButton(int floor, String val) {
        super(val);
        this.floor = floor;
    }

}