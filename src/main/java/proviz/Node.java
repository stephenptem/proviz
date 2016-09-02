package proviz;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * Created by brandonpickup on 2016/09/02.
 */
public abstract class Node
{
    protected String label;
    protected OWLClass owlClass;

    public Node(String label, OWLClass owlClass)
    {
        this.label = label;
        this.owlClass = owlClass;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void setOWLClass (OWLClass owlClass)
    {
        this.owlClass = owlClass;
    }

    public OWLClass getOWLClass()
    {
        return owlClass;
    }

    public String toString()
    {
        return label;
    }
}
