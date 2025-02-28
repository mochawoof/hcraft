import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.table.DefaultTableModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import java.util.jar.*;
import java.util.Enumeration;

import java.net.URLClassLoader;
import java.net.URL;

class Main {
    // .\\ should be changed to ..\\ while testing
    private static String dir = ".\\jars\\";
    private static String bindir = ".\\bin\\";
    
    private static JFrame f;
    private static JTable table;
    private static String[][] rows;
    private static ArrayList<Process> processes;
    
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
        }
    }
    private static void launchbeta(String file) {
        String cmd = "java \"-Djava.library.path=" + bindir + "natives\" -cp \"" + bindir + "*;" + dir + file + "\" net.minecraft.client.Minecraft";
        System.out.println(cmd);
        
        new Thread() {
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec(cmd);
                    processes.add(p);
                    
                    Scanner errorscan = new Scanner(p.getErrorStream()).useDelimiter("\n");
                    while (errorscan.hasNext()) {
                        System.err.print(errorscan.next());
                    }
                } catch (Exception e) {
                    errorbox(e);
                }
            }
        }.start();
    }
    private static void launchapplet(String file) {
        new Thread() {
            public void run() {
                errorbox("Not yet supported!");
            }
        }.start();
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
    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        f = new JFrame("HCraft 1.2");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(500, 300);
        f.setIconImage(Res.getAsImage("icon.png"));
        
        table = new JTable();
        refresh();
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension((int) scroll.getPreferredSize().getWidth(), 100));
        f.add(scroll, BorderLayout.CENTER);
        
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        JButton openfolderbtn = new JButton("Open Folder");
        openfolderbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(dir));
                } catch (Exception ex) {
                    errorbox(ex.toString());
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
        
        processes = new ArrayList<Process>();
        JButton quitallbtn = new JButton("Quit All");
        quitallbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (Process p : processes) {
                    p.destroyForcibly();
                }
                processes.clear();
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