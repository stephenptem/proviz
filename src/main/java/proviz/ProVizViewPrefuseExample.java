package proviz;

import org.apache.log4j.Logger;
import org.pg.eti.kask.sova.utils.ReasonerLoader;
import org.pg.eti.kask.sova.visualization.OVDisplay;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.controls.PanControl;
import prefuse.controls.SubtreeDragControl;
import prefuse.controls.WheelZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Random;

/**
 * 
 * This plugin displays the visualization
 *
 * @author stephen
 *
 */
public class ProVizViewPrefuseExample extends AbstractOWLViewComponent {

    // Debugging
	private Logger logger = Logger.getLogger(ProVizViewPrefuseExample.class);

    // prefuse vars
    private Graph graph;
    private Random rand;
    private Visualization vis;
    private Display d;

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
        logger.info("Initializing prefuse example view");
		selectionModel = getOWLWorkspace().getOWLSelectionModel();
		selectionModel.addListener(listener);

        setUpData();
        setUpVisualization();
        setUpRenderers();
        setUpActions();
        setUpDisplay();

        // launch the visualization -------------------------------------

        // The following is standard java.awt.
        // A JFrame is the basic window element in awt.
        // It has a menu (minimize, maximize, close) and can hold
        // other gui elements.

        // Create a new panel to hold the visualization.
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);

        // The Display object (d) is a subclass of JComponent, which
        // can be added to JPanel with the add method.
        panel.add(d);

        // Add the panel to this view component
        setLayout(new BorderLayout());
        add(panel);

        // We have to start the ActionLists that we added to the visualization
        vis.run("color");
        vis.run("layout");
	}

	private void updateView(OWLEntity e) {
		// TODO: update graph on selected node
	}

    // -- 1. load the data ------------------------------------------------
    public void setUpData() {
        // Here we are manually creating the data structures.  100 nodes are
        // added to the Graph structure.  100 edges are made randomly
        // between the nodes.

        graph = new Graph();

        graph.addColumn("id", Integer.class);
        graph.addColumn("name", String.class);

        for (int i = 0; i < 10; i++) {
            Node n = graph.addNode();
            n.set("id", i);
            n.set("name", "Node #" + i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 5);
        graph.addEdge(2, 6);
        graph.addEdge(2, 7);
        graph.addEdge(7, 8);
        graph.addEdge(8, 9);
        graph.addEdge(4, 9);


        logger.info("Data set up");
    }

    // -- 2. the visualization --------------------------------------------
    public void setUpVisualization() {
        // Create the Visualization object.
        vis = new Visualization();

        // Now we add our previously created Graph object to the visualization.
        // The graph gets a textual label so that we can refer to it later on.
        vis.add("graph", graph);

        logger.info("Visualzation set up");
    }

    // -- 3. the renderers and renderer factory ---------------------------
    public void setUpRenderers() {

        // draw the "name" label for NodeItems
        LabelRenderer l = new LabelRenderer("name");
        l.setRoundedCorner(8, 8); // round the corners

        // create a new default renderer factory
        // return our name label renderer as the default for all non-EdgeItems
        // includes straight line edges for EdgeItems by default
        vis.setRendererFactory(new DefaultRendererFactory(l));

        logger.info("Renderers set up");
    }

    public void setUpActions() {

        // -- 4. the processing actions ---------------------------------------

        // We must color the nodes of the graph.
        // Notice that we refer to the nodes using the text label for the graph,
        // and then appending ".nodes".  The same will work for ".edges" when we
        // only want to access those items.
        // The ColorAction must know what to color, what aspect of those
        // items to color, and the color that should be used.
        ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, ColorLib.rgb(0, 200, 0));

        // Add a border to the nodes
        ColorAction border = new ColorAction("graph.nodes", VisualItem.STROKECOLOR, ColorLib.rgb(33, 33, 33));

        // Similarly to the node coloring, we use a ColorAction for the
        // edges
        ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));

        // use black for node text
        ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));

        // Create an action list containing all color assignments
        // ActionLists are used for actions that will be executed
        // at the same time.
        ActionList color = new ActionList();
        color.add(fill);
        color.add(border);
        color.add(edges);
        color.add(text);

        // The layout ActionList recalculates
        // the positions of the nodes.
        ActionList layout = new ActionList();

        // We add the layout to the layout ActionList, and tell it
        // to operate on the "graph".
        layout.add(new NodeLinkTreeLayout("graph"));

        // We add a RepaintAction so that every time the layout is
        // changed, the Visualization updates it's screen.
        layout.add(new RepaintAction());

        // add the actions to the visualization
        vis.putAction("color", color);
        vis.putAction("layout", layout);

        logger.info("Actions set up");
    }

    public void setUpDisplay() {
        // -- 5. the display and interactive controls -------------------------

        // Create the Display object, and pass it the visualization that it
        // will hold.
        d = new Display(vis);

        // We use the addControlListener method to set up interaction.

        // The DragControl is a built in class for manually moving
        // nodes with the mouse.
//        d.addControlListener(new DragControl());
        d.addControlListener(new SubtreeDragControl());
        // Pan with left-click drag on background
        d.addControlListener(new PanControl());
        // Zoom with right-click drag
        d.addControlListener(new WheelZoomControl());

        logger.info("Display set up");
    }

    @Override
    protected void disposeOWLView() {
        selectionModel.removeListener(listener);
    }

}
