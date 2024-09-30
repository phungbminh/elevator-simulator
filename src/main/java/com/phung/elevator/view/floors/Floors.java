package com.phung.elevator.view.floors;

import com.phung.elevator.utils.ConstantInfo;
import com.phung.elevator.controller.ElevatorsController;
import com.phung.elevator.controller.ElevatorsTask;
import com.phung.elevator.utils.FloorFLags;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

//Nút bấm cho nhiều tầng
public class Floors extends JPanel {
    //UI
    private OuterButton[] buttons = new OuterButton[ConstantInfo.MaxFloor * 2];
    private JLabel[] floorFlags = new JLabel[ConstantInfo.MaxFloor];
    private ElevatorsController controller;

//    TODO: paintComponent()

    public Floors(ElevatorsController controller) {
        this.controller = controller;
        controller.setFloors(this);

        setLayout(null);
        for (int i = 1; i < ConstantInfo.MaxFloor * 2 - 1; i++) {
            buttons[i] = new OuterButton(ConstantInfo.MaxFloor - i / 2, (i + 1) % 2);
            //init
            buttons[i].setMargin(new Insets(1, 1, 1, 1));
            buttons[i].setFont(new Font(buttons[i].getFont().getFontName(), buttons[i].getFont().getStyle(), 15));
            buttons[i].setBounds((i % 2 + 1) * (ConstantInfo.floorButtonWidth + ConstantInfo.floorButtonSpace),
                    (i / 2) * (ConstantInfo.floorButtonSpace + ConstantInfo.floorButtonHeight),
                    //0,0,
                    ConstantInfo.floorButtonWidth,
                    ConstantInfo.floorButtonHeight
            );
            buttons[i].setBackground(Color.WHITE);
            buttons[i].setForeground(Color.BLACK);
            buttons[i].addActionListener(buttonListener);

            this.add(buttons[i]);
        }

        FloorFLags.addFloorFLags(floorFlags, this, ConstantInfo.floorFlagStart);
    }
    //TODO: MOdify this fuction to commit a Task
    ActionListener buttonListener = event -> {
        OuterButton pressButton = (OuterButton) event.getSource();
        pressButton.turnon();
        controller.commitTask(new ElevatorsTask(pressButton.getFloor(), pressButton.getDirection()));
    };

    public void turnoffLight(int floor, int up) {
        System.out.println("Turn off" + ((1 - up) + 2 * (floor - 1)));
        buttons[(ConstantInfo.MaxFloor - floor) * 2 + (1 - up)].turnoff();
    }
}
