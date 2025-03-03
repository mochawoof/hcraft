import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.table.DefaultTableModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import java.util.jar.*;
import java.util.Enumeration;

import java.net.*;
import java.nio.file.Paths;

import java.applet.*;
import java.lang.reflect.*;

class Main {
    // ./ should be changed to ../ while testing
    private static String dir = norm("../jars/");
    private static String bindir = norm("../bin/");
    private static String datadir = norm("../data/");
    
    private static JFrame f;
    private static JTable table;
    private static String[][] rows;
    private static ArrayList<Process> processes = new ArrayList<Process>();
    
    private static String resolve(String path, String opath) {
        return Paths.get(path).toAbsolutePath().normalize().resolve(opath).toAbsolutePath().normalize().toString();
    }
    
    private static String norm(String path) {
        return Paths.get(path).toAbsolutePath().normalize().toString();
    }
    
    private static void errorbox(Exception e) {
        errorbox(e.toString());
    }
    private static void errorbox(String e) {
        System.err.println(e);
        JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private static void launch(String file, String type) {
        if (type.equals("Beta")) {
            launchbeta(file);
        } else if (type.equals("Applet")) {
            launchapplet(file);
        } else if (type.equals("Classic Applet")) {
            launchclassicapplet(file);
        }
    }
    private static void wrapprocess(ProcessBuilder pb, String file) {
        new Thread() {
            public void run() {
                try {
                    pb.environment().put("APPDATA", resolve(datadir, file.substring(0, file.lastIndexOf("."))));
                    System.out.println(pb.command().toString());
                    
                    Process p = pb.start();
                    processes.add(p);
                    
                    Scanner errorscan = new Scanner(p.getErrorStream()).useDelimiter("\n");
                    while (errorscan.hasNext()) {
                        System.err.println(errorscan.next());
                    }
                } catch (Exception e) {
                    errorbox(e);
                }
            }
        }.start();
    }
    private static void launchbeta(String file) {
        wrapprocess(
            new ProcessBuilder("java", "-Djava.library.path=" + resolve(bindir, "natives"), "-cp", resolve(bindir, "STAR").replace("STAR", "*") + ";" + resolve(dir, file), "net.minecraft.client.Minecraft"), 
            file
        );
    }
    private static void appletrunner(String file, String clazz) {
        wrapprocess(
            new ProcessBuilder("java", "-cp", resolve(dir, file), "-jar", resolve(bindir, "appletrunner.jar"), resolve(bindir, "natives"), bindir, norm(file), clazz), 
            file
        );
    }
    private static void launchapplet(String file) {
        appletrunner(resolve(dir, file), "net.minecraft.client.MinecraftApplet");
    }
    private static void launchclassicapplet(String file) {
        appletrunner(resolve(dir, file), "com.mojang.minecraft.MinecraftApplet");
    }
    private static Component spacing() {
        return Box.createRigidArea(new Dimension(1, 0));
    }
    private static void refresh() {
        ArrayList<String[]> rowlist = new ArrayList<String[]>();
        
        for (File fl : new File(dir).listFiles()) {
            String fname = fl.getName();
            String type = "Invalid";
            
            if (fname.endsWith(".jar")) {
                try (JarFile jar = new JarFile(fl)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry e = entries.nextElement();
                        if (e.getName().equals("net/minecraft/client/Minecraft.class")) {
                            type = "Beta";
                        } else if (e.getName().equals("net/minecraft/client/MinecraftApplet.class")) {
                            if (!type.equals("Beta")) {
                                type = "Applet";
                            }
                        } else if (e.getName().equals("com/mojang/minecraft/MinecraftApplet.class")) {
                            if (!type.equals("Beta")) {
                                type = "Classic Applet";
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Invalid jar " + fname);
                }
                
                if (!type.equals("Invalid")) {
                    rowlist.add(new String[] {fname, type});
                }
            }
        }
        
        rows = new String[rowlist.size()][1];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = rowlist.get(i);
        }
        
        table.setModel(new DefaultTableModel(rows, new String[] {"File", "Launcher"}) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
    }
    private static void quitAll() {
        for (Process p : processes) {
            p.destroyForcibly();
        }
        processes.clear();
    }
    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        f = new JFrame("HCraft 1.3");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(600, 300);
        f.setIconImage(Res.getAsImage("icon.png"));
        
        table = new JTable();
        refresh();
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension((int) scroll.getPreferredSize().getWidth(), 100));
        f.add(scroll, BorderLayout.CENTER);
        
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        JButton openselecteddatafolderbtn = new JButton("Open Selected Data Folder");
        openselecteddatafolderbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int r = table.getSelectedRow();
                if (r != -1 && r < rows.length) {
                    try {
                        Desktop.getDesktop().open(new File(resolve(datadir, rows[r][0].substring(0, rows[r][0].lastIndexOf(".")))));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        errorbox("Selected data folder does not exist yet!");
                    }
                }
            }
        });
        bottom.add(openselecteddatafolderbtn);
        bottom.add(spacing());
        
        JButton openfolderbtn = new JButton("Open Folder");
        openfolderbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(dir));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorbox("Could not access jar folder!");
                }
            }
        });
        bottom.add(openfolderbtn);
        bottom.add(spacing());
        
        JButton refreshbtn = new JButton("Refresh");
        refreshbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        bottom.add(refreshbtn);
        
        bottom.add(Box.createGlue());
        f.add(bottom, BorderLayout.PAGE_END);
        
        JButton playbtn = new JButton("Play");
        playbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int r = table.getSelectedRow();
                if (r != -1 && r < rows.length) {
                    launch(rows[r][0], rows[r][1]);
                }
            }
        });
        bottom.add(playbtn);
        bottom.add(spacing());
        
        JButton quitallbtn = new JButton("Quit All");
        quitallbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quitAll();
            }
        });
        bottom.add(quitallbtn);
        bottom.add(spacing());
        
        JButton closebtn = new JButton("Close");
        closebtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               System.exit(0);
            }
        });
        bottom.add(closebtn);
        
        f.setVisible(true);
    }
}