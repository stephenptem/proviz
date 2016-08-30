package proviz;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLEntityDisplayProvider;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;

/**
 * @author Stephen Temple
 * @version 2016.08.09
 */
public class ProVizTab extends OWLWorkspaceViewsTab {

    private OWLEntityDisplayProvider provider = new OWLEntityDisplayProvider() {
        @Override
        public boolean canDisplay(OWLEntity owlEntity) {
            return true;
        }

        @Override
        public JComponent getDisplayComponent() {
            return ProVizTab.this;
        }
    };

    /**
     * Initialise the workspace.
     */
    public void initialise() {
        super.initialise();
        getOWLEditorKit().getWorkspace().registerOWLEntityDisplayProvider(provider);
    }

    /**
     * Disposes the workspace?
     */
    public void dispose() {
        getOWLEditorKit().getWorkspace().unregisterOWLEntityDisplayProvider(provider);
        super.dispose();
    }

    /**
     *
     * @return the owl model manager
     */
    public OWLModelManager getOWLModelManager() {
        return (OWLModelManager) getWorkspace().getEditorKit().getModelManager();
    }

    /**
     *
     * @return the owl editor kit
     */
    public OWLEditorKit getOWLEditorKit() {
        return (OWLEditorKit) getWorkspace().getEditorKit();
    }
}
