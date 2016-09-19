package proviz;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Stephen Temple
 * @version 2016.09.19
 */
public class ProVizEdge {
    private ProVizNode parent;
    private ProVizNode child;

    public ProVizEdge(ProVizNode parent, ProVizNode child) {
        this.parent = parent;
        this.child = child;
    }
}
