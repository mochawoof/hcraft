import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.table.DefaultTableModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

class Main {
    private static final String dir = "..\\";
    private static JFrame f;
    private static void errorbox(String e) {
        System.err.println(e);
        JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private static void launch(String v, String file) {
        String cmd = "java \"-Djava.library.path=" + dir + "bin\\natives\" -cp \"" + dir + "bin\\*;" + dir + file + "\" net.minecraft.client.Minecraft";
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
    public static void main(String[] args) {
        String[] versions = new String[] {"b1.8"};
        
        String[] colnames = new String[] {"Version", "File"};
        
        ArrayList<String[]> rowlist = new ArrayList<String[]>();
        
        for (File f : new File(dir).listFiles()) {
            String fname = f.getName();
            
            if (fname.endsWith(".jar")) {
                String v = "";
                for (String ver : versions) {
                    if (fname.contains(ver)) {
                        v = ver;
                    }
                }
                
                rowlist.add(new String[] {v, fname});
            }
        }
        
        String[][] rows = new String[rowlist.size()][2];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = rowlist.get(i);
        }
        
        JTable table = new JTable(new DefaultTableModel(rows, colnames) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });  
        
        f = new JFrame("HCraft");
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