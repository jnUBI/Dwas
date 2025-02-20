package client;

import common.Command;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class InputController {

    private static ObjectOutputStream out;
    private static Robot robot;

    public static void startListening(ObjectOutputStream outputStream){
        out = outputStream;

        try{
            robot = new Robot();
        } catch (AWTException e){
            System.err.println("Error creating Robot instance: " + e.getMessage());
        }

        //Example usage Mouse and Key Listeners
        // Add Key and Mouse Listener implementations here if needed
        // For example:
        // MouseInfo.getPointerInfo().getLocation()  //to get mouse location
        // KeyEvent.VK_A // Example keycode
    }

    public static void mouseMove(MouseEvent e){
        //Implement mousemove logic
    }

    public static void mouseClick(MouseEvent e){
        //Implement mouseclick logic
    }

    public static void keyPress(KeyEvent e){
        //Implement keyPress logic
    }
}