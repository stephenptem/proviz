package proviz;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.*;

import java.awt.*;

/**
 * Created by brandonpickup on 2016/09/01.
 */
public class ProVizViewJung extends AbstractOWLViewComponent
{
    // Debugging
    private Logger logger = Logger.getLogger(ProVizViewJung.class);

    //
    private OWLOntology ontology;
    private edu.uci.ics.jung.graph.Graph<Node, Edge> graph = new SparseGraph<Node, Edge>();
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

        for (OWLClass currentClass : ontology.getClassesInSignature(true))
        {
            if (currentClass.isOWLThing())
            {
                //For owl:Thing
                root = new ThingNode(currentClass.getIRI().getFragment(),currentClass);
                graph.addVertex(root);
            }
            else
            {
                //normal nodes
                ClassNode node = new ClassNode(currentClass.getIRI().getFragment(), currentClass);
                graph.addVertex(node);
//                if (currentClass.getSuperClasses(ontology).isEmpty())
//                {
//                    // If node is a parent node
//                    graph.addEdge(new SubClassEdge(), root, node);
//                }
            }
        }
//
//        for (OWLAxiom axiom : ontology.getAxioms()) {
//
//            if (axiom instanceof OWLSubClassOfAxiom) {
//                int subClassID = -1;
//                int superClassID = -1;
//
//                // SubClass
//                if (((OWLSubClassOfAxiom) axiom).getSubClass() instanceof OWLClass )
//                {
//                    subClassID = classes.get(((OWLSubClassOfAxiom) axiom).getSubClass().asOWLClass().getIRI().getFragment());
//                }
//
//                // SuperClass
//                if (((OWLSubClassOfAxiom) axiom).getSuperClass() instanceof OWLClass ) {
//                    superClassID = classes.get(((OWLSubClassOfAxiom) axiom).getSuperClass().asOWLClass().getIRI().getFragment());
//                }
//
//                // Only add edge if subclass doesn't already have a parent
//                if (tree.getNode(subClassID).getParent() == null) {
//                    // Add edge
//                    if (subClassID >= 0 && superClassID >= 0) {
//                        tree.addEdge(superClassID, subClassID);
//                        logger.info("Edge: super=" + superClassID + " sub=" + subClassID);
//                    }
//                }
//            }
//
//        }
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
        Layout<Node, Edge> layout = new CircleLayout<Node, Edge>(graph);
        layout.setSize(new Dimension(300,300)); // sets the initial size of the space
        // The BasicVisualizationServer<V,E> is parameterized by the edge types
        VisualizationViewer<Node, Edge> vv = new VisualizationViewer<Node, Edge>(layout);
        vv.setBackground(Color.WHITE);
        vv.setPreferredSize(new Dimension(350,350)); //Sets the viewing area size

        setLayout(new BorderLayout());
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        add(vv);


    }

    @Override
    protected void disposeOWLView()
    {

    }
}
