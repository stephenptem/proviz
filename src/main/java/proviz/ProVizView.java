package proviz;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.*;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
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
    private DelegateForest<String, Integer> g;

    // OWL vars
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
        g = new DelegateForest<String, Integer>();

        // Add some vertices. From above we defined these to be type Integer.
        OWLOntology ontology = getOWLModelManager().getActiveOntology();

        OWLReasoner reasoner = getOWLModelManager().getReasoner();

        Set<OWLClass> classes = ontology.getClassesInSignature();

        // Add nodes
        for (OWLClass c : classes) {

            g.addVertex(c.getIRI().getFragment());

        }

        // Add edges
        int edge = 0;
        for (OWLClass c : classes) {
            // Get subclasses
            Set<OWLClass> subClasses = reasoner.getSubClasses(c, true).getFlattened();

            // Add subclass relationships
            for (OWLClass sub : subClasses) {
                // Get the subclass name
                String subclassFragment = sub.getIRI().getFragment();

                // Only add edge if the subclass isn't owl:Nothing
                if (!subclassFragment.equals("Nothing")) {

                    // Only add edge if the subclass doesn't already have a parent
                    if (g.getParentEdge(subclassFragment) == null) {
                        g.addEdge(edge, c.getIRI().getFragment(), subclassFragment);
                        edge++;
                    }
                }
            }
        }

        // Create the layout for the graph
        TreeLayout<String, Integer> layout = new TreeLayout<String, Integer>(g);

        // Visualizes the graph
        VisualizationViewer<String, Integer> vv = new VisualizationViewer<String, Integer>(layout);
        vv.setBackground(Color.WHITE);

        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(g));
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

}
