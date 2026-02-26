package com.mycompany.todoultra;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class TodoFrame extends JFrame {

    private Map<String, String> texts = new HashMap<>();
    private final Map<String, Map<String, String>> taskDictionary = new HashMap<>();
    private List<Task> allTasks = new ArrayList<>();
    private final DefaultListModel<Task> listModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(listModel);
    
    private JTextField inputField;
    private JComboBox<String> catCombo, themeCombo, langCombo;
    private JProgressBar mainProgress;
    private JLabel etaLabel, rachaLabel, titleLabel, avatarLabel;
    private JButton addBtn;
    
    private String currentLang = "ES";
    private int racha = 0, selectedAvatarIdx = 0;
    private String currentUser;
    
    // --- NUEVA CONFIGURACIÓN DE RUTA PORTABLE ---
    private final String RUTA_DATOS = System.getProperty("user.dir") + File.separator + "datos" + File.separator;

    private Color bg, surface, accent, textMain, textSec, urgentColor, successColor, inputBg;
    private Color[] avatarColors = {new Color(255,100,100), new Color(100,255,100), new Color(100,100,255), new Color(255,200,50)};

    public TodoFrame(String user, int avIdx) {
        this.currentUser = user;
        this.selectedAvatarIdx = avIdx;
        setUndecorated(true);
        setSize(1100, 850);
        setLocationRelativeTo(null);
        setBackground(new Color(0,0,0,0)); 
        
        initDictionary();
        loadLanguage("ES");
        loadTasks(); // Usa la nueva ruta automática
        initUI(); 
        applyTheme("Midnight Apple"); 
        setupEvents();
        
        new javax.swing.Timer(1000, e -> { updateLogic(); refreshStats(); }).start();
        new javax.swing.Timer(16, e -> repaint()).start();
    }

    private void initDictionary() {
        Map<String, String> es = new HashMap<>();
        es.put("estudiar", "estudiar"); es.put("leer", "leer"); es.put("comprar", "comprar"); es.put("limpiar", "limpiar");
        Map<String, String> en = new HashMap<>();
        en.put("estudiar", "study"); en.put("leer", "read"); en.put("comprar", "buy"); en.put("limpiar", "clean");
        Map<String, String> fr = new HashMap<>();
        fr.put("estudiar", "étudier"); fr.put("leer", "lire"); fr.put("comprar", "acheter"); fr.put("limpiar", "nettoyer");
        taskDictionary.put("ES", es); taskDictionary.put("EN", en); taskDictionary.put("FR", fr); taskDictionary.put("DE", en);
    }

    // --- MÉTODOS DE GUARDADO ACTUALIZADOS PARA SER PORTABLES ---
    private void saveTasks() {
        try { 
            File d = new File(RUTA_DATOS); 
            if(!d.exists()) d.mkdirs(); // Crea la carpeta 'datos' si no existe
            
            ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(RUTA_DATOS + currentUser + ".dat"));
            o.writeObject(allTasks); 
            o.close();
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void loadTasks() {
        try { 
            ObjectInputStream i = new ObjectInputStream(new FileInputStream(RUTA_DATOS + currentUser + ".dat"));
            allTasks = (List<Task>) i.readObject(); 
            i.close();
        } catch(Exception e) { 
            allTasks = new ArrayList<>(); 
        }
    }

    private void loadLanguage(String l) {
        this.currentLang = l;
        switch(l) {
            case "EN": setTexts("FocusBoard Pro", "Write a task...", "Add", "⚡ Streak: ", "🕒 Goal: ", "Done", "Study", "URGENT", "General"); break;
            case "FR": setTexts("FocusBoard Pro", "Écrire une tâche...", "Ajouter", "⚡ Série: ", "🕒 Objectif: ", "Fait", "Étude", "URGENT", "Général"); break;
            default:   setTexts("FocusBoard Pro", "Escribe una tarea...", "Añadir", "⚡ Racha: ", "🕒 Meta: ", "Listo", "Estudio", "URGENTE", "General"); break;
        }
    }

    private void setTexts(String... s) {
        String[] keys = {"title","hint","add","racha","meta","done","c1","c2","c3"};
        for(int i=0; i<keys.length; i++) texts.put(keys[i], s[i]);
    }

    private void applyTheme(String theme) {
        switch(theme) {
            case "Cozzy Nordic":
                bg = new Color(245, 240, 230, 245); surface = new Color(200, 180, 160, 40);
                accent = new Color(160, 120, 90); textMain = new Color(50, 40, 30);
                textSec = new Color(120, 110, 100); urgentColor = new Color(200, 80, 80);
                successColor = new Color(80, 130, 80); inputBg = Color.WHITE;
                break;
            case "Industrial Steel":
                bg = new Color(45, 50, 55, 245); surface = new Color(255, 255, 255, 10);
                accent = new Color(200, 200, 200); textMain = Color.WHITE;
                textSec = new Color(150, 150, 150); urgentColor = new Color(255, 100, 0);
                successColor = new Color(100, 200, 255); inputBg = new Color(30, 30, 30);
                break;
            default: 
                bg = new Color(15, 18, 25, 245); surface = new Color(255, 255, 255, 15);
                accent = new Color(10, 132, 255); textMain = Color.WHITE;
                textSec = new Color(200, 200, 200, 140); urgentColor = new Color(255, 69, 58);
                successColor = new Color(48, 209, 88); inputBg = new Color(30, 35, 45);
                break;
        }
        if(titleLabel != null) updateStyles();
    }

    private void updateStyles() {
        titleLabel.setForeground(textMain);
        rachaLabel.setForeground(textMain);
        etaLabel.setForeground(textMain);
        inputField.setBackground(inputBg);
        inputField.setForeground(textMain);
        inputField.setCaretColor(textMain);
        mainProgress.setForeground(accent);
        addBtn.repaint();
    }

    private void initUI() {
        JPanel glass = new JPanel(new BorderLayout(0, 25)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),40,40);
                g2.dispose();
            }
        };
        glass.setOpaque(false); glass.setBorder(new EmptyBorder(35, 45, 35, 45));
        setContentPane(glass);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        avatarLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(avatarColors[selectedAvatarIdx]); g2.fillOval(0,0,40,40);
                g2.setColor(Color.WHITE); g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                g2.drawString(currentUser.substring(0,1).toUpperCase(), 14, 27); g2.dispose();
            }
        };
        avatarLabel.setPreferredSize(new Dimension(40,40));
        titleLabel = new JLabel(); titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        rachaLabel = new JLabel(); etaLabel = new JLabel();

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); leftSide.setOpaque(false);
        leftSide.add(avatarLabel); leftSide.add(titleLabel); leftSide.add(rachaLabel); leftSide.add(etaLabel);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10)); nav.setOpaque(false);
        langCombo = new JComboBox<>(new String[]{"ES", "EN", "FR", "DE"});
        themeCombo = new JComboBox<>(new String[]{"Midnight Apple", "Cozzy Nordic", "Industrial Steel"});
        nav.add(langCombo); nav.add(themeCombo); nav.add(createExitBtn());
        header.add(leftSide, BorderLayout.WEST); header.add(nav, BorderLayout.EAST);

        taskList.setCellRenderer(new GlassRenderer()); taskList.setOpaque(false);
        taskList.setFixedCellHeight(100);
        JScrollPane scroll = new JScrollPane(taskList); scroll.setBorder(null);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);

        inputField = new JTextField(); inputField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        inputField.setBorder(new EmptyBorder(10,15,10,15));
        catCombo = new JComboBox<>();
        addBtn = createCapsuleBtn("", new Color(48, 209, 88), e -> addTask());
        addBtn.setPreferredSize(new Dimension(140, 45));

        mainProgress = new JProgressBar(0,100); mainProgress.setPreferredSize(new Dimension(0,12));
        mainProgress.setBorderPainted(false); mainProgress.setBackground(new Color(0,0,0,40));

        JPanel bottom = new JPanel(new BorderLayout(0, 15)); bottom.setOpaque(false);
        JPanel inputLine = new JPanel(new BorderLayout(15,0)); inputLine.setOpaque(false);
        inputLine.add(inputField, BorderLayout.CENTER);
        
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); rightActions.setOpaque(false);
        rightActions.add(catCombo); rightActions.add(addBtn);
        inputLine.add(rightActions, BorderLayout.EAST);
        
        bottom.add(inputLine, BorderLayout.NORTH); bottom.add(mainProgress, BorderLayout.SOUTH);
        glass.add(header, BorderLayout.NORTH); glass.add(scroll, BorderLayout.CENTER); glass.add(bottom, BorderLayout.SOUTH);
        updateTexts();
    }

    private void setupEvents() {
        inputField.addActionListener(e -> addTask());
        inputField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if(inputField.getText().equals(texts.get("hint"))) inputField.setText(""); }
            public void focusLost(FocusEvent e) { if(inputField.getText().isEmpty()) inputField.setText(texts.get("hint")); }
        });

        langCombo.addActionListener(e -> { loadLanguage((String)langCombo.getSelectedItem()); updateTexts(); });
        themeCombo.addActionListener(e -> applyTheme((String)themeCombo.getSelectedItem()));

        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int i = taskList.locationToIndex(e.getPoint()); if(i == -1) return;
                Task t = listModel.get(i); Rectangle r = taskList.getCellBounds(i, i);
                int x = e.getX() - r.x;
                if(x < 50) allTasks.remove(t);
                else if(x > r.width - 70) { t.done = !t.done; racha = t.done ? racha+1 : Math.max(0, racha-1); }
                else if(t.catId == 1 && !t.done) { 
                    if(x > r.width - 270 && x < r.width - 210) t.timerRunning = !t.timerRunning;
                }
                saveTasks(); refreshStats();
            }
        });
        
        MouseAdapter ma = new MouseAdapter() {
            Point p; public void mousePressed(MouseEvent e){ p = e.getPoint(); }
            public void mouseDragged(MouseEvent e){ setLocation(getLocation().x+e.getX()-p.x, getLocation().y+e.getY()-p.y); }
        };
        addMouseListener(ma); addMouseMotionListener(ma);
    }

    private void updateTexts() {
        titleLabel.setText(texts.get("title") + " - " + currentUser);
        addBtn.setText(texts.get("add"));
        inputField.setText(texts.get("hint"));
        catCombo.removeAllItems();
        catCombo.addItem(texts.get("c1")); catCombo.addItem(texts.get("c2")); catCombo.addItem(texts.get("c3"));
        refreshStats();
    }

    private void refreshStats() {
        if (mainProgress == null) return;
        int done = 0, mins = 0;
        listModel.clear();
        for(Task t : allTasks) { listModel.addElement(t); if(t.done) done++; else if(t.catId == 1) mins += (t.secondsLeft/60); }
        rachaLabel.setText(texts.get("racha") + racha);
        etaLabel.setText(texts.get("meta") + (mins > 0 ? LocalTime.now().plusMinutes(mins).format(DateTimeFormatter.ofPattern("HH:mm")) : texts.get("done")));
        mainProgress.setValue(allTasks.isEmpty() ? 0 : (done * 100 / allTasks.size()));
    }

    private void updateLogic() {
        for(Task t : allTasks) if(t.timerRunning && t.secondsLeft > 0 && !t.done) t.secondsLeft--;
    }

    private void addTask() {
        String s = inputField.getText().trim();
        int catIdx = catCombo.getSelectedIndex();
        if(!s.isEmpty() && !s.equals(texts.get("hint"))) {
            int mins = 25;
            if(catIdx == 0) {
                String input = JOptionPane.showInputDialog(this, "¿Minutos de Pomodoro?", "25");
                try { mins = Integer.parseInt(input); } catch(Exception e) { mins = 25; }
            }
            allTasks.add(new Task(s, catIdx, mins));
            inputField.setText(""); saveTasks(); refreshStats();
        }
    }

    private JButton createExitBtn() {
        JButton b = new JButton() { @Override protected void paintComponent(Graphics g) { g.setColor(urgentColor); g.fillOval(0,0,16,16); } };
        b.setPreferredSize(new Dimension(16,16)); b.setBorder(null); b.addActionListener(e -> { saveTasks(); System.exit(0); });
        return b;
    }

    private JButton createCapsuleBtn(String t, Color c, ActionListener al) {
        JButton b = new JButton(t); b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false);
        b.addActionListener(al); b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c1) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c); g2.fillRoundRect(0,0,c1.getWidth(),c1.getHeight(),20,20); 
                g2.setColor(Color.WHITE); g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int x = (c1.getWidth() - fm.stringWidth(b.getText())) / 2;
                int y = (c1.getHeight() + fm.getAscent()) / 2 - 4;
                g2.drawString(b.getText(), x, y); g2.dispose();
            }
        });
        return b;
    }

    static class Task implements Serializable {
        String originalText; int catId; boolean done = false; int secondsLeft;
        boolean timerRunning = false;
        Task(String t, int cid, int m) { this.originalText = t.toLowerCase(); this.catId = (cid + 1); this.secondsLeft = m*60; }
        public String getDisplay(Map<String, Map<String, String>> dict, String lang) {
            if (!dict.containsKey(lang)) return originalText;
            String[] words = originalText.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String w : words) { sb.append(dict.get(lang).getOrDefault(w, w)).append(" "); }
            String res = sb.toString().trim();
            return res.isEmpty() ? "" : res.substring(0, 1).toUpperCase() + res.substring(1);
        }
    }

    class GlassRenderer extends JPanel implements ListCellRenderer<Task> {
        JLabel n = new JLabel(), ti = new JLabel(); Task curr;
        public GlassRenderer() { setLayout(new BorderLayout()); setOpaque(false); n.setFont(new Font("SansSerif", Font.PLAIN, 18));
            ti.setFont(new Font("Monospaced", Font.BOLD, 18)); add(n, BorderLayout.WEST); add(ti, BorderLayout.EAST);
            setBorder(new EmptyBorder(0,70,0,120));
        }
        public Component getListCellRendererComponent(JList<? extends Task> l, Task t, int i, boolean s, boolean f) {
            curr = t; n.setText(t.getDisplay(taskDictionary, currentLang)); n.setForeground(t.done ? textSec : textMain);
            String timeStr = String.format("%02d:%02d  ", t.secondsLeft/60, t.secondsLeft%60);
            ti.setText(t.catId == 1 ? timeStr : ""); ti.setForeground(accent); return this;
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0,0,0,30)); g2.fillRoundRect(8,8,getWidth()-16,getHeight()-16,20,20);
            Color cardColor = surface;
            if(curr.catId == 2) cardColor = new Color(urgentColor.getRed(), urgentColor.getGreen(), urgentColor.getBlue(), 50);
            if(curr.catId == 1) cardColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50);
            g2.setColor(cardColor); g2.fillRoundRect(5,5,getWidth()-10,getHeight()-10,20,20);
            g2.setColor(urgentColor); g2.setFont(new Font("SansSerif", Font.BOLD, 18)); g2.drawString("✕", 25, 55);
            if(curr.catId == 1 && !curr.done) {
                g2.setColor(accent); int startX = getWidth() - 250;
                if(curr.timerRunning) { g2.fillRect(startX, 40, 6, 22); g2.fillRect(startX + 10, 40, 6, 22); }
                else { g2.fillPolygon(new int[]{startX, startX, startX + 20}, new int[]{40, 62, 51}, 3); }
            }
            g2.setColor(curr.done ? successColor : new Color(150,150,150,100)); g2.fillRoundRect(getWidth()-60, 35, 45, 24, 12, 12);
            g2.setColor(Color.WHITE); g2.fillOval(curr.done ? getWidth()-36 : getWidth()-58, 37, 20, 20);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        JPanel lp = new JPanel(new GridLayout(3, 1, 10, 10));
        JTextField uf = new JTextField("Admin");
        uf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { if(uf.getText().equals("Admin")) uf.setText(""); }
        });
        JComboBox<String> ac = new JComboBox<>(new String[]{"Red Hero", "Green Nature", "Blue Sky", "Gold Star"});
        lp.add(new JLabel("Usuario:")); lp.add(uf); lp.add(ac);
        if (JOptionPane.showConfirmDialog(null, lp, "FocusBoard Login", 2, -1) == 0 && !uf.getText().trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> { new TodoFrame(uf.getText(), ac.getSelectedIndex()).setVisible(true); });
        } else { System.exit(0); }
    }
}