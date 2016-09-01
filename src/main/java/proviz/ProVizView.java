package proviz;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;

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
    private Forest<Integer, String> g;

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
        g = new DelegateForest<Integer, String>();

        // Add some vertices. From above we defined these to be type Integer.
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(8);
        g.addVertex(9);

        // Note that the default is for undirected edges, our Edges are Strings.
        g.addEdge("Edge-A", 1, 2);
        g.addEdge("Edge-B", 2, 3);
        g.addEdge("Edge-C", 2, 4);
        g.addEdge("Edge-D", 2, 5);
        g.addEdge("Edge-E", 1, 6);
        g.addEdge("Edge-F", 6, 7);
        g.addEdge("Edge-G", 7, 8);
        g.addEdge("Edge-H", 8, 9);

        // Create the layout for the graph
        TreeLayout<Integer, String> layout = new TreeLayout<Integer, String>(g);

        // Visualizes the graph
        VisualizationViewer<Integer, String> vv = new VisualizationViewer<Integer, String>(layout);
        vv.setBackground(Color.WHITE);

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
