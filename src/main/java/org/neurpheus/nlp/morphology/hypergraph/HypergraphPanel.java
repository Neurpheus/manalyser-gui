/*
 * HypergraphPanel.java
 *
 * Created on 9 styczeń 2006, 11:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.neurpheus.nlp.morphology.hypergraph;
import hypergraph.graphApi.AttributeManager;
import hypergraph.graphApi.Edge;
import hypergraph.graphApi.Element;
import hypergraph.graphApi.Graph;
import hypergraph.graphApi.Node;
import hypergraph.hyperbolic.PropertyManager;
import hypergraph.visualnet.GraphLayoutModel;
import hypergraph.visualnet.GraphPanel;
import hypergraph.visualnet.NodeRenderer;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author kuba
 */
public class HypergraphPanel extends GraphPanel {
    
    private InflectionHypergraphBuilder builder;
    private String pattern;
    
    /**
     * Creates a new instance of HypergraphPanel
     */
    public HypergraphPanel(Graph graph, InflectionHypergraphBuilder builder) {
        super(graph);
        this.builder = builder;
        getEdgeRenderer().setLabelVisible(true);
        PropertyManager pm = getPropertyManager();
        pm.setProperty("hypergraph.hyperbolic.text.size1", new Double(22));
        pm.setProperty("hypergraph.hyperbolic.text.size2", new Double(18));
        pm.setProperty("hypergraph.hyperbolic.text.size3", new Double(12));
        pm.setProperty("hypergraph.hyperbolic.text.size4", new Double(0));
        pm.setProperty("visualnet.layout.expandingEnabled", "true");
        expandNode((Node) graph.getElement("r1"));
        setLogo(null);
        setSmallLogo(null);
        pattern = "";
        
    }

    
    public void expandNode(Node node) {
        builder.expandNode(getGraph(), node, 1);
        super.expandNode(node);
        getGraphLayout().layout();
        centerNode(node);
    }

    public void mouseClicked(MouseEvent e) {
        Point point = e.getPoint();
        Element element = getElement(point);
        if (element != null && element.getElementType() == Element.NODE_ELEMENT) {
            Node node = (Node) element;
            boolean hasChildren = ((Boolean) getGraph().getAttributeManager().getAttribute("hasChildren", node)).booleanValue();
            if (hasChildren) {
                // check expander
                GraphLayoutModel glm = getGraphLayout().getGraphLayoutModel();
                synchronized (glm) {
                    NodeRenderer nr = getNodeRenderer();
                    nr.configure(this, glm.getNodePosition(node), node);
                    Component c = nr.getComponent();
                    if (point.getX() - c.getX() > c.getWidth() - 10 && point.getY() - c.getY() < 10) {
                        if (isExpanded(node)) {
                            shrinkNode(node);
                        } else {
                            expandNode(node);
                        }
                    } else {
                        centerNode(node);
                    }
                }
            }
        } else {
            // super.mouseClicked(e);
        }
    }

    public boolean hasExpander(Node node){
        AttributeManager amgr = getGraphLayout().getGraph().getAttributeManager();
        boolean hasChildren = ((Boolean) getGraph().getAttributeManager().getAttribute("hasChildren", node)).booleanValue();
        return hasChildren;
    }
    
    public void shrinkNode(Node node) {
        AttributeManager amgr = getGraphLayout().getGraph().getAttributeManager();
        // get all outgoing edges in the original (!) graph.
        @SuppressWarnings("unchecked")
        ArrayList tmp = new ArrayList(getGraph().getEdges(node));
        Iterator iter = tmp.iterator();
        while (iter.hasNext()) {
                Edge edge = (Edge) iter.next();
                if (edge.getSource() != node)
                        continue; // only outgoing edges
                Node target = edge.getOtherNode(node);
                getGraphLayout().getGraph().removeElement(target);
        }
        amgr.setAttribute(NODE_EXPANDED, node, expandAction);
        amgr.setAttribute("wasExpanded", node, Boolean.FALSE);
        getGraphLayout().layout();
        centerNode(node);
    }


    public InflectionHypergraphBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(InflectionHypergraphBuilder builder) {
        this.builder = builder;
    }
    
    
    public String getPattern() {
        return pattern;
    }

    
    public void centerNode(Node node) {
        if (node != null) {
            AttributeManager amgr = getGraphLayout().getGraph().getAttributeManager();
            String p = (String) amgr.getAttribute("pattern", node);
            pattern = p == null ? "" : p;
        }
        super.centerNode(node);
    }
    
    
    public void paint(Graphics g) {
        super.paint(g);
        g.drawString(pattern, 2, 20);
    }
	    
}
