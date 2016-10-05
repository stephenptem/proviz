package proviz;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * A container class for an edge to be added to the tree.
 * Sets a parent and a child node of the edge.
 */
public class ProVizEdge {
    private ProVizNode parent;
    private ProVizNode child;

    /**
     * Constructs an edge from the parent and the child nodes.
     * @param parent parent ProVizNode object
     * @param child child ProVizNode object
     */
    public ProVizEdge(ProVizNode parent, ProVizNode child) {
        this.parent = parent;
        this.child = child;
    }
}
