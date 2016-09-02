package proviz;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Created by brandonpickup on 2016/09/02.
 */
public class SubClassEdge extends Edge
{
    public SubClassEdge()
    {
        super();
    }

    public void setOWLAxiom (OWLAxiom axiom)
    {
        this.axiom = axiom;
    }

    public OWLAxiom getOWLAxiom()
    {
        return axiom;
    }
}
