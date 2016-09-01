package proviz;

import org.apache.log4j.Logger;
import org.pg.eti.kask.sova.graph.OWLtoGraphConverter;
import org.pg.eti.kask.sova.utils.ReasonerLoader;
import org.pg.eti.kask.sova.visualization.OVDisplay;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.Activity;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

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
    private Node root;
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

        // launch the visualization --------------------------------------

        // Add the display to this view component
        setLayout(new BorderLayout());
        add(d);

        // We have to start the ActionLists that we added to the visualization
        vis.run("color");
        vis.run("layout");
	}

	private void updateView(OWLEntity e) {
        logger.info("Updating view");

        if (root != null) {
            graph.removeNode(root);
        }

        root = graph.addNode();
        root.set("name", getOWLModelManager().getRendering(e));

        Set<OWLClass> classes = getOWLModelManager().getActiveOntology().getClassesInSignature();

        for (OWLClass c : classes) {
            Node n = graph.addNode();
            n.set("name", c.getIRI().getFragment());

            graph.addEdge(root, n);
        }

        vis.run("repaint");
	}

    // -- 1. load the data ------------------------------------------------
    public void setUpData() {

        graph = new Graph();
        graph.addColumn("name", String.class);

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

        // Add padding
        l.setHorizontalPadding(5);
        l.setVerticalPadding(5);

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
        ColorAction border = new ColorAction("graph.nodes", VisualItem.STROKECOLOR, ColorLib.gray(100));

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
        ActionList repaint = new ActionList();
        repaint.add(color);
        repaint.add(layout);
        repaint.add(new RepaintAction());

        // add the actions to the visualization
        vis.putAction("repaint", repaint);

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
