import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class VedicCalculator extends JFrame {
    private static final Color BG = new Color(246, 242, 232);
    private static final Color PANEL = new Color(255, 251, 243);
    private static final Color CARD = new Color(255, 255, 255);
    private static final Color ACCENT = new Color(188, 94, 52);
    private static final Color ACCENT_DARK = new Color(127, 63, 35);
    private static final Color ACCENT_SOFT = new Color(241, 222, 207);
    private static final Color SPOTLIGHT_BG = new Color(109, 58, 36);
    private static final Color SPOTLIGHT_TEXT = new Color(255, 248, 242);
    private static final Color LINE = new Color(220, 208, 194);
    private static final Color INK = new Color(36, 31, 27);
    private static final Color MUTED = new Color(102, 86, 72);
    private static final Font DISPLAY_FONT = new Font("Serif", Font.BOLD, 20);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);
    private static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 13);
    private static final Random RANDOM = new Random();
    private final Path projectRoot;
    private final Path verilogDir;
    private final Path sourceDir;
    private final Map<String, Path> rtlFiles;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();
            VedicCalculator calculator = new VedicCalculator();
            calculator.setVisible(true);
        });
    }

    public VedicCalculator() {
        this.projectRoot = resolveProjectRoot();
        this.verilogDir = projectRoot.resolve("verilog");
        this.sourceDir = projectRoot.resolve("src");
        this.rtlFiles = buildRtlFileMap();

        setTitle("Vedic Calculator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setMinimumSize(new Dimension(920, 640));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(BG);
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createTabs(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("TabbedPane.selected", CARD);
        UIManager.put("TabbedPane.contentAreaColor", BG);
        UIManager.put("TabbedPane.focus", ACCENT_SOFT);
        UIManager.put("TabbedPane.foreground", INK);
        UIManager.put("Button.font", LABEL_FONT);
        UIManager.put("Label.foreground", INK);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(PANEL);
        header.setBorder(createCardBorder());

        JLabel title = new JLabel("Vedic Calculator");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(INK);

        JLabel subtitle = new JLabel("Simulation-first arithmetic explorer with guided binary walkthroughs and RTL inspection.");
        subtitle.setFont(BODY_FONT);
        subtitle.setForeground(MUTED);

        JLabel badge = createBadgeLabel("8-bit Simulation Suite");
        JLabel badgeTwo = createBadgeLabel("Java + Verilog");

        JPanel badgeRow = new JPanel();
        badgeRow.setOpaque(false);
        badgeRow.setLayout(new BoxLayout(badgeRow, BoxLayout.X_AXIS));
        badgeRow.add(badge);
        badgeRow.add(Box.createHorizontalStrut(8));
        badgeRow.add(badgeTwo);

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.add(title);
        textBox.add(Box.createVerticalStrut(6));
        textBox.add(subtitle);
        textBox.add(Box.createVerticalStrut(10));
        textBox.add(badgeRow);

        JPanel spotlight = new JPanel();
        spotlight.setBackground(SPOTLIGHT_BG);
        spotlight.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 95, 68)),
            new EmptyBorder(16, 18, 16, 18)
        ));
        spotlight.setPreferredSize(new Dimension(360, 132));
        spotlight.setLayout(new BoxLayout(spotlight, BoxLayout.Y_AXIS));

        JLabel spotlightTitle = new JLabel("Demo Highlights");
        spotlightTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        spotlightTitle.setForeground(new Color(255, 222, 197));
        spotlightTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel spotlightLine1 = new JLabel("• Stepwise arithmetic reports");
        spotlightLine1.setFont(new Font("SansSerif", Font.PLAIN, 15));
        spotlightLine1.setForeground(SPOTLIGHT_TEXT);
        spotlightLine1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel spotlightLine2 = new JLabel("• Random stimulus generation");
        spotlightLine2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        spotlightLine2.setForeground(SPOTLIGHT_TEXT);
        spotlightLine2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel spotlightLine3 = new JLabel("• RTL source and block diagrams");
        spotlightLine3.setFont(new Font("SansSerif", Font.PLAIN, 15));
        spotlightLine3.setForeground(SPOTLIGHT_TEXT);
        spotlightLine3.setAlignmentX(Component.LEFT_ALIGNMENT);

        spotlight.add(spotlightTitle);
        spotlight.add(Box.createVerticalStrut(14));
        spotlight.add(spotlightLine1);
        spotlight.add(Box.createVerticalStrut(8));
        spotlight.add(spotlightLine2);
        spotlight.add(Box.createVerticalStrut(8));
        spotlight.add(spotlightLine3);

        header.add(textBox, BorderLayout.CENTER);
        header.add(spotlight, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(BODY_FONT);
        tabs.setBackground(BG);
        tabs.addTab("Addition", createAdditionPanel());
        tabs.addTab("Subtraction", createSubtractionPanel());
        tabs.addTab("Multiplication", createMultiplicationPanel());
        tabs.addTab("Division", createDivisionPanel());
        tabs.addTab("Simulation Lab", createSimulationLabPanel());
        tabs.addTab("Verilog RTL", createRtlPanel());
        return tabs;
    }

    private JPanel createAdditionPanel() {
        OperationPanel panel = new OperationPanel(
            "Anurupyena Addition",
            "Parallel carry-style explanation inspired by propagate/generate trees."
        );
        panel.bind((a, b) -> buildAdditionReport(a, b));
        return panel;
    }

    private JPanel createSubtractionPanel() {
        OperationPanel panel = new OperationPanel(
            "Nikhilam Subtraction",
            "Bit inversion and two's-complement addition for fast subtraction."
        );
        panel.bind((a, b) -> buildSubtractionReport(a, b));
        return panel;
    }

    private JPanel createMultiplicationPanel() {
        OperationPanel panel = new OperationPanel(
            "Urdhva Tiryagbhyam Multiplication",
            "Split operands into nibbles and combine four 4x4 partial products."
        );
        panel.bind((a, b) -> buildMultiplicationReport(a, b));
        return panel;
    }

    private JPanel createDivisionPanel() {
        OperationPanel panel = new OperationPanel(
            "Paravartya Division",
            "Shift-subtract walkthrough across 8 steps with quotient formation."
        );
        panel.bind((a, b) -> buildDivisionReport(a, b));
        return panel;
    }

    private JPanel createRtlPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG);

        JLabel title = new JLabel("Verilog RTL Browser");
        title.setFont(DISPLAY_FONT);
        title.setForeground(INK);

        JList<String> fileList = new JList<>(rtlFiles.keySet().toArray(String[]::new));
        fileList.setFont(BODY_FONT);
        fileList.setBackground(CARD);
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                label.setBorder(new EmptyBorder(8, 10, 8, 10));
                return label;
            }
        });

        JTextPane viewer = new JTextPane();
        viewer.setFont(MONO_FONT);
        viewer.setEditable(false);
        viewer.setBackground(new Color(28, 30, 33));
        viewer.setForeground(new Color(232, 237, 240));
        JScrollPane viewerScroll = new JScrollPane(viewer);
        viewerScroll.setBorder(createCardBorder());

        DiagramPanel diagramPanel = new DiagramPanel();
        JScrollPane diagramScroll = new JScrollPane(diagramPanel);
        diagramScroll.setBorder(createCardBorder());
        diagramScroll.getViewport().setBackground(new Color(251, 248, 240));

        JLabel infoLabel = new JLabel("Block diagram view is educational and derived from the selected RTL module.");
        infoLabel.setFont(BODY_FONT);
        infoLabel.setForeground(MUTED);

        JTabbedPane viewerTabs = new JTabbedPane();
        viewerTabs.setFont(BODY_FONT);
        viewerTabs.addTab("Source View", viewerScroll);
        viewerTabs.addTab("Block Diagram", diagramScroll);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setOpaque(false);
        right.add(infoLabel, BorderLayout.NORTH);
        right.add(viewerTabs, BorderLayout.CENTER);

        fileList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                String selected = fileList.getSelectedValue();
                if (selected != null) {
                    renderSource(viewer, readFile(rtlFiles.get(selected)));
                    diagramPanel.setModule(selected);
                }
            }
        });

        JButton exportButton = new JButton("Export All");
        exportButton.addActionListener(event -> exportAllFiles());
        stylePrimaryButton(exportButton);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBackground(PANEL);
        left.setBorder(createCardBorder());
        JLabel sourceTitle = new JLabel("Source Files");
        sourceTitle.setFont(LABEL_FONT);
        sourceTitle.setForeground(ACCENT_DARK);
        left.add(sourceTitle, BorderLayout.NORTH);
        left.add(new JScrollPane(fileList), BorderLayout.CENTER);
        left.add(exportButton, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.24);

        panel.add(title, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        if (!rtlFiles.isEmpty()) {
            fileList.setSelectedIndex(0);
        }
        return panel;
    }

    private JPanel createSimulationLabPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG);

        JLabel title = new JLabel("Simulation Lab");
        title.setFont(DISPLAY_FONT);
        title.setForeground(INK);

        JLabel subtitle = new JLabel("Generate directed and random stimulus with expected results for the Verilog testbench.");
        subtitle.setFont(BODY_FONT);
        subtitle.setForeground(MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        JTextArea outputArea = new JTextArea();
        outputArea.setFont(MONO_FONT);
        outputArea.setEditable(false);
        outputArea.setLineWrap(false);
        outputArea.setBackground(new Color(252, 249, 242));
        outputArea.setForeground(INK);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(createCardBorder());

        JSpinner randomCaseCount = new JSpinner(new SpinnerNumberModel(8, 1, 64, 1));
        JButton generateReportButton = new JButton("Generate Cases");
        JButton exportStimulusButton = new JButton("Export Stimulus");
        stylePrimaryButton(generateReportButton);
        styleSecondaryButton(exportStimulusButton);

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBackground(PANEL);
        controls.setBorder(createCardBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel randomCasesLabel = new JLabel("Random cases");
        randomCasesLabel.setFont(LABEL_FONT);
        controls.add(randomCasesLabel, gbc);
        gbc.gridx = 1;
        controls.add(randomCaseCount, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controls.add(generateReportButton, gbc);

        gbc.gridy = 2;
        controls.add(exportStimulusButton, gbc);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);
        content.add(controls, BorderLayout.WEST);
        content.add(scrollPane, BorderLayout.CENTER);

        generateReportButton.addActionListener(event -> outputArea.setText(buildSimulationLabReport((Integer) randomCaseCount.getValue())));
        exportStimulusButton.addActionListener(event -> exportSimulationStimulus((Integer) randomCaseCount.getValue()));

        panel.add(header, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        outputArea.setText(buildSimulationLabReport((Integer) randomCaseCount.getValue()));
        outputArea.setCaretPosition(0);
        return panel;
    }

    private Report buildAdditionReport(int a, int b) {
        int raw = a + b;
        int sum = raw & 0xFF;
        int carryOut = (raw >> 8) & 0x1;
        StringBuilder detail = new StringBuilder();
        detail.append("Inputs\n");
        detail.append("A = ").append(formatBinary(a, 8)).append('\n');
        detail.append("B = ").append(formatBinary(b, 8)).append("\n\n");
        detail.append("Propagate / Generate\n");
        for (int i = 7; i >= 0; i--) {
            int ai = (a >> i) & 1;
            int bi = (b >> i) & 1;
            detail.append("bit ").append(i)
                .append(": P=").append(ai ^ bi)
                .append(" G=").append(ai & bi)
                .append('\n');
        }

        int carry = 0;
        detail.append("\nCarry Walk\n");
        for (int i = 0; i < 8; i++) {
            int p = ((a >> i) & 1) ^ ((b >> i) & 1);
            int g = ((a >> i) & 1) & ((b >> i) & 1);
            int nextCarry = g | (p & carry);
            detail.append("c").append(i).append("=").append(carry)
                .append(" -> s").append(i).append('=').append(p ^ carry)
                .append(", c").append(i + 1).append('=').append(nextCarry)
                .append('\n');
            carry = nextCarry;
        }

        String summary = "Sum = " + raw + " (" + sum + " mod 256, carry " + carryOut + ")";
        String metrics = "Estimated gates: 24 XOR / 16 AND / 8 OR | Critical depth: about 4-5 logic levels";
        return new Report(summary, valueTable(
            labeled("Decimal A", String.valueOf(a)),
            labeled("Decimal B", String.valueOf(b)),
            labeled("Binary Sum", formatBinary(sum, 8) + "  carry=" + carryOut),
            labeled("Hex Sum", String.format("0x%02X", sum))
        ), detail.toString(), metrics);
    }

    private Report buildSubtractionReport(int a, int b) {
        int diff = (a - b) & 0xFF;
        boolean borrow = a < b;
        int complement = (~b + 1) & 0xFF;

        StringBuilder detail = new StringBuilder();
        detail.append("Inputs\n");
        detail.append("A = ").append(formatBinary(a, 8)).append('\n');
        detail.append("B = ").append(formatBinary(b, 8)).append("\n\n");
        detail.append("Nikhilam Step\n");
        detail.append("~B        = ").append(formatBinary((~b) & 0xFF, 8)).append('\n');
        detail.append("2's comp  = ").append(formatBinary(complement, 8)).append('\n');
        detail.append("A + compB = ").append(formatBinary(diff, 8)).append('\n');
        detail.append("Borrow    = ").append(borrow ? "1 (A < B)" : "0").append('\n');

        String summary = "Difference = " + (a - b) + " (8-bit result " + diff + ")";
        String metrics = "Estimated gates: 8 NOT + 8 full-adders | Critical depth: complement in 1 level then adder depth";
        return new Report(summary, valueTable(
            labeled("Decimal A", String.valueOf(a)),
            labeled("Decimal B", String.valueOf(b)),
            labeled("Binary Diff", formatBinary(diff, 8) + "  borrow=" + (borrow ? 1 : 0)),
            labeled("Hex Diff", String.format("0x%02X", diff))
        ), detail.toString(), metrics);
    }

    private Report buildMultiplicationReport(int a, int b) {
        int aLow = a & 0xF;
        int aHigh = (a >> 4) & 0xF;
        int bLow = b & 0xF;
        int bHigh = (b >> 4) & 0xF;
        int q0 = aLow * bLow;
        int q1 = aHigh * bLow;
        int q2 = aLow * bHigh;
        int q3 = aHigh * bHigh;
        int product = a * b;

        StringBuilder detail = new StringBuilder();
        detail.append("Nibble Split\n");
        detail.append("A_hi=").append(aHigh).append("  A_lo=").append(aLow).append('\n');
        detail.append("B_hi=").append(bHigh).append("  B_lo=").append(bLow).append("\n\n");
        detail.append("4 Sub-products\n");
        detail.append("Q0 = A_lo x B_lo = ").append(q0).append('\n');
        detail.append("Q1 = A_hi x B_lo = ").append(q1).append('\n');
        detail.append("Q2 = A_lo x B_hi = ").append(q2).append('\n');
        detail.append("Q3 = A_hi x B_hi = ").append(q3).append("\n\n");
        detail.append("Recombine\n");
        detail.append("Product = Q0 + (Q1 << 4) + (Q2 << 4) + (Q3 << 8)\n");
        detail.append("        = ").append(q0)
            .append(" + ").append(q1 << 4)
            .append(" + ").append(q2 << 4)
            .append(" + ").append(q3 << 8)
            .append(" = ").append(product).append("\n\n");
        detail.append("Diagonal intuition: Urdhva multiplies crosswise so partial products accumulate in parallel.");

        String summary = "Product = " + product;
        String metrics = "Estimated structure: 4 x 4-bit multipliers + 3 add stages | Critical depth: about 3 arithmetic stages";
        return new Report(summary, valueTable(
            labeled("Decimal A", String.valueOf(a)),
            labeled("Decimal B", String.valueOf(b)),
            labeled("Binary Product", formatBinary(product, 16)),
            labeled("Hex Product", String.format("0x%04X", product))
        ), detail.toString(), metrics);
    }

    private Report buildDivisionReport(int a, int b) {
        if (b == 0) {
            return new Report(
                "Division undefined for divisor 0",
                valueTable(
                    labeled("Dividend", String.valueOf(a)),
                    labeled("Divisor", "0"),
                    labeled("Quotient", "undefined"),
                    labeled("Remainder", "undefined")
                ),
                "Paravartya step trace cannot continue because the divisor is zero.",
                "Estimated structure: 8 compare/subtract rounds | Critical depth: 8 iterative steps"
            );
        }

        int remainder = 0;
        int quotient = 0;
        StringBuilder detail = new StringBuilder();
        detail.append("Shift-Subtract Steps\n");
        for (int i = 7; i >= 0; i--) {
            remainder = (remainder << 1) | ((a >> i) & 1);
            boolean subtract = remainder >= b;
            int before = remainder;
            if (subtract) {
                remainder -= b;
                quotient |= (1 << i);
            }
            detail.append("step ").append(8 - i)
                .append(": bring ").append((a >> i) & 1)
                .append(", remainder=").append(before)
                .append(subtract ? " >= " : " < ").append(b);
            if (subtract) {
                detail.append(", subtract -> ").append(remainder).append(", q[").append(i).append("]=1");
            } else {
                detail.append(", keep -> ").append(remainder).append(", q[").append(i).append("]=0");
            }
            detail.append('\n');
        }

        String summary = "Quotient = " + quotient + ", Remainder = " + remainder;
        String metrics = "Estimated structure: 8 compare/subtract rounds | Critical depth: 8 iterative decision steps";
        return new Report(summary, valueTable(
            labeled("Dividend", String.valueOf(a)),
            labeled("Divisor", String.valueOf(b)),
            labeled("Binary Quotient", formatBinary(quotient, 8)),
            labeled("Binary Remainder", formatBinary(remainder, 8))
        ), detail.toString(), metrics);
    }

    private String buildSimulationLabReport(int randomCount) {
        int[][] directedCases = {
            {0, 0},
            {1, 1},
            {12, 5},
            {15, 15},
            {64, 8},
            {127, 3},
            {128, 2},
            {255, 1},
            {255, 15},
            {23, 0}
        };
        int[][] randomCases = buildRandomCases(randomCount);

        StringBuilder builder = new StringBuilder();
        builder.append("SIMULATION LAB REPORT\n");
        builder.append("=====================\n\n");
        builder.append("Directed cases\n");
        builder.append("----------------\n");
        for (int[] testCase : directedCases) {
            builder.append(buildCaseLine(testCase[0], testCase[1])).append('\n');
        }

        builder.append("\nRandom cases\n");
        builder.append("-------------\n");
        for (int[] testCase : randomCases) {
            builder.append(buildCaseLine(testCase[0], testCase[1])).append('\n');
        }

        builder.append("\nVerilog stimulus snippet\n");
        builder.append("-----------------------\n");
        builder.append(buildStimulusSnippet(directedCases, randomCases));
        return builder.toString();
    }

    private String buildCaseLine(int a, int b) {
        int sumRaw = a + b;
        int sum = sumRaw & 0xFF;
        int cout = (sumRaw >> 8) & 1;
        int diff = (a - b) & 0xFF;
        int borrow = a < b ? 1 : 0;
        int product = a * b;
        String quotientText;
        String remainderText;
        if (b == 0) {
            quotientText = "undef";
            remainderText = "undef";
        } else {
            quotientText = String.valueOf(a / b);
            remainderText = String.valueOf(a % b);
        }

        return String.format(
            "a=%3d b=%3d | sum=%3d cout=%d | diff=%3d borrow=%d | product=%5d | quotient=%5s | remainder=%5s",
            a, b, sum, cout, diff, borrow, product, quotientText, remainderText
        );
    }

    private int[][] buildRandomCases(int randomCount) {
        int[][] randomCases = new int[randomCount][2];
        for (int i = 0; i < randomCount; i++) {
            randomCases[i][0] = RANDOM.nextInt(256);
            randomCases[i][1] = RANDOM.nextInt(256);
        }
        return randomCases;
    }

    private String buildStimulusSnippet(int[][] directedCases, int[][] randomCases) {
        StringBuilder builder = new StringBuilder();
        for (int[] testCase : directedCases) {
            builder.append("apply_case(8'd").append(testCase[0]).append(", 8'd").append(testCase[1]).append(");\n");
        }
        for (int[] testCase : randomCases) {
            builder.append("apply_case(8'd").append(testCase[0]).append(", 8'd").append(testCase[1]).append(");\n");
        }
        return builder.toString();
    }

    private void exportSimulationStimulus(int randomCount) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export simulation stimulus");
        chooser.setSelectedFile(projectRoot.resolve("simulation_vectors.txt").toFile());
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.writeString(chooser.getSelectedFile().toPath(), buildSimulationLabReport(randomCount), StandardCharsets.UTF_8);
                JOptionPane.showMessageDialog(this, "Simulation vectors exported successfully.", "Export complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportAllFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Export RTL and Java sources");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path target = chooser.getSelectedFile().toPath();
            try {
                Files.createDirectories(target);
                for (Map.Entry<String, Path> entry : rtlFiles.entrySet()) {
                    Files.copy(entry.getValue(), target.resolve(entry.getKey()), StandardCopyOption.REPLACE_EXISTING);
                }
                Path javaSource = sourceDir.resolve("VedicCalculator.java");
                if (Files.exists(javaSource)) {
                    Files.copy(javaSource, target.resolve("VedicCalculator.java"), StandardCopyOption.REPLACE_EXISTING);
                }
                JOptionPane.showMessageDialog(this, "Files exported to " + target, "Export complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String readFile(Path path) {
        try {
            if (path == null) {
                return "// Unable to read file\n// No path was resolved for this selection.";
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "// Unable to read " + path + "\n// " + ex.getMessage();
        }
    }

    private Path resolveProjectRoot() {
        Path current = Path.of("").toAbsolutePath();
        for (Path cursor = current; cursor != null; cursor = cursor.getParent()) {
            if (isProjectLayout(cursor)) {
                return cursor;
            }
            Path nested = cursor.resolve("vedic_calc");
            if (isProjectLayout(nested)) {
                return nested;
            }
        }
        return current;
    }

    private boolean isProjectLayout(Path candidate) {
        return candidate != null
            && Files.isDirectory(candidate.resolve("verilog"))
            && Files.isDirectory(candidate.resolve("src"));
    }

    private void renderSource(JTextPane pane, String code) {
        StyledDocument doc = pane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());
            SimpleAttributeSet normal = new SimpleAttributeSet();
            StyleConstants.setForeground(normal, new Color(232, 237, 240));
            StyleConstants.setFontFamily(normal, MONO_FONT.getFamily());
            StyleConstants.setFontSize(normal, 13);

            SimpleAttributeSet keyword = new SimpleAttributeSet(normal);
            StyleConstants.setForeground(keyword, new Color(255, 165, 90));
            StyleConstants.setBold(keyword, true);

            SimpleAttributeSet comment = new SimpleAttributeSet(normal);
            StyleConstants.setForeground(comment, new Color(129, 180, 120));

            String[] lines = code.split("\\R", -1);
            for (String line : lines) {
                SimpleAttributeSet style = normal;
                String trimmed = line.trim();
                if (trimmed.startsWith("//") || trimmed.startsWith("`")) {
                    style = comment;
                } else if (trimmed.startsWith("module") || trimmed.startsWith("assign")
                        || trimmed.startsWith("always") || trimmed.startsWith("endmodule")) {
                    style = keyword;
                }
                doc.insertString(doc.getLength(), line + "\n", style);
            }
            pane.setCaretPosition(0);
        } catch (BadLocationException ex) {
            pane.setText(code);
        }
    }

    private Map<String, Path> buildRtlFileMap() {
        Map<String, Path> files = new LinkedHashMap<>();
        files.put("primitives.v", verilogDir.resolve("primitives.v"));
        files.put("vedic_adder_8bit.v", verilogDir.resolve("vedic_adder_8bit.v"));
        files.put("vedic_subtractor_8bit.v", verilogDir.resolve("vedic_subtractor_8bit.v"));
        files.put("vedic_mult_4x4.v", verilogDir.resolve("vedic_mult_4x4.v"));
        files.put("vedic_mult_8x8.v", verilogDir.resolve("vedic_mult_8x8.v"));
        files.put("vedic_divider_8bit.v", verilogDir.resolve("vedic_divider_8bit.v"));
        files.put("vedic_top.v", verilogDir.resolve("vedic_top.v"));
        files.put("testbench.v", verilogDir.resolve("testbench.v"));
        return files;
    }

    private static String formatBinary(int value, int width) {
        int mask = width >= 31 ? -1 : (1 << width) - 1;
        String binary = Integer.toBinaryString(value & mask);
        binary = String.format("%" + width + "s", binary).replace(' ', '0');
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < binary.length(); i++) {
            if (i > 0 && (binary.length() - i) % 4 == 0) {
                grouped.append(' ');
            }
            grouped.append(binary.charAt(i));
        }
        return grouped.toString();
    }

    private static JPanel valueTable(String... rows) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        for (String row : rows) {
            JLabel label = new JLabel(row);
            label.setFont(BODY_FONT);
            label.setForeground(INK);
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 235, 226)),
                new EmptyBorder(6, 0, 6, 0)
            ));
            panel.add(label);
        }
        return panel;
    }

    private static String labeled(String label, String value) {
        return label + ": " + value;
    }

    private static String flattenReport(Report report) {
        return report.summary() + "\n\n" + report.detail() + "\n\n" + report.metrics();
    }

    private static javax.swing.border.Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(247, 242, 235))
            ),
            new EmptyBorder(16, 16, 16, 16)
        );
    }

    private static JLabel createBadgeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(ACCENT_SOFT);
        label.setForeground(ACCENT_DARK);
        label.setFont(LABEL_FONT);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(227, 196, 173)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return label;
    }

    private static void stylePrimaryButton(JButton button) {
        button.setBackground(CARD);
        button.setForeground(ACCENT_DARK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 186, 167)),
            new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private static void styleSecondaryButton(JButton button) {
        button.setBackground(CARD);
        button.setForeground(ACCENT_DARK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 186, 167)),
            new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private interface ReportBuilder {
        Report build(int a, int b);
    }

    private record Report(String summary, JPanel values, String detail, String metrics) {
    }

    private final class DiagramPanel extends JPanel {
        private String moduleName = "vedic_adder_8bit.v";

        DiagramPanel() {
            setBackground(new Color(251, 248, 240));
            setPreferredSize(new Dimension(920, 620));
            setBorder(new EmptyBorder(18, 18, 18, 18));
        }

        void setModule(String moduleName) {
            this.moduleName = moduleName;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(BODY_FONT);

            drawDiagramFrame(g2);
            switch (moduleName) {
                case "vedic_adder_8bit.v" -> drawAdderDiagram(g2);
                case "vedic_subtractor_8bit.v" -> drawSubtractorDiagram(g2);
                case "vedic_mult_4x4.v" -> drawMult4Diagram(g2);
                case "vedic_mult_8x8.v" -> drawMult8Diagram(g2);
                case "vedic_divider_8bit.v" -> drawDividerDiagram(g2);
                case "vedic_top.v" -> drawTopDiagram(g2);
                case "primitives.v" -> drawPrimitivesDiagram(g2);
                case "testbench.v" -> drawTestbenchDiagram(g2);
                default -> drawGenericDiagram(g2);
            }
            g2.dispose();
        }

        private void drawDiagramFrame(Graphics2D g2) {
            g2.setColor(INK);
            g2.setFont(new Font("Serif", Font.BOLD, 24));
            g2.drawString("Logic / Block Diagram", 24, 34);
            g2.setFont(BODY_FONT);
            g2.setColor(new Color(103, 88, 75));
            g2.drawString("Selected module: " + moduleName, 24, 58);
        }

        private void drawAdderDiagram(Graphics2D g2) {
            drawInputBus(g2, "A[7:0]", 40, 130, 170);
            drawInputBus(g2, "B[7:0]", 40, 230, 170);
            drawBlock(g2, 220, 90, 180, 90, "Propagate", "P = A xor B");
            drawBlock(g2, 220, 210, 180, 90, "Generate", "G = A and B");
            drawBlock(g2, 455, 145, 200, 100, "Carry Chain", "c(i+1)=G | P.c");
            drawBlock(g2, 710, 145, 160, 100, "Sum XOR", "S = P xor C");
            drawOutputBus(g2, "SUM[7:0]", 900, 180, 120);
            drawOutputBus(g2, "COUT", 900, 235, 120);
            connect(g2, 170, 130, 220, 130);
            connect(g2, 170, 230, 220, 230);
            connect(g2, 170, 130, 220, 255);
            connect(g2, 170, 230, 220, 155);
            connect(g2, 400, 135, 455, 175);
            connect(g2, 400, 255, 455, 215);
            connect(g2, 655, 195, 710, 195);
            connect(g2, 870, 180, 900, 180);
            connect(g2, 655, 225, 900, 235);
            annotate(g2, 240, 330, "8-bit educational view: propagate/generate stage, carry resolution, and final sum formation.");
        }

        private void drawSubtractorDiagram(Graphics2D g2) {
            drawInputBus(g2, "A[7:0]", 40, 150, 170);
            drawInputBus(g2, "B[7:0]", 40, 260, 170);
            drawBlock(g2, 220, 225, 150, 70, "Invert B", "~B");
            drawBlock(g2, 410, 225, 120, 70, "+1", "2's comp");
            drawBlock(g2, 590, 140, 180, 140, "8-bit Adder", "A + (~B + 1)");
            drawOutputBus(g2, "DIFF[7:0]", 840, 180, 130);
            drawOutputBus(g2, "BORROW", 840, 235, 130);
            connect(g2, 170, 150, 590, 170);
            connect(g2, 170, 260, 220, 260);
            connect(g2, 370, 260, 410, 260);
            connect(g2, 530, 260, 590, 230);
            connect(g2, 770, 180, 840, 180);
            connect(g2, 770, 235, 840, 235);
            annotate(g2, 230, 330, "Nikhilam-inspired datapath: invert subtrahend, add one, then feed the adder with minuend A.");
        }

        private void drawMult4Diagram(Graphics2D g2) {
            drawInputBus(g2, "A[3:0]", 40, 140, 170);
            drawInputBus(g2, "B[3:0]", 40, 250, 170);
            drawBlock(g2, 220, 105, 130, 70, "A split", "hi / lo");
            drawBlock(g2, 220, 215, 130, 70, "B split", "hi / lo");
            drawBlock(g2, 410, 80, 130, 70, "2x2 M0", "lo x lo");
            drawBlock(g2, 410, 170, 130, 70, "2x2 M1", "hi x lo");
            drawBlock(g2, 410, 260, 130, 70, "2x2 M2", "lo x hi");
            drawBlock(g2, 410, 350, 130, 70, "2x2 M3", "hi x hi");
            drawBlock(g2, 615, 165, 170, 120, "Shift + Add", "Q0 + Q1<<2 + Q2<<2 + Q3<<4");
            drawOutputBus(g2, "PRODUCT[7:0]", 855, 220, 140);
            connect(g2, 170, 140, 220, 140);
            connect(g2, 170, 250, 220, 250);
            connect(g2, 350, 140, 410, 115);
            connect(g2, 350, 140, 410, 205);
            connect(g2, 350, 250, 410, 205);
            connect(g2, 350, 250, 410, 295);
            connect(g2, 350, 140, 410, 385);
            connect(g2, 350, 250, 410, 385);
            connect(g2, 540, 115, 615, 190);
            connect(g2, 540, 205, 615, 215);
            connect(g2, 540, 295, 615, 240);
            connect(g2, 540, 385, 615, 265);
            connect(g2, 785, 220, 855, 220);
            annotate(g2, 200, 470, "Urdhva layout uses four 2x2 cells and recombines aligned partial products.");
        }

        private void drawMult8Diagram(Graphics2D g2) {
            drawInputBus(g2, "A[7:0]", 40, 140, 170);
            drawInputBus(g2, "B[7:0]", 40, 250, 170);
            drawBlock(g2, 220, 105, 140, 70, "A split", "Ahi / Alo");
            drawBlock(g2, 220, 215, 140, 70, "B split", "Bhi / Blo");
            drawBlock(g2, 430, 70, 150, 70, "4x4 Q0", "Alo x Blo");
            drawBlock(g2, 430, 160, 150, 70, "4x4 Q1", "Ahi x Blo");
            drawBlock(g2, 430, 250, 150, 70, "4x4 Q2", "Alo x Bhi");
            drawBlock(g2, 430, 340, 150, 70, "4x4 Q3", "Ahi x Bhi");
            drawBlock(g2, 650, 155, 180, 150, "Shift + Add", "Q0 + Q1<<4 + Q2<<4 + Q3<<8");
            drawOutputBus(g2, "PRODUCT[15:0]", 900, 225, 120);
            connect(g2, 170, 140, 220, 140);
            connect(g2, 170, 250, 220, 250);
            connect(g2, 360, 140, 430, 105);
            connect(g2, 360, 140, 430, 195);
            connect(g2, 360, 250, 430, 195);
            connect(g2, 360, 250, 430, 285);
            connect(g2, 360, 140, 430, 375);
            connect(g2, 360, 250, 430, 375);
            connect(g2, 580, 105, 650, 185);
            connect(g2, 580, 195, 650, 215);
            connect(g2, 580, 285, 650, 245);
            connect(g2, 580, 375, 650, 275);
            connect(g2, 830, 225, 900, 225);
            annotate(g2, 210, 470, "Hierarchical multiplier: four 4x4 blocks followed by aligned recombination.");
        }

        private void drawDividerDiagram(Graphics2D g2) {
            drawInputBus(g2, "DIVIDEND[7:0]", 40, 150, 170);
            drawInputBus(g2, "DIVISOR[7:0]", 40, 280, 170);
            drawBlock(g2, 230, 125, 160, 80, "Shift Register", "bring next bit");
            drawBlock(g2, 450, 125, 170, 80, "Remainder", "work register");
            drawBlock(g2, 450, 245, 170, 80, "Compare/Sub", "if rem >= div");
            drawBlock(g2, 685, 165, 170, 120, "Quotient Build", "set q[i] per step");
            drawOutputBus(g2, "QUOT[7:0]", 915, 190, 110);
            drawOutputBus(g2, "REM[7:0]", 915, 250, 110);
            connect(g2, 170, 150, 230, 150);
            connect(g2, 390, 165, 450, 165);
            connect(g2, 170, 280, 450, 280);
            connect(g2, 535, 205, 535, 245);
            connect(g2, 620, 185, 685, 185);
            connect(g2, 620, 280, 685, 245);
            connect(g2, 855, 190, 915, 190);
            connect(g2, 620, 165, 915, 250);
            annotate(g2, 205, 390, "Eight sequential decision rounds: shift in dividend bit, compare against divisor, subtract when possible.");
        }

        private void drawTopDiagram(Graphics2D g2) {
            drawInputBus(g2, "A[7:0]", 40, 130, 170);
            drawInputBus(g2, "B[7:0]", 40, 230, 170);
            drawBlock(g2, 250, 60, 180, 70, "Adder", "sum, cout");
            drawBlock(g2, 250, 160, 180, 70, "Subtractor", "diff, borrow");
            drawBlock(g2, 250, 260, 180, 70, "Multiplier", "product");
            drawBlock(g2, 250, 360, 180, 70, "Divider", "quotient, remainder");
            drawOutputBus(g2, "SUM / COUT", 520, 95, 150);
            drawOutputBus(g2, "DIFF / BORROW", 520, 195, 150);
            drawOutputBus(g2, "PRODUCT", 520, 295, 150);
            drawOutputBus(g2, "QUOT / REM", 520, 395, 150);
            connect(g2, 170, 130, 250, 95);
            connect(g2, 170, 230, 250, 115);
            connect(g2, 170, 130, 250, 195);
            connect(g2, 170, 230, 250, 215);
            connect(g2, 170, 130, 250, 295);
            connect(g2, 170, 230, 250, 315);
            connect(g2, 170, 130, 250, 395);
            connect(g2, 170, 230, 250, 415);
            connect(g2, 430, 95, 520, 95);
            connect(g2, 430, 195, 520, 195);
            connect(g2, 430, 295, 520, 295);
            connect(g2, 430, 395, 520, 395);
            annotate(g2, 210, 500, "Top-level integration fans the same operands into each arithmetic engine for parallel observation.");
        }

        private void drawPrimitivesDiagram(Graphics2D g2) {
            drawInputBus(g2, "a, b", 70, 170, 150);
            drawBlock(g2, 280, 120, 140, 70, "Half Adder", "xor / and");
            drawOutputBus(g2, "sum, carry", 500, 155, 130);
            drawInputBus(g2, "a, b, cin", 70, 300, 150);
            drawBlock(g2, 280, 250, 140, 70, "Full Adder", "a+b+cin");
            drawOutputBus(g2, "sum, carry", 500, 285, 130);
            connect(g2, 150, 170, 280, 155);
            connect(g2, 420, 155, 500, 155);
            connect(g2, 150, 300, 280, 285);
            connect(g2, 420, 285, 500, 285);
            annotate(g2, 150, 410, "Primitive building blocks used by higher arithmetic modules.");
        }

        private void drawTestbenchDiagram(Graphics2D g2) {
            drawBlock(g2, 120, 160, 170, 90, "Stimulus", "apply_case tasks");
            drawBlock(g2, 400, 150, 180, 110, "vedic_top DUT", "adder, subtractor,\nmultiplier, divider");
            drawBlock(g2, 690, 160, 170, 90, "Monitor", "$display results");
            connect(g2, 290, 205, 400, 205);
            connect(g2, 580, 205, 690, 205);
            annotate(g2, 150, 350, "Behavioral simulation harness driving the integrated arithmetic core.");
        }

        private void drawGenericDiagram(Graphics2D g2) {
            drawBlock(g2, 280, 160, 260, 100, "Module View", "No specialized diagram yet");
            annotate(g2, 290, 320, "The source view remains available for any module.");
        }

        private void drawBlock(Graphics2D g2, int x, int y, int width, int height, String title, String subtitle) {
            g2.setColor(CARD);
            g2.fillRoundRect(x, y, width, height, 24, 24);
            g2.setColor(new Color(199, 171, 147));
            g2.drawRoundRect(x, y, width, height, 24, 24);
            g2.setColor(INK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString(title, x + 16, y + 28);
            g2.setFont(BODY_FONT);
            for (int i = 0; i < subtitle.split("\n").length; i++) {
                g2.drawString(subtitle.split("\n")[i], x + 16, y + 50 + (i * 18));
            }
        }

        private void drawInputBus(Graphics2D g2, String label, int x, int y, int width) {
            g2.setColor(ACCENT);
            g2.drawLine(x, y, x + width, y);
            g2.fillOval(x + width - 6, y - 6, 12, 12);
            g2.setColor(INK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(label, x, y - 10);
        }

        private void drawOutputBus(Graphics2D g2, String label, int x, int y, int width) {
            g2.setColor(new Color(76, 117, 78));
            g2.drawLine(x - width, y, x, y);
            g2.fillOval(x - 6, y - 6, 12, 12);
            g2.setColor(INK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(label, x - width + 4, y - 10);
        }

        private void connect(Graphics2D g2, int x1, int y1, int x2, int y2) {
            g2.setColor(new Color(121, 107, 94));
            g2.drawLine(x1, y1, x2, y2);
        }

        private void annotate(Graphics2D g2, int x, int y, String text) {
            g2.setColor(new Color(93, 79, 67));
            g2.setFont(BODY_FONT);
            g2.drawString(text, x, y);
        }
    }

    private final class OperationPanel extends JPanel {
        private final JSpinner aInput;
        private final JSpinner bInput;
        private final JLabel summaryLabel;
        private final JPanel valueHost;
        private final JTextPane detailPane;
        private final JTextField metricsField;
        private ReportBuilder builder;

        OperationPanel(String title, String subtitle) {
            super(new BorderLayout(12, 12));
            setBackground(BG);

            aInput = new JSpinner(new SpinnerNumberModel(12, 0, 255, 1));
            bInput = new JSpinner(new SpinnerNumberModel(5, 0, 255, 1));
            summaryLabel = new JLabel();
            valueHost = new JPanel(new BorderLayout());
            valueHost.setOpaque(false);
            detailPane = new JTextPane();
            metricsField = new JTextField();

            JPanel top = new JPanel(new BorderLayout(8, 8));
            top.setBackground(PANEL);
            top.setBorder(createCardBorder());

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(DISPLAY_FONT);
            titleLabel.setForeground(INK);

            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(BODY_FONT);
            subtitleLabel.setForeground(MUTED);

            JPanel titleBox = new JPanel();
            titleBox.setOpaque(false);
            titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
            titleBox.add(titleLabel);
            titleBox.add(Box.createVerticalStrut(4));
            titleBox.add(subtitleLabel);
            titleBox.add(Box.createVerticalStrut(10));
            titleBox.add(createBadgeLabel("Interactive walkthrough"));

            JPanel controls = new JPanel(new GridBagLayout());
            controls.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 6, 4, 6);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0;
            gbc.gridy = 0;
            JLabel operandALabel = new JLabel("Operand A");
            operandALabel.setFont(LABEL_FONT);
            gbc.gridx = 2;
            controls.add(operandALabel, gbc);
            gbc.gridx = 3;
            controls.add(aInput, gbc);

            gbc.gridx = 2;
            gbc.gridy = 1;
            JLabel operandBLabel = new JLabel("Operand B");
            operandBLabel.setFont(LABEL_FONT);
            controls.add(operandBLabel, gbc);
            gbc.gridx = 3;
            controls.add(bInput, gbc);

            JButton compute = new JButton("Compute");
            compute.addActionListener(this::computeReport);
            stylePrimaryButton(compute);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            controls.add(compute, gbc);

            JButton randomize = new JButton("Random");
            randomize.addActionListener(event -> {
                aInput.setValue(RANDOM.nextInt(256));
                bInput.setValue(RANDOM.nextInt(256));
                computeReport(null);
            });
            styleSecondaryButton(randomize);
            gbc.gridx = 1;
            controls.add(randomize, gbc);

            JButton swap = new JButton("Swap");
            swap.addActionListener(event -> {
                Object temp = aInput.getValue();
                aInput.setValue(bInput.getValue());
                bInput.setValue(temp);
                computeReport(null);
            });
            styleSecondaryButton(swap);
            gbc.gridx = 0;
            gbc.gridy = 1;
            controls.add(swap, gbc);

            JButton export = new JButton("Export Report");
            export.addActionListener(event -> exportCurrentReport());
            styleSecondaryButton(export);
            gbc.gridx = 1;
            controls.add(export, gbc);

            ChangeListener liveUpdate = event -> computeReport(null);
            aInput.addChangeListener(liveUpdate);
            bInput.addChangeListener(liveUpdate);

            top.add(titleBox, BorderLayout.CENTER);
            top.add(controls, BorderLayout.EAST);

            JPanel bottom = new JPanel(new BorderLayout(12, 12));
            bottom.setOpaque(false);

            JPanel leftCard = new JPanel(new BorderLayout(8, 8));
            leftCard.setBackground(CARD);
            leftCard.setBorder(createCardBorder());
            summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            summaryLabel.setForeground(ACCENT);
            JLabel summaryTitle = new JLabel("Computed Result");
            summaryTitle.setFont(LABEL_FONT);
            summaryTitle.setForeground(ACCENT_DARK);
            JPanel summaryHeader = new JPanel();
            summaryHeader.setOpaque(false);
            summaryHeader.setLayout(new BoxLayout(summaryHeader, BoxLayout.Y_AXIS));
            summaryHeader.add(summaryTitle);
            summaryHeader.add(Box.createVerticalStrut(6));
            summaryHeader.add(summaryLabel);
            leftCard.add(summaryHeader, BorderLayout.NORTH);
            leftCard.add(valueHost, BorderLayout.CENTER);

            detailPane.setEditable(false);
            detailPane.setFont(MONO_FONT);
            detailPane.setBackground(new Color(252, 249, 242));
            detailPane.setForeground(INK);
            JScrollPane detailScroll = new JScrollPane(detailPane);
            detailScroll.setBorder(createCardBorder());

            metricsField.setEditable(false);
            metricsField.setBackground(CARD);
            metricsField.setBorder(createCardBorder());
            metricsField.setFont(BODY_FONT);

            JPanel rightStack = new JPanel(new BorderLayout(10, 10));
            rightStack.setOpaque(false);
            JLabel detailTitle = new JLabel("Bit-Level Walkthrough");
            detailTitle.setFont(LABEL_FONT);
            detailTitle.setForeground(ACCENT_DARK);
            JLabel metricsTitle = new JLabel("Implementation Notes");
            metricsTitle.setFont(LABEL_FONT);
            metricsTitle.setForeground(ACCENT_DARK);

            JPanel detailCard = new JPanel(new BorderLayout(8, 8));
            detailCard.setBackground(CARD);
            detailCard.setBorder(createCardBorder());
            detailCard.add(detailTitle, BorderLayout.NORTH);
            detailCard.add(detailScroll, BorderLayout.CENTER);

            JPanel metricsCard = new JPanel(new BorderLayout(8, 8));
            metricsCard.setBackground(CARD);
            metricsCard.setBorder(createCardBorder());
            metricsCard.add(metricsTitle, BorderLayout.NORTH);
            metricsCard.add(metricsField, BorderLayout.CENTER);

            rightStack.add(detailCard, BorderLayout.CENTER);
            rightStack.add(metricsCard, BorderLayout.SOUTH);

            bottom.add(leftCard, BorderLayout.WEST);
            bottom.add(rightStack, BorderLayout.CENTER);
            leftCard.setPreferredSize(new Dimension(290, 0));

            add(top, BorderLayout.NORTH);
            add(bottom, BorderLayout.CENTER);
        }

        void bind(ReportBuilder reportBuilder) {
            this.builder = reportBuilder;
            computeReport(null);
        }

        private void computeReport(ActionEvent event) {
            if (builder == null) {
                return;
            }
            int a = (Integer) aInput.getValue();
            int b = (Integer) bInput.getValue();
            Report report = builder.build(a, b);
            summaryLabel.setText(report.summary());
            valueHost.removeAll();
            valueHost.add(report.values(), BorderLayout.NORTH);
            valueHost.revalidate();
            valueHost.repaint();
            detailPane.setText(report.detail());
            detailPane.setCaretPosition(0);
            metricsField.setText(report.metrics());
        }

        private void exportCurrentReport() {
            if (builder == null) {
                return;
            }
            int a = (Integer) aInput.getValue();
            int b = (Integer) bInput.getValue();
            Report report = builder.build(a, b);
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export operation report");
            chooser.setSelectedFile(projectRoot.resolve("report_" + a + "_" + b + ".txt").toFile());
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.writeString(chooser.getSelectedFile().toPath(), flattenReport(report), StandardCharsets.UTF_8);
                    JOptionPane.showMessageDialog(this, "Report exported successfully.", "Export complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
