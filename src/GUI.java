import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.EmptyBorder;

public class GUI {

    private final String dir = "Cards/";
    private String filename = "";
    private FlashcardSet set;

    // Shared UI state so Create/Modify can update review panel directly
    private JLabel sharedCardLabel;
    private JLabel sharedProgressLabel;
    private Runnable sharedUpdateCard;
    private int sharedCurrentIndex = 0;
    private boolean sharedShowQuestion = true;

    public GUI() {
        Font helvetica = new Font("Helvetica", Font.PLAIN, 18);
        UIManager.put("Label.font", helvetica);
        UIManager.put("Button.font", helvetica);
        UIManager.put("TextField.font", helvetica);
        UIManager.put("TextArea.font", helvetica);
        UIManager.put("ComboBox.font", helvetica);
        UIManager.put("Panel.font", helvetica);
        UIManager.put("TabbedPane.font", helvetica);

        JFrame frame = new JFrame("CogniStudy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        JPanel background = new GradientPanel();
        background.setLayout(new BorderLayout());
        frame.setContentPane(background);

        JLabel title = new JLabel("CogniStudy: An Interactive Education Application", SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(30, 0, 20, 0));
        background.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 40, 40));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(40, 80, 40, 80));

        AnimatedButton reviewButton = new AnimatedButton("Review Flashcards");
        reviewButton.setPreferredSize(new Dimension(200, 200));
        reviewButton.setGradient(new Color(255, 152, 0), new Color(245, 124, 0)); // Orange gradient

        AnimatedButton createButton = new AnimatedButton("Edit and Modify");
        createButton.setPreferredSize(new Dimension(200, 200));
        createButton.setGradient(new Color(255, 64, 129), new Color(197, 17, 98)); // Hot pink → magenta

        AnimatedButton folderButton = new AnimatedButton("Create a New Folder");
        folderButton.setPreferredSize(new Dimension(200, 200));
        folderButton.setGradient(new Color(76, 175, 80), new Color(56, 142, 60)); // Green Gradient

        centerPanel.add(folderButton);
        centerPanel.add(reviewButton);
        centerPanel.add(createButton);
        background.add(centerPanel, BorderLayout.CENTER);

        JLabel credits = new JLabel(
                "Created by Mateo Alado, Amreen Ahmed, Sanad Atia, Ohenewaa Ampem Darko, and Twinkle Wilson - Fall 2025",
                SwingConstants.CENTER);
        credits.setFont(new Font("Helvetica", Font.PLAIN, 16));
        credits.setForeground(Color.WHITE);
        credits.setBorder(new EmptyBorder(10, 0, 20, 0));
        background.add(credits, BorderLayout.SOUTH);

        reviewButton.addActionListener(e -> {

            reviewGUI();
        });

        createButton.addActionListener(e -> {

            createModifyGUI();
        });

        folderButton.addActionListener(e -> {

            folderGUI();
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private java.io.File resolveSetFile(String baseName) {
        java.io.File f1 = new java.io.File("Cards/" + baseName + ".csv");
        if (f1.exists()) return f1;
        java.io.File f2 = new java.io.File("../Cards/" + baseName + ".csv");
        if (f2.exists()) return f2;
        return null;
    }

    public void reviewGUI() {
        JFrame reviewFrame = new JFrame("Review Flashcards");

        reviewFrame.setLayout(new BorderLayout());
        reviewFrame.setResizable(true);

        JPanel textBoxPanel = new JPanel();
        JLabel textBoxLabel = new JLabel("Enter a Flashcard Set: ");
        JTextField textField = new JTextField(20);
        JButton submitButton = new JButton("Select");
        textBoxPanel.add(textBoxLabel);
        textBoxPanel.add(textField);
        textBoxPanel.add(submitButton);
        reviewFrame.add(textBoxPanel, BorderLayout.NORTH);

        JPanel reviewPanel = new JPanel();
        JButton cardButton = new JButton("Start");
        cardButton.setFont(new Font("Helvetica", Font.PLAIN, 24));
        cardButton.setEnabled(false);
        reviewPanel.add(cardButton);
        reviewFrame.add(reviewPanel, BorderLayout.CENTER);

        reviewFrame.pack();
        reviewFrame.setLocationRelativeTo(null);
        reviewFrame.setVisible(true);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = textField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(reviewFrame, "Please enter a filename.");
                    return;
                }
                java.io.File f = resolveSetFile(name);
                if (f == null) {
                    JOptionPane.showMessageDialog(
                            reviewFrame,
                            "File not found:\n• Cards/" + name + ".csv\n• ../Cards/" + name + ".csv"
                    );
                    return;
                }

                FlashcardSet loaded = new FlashcardSet(f.getPath());
                if (loaded.getSize() <= 0) {
                    JOptionPane.showMessageDialog(reviewFrame, "That file has no cards.");
                    return;
                }

                filename = name;
                set = loaded;

                textBoxPanel.remove(textBoxLabel);
                textBoxPanel.remove(textField);
                textBoxPanel.remove(submitButton);
                reviewFrame.revalidate();
                reviewFrame.repaint();

                cardButton.setEnabled(true);
            }
        });

        cardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (set == null || set.questions.isEmpty()) {
                    JOptionPane.showMessageDialog(reviewFrame, "Load a flashcard set first.");
                    return;
                }
                JFrame reviewWindow = new JFrame("Review: " + filename);
                reviewWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                reviewWindow.setLayout(new BorderLayout());
                reviewWindow.add(reviewFlashcardUI(), BorderLayout.CENTER);
                reviewWindow.pack();
                reviewWindow.setLocationRelativeTo(null);
                reviewWindow.setVisible(true);
            }
        });
    }

    public void folderGUI()
    {
        JFrame folderFrame = new JFrame("Create a New Folder");

        folderFrame.setResizable(true);

        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        folderFrame.add(mainPanel);

        JPanel textBoxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel textBoxLabel = new JLabel("Enter a New Folder: ");
        JTextField textField = new JTextField(20);
        textField.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));

        AnimatedButton submitButton = new AnimatedButton("Select");
        submitButton.setPreferredSize(new Dimension(120, 40));

        textBoxPanel.add(textBoxLabel);
        textBoxPanel.add(textField);
        textBoxPanel.add(submitButton);
        folderFrame.add(textBoxPanel, BorderLayout.CENTER);

        folderFrame.pack();
        folderFrame.setLocationRelativeTo(null);
        folderFrame.setVisible(true);

        submitButton.addActionListener(e -> {
            String folderName = textField.getText().trim();

            if(folderName.isEmpty()){
                JOptionPane.showMessageDialog(folderFrame, "Please enter a folder name: ");
                return;
            }

            File newFolder = new File("Cards/" + folderName + ".csv");

            if(newFolder.exists())
            {
                JOptionPane.showMessageDialog(folderFrame, "This folder already exists! Please choose a new name: ");
                return;
            }

            try
            {
                newFolder.createNewFile();
                JOptionPane.showMessageDialog(folderFrame, "Folder Created Successfully!");
            }

            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(folderFrame, "Unable to create folder");
                ex.printStackTrace();
                return;
            }
        });

    }

    public void createModifyGUI() {
        JFrame createFrame = new JFrame("Edit and Modify");

        createFrame.setResizable(true);

        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        createFrame.add(mainPanel);

        JPanel textBoxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        textBoxPanel.setOpaque(false);
        JLabel textBoxLabel = new JLabel("Enter a Flashcard Set: ");
        textBoxLabel.setForeground(Color.WHITE);
        textBoxLabel.setFont(new Font("Helvetica", Font.ITALIC, 18));
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Helvetica", Font.PLAIN, 16));
        textField.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));

        AnimatedButton submitButton = new AnimatedButton("Select");
        submitButton.setPreferredSize(new Dimension(120, 40));

        textBoxPanel.add(textBoxLabel);
        textBoxPanel.add(textField);
        textBoxPanel.add(submitButton);

        JPanel flashcardPanel = createFlashcardUI();
        flashcardPanel.setOpaque(false);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(textBoxPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(flashcardPanel);
        mainPanel.add(Box.createVerticalGlue());

        submitButton.addActionListener(e -> {
            String filename = textField.getText().trim();
            if (!filename.isEmpty()) {
                java.io.File f = resolveSetFile(filename);
                if (f == null) {
                    JOptionPane.showMessageDialog(
                            createFrame,
                            "File not found:\n• Cards/" + filename + ".csv\n• ../Cards/" + filename + ".csv"
                    );
                    return;
                }

                set = new FlashcardSet(dir + filename + ".csv");
                sharedCurrentIndex = 0;
                sharedShowQuestion = true;

                if (sharedCardLabel != null && sharedProgressLabel != null) {
                    if (set != null && !set.questions.isEmpty()) {
                        sharedCardLabel.setText(set.questions.get(0).getQuestion());
                        sharedProgressLabel.setText("1 / " + set.questions.size());
                    } else {
                        sharedCardLabel.setText("No cards available");
                        sharedProgressLabel.setText("0 / 0");
                    }
                }

                flashcardPanel.setVisible(true);
                createFrame.revalidate();
                createFrame.repaint();
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a filename: ");
            }
        });

        createFrame.pack();
        createFrame.setLocationRelativeTo(null);
        createFrame.setVisible(true);
    }

    public JPanel createFlashcardUI()
    {
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(600, 300));
        mainPanel.setOpaque(false);

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(255,255,255),
                        0, h, new Color(230,230,230));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 20, 20);

                g2.setColor(new Color(0,0,0,40));
                g2.fillRoundRect(4,4,w-8,h-8,20,20);

                g2.dispose();
            }
        };
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(new EmptyBorder(20,20,20,20));

        sharedProgressLabel = new JLabel("0 / 0", SwingConstants.CENTER);
        sharedProgressLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
        cardPanel.add(sharedProgressLabel, BorderLayout.NORTH);

        sharedCardLabel = new JLabel("Your Card Appears Here", SwingConstants.CENTER);
        sharedCardLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        sharedCardLabel.setForeground(Color.DARK_GRAY);
        sharedCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardPanel.add(sharedCardLabel, BorderLayout.CENTER);

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        Dimension buttonSize = new Dimension(120, 40);

        AnimatedButton addButton = new AnimatedButton("Add/Modify");
        addButton.setPreferredSize(buttonSize);
        addButton.setGradient(new Color(255, 152, 0), new Color(245, 124, 0));

        AnimatedButton removeButton = new AnimatedButton("Remove Term");
        removeButton.setPreferredSize(buttonSize);
        removeButton.setGradient(new Color(244, 67, 54), new Color(211, 47, 47));

        AnimatedButton prevButton = new AnimatedButton("Previous");
        prevButton.setPreferredSize(buttonSize);
        prevButton.setGradient(new Color(255, 235, 59), new Color(251, 192, 45));

        AnimatedButton flipButton = new AnimatedButton("Flip");
        flipButton.setPreferredSize(buttonSize);
        flipButton.setGradient(new Color(255, 64, 129), new Color(197, 17, 98));

        AnimatedButton nextButton = new AnimatedButton("Next");
        nextButton.setPreferredSize(buttonSize);
        nextButton.setGradient(new Color(139, 195, 74), new Color(104, 159, 56));
        

        buttonPanel.add(addButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(flipButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(removeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        final int[] currentIndex = {0};
        final boolean[] showQuestion = {true};

        Runnable updateCard = () -> {
            if (set != null && !set.questions.isEmpty()) {
                sharedCurrentIndex = currentIndex[0];
                sharedShowQuestion = showQuestion[0];

                QA current = set.questions.get(sharedCurrentIndex);
                sharedCardLabel.setText(sharedShowQuestion ? current.getQuestion() : current.getAnswer());
                sharedProgressLabel.setText((sharedCurrentIndex+1) + " / " + set.questions.size());
            } else {
                sharedProgressLabel.setText("0 / 0");
                sharedCardLabel.setText("No cards available");

            }
        };

        sharedUpdateCard = updateCard;
        updateCard.run();

        addButton.addActionListener(e -> {
            if (set != null ) {

                String question = JOptionPane.showInputDialog(mainPanel,"Enter Question/Term:", JOptionPane.PLAIN_MESSAGE);

                if(!set.isTerm((question)))
                {
                    String answer = JOptionPane.showInputDialog(mainPanel, "Enter Answer/Definition:", JOptionPane.PLAIN_MESSAGE);
                    set.add(question, answer);
                    set.saveData();
                }

                else
                {
                    String answer = JOptionPane.showInputDialog(mainPanel, "Enter Answer/Definition:", JOptionPane.PLAIN_MESSAGE);
                    set.edit(question, answer);
                    set.saveData();
                }

                currentIndex[0] = 0;
                showQuestion[0] = true;
                set.loadData();
                updateCard.run();
            }
        });

        removeButton.addActionListener(e -> {

            if(set != null && !set.questions.isEmpty()) {

                set.remove(set.getTerm(currentIndex[0]));

                currentIndex[0] = 0;
                showQuestion[0] = true;

                set.saveData();
                updateCard.run();
                set.loadData();
                updateCard.run();
            }
        });

        prevButton.addActionListener(e -> {
            if (set != null && !set.questions.isEmpty()) {
                currentIndex[0] = (currentIndex[0]-1 + set.questions.size())%set.questions.size();
                showQuestion[0] = true;
                updateCard.run();
            }
        });

        nextButton.addActionListener(e -> {
            if (set != null && !set.questions.isEmpty()) {
                currentIndex[0] = (currentIndex[0]+1)%set.questions.size();
                showQuestion[0] = true;
                updateCard.run();
            }
        });

        flipButton.addActionListener(e -> {
            if (set != null && !set.questions.isEmpty()) {
                showQuestion[0] = !showQuestion[0];
                updateCard.run();
            }
        });

        InputMap im = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = mainPanel.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0),"prev");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0),"next");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0),"flip");

        am.put("prev", new AbstractAction() {@Override public void actionPerformed(ActionEvent e){ if(set!=null&&!set.questions.isEmpty()){currentIndex[0]=(currentIndex[0]-1+set.questions.size())%set.questions.size(); showQuestion[0]=true; updateCard.run();}}});
        am.put("next", new AbstractAction() {@Override public void actionPerformed(ActionEvent e){ if(set!=null&&!set.questions.isEmpty()){currentIndex[0]=(currentIndex[0]+1)%set.questions.size(); showQuestion[0]=true; updateCard.run();}}});
        am.put("flip", new AbstractAction() {@Override public void actionPerformed(ActionEvent e){ if(set!=null&&!set.questions.isEmpty()){showQuestion[0]=!showQuestion[0]; updateCard.run();}}});

        return mainPanel;

    }

    public JPanel reviewFlashcardUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(600, 300));
        mainPanel.setOpaque(false);

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(255,255,255),
                        0, h, new Color(230,230,230));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 20, 20);

                g2.setColor(new Color(0,0,0,40));
                g2.fillRoundRect(4,4,w-8,h-8,20,20);

                g2.dispose();
            }
        };
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(new EmptyBorder(20,20,20,20));

        sharedProgressLabel = new JLabel("0 / 0", SwingConstants.CENTER);
        sharedProgressLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
        cardPanel.add(sharedProgressLabel, BorderLayout.NORTH);

        sharedCardLabel = new JLabel("Your Card Appears Here", SwingConstants.CENTER);
        sharedCardLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        sharedCardLabel.setForeground(Color.DARK_GRAY);
        sharedCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardPanel.add(sharedCardLabel, BorderLayout.CENTER);

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        Dimension buttonSize = new Dimension(120, 40);

        AnimatedButton shuffleButton = new AnimatedButton("Shuffle");
        shuffleButton.setPreferredSize(buttonSize);
        shuffleButton.setGradient(new Color(255, 152, 0), new Color(245, 124, 0));

        AnimatedButton prevButton = new AnimatedButton("Previous");
        prevButton.setPreferredSize(buttonSize);
        prevButton.setGradient(new Color(255, 235, 59), new Color(251, 192, 45));

        AnimatedButton flipButton = new AnimatedButton("Flip");
        flipButton.setPreferredSize(buttonSize);
        flipButton.setGradient(new Color(255, 64, 129), new Color(197, 17, 98));

        AnimatedButton nextButton = new AnimatedButton("Next");
        nextButton.setPreferredSize(buttonSize);
        nextButton.setGradient(new Color(139, 195, 74), new Color(104, 159, 56));

        buttonPanel.add(shuffleButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(flipButton);
        buttonPanel.add(nextButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        final int[] currentIndex = {0};
        final boolean[] showQuestion = {true};

        Runnable updateCard = () -> {
            if (set != null && !set.questions.isEmpty()) {
                sharedCurrentIndex = currentIndex[0];
                sharedShowQuestion = showQuestion[0];

                QA current = set.questions.get(sharedCurrentIndex);
                sharedCardLabel.setText(sharedShowQuestion ? current.getQuestion() : current.getAnswer());
                sharedProgressLabel.setText((sharedCurrentIndex+1) + " / " + set.questions.size());
            } else {
                sharedProgressLabel.setText("0 / 0");
                sharedCardLabel.setText("No cards available");
            }
        };

        sharedUpdateCard = updateCard;
        updateCard.run();

        shuffleButton.addActionListener(e -> {
            if (set != null && !set.questions.isEmpty()) {
                set.shuffle();
                currentIndex[0] = 0;
                showQuestion[0] = true;
                updateCard.run();
            }
        });

        prevButton.addActionListener(e -> {
            if (set != null && !set.questions.isEmpty()) {
                currentIndex[0] = (currentIndex[0]-1 + set.questions.size())%set.questions.size();
                showQuestion[0] = true;
                updateCard.run();
            }
        });

        nextButton.addActionListener(e -> {
            if (set != null && !set.questions.isEmpty()) {
                currentIndex[0] = (currentIndex[0]+1)%set.questions.size();
                showQuestion[0] = true;
                updateCard.run();
            }
        });

        flipButton.addActionListener(e -> {
            if (set != null && !set.questions.isEmpty()) {
                showQuestion[0] = !showQuestion[0];
                updateCard.run();
            }
        });

        InputMap im = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = mainPanel.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0),"prev");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0),"next");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0),"flip");

        am.put("prev", new AbstractAction() {@Override public void actionPerformed(ActionEvent e){ if(set!=null&&!set.questions.isEmpty()){currentIndex[0]=(currentIndex[0]-1+set.questions.size())%set.questions.size(); showQuestion[0]=true; updateCard.run();}}});
        am.put("next", new AbstractAction() {@Override public void actionPerformed(ActionEvent e){ if(set!=null&&!set.questions.isEmpty()){currentIndex[0]=(currentIndex[0]+1)%set.questions.size(); showQuestion[0]=true; updateCard.run();}}});
        am.put("flip", new AbstractAction() {@Override public void actionPerformed(ActionEvent e){ if(set!=null&&!set.questions.isEmpty()){showQuestion[0]=!showQuestion[0]; updateCard.run();}}});

        return mainPanel;
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();

            Color top = new Color(0,180,255);
            Color middle = new Color(0,120,200);
            Color bottom = new Color(0,80,180);

            GradientPaint gp1 = new GradientPaint(0,0,top,0,h/2f,middle);
            GradientPaint gp2 = new GradientPaint(0,h/2f,middle,0,h,bottom);

            g2.setPaint(gp1);
            g2.fillRect(0,0,w,h/2);
            g2.setPaint(gp2);
            g2.fillRect(0,h/2,w,h);

            g2.dispose();
        }
    }

    class AnimatedButton extends JButton {

        private float scale = 1f;
        private Color gradientTop = new Color(255, 140, 0);
        private Color gradientBottom = new Color(255, 0, 102);

        private boolean hover = false;
        private Timer animationTimer;

        public AnimatedButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("Helvetica", Font.BOLD, 18));

            animationTimer = new Timer(16, e -> animate());
            animationTimer.start();

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; }
                @Override public void mouseExited(java.awt.event.MouseEvent e) { hover = false; }
                @Override public void mousePressed(java.awt.event.MouseEvent e) { scale = 0.97f; }
                @Override public void mouseReleased(java.awt.event.MouseEvent e) { scale = 1f; }
            });
        }

        private void animate() {
            float target = hover ? 1.05f : 1f;
            scale += (target - scale) * 0.1f;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int scaledW = (int)(w*scale);
            int scaledH = (int)(h*scale);
            int x = (w - scaledW)/2;
            int y = (h - scaledH)/2;

            GradientPaint gp = new GradientPaint(x,y,gradientTop.brighter(), x,y+scaledH, gradientBottom.darker());
            g2.setPaint(gp);
            g2.fillRoundRect(x,y,scaledW,scaledH,30,30);

            g2.setColor(new Color(0,0,0,40));
            g2.fillRoundRect(x,y+4,scaledW,scaledH,30,30);

            if(hover){
                g2.setColor(new Color(255,255,255,50));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(x+2,y+2,scaledW-4,scaledH-4,30,30);
            }

            super.paintComponent(g2);
            g2.dispose();
        }

        public void setGradient(Color top, Color bottom) {
            this.gradientTop = top;
            this.gradientBottom = bottom;
        }
    }

    public static void main(String[] args){
        try{
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()){
                if("Nimbus".equals(info.getName())){
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }catch(Exception ignore){}
        new GUI();
    }
}
