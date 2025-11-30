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
        // Apply Helvetica font everywhere 
        Font helvetica = new Font("Helvetica", Font.PLAIN, 18);
        UIManager.put("Label.font", helvetica);
        UIManager.put("Button.font", helvetica);
        UIManager.put("TextField.font", helvetica);
        UIManager.put("TextArea.font", helvetica);
        UIManager.put("ComboBox.font", helvetica);
        UIManager.put("Panel.font", helvetica);
        UIManager.put("TabbedPane.font", helvetica);

        JFrame frame = new JFrame("Flashcard101");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        // Gradient background for the whole home screen
        JPanel background = new GradientPanel();
        background.setLayout(new BorderLayout());
        frame.setContentPane(background);

        // Title (white, centered)
        JLabel title = new JLabel("Flashcard101: An Interactive Flashcard Application", SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(30, 0, 20, 0));
        background.add(title, BorderLayout.NORTH);

        // Center: two big animated buttons
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 40, 40));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(40, 80, 40, 80));

        AnimatedButton reviewButton = new AnimatedButton("Review Flashcards");
        reviewButton.setPreferredSize(new Dimension(300, 200));
        reviewButton.setGradient(new Color(0, 200, 83), new Color(0, 150, 70)); 

        AnimatedButton createButton = new AnimatedButton("Create and Modify");
        createButton.setPreferredSize(new Dimension(300, 200));
        createButton.setGradient(new Color(171, 71, 188), new Color(123, 31, 162));

        centerPanel.add(reviewButton);
        centerPanel.add(createButton);
        background.add(centerPanel, BorderLayout.CENTER);

        // Footer credits (white, bottom)
        JLabel credits = new JLabel(
                "Created by Mateo Alado, Amreen Ahmed, Sanad Atia, Ohenewaa Ampem Darko, and Twinkle Johnson - Fall 2025",
                SwingConstants.CENTER);
        credits.setFont(new Font("Helvetica", Font.PLAIN, 16));
        credits.setForeground(Color.WHITE);
        credits.setBorder(new EmptyBorder(10, 0, 20, 0));
        background.add(credits, BorderLayout.SOUTH);

        // Button actions: same behavior as before
        reviewButton.addActionListener(e -> {
            frame.dispose();
            reviewGUI();
        });

        createButton.addActionListener(e -> {
            frame.dispose();
            createModifyGUI();
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private java.io.File resolveSetFile(String baseName) {
        java.io.File f1 = new java.io.File("Cards/" + baseName + ".csv");   // run from project root
        if (f1.exists()) {
            return f1;
        }
        java.io.File f2 = new java.io.File("../Cards/" + baseName + ".csv"); // run from src/
        if (f2.exists()) {
            return f2;
        }
        return null;
    }

    public void reviewGUI() {
        JFrame reviewFrame = new JFrame("Review Flashcards");
        reviewFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        reviewFrame.setLayout(new BorderLayout());
        reviewFrame.setResizable(true);

        //top: load a set
        JPanel textBoxPanel = new JPanel();
        JLabel textBoxLabel = new JLabel("Enter a Flashcard Set: ");
        JTextField textField = new JTextField(20);
        JButton submitButton = new JButton("Select");
        textBoxPanel.add(textBoxLabel);
        textBoxPanel.add(textField);
        textBoxPanel.add(submitButton);
        reviewFrame.add(textBoxPanel, BorderLayout.NORTH);

        //center: Start button
        JPanel reviewPanel = new JPanel();
        JButton cardButton = new JButton("Start");
        cardButton.setFont(new Font("Helvetica", Font.PLAIN, 24));
        cardButton.setEnabled(false);              // disabled until a valid set loads
        reviewPanel.add(cardButton);
        reviewFrame.add(reviewPanel, BorderLayout.CENTER);

        reviewFrame.pack();
        reviewFrame.setLocationRelativeTo(null);
        reviewFrame.setVisible(true);

        // Try to resolve file from project root or from src/ (see helper below)
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = textField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(reviewFrame, "Please enter a filename.");
                    return;
                }
                java.io.File f = resolveSetFile(name);          // <— uses helper below
                System.out.println("user.dir = " + System.getProperty("user.dir"));
                if (f == null) {
                    JOptionPane.showMessageDialog(
                            reviewFrame,
                            "File not found:\n"
                            + "• Cards/" + name + ".csv\n"
                            + "• ../Cards/" + name + ".csv"
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
                System.out.println("Loaded flashcard set: " + f.getPath());

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

    public void createModifyGUI() {
        JFrame createFrame = new JFrame("Create and Modify");
        createFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createFrame.setResizable(true);

        // Use the gradient panel as the main background for this page
        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        createFrame.add(mainPanel);

        JPanel textBoxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        textBoxPanel.setOpaque(false); // allow gradient to show
        JLabel textBoxLabel = new JLabel("Enter a Flashcard Set: ");
        textBoxLabel.setForeground(Color.WHITE);
        textBoxLabel.setFont(new Font("Helvetica", Font.ITALIC, 18));
        textBoxLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        textBoxLabel.setOpaque(false);
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Helvetica", Font.PLAIN, 16));
        textField.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true)); // rounded border
        textField.setBackground(new Color(255, 255, 255, 220)); // soft white bg
        textField.setOpaque(false);

        AnimatedButton submitButton = new AnimatedButton("Select");
        submitButton.setPreferredSize(new Dimension(120, 40));

        textBoxPanel.add(textBoxLabel);
        textBoxPanel.add(textField);
        textBoxPanel.add(submitButton);

        // Retrieve the flashcard UI but make it transparent so gradient shows through
        JPanel flashcardPanel = reviewFlashcardUI();
        flashcardPanel.setOpaque(false);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(textBoxPanel);
        mainPanel.add(Box.createVerticalStrut(10)); // spacing
        mainPanel.add(flashcardPanel);
        mainPanel.add(Box.createVerticalGlue());

        submitButton.addActionListener(e -> {
            String filename = textField.getText().trim();
            if (!filename.isEmpty()) {

                File f = new File(dir + filename + ".csv");

                if(!f.exists())
                {
                   try {
                    f.createNewFile();
                   }

                   catch (Exception ex)
                   {
                    JOptionPane.showMessageDialog(null, "error creating new file");
                    return;
                   }
                }
                
                set = new FlashcardSet(dir + filename + ".csv");
                System.out.println("Loaded flashcard set: " + filename);

                // Reset index and flip state
                sharedCurrentIndex = 0;
                sharedShowQuestion = true;

                // If UI labels available, update them directly
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

    public JPanel reviewFlashcardUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(600, 280));
        mainPanel.setOpaque(false); // default transparent so gradient can show when embedded

        // NEW: progress label at the top
        sharedProgressLabel = new JLabel("0 / 0", SwingConstants.CENTER);
        sharedProgressLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
        sharedProgressLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        mainPanel.add(sharedProgressLabel, BorderLayout.NORTH);

        sharedCardLabel = new JLabel("Your Card Appears Here", SwingConstants.CENTER);
        sharedCardLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
        sharedCardLabel.setOpaque(false);
        sharedCardLabel.setForeground(Color.WHITE); // good contrast if on gradient; safe if not because white will show
        mainPanel.add(sharedCardLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        Dimension buttonSize = new Dimension(120, 40);

        // Create animated buttons with individual color themes
        AnimatedButton shuffleButton = new AnimatedButton("Shuffle");
        shuffleButton.setPreferredSize(buttonSize);
        shuffleButton.setGradient(new Color(0, 200, 83), new Color(0, 150, 70));   // green theme

        AnimatedButton prevButton = new AnimatedButton("Previous");
        prevButton.setPreferredSize(buttonSize);
        prevButton.setGradient(new Color(0, 140, 255), new Color(0, 90, 200));     // blue theme

        AnimatedButton flipButton = new AnimatedButton("Flip");
        flipButton.setPreferredSize(buttonSize);
        flipButton.setGradient(new Color(171, 71, 188), new Color(123, 31, 162));  // purple theme

        AnimatedButton nextButton = new AnimatedButton("Next");
        nextButton.setPreferredSize(buttonSize);
        nextButton.setGradient(new Color(0, 140, 255), new Color(0, 90, 200));     // same as previous

        buttonPanel.add(shuffleButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(flipButton);
        buttonPanel.add(nextButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        final int[] currentIndex = {0};
        final boolean[] showQuestion = {true};

        // Provide updateCard that uses shared fields (so other methods can call it)
        Runnable updateCard = () -> {
            if (set != null && !set.questions.isEmpty()) {
                // sync shared index/state with local arrays
                sharedCurrentIndex = currentIndex[0];
                sharedShowQuestion = showQuestion[0];

                QA current = set.questions.get(sharedCurrentIndex);
                sharedCardLabel.setText(sharedShowQuestion ? current.getQuestion() : current.getAnswer());
                //Update the progress label
                sharedProgressLabel.setText((sharedCurrentIndex + 1) + " / " + set.questions.size());
            } else {
                sharedProgressLabel.setText("0 / 0");
                sharedCardLabel.setText("No cards available");
            }
        };

        // expose runnable so createModifyGUI can call it if needed
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
                currentIndex[0] = (currentIndex[0] - 1 + set.questions.size()) % set.questions.size();
                showQuestion[0] = true; // show question when moving
                updateCard.run();
            }
        });

        nextButton.addActionListener(e -> {
            if (set != null && !set.questions.isEmpty()) {
                currentIndex[0] = (currentIndex[0] + 1) % set.questions.size();
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

        // Keyboard shortcuts: Left=Prev, Right=Next, Space=Flip
        InputMap im = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = mainPanel.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prev");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "flip");

        am.put("prev", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (set != null && !set.questions.isEmpty()) {
                    currentIndex[0] = (currentIndex[0] - 1 + set.questions.size()) % set.questions.size();
                    showQuestion[0] = true;
                    updateCard.run();
                }
            }
        });
        am.put("next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (set != null && !set.questions.isEmpty()) {
                    currentIndex[0] = (currentIndex[0] + 1) % set.questions.size();
                    showQuestion[0] = true;
                    updateCard.run();
                }
            }
        });
        am.put("flip", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (set != null && !set.questions.isEmpty()) {
                    showQuestion[0] = !showQuestion[0];
                    updateCard.run();
                }
            }
        });
        return mainPanel;
    }
    // gradient: Purple → Indigo → Aqua
    private static class GradientPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
    
            // Canva gradient colors
            Color top = new Color(124, 58, 237);     // #7C3AED  (purple)
            Color middle = new Color(109, 77, 224);  // #6D4DE0  (indigo blend)
            Color bottom = new Color(79, 209, 197);  // #4FD1C5  (aqua/turquoise)
    
            // First: purple → indigo (top half)
            GradientPaint gp1 = new GradientPaint(
                    0, 0, top,
                    0, h / 2f, middle
            );
    
            // Second: indigo → aqua (bottom half)
            GradientPaint gp2 = new GradientPaint(
                    0, h / 2f, middle,
                    0, h, bottom
            );
    
            // Paint both portions
            g2.setPaint(gp1);
            g2.fillRect(0, 0, w, h / 2);
    
            g2.setPaint(gp2);
            g2.fillRect(0, h / 2, w, h);
    
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
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                }
                @Override public void mousePressed(java.awt.event.MouseEvent e) {
                    scale = 0.97f; // small press effect
                }
                @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                    scale = 1f;
                }
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
    
            int scaledW = (int)(w * scale);
            int scaledH = (int)(h * scale);
            int x = (w - scaledW) / 2;
            int y = (h - scaledH) / 2;
    
            // Canva smooth glossy gradient
            GradientPaint gp = new GradientPaint(
                    x, y, gradientTop.brighter(),
                    x, y + scaledH, gradientBottom.darker()
            );
    
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, scaledW, scaledH, 30, 30); // round like Canva
    
            // DROP SHADOW (soft)
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(x, y + 4, scaledW, scaledH, 30, 30);
    
            // INNER HIGHLIGHT
            g2.setColor(new Color(255, 255, 255, 35));
            g2.fillRoundRect(x + 4, y + 4, scaledW - 8, scaledH / 2, 25, 25);
    
            super.paintComponent(g2);
            g2.dispose();
        }
    
        public void setGradient(Color top, Color bottom) {
            this.gradientTop = top;
            this.gradientBottom = bottom;
        }
    }
    

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignore) {
        }
        new GUI();
    }
}
