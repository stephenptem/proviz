package proviz;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;

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
		label = new JLabel("Hello world");
		setLayout(new BorderLayout());
		add(label, BorderLayout.CENTER);
		selectionModel = getOWLWorkspace().getOWLSelectionModel();
		selectionModel.addListener(listener);
	}
	@Override
	protected void disposeOWLView() {
		selectionModel.removeListener(listener);
	}

	private void updateView(OWLEntity e) {
		if (e != null) {
			String entityName = getOWLModelManager().getRendering(e);
			label.setText("Hello World! Selected entity = " +  entityName);
		}
		else {
			label.setText("Hello World!");
		}
	}

}
