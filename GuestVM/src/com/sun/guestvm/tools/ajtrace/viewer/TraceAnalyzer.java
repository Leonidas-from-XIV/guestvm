/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, California 95054, U.S.A. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are
 * subject to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * Parts of the product may be derived from Berkeley BSD systems,
 * licensed from the University of California. UNIX is a registered
 * trademark in the U.S.  and in other countries, exclusively licensed
 * through X/Open Company, Ltd.
 *
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other
 * countries.
 *
 * This product is covered and controlled by U.S. Export Control laws and
 * may be subject to the export or import laws in other
 * countries. Nuclear, missile, chemical biological weapons or nuclear
 * maritime end uses or end users, whether direct or indirect, are
 * strictly prohibited. Export or reexport to countries subject to
 * U.S. embargo or to entities identified on U.S. export exclusion lists,
 * including, but not limited to, the denied persons and specially
 * designated nationals lists is strictly prohibited.
 *
 */
package com.sun.guestvm.tools.ajtrace.viewer;
/**

 */
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.SpringLayout;

import java.net.URL;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

/**
<<<<<<< local
 * A tool to display method traces from the TraceAspect aspect, hacked from Swing Tree tutorial.
=======
 * A tool to display method traces from the TraceAspect aspect, hacked from Swing Tree tutorial
>>>>>>> other
 *
 * @author Mick Jordan
 *
 */

public class TraceAnalyzer extends JPanel {

    private URL helpURL;
    private static boolean DEBUG = false;
    private static boolean PROGRESS = false;

    enum TimeFormat {
        Nano, Micro, Milli, Sec
    };

    private static TimeFormat timeFormat = TimeFormat.Micro;

    enum TimeDisplay {
        WallRel, WallAbs, Duration
    };

    private static TimeDisplay timeDisplay = TimeDisplay.Duration;
    private long traceStartTime; // used for WallRel time display

    // Optionally play with line styles. Possible values are
    // "Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";

    Map<String, JTree> threadJTrees = new HashMap<String, JTree>(); // thread trees
    Set<JTree> matchJTrees = new HashSet<JTree>();
    JTree currentJTree;

    // Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = false;
    JFrame myFrame;
    String traceFilePathName;
    long nodeCount;

    public TraceAnalyzer(String[] args) {
        super(new GridLayout(1, 0));

        try {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("-f")) {
                    traceFilePathName = args[++i];
                } else if (arg.equals("-debug")) {
                    DEBUG = true;
                } else if (arg.equals("-progress")) {
                    PROGRESS = true;
                } else if (arg.equals("-time")) {
                    i++;
                    String tf = args[i];
                    if (tf.equals("milli")) {
                        timeFormat = TimeFormat.Milli;
                    } else if (tf.equals("nano"))
                        timeFormat = TimeFormat.Nano;
                    else if (tf.equals("micro")) {
                        timeFormat = TimeFormat.Micro;
                    } else if (tf.equals("sec")) {
                        timeFormat = TimeFormat.Sec;
                    } else
                        throw new Exception("unknown time format: " + tf);
                }
            }

            // Create and set up the window.
            myFrame = new JFrame("TraceAnalyzer " + traceFilePathName);
            myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            if (traceFilePathName == null) {
                usage();
                return;
            }
            BufferedReader r = new BufferedReader(new FileReader(traceFilePathName));

            // Create the nodes.
            Map<String, DefaultMutableTreeNode> threadTreeNodes = createNodes(r);
            r.close();

            // JComponent left = null; int count = 0;
            JTabbedPane threadTabPane = new JTabbedPane();
            threadTabPane.addChangeListener(new TabbedPaneChangeListener());
            for (DefaultMutableTreeNode top : threadTreeNodes.values()) {

                // Create a tree that allows one selection at a time.
                JTree tree = new JTree(top);
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                ToolTipManager.sharedInstance().registerComponent(tree);
                tree.setCellRenderer(new ToolTipRenderer());

                new APopupMenu(tree);

                tree.setShowsRootHandles(true);

                if (playWithLineStyle) {
                    System.out.println("line style = " + lineStyle);
                    tree.putClientProperty("JTree.lineStyle", lineStyle);
                }

                // Create the scroll pane and add the tree to it.
                JScrollPane treeView = new JScrollPane(tree);
                // Dimension minimumSize = new Dimension(500, 500);
                // treeView.setMinimumSize(minimumSize);

                MethodData md = (MethodData) top.getUserObject();
                threadJTrees.put(md.thread, tree);
                threadTabPane.add(md.thread, treeView);
            }
            add(threadTabPane);
            this.setOpaque(true); // content panes must be opaque
            myFrame.setContentPane(this);

            myFrame.setJMenuBar(createMenuBar());

            // Display the window.
            myFrame.pack();
            myFrame.setVisible(true);
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
    }

    private static void usage() {
        System.out.println("usage: -f tracefile [-debug] [-progress] [-time sec | milli | micro | nano]");
    }

    class ToolTipRenderer extends DefaultTreeCellRenderer {

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            MethodData md = (MethodData) node.getUserObject();
            if (md.thisArg != null) {
                setToolTipText(md.thisArg);
            } else
                setToolTipText(null);
            return this;
        }
    }

    class TabbedPaneChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
            JViewport viewPort = (JViewport) scrollPane.getComponent(0);
            JTree t = (JTree) viewPort.getComponent(0);
            currentJTree = t;
        }
    }

    enum FindType {
        First, Next, All
    };

    enum FindWhat {
        Method, Arg
    };

    public JTree getCurrentJTree() {
        return currentJTree;
    }

    class APopupMenu implements TreeSelectionListener {

        JTree tree;
        FindHelper findHelper;
        boolean moveByFind = false;

        APopupMenu(JTree tree) {
            this.tree = tree;
            tree.addTreeSelectionListener(this);

            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(new PropertiesAction());
            popup.add(menuItem);
            menuItem = new JMenuItem(new ExpandAllAction("ExpandAll"));
            popup.add(menuItem);
            menuItem = new JMenuItem(new ExpandAction("Expand"));
            popup.add(menuItem);
            findHelper = new FindHelper(); // singleton
            popup.add(makeFindMenu(FindWhat.Method));
            popup.add(makeFindMenu(FindWhat.Arg));
            JMenuItem myTimeItem = new JMenuItem(new MyTimeAction("My Time"));
            popup.add(myTimeItem);
            popup.add(makeTimeSortMenu());

            MouseListener popupListener = new PopupListener(popup);
            tree.addMouseListener(popupListener);
        }

        /** Required by TreeSelectionListener interface. */
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (node == null)
                return;

            MethodData md = (MethodData) node.getUserObject();
            if (DEBUG) {
                System.out.println(md.toString());
            }
            // reset FindHelper unless this change came from moveByNode
            if (!moveByFind)
                findHelper.startTp = null;
        }

        class PropertiesAction extends AbstractAction {

            JPanel propsPanel;
            Map<String, JTextField> propsMap = new HashMap<String, JTextField>();

            public PropertiesAction() {
                super("Properties", null);
                // putValue(SHORT_DESCRIPTION, desc);
                // putValue(MNEMONIC_KEY, mnemonic);
                String[] labels = { "Name", "Entry time", "Exit time", "Entry cpu", "Exit cpu", "This", "Result", "Parameters"};

                propsPanel = new JPanel(new SpringLayout());
                for (int i = 0; i < labels.length; i++) {
                    JLabel l = new JLabel(labels[i] + ": ", JLabel.TRAILING);
                    propsPanel.add(l);
                    JTextField textField = new JTextField(10);
                    l.setLabelFor(textField);
                    propsPanel.add(textField);
                    propsMap.put(labels[i], textField);
                }
                SpringUtilities.makeCompactGrid(propsPanel, labels.length, 2, 5, 5, 5, 5);

            }

            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (DEBUG)
                    System.out.println(e);

                if (node == null)
                    return;

                MethodData md = (MethodData) node.getUserObject();
                propsMap.get("Name").setText(md.methodName);
                propsMap.get("This").setText(md.thisArg == null ? "" : md.thisArg);
                propsMap.get("Result").setText(md.result == null ? "" : md.result);
                long tst = timeDisplay == TimeDisplay.WallRel ? traceStartTime : 0;
                propsMap.get("Entry time").setText(TimeFunctions.formatTime(md.entryTimeInfo.wallTime - tst));
                propsMap.get("Exit time").setText(md.exitTimeInfo == null ? "?" : TimeFunctions.formatTime(md.exitTimeInfo.wallTime - tst));
                propsMap.get("Entry cpu").setText("u: " + TimeFunctions.formatTime(md.entryTimeInfo.userUsage) + " s:" + TimeFunctions.formatTime(md.entryTimeInfo.sysUsage));
                propsMap.get("Exit cpu").setText(
                                md.exitTimeInfo == null ? "?" : "u:" + TimeFunctions.formatTime(md.exitTimeInfo.userUsage) + " s:" + TimeFunctions.formatTime(md.exitTimeInfo.sysUsage));
                propsMap.get("Parameters").setText(md.params == null ? "" : fixNL(md.params));
                JFrame propsFrame = new JFrame("Properties for " + md.methodName);
                propsFrame.add(propsPanel);
                propsFrame.pack();
                propsFrame.setVisible(true);
            }

            private String fixNL(String s) {
                String[] sp = s.split("\\\\n");
                StringBuilder sb = new StringBuilder(s.length());
                for (int i = 0; i < sp.length; i++) {
                    sb.append(sp[i]);
                    if (i != sp.length - 1)
                        sb.append('\n');
                }
                return sb.toString();
            }

        }

        class ExpandAction extends AbstractAction {

            public ExpandAction(String name) {
                super(name, null);
                // putValue(SHORT_DESCRIPTION, desc);
                // putValue(MNEMONIC_KEY, mnemonic);
            }

            public void actionPerformed(ActionEvent e) {
                TreePath tp = tree.getSelectionPath();
                // System.out.println("Path is " + tp);
                String response = JOptionPane.showInputDialog(myFrame, "Depth:");
                if (response != null && !response.equals("")) {
                    try {
                        int n = Integer.parseInt(response);
                        expandNodes(tp, n);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(myFrame, "Integer required", "", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            protected void expandNodes(TreePath tp, int n) {
                TreeNode tn = (TreeNode) tp.getLastPathComponent();
                tree.makeVisible(tp);
                if (!tn.isLeaf() && n > 0) {
                    Enumeration en = tn.children();
                    while (en.hasMoreElements()) {
                        TreeNode ctn = (TreeNode) en.nextElement();
                        TreePath ctp = tp.pathByAddingChild(ctn);
                        expandNodes(ctp, n - 1);
                    }
                }
            }
        }

        class ExpandAllAction extends ExpandAction {

            public ExpandAllAction(String name) {
                super(name);
                // putValue(SHORT_DESCRIPTION, desc);
                // putValue(MNEMONIC_KEY, mnemonic);
            }

            public void actionPerformed(ActionEvent e) {
                TreePath tp = tree.getSelectionPath();
                // System.out.println("Path is " + tp);
                expandNodes(tp, Integer.MAX_VALUE);
            }

        }

        class FindHelper {
            static final String DEFAULT_SEARCH_TEXT = "com.sun.guestvm[\\.\\w]*";
            String searchText = DEFAULT_SEARCH_TEXT;
            TreePath startTp = null;
            TreePath[] matches = null;
            int nextIndex = 0;
        }

        private JMenu makeFindMenu(FindWhat findWhat) {
            JMenu result = new JMenu("Find " + findWhat);
            result.add(new JMenuItem(new FindAction(FindType.First, findWhat, "First", findHelper)));
            result.add(new JMenuItem(new FindAction(FindType.Next, findWhat, "Next", findHelper)));
            result.add(new JMenuItem(new FindAction(FindType.All, findWhat, "All", findHelper)));
            return result;
        }

        class FindAction extends AbstractAction {

            FindType type;
            FindWhat findWhat;
            FindHelper findHelper;

            public FindAction(FindType type, FindWhat findWhat, String menuName, FindHelper findHelper) {
                super(menuName, null);
                this.type = type;
                this.findWhat = findWhat;
                this.findHelper = findHelper;
            }

            public void actionPerformed(ActionEvent e) {
                if (type == FindType.Next) {
                    if (findHelper.startTp == null) {
                        JOptionPane.showMessageDialog(myFrame, "no active Find from selection", "", JOptionPane.ERROR_MESSAGE);

                    } else if (findHelper.matches == null || findHelper.nextIndex >= findHelper.matches.length) {
                        JOptionPane.showMessageDialog(myFrame, "no more matches", "", JOptionPane.ERROR_MESSAGE);
                    } else {
                        moveToNode(findHelper.matches[findHelper.nextIndex++]);
                    }
                } else {
                    TreePath tp = tree.getSelectionPath();
                    String response = (String) JOptionPane.showInputDialog(myFrame, "Find:", "", JOptionPane.PLAIN_MESSAGE, null, null, findHelper.searchText);
                    if (response == null)
                        return;
                    findHelper.searchText = response;
                    findHelper.startTp = tp;
                    findHelper.nextIndex = 1; // where Find->Next looks
                    TreePath[] tpa = findNodes();
                    if (tpa != null) {
                        findHelper.matches = tpa;
                        if (type == FindType.All) {
                            showAllMatches(tpa);
                        } else {
                            moveToNode(tpa[0]);
                        }
                    } else {
                        JOptionPane.showMessageDialog(myFrame, findWhat + " " + findHelper.searchText + " not found", "", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            private void moveToNode(TreePath tp) {
                if (DEBUG)
                    System.out.println(tp);
                moveByFind = true;
                tree.setSelectionPath(tp);
                tree.scrollPathToVisible(tp);
                moveByFind = false;
            }

            class MyTreePath extends TreePath {

                TreePath tp;

                MyTreePath(TreePath tp) {
                    this.tp = tp;
                }

                public String toString() {
                    return tp.getLastPathComponent().toString();
                }
            }

            protected void showAllMatches(TreePath[] tpa) {
                // We create a depth 1 tree model where the "userObject" is the
                // TreePath to the original node in the threadTree. This allows
                // us to move the selection in the threadTree when the user double-clicks
                // on a node in the depth 1 tree.
                MyTreePath[] mtpa = new MyTreePath[tpa.length];
                for (int i = 0; i < tpa.length; i++) {
                    mtpa[i] = new MyTreePath(tpa[i]);
                }
                final JTree tpaTree = new JTree(mtpa);
                matchJTrees.add(tpaTree);
                MouseListener ml = new MouseAdapter() {

                    public void mousePressed(MouseEvent e) {
                        DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tpaTree.getLastSelectedPathComponent();
                        if (tn == null)
                            return;
                        MyTreePath ttp = (MyTreePath) tn.getUserObject();
                        // ttp is the path to the node in the threadTree
                        if (DEBUG)
                            System.out.println(ttp.tp);
                        tree.setSelectionPath(ttp.tp);
                        tree.scrollPathToVisible(ttp.tp);
                    }
                };
                tpaTree.addMouseListener(ml);
                JScrollPane treeView = new JScrollPane(tpaTree);
                JFrame tpaFrame = new JFrame("Matches for " + findHelper.searchText);
                tpaFrame.add(treeView);
                JMenuBar tpaFrameMenuBar = new JMenuBar();
                JMenu tpaFrameMenu = new JMenu("File");
                tpaFrameMenuBar.add(tpaFrameMenu);
                JMenuItem tpaFrameMenuSaveItem = new JMenuItem(new SaveAction(tpaFrame, new FindSaveActionBody(tpaTree)));
                tpaFrameMenu.add(tpaFrameMenuSaveItem);
                tpaFrameMenu.add(new JMenuItem(new CloseAction(tpaFrame)));
                tpaFrame.setJMenuBar(tpaFrameMenuBar);
                tpaFrame.pack();
                tpaFrame.setVisible(true);
            }

            class FindSaveActionBody implements SaveActionBody {

                JTree myTree;

                FindSaveActionBody(JTree myTree) {
                    this.myTree = myTree;
                }

                public void doSave(PrintWriter pw) {
                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) myTree.getModel().getRoot();
                    for (int i = 0; i < root.getChildCount(); i++) {
                        DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
                        MyTreePath mtp = (MyTreePath) child.getUserObject();
                        DefaultMutableTreeNode realChild = (DefaultMutableTreeNode) mtp.tp.getLastPathComponent();
                        MethodData md = (MethodData) realChild.getUserObject();
                        pw.println(md);
                    }
                }
            }

            protected TreePath[] findNodes() {
                DefaultMutableTreeNode tn = (DefaultMutableTreeNode) findHelper.startTp.getLastPathComponent();
                ArrayList<TreePath> result = new ArrayList<TreePath>();
                Pattern pattern;
                try {
                    pattern = Pattern.compile(findHelper.searchText);
                } catch (PatternSyntaxException ex) {
                    JOptionPane.showMessageDialog(myFrame, "Pattern Syntax Error", "", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                matchTree(tn, result, pattern);
                int size = result.size();
                if (size == 0)
                    return null;
                else {
                    return result.toArray(new TreePath[size]);
                }
            }

            protected boolean matchTree(DefaultMutableTreeNode tn, ArrayList<TreePath> result, Pattern pattern) {
                MethodData md = (MethodData) tn.getUserObject();
                String toMatch = findWhat == FindWhat.Method ? md.methodName : md.params;
                if (toMatch != null && pattern.matcher(toMatch).matches()) {
                    result.add(new TreePath(tn.getPath()));
                    // to make Find->Next easy we find all the matches always
                    // if (type != FindType.All) return true;
                }
                Enumeration en = tn.children();
                while (en.hasMoreElements()) {
                    DefaultMutableTreeNode ctn = (DefaultMutableTreeNode) en.nextElement();
                    if (matchTree(ctn, result, pattern) /* && type != FindType.All */)
                        return true;
                }
                return false;
            }
        }

        class MyTimeAction extends AbstractAction {

            public MyTimeAction(String name) {
                super(name, null);
            }

            public void actionPerformed(ActionEvent e) {
                TreePath tp = tree.getSelectionPath();
                DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                TimeInfo ti = timeForNode(tn);
                JOptionPane.showMessageDialog(myFrame, "w=" + TimeFunctions.formatTime(ti.wallTime) + ", u=" + TimeFunctions.formatTime(ti.userUsage) + ", s=" + TimeFunctions.formatTime(ti.sysUsage),
                                "", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private JMenu makeTimeSortMenu() {
            JMenu result = new JMenu("TimeSort");
            result.add(new JMenuItem(new TimeSortAction("Wall")));
            result.add(new JMenuItem(new TimeSortAction("User")));
            result.add(new JMenuItem(new TimeSortAction("Sys")));
            return result;
        }

        class TimeSortAction extends AbstractAction {

            String sortBy;
            Comparator nameAndTimeInfoComparator;
            TimeInfo.Adder timeInfoAdder;
            TimeInfo.Type timeInfoType;

            public TimeSortAction(String name) {
                super(name, null);
                sortBy = name;
                Comparator<TimeInfo> timeInfoComparator = null;
                if (name.equals("Wall")) {
                    timeInfoComparator = new TimeInfo.WallTimeComparator();
                    timeInfoAdder = new TimeInfo.WallTimeAdder();
                    timeInfoType = TimeInfo.Type.WallTime;
                } else if (name.equals("User")) {
                    timeInfoComparator = new TimeInfo.UserUsageComparator();
                    timeInfoAdder = new TimeInfo.UserUsageAdder();
                    timeInfoType = TimeInfo.Type.UserUsage;
                } else if (name.equals("Sys")) {
                    timeInfoComparator = new TimeInfo.SysUsageComparator();
                    timeInfoAdder = new TimeInfo.SysUsageAdder();
                    timeInfoType = TimeInfo.Type.SysUsage;
                }
                nameAndTimeInfoComparator = new NameAndTimeInfoComparator(timeInfoComparator);
            }

            class NameAndTimeInfo {

                String name;
                TimeInfo timeInfo;

                NameAndTimeInfo(String name, TimeInfo timeInfo) {
                    this.name = name;
                    this.timeInfo = timeInfo;
                }
            }

            class NameAndTimeInfoComparator implements Comparator<NameAndTimeInfo> {

                Comparator<TimeInfo> timeInfoComparator;

                NameAndTimeInfoComparator(Comparator<TimeInfo> timeInfoComparator) {
                    this.timeInfoComparator = timeInfoComparator;
                }

                public int compare(NameAndTimeInfo n1, NameAndTimeInfo n2) {
                    return timeInfoComparator.compare(n1.timeInfo, n2.timeInfo);
                }
            }

            public void actionPerformed(ActionEvent e) {
                TreePath tp = tree.getSelectionPath();
                DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                Map<String, ArrayList<DefaultMutableTreeNode>> map = new HashMap<String, ArrayList<DefaultMutableTreeNode>>();
                visitNodes(map, tn);
                // now we need to sum up the times for the individual methods
                NameAndTimeInfo[] nameAndTimeInfo = new NameAndTimeInfo[map.size()];
                int ix = 0;
                for (Map.Entry me : map.entrySet()) {
                    ArrayList<DefaultMutableTreeNode> altn = (ArrayList<DefaultMutableTreeNode>) me.getValue();
                    TimeInfo alti = new TimeInfo();
                    for (DefaultMutableTreeNode xtn : altn) {
                        alti.add(timeForNode(xtn));
                    }
                    nameAndTimeInfo[ix++] = new NameAndTimeInfo((String) me.getKey(), alti);
                }
                Arrays.sort(nameAndTimeInfo, nameAndTimeInfoComparator);
                long totalTime = 0;
                for (NameAndTimeInfo nti : nameAndTimeInfo) {
                    totalTime = timeInfoAdder.add(totalTime, nti.timeInfo);
                }
                String[] labels = { "Name", "Entry time", "Exit time", "Entry cpu", "Exit cpu", "This", "Result", "Parameters"};

                JPanel panel = new JPanel(new SpringLayout());
                panel.add(new JLabel("Percent"));
                panel.add(new JLabel("Time"));
                panel.add(new JLabel("Method"));
                for (int i = nameAndTimeInfo.length - 1; i >= 0; i--) {
                    NameAndTimeInfo nti = nameAndTimeInfo[i];
                    long time = nti.timeInfo.get(timeInfoType);
                    double percent = ((double) time * 100) / (double) totalTime;
                    panel.add(new JTextField(TimeFunctions.ftime(percent, TimeFunctions.format2d)));
                    panel.add(new JTextField(TimeFunctions.formatTime(time)));
                    panel.add(new JTextField(nti.name));
                }
                SpringUtilities.makeCompactGrid(panel, nameAndTimeInfo.length + 1, 3, 3, 3, 3, 3);
                JFrame frame = new JFrame("Sorted Time: " + sortBy);
                JScrollPane scrollPane = new JScrollPane(panel);
                frame.add(scrollPane);
                JMenu frameMenu = new JMenu("File");
                JMenuBar frameMenuBar = new JMenuBar();
                frameMenuBar.add(frameMenu);
                JMenuItem frameMenuSaveItem = new JMenuItem(new SaveAction(frame, new TimeInfoSaveActionBody(nameAndTimeInfo, totalTime)));
                frameMenu.add(frameMenuSaveItem);
                frameMenu.add(new JMenuItem(new CloseAction(frame)));
                frame.setJMenuBar(frameMenuBar);
                frame.pack();
                frame.setVisible(true);
            }

            private void visitNodes(Map<String, ArrayList<DefaultMutableTreeNode>> map, DefaultMutableTreeNode ptn) {
                Enumeration en = ptn.children();
                while (en.hasMoreElements()) {
                    DefaultMutableTreeNode tn = (DefaultMutableTreeNode) en.nextElement();
                    MethodData md = (MethodData) tn.getUserObject();
                    ArrayList<DefaultMutableTreeNode> instances = map.get(md.methodName);
                    if (instances == null) {
                        // new
                        instances = new ArrayList<DefaultMutableTreeNode>();
                        map.put(md.methodName, instances);
                    }
                    instances.add(tn);
                    visitNodes(map, tn);
                }
            }

            class TimeInfoSaveActionBody implements SaveActionBody {

                NameAndTimeInfo[] nameAndTimeInfo;
                long totalTime;

                TimeInfoSaveActionBody(NameAndTimeInfo[] nameAndTimeInfo, long totalTime) {
                    this.nameAndTimeInfo = nameAndTimeInfo;
                    this.totalTime = totalTime;
                }

                public void doSave(PrintWriter pw) {
                    for (int i = nameAndTimeInfo.length - 1; i >= 0; i--) {
                        NameAndTimeInfo nti = nameAndTimeInfo[i];
                        long time = nti.timeInfo.get(timeInfoType);
                        double percent = ((double) time * 100) / (double) totalTime;
                        pw.print(TimeFunctions.ftime(percent, TimeFunctions.format2d));
                        pw.print("\t");
                        pw.print(TimeFunctions.formatTime(time));
                        pw.print("\t");
                        pw.println(nti.name);
                    }
                }
            }
        }

        private TimeInfo timeForNode(DefaultMutableTreeNode tn) {
            MethodData mymd = (MethodData) tn.getUserObject();
            long childWall = 0;
            long childUser = 0;
            long childSys = 0;
            Enumeration en = tn.children();
            while (en.hasMoreElements()) {
                DefaultMutableTreeNode ctn = (DefaultMutableTreeNode) en.nextElement();
                MethodData cmd = (MethodData) ctn.getUserObject();
                childWall += cmd.exitTimeInfo.wallTime - cmd.entryTimeInfo.wallTime;
                childUser += cmd.exitTimeInfo.userUsage - cmd.entryTimeInfo.userUsage;
                childSys += cmd.exitTimeInfo.sysUsage - cmd.entryTimeInfo.sysUsage;
            }
            long myWall = mymd.exitTimeInfo.wallTime - mymd.entryTimeInfo.wallTime;
            long myUser = mymd.exitTimeInfo.userUsage - mymd.entryTimeInfo.userUsage;
            long mySys = mymd.exitTimeInfo.sysUsage - mymd.entryTimeInfo.sysUsage;
            return new TimeInfo(myWall - childWall, myUser - childUser, mySys - childSys);
        }
    }

    static class PopupListener extends MouseAdapter {

        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    static class NodeStack {

        ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
        int depth = 0;
    }

    private Map<String, DefaultMutableTreeNode> createNodes(BufferedReader r) throws Exception {
        Map<String, DefaultMutableTreeNode> result = new HashMap<String, DefaultMutableTreeNode>();
        Map<String, NodeStack> forest = new HashMap<String, NodeStack>();

        int lineCount = 0;
        long startTime = System.currentTimeMillis();
        while (true) {
            String line = r.readLine();
            if (line == null)
                break;
            MethodData md = parseLine(line);
            lineCount++;
            if (PROGRESS && (lineCount % 1000 == 0)) {
                System.out.println("processed " + lineCount + " lines in " + (System.currentTimeMillis() - startTime));
            }
            if (DEBUG) {
                System.out.print("line " + lineCount + ", " + md.ttype + " ");
                switch (md.ttype) {
                    case StartTime:
                        System.out.println(traceStartTime);
                        break;
                    case Entry:
                        System.out.println(", d " + md.depth +
                                        ", [t " + md.entryTimeInfo.wallTime +
                                        ", u " + md.entryTimeInfo.userUsage +
                                        ", s " + md.entryTimeInfo.sysUsage + "] " +
                                        md.thread + ", " +
                                        md.methodName +
                                        (md.params==null ? "" : ("(" + md.params + ")")));
                        break;
                    case Return:
                        System.out.println(", d " + md.depth +
                                        ", [t " +  md.exitTimeInfo.wallTime + ", " +
                                        ", u " +  md.exitTimeInfo.userUsage + ", " +
                                        ", s " +  md.exitTimeInfo.sysUsage + "], " +
                                        md.thread + ", " +
                                        md.methodName +
                                        (md.params==null ? "" : ("(" + md.params + ")")));
                        break;
                    case DefineThread:
                    case DefineMethod:
                        System.out.println(md.methodName + " " + md.thread);
                        break;
                }
            }

            NodeStack ns = forest.get(md.thread);
            switch (md.ttype) {
                case Entry:
                    DefaultMutableTreeNode nnode = new DefaultMutableTreeNode(md);
                    nodeCount++;
                    ns.nodes.get(md.depth - 1).add(nnode);
                    if (md.depth > ns.depth) {
                        if (md.depth >= ns.nodes.size())
                            ns.nodes.add(null);
                    } else if (md.depth < ns.depth) {
                    } else {
                    }
                    ns.nodes.set(md.depth, nnode);
                    ns.depth = md.depth;
                    break;

                case Return:
                    DefaultMutableTreeNode parent = ns.nodes.get(md.depth - 1);
                    int count = parent.getChildCount();
                    boolean found = false;
                    for (int i = count - 1; i >= 0; i--) {
                        DefaultMutableTreeNode tn = (DefaultMutableTreeNode) parent.getChildAt(i);
                        MethodData cmd = (MethodData) tn.getUserObject();
                        if (cmd.methodName.equals(md.methodName)) {
                            cmd.exitTimeInfo = md.exitTimeInfo;
                            if (md.params != null)
                                cmd.result = mungeResult(md.params);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.err.println("line " + lineCount + " failed to find return for " + md.methodName);
                    }
                    break;

                case DefineThread:
                    ns = new NodeStack();
                    ns.nodes.add(new DefaultMutableTreeNode(md));
                    forest.put(md.thread, ns);
                    break;

                case DefineMethod:
            }
        }
        for (Map.Entry<String, NodeStack> me : forest.entrySet()) {
            result.put(me.getKey(), me.getValue().nodes.get(0));
        }
        return result;
    }

    /**
     * If results is a shortForm transform it
     */
    private String mungeResult(String r) {
        if (r.charAt(0) == 'A') {
            return paramMap.get(r);
        } else
            return r;
    }

    enum TraceType {
        Entry, Return, DefineThread, DefineMethod, DefineParam, StartTime;
    }

    Map<String,String> threadMap = new HashMap<String,String>();
    Map<String,String> methodMap = new HashMap<String,String>();
    Map<String,String> paramMap = new HashMap<String,String>();

    ArrayList<MethodData> forwardRefs = new ArrayList<MethodData>();

    class MethodData {

        TraceType ttype;
        int depth;
        String thread;
        String methodName;
        String params;
        String thisArg;
        String result;
        TimeInfo entryTimeInfo;
        TimeInfo exitTimeInfo;

        MethodData(TraceType ttype, String thread, int depth, String methodName, String params, TimeInfo timeInfo) {
            this.ttype = ttype;
            this.thread = threadMap.get(thread);
            this.depth = depth;
            this.methodName = methodMap.get(methodName);
            if (this.methodName == null) {
                // forward reference in case where we have per-thread output
                this.methodName = methodName;
                forwardRefs.add(this);
            }
            this.params = params;
            // params includes the this arg, possible null for a static method call
            // separate it out here
            if (ttype == TraceType.Entry) {
                this.entryTimeInfo = timeInfo;
                if (params != null) {
                    int a1x = params.indexOf(',');
                    if (a1x < 0) { // no actual args
                        thisArg = paramMap.get(params);
                        this.params = null;
                    } else {
                        thisArg = paramMap.get(params.substring(0, a1x));
                        this.params = params.substring(a1x + 1);
                    }
                }
            } else if (ttype == TraceType.Return) {
                this.exitTimeInfo = timeInfo;
            }
        }

        /**
         * This variant is used for the DefineXXX variants. N.B. definitions do NOT always precede uses!
         *
         * @param ttype
         *            DefineXXX
         * @param realName
         *            the real (full) name of the thread, method, param
         * @param shortForm
         *            the short form that is used in the rest of the trace
         */
        MethodData(TraceType ttype, String realName, String shortForm) {
            this.ttype = ttype;
            this.thread = realName;
            this.methodName = shortForm;
            if (ttype == TraceType.DefineThread)
                threadMap.put(shortForm, realName);
            else if (ttype == TraceType.DefineParam)
                paramMap.put(shortForm, realName);
            else if (ttype == TraceType.DefineMethod) {
                methodMap.put(shortForm, realName);
                for (MethodData m : forwardRefs) {
                    if (shortForm.equals(m.methodName)) {
                        m.methodName = realName;
                    }
                }
            }
        }

        public String toString() {
            switch (ttype) {
                case DefineThread:
                    return thread + " stack root";

                case Entry:
                    String time = "";
                    if (entryTimeInfo.wallTime > 0) {
                        time = displayTime(entryTimeInfo, exitTimeInfo, TimeInfo.Type.WallTime) + " ";
                        time = time + displayTime(entryTimeInfo, exitTimeInfo, TimeInfo.Type.UserUsage) + " ";
                        time = time + displayTime(entryTimeInfo, exitTimeInfo, TimeInfo.Type.SysUsage) + " ";
                    }
                    String s = time + methodName;
                    if (params != null)
                        s += "(" + params + ")";
                    if (result != null)
                        s += " returned " + result;
                    return s;

                default:
                    return "unexpected type " + ttype;
            }
        }

        private String displayTime(TimeInfo startInfo, TimeInfo endInfo, TimeInfo.Type tt) {
            String result = null;
            long time = -1;
            // if endInfo == null the method did not return
            switch (tt) {
                case WallTime:
                    if (timeDisplay == TimeDisplay.WallRel) {
                        time = startInfo.wallTime - traceStartTime;
                    } else if (timeDisplay == TimeDisplay.WallAbs) {
                        time = startInfo.wallTime;
                    } else if (timeDisplay == TimeDisplay.Duration) {
                        if (endInfo != null) {
                            time = endInfo.wallTime - startInfo.wallTime;
                        }
                    }
                    break;
                case UserUsage:
                    if (timeDisplay == TimeDisplay.Duration) {
                        if (endInfo != null) {
                            time = endInfo.userUsage - startInfo.userUsage;
                        }
                    } else {
                        time = startInfo.userUsage;
                    }
                    break;
                case SysUsage:
                    if (timeDisplay == TimeDisplay.Duration) {
                        if (endInfo != null) {
                            time = endInfo.sysUsage - startInfo.sysUsage;
                        }
                    } else {
                        time = startInfo.sysUsage;
                    }
                    break;
            }
            if (time < 0) {
                // no data
                result = "?";
            } else {
                result = TimeFunctions.formatTime(time);
            }
            return result;
        }
    }

    static class TimeInfo {

        static interface Adder {

            long add(long sum, TimeInfo ti);
        }

        enum Type {
            WallTime, UserUsage, SysUsage
        };

        long wallTime;
        long userUsage;
        long sysUsage;

        TimeInfo() {
        }

        TimeInfo(long wallTime, long userUsage, long sysUsage) {
            this.wallTime = wallTime;
            this.userUsage = userUsage;
            this.sysUsage = sysUsage;
        }

        long get(Type t) {
            if (t == Type.WallTime)
                return wallTime;
            else if (t == Type.UserUsage)
                return userUsage;
            else
                return sysUsage;
        }

        void add(TimeInfo ti) {
            this.wallTime += ti.wallTime;
            this.userUsage += ti.userUsage;
            this.sysUsage += ti.sysUsage;
        }

        static class WallTimeAdder implements TimeInfo.Adder {

            // result = sum + ti.wallTime;
            public long add(long sum, TimeInfo ti) {
                return sum + ti.wallTime;
            }
        }

        static class UserUsageAdder implements TimeInfo.Adder {

            // result = sum + ti.userUsage;
            public long add(long sum, TimeInfo ti) {
                return sum + ti.userUsage;
            }
        }

        static class SysUsageAdder implements TimeInfo.Adder {

            // result = sum + ti.susUsage;
            public long add(long sum, TimeInfo ti) {
                return sum + ti.sysUsage;
            }
        }

        static class WallTimeComparator implements Comparator<TimeInfo> {

            public int compare(TimeInfo t1, TimeInfo t2) {
                if (t1.wallTime < t2.wallTime)
                    return -1;
                else if (t1.wallTime > t2.wallTime)
                    return +1;
                else
                    return 0;
            }
        }

        static class UserUsageComparator implements Comparator<TimeInfo> {

            public int compare(TimeInfo t1, TimeInfo t2) {
                if (t1.userUsage < t2.userUsage)
                    return -1;
                else if (t1.userUsage > t2.userUsage)
                    return +1;
                else
                    return 0;
            }
        }

        static class SysUsageComparator implements Comparator<TimeInfo> {

            public int compare(TimeInfo t1, TimeInfo t2) {
                if (t1.sysUsage < t2.sysUsage)
                    return -1;
                else if (t1.sysUsage > t2.sysUsage)
                    return +1;
                else
                    return 0;
            }
        }
    }

    static class TimeFunctions {

        static DecimalFormat format2d = new DecimalFormat("#.##");
        static DecimalFormat format3d = new DecimalFormat("#.###");
        static DecimalFormat format6d = new DecimalFormat("#.######");
        static DecimalFormat format9d = new DecimalFormat("#.#########");

        static String formatTime(long time) {
            String ds = null;
            switch (timeFormat) {
                case Milli:
                    ds = ftime(mtime(time), format6d) + "ms";
                    break;
                case Micro:
                    ds = ftime(utime(time), format3d) + "us";
                    break;
                case Sec:
                    ds = ftime(stime(time), format9d) + "s";
                    break;
                case Nano:
                    ds = Long.toString(time) + "ns";
            }
            return ds;
        }

        private static double utime(long time) {
            return (double) time / 1000;
        }

        private static double mtime(long time) {
            return (double) time / (1000 * 1000);
        }

        private static double stime(long time) {
            return (double) time / (1000 * 1000 * 1000);
        }

        static String ftime(double time, DecimalFormat f) {
            return f.format(time);
        }
    }

    Map<String, TimeInfo> lastTimeInfoMap = new HashMap<String, TimeInfo>();
    TimeInfo timeInfo = new TimeInfo();

    private MethodData parseLine(String line) throws Exception {
        // Format, four cases, [] optional
        // 0 S S t start time
        // 0 D TX name define short name SX for thread name
        // 0 M MX name define short name MX for method name
        // 0 P AX name define short name AX for arg/result name
        // d E[t] T M[( ... )] method M entry [at time t,u,s] in thread T, optional args
        // d R[t] M [ (result) ] method M return [at time t,u,s] with optional result
        // wall time is relative to start time

        int s1 = line.indexOf(' ');        // before E/R
        int s2 = line.indexOf(' ', s1+1);  // before T
        int s3 = line.indexOf(' ', s2+1);  // before M
        if (s1 < 0 || s2 < 0 || s3 < 0 ) throw new Exception("syntax error");

        int depth = Integer.parseInt(line.substring(0, s1));
        String ttype = line.substring(s1 + 1, s2);
        String thread = line.substring(s2 + 1, s3);
        String params = null;
        String methodName = null;

        int s4 = line.indexOf('(', s3 + 1);
        if (s4 > 0) {
            methodName = line.substring(s3 + 1, s4);
            params = line.substring(s4 + 1, line.length() - 1);
        } else {
            methodName = line.substring(s3 + 1);
        }

        if (depth == 0) {
            if (ttype.equals("D")) {
                lastTimeInfoMap.put(thread, new TimeInfo());
                return new MethodData(TraceType.DefineThread, methodName, thread);
            } else if (ttype.equals("M")) {
                return new MethodData(TraceType.DefineMethod, methodName, thread);
            } else if (ttype.equals("P")) {
                return new MethodData(TraceType.DefineParam, methodName, thread);
            } else if (ttype.equals("S")) {
                traceStartTime = Long.parseLong(methodName);
                return new MethodData(TraceType.StartTime, methodName, thread);
            } else {
                throw new Exception("non-D/M trace at depth 0");
            }
        } else {
            // E or R
            TraceType traceType = isReturn(ttype) ? TraceType.Return : TraceType.Entry;
            TimeInfo timeInfo = getTimeInfo(ttype);
            TimeInfo lastTimeInfo = lastTimeInfoMap.get(thread);
            if (timeInfo.userUsage == 0)
                timeInfo.userUsage = lastTimeInfo.userUsage;
            else
                lastTimeInfo.userUsage = timeInfo.userUsage;
            if (timeInfo.sysUsage == 0)
                timeInfo.sysUsage = lastTimeInfo.sysUsage;
            else
                lastTimeInfo.sysUsage = timeInfo.sysUsage;

            return new MethodData(traceType, thread, depth, methodName, params, timeInfo);
        }
    }

    private TimeInfo getTimeInfo(String t) {
        TimeInfo result = new TimeInfo();
        if (t.length() > 1) {
            int t1 = t.indexOf(',');
            int t2 = t.indexOf(',', t1 + 1);
            if (t1 > 0) {
                result.wallTime = Long.parseLong(t.substring(1, t1)) + traceStartTime;
                result.userUsage = Long.parseLong(t.substring(t1 + 1, t2));
                result.sysUsage = Long.parseLong(t.substring(t2 + 1));
            } else {
                result.wallTime = Long.parseLong(t.substring(1));
            }
        }
        return result;
    }

    private boolean isReturn(String token) {
        return token.length() > 0 && token.charAt(0) == 'R' && (token.length() == 1 || ('0' <= token.charAt(1) && token.charAt(1) <= '9'));
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be invoked from the event-dispatching thread.
     */
    private static void createAndShowGUI(String[] args) {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        new TraceAnalyzer(args);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        // ------------ File ---------------
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem openItem = new JMenuItem(new OpenAction());
        fileMenu.add(openItem);
        JMenuItem snapShotItem = new JMenuItem(new SnapShotAction());
        fileMenu.add(snapShotItem);
        fileMenu.addSeparator();
        JMenuItem propsItem = new JMenuItem(new PropsAction());
        fileMenu.add(propsItem);
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem(new ExitAction());
        fileMenu.add(exitItem);
        // ------------ Edit ---------------
        // JMenu edit = new JMenu("Edit");
        // menuBar.add(edit);
        // JMenuItem findItem = new JMenuItem(new FindAction());
        // edit.add(findItem);
        // ------------ Show ---------------
        JMenu showMenu = new JMenu("Show");
        ButtonGroup showGroup = new ButtonGroup();
        JRadioButtonMenuItem etaItem = new JRadioButtonMenuItem(new ElapsedTimeAction());
        JRadioButtonMenuItem absetaItem = new JRadioButtonMenuItem(new AbsElapsedTimeAction());
        JRadioButtonMenuItem durItem = new JRadioButtonMenuItem(new DurationTimeAction());
        showGroup.add(etaItem);
        showGroup.add(absetaItem);
        showGroup.add(durItem);
        durItem.setSelected(true);
        JMenu showTimeSubMenu = new JMenu("Time");
        showTimeSubMenu.add(etaItem);
        showTimeSubMenu.add(absetaItem);
        showTimeSubMenu.add(durItem);
        showMenu.add(showTimeSubMenu);
        showMenu.addSeparator();
        JMenuItem selItem = new JMenuItem(new ScrollSelAction());
        showMenu.add(selItem);
        menuBar.add(showMenu);
        // ------------ Format ---------------
        JMenu formatMenu = new JMenu("Format");
        JMenu timeSubMenu = new JMenu("Time");
        ButtonGroup timeGroup = new ButtonGroup();
        JRadioButtonMenuItem nanoItem = new JRadioButtonMenuItem(new TimeAction(TimeFormat.Nano));
        JRadioButtonMenuItem microItem = new JRadioButtonMenuItem(new TimeAction(TimeFormat.Micro));
        JRadioButtonMenuItem milliItem = new JRadioButtonMenuItem(new TimeAction(TimeFormat.Milli));
        JRadioButtonMenuItem secItem = new JRadioButtonMenuItem(new TimeAction(TimeFormat.Sec));
        switch (timeFormat) {
            case Nano: nanoItem.setSelected(true); break;
            case Micro: microItem.setSelected(true); break;
            case Milli: milliItem.setSelected(true); break;
            case Sec: secItem.setSelected(true); break;
        }

        timeGroup.add(nanoItem); timeGroup.add(microItem);
        timeGroup.add(milliItem); timeGroup.add(secItem);
        timeSubMenu.add(nanoItem); timeSubMenu.add(microItem);
        timeSubMenu.add(milliItem); timeSubMenu.add(secItem);
        formatMenu.add(timeSubMenu);
        menuBar.add(formatMenu);

        return menuBar;
    }

    public static void main(final String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI(args);
            }
        });
    }

    class OpenAction extends AbstractAction {

        public OpenAction() {
            super("Open", null);
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(myFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                new TraceAnalyzer(new String[] { "-f", file.getPath()});
            }
        }
    }

    class PropsAction extends AbstractAction {

        public PropsAction() {
            super("Properties", null);
        }

        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(myFrame, "Trace file path: " + traceFilePathName, "", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    static class ExitAction extends AbstractAction {

        public ExitAction() {
            super("Exit", null);
            // putValue(SHORT_DESCRIPTION, desc);
            // putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    static class CloseAction extends AbstractAction {

        JFrame frame;

        public CloseAction(JFrame frame) {
            super("Close", null);
            this.frame = frame;
            // putValue(SHORT_DESCRIPTION, desc);
            // putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
            frame.dispose();
        }
    }

    class TimeAction extends AbstractAction {

        TimeFormat tf;

        public TimeAction(TimeFormat tf) {
            super(tf.toString());
            this.tf = tf;
        }

        public void actionPerformed(ActionEvent e) {
            timeFormat = tf;
            repaintTrees();
        }
    }

    class FindAction extends AbstractAction {

        public FindAction() {
            super("Find");
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    class SnapShotAction extends AbstractAction {

        public SnapShotAction() {
            super("Snapshot");
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    class AbsElapsedTimeAction extends AbstractAction {

        public AbsElapsedTimeAction() {
            super("Wall Clock Absolute");
        }

        public void actionPerformed(ActionEvent e) {
            timeDisplay = TimeDisplay.WallAbs;
            repaintTrees();
        }
    }

    class ElapsedTimeAction extends AbstractAction {

        public ElapsedTimeAction() {
            super("Wall Clock Relative");
        }

        public void actionPerformed(ActionEvent e) {
            timeDisplay = TimeDisplay.WallRel;
            repaintTrees();
        }
    }

    class DurationTimeAction extends AbstractAction {

        public DurationTimeAction() {
            super("Duration");
        }

        public void actionPerformed(ActionEvent e) {
            timeDisplay = TimeDisplay.Duration;
            repaintTrees();
        }
    }

    class ScrollSelAction extends AbstractAction {

        public ScrollSelAction() {
            super("Selection");
        }

        public void actionPerformed(ActionEvent e) {
            TreePath tp = currentJTree.getSelectionPath();
            currentJTree.scrollPathToVisible(tp);
        }
    }

    void repaintTrees() {
        for (JTree tree : threadJTrees.values()) {
            tree.repaint();
        }
        for (JTree tree : matchJTrees) {
            tree.repaint();
        }
    }

    static interface SaveActionBody {

        public void doSave(PrintWriter pw);
    }

    static class SaveAction extends AbstractAction {

        JFrame frame;
        SaveActionBody body;

        public SaveAction(JFrame frame, SaveActionBody body) {
            super("Save", null);
            this.frame = frame;
            this.body = body;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (file.exists()) {
                    int r = JOptionPane.showConfirmDialog(frame, "File exists, ok to overwrite?", "File exists", JOptionPane.YES_NO_OPTION);
                    if (r == JOptionPane.NO_OPTION)
                        return;
                }
                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(new FileWriter(file));
                    body.doSave(pw);
                    JOptionPane.showMessageDialog(frame, "Data saved", "", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "error writing file", "", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (pw != null)
                        pw.close();
                }
            }
        }
    }

}
