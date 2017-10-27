package de.eome.guide.gm;

import de.eome.guide.gm.controller.SynchronizeController;
import de.eome.guide.gm.controller.EditGuideController;
import de.eome.guide.gm.controller.MainController;
import de.eome.guide.gm.controller.SettingsController;
import de.eome.guide.gm.controller.OpenGuideController;
import de.eome.guide.gm.controller.SceneController;
import de.eome.guide.gm.model.GuideManagerModel;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main application.
 * @author simon.schwantzer(at)im-c.de
 */
public class MainApp extends Application {
    public static final String LANG = "de_DE";
    
    /**
     * Enum for available scenes.
     */
    public enum AppScene {
        MAIN,
        SETTINGS,
        OPEN_GUIDE,
        SYNCHRONIZE,
        EDIT_GUIDE
    }
    
    public static final AppConfiguration CONFIG;
    
    private final PersistenceHandler persistenceHandler;
    private final Map<AppScene, Scene> scenes;
    private final Session session;
    
    static {
        CONFIG = new AppConfiguration();
    }
    
    public MainApp() {
        scenes = new HashMap<>();
        session = new Session();
        persistenceHandler = new PersistenceHandler();
    }

    @Override
    public void start(Stage stage) throws Exception {
        MainController mainController = new MainController(scenes, persistenceHandler, session);
        initScene(AppScene.MAIN, "/fxml/MainWindow.fxml", mainController);
        initScene(AppScene.SETTINGS, "/fxml/SettingsWindow.fxml", new SettingsController(scenes, persistenceHandler, session));
        initScene(AppScene.OPEN_GUIDE, "/fxml/OpenGuideWindow.fxml", new OpenGuideController(session));
        initScene(AppScene.SYNCHRONIZE, "/fxml/SynchronizeWindow.fxml", new SynchronizeController(persistenceHandler, session));
        initScene(AppScene.EDIT_GUIDE, "/fxml/EditGuideWindow.fxml", new EditGuideController(persistenceHandler, session));
        
        stage.setTitle("Guide Manager");
        stage.setScene(scenes.get(AppScene.MAIN));
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/icon.png")));
        stage.show();
        
        String repoDirPath = CONFIG.getRepoDir();
        if (repoDirPath != null) {
            File repoDir = new File(repoDirPath);
            if (repoDir.exists()) {
                GuideManagerModel gmm = new GuideManagerModel(persistenceHandler.initializeRepository(repoDir), LANG);
                session.setGuideManager(gmm);
            } else {
                mainController.showPreferences();
            }
        } else {
            mainController.showPreferences();
        }
    }
    
    private void initScene(AppScene appScene, String fxmlPath, SceneController controller) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
        fxmlLoader.setController(controller);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scenes.put(appScene, scene);
        controller.setScene(scene);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
