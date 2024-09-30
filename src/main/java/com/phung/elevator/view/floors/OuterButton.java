package com.phung.elevator.view.floors;

import com.phung.elevator.utils.BaseButton;
import lombok.Getter;

@Getter
public class OuterButton extends BaseButton {
    private static String[] buttonFlags = new String[]{"🔻", "🔺"};
    private int floor;
    private int direction;

    OuterButton(int floor, int up) {
        super(buttonFlags[up]);
        this.floor = floor;
        this.direction = up;
    }

}