package de.eome.guide.gm.controller;

import de.eome.guide.gm.Session;
import de.eome.guide.gm.model.GuideManagerModel;
import de.eome.guide.gm.model.GuideModel;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * Controller for the guide selection window.
 * @author simon.schwantzer(at)im-c.de
 */
public class OpenGuideController implements SceneController{
    @FXML private ListView<GuideModel> guideListView;
    @FXML private Button okButton;
    
    private final Session session;
    private Scene scene;
    
    public OpenGuideController(Session session) {
        this.session = session;
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
        
        session.guideManagerProperty().addListener((ObservableValue<? extends GuideManagerModel> observable, GuideManagerModel oldValue, GuideManagerModel guideManager) -> {
            if (guideManager != null) {
                guideListView.itemsProperty().bind(guideManager.guidesProperty());
            } else {
                guideListView.itemsProperty().get().clear();
            }
        });
        if (session.guideManagerProperty().isNotNull().get()) {
            guideListView.itemsProperty().bind(session.guideManagerProperty().get().guidesProperty());
        }
        
    }
    
    @FXML
    private void selectGuide(ActionEvent event) {
        GuideModel guide = guideListView.getSelectionModel().getSelectedItem();
        session.setGuide(guide);
        ((Stage) scene.getWindow()).close();
    }
    
    @FXML
    private void cancel(ActionEvent event) {
        ((Stage) scene.getWindow()).close();
    }

    @Override
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @Override
    public Scene getScene() {
        return scene;
    }
}
