package proviz;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * A container class for a node to be added to the tree.
 * Includes an OWLEntity which is a super class of an OWLNamedInstance and an OWLClass.
 */
public class ProVizNode {
    private OWLEntity entity;

    /**
     * Constructs the node using the given OWLEntity.
     * @param entity either an OWLClass or OWLNamedInstance
     */
    public ProVizNode(OWLEntity entity) {
        this.entity = entity;
    }

    /**
     * If the entity is an OWLClass returns the OWLClass.
     * @return OWLEntity converted to an OWLClass object
     */
    public OWLClass getOwlClass() {
        if (entity.isOWLClass()) {
            return entity.asOWLClass();
        } else {
            return null;
        }
    }

    /**
     * If the entity is an OWLNamedIndividual returns the OWLNamedIndividual.
     * @return OWLEntity converted to an OWLNamedIndividual object
     */
    public OWLNamedIndividual getInstance() {
        if (entity.isOWLNamedIndividual()) {
            return entity.asOWLNamedIndividual();
        } else {
            return null;
        }
    }

    /**
     * Returns the name of the entity from the IRI fragment.
     * @return the IRI fragment string
     */
    public String toString() {
        return entity.getIRI().getFragment();
    }
}
