package de.eome.guide.gm.controller;

import de.eome.guide.gm.model.GuideModel;
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
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class SynchronizeListItemController extends ListCell<GuideModel> {
    private static final Logger LOGGER = Logger.getLogger(SynchronizeListItemController.class.getName());
    
    private final ListView<GuideModel> list;
    
    @FXML Label titleLabel;
    @FXML Pane iconNew, iconRefresh, iconInstall, iconOk, itemPane;
    
    public SynchronizeListItemController(ListView<GuideModel> listView) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SychronizeListItem.fxml"));
        loader.setController(this);
        
        this.list = listView;
        
        try {
            loader.load();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize step or chapter item.", e);
        }
    }
    
    @FXML
    protected void initialize() {
        itemPane.prefWidthProperty().bind(list.widthProperty().subtract(20));
        itemPane.setMaxWidth(Control.USE_PREF_SIZE);
    }
    
    @Override
    protected void updateItem(GuideModel item, boolean isEmpty) {
        super.updateItem(item, isEmpty);
        if (isEmpty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            titleLabel.textProperty().bind(item.titleProperty());
            switch (item.syncStatusProperty().get()) {
                case "new":
                    showIcon(iconNew);
                    break;
                case "outdated":
                    showIcon(iconRefresh);
                    break;
                case "unknown":
                    showIcon(iconInstall);
                    break;
                case "ok":
                    showIcon(iconOk);
                    break;
                default:
                    showIcon(iconOk);
            }
            
            setText(null);
            setGraphic(itemPane);
        }
    }
    
    private void showIcon(Pane visibleIcon) {
        iconNew.setVisible(false);
        iconRefresh.setVisible(false);
        iconInstall.setVisible(false);
        iconOk.setVisible(false);
        visibleIcon.toFront();
        visibleIcon.setVisible(true);
    }
}
