package proviz;

import java.awt.BorderLayout;

import javax.swing.*;

import org.apache.log4j.Logger;
import org.pg.eti.kask.sova.utils.ReasonerLoader;
import org.pg.eti.kask.sova.visualization.OVDisplay;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * 
 * This plugin displays is visualization
 *
 * @author stephen
 *
 */
public class ProvizView extends AbstractOWLViewComponent {

    // Debugging
	private Logger logger = Logger.getLogger(ProvizView.class);

    // SOVA vars
	private OVDisplay display;
    private boolean showFullTree = true;

    // Swing vars
	private JLabel label;
    private JPanel panel;

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
        ReasonerLoader.getInstance().setReasoner(getOWLModelManager().getReasoner());
        System.out.println("załadowanie reasonera: " + getOWLModelManager().getReasoner());

        if (display == null) {
            display = new OVDisplay();
            display.setSize(1000, 900);
        }
        display.generateTreeFromOWl(getOWLModelManager().getActiveOntology());
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        display.setSize(1000, 900);
        if (leftPanel == null) {
            initLeftPanel();
            this.add(leftPanel);
        }
        if (rightPanel == null) {
            initRightPanel();
            this.add(rightPanel);
        }

        if (!getOWLModelManager().getReasoner().toString().toLowerCase().contains("hermit")) {
            String message = "To use this method of visualization you need to install HermiT "
                    + "and choose it in reasoner menu.";
            JOptionPane.showMessageDialog(this, message);
        }


		logger.info("Initializing test view");
		label = new JLabel("Hello world");
		setLayout(new BorderLayout());
		add(label, BorderLayout.CENTER);
		selectionModel = getOWLWorkspace().getOWLSelectionModel();
		selectionModel.addListener(listener);
	}
	@Override
	protected void disposeOWLView() {
        display.removeDisplayVis();
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
