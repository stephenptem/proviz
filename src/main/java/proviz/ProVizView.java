package proviz;

import java.awt.*;
import java.util.Set;

import javax.swing.*;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.*;
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
import org.semanticweb.owlapi.reasoner.OWLReasoner;


/**
 * This class handles the set up and actions contained within the ProViz view.
 */
public class ProVizView extends AbstractOWLViewComponent {

    // Debugging
	private Logger logger = Logger.getLogger(ProVizView.class);

    // JUNG vars
    private DelegateTree<ProVizNode, ProVizEdge> tree;
    private VisualizationViewer<ProVizNode, ProVizEdge> viewer;

    // Swing vars
    private JSpinner depthSpinner;

    // OWL vars
	private OWLSelectionModel selectionModel;
	private OWLSelectionModelListener listener = new OWLSelectionModelListener() {
		@Override
		public void selectionChanged() throws Exception {
			OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
			updateView(entity);
		}
	};

    /**
     * Creates the view panel on the initial load of the tab.
     * Sets up the layout manager, the menu bar, and calls to build the graph visualization.
     */
    @Override
	protected void initialiseOWLView() throws Exception {
        logger.info("Initializing ProVizView");

        // Set up display
		setLayout(new BorderLayout());
        JPanel menu = new JPanel(new FlowLayout());

        menu.add(new JLabel("Tree depth:"));

        SpinnerModel model = new SpinnerNumberModel(-1, -1, 1000, 1);
        depthSpinner = new JSpinner(model);
        depthSpinner.setToolTipText("Use -1 to not restrict depth of tree. Click a class on the left to refresh.");
        menu.add(depthSpinner);

        add(menu, BorderLayout.NORTH);

        // Selection listener
		selectionModel = getOWLWorkspace().getOWLSelectionModel();
		selectionModel.addListener(listener);

        // Get the reasoner
        OWLReasoner reasoner = getOWLModelManager().getReasoner();

        // Get root node
        ProVizNode root = new ProVizNode(reasoner.getTopClassNode().getRepresentativeElement());

        // Visualize
        view(root);
	}

    /**
     * Handles the disposal of the view.
     */
	@Override
	protected void disposeOWLView() {
		selectionModel.removeListener(listener);
	}

    /**
     * Handles the click events on OWL classes on the left panel.
     * @param e Class that was clicked in the hierarchy
     */
	private void updateView(OWLEntity e) {
        // If selected entity is an OWL class then rebuild the tree from selected node down
		if (e.isOWLClass()) {
            // Add root node
            ProVizNode root = new ProVizNode(e.asOWLClass());

            // Visualize
            view(root);
        }
	}

    /**
     * Displays the visualization using JUNG tree layout.
     * @param root the root of the ontology encapsulated in a ProVizNode
     */
	private void view(ProVizNode root) {
        // Graph<V, E> where V is the type of the vertices and E is the type of the edges
        tree = new DelegateTree<ProVizNode, ProVizEdge>();
        tree.setRoot(root);

        // Recursively add edges
        addAllEdges(root);

        // Create the layout for the graph
        TreeLayout<ProVizNode, ProVizEdge> layout = new TreeLayout<ProVizNode, ProVizEdge>(tree);

        // Remove the old visualization from the display if there is one
        try {
            remove(viewer);
        } catch (NullPointerException ignored) {}

        // Visualizes the graph
        viewer = new VisualizationViewer<ProVizNode, ProVizEdge>(layout);
        // White background
        viewer.setBackground(Color.WHITE);
        // Add labels to nodes
        viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        // Use straight lines for edges
        viewer.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(tree));
        // Add mouse controls
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        viewer.setGraphMouse(gm);

        add(viewer);
    }

    /**
     * Recursively adds edges between a supplied class and all its subclasses and all instances.
     * @param node the current node in the ontology that is being processed by the recursion
     */
    private void addAllEdges(ProVizNode node) {
        // Get the reasoner
        OWLReasoner reasoner = getOWLModelManager().getReasoner();

        // Continue only if depth set at -1, or if current tree depth is less than set depth
        if (depthSpinner.getValue().equals(-1) || tree.getDepth(node) < (Integer)depthSpinner.getValue()) {

            // Add instances
            Set<OWLNamedIndividual> instances = reasoner.getInstances(node.getOwlClass(), true).getFlattened();
            addInstances(instances, node);

            // Add edges for subclasses with instances
            Set<OWLClass> subClasses = reasoner.getSubClasses(node.getOwlClass(), true).getFlattened();

            if (subClasses.isEmpty()){return;}

            // Add subclass relationships
            for (OWLClass childClass : subClasses) {

                // Only add edge if the subclass isn't owl:Nothing and is satisfiable
                if (!childClass.getIRI().getFragment().equals("Nothing") && reasoner.isSatisfiable(childClass)) {
                    ProVizNode childNode = new ProVizNode(childClass);
                    // Add child node & edge
                    tree.addChild(new ProVizEdge(node, childNode), node, childNode);
                    addAllEdges(childNode);
                }

            }

        }
    }

    /**
     * Returns true if the supplied class, or any of its children has any instances, and false otherwise.
     * @param owlClass the OWLClass to be checked for instances
     * @return boolean - whether or not the class or its children has instances
     */
    private boolean hasInstances(OWLClass owlClass){
        // Get the reasoner
        OWLReasoner reasoner = getOWLModelManager().getReasoner();

        Set<OWLNamedIndividual> instances = reasoner.getInstances(owlClass, true).getFlattened();
        if (!instances.isEmpty()){
            return true;}
        else{
            Set<OWLClass> subClasses = reasoner.getSubClasses(owlClass, true).getFlattened();
            // Check subclasses for instances
            for (OWLClass childClass : subClasses) {
                // Only check if the subclass isn't owl:Nothing
                if (!childClass.getIRI().getFragment().equals("Nothing") && hasInstances(childClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds edges between the supplied node, and all its instances.
     * @param instances Set of instances to be added as children to the node
     * @param node the node to add the instances too
     */
    private void addInstances(Set<OWLNamedIndividual> instances, ProVizNode node){
        // Get the reasoner
        OWLReasoner reasoner = getOWLModelManager().getReasoner();

        if (!instances.isEmpty()){
            for (OWLNamedIndividual instance : instances){
                // Only add edge if the instance isn't owl:Nothing
                if (!instance.getIRI().getFragment().equals("Nothing")){
                    ProVizNode instanceNode = new ProVizNode(instance);
                    // Add instance & edge
                    tree.addChild(new ProVizEdge(node, instanceNode), node, instanceNode);
                }
            }
        }
    }
}
