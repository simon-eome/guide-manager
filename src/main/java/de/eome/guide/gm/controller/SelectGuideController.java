package de.eome.guide.gm.controller;

import de.eome.guide.gm.model.GuideManagerModel;
import de.eome.guide.gm.model.GuideModel;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Controller for the guide selection window.
 * @author simon.schwantzer(at)im-c.de
 */
public class SelectGuideController {
    private static final Logger LOGGER = Logger.getLogger(SelectGuideController.class.getName());
    
    @FXML private ListView<GuideModel> guideListView;
    @FXML private Button okButton;
    
    private final Stage stage;
    private final GuideManagerModel guideManager;
    
    public SelectGuideController(Window owner, String title, GuideManagerModel guideManager) {
        stage = new Stage();
        this.guideManager = guideManager;
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SelectGuideDialog.fxml"));
        fxmlLoader.setController(this);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load guide selection dialog.", ex);
            throw new RuntimeException("Failed to load guide selection dialog.", ex);
        }
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icon.png")));
        stage.initOwner(owner);
    }
    
    @FXML
    private void initialize() {
        okButton.setDisable(true);
        
        guideListView.setCellFactory((ListView<GuideModel> listView) -> new ListCell<GuideModel>() {
            @Override
            protected void updateItem(GuideModel guide, boolean isEmpty) {
                super.updateItem(guide, isEmpty);
                if (isEmpty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(guide.titleProperty().get());
                }
            }
        });
        guideListView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends GuideModel> observable, GuideModel oldValue, GuideModel newValue) -> {
            okButton.setDisable(false);
        });
        
        guideListView.itemsProperty().bind(guideManager.guidesProperty());
    }
    
    private void updateList() {
        /*
        ObservableList<GuideModel> items = FXCollections.observableArrayList();
        guideManager.getGuides(null, (Guide g1, Guide g2) -> {
            String compStr1 = g1.getMetadata().getTitle();
            String compStr2 = g2.getMetadata().getTitle();
            return compStr1.compareTo(compStr2);
        }).forEach(guide -> items.add(new GuideModel(guide, MainApp.LANG)));
        guideListView.setItems(items);*/
        
    }
    
    @FXML
    private void selectGuide(ActionEvent event) {
        GuideModel guide = guideListView.getSelectionModel().getSelectedItem();
        stage.setUserData(guide);
        stage.close();
    }
    
    @FXML
    private void cancel(ActionEvent event) {
        stage.setUserData(null);
        stage.close();
    }
    
    /**
     * Displays the dialog and waits for a user interaction.
     * @return Option between a new non-empty text if applied or NULL.
     */
    public Optional<GuideModel> showAndWait() {
        stage.showAndWait();
        return Optional.ofNullable((GuideModel) stage.getUserData());
    }
}
