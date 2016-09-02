package proviz;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Created by brandonpickup on 2016/09/02.
 */
public abstract class Edge
{
    OWLAxiom axiom;

    public Edge()
    {
    }

    public OWLAxiom getAxiom()
    {
        return axiom;
    }

    public void setOWLAxiom(OWLAxiom axiom)
    {
        this.axiom = axiom;
    }
}
