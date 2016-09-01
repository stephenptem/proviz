package proviz;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.*;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.data.*;
import prefuse.render.*;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.TreeDepthItemSorter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * 
 * This plugin displays the visualization
 *
 * @author stephen
 *
 */
@SuppressWarnings("PackageAccessibility")
public class ProVizViewPrefuseExample extends AbstractOWLViewComponent {

    // Debugging
	private Logger logger = Logger.getLogger(ProVizViewPrefuseExample.class);

    // prefuse vars
    private Graph tree;
    private Node root;
    private Node thing;
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
        vis.run("repaint");
	}

	private void updateView(OWLEntity e) {
        logger.info("Updating view");

//        if (root != null) {
//            tree.removeNode(root);
//        }
//
//        root = tree.addNode();
//        root.set("name", getOWLModelManager().getRendering(e));



//        vis.run("repaint");
	}

    // -- 1. load the data ------------------------------------------------
    public void setUpData() {

        tree = new Graph();
        tree.addColumn("name", String.class);

        OWLOntology ontology = getOWLModelManager().getActiveOntology();

        Set<OWLClass> set = ontology.getClassesInSignature(true);

        Hashtable<String, Integer> classes = new Hashtable<String, Integer>();

        for (OWLClass c : set) {

            if (c.isOWLThing()) {
                // For owl:Thing
                thing = tree.addNode();
                thing.set("name", c.getIRI().getFragment());

                classes.put(c.getIRI().getFragment(), thing.getRow());
                logger.info("Thing: " + c.getIRI().getFragment());
            } else {
                // For normal nodes
                Node n = tree.addNode();
                n.set("name", c.getIRI().getFragment());

//                if (c.getSuperClasses(ontology).isEmpty()) {
//                    // If node is a parent node
//                    tree.addEdge(thing, n);
//                }

                logger.info("Node: " + c.getIRI().getFragment());
                classes.put(c.getIRI().getFragment(), n.getRow());
            }

        }

        for (OWLAxiom axiom : ontology.getAxioms()) {

            if (axiom instanceof OWLSubClassOfAxiom) {
                int subClassID = -1;
                int superClassID = -1;

                // SubClass
                if (((OWLSubClassOfAxiom) axiom).getSubClass() instanceof OWLClass ) {
                    subClassID = classes.get(((OWLSubClassOfAxiom) axiom).getSubClass().asOWLClass().getIRI().getFragment());
                }

                // SuperClass
                if (((OWLSubClassOfAxiom) axiom).getSuperClass() instanceof OWLClass ) {
                    superClassID = classes.get(((OWLSubClassOfAxiom) axiom).getSuperClass().asOWLClass().getIRI().getFragment());
                }

                // Only add edge if subclass doesn't already have a parent
                if (tree.getNode(subClassID).getParent() == null) {
                    // Add edge
                    if (subClassID >= 0 && superClassID >= 0) {
                        tree.addEdge(superClassID, subClassID);
                        logger.info("Edge: super=" + superClassID + " sub=" + subClassID);
                    }
                }
            }

        }

//        for (int i = 0; i < tree.getNodeCount(); i++) {
//            if (tree.getNode(i).getParent() == null && tree.getNode(i).getChildCount() == 0) {
//                tree.removeNode(i);
//            }
//        }

//        logger.info("Is valid tree: " + tree.isValidTree());

        logger.info("Data set up");
    }

    // -- 2. the visualization --------------------------------------------
    public void setUpVisualization() {
        // Create the Visualization object.
        vis = new Visualization();

        // Now we add our previously created Graph object to the visualization.
        // The tree gets a textual label so that we can refer to it later on.
        vis.add("tree", tree);

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

        // We must color the nodes of the tree.
        // Notice that we refer to the nodes using the text label for the tree,
        // and then appending ".nodes".  The same will work for ".edges" when we
        // only want to access those items.
        // The ColorAction must know what to color, what aspect of those
        // items to color, and the color that should be used.
        ColorAction fill = new ColorAction("tree.nodes", VisualItem.FILLCOLOR, ColorLib.rgb(0, 200, 0));

        // Add a border to the nodes
        ColorAction border = new ColorAction("tree.nodes", VisualItem.STROKECOLOR, ColorLib.gray(0));

        // Similarly to the node coloring, we use a ColorAction for the
        // edges
        ColorAction edges = new ColorAction("tree.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));

        // use black for node text
        ColorAction text = new ColorAction("tree.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));

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
        // to operate on the "tree".
        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout("tree", Constants.ORIENT_LEFT_RIGHT, 50, 10, 10);
        treeLayout.setLayoutAnchor(new Point2D.Double(25,300));
        layout.add(treeLayout);

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

        // The item sorter that determines the ordering of the visual items
        d.setItemSorter(new TreeDepthItemSorter());

        // We use the addControlListener method to set up interaction.

        // The DragControl is a built in class for manually moving
        // nodes with the mouse.
        d.addControlListener(new DragControl());
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
