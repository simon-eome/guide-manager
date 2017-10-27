package de.eome.guide.gm.controller;

import de.eome.guide.gm.AppConfiguration;
import de.eome.guide.gm.MainApp;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.Session;
import de.eome.guide.gm.model.GuideManagerModel;
import de.glassroom.gpe.GuideManager;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Controller for the settings window.
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public class SettingsController implements SceneController {
    private final static Logger LOGGER = Logger.getLogger(SettingsController.class.getName());
    
    private final Map<MainApp.AppScene, Scene> scenes;
    private final PersistenceHandler persistenceHandler;
    private final Session session;
    private final StringProperty repoErrorProperty, editorErrorProperty;
    private final StringProperty selectedImageEditor, selectedVideoEditor;
    private Scene scene;
    
    @FXML private TextField
            repoDirInput,
            imageEditorInput,
            videoEditorInput;
    @FXML private Label
            missingDirMessage,
            editorErrorMessage;
    @FXML private Pane
            imageEditorPane,
            videoEditorPane;
    @FXML private CheckBox
            useDefaultImageEditorCheck,
            useDefaultVideoEditorCheck;
    
    
    public SettingsController(Map<MainApp.AppScene, Scene> scenes, PersistenceHandler persistenceHandler, Session session) {
        this.scenes = scenes;
        this.persistenceHandler = persistenceHandler;
        this.session = session;
        this.repoErrorProperty = new SimpleStringProperty();
        this.editorErrorProperty = new SimpleStringProperty();
        this.selectedImageEditor = new SimpleStringProperty();
        this.selectedVideoEditor = new SimpleStringProperty();
    }
    
    @FXML
    private void initialize() {
        missingDirMessage.managedProperty().bind(missingDirMessage.visibleProperty());
        missingDirMessage.setVisible(false);
        repoDirInput.setText(MainApp.CONFIG.getRepoDir(""));
        missingDirMessage.visibleProperty().bind(repoErrorProperty.isNotEmpty());
        missingDirMessage.textProperty().bind(repoErrorProperty);
        editorErrorMessage.visibleProperty().bind(editorErrorProperty.isNotEmpty());
        editorErrorMessage.textProperty().bind(editorErrorProperty);
        imageEditorPane.disableProperty().bind(useDefaultImageEditorCheck.selectedProperty());
        videoEditorPane.disableProperty().bind(useDefaultVideoEditorCheck.selectedProperty());
        
        String imageEditor = MainApp.CONFIG.getImageEditor();
        if (imageEditor != null) {
            selectedImageEditor.set(imageEditor);
            useDefaultImageEditorCheck.selectedProperty().set(false);
        } else {
            useDefaultImageEditorCheck.selectedProperty().set(true);
        }
        
        String videoEditor = MainApp.CONFIG.getVideoEditor();
        if (videoEditor != null) {
            selectedVideoEditor.set(videoEditor);
            useDefaultVideoEditorCheck.selectedProperty().set(false);
        } else {
            useDefaultVideoEditorCheck.selectedProperty().set(true);
        }
        
        imageEditorInput.textProperty().bindBidirectional(selectedImageEditor);
        videoEditorInput.textProperty().bindBidirectional(selectedVideoEditor);
    }
    
    @FXML
    private void showFolderSelection(ActionEvent event) {
        repoErrorProperty.set(null);
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Verzeichnis auswählen");
        String oldRepoDir = MainApp.CONFIG.getRepoDir();
        if (oldRepoDir != null ) {
            File oldRepoDirFile = new File(oldRepoDir);
            if (oldRepoDirFile.exists()) {
                chooser.setInitialDirectory(oldRepoDirFile);
            }
        }
        File repoDir = chooser.showDialog(scenes.get(MainApp.AppScene.SETTINGS).getWindow());
        if (repoDir != null) {
            String absolutPath = repoDir.getAbsolutePath();
            repoDirInput.setText(absolutPath);
        }
    }
    
    @FXML
    private void showImageEditorSelection(ActionEvent event) {
        editorErrorProperty.set(null);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Editor auswählen");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ausführbare Dateien", Arrays.asList("*.exe", "*.bat", "*.cmd")));
        File executable = chooser.showOpenDialog(scene.getWindow());
        if (executable != null) {
            selectedImageEditor.set(executable.getAbsolutePath());
        }
    }
    
    @FXML
    private void showVideoEditorSelection(ActionEvent event) {
        editorErrorProperty.set(null);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Editor auswählen");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ausführbare Dateien", Arrays.asList("*.exe", "*.bat", "*.cmd")));
        File executable = chooser.showOpenDialog(scene.getWindow());
        if (executable != null) {
            selectedVideoEditor.set(executable.getAbsolutePath());
        }
    }
    
    @FXML
    private void validateAndClose(ActionEvent event) {
        // Check settings.
        if (!checkRepoSettings()) {
            return;
        }
        
        if (!checkEditorSettings()) {
            return;
        }
        
        // Apply repo settings.
        AppConfiguration config = MainApp.CONFIG;
        File repoDir = new File(repoDirInput.getText());
        File guidesSubDir = new File(repoDir, "guides");
        if ("glassroom".equals(repoDir.getName()) && guidesSubDir.exists()) {
            repoDir = guidesSubDir;
        }
        config.setRepoDir(repoDir.getAbsolutePath());
        GuideManager gm = persistenceHandler.initializeRepository(repoDir);
        session.setGuideManager(new GuideManagerModel(gm, MainApp.LANG));
        Stage stage = (Stage) scenes.get(MainApp.AppScene.SETTINGS).getWindow();
        stage.close();
        
        // Apply editor settings.
        config.setImageEditor(useDefaultImageEditorCheck.isSelected() ? null : selectedImageEditor.get());
        config.setVideoEditor(useDefaultVideoEditorCheck.isSelected() ? null : selectedVideoEditor.get());
    }
    
    private boolean checkRepoSettings() {
        File repoDir = new File(repoDirInput.getText());
        if (!repoDir.exists()) {
            repoErrorProperty.set("Der ausgewählte Order existiert nicht.");
            repoDirInput.selectAll();
            repoDirInput.requestFocus();
            return false;
        } else {
            return true;
        }
    }
    
    private boolean checkEditorSettings() {
        if (!useDefaultImageEditorCheck.isSelected()) {
            File imageEditorExecutable = new File(selectedImageEditor.get());
            if (!imageEditorExecutable.exists() || !imageEditorExecutable.canExecute()) {
                editorErrorProperty.set("Die ausgewählte Bildbearbeitung existiert nicht oder kann nicht gestartet werden.");
                return false;
            }
        }
        if (!useDefaultVideoEditorCheck.isSelected()) {
            File videoEditorExecutable = new File(selectedVideoEditor.get());
            if (!videoEditorExecutable.exists() || !videoEditorExecutable.canExecute()) {
                editorErrorProperty.set("Der ausgewählte Video-Editor existiert nicht oder kann nicht gestartet werden.");
                return false;
            }
        }
        
        return true;
    }
    
    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) scene.getWindow();
        stage.close();
    }
    
    @FXML
    private void repoDirInputChanged(KeyEvent event) {
        if (missingDirMessage.isVisible()) {
            missingDirMessage.setVisible(false);
        }
    }

    @Override
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @Override
    public Scene getScene() {
        return scene;
    }
    
    @FXML
    protected void importTools(ActionEvent event) {
        Window window = scene.getWindow();
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Werkzeugdatenbank", "*.xml"));
        File selectedFile = chooser.showOpenDialog(window);
        if (selectedFile != null) {
            try {
                MainApp.CONFIG.importTools(selectedFile.toPath());
                new Alert(Alert.AlertType.INFORMATION, "Es wurden " + MainApp.CONFIG.getTools().size() + " Werkzeuge importiert.").show();
            } catch (IOException | IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Failed to import tool description.", ex);
                new Alert(Alert.AlertType.ERROR, "Die Werkzeugdatenbank konnte nicht importiert werden.").show();
            }
        }
    }
    
    @FXML
    protected void importVRMethods(ActionEvent event) {
        Window window = scene.getWindow();
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("VR-Methodenspezifikation", "*.xml"));
        File selectedFile = chooser.showOpenDialog(window);
        if (selectedFile != null) {
            try {
                MainApp.CONFIG.importVRMethods(selectedFile.toPath());
                new Alert(Alert.AlertType.INFORMATION, "Die VR-Methodenspezifikation wurde aktualisiert.").show();
            } catch (IOException | IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Failed to import VR method description.", ex);
                new Alert(Alert.AlertType.ERROR, "Die VR-Methodenspezifikation konnte nicht importiert werden.").show();
            }
        }
    }
}
