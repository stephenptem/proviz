package proviz;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author Stephen Temple
 * @version 2016.09.19
 */
public class ProVizNode {
    private OWLClass owlClass;
    private OWLNamedIndividual instance;


    public ProVizNode(OWLClass owlClass) {
        this.owlClass = owlClass;
    }
    public ProVizNode(OWLNamedIndividual instance) {
        this.instance = instance;
    }

    public OWLClass getOwlClass() {
        return owlClass;
    }
    public OWLNamedIndividual getInstance() {
        return instance;
    }

    public String toString() {
        if (owlClass == null && instance != null){
            return instance.getIRI().getFragment();
        }
        else if (owlClass != null && instance == null){
            return owlClass.getIRI().getFragment();
        }
        else{
            return null;
        }
    }
}
