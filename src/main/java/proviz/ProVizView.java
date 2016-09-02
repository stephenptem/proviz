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
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.*;

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
    private DirectedSparseGraph<String, Integer> g;

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
        g = new DirectedSparseGraph<String, Integer>();

        // Add some vertices. From above we defined these to be type Integer.
        OWLOntology ontology = getOWLModelManager().getActiveOntology();

        Set<OWLClass> set = ontology.getClassesInSignature(true);

        // Add nodes
        for (OWLClass c : set) {

            g.addVertex(c.getIRI().getFragment());

        }

        // Add edges
        int edge = 0;
        for (OWLAxiom axiom : ontology.getAxioms()) {

            if (axiom instanceof OWLSubClassOfAxiom) {
                String subClass = null;
                String superClass = null;

                // SubClass
                if (((OWLSubClassOfAxiom) axiom).getSubClass() instanceof OWLClass ) {
                    subClass = ((OWLSubClassOfAxiom) axiom).getSubClass().asOWLClass().getIRI().getFragment();
                }

                // SuperClass
                if (((OWLSubClassOfAxiom) axiom).getSuperClass() instanceof OWLClass ) {
                    superClass = ((OWLSubClassOfAxiom) axiom).getSuperClass().asOWLClass().getIRI().getFragment();
                }

                // Add the edge between sub and super classes
                if (subClass != null && superClass != null) {
                    g.addEdge(edge, superClass, subClass);
                    edge++;
                }
            }

        }


        // Create the layout for the graph
        DAGLayout<String, Integer> layout = new DAGLayout<String, Integer>(g);
        layout.setSize(new Dimension(800,600));

        // Visualizes the graph
        VisualizationViewer<String, Integer> vv = new VisualizationViewer<String, Integer>(layout);
        vv.setBackground(Color.WHITE);

        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
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
