package com.phung.elevator.view;

import com.phung.elevator.controller.ElevatorsController;
import com.phung.elevator.utils.ConstantInfo;
import com.phung.elevator.view.elevator.Elevator;
import com.phung.elevator.view.floors.Floors;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {
    public MainView() {
        super();
        int elevatorWidth = ConstantInfo.innerTotalButtonx + ConstantInfo.elevatorSpace;
        //init
        setSize(300 + (elevatorWidth * ConstantInfo.ElevatorNum),  ConstantInfo.WindowHeight);

        Elevator[] elevators = new Elevator[ConstantInfo.ElevatorNum];
        for (int i = 0; i < ConstantInfo.ElevatorNum; i++) {
            elevators[i] = new Elevator(ConstantInfo.WindowHeight - ConstantInfo.innerTotalButtony, this);
            elevators[i].setBounds(ConstantInfo.elevatorStart + i * (ConstantInfo.innerTotalButtonx + ConstantInfo.elevatorSpace),
                    0,
                    elevatorWidth, ConstantInfo.WindowHeight);
            elevators[i].start();
        }

        ElevatorsController controller = new ElevatorsController(elevators);
        controller.start();

        Floors floorView = new Floors(controller);
        floorView.setBackground(Color.WHITE);
        floorView.setBounds(1180, 0, (ConstantInfo.floorButtonWidth + ConstantInfo.floorButtonSpace) * 3,
                (ConstantInfo.floorButtonHeight + ConstantInfo.floorButtonSpace) * ConstantInfo.MaxFloor);
        add(floorView);

        this.setVisible(true);
        this.setResizable(false);
    }

}
