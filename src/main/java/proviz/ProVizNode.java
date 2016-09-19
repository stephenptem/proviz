package proviz;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Stephen Temple
 * @version 2016.09.19
 */
public class ProVizNode {
    private OWLClass owlClass;

    public ProVizNode(OWLClass owlClass) {
        this.owlClass = owlClass;
    }

    public OWLClass getOwlClass() {
        return owlClass;
    }

    public String toString() {
        return owlClass.getIRI().getFragment();
    }
}
