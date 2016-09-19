package proviz;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.*;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.Layer;
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
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * 
 * This plugin displays the visualization
 *
 * @author stephen
 *
 */
public class ProVizView extends AbstractOWLViewComponent {

    // Debugging
	private Logger logger = Logger.getLogger(ProVizView.class);

    // Swing vars
	private JLabel label;
    private JPanel panel;
    private JButton but3 = null;
    private JPanel leftPanel = null, rightPanel = null;

    // JUNG vars
    private DelegateTree<ProVizNode, ProVizEdge> g;

    // OWL vars
    private OWLReasoner reasoner;
	private OWLSelectionModel selectionModel;
	private OWLSelectionModelListener listener = new OWLSelectionModelListener() {
		@Override
		public void selectionChanged() throws Exception {
			OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
			updateView(entity);
		}
	};
	
	@Override
	protected void initialiseOWLView() throws Exception {
        logger.info("Initializing test view");

		setLayout(new BorderLayout());

		selectionModel = getOWLWorkspace().getOWLSelectionModel();
		selectionModel.addListener(listener);

        // Graph<V, E> where V is the type of the vertices and E is the type of the edges
        g = new DelegateTree<ProVizNode, ProVizEdge>();

        // Add some vertices. From above we defined these to be type Integer.
        OWLOntology ontology = getOWLModelManager().getActiveOntology();

        reasoner = getOWLModelManager().getReasoner();

        Set<OWLClass> classes = ontology.getClassesInSignature();

        // Add root node
        ProVizNode root = new ProVizNode(reasoner.getTopClassNode().getRepresentativeElement());
        g.setRoot(root);

        // Recursively add edges
        addChildEdges(root);

        // Create the layout for the graph
        TreeLayout<ProVizNode, ProVizEdge> layout = new TreeLayout<ProVizNode, ProVizEdge>(g);


        // Visualizes the graph
        VisualizationViewer<ProVizNode, ProVizEdge> vv = new VisualizationViewer<ProVizNode, ProVizEdge>(layout);
        // White background
        vv.setBackground(Color.WHITE);
        // Add labels to nodes
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        // Use straight lines for edges
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(g));
        // Add mouse controls
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);

        add(vv);
	}
	@Override
	protected void disposeOWLView() {
		selectionModel.removeListener(listener);
	}

	private void updateView(OWLEntity e) {
		// TODO
	}

	private void addChildEdges(ProVizNode node) {
        // Get subclasses
        Set<OWLClass> subClasses = reasoner.getSubClasses(node.getOwlClass(), true).getFlattened();

        // Add subclass relationships
        for (OWLClass childClass : subClasses) {

            // Only add edge if the subclass isn't owl:Nothing
            if (!childClass.getIRI().getFragment().equals("Nothing")) {

                ProVizNode childNode = new ProVizNode(childClass);

                // Add child node & edge
                g.addChild(new ProVizEdge(node, childNode), node, childNode);

                // Recurse through the child to its edges
                addChildEdges(childNode);
            }
        }
    }

}
