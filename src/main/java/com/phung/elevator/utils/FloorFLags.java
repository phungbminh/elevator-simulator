package com.phung.elevator.utils;


import javax.swing.*;
import java.awt.*;

public class FloorFLags {

    public static void addFloorFLags(JLabel[] floorFlags, JPanel parent, int startPos) {
        System.out.println("addFloorFLags: startPos: " + startPos);
        for (int i = 0; i < ConstantInfo.MaxFloor; i++) {
            floorFlags[i] = new JLabel(String.valueOf((i + 1)));
            floorFlags[i].setHorizontalAlignment(SwingConstants.CENTER);
            floorFlags[i].setFont(new Font(floorFlags[i].getFont().getFontName(),
                    floorFlags[i].getFont().getStyle(), 15));
            floorFlags[i].setBounds(startPos,
                    (ConstantInfo.MaxFloor - 1 - i) * (ConstantInfo.floorButtonSpace + ConstantInfo.floorButtonHeight),
                    ConstantInfo.floorButtonWidth, ConstantInfo.floorButtonHeight
            );
            floorFlags[i].setBackground(Color.green);
            floorFlags[i].setOpaque(true);
            parent.add(floorFlags[i]);
        }
    }
}