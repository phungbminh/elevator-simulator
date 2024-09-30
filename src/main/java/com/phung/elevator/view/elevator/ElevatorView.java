package com.phung.elevator.view.elevator;

import com.phung.elevator.utils.ConstantInfo;
import com.phung.elevator.utils.FloorFLags;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ElevatorView extends JPanel {

    ArrayList<InnerNumButton> innerButtons = new ArrayList<>();
    private JLabel[] floorFlags = new JLabel[20];
    JLabel curPos;
    private static final int othersNum = 3;
    private static final String[] others = {"><", "<>", "🔔", };

    public ElevatorView(int buttonStarty) {
        super();
        this.setBorder(BorderFactory.createEtchedBorder());
        setLayout(null);

        //Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(null);
        add(buttonPanel);
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        buttonPanel.setBounds(0, buttonStarty, ConstantInfo.innerTotalButtonx, ConstantInfo.innerTotalButtony);

        for (int i = 0; i < ConstantInfo.MaxFloor; i++) {
            innerButtons.add(new InnerNumButton(i + 1, "" + (i + 1)));
            JButton ref = innerButtons.get(i);
            ref.setMargin(new Insets(1, 1, 1, 1));
            ref.setFont(new Font(ref.getFont().getFontName(),
                    ref.getFont().getStyle(), 15));

            ref.setBounds(
                    (i % ConstantInfo.buttonsPerLine) * ConstantInfo.innerButtonWidth, ConstantInfo.innerSpace + (i / ConstantInfo.buttonsPerLine) * ConstantInfo.innerButtonWidth,
                    ConstantInfo.innerButtonWidth, ConstantInfo.innerButtonHeight
            );

            ref.setBackground(Color.GRAY);
            ref.setForeground(Color.DARK_GRAY);
            ref.addActionListener(event -> {
                InnerNumButton pressButton = (InnerNumButton) event.getSource();
            });

            buttonPanel.add(ref);
        }

        for (int i = 0; i < othersNum; i++) {
            innerButtons.add(new InnerNumButton(0, others[i]));
            JButton ref = innerButtons.get(i + ConstantInfo.MaxFloor);
            ref.setMargin(new Insets(1, 1, 1, 1));
            ref.setFont(new Font(ref.getFont().getFontName(),
                    ref.getFont().getStyle(), 15));

            ref.setBounds(
                    ConstantInfo.buttonsPerLine * ConstantInfo.innerButtonWidth, ConstantInfo.innerSpace + (i % ConstantInfo.buttonsPerLine + 1) * ConstantInfo.innerButtonHeight,
                    ConstantInfo.innerButtonWidth * 2, ConstantInfo.innerButtonHeight
            );
            buttonPanel.add(ref);

            ref.setBackground(Color.GRAY);
            ref.setForeground(Color.DARK_GRAY);
            ref.addActionListener(null);
        }
        curPos = new JLabel("=||=");
        curPos.setBackground(Color.GRAY);
        curPos.setOpaque(true);

        curPos.setHorizontalAlignment(SwingConstants.CENTER);

        this.add(curPos);

        FloorFLags.addFloorFLags(floorFlags, this, ConstantInfo.innerTotalButtonx);

        Point floorOne = floorFlags[0].getLocation();
        curPos.setBounds(floorOne.x, floorOne.y, ConstantInfo.floorButtonWidth, ConstantInfo.floorButtonHeight);
    }

}