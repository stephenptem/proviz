package proviz;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;

import org.apache.log4j.Logger;
import org.pg.eti.kask.sova.utils.ReasonerLoader;
import org.pg.eti.kask.sova.visualization.OVDisplay;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

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

    // SOVA vars
	private OVDisplay display;
    private boolean showFullTree = true;

    // Swing vars
	private JLabel label;
    private JPanel panel;
    private JButton but3 = null;
    private JPanel leftPanel = null, rightPanel = null;

//    // OWL vars
//	private OWLSelectionModel selectionModel;
//	private OWLSelectionModelListener listener = new OWLSelectionModelListener() {
//		@Override
//		public void selectionChanged() throws Exception {
//			OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
//			updateView(entity);
//		}
//	};
	
//	@Override
//	protected void initialiseOWLView() throws Exception {
//        logger.info("Initializing test view");
//		label = new JLabel("Hello world");
//		setLayout(new BorderLayout());
//		add(label, BorderLayout.CENTER);
//		selectionModel = getOWLWorkspace().getOWLSelectionModel();
//		selectionModel.addListener(listener);
//	}
//	@Override
//	protected void disposeOWLView() {
//		selectionModel.removeListener(listener);
//	}
//
//	private void updateView(OWLEntity e) {
//		if (e != null) {
//			String entityName = getOWLModelManager().getRendering(e);
//			label.setText("Hello World! Selected entity = " +  entityName);
//		}
//		else {
//			label.setText("Hello World!");
//		}
//	}

    @Override
    protected void disposeOWLView() {
        display.removeDisplayVis();
    }

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

    }

    private void initRightPanel() {
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel(new GridLayout(8, 1));
        JButton but2 = new JButton("Reset");
        but2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                display.removeDisplayVis();
                ReasonerLoader.getInstance().setReasoner(getOWLModelManager().getReasoner());
                display.generateTreeFromOWl(getOWLModelManager().getActiveOntology());
                but3.setText("Show Full Tree");
                showFullTree = true;
            }
        });
        but2.setSize(100, 80);
        but2.setToolTipText("Reload ontology");
        buttonPanel.add(but2);


        but3 = new JButton("Show Full Tree");
        but3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                if (showFullTree) {
                    display.showFullTree();
                    but3.setText("Hide Full Tree");
                    showFullTree = false;
                } else {
                    display.hideFullTree();
                    but3.setText("Show Full Tree");
                    showFullTree = true;
                }
            }
        });
        buttonPanel.add(but3);
        JButton saveImage = new JButton("Save Image");
        saveImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                File f = new File("");
                FileDialog fd = new FileDialog(new Frame(), "Save", FileDialog.SAVE);
                fd.setFilenameFilter(new FilenameFilter() {

                    public boolean accept(File dir, String name) {
                        if (name.toUpperCase().endsWith(".PNG")
                                || name.toUpperCase().endsWith(".JPG")) {
                            return true;
                        }
                        return false;
                    }
                });
                fd.setLocation(50, 50);
                fd.setVisible(true);
                String sFile = fd.getDirectory() + fd.getFile();

                String format = "png";

                if (sFile.toUpperCase().endsWith(".PNG")
                        || sFile.toUpperCase().endsWith(".JPG")) {
                    format = sFile.substring(sFile.length() - 3, sFile.length());
                } else {
                    sFile += '.' + format;
                }

                File file = new File(sFile);

                FileOutputStream os;
                try {

                    os = new FileOutputStream(file);
                    display.saveImage(os, format.toUpperCase(), 5);
                    os.close();
                    //zapis do pliku
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });
        buttonPanel.add(saveImage);

//        JButton saveFullImage = new JButton("Save Full Image");
//        saveFullImage.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                display.getVisualization().stopLayout();
//                File f = new File("");
//                FileDialog fd = new FileDialog(new Frame(), "Save Full Image", FileDialog.SAVE);
//                fd.setFilenameFilter(new FilenameFilter() {
//
//                    public boolean accept(File dir, String name) {
//                        if (name.toUpperCase().endsWith(".PNG")
//                                || name.toUpperCase().endsWith(".JPG")) {
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                fd.setLocation(50, 50);
//                fd.setVisible(true);
//                if (fd.getDirectory() == null || fd.getFile() == null) {
//                    return;
//                }
//                String sFile = fd.getDirectory() + fd.getFile();
//
//                String format = "png";
//
//                if (sFile.toUpperCase().endsWith(".PNG")
//                        || sFile.toUpperCase().endsWith(".JPG")) {
//                    format = sFile.substring(sFile.length() - 3, sFile.length());
//                } else {
//                    sFile += '.' + format;
//                }
//
//                File file = new File(sFile);
//
//                FileOutputStream os;
//                try {
//
//                    os = new FileOutputStream(file);
//                    display.saveFullImage(os, 5.0);
//                    os.close();
//                    //zapis do pliku
//                } catch (FileNotFoundException e1) {
//                    e1.printStackTrace();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        });
//        buttonPanel.add(saveFullImage);

        rightPanel.add(buttonPanel);
        rightPanel.setPreferredSize(new Dimension(120, Integer.MAX_VALUE));
        rightPanel.setMaximumSize(new Dimension(140, Integer.MAX_VALUE));
        rightPanel.setMinimumSize(new Dimension(100, Integer.MAX_VALUE));
    }

    private void initLeftPanel() {
        leftPanel = new JPanel();
        leftPanel.add(display);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    }

}
