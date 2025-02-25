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
    private static String dir = "jars\\";
    private static JFrame f;
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
        String cmd = "java \"-Djava.library.path=" + "bin\\natives\" -cp \"" + "bin\\*;" + dir + file + "\" net.minecraft.client.Minecraft";
        System.out.println(cmd);
        
        new Thread() {
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec(cmd);
                    
                    Scanner errorscan = new Scanner(p.getErrorStream()).useDelimiter("\n");
                    while (errorscan.hasNext()) {
                        System.err.print(errorscan.next());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    errorbox(e.toString());
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
    public static void main(String[] args) {
        
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
        
        String[][] rows = new String[rowlist.size()][1];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = rowlist.get(i);
        }
        
        JTable table = new JTable(new DefaultTableModel(rows, new String[] {"File", "Launcher"}) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });  
        
        f = new JFrame("HCraft 1.0");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(400, 200);
        f.setIconImage(Res.getAsImage("icon.png"));
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension((int) scroll.getPreferredSize().getWidth(), 100));
        f.add(scroll, BorderLayout.CENTER);
        
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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
        bottom.add(Box.createRigidArea(new Dimension(2, 0)));
        
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