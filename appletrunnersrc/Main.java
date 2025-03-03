import java.util.ArrayList;
import java.net.*;
import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;

class Main {
    public static void main(String[] args) {
        String nativesdir = args[0];
        String bindir = args[1];
        String file = args[2];
        String clazz = args[3];
        
        try {
            // Load natives
            System.setProperty("org.lwjgl.librarypath", nativesdir);
            
            // Load classes
            ArrayList<URL> classeslist = new ArrayList<URL>();
            for (File fl : new File(bindir).listFiles()) {
                if (fl.isFile()) {
                    classeslist.add(fl.toURL());
                }
            }
            classeslist.add(new File(file).toURL());
            
            URL[] classes = new URL[classeslist.size()];
            for (int i = 0; i < classes.length; i++) {
                classes[i] = classeslist.get(i);
            }
            
            //  Load mc jar
            URLClassLoader cl = new URLClassLoader(classes);
            
            Class c = Class.forName(clazz, true, cl);
            Applet a = (Applet) c.newInstance();
            
            JFrame fr = new JFrame("Minecraft");
            fr.setSize(656, 519);
            fr.add(a, BorderLayout.CENTER);
            fr.addComponentListener(new ComponentListener() {
                public void componentShown(ComponentEvent e) {}
                public void componentHidden(ComponentEvent e) {}
                public void componentMoved(ComponentEvent e) {}
                public void componentResized(ComponentEvent e) {
                    a.resize(fr.getWidth(), fr.getHeight());
                }
            });
            
            a.init();
            a.start();
            a.resize(fr.getWidth(), fr.getHeight());
            
            fr.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Class load failed");
        }
    }
}