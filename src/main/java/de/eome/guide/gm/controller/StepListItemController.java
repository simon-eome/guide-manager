package de.eome.guide.gm.controller;

import de.eome.guide.gm.model.ChapterItem;
import de.eome.guide.gm.model.NodeItem;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

/**
 * List item for the step list.
 * @author simon.schwantzer(at)im-c.de
 */
public class StepListItemController extends ListCell<NodeItem> {
    private static final Logger LOGGER = Logger.getLogger(StepListItemController.class.getName());
    
    @FXML Pane itemPane, iconStackPane, stepIconPane, chapterIconPane;
    @FXML Label nodeLabel;
    
    private final ListView<NodeItem> list;
    
    public StepListItemController(ListView<NodeItem> listView) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StepListItem.fxml"));
        loader.setController(this);
        
        this.list = listView;
        
        try {
            loader.load();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize step or chapter item.", e);
        }
    }
    
    @FXML
    private void initialize() {
        itemPane.prefWidthProperty().bind(list.widthProperty().subtract(2));
        itemPane.setMaxWidth(Control.USE_PREF_SIZE);
    }
    
    @Override
    protected void updateItem(NodeItem item, boolean isEmpty) {
        super.updateItem(item, isEmpty);
        if (isEmpty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            nodeLabel.textProperty().bind(item.labelProperty());
            if (item instanceof ChapterItem) {
                chapterIconPane.toFront();
                chapterIconPane.setVisible(true);
                stepIconPane.setVisible(false);
            } else {
                stepIconPane.toFront();
                stepIconPane.setVisible(true);
                chapterIconPane.setVisible(false);
            }
            
            setText(null);
            setGraphic(itemPane);
        }
    }
}
