package proviz;

import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.*;

import java.awt.*;
import java.util.Hashtable;

/**
 * Created by brandonpickup on 2016/09/01.
 */
public class ProVizViewJung extends AbstractOWLViewComponent
{
    // Debugging
    private Logger logger = Logger.getLogger(ProVizViewJung.class);

    //
    private OWLOntology ontology;
    private edu.uci.ics.jung.graph.Graph<Node, Edge> graph = new DirectedSparseGraph<Node, Edge>();
    private Node root;

    // OWL vars
    private OWLSelectionModel selectionModel;
    private OWLSelectionModelListener listener = new OWLSelectionModelListener()
    {
        @Override
        public void selectionChanged() throws Exception
        {
            OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
            updateView(entity);
        }
    };


    private void setupData()
    {
        logger.info("Data set up");


        ontology = getOWLModelManager().getActiveOntology();
        //IRI as the String, Node as the second item
        Hashtable<IRI, Node> classes = new Hashtable<IRI, Node>();

        for (OWLClass currentClass : ontology.getClassesInSignature(true))
        {
            if (currentClass.isOWLThing())
            {
                //For owl:Thing
                root = new ThingNode(currentClass.getIRI().getFragment(),currentClass);
                graph.addVertex(root);
                classes.put(currentClass.getIRI(), root);
            }
            else
            {
                //normal nodes
                ClassNode node = new ClassNode(currentClass.getIRI().getFragment(), currentClass);
                graph.addVertex(node);
                classes.put(currentClass.getIRI(), node);
//                if (currentClass.getSuperClasses(ontology).isEmpty())
//                {
//                    // If node is a parent node
//                    graph.addEdge(new SubClassEdge(), root, node);
//                }
            }
        }

        for (OWLAxiom axiom : ontology.getAxioms()) {

            if (axiom instanceof OWLSubClassOfAxiom)
            {
                Node subClass= null;
                Node superClass = null;

                // SubClass
                if (((OWLSubClassOfAxiom) axiom).getSubClass() instanceof OWLClass )
                {
                    subClass = classes.get(((OWLSubClassOfAxiom) axiom).getSubClass().asOWLClass().getIRI());
                }

                // SuperClass
                if (((OWLSubClassOfAxiom) axiom).getSuperClass() instanceof OWLClass ) {
                    superClass = classes.get(((OWLSubClassOfAxiom) axiom).getSuperClass().asOWLClass().getIRI());
                }

                // Add edge
                 if (subClass != null && superClass != null)
                 {
                     graph.addEdge(new SubClassEdge(), superClass, subClass);
                 }

            }

        }
    }

    private void updateView(OWLEntity e)
    {
        logger.info("Updating View");


    }

    @Override
    protected void initialiseOWLView() throws Exception
    {
        logger.info("Initializing prefuse example view");
        selectionModel = getOWLWorkspace().getOWLSelectionModel();
        selectionModel.addListener(listener);

        setupData();
        // The Layout<V, E> is parameterized by the vertex and edge types
        DAGLayout<Node, Edge> layout = new DAGLayout<Node, Edge>(graph);
        layout.setSize(new Dimension(900,900)); // sets the initial size of the space
        // The BasicVisualizationServer<V,E> is parameterized by the edge types

        VisualizationViewer<Node, Edge> vv = new VisualizationViewer<Node, Edge>(layout);
        vv.setBackground(Color.WHITE);
        //vv.setPreferredSize(new Dimension(350,350)); //Sets the viewing area size

        setLayout(new BorderLayout());
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());

        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));

        add(vv);


    }

    @Override
    protected void disposeOWLView()
    {

    }
}
