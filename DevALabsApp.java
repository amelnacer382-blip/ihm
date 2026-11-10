/**
 * ============================================================================
 * PROJET IHM 2026 — PLATEFORME PÉDAGOGIQUE PAR NIVEAUX
 * DÉVELOPPÉ PAR : DEVA LABS
 * ARCHITECTURE  : Java Swing (JFrame, CardLayout, JSplitPane, JTree, JTable)
 * MODULE        : Conception d'Interface Homme-Machine & Ergonomie
 * ============================================================================
 */

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DevALabsApp extends JFrame {

    // --- Identifiants des Cartes (CardLayout) ---
    public static final String CARD_AUTH      = "AUTH";
    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_WORKSHOP  = "WORKSHOP";
    public static final String CARD_EXAM      = "EXAM";
    public static final String CARD_GRADE     = "GRADE";
    public static final String CARD_TEACHER   = "TEACHER";

    // --- Palette de Couleurs Modernes DEVA LABS ---
    public static final Color COLOR_BG_DARK    = new Color(15, 23, 42);     // Slate 900
    public static final Color COLOR_BG_SURFACE = new Color(248, 250, 252);  // Slate 50
    public static final Color COLOR_CARD_BG    = Color.WHITE;
    public static final Color COLOR_PRIMARY    = new Color(37, 99, 235);    // Blue 600
    public static final Color COLOR_CYAN       = new Color(6, 182, 212);    // Cyan 500
    public static final Color COLOR_GREEN      = new Color(16, 185, 129);   // Emerald 500
    public static final Color COLOR_AMBER      = new Color(245, 158, 11);   // Amber 500
    public static final Color COLOR_RED        = new Color(239, 68, 68);    // Red 500
    public static final Color COLOR_TEXT_MAIN  = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    public static final Color COLOR_BORDER     = new Color(226, 232, 240);

    // --- État de l'Application (Modèle MVC) ---
    private int teacherThreshold = 70; // Seuil de passage en % (RG2/RG3)
    private int maxAttempts = 2;       // Règle RG5
    private int currentAttempts = 0;
    private boolean level2Passed = false;
    private boolean level3Unlocked = false;

    // --- Composants Clés de l'Interface ---
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private JLabel statusRouteLabel;
    private JLabel statusActiveLevelLabel;
    private JProgressBar globalProgressBar;
    private JLabel progressPercentLabel;

    // Dashboard widgets
    private JPanel cardL2Panel;
    private JLabel chipL2;
    private JPanel cardL3Panel;
    private JLabel chipL3;
    private JButton btnL3;
    private JLabel lblSeuilL2;
    private JLabel lblAttemptsL2;

    // Workshop widgets
    private JEditorPane courseEditorPane;
    private JTextArea codeEditorArea;
    private JPanel previewStage;

    // Exam widgets
    private javax.swing.Timer examTimer;
    private int examTimeSeconds = 15 * 60;
    private JLabel timerDisplayLabel;
    private JLabel examQuestionStatement;
    private JLabel examCounterLabel;
    private JLabel examAttemptsLabel;
    private JRadioButton[] choiceRadios;
    private ButtonGroup choiceGroup;
    private int currentQuestionIndex = 0;
    private Integer[] userAnswers = new Integer[3];

    // Grade widgets
    private JLabel gradeBadgeLabel;
    private JLabel gradeTitleLabel;
    private JLabel gradeSubtitleLabel;
    private JPanel decisionBoxPanel;
    private JLabel decisionTitleLabel;
    private JLabel decisionDescLabel;
    private JButton btnProceedL3;

    // Teacher widgets
    private JLabel thresholdValueLabel;
    private JSlider thresholdSlider;
    private DefaultTableModel studentTableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    // Questions de test
    private final String[][] questions = {
        {
            "Quelle propriété CSS permet de modifier l'axe principal d'un conteneur Flexbox pour empiler les enfants verticalement ?",
            "flex-wrap: wrap;",
            "flex-direction: column;",
            "align-items: vertical;",
            "display: block-flex;",
            "1" // index bonne réponse
        },
        {
            "Quelle valeur de 'justify-content' centre les enfants le long de l'axe principal ?",
            "center",
            "space-between",
            "flex-start",
            "baseline",
            "0"
        },
        {
            "Quelle propriété est utilisée pour contrôler l'alignement le long de l'axe secondaire (cross axis) ?",
            "align-items",
            "justify-items",
            "cross-direction",
            "flex-cross",
            "0"
        }
    };

    public DevALabsApp() {
        super("DEVA LABS Studio — Plateforme Pédagogique par Niveaux (Module IHM 2026)");
        initWindow();
        initMenuBar();
        initToolBar();
        initContentCards();
        initStatusBar();
        showCard(CARD_AUTH, "Authentification");
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 800);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        // Appliquer un style natif propre
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    // ========================================================================
    // 1. JMenuBar Swing Réel
    // ========================================================================
    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(COLOR_BG_SURFACE);
        menuBar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Menu Fichier
        JMenu menuFichier = new JMenu("Fichier");
        JMenuItem miConnect = new JMenuItem("Nouvelle session...");
        miConnect.addActionListener(e -> showCard(CARD_AUTH, "Authentification"));
        JMenuItem miExport = new JMenuItem("Exporter le relevé (PDF/CSV)...");
        miExport.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "Export du relevé de notes vers SQLite / CSV effectué avec succès.", 
            "Export Données • DEVA LABS", JOptionPane.INFORMATION_MESSAGE));
        JMenuItem miExit = new JMenuItem("Quitter l'application");
        miExit.addActionListener(e -> System.exit(0));
        menuFichier.add(miConnect);
        menuFichier.add(miExport);
        menuFichier.addSeparator();
        menuFichier.add(miExit);

        // Menu Édition
        JMenu menuEdition = new JMenu("Édition");
        JMenuItem miReset = new JMenuItem("Réinitialiser les tentatives (Test)");
        miReset.addActionListener(e -> {
            currentAttempts = 0;
            level2Passed = false;
            level3Unlocked = false;
            lblAttemptsL2.setText("0/" + maxAttempts);
            JOptionPane.showMessageDialog(this, "Tentatives réinitialisées à 0 pour démonstration.");
        });
        menuEdition.add(miReset);

        // Menu Parcours
        JMenu menuParcours = new JMenu("Parcours");
        JMenuItem miDash = new JMenuItem("Hub des Niveaux (Dashboard)");
        miDash.addActionListener(e -> showCard(CARD_DASHBOARD, "Tableau de bord des Niveaux"));
        JMenuItem miWork = new JMenuItem("Atelier CSS Flexbox (Niveau 2)");
        miWork.addActionListener(e -> showCard(CARD_WORKSHOP, "Cours & Atelier Pratique"));
        menuParcours.add(miDash);
        menuParcours.add(miWork);

        // Menu Pédagogie
        JMenu menuPedagogie = new JMenu("Pédagogie");
        JMenuItem miTeacher = new JMenuItem("Espace Enseignant & Règles");
        miTeacher.addActionListener(e -> showCard(CARD_TEACHER, "Espace Enseignant"));
        menuPedagogie.add(miTeacher);

        // Menu Aide & Ergonomie
        JMenu menuAide = new JMenu("Aide & Ergonomie");
        JMenuItem miErgo = new JMenuItem("Fiche Ergonomique (Bastien & Scapin)");
        miErgo.addActionListener(e -> openErgonomicsDialog());
        JMenuItem miAbout = new JMenuItem("À propos de DEVA LABS Studio");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "DEVA LABS Studio v2.4\nPlateforme d'Apprentissage des Langages du Web par Niveaux\nConception IHM 2026 • Java Swing FlatLaf Architecture",
            "À propos • DEVA LABS", JOptionPane.INFORMATION_MESSAGE));
        menuAide.add(miErgo);
        menuAide.add(miAbout);

        menuBar.add(menuFichier);
        menuBar.add(menuEdition);
        menuBar.add(menuParcours);
        menuBar.add(menuPedagogie);
        menuBar.add(menuAide);

        setJMenuBar(menuBar);
    }

    // ========================================================================
    // 2. JToolBar de Navigation & Sélecteur d'Écrans
    // ========================================================================
    private void initToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(new EmptyBorder(8, 14, 8, 14));
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        // Boutons de navigation
        JButton btnNav1 = createNavButton("1. Connexion", CARD_AUTH, "Authentification");
        JButton btnNav2 = createNavButton("2. Tableau de bord", CARD_DASHBOARD, "Tableau de bord des Niveaux");
        JButton btnNav3 = createNavButton("3. Cours & Atelier", CARD_WORKSHOP, "Cours & Atelier Pratique");
        JButton btnNav4 = createNavButton("4. Évaluation", CARD_EXAM, "Évaluation Certifiante");
        JButton btnNav5 = createNavButton("5. Relevé de note", CARD_GRADE, "Relevé de Délibération");
        JButton btnNav6 = createNavButton("6. Espace Enseignant", CARD_TEACHER, "Espace Enseignant");

        toolBar.add(btnNav1);
        toolBar.add(btnNav2);
        toolBar.add(btnNav3);
        toolBar.add(btnNav4);
        toolBar.add(btnNav5);
        toolBar.add(btnNav6);

        // Séparateur élastique vers l'étiquette utilisateur
        toolBar.add(Box.createHorizontalGlue());

        JButton btnErgoQuick = new JButton("ℹ Fiche Ergonomique (Bastien & Scapin)");
        btnErgoQuick.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnErgoQuick.setBackground(new Color(239, 246, 255));
        btnErgoQuick.setForeground(COLOR_PRIMARY);
        btnErgoQuick.setFocusPainted(false);
        btnErgoQuick.addActionListener(e -> openErgonomicsDialog());
        toolBar.add(btnErgoQuick);

        add(toolBar, BorderLayout.NORTH);
    }

    private JButton createNavButton(String label, String cardId, String routeTitle) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(COLOR_BG_SURFACE);
        btn.setForeground(COLOR_TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showCard(cardId, routeTitle));
        return btn;
    }

    // ========================================================================
    // 3. CardLayout — Les 6 Écrans
    // ========================================================================
    private void initContentCards() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(COLOR_BG_SURFACE);

        mainContainer.add(buildAuthCard(), CARD_AUTH);
        mainContainer.add(buildDashboardCard(), CARD_DASHBOARD);
        mainContainer.add(buildWorkshopCard(), CARD_WORKSHOP);
        mainContainer.add(buildExamCard(), CARD_EXAM);
        mainContainer.add(buildGradeCard(), CARD_GRADE);
        mainContainer.add(buildTeacherCard(), CARD_TEACHER);

        add(mainContainer, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------------
    // ÉCRAN 1 : Authentification & Choix de Rôle
    // ------------------------------------------------------------------------
    private JPanel buildAuthCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 245, 249));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(32, 36, 32, 36)
        ));
        card.setPreferredSize(new Dimension(420, 430));

        JLabel title = new JLabel("DEVA LABS Studio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Portail d'Apprentissage des Langages du Web");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Commutateur de Rôles
        JPanel rolePanel = new JPanel(new GridLayout(1, 3, 6, 0));
        rolePanel.setBackground(Color.WHITE);
        rolePanel.setBorder(new EmptyBorder(18, 0, 18, 0));

        JTextField tfLogin = new JTextField("yacine.benali@univ.dz");
        tfLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton rBtn1 = new JButton("Apprenant");
        JButton rBtn2 = new JButton("Enseignant");
        JButton rBtn3 = new JButton("Admin");

        rBtn1.setBackground(COLOR_PRIMARY);
        rBtn1.setForeground(Color.WHITE);
        rBtn2.setBackground(COLOR_BG_SURFACE);
        rBtn3.setBackground(COLOR_BG_SURFACE);

        rBtn1.addActionListener(e -> {
            tfLogin.setText("yacine.benali@univ.dz");
            rBtn1.setBackground(COLOR_PRIMARY); rBtn1.setForeground(Color.WHITE);
            rBtn2.setBackground(COLOR_BG_SURFACE); rBtn2.setForeground(COLOR_TEXT_MAIN);
            rBtn3.setBackground(COLOR_BG_SURFACE); rBtn3.setForeground(COLOR_TEXT_MAIN);
        });
        rBtn2.addActionListener(e -> {
            tfLogin.setText("prof.meziani@univ.dz");
            rBtn2.setBackground(COLOR_PRIMARY); rBtn2.setForeground(Color.WHITE);
            rBtn1.setBackground(COLOR_BG_SURFACE); rBtn1.setForeground(COLOR_TEXT_MAIN);
            rBtn3.setBackground(COLOR_BG_SURFACE); rBtn3.setForeground(COLOR_TEXT_MAIN);
        });
        rBtn3.addActionListener(e -> {
            tfLogin.setText("admin.system@univ.dz");
            rBtn3.setBackground(COLOR_PRIMARY); rBtn3.setForeground(Color.WHITE);
            rBtn1.setBackground(COLOR_BG_SURFACE); rBtn1.setForeground(COLOR_TEXT_MAIN);
            rBtn2.setBackground(COLOR_BG_SURFACE); rBtn2.setForeground(COLOR_TEXT_MAIN);
        });

        rolePanel.add(rBtn1);
        rolePanel.add(rBtn2);
        rolePanel.add(rBtn3);

        JLabel lblLogin = new JLabel("Identifiant académique :");
        lblLogin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPass = new JLabel("Mot de passe :");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField pfPass = new JPasswordField("password123");

        JButton btnLogin = new JButton("Ouvrir ma session ➔");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(COLOR_PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Session ouverte avec succès pour : " + tfLogin.getText(),
                "Connexion Réussie", JOptionPane.INFORMATION_MESSAGE);
            showCard(CARD_DASHBOARD, "Tableau de bord des Niveaux");
        });

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(10));
        card.add(rolePanel);
        card.add(lblLogin);
        card.add(Box.createVerticalStrut(4));
        card.add(tfLogin);
        card.add(Box.createVerticalStrut(12));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(4));
        card.add(pfPass);
        card.add(Box.createVerticalStrut(20));
        card.add(btnLogin);

        panel.add(card);
        return panel;
    }

    // ------------------------------------------------------------------------
    // ÉCRAN 2 : Tableau de Bord (Hub des Niveaux)
    // ------------------------------------------------------------------------
    private JPanel buildDashboardCard() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(COLOR_BG_SURFACE);
        root.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Bannière Hero
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(COLOR_PRIMARY);
        banner.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel bannerTitle = new JLabel("Cursus de Programmation Web — DEVA LABS");
        bannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bannerTitle.setForeground(Color.WHITE);

        JLabel bannerDesc = new JLabel("Progression séquentielle conditionnée par validation automatique (Règles RG1 & RG3)");
        bannerDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bannerDesc.setForeground(new Color(224, 242, 254));

        JPanel bannerText = new JPanel(new GridLayout(2, 1, 0, 4));
        bannerText.setOpaque(false);
        bannerText.add(bannerTitle);
        bannerText.add(bannerDesc);

        JPanel progBox = new JPanel(new GridLayout(2, 1, 0, 6));
        progBox.setOpaque(false);
        progBox.setPreferredSize(new Dimension(280, 50));

        progressPercentLabel = new JLabel("33% (1/3 validé)", SwingConstants.RIGHT);
        progressPercentLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        progressPercentLabel.setForeground(new Color(110, 231, 183));

        globalProgressBar = new JProgressBar(0, 100);
        globalProgressBar.setValue(33);
        globalProgressBar.setForeground(COLOR_GREEN);
        globalProgressBar.setBackground(new Color(30, 58, 138));

        progBox.add(progressPercentLabel);
        progBox.add(globalProgressBar);

        banner.add(bannerText, BorderLayout.CENTER);
        banner.add(progBox, BorderLayout.EAST);
        root.add(banner, BorderLayout.NORTH);

        // Matrice des 3 Niveaux (GridLayout 1x3)
        JPanel matrix = new JPanel(new GridLayout(1, 3, 18, 0));
        matrix.setOpaque(false);

        // NIVEAU 1 : HTML (Validé)
        JPanel cardL1 = createLevelCard("NIVEAU 01", "✓ Validé", COLOR_GREEN,
            "HTML — Structure & Sémantique",
            "Hiérarchie des documents, formulaires web, tableaux et balises sémantiques.",
            "Score : 85%", "Statut : Acquis", "Consulter le support", e -> showCard(CARD_WORKSHOP, "Cours & Atelier"));
        
        // NIVEAU 2 : CSS (En cours)
        cardL2Panel = new JPanel(new BorderLayout(0, 12));
        cardL2Panel.setBackground(COLOR_CARD_BG);
        cardL2Panel.setBorder(new CompoundBorder(
            new LineBorder(COLOR_PRIMARY, 2, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel topL2 = new JPanel(new BorderLayout());
        topL2.setOpaque(false);
        chipL2 = new JLabel("● En cours ");
        chipL2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chipL2.setForeground(COLOR_PRIMARY);
        JLabel numL2 = new JLabel("NIVEAU 02");
        numL2.setFont(new Font("Consolas", Font.BOLD, 11));
        numL2.setForeground(COLOR_TEXT_MUTED);
        topL2.add(chipL2, BorderLayout.WEST);
        topL2.add(numL2, BorderLayout.EAST);

        JLabel titleL2 = new JLabel("<html><b>CSS — Flexbox & Responsive</b></html>");
        titleL2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel descL2 = new JLabel("<html><font color='#64748b'>Modèle de boîte, disposition flexible unidimensionnelle et règles d'alignement adaptatif.</font></html>");

        JPanel metricsL2 = new JPanel(new GridLayout(2, 1, 0, 4));
        metricsL2.setBackground(COLOR_BG_SURFACE);
        metricsL2.setBorder(new EmptyBorder(8, 10, 8, 10));
        lblSeuilL2 = new JLabel("Seuil requis : " + teacherThreshold + "%");
        lblAttemptsL2 = new JLabel("Tentatives : " + currentAttempts + "/" + maxAttempts);
        metricsL2.add(lblSeuilL2);
        metricsL2.add(lblAttemptsL2);

        JPanel btnBoxL2 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBoxL2.setOpaque(false);
        JButton btnOpenL2 = new JButton("Ouvrir l'atelier");
        btnOpenL2.addActionListener(e -> showCard(CARD_WORKSHOP, "Cours & Atelier"));
        JButton btnTestL2 = new JButton("Passer le test");
        btnTestL2.setBackground(COLOR_PRIMARY);
        btnTestL2.setForeground(Color.WHITE);
        btnTestL2.addActionListener(e -> startExam());
        btnBoxL2.add(btnOpenL2);
        btnBoxL2.add(btnTestL2);

        JPanel centerL2 = new JPanel(new GridLayout(3, 1, 0, 8));
        centerL2.setOpaque(false);
        centerL2.add(titleL2);
        centerL2.add(descL2);
        centerL2.add(metricsL2);

        cardL2Panel.add(topL2, BorderLayout.NORTH);
        cardL2Panel.add(centerL2, BorderLayout.CENTER);
        cardL2Panel.add(btnBoxL2, BorderLayout.SOUTH);

        // NIVEAU 3 : JavaScript (Verrouillé)
        cardL3Panel = new JPanel(new BorderLayout(0, 12));
        cardL3Panel.setBackground(new Color(248, 250, 252));
        cardL3Panel.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 2, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel topL3 = new JPanel(new BorderLayout());
        topL3.setOpaque(false);
        chipL3 = new JLabel("🔒 Verrouillé");
        chipL3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chipL3.setForeground(COLOR_TEXT_MUTED);
        JLabel numL3 = new JLabel("NIVEAU 03");
        numL3.setFont(new Font("Consolas", Font.BOLD, 11));
        numL3.setForeground(COLOR_TEXT_MUTED);
        topL3.add(chipL3, BorderLayout.WEST);
        topL3.add(numL3, BorderLayout.EAST);

        JLabel titleL3 = new JLabel("<html><b>JavaScript — DOM & Événements</b></html>");
        titleL3.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel descL3 = new JLabel("<html><font color='#64748b'>Manipulation des nœuds HTML, écouteurs d'événements et requêtes asynchrones.</font></html>");

        JPanel metricsL3 = new JPanel(new GridLayout(2, 1, 0, 4));
        metricsL3.setBackground(COLOR_BG_SURFACE);
        metricsL3.setBorder(new EmptyBorder(8, 10, 8, 10));
        metricsL3.add(new JLabel("Condition : Valider Niv. 2"));
        metricsL3.add(new JLabel("Règle système : RG3"));

        btnL3 = new JButton("Verrouillé (RG3)");
        btnL3.setEnabled(false);
        btnL3.addActionListener(e -> {
            if (level3Unlocked) {
                JOptionPane.showMessageDialog(this, "Bienvenue dans le Niveau 3 : JavaScript !", "Niveau 3 Ouvert", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Niveau 3 Verrouillé : validez d'abord le test du Niveau 2 avec succès (RG3).", "Accès Refusé", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel centerL3 = new JPanel(new GridLayout(3, 1, 0, 8));
        centerL3.setOpaque(false);
        centerL3.add(titleL3);
        centerL3.add(descL3);
        centerL3.add(metricsL3);

        cardL3Panel.add(topL3, BorderLayout.NORTH);
        cardL3Panel.add(centerL3, BorderLayout.CENTER);
        cardL3Panel.add(btnL3, BorderLayout.SOUTH);

        matrix.add(cardL1);
        matrix.add(cardL2Panel);
        matrix.add(cardL3Panel);

        root.add(matrix, BorderLayout.CENTER);
        return root;
    }

    private JPanel createLevelCard(String num, String stateText, Color stateColor,
                                   String title, String desc, String met1, String met2,
                                   String btnText, ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(stateColor, 2, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel chip = new JLabel(stateText);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chip.setForeground(stateColor);
        JLabel numLbl = new JLabel(num);
        numLbl.setFont(new Font("Consolas", Font.BOLD, 11));
        numLbl.setForeground(COLOR_TEXT_MUTED);
        top.add(chip, BorderLayout.WEST);
        top.add(numLbl, BorderLayout.EAST);

        JLabel titleLbl = new JLabel("<html><b>" + title + "</b></html>");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel descLbl = new JLabel("<html><font color='#64748b'>" + desc + "</font></html>");

        JPanel metrics = new JPanel(new GridLayout(2, 1, 0, 4));
        metrics.setBackground(COLOR_BG_SURFACE);
        metrics.setBorder(new EmptyBorder(8, 10, 8, 10));
        metrics.add(new JLabel(met1));
        metrics.add(new JLabel(met2));

        JButton btn = new JButton(btnText);
        btn.addActionListener(action);

        JPanel center = new JPanel(new GridLayout(3, 1, 0, 8));
        center.setOpaque(false);
        center.add(titleLbl);
        center.add(descLbl);
        center.add(metrics);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    // ------------------------------------------------------------------------
    // ÉCRAN 3 : Cours & Atelier Pratique (JSplitPane + JTree + Rendu Live)
    // ------------------------------------------------------------------------
    private JPanel buildWorkshopCard() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG_SURFACE);

        // JTree (Sommaire)
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Niveau 2 : CSS Flexbox");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("01_Introduction.css");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("02_Selecteurs.css");
        DefaultMutableTreeNode c3 = new DefaultMutableTreeNode("03_Flexbox_Align.css");
        top.add(c1); top.add(c2); top.add(c3);

        DefaultMutableTreeNode evalNode = new DefaultMutableTreeNode("Test_Validation.eval");
        top.add(evalNode);

        JTree tree = new JTree(top);
        tree.setBorder(new EmptyBorder(12, 12, 12, 12));
        tree.setBackground(Color.WHITE);
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selected == null) return;
            String name = selected.getUserObject().toString();
            if (name.contains("01")) {
                loadLesson(1);
            } else if (name.contains("02")) {
                loadLesson(2);
            } else if (name.contains("03")) {
                loadLesson(3);
            } else if (name.contains("eval")) {
                startExam();
            }
        });

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setPreferredSize(new Dimension(220, 0));

        // Document de cours
        courseEditorPane = new JEditorPane();
        courseEditorPane.setContentType("text/html");
        courseEditorPane.setEditable(false);
        courseEditorPane.setBorder(new EmptyBorder(16, 20, 16, 20));
        loadLesson(3);

        JScrollPane courseScroll = new JScrollPane(courseEditorPane);

        // Éditeur de code + Zone d'aperçu live
        JPanel rightWorkbench = new JPanel(new BorderLayout(0, 10));
        rightWorkbench.setBackground(Color.WHITE);
        rightWorkbench.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel edHead = new JLabel("style.css • RSyntaxTextArea (Simulation Live)");
        edHead.setFont(new Font("Consolas", Font.BOLD, 12));
        edHead.setForeground(COLOR_PRIMARY);

        codeEditorArea = new JTextArea();
        codeEditorArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        codeEditorArea.setText(".conteneur {\n  display: flex;\n  justify-content: space-between;\n  align-items: center;\n}");
        codeEditorArea.setBackground(new Color(15, 23, 42));
        codeEditorArea.setForeground(new Color(74, 222, 128));
        codeEditorArea.setCaretColor(Color.WHITE);
        codeEditorArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Zone de rendu graphique direct
        JPanel renderBox = new JPanel(new BorderLayout());
        renderBox.setBorder(new TitledBorder("Aperçu du rendu graphique embarqué"));
        renderBox.setPreferredSize(new Dimension(0, 130));

        previewStage = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 20));
        previewStage.setBackground(new Color(241, 245, 249));

        JButton bA = new JButton("Bloc A"); bA.setBackground(COLOR_PRIMARY); bA.setForeground(Color.WHITE);
        JButton bB = new JButton("Bloc B"); bB.setBackground(COLOR_CYAN);    bB.setForeground(Color.WHITE);
        JButton bC = new JButton("Bloc C"); bC.setBackground(COLOR_GREEN);   bC.setForeground(Color.WHITE);

        previewStage.add(bA); previewStage.add(bB); previewStage.add(bC);
        renderBox.add(previewStage, BorderLayout.CENTER);

        JButton btnRunCode = new JButton("▶ Exécuter le code (Appliquer au rendu)");
        btnRunCode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRunCode.setBackground(COLOR_GREEN);
        btnRunCode.setForeground(Color.WHITE);
        btnRunCode.setFocusPainted(false);
        btnRunCode.addActionListener(e -> executeCodeSimulation());

        JPanel edBottom = new JPanel(new BorderLayout());
        edBottom.setOpaque(false);
        edBottom.add(renderBox, BorderLayout.CENTER);
        edBottom.add(btnRunCode, BorderLayout.SOUTH);

        rightWorkbench.add(edHead, BorderLayout.NORTH);
        rightWorkbench.add(new JScrollPane(codeEditorArea), BorderLayout.CENTER);
        rightWorkbench.add(edBottom, BorderLayout.SOUTH);

        // JSplitPane 1 (Cours + Atelier)
        JSplitPane splitRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, courseScroll, rightWorkbench);
        splitRight.setResizeWeight(0.55);

        // JSplitPane 2 (Sommaire + Droite)
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, splitRight);
        splitMain.setResizeWeight(0.20);

        root.add(splitMain, BorderLayout.CENTER);
        return root;
    }

    private void loadLesson(int chapter) {
        if (chapter == 1) {
            courseEditorPane.setText("<h2>Chapitre 1 : Introduction CSS</h2><p>Notions de cascade et règles de style pour la mise en forme de base.</p><pre>body { margin: 0; font-family: sans-serif; }</pre>");
        } else if (chapter == 2) {
            courseEditorPane.setText("<h2>Chapitre 2 : Sélecteurs &amp; Règles</h2><p>Sélecteurs de classe, d'identifiant et pseudo-classes.</p><pre>.card { border-radius: 6px; padding: 12px; }</pre>");
        } else {
            courseEditorPane.setText("<h2>Chapitre 3 : Alignements avec Flexbox</h2>"
                + "<p>La propriété <code>justify-content</code> régit l'espacement et l'alignement des éléments le long de l'axe principal.</p>"
                + "<pre>.conteneur {\n  display: flex;\n  justify-content: space-between;\n}</pre>"
                + "<p><b>Exercice pratique :</b> Modifiez le code dans l'éditeur de droite en utilisant <code>justify-content: center;</code> ou <code>space-between;</code>, puis cliquez sur <b>'Exécuter le code'</b> pour observer le déplacement dynamique des blocs.</p>");
        }
    }

    private void executeCodeSimulation() {
        String code = codeEditorArea.getText();
        if (code.contains("center")) {
            previewStage.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 20));
        } else if (code.contains("flex-end")) {
            previewStage.setLayout(new FlowLayout(FlowLayout.RIGHT, 14, 20));
        } else if (code.contains("flex-start")) {
            previewStage.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 20));
        } else {
            previewStage.setLayout(new FlowLayout(FlowLayout.CENTER, 28, 20));
        }
        previewStage.revalidate();
        previewStage.repaint();
        JOptionPane.showMessageDialog(this, "Rendu graphique mis à jour avec succès !", "Exécution Réussie", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------------------
    // ÉCRAN 4 : Évaluation Chronométrée (RG5 & JOptionPane RG8)
    // ------------------------------------------------------------------------
    private JPanel buildExamCard() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(COLOR_BG_SURFACE);

        JPanel examBox = new JPanel(new BorderLayout(0, 16));
        examBox.setBackground(Color.WHITE);
        examBox.setPreferredSize(new Dimension(680, 480));
        examBox.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(26, 30, 26, 30)
        ));

        // En-tête de l'évaluation
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDER));

        JLabel title = new JLabel("Test Certifiant — Niveau 2 (CSS & Flexbox)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        examCounterLabel = new JLabel("Question 1 sur 3 • Seuil requis : 70%");
        examCounterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        examCounterLabel.setForeground(COLOR_TEXT_MUTED);

        JPanel titleGrp = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGrp.setOpaque(false);
        titleGrp.add(title);
        titleGrp.add(examCounterLabel);

        timerDisplayLabel = new JLabel("15:00", SwingConstants.CENTER);
        timerDisplayLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        timerDisplayLabel.setForeground(COLOR_RED);
        timerDisplayLabel.setBackground(new Color(254, 226, 226));
        timerDisplayLabel.setOpaque(true);
        timerDisplayLabel.setBorder(new EmptyBorder(6, 12, 6, 12));

        header.add(titleGrp, BorderLayout.CENTER);
        header.add(timerDisplayLabel, BorderLayout.EAST);
        examBox.add(header, BorderLayout.NORTH);

        // Corps : Question et Choix
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        examQuestionStatement = new JLabel("Énoncé de la question...");
        examQuestionStatement.setFont(new Font("Segoe UI", Font.BOLD, 14));
        examQuestionStatement.setBorder(new EmptyBorder(10, 0, 16, 0));

        choiceGroup = new ButtonGroup();
        choiceRadios = new JRadioButton[4];
        JPanel choicesPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        choicesPanel.setOpaque(false);

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            choiceRadios[i] = new JRadioButton();
            choiceRadios[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            choiceRadios[i].setBackground(COLOR_BG_SURFACE);
            choiceRadios[i].setBorder(new EmptyBorder(8, 12, 8, 12));
            choiceRadios[i].addActionListener(e -> userAnswers[currentQuestionIndex] = idx);
            choiceGroup.add(choiceRadios[i]);
            choicesPanel.add(choiceRadios[i]);
        }

        body.add(examQuestionStatement);
        body.add(choicesPanel);
        examBox.add(body, BorderLayout.CENTER);

        // Pied : Tentatives et Bouton Suivant / Soumettre
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, COLOR_BORDER));

        examAttemptsLabel = new JLabel("Tentative 1 sur 2 autorisées");
        examAttemptsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        examAttemptsLabel.setForeground(COLOR_TEXT_MUTED);

        JButton btnNext = new JButton("Question suivante ➔");
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNext.setBackground(COLOR_PRIMARY);
        btnNext.setForeground(Color.WHITE);
        btnNext.setFocusPainted(false);
        btnNext.addActionListener(e -> handleExamNextOrSubmit());

        footer.add(examAttemptsLabel, BorderLayout.WEST);
        footer.add(btnNext, BorderLayout.EAST);
        examBox.add(footer, BorderLayout.SOUTH);

        root.add(examBox);
        return root;
    }

    private void startExam() {
        if (currentAttempts >= maxAttempts && !level2Passed) {
            JOptionPane.showMessageDialog(this, "Nombre maximal de tentatives atteint (2/2) pour ce niveau.",
                "Quota Dépassé (RG5)", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentQuestionIndex = 0;
        Arrays.fill(userAnswers, null);
        loadExamQuestion(0);

        // Lancement Timer Swing
        if (examTimer != null && examTimer.isRunning()) examTimer.stop();
        examTimeSeconds = 15 * 60;
        examTimer = new javax.swing.Timer(1000, e -> {
            examTimeSeconds--;
            int m = examTimeSeconds / 60;
            int s = examTimeSeconds % 60;
            timerDisplayLabel.setText(String.format("%02d:%02d", m, s));
            if (examTimeSeconds <= 0) {
                examTimer.stop();
                submitExamAnswers();
            }
        });
        examTimer.start();

        showCard(CARD_EXAM, "Évaluation Certifiante (Niveau 2)");
    }

    private void loadExamQuestion(int qIdx) {
        String[] qData = questions[qIdx];
        examCounterLabel.setText("Question " + (qIdx + 1) + " sur " + questions.length + " • Seuil requis : " + teacherThreshold + "%");
        examQuestionStatement.setText("<html>" + qData[0] + "</html>");
        examAttemptsLabel.setText("Tentative " + (currentAttempts + 1) + " sur " + maxAttempts);

        choiceGroup.clearSelection();
        for (int i = 0; i < 4; i++) {
            choiceRadios[i].setText(qData[i + 1]);
            if (userAnswers[qIdx] != null && userAnswers[qIdx] == i) {
                choiceRadios[i].setSelected(true);
            }
        }
    }

    private void handleExamNextOrSubmit() {
        if (userAnswers[currentQuestionIndex] == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une réponse avant de continuer.",
                "Réponse Requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentQuestionIndex < questions.length - 1) {
            currentQuestionIndex++;
            loadExamQuestion(currentQuestionIndex);
        } else {
            // Boîte de Confirmation JOptionPane (Règle RG8)
            int choice = JOptionPane.showConfirmDialog(this,
                "Êtes-vous certain de vouloir soumettre vos réponses pour évaluation ?\nLe score sera calculé immédiatement et déterminera le déblocage du Niveau 3 (RG3).",
                "Confirmation de Soumission • JOptionPane",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                if (examTimer != null) examTimer.stop();
                submitExamAnswers();
            }
        }
    }

    private void submitExamAnswers() {
        currentAttempts++;
        int correctCount = 0;
        for (int i = 0; i < questions.length; i++) {
            int expected = Integer.parseInt(questions[i][5]);
            if (userAnswers[i] != null && userAnswers[i] == expected) {
                correctCount++;
            }
        }

        int scorePercent = Math.round(((float) correctCount / questions.length) * 100);

        // Décision Pédagogique (RG2, RG3, RG4)
        if (scorePercent >= teacherThreshold) {
            // SUCCÈS
            level2Passed = true;
            level3Unlocked = true;

            gradeBadgeLabel.setText(scorePercent + "%");
            gradeBadgeLabel.setForeground(COLOR_GREEN);
            gradeBadgeLabel.setBackground(new Color(220, 252, 231));

            gradeTitleLabel.setText("Niveau 2 Validé avec Succès !");
            gradeSubtitleLabel.setText("Décision pédagogique enregistrée dans SQLite (learnweb.db)");

            decisionTitleLabel.setText("Conditions de passage satisfaites (Règles RG2 & RG3) :");
            decisionDescLabel.setText("<html>Note finale de <b>" + scorePercent + "%</b> supérieure ou égale au seuil (" + teacherThreshold + "%). "
                + "Le Niveau 3 (JavaScript & DOM) a été déverrouillé automatiquement.</html>");
            btnProceedL3.setVisible(true);

            // Mise à jour Dashboard
            chipL2.setText("✓ Validé (" + scorePercent + "%)");
            chipL2.setForeground(COLOR_GREEN);
            chipL3.setText("● Débloqué");
            chipL3.setForeground(COLOR_PRIMARY);
            btnL3.setEnabled(true);
            btnL3.setText("Commencer le Niveau 3");
            btnL3.setBackground(COLOR_PRIMARY);
            btnL3.setForeground(Color.WHITE);

            globalProgressBar.setValue(66);
            progressPercentLabel.setText("66% (2/3 validés)");
            statusActiveLevelLabel.setText("• Niveau actif : 3");

            // Mise à jour table enseignant
            studentTableModel.setValueAt("Niveau 3", 0, 1);
            studentTableModel.setValueAt(currentAttempts + " / " + maxAttempts, 0, 2);
            studentTableModel.setValueAt(scorePercent + "%", 0, 3);
            studentTableModel.setValueAt("Validé", 0, 4);

        } else {
            // ÉCHEC
            gradeBadgeLabel.setText(scorePercent + "%");
            gradeBadgeLabel.setForeground(COLOR_RED);
            gradeBadgeLabel.setBackground(new Color(254, 226, 226));

            gradeTitleLabel.setText("Seuil d'Admission Non Atteint");
            gradeSubtitleLabel.setText("Le niveau courant doit être consolidé (Règle RG4)");

            decisionTitleLabel.setText("Décision système : Niveau 3 maintenu verrouillé");
            decisionDescLabel.setText("<html>La note obtenue (" + scorePercent + "%) est inférieure au seuil de " + teacherThreshold + "%. "
                + "Il vous reste <b>" + (maxAttempts - currentAttempts) + "</b> tentative(s). Veuillez réviser les ateliers.</html>");
            btnProceedL3.setVisible(false);

            lblAttemptsL2.setText("Tentatives : " + currentAttempts + "/" + maxAttempts);

            // Table enseignant
            studentTableModel.setValueAt(currentAttempts + " / " + maxAttempts, 0, 2);
            studentTableModel.setValueAt(scorePercent + "%", 0, 3);
            studentTableModel.setValueAt("Non validé", 0, 4);
        }

        showCard(CARD_GRADE, "Relevé de Délibération");
    }

    // ------------------------------------------------------------------------
    // ÉCRAN 5 : Relevé de Note & Délibération
    // ------------------------------------------------------------------------
    private JPanel buildGradeCard() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(COLOR_BG_SURFACE);

        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(540, 400));
        card.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(32, 32, 32, 32)
        ));

        // En-tête : Badge Note + Titres
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        top.setOpaque(false);

        gradeBadgeLabel = new JLabel("85%", SwingConstants.CENTER);
        gradeBadgeLabel.setFont(new Font("Consolas", Font.BOLD, 26));
        gradeBadgeLabel.setPreferredSize(new Dimension(80, 80));
        gradeBadgeLabel.setOpaque(true);
        gradeBadgeLabel.setBorder(new LineBorder(COLOR_GREEN, 2, true));

        gradeTitleLabel = new JLabel("Niveau 2 Validé avec Succès");
        gradeTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        gradeSubtitleLabel = new JLabel("Décision enregistrée dans la base SQLite");
        gradeSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gradeSubtitleLabel.setForeground(COLOR_TEXT_MUTED);

        JPanel titleGrp = new JPanel(new GridLayout(2, 1, 0, 4));
        titleGrp.setOpaque(false);
        titleGrp.add(gradeTitleLabel);
        titleGrp.add(gradeSubtitleLabel);

        top.add(gradeBadgeLabel);
        top.add(titleGrp);
        card.add(top, BorderLayout.NORTH);

        // Décision Pédagogique
        decisionBoxPanel = new JPanel(new BorderLayout(0, 6));
        decisionBoxPanel.setBackground(COLOR_BG_SURFACE);
        decisionBoxPanel.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        decisionTitleLabel = new JLabel("Conditions de passage satisfaites (Règles RG2 & RG3) :");
        decisionTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        decisionDescLabel = new JLabel("<html>La note obtenue est supérieure ou égale au seuil fixé à 70%. Le Niveau 3 a été déverrouillé automatiquement.</html>");
        decisionDescLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        decisionDescLabel.setForeground(COLOR_TEXT_MUTED);

        decisionBoxPanel.add(decisionTitleLabel, BorderLayout.NORTH);
        decisionBoxPanel.add(decisionDescLabel, BorderLayout.CENTER);
        card.add(decisionBoxPanel, BorderLayout.CENTER);

        // Boutons bas
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnBackDash = new JButton("Retour au tableau de bord");
        btnBackDash.addActionListener(e -> showCard(CARD_DASHBOARD, "Tableau de bord des Niveaux"));

        btnProceedL3 = new JButton("Accéder au Niveau 3 ➔");
        btnProceedL3.setBackground(COLOR_PRIMARY);
        btnProceedL3.setForeground(Color.WHITE);
        btnProceedL3.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Ouverture du Niveau 3 : JavaScript & Manipulation du DOM.", "Niveau 3", JOptionPane.INFORMATION_MESSAGE);
            showCard(CARD_DASHBOARD, "Tableau de bord des Niveaux");
        });

        actions.add(btnBackDash);
        actions.add(btnProceedL3);
        card.add(actions, BorderLayout.SOUTH);

        root.add(card);
        return root;
    }

    // ------------------------------------------------------------------------
    // ÉCRAN 6 : Espace Enseignant (Suivi JTable + Paramétrage JSlider)
    // ------------------------------------------------------------------------
    private JPanel buildTeacherCard() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(COLOR_BG_SURFACE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 3 Widgets de Statistiques
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        statsRow.add(createStatCard("34", "Étudiants Inscrits (L3)", COLOR_PRIMARY));
        statsRow.add(createStatCard("78%", "Taux de Réussite Global", COLOR_GREEN));
        statsRow.add(createStatCard("2.1", "Niveau Moyen Atteint", new Color(139, 92, 246)));
        root.add(statsRow, BorderLayout.NORTH);

        // Séparation gauche (Paramétrage) et droite (JTable)
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);

        // Configuration Pédagogique (Curseurs)
        JPanel configCard = new JPanel();
        configCard.setLayout(new BoxLayout(configCard, BoxLayout.Y_AXIS));
        configCard.setBackground(Color.WHITE);
        configCard.setPreferredSize(new Dimension(320, 0));
        configCard.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel configTitle = new JLabel("Paramétrage des Règles (RG2 & RG5)");
        configTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        configTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        thresholdValueLabel = new JLabel("Seuil d'admission : " + teacherThreshold + "%");
        thresholdValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        thresholdValueLabel.setForeground(COLOR_PRIMARY);

        thresholdSlider = new JSlider(50, 90, teacherThreshold);
        thresholdSlider.setBackground(Color.WHITE);
        thresholdSlider.addChangeListener(e -> {
            teacherThreshold = thresholdSlider.getValue();
            thresholdValueLabel.setText("Seuil d'admission : " + teacherThreshold + "%");
            lblSeuilL2.setText("Seuil requis : " + teacherThreshold + "%");
        });

        JLabel timeLabel = new JLabel("Durée du test : 15 min");
        JSlider timeSlider = new JSlider(5, 30, 15);
        timeSlider.setBackground(Color.WHITE);

        JLabel attLabel = new JLabel("Tentatives autorisées : 2 essais");
        JSlider attSlider = new JSlider(1, 4, 2);
        attSlider.setBackground(Color.WHITE);

        JButton btnSaveRules = new JButton("Enregistrer les règles (SQLite)");
        btnSaveRules.setBackground(COLOR_PRIMARY);
        btnSaveRules.setForeground(Color.WHITE);
        btnSaveRules.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Règles mises à jour dans la base SQLite :\n- Seuil d'admission fixé à : " + teacherThreshold + "%\n- Quota d'essais : 2",
            "Mise à jour Pédagogique (RG7)", JOptionPane.INFORMATION_MESSAGE));

        configCard.add(configTitle);
        configCard.add(Box.createVerticalStrut(16));
        configCard.add(thresholdValueLabel);
        configCard.add(thresholdSlider);
        configCard.add(Box.createVerticalStrut(12));
        configCard.add(timeLabel);
        configCard.add(timeSlider);
        configCard.add(Box.createVerticalStrut(12));
        configCard.add(attLabel);
        configCard.add(attSlider);
        configCard.add(Box.createVerticalStrut(20));
        configCard.add(btnSaveRules);

        // Tableau JTable de Suivi des Étudiants
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        // Barre de filtrage de table
        JPanel filterBar = new JPanel(new BorderLayout(10, 0));
        filterBar.setOpaque(false);
        JLabel filterLbl = new JLabel("Rechercher un apprenant :");
        JTextField searchField = new JTextField();
        searchField.addCaretListener(e -> {
            String q = searchField.getText();
            if (q.trim().isEmpty()) {
                tableSorter.setRowFilter(null);
            } else {
                tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + q));
            }
        });
        filterBar.add(filterLbl, BorderLayout.WEST);
        filterBar.add(searchField, BorderLayout.CENTER);

        String[] cols = {"Nom & Prénom", "Niveau Actif", "Tentatives", "Dernier Score", "Statut"};
        Object[][] data = {
            {"Yacine Benali (L3)", "Niveau 2", "0 / 2", "—", "En cours"},
            {"Amina Khelil", "Niveau 2", "1 / 2", "58%", "Non validé"},
            {"Karim Tahar", "Niveau 1", "0 / 2", "—", "En cours"},
            {"Sarah Meziane", "Niveau 3", "1 / 2", "92%", "Validé"}
        };

        studentTableModel = new DefaultTableModel(data, cols);
        JTable table = new JTable(studentTableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableSorter = new TableRowSorter<>(studentTableModel);
        table.setRowSorter(tableSorter);

        tableCard.add(filterBar, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        content.add(configCard, BorderLayout.WEST);
        content.add(tableCard, BorderLayout.CENTER);

        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private JPanel createStatCard(String val, String lbl, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, accentColor),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel valLbl = new JLabel(val);
        valLbl.setFont(new Font("Consolas", Font.BOLD, 22));
        valLbl.setForeground(accentColor);

        JLabel descLbl = new JLabel(lbl);
        descLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        descLbl.setForeground(COLOR_TEXT_MUTED);

        card.add(valLbl, BorderLayout.NORTH);
        card.add(descLbl, BorderLayout.SOUTH);
        return card;
    }

    // ========================================================================
    // 4. JStatusBar Swing
    // ========================================================================
    private void initStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(COLOR_BG_SURFACE);
        statusBar.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, COLOR_BORDER),
            new EmptyBorder(4, 12, 4, 12)
        ));

        statusRouteLabel = new JLabel("Prêt • SQLite: learnweb.db (Connecté) • " + "Authentification");
        statusRouteLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        statusRouteLabel.setForeground(COLOR_TEXT_MUTED);

        statusActiveLevelLabel = new JLabel("• Niveau actif : 2");
        statusActiveLevelLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        statusActiveLevelLabel.setForeground(COLOR_PRIMARY);

        JPanel leftStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftStatus.setOpaque(false);
        leftStatus.add(statusRouteLabel);
        leftStatus.add(statusActiveLevelLabel);

        JLabel rightStatus = new JLabel("UTF-8 | Java 17+ (Swing Native)");
        rightStatus.setFont(new Font("Consolas", Font.PLAIN, 11));
        rightStatus.setForeground(COLOR_TEXT_MUTED);

        statusBar.add(leftStatus, BorderLayout.WEST);
        statusBar.add(rightStatus, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    // ========================================================================
    // 5. Dialogue Ergonomique (Bastien & Scapin)
    // ========================================================================
    private void openErgonomicsDialog() {
        JDialog dlg = new JDialog(this, "Fiche d'Évaluation Ergonomique — Critères de Bastien & Scapin", true);
        dlg.setSize(560, 500);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(18, 20, 18, 20));
        p.setBackground(Color.WHITE);

        addErgoRow(p, "1. Guidage & Incitation :",
            "Les statuts des niveaux sont identifiables instantanément par des codes couleurs clairs (Vert = Validé, Bleu = En cours, Gris = Verrouillé). Composants : JProgressBar, BorderFactory.");

        addErgoRow(p, "2. Charge de travail & Concision :",
            "L'atelier de code est directement intégré à côté du cours dans un JSplitPane pour éviter les allers-retours cognitifs avec des outils externes.");

        addErgoRow(p, "3. Contrôle & Prévention des erreurs :",
            "L'évaluation sommative est protégée par un dialogue modal JOptionPane.showConfirmDialog avant d'enregistrer la tentative.");

        addErgoRow(p, "4. Automatisation Pédagogique (RG3) :",
            "Dès que la note calculée atteint le seuil (70%), le déverrouillage du Niveau 3 s'effectue automatiquement sans intervention manuelle.");

        addErgoRow(p, "5. Adaptabilité & Cloisonnement des Rôles :",
            "Séparation stricte des privilèges : seul l'Enseignant a accès aux curseurs de seuils et à la JTable complète de cohorte.");

        JButton btnClose = new JButton("Fermer");
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dlg.dispose());
        p.add(Box.createVerticalStrut(10));
        p.add(btnClose);

        dlg.add(new JScrollPane(p));
        dlg.setVisible(true);
    }

    private void addErgoRow(JPanel p, String title, String text) {
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(COLOR_PRIMARY);

        JLabel d = new JLabel("<html><font color='#475569'>" + text + "</font></html>");
        d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        d.setBorder(new EmptyBorder(2, 0, 12, 0));

        p.add(t);
        p.add(d);
    }

    private void showCard(String cardId, String routeTitle) {
        cardLayout.show(mainContainer, cardId);
        statusRouteLabel.setText("Prêt • SQLite: learnweb.db (Connecté) • " + routeTitle);
    }

    // ========================================================================
    // Méthode Principale (Point d'Entrée)
    // ========================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DevALabsApp app = new DevALabsApp();
            app.setVisible(true);
        });
    }
}
