package proviz;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author Stephen Temple
 * @version 2016.09.19
 */
public class ProVizNode {
    private OWLEntity entity;

    public ProVizNode(OWLEntity entity) {
        this.entity = entity;
    }

    public OWLClass getOwlClass() {
        if (entity.isOWLClass()) {
            return entity.asOWLClass();
        } else {
            return null;
        }
    }

    public OWLNamedIndividual getInstance() {
        if (entity.isOWLNamedIndividual()) {
            return entity.asOWLNamedIndividual();
        } else {
            return null;
        }
    }

    public String toString() {
        return entity.getIRI().getFragment();
    }
}
