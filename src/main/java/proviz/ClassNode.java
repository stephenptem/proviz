package proviz;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * Created by brandonpickup on 2016/09/02.
 */
public class ClassNode extends Node
{
    public ClassNode(String label, OWLClass owlClass)
    {
        super(label, owlClass);
    }
}
