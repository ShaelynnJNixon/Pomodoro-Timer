
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GraphDFS extends JFrame {
    private JTextArea inputArea;
    private JTextField startField;
    private JButton runButton;
    private JTextArea outputArea;

    public GraphDFS() {
        super("Simple DFS Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout(8, 8));

        // Top panel: instructions + sample
        JPanel top = new JPanel(new BorderLayout(6,6));
        JLabel instr = new JLabel("<html>Enter adjacency list (one line each): <br>"
                + "<i>Node: neighbor1 neighbor2 ...</i> (directed edges).<br>"
                + "Example lines (click 'Load Sample' to populate):</html>");
        top.add(instr, BorderLayout.NORTH);

        JButton sampleBtn = new JButton("Load Sample (your graph)");
        sampleBtn.addActionListener(e -> loadSample());
        top.add(sampleBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // Center: input and output split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        inputArea = new JTextArea();
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        inputArea.setBorder(BorderFactory.createTitledBorder("Adjacency List (left)"));
        JScrollPane inScroll = new JScrollPane(inputArea);
        split.setLeftComponent(inScroll);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setBorder(BorderFactory.createTitledBorder("Output (right)"));
        JScrollPane outScroll = new JScrollPane(outputArea);
        split.setRightComponent(outScroll);

        split.setDividerLocation(380);
        add(split, BorderLayout.CENTER);

        // Bottom: controls
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.add(new JLabel("Start node (optional):"));
        startField = new JTextField(4);
        bottom.add(startField);

        runButton = new JButton("Run DFS");
        runButton.addActionListener(e -> runDFS());
        bottom.add(runButton);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
            startField.setText("");
        });
        bottom.add(clearBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadSample() {
        // sample adjacency from your earlier graph
        String sample = ""
            + "A: D F C\n"
            + "B: A C E L\n"
            + "C: F G E\n"
            + "D: F I\n"
            + "E: G L\n"
            + "F: H\n"
            + "G: H\n"
            + "H: I\n"
            + "I: K J\n"
            + "J: H L\n"
            + "K: J\n"
            + "L: G\n";
        inputArea.setText(sample);
    }

    private void runDFS() {
        outputArea.setText("");
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            outputArea.setText("Enter an adjacency list first (or click 'Load Sample').");
            return;
        }

        // Parse adjacency
        Map<String, List<String>> graph = new LinkedHashMap<>();
        // keep track of insertion order for stable outputs
        Set<String> allNodes = new TreeSet<>(); // alphabetical order if no start node
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            // format: Node: neighbors...
            String[] parts = line.split(":", 2);
            if (parts.length == 0) continue;
            String node = parts[0].trim();
            allNodes.add(node);
            List<String> neigh = new ArrayList<>();
            if (parts.length == 2) {
                String rhs = parts[1].trim();
                if (!rhs.isEmpty()) {
                    String[] tokens = rhs.split("\\s+");
                    for (String t : tokens) {
                        if (!t.isEmpty()) {
                            neigh.add(t);
                            allNodes.add(t);
                        }
                    }
                }
            }
            graph.put(node, neigh);
        }
        // ensure every node has an entry (even if no outgoing edges)
        for (String n : new ArrayList<>(allNodes)) {
            graph.putIfAbsent(n, new ArrayList<>());
        }

        // Determine start order
        String start = startField.getText().trim();
        List<String> order = new ArrayList<>();
        if (!start.isEmpty()) {
            if (!graph.containsKey(start)) {
                outputArea.setText("Start node '" + start + "' not found in graph.");
                return;
            }
            // order: start first, then the rest in alphabetical order excluding start
            order.add(start);
            for (String n : allNodes) if (!n.equals(start)) order.add(n);
        } else {
            // full DFS forest; iterate nodes in sorted order (allNodes is a TreeSet)
            order.addAll(allNodes);
        }

        // DFS bookkeeping
        Map<String, Integer> disc = new HashMap<>();
        Map<String, Integer> fin = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();
        List<String> discoverOrder = new ArrayList<>();
        int time = 0;

        // We'll need to capture all directed edges for classification later
        List<Edge> edges = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : graph.entrySet()) {
            String u = e.getKey();
            for (String v : e.getValue()) {
                edges.add(new Edge(u, v));
            }
        }

        // DFS recursive using stack-safe approach (but recursion is fine for reasonable graphs).
        // Using recursion for clarity.
        final int[] tRef = new int[1];
        tRef[0] = time;

        for (String node : order) {
            if (!visited.contains(node)) {
                parent.put(node, null);
                dfsVisit(node, graph, visited, inStack, disc, fin, parent, discoverOrder, tRef);
            }
        }

        // produce traversal
        StringBuilder out = new StringBuilder();
        out.append("DFS discovery order: ").append(String.join(" ", discoverOrder)).append("\n\n");
        out.append(String.format("%-6s %-6s %-6s %-8s\n","Node","d","f","Parent"));
        List<String> nodesByDisc = new ArrayList<>(disc.keySet());
        // sort by discovery time
        nodesByDisc.sort(Comparator.comparingInt(disc::get));
        for (String n : nodesByDisc) {
            out.append(String.format("%-6s %-6d %-6d %-8s\n", n, disc.get(n), fin.get(n), parent.get(n) == null ? "-" : parent.get(n)));
        }

        // Classify edges using d/f times
        out.append("\nEdge classifications:\n");
        out.append(String.format("%-8s %-8s %-12s\n","From","To","Type"));
        for (Edge e : edges) {
            String u = e.u, v = e.v;
            String type = classifyEdge(u, v, disc, fin, parent);
            out.append(String.format("%-8s %-8s %-12s\n", u, v, type));
        }

        outputArea.setText(out.toString());
    }

    private void dfsVisit(String u,
                          Map<String, List<String>> graph,
                          Set<String> visited,
                          Set<String> inStack,
                          Map<String,Integer> disc,
                          Map<String,Integer> fin,
                          Map<String,String> parent,
                          List<String> discoverOrder,
                          int[] tRef) {
        tRef[0] = tRef[0] + 1;
        disc.put(u, tRef[0]);
        discoverOrder.add(u);
        visited.add(u);
        inStack.add(u);

        // iterate neighbors in the order provided in adjacency list
        for (String v : graph.getOrDefault(u, Collections.emptyList())) {
            if (!visited.contains(v)) {
                parent.put(v, u);
                dfsVisit(v, graph, visited, inStack, disc, fin, parent, discoverOrder, tRef);
            } else {
                // visited already; we still don't mark finish here
            }
        }

        inStack.remove(u);
        tRef[0] = tRef[0] + 1;
        fin.put(u, tRef[0]);
    }

    private String classifyEdge(String u, String v, Map<String,Integer> disc, Map<String,Integer> fin, Map<String,String> parent) {
        // If v was discovered by u (i.e., parent[v] == u) it's a tree edge
        if (parent.containsKey(v) && u.equals(parent.get(v))) return "TREE";
        Integer du = disc.get(u), dv = disc.get(v), fu = fin.get(u), fv = fin.get(v);
        if (du == null || dv == null || fu == null || fv == null) return "UNKNOWN";

        // Back edge: v is ancestor of u (dv < du && fu < fv) but easier:
        // back edge: dv < du && du < fu && fu < fv  (v discovered before u and finishes after u starts)
        if (dv < du && du < fu && fu < fv) return "BACK";

        // Forward edge: u is ancestor of v but isn't the tree edge (du < dv && dv < fv && fv < fu)
        if (du < dv && dv < fv && fv < fu) return "FORWARD";

        // Cross edge: otherwise (different branches / already finished)
        return "CROSS";
    }

    // small helper
    private static class Edge {
        String u, v;
        Edge(String u, String v) { this.u = u; this.v = v; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphDFS app = new GraphDFS();
            app.setVisible(true);
        });
    }
}