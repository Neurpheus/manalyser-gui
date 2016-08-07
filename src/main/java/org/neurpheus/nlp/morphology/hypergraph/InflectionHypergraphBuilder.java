/*
 * InflectionHypergraphBuilder.java
 *
 * Created on 9 styczeń 2006, 15:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.neurpheus.nlp.morphology.hypergraph;

import hypergraph.graphApi.AttributeManager;
import hypergraph.graphApi.Edge;
import hypergraph.graphApi.Graph;
import hypergraph.graphApi.GraphException;
import hypergraph.graphApi.GraphSystem;
import hypergraph.graphApi.GraphSystemFactory;
import hypergraph.graphApi.Group;
import hypergraph.graphApi.Node;
import hypergraph.visualnet.GraphPanel;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeLeaf;
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.nlp.morphology.*;
import org.neurpheus.nlp.morphology.inflection.InflectionPatternsMap;

/**
 *
 * @author kuba
 */
public class InflectionHypergraphBuilder {
    
    private Group rootGroup;
    private Group nodeGroup;
    private Group ipGroup;
    private Group edgeGroup;
    private Group edge2ipGroup;
    private Node centerNode;
    private Tree tree;
    private InflectionPatternsMap ipm;
    private long idCounter = 1;
    
    public InflectionHypergraphBuilder(Tree tree, InflectionPatternsMap ipm) {
        this.tree = tree;
        this.ipm = ipm;
    }
    
    public Graph initGraph() {
        GraphSystem graphSystem = null;
        try {
                graphSystem = GraphSystemFactory.createGraphSystem("hypergraph.graph.GraphSystemImpl",null); 
        } catch (Exception e) {
                e.printStackTrace();
        }
        
        centerNode = null;
        
        Graph graph = graphSystem.createGraph();
        
        rootGroup = graph.createGroup();
        nodeGroup = graph.createGroup();
        ipGroup = graph.createGroup();
        edgeGroup = graph.createGroup();
        edge2ipGroup = graph.createGroup();

        
        AttributeManager aManager = graph.getAttributeManager();
        
        
        aManager.setAttribute(GraphPanel.NODE_ICON, rootGroup, null);
        aManager.setAttribute(GraphPanel.NODE_FOREGROUND, rootGroup, Color.WHITE);
        aManager.setAttribute(GraphPanel.NODE_BACKGROUND, rootGroup, new Color(0,0,128));
        
        aManager.setAttribute(GraphPanel.NODE_ICON, nodeGroup, null);
        aManager.setAttribute(GraphPanel.NODE_FOREGROUND, nodeGroup, Color.BLACK);
        aManager.setAttribute(GraphPanel.NODE_BACKGROUND, nodeGroup, Color.WHITE);
         
        aManager.setAttribute(GraphPanel.NODE_ICON, ipGroup, null);
        aManager.setAttribute(GraphPanel.NODE_FOREGROUND, ipGroup, Color.YELLOW);
        aManager.setAttribute(GraphPanel.NODE_BACKGROUND, ipGroup, new Color(0,0, 128));
        
        
        
        aManager.setAttribute(GraphPanel.EDGE_LINECOLOR, edgeGroup, Color.BLACK);
        aManager.setAttribute(GraphPanel.EDGE_TEXTCOLOR, edgeGroup, Color.BLACK);

        aManager.setAttribute(GraphPanel.EDGE_LINECOLOR, edge2ipGroup, new Color(0,0,128));
        aManager.setAttribute(GraphPanel.EDGE_TEXTCOLOR, edge2ipGroup, new Color(0,0,128));
        aManager.setAttribute(GraphPanel.EDGE_STROKE, edge2ipGroup, new float[] {1, 2});

        
        return graph;
    }
    
    public Graph getTestGraph() {
        return getGraph();
    }
    
    public void expandNode(Graph graph, Node node, int expandLevel) {
        AttributeManager attributeManager = graph.getAttributeManager();
        Boolean wasExpanded = (Boolean) attributeManager.getAttribute("wasExpanded", node);
        if (!wasExpanded.booleanValue()) {
            attributeManager.setAttribute("wasExpanded", node, Boolean.TRUE);
            TreeNode treeNode = (TreeNode) attributeManager.getAttribute("treeNode", node);
            String pattern = (String) attributeManager.getAttribute("pattern", node);
            if (treeNode != null) {
                if (treeNode.isLeaf()) {
                    int ipaIndex = ((Integer) ((TreeLeaf) treeNode).getData()).intValue();
                    ExtendedInflectionPattern[] ipa = ipm.get(ipaIndex);
                    for (int i = 0; i < ipa.length; i++) {
                        String label = Integer.toString(ipa[i].getId());
                        try {
                            String id = "ip" + Long.toString(idCounter++);
                            Node childNode = graph.createNode(id);
                            childNode.setGroup(ipGroup);
                            childNode.setLabel(label);
                            attributeManager.setAttribute("treeNode", childNode, ipa[i]);
                            attributeManager.setAttribute("wasExpanded", childNode, Boolean.TRUE);
                            attributeManager.setAttribute("hasChildren", childNode, Boolean.FALSE);
                            attributeManager.setAttribute(GraphPanel.NODE_EXPANDED, childNode, null);
                            attributeManager.setAttribute("pattern", childNode, pattern + " -> " + id);
                            Edge e = graph.createEdge(node, childNode);
                            e.setGroup(edge2ipGroup);
                        } catch (GraphException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    
                }
                List children = treeNode.getChildren();
                for (final Iterator it = children.iterator(); it.hasNext();) {
                    TreeNode child = (TreeNode) it.next();
                    String label = child.getValue() == null ? "null" : "" + (char) ((Integer) child.getValue()).intValue();
                    try {
                        String id = "n" + Long.toString(idCounter++);
                        Node childNode = graph.createNode(id);
                        childNode.setGroup(nodeGroup);
                        childNode.setLabel(label);
                        attributeManager.setAttribute("treeNode", childNode, child); 
                        boolean hasChildren = child.getNumberOfChildren() > 0 || child.isLeaf();
                        attributeManager.setAttribute("wasExpanded", childNode, hasChildren ? Boolean.FALSE : Boolean.TRUE);
                        attributeManager.setAttribute("hasChildren", childNode, hasChildren ? Boolean.TRUE : Boolean.FALSE);
                        int pos = pattern.indexOf('*');
                        if (pos >= 0) {
                            attributeManager.setAttribute("pattern", childNode, pattern.substring(0, pos) + label + pattern.substring(pos));
                        } else {
                            attributeManager.setAttribute("pattern", childNode, label + pattern);
                        }
                        Edge e = graph.createEdge(node, childNode);
                        //e.setLabel(label);
                        e.setGroup(edgeGroup);
                    } catch (GraphException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public Graph getGraph() {
        Graph graph = initGraph();
        try {
            TreeNode root = tree.getRoot();
            String id = "r" + Long.toString(idCounter++);
            centerNode = graph.createNode(id);
            centerNode.setGroup(rootGroup);
            centerNode.setLabel("ROOT");
            graph.getAttributeManager().setAttribute("treeNode", centerNode, root);
            graph.getAttributeManager().setAttribute("hasChildren", centerNode, Boolean.TRUE);
            graph.getAttributeManager().setAttribute("wasExpanded", centerNode, Boolean.FALSE);
            graph.getAttributeManager().setAttribute("pattern", centerNode, "");
            // expandNode(graph, centerNode, 2);
        } catch (GraphException e) {
            e.printStackTrace();
        }
        return graph;
    }
    
    public Node getCenterNode() {
        return this.centerNode;
    }


    
}
