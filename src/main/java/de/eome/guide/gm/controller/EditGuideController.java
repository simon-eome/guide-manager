package de.eome.guide.gm.controller;

import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.Session;
import de.eome.guide.gm.model.GuideModel;
import de.eome.guide.gm.model.Parameter;
import de.eome.guide.gm.model.VRScene;
import de.glassroom.gpe.annotations.MetadataAnnotation;
import de.glassroom.gpe.utils.XMLUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Cotnroller for the guide editing window.
 * @author simon.schwantzer(at)im-c.de
 */
public class EditGuideController implements SceneController {
    private static final Logger LOGGER = Logger.getLogger(EditGuideController.class.getName());
    
    @FXML TextField guideTitleInput, vrSceneInput;
    @FXML TextArea guideDescriptionInput;
    @FXML Button saveButton, removeSceneButton;
    @FXML Pane vrSceneParameterPane, vrSceneParameterListPane;
    
    private final Session session;
    private final PersistenceHandler persistenceHandler;
    private Scene scene;
    private File vrSceneDescriptor;
    private VRScene vrScene;
    private boolean removeScene;
    
    public EditGuideController(PersistenceHandler persistenceHandler, Session session) {
        this.session = session;
        this.persistenceHandler = persistenceHandler;
        this.vrScene = session.getVRScene();
        this.removeScene = false;
    }
    
    @FXML
    private void initialize() {
        session.guideProperty().addListener((ObservableValue<? extends GuideModel> observable, GuideModel oldValue, GuideModel guide) -> {
            if (guide != null) {
                guideTitleInput.setText(guide.titleProperty().get());
                guideDescriptionInput.setText(guide.descriptionProperty().get());
            } else {
                guideTitleInput.setText("-");
                guideDescriptionInput.setText("-");
            }
        });
        saveButton.disableProperty().bind(Bindings.isEmpty(guideTitleInput.textProperty()).or(Bindings.isEmpty(guideDescriptionInput.textProperty())));
        removeSceneButton.disableProperty().bind(vrSceneInput.textProperty().isEmpty());
        session.vrSceneProperty().addListener((observable, oldScene, newScene) -> {
            vrScene = newScene;
            if (vrScene != null) {
                vrSceneInput.setText(newScene != null ? newScene.getTitle() + " (" + newScene.getId() + ")" : "");
                if (newScene.getParameters().isEmpty()) {
                    vrSceneParameterPane.setVisible(false);
                } else {
                    MetadataAnnotation metadata = session.getGuide().metadataProperty().get();
                    vrSceneParameterListPane.getChildren().clear();
                    newScene.getParameters().forEach(param -> {
                        ParameterItem item = new ParameterItem(param);
                        String value = metadata.getVRSceneParameter(param.getId());
                        if (value != null) item.setValue(value);
                        vrSceneParameterListPane.getChildren().add(item);
                    });
                    vrSceneParameterPane.setVisible(true);
                }
            } else {
                vrSceneInput.setText("");
                vrSceneParameterPane.setVisible(false);
            }
        });
    }
    
    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) scene.getWindow();
        stage.close();
    }
    
    @FXML
    private void save(ActionEvent event) {
        GuideModel guideModel = session.getGuide();
        if (vrSceneDescriptor != null) {
            try {
                // Store the scene in the guide directory.
                File guideDir = persistenceHandler.getGuideDir(guideModel.getId());
                File targetFile = new File(guideDir, "scene.xml");
                Files.copy(vrSceneDescriptor.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
            } catch (IOException | IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Failed to load VR scene.", ex);
                showAlert("Die VR-Szene konnte nicht geladen werden.");
            }
        }
        
        MetadataAnnotation metadata = session.getGuide().metadataProperty().get();
        if (removeScene) {
            metadata.removeVRScene();
            File guideDir = persistenceHandler.getGuideDir(guideModel.getId());
            File targetFile = new File(guideDir, "scene.xml");
            session.setVRScene(null);
            try {
                Files.delete(targetFile.toPath());
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to delete scene descriptor.", ex);
            }
        } else if (vrScene != null) {
            try {
                validateParameters();
            } catch (IllegalArgumentException e) {
                showAlert(e.getMessage());
                return;
            }
            
            // Link scene and guide.
            metadata.setVRScene(vrScene.getId());
            metadata.clearVRSceneParameters();
            Map<Parameter, ParameterItem> controllerMap = vrSceneParameterListPane.getChildren().stream()
                .map(node -> (ParameterItem) node)
                .collect(Collectors.toMap(ParameterItem::getParameter, Function.identity()));
            vrScene.getParameters().forEach((param) -> {
                String value = controllerMap.get(param).getValueAsString();
                metadata.addVRSceneParameter(param.getId(), value);
            });
            session.setVRScene(vrScene);
        }
        
        guideModel.titleProperty().set(guideTitleInput.getText());
        guideModel.descriptionProperty().set(guideDescriptionInput.getText());
        guideModel.getBean().update();
        try {
            persistenceHandler.writeGuide(guideModel.getBean());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write guide.", ex);
        }
        
        Stage stage = (Stage) scene.getWindow();
        stage.close();
    }
    
    private void validateParameters() throws IllegalArgumentException {
        Map<Parameter, ParameterItem> controllerMap = vrSceneParameterListPane.getChildren().stream()
            .map(node -> (ParameterItem) node)
            .collect(Collectors.toMap(ParameterItem::getParameter, Function.identity()));
        vrScene.getParameters().forEach((param) -> {
            String value = controllerMap.get(param).getValueAsString();
            boolean isValid = param.validateValueString(value);
            if (!isValid) {
                throw new IllegalArgumentException("Ungültiger Wert für Parameter " + param.getName() + " (" + param.getId() + ").");
            }
        });
    }
    
    @FXML
    private void loadScene(ActionEvent event) {
        removeScene = false;
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("VR-Szene", "*.xml"));
        File selectedFile = chooser.showOpenDialog(scene.getWindow());
        
        if (selectedFile != null) {
            try {
                String vrSceneXML = PersistenceHandler.readFile(selectedFile);
                vrScene = new VRScene(XMLUtils.importFromString(vrSceneXML));
                vrSceneInput.setText(vrScene.getTitle() + " (" + vrScene.getId() + ")");
                vrSceneParameterListPane.getChildren().clear();
                vrScene.getParameters().forEach(param -> {
                    ParameterItem item = new ParameterItem(param);
                    vrSceneParameterListPane.getChildren().add(item);
                });
                vrSceneDescriptor = selectedFile;
            } catch (IOException | IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Failed to load VR scene.", ex);
                showAlert("Die VR-Szene konnte nicht geladen werden.");
            }
        }
    }
    
    @FXML
    private void removeScene(ActionEvent event) {
        removeScene = true;
        vrSceneDescriptor = null;
        vrSceneInput.setText("");
    }

    @Override
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @Override
    public Scene getScene() {
        return scene;
    }
    
    private void showAlert(String message) {
        Window owner = scene.getWindow();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.show();
    }
}
