package proviz;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.*;
import javax.xml.transform.Transformer;
//import org.apache.commons.collections15.Transformer;

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
 * This plugin displays the visualization of the ontology
 *
 * @author Stephen Temple, Jeremy Bishop, Brandon Pickup
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
    private VisualizationViewer<ProVizNode, ProVizEdge> vv;

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
        logger.info("Initializing ProVizView");

		setLayout(new BorderLayout());

		selectionModel = getOWLWorkspace().getOWLSelectionModel();
		selectionModel.addListener(listener);

        // Graph<V, E> where V is the type of the vertices and E is the type of the edges
        g = new DelegateTree<ProVizNode, ProVizEdge>();

        // Get the reasoner
        reasoner = getOWLModelManager().getReasoner();

        // Add root node
        ProVizNode root = new ProVizNode(reasoner.getTopClassNode().getRepresentativeElement());
        g.setRoot(root);
        view();
	}

	@Override
	protected void disposeOWLView() {
		selectionModel.removeListener(listener);
	}

	private void updateView(OWLEntity e) {
		if (e.isOWLClass()) {
            // Delete the old visualization
            remove(vv);

            // If selected entity is an OWL class then rebuild the tree from selected node down
            // Graph<V, E> where V is the type of the vertices and E is the type of the edges
            g = new DelegateTree<ProVizNode, ProVizEdge>();

            // Add root node
            ProVizNode root = new ProVizNode(e.asOWLClass());
            g.setRoot(root);

            view();

        }
	}

    // Creates JUNG objects to view graph
    private void view (){

//                // Transformer maps the vertex number to a vertex property
//        Transformer<ProVizNode,Paint> vertexColor = new Transformer<ProVizNode,Paint>() {
//            public Paint transform(Integer i) {
//                if(i == 1) return Color.GREEN;
//                return Color.RED;
//            }
//        };

        // Build edges in graph
//        addChildEdges(g.getRoot());
//        addInstanceEdges(g.getRoot());
        addAllEdges(g.getRoot());

        // Create the layout for the graph
        TreeLayout<ProVizNode, ProVizEdge> layout = new TreeLayout<ProVizNode, ProVizEdge>(g);
        // Visualizes the graph
        vv = new VisualizationViewer<ProVizNode, ProVizEdge>(layout);
        // White background
        vv.setBackground(Color.WHITE);
        // Add labels to nodes
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//        // Set node colours
//        vv.getRenderContext().setVertexFillPaintTransformer()
        // Use straight lines for edges
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(g));
        // Add mouse controls
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        add(vv);
    }

    // Recursively adds edges between a supplied class and all its subclasses and all instances
    private void addAllEdges(ProVizNode node) {
        // Get instances
        Set<OWLNamedIndividual> instances = reasoner.getInstances(node.getOwlClass(), true).getFlattened();
        addInstances(instances, node);

        // Add edges for subclasses with instances
        Set<OWLClass> subClasses = reasoner.getSubClasses(node.getOwlClass(), true).getFlattened();
        if (subClasses.isEmpty()){return;}
        for (OWLClass childClass : subClasses) {
            // Only add edge if the subclass isn't owl:Nothing and is satisfiable
            if (!childClass.getIRI().getFragment().equals("Nothing") && reasoner.isSatisfiable(childClass)) {
                ProVizNode childNode = new ProVizNode(childClass);
                // Add child node & edge
                g.addChild(new ProVizEdge(node, childNode), node, childNode);
                addAllEdges(childNode);
            }
        }
    }

	private void addChildEdges(ProVizNode node) {
        // Get subclasses
        Set<OWLClass> subClasses = reasoner.getSubClasses(node.getOwlClass(), true).getFlattened();

        // Add subclass relationships
        for (OWLClass childClass : subClasses) {

            // Only add edge if the subclass isn't owl:Nothing and is satisfiable
            if (!childClass.getIRI().getFragment().equals("Nothing") && reasoner.isSatisfiable(childClass)) {

                ProVizNode childNode = new ProVizNode(childClass);

                // Add child node & edge
                g.addChild(new ProVizEdge(node, childNode), node, childNode);

                // Recurse through the child to its edges
                addChildEdges(childNode);
            }
        }
    }

    // Recursively adds edges between a supplied class and its subclasses with instances
    private void addInstanceEdges(ProVizNode node) {
        // Get instances
        Set<OWLNamedIndividual> instances = reasoner.getInstances(node.getOwlClass(), true).getFlattened();
        addInstances(instances, node);

        // Add edges for subclasses with instances
        Set<OWLClass> subClasses = reasoner.getSubClasses(node.getOwlClass(), true).getFlattened();
        if (subClasses.isEmpty()){return;}
        for (OWLClass childClass : subClasses) {
            // Only add edge if the subclass isn't owl:Nothing, has instances/has a child that has instances, and is satisfiable
            if (!childClass.getIRI().getFragment().equals("Nothing")&& hasInstances(childClass) && reasoner.isSatisfiable(childClass)) {
                ProVizNode childNode = new ProVizNode(childClass);
                // Add child node & edge
                g.addChild(new ProVizEdge(node, childNode), node, childNode);
                addInstanceEdges(childNode);
            }
        }
    }

    // hasInstances returns true if the supplied node, or any of its children has any instances, and false otherwise
    private boolean hasInstances(OWLClass node){
        Set<OWLNamedIndividual> instances = reasoner.getInstances(node, true).getFlattened();
        if (!instances.isEmpty()){
            return true;}
        else{
            Set<OWLClass> subClasses = reasoner.getSubClasses(node, true).getFlattened();
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

    // Adds edges between the supplied node, and all its instances
    private void addInstances(Set<OWLNamedIndividual> instances, ProVizNode node){
        if (!instances.isEmpty()){
            for (OWLNamedIndividual instance : instances){
                // Only add edge if the instance isn't owl:Nothing
                if (!instance.getIRI().getFragment().equals("Nothing")){
                    ProVizNode instanceNode = new ProVizNode(instance);
                    // Add instance & edge
                    g.addChild(new ProVizEdge(node, instanceNode), node, instanceNode);
                }
            }
        }
    }


}


