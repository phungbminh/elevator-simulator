package com.phung.elevator;

import com.phung.elevator.view.MainView;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to Elevator Simulator!");
        MainView frame = new MainView();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
