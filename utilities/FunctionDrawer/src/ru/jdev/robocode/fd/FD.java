package ru.jdev.robocode.fd;

import javax.swing.*;
import java.awt.*;

/**
 * User: jdev
 * Date: 08.10.2010
 */
public class FD extends JFrame {

    public FD() throws HeadlessException {
        getRootPane().setLayout(new BorderLayout());

        FunctionCanvas fCanvas = new FunctionCanvas(new Sigmoid());        
        getRootPane().add(fCanvas, BorderLayout.CENTER);

        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        FD fd = new FD();
        fd.setVisible(true);
        fd.repaint();
    }

}
