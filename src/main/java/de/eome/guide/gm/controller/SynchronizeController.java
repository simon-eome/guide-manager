package de.eome.guide.gm.controller;

import de.eome.guide.gm.MainApp;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.Session;
import de.eome.guide.gm.model.GuideManagerModel;
import de.eome.guide.gm.model.GuideModel;
import de.glassroom.gpe.Guide;
import de.glassroom.gpe.GuideManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * Controller for the guide synchronization window.
 *
 * @author lena.rohlfs(at)im-c.de
 */
public class SynchronizeController implements Initializable, SceneController {

    private static final Logger LOGGER = Logger.getLogger(SynchronizeController.class.getName());

    private final Session session;
    private final ListProperty<GuideModel> repoItemsProperty, clientItemsProperty;
    private final PersistenceHandler repoPersistenceHandler;
    private PersistenceHandler clientPersistenceHandler;
    private GuideManagerModel clientGuideManager;
    private ListView<GuideModel> activeList;
    private final ObjectProperty<GuideModel> selectedGuide;
    private boolean isDelegated;
    private Scene scene;

    // private GuideModel[] repositoryGuides, clientGuides;
    @FXML
    private Pane stackPane, progressPane, mainPane;
    @FXML
    private ListView<GuideModel> repositoryGuideList, clientGuideList;
    @FXML
    private Label guideIdLabel, guideTitleLabel, guideDescriptionLabel;
    @FXML
    private Button copyToClientButton, copyToRepoButton, deleteGuideButton, synchronizeButton;

    public SynchronizeController(PersistenceHandler persistenceHandler, Session session) {
        this.session = session;
        this.repoItemsProperty = new SimpleListProperty<>();
        this.clientItemsProperty = new SimpleListProperty<>();
        this.repoPersistenceHandler = persistenceHandler;
        this.selectedGuide = new SimpleObjectProperty<>();
        isDelegated = false;
    }

    @Override
    public void setScene(Scene scene) {
        this.scene = scene;
        scene.windowProperty().addListener((windowObservable, oldWindow, newWindow) -> {
            if (newWindow != null) {
                newWindow.addEventHandler(WindowEvent.WINDOW_SHOWN, (event) -> {
                    if (clientGuideManager != null) {
                        updateLists(session.getGuideManager().getBean(), clientGuideManager.getBean());
                    }
                });
            }
        });
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    public void setLoading(boolean loading) {
        if (loading) {
            progressPane.toFront();
            progressPane.setVisible(true);
        } else {
            mainPane.toFront();
            progressPane.setVisible(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SelectionModel<GuideModel> repoSelection = repositoryGuideList.getSelectionModel();
        SelectionModel<GuideModel> clientSelection = clientGuideList.getSelectionModel();
        BooleanBinding noRepoSelection = repoSelection.selectedIndexProperty().lessThan(0).or(repositoryGuideList.focusedProperty().not());
        BooleanBinding noClientSelection = clientSelection.selectedIndexProperty().lessThan(0).or(clientGuideList.focusedProperty().not());

        deleteGuideButton.setDisable(true);
        copyToClientButton.setDisable(true);
        copyToRepoButton.setDisable(true);
        selectedGuide.addListener((observable, oldGuide, newGuide) -> {
            deleteGuideButton.setDisable(newGuide == null);
        });

        repositoryGuideList.setCellFactory((ListView<GuideModel> listView) -> new SynchronizeListItemController(repositoryGuideList));
        repoSelection.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!isDelegated) {
                updateGuideDetails(newValue);
                selectedGuide.set(newValue);
                isDelegated = true;
                clientSelection.select(repoSelection.getSelectedIndex());
                updateActions(newValue, clientSelection.getSelectedItem());
            } else {
                isDelegated = false;
            }
        });
        session.guideManagerProperty().addListener((observable, oldValue, guideManager) -> {
            // Created ordered List of guides
            if (guideManager == null) {
                repoItemsProperty.clear();
                clientItemsProperty.clear();
            } else if (clientGuideManager != null) {
                updateLists(guideManager.getBean(), clientGuideManager.getBean());
            } else {
                repositoryGuideList.itemsProperty().bind(guideManager.guidesProperty());
            }
        });

        clientGuideList.setCellFactory((ListView<GuideModel> listView) -> new SynchronizeListItemController(clientGuideList));
        clientSelection.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!isDelegated) {
                updateGuideDetails(newValue);
                selectedGuide.set(newValue);
                isDelegated = true;
                repoSelection.select(clientSelection.getSelectedIndex());
                updateActions(repoSelection.getSelectedItem(), newValue);
            } else {
                isDelegated = false;
            }
        });

        repositoryGuideList.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == true) {
                activeList = repositoryGuideList;
                selectedGuide.set(repoSelection.getSelectedItem());
            }
        });

        clientGuideList.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue = true) {
                activeList = clientGuideList;
                selectedGuide.set(clientSelection.getSelectedItem());
            }
        });
    }

    private void updateActions(GuideModel repoModel, GuideModel clientModel) {
        if (repoModel == null || clientModel == null) {
            copyToClientButton.setDisable(true);
            copyToRepoButton.setDisable(true);
        } else {
            switch (repoModel.syncStatusProperty().get()) {
                case "new":
                    copyToClientButton.setDisable(false);
                    copyToRepoButton.setDisable(true);
                    break;
                case "outdated":
                case "unknown":
                    copyToClientButton.setDisable(true);
                    copyToRepoButton.setDisable(false);
                    break;
                default:
                    if ("ok".equals(clientModel.syncStatusProperty().get())) {
                        copyToClientButton.setDisable(true);
                        copyToRepoButton.setDisable(true);
                    } else {
                        copyToClientButton.setDisable(false);
                        copyToRepoButton.setDisable(true);
                    }
            }
        }
    }

    private void updateGuideDetails(GuideModel guideModel) {
        if (guideModel != null) {
            Guide guide = guideModel.getBean();
            guideIdLabel.setText(guide.getId());
            guideDescriptionLabel.setText(guide.getMetadata().getDescription());
            guideTitleLabel.setText(guide.getMetadata().getTitle());
        } else {
            guideIdLabel.setText("-");
            guideDescriptionLabel.setText("-");
            guideTitleLabel.setText("-");
        }
    }

    @FXML
    private void selectClientDirectory(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Verzeichnis auswählen");
        String oldClientDir = MainApp.CONFIG.getClientDir();
        if (oldClientDir != null) {
            File clientDirFile = new File(oldClientDir);
            if (clientDirFile.exists() && clientDirFile.isDirectory()) {
                chooser.setInitialDirectory(clientDirFile);
            }
        }

        File clientDir = chooser.showDialog(stage);
        if (clientDir != null && clientDir.exists()) {
            File guidesSubDir = new File(clientDir, "guides");
            if ("glassroom".equals(clientDir.getName()) && guidesSubDir.exists()) {
                clientDir = guidesSubDir;
            }
            MainApp.CONFIG.setClientDir(clientDir.getAbsolutePath());
            clientPersistenceHandler = new PersistenceHandler();
            GuideManager gm = clientPersistenceHandler.initializeRepository(clientDir);
            clientGuideManager = new GuideManagerModel(gm, MainApp.LANG);
            updateLists(session.getGuideManager().getBean(), clientGuideManager.getBean());
        }
    }

    private void updateLists(GuideManager repoGuideManager, GuideManager clientGuideManager) {
        Set<String> knownGuides = new HashSet<>();
        Map<String, Guide> repoGuides = new HashMap<>();
        Map<String, Guide> clientGuides = new HashMap<>();
        repoGuideManager.getGuides(null, null).forEach((guide) -> repoGuides.put(guide.getId(), guide));
        clientGuideManager.getGuides(null, null).forEach((guide) -> clientGuides.put(guide.getId(), guide));

        knownGuides.addAll(repoGuides.keySet());
        knownGuides.addAll(clientGuides.keySet());

        ObservableList<GuideModel> repoItems = FXCollections.observableArrayList();
        ObservableList<GuideModel> clientItems = FXCollections.observableArrayList();
        List<String> sortedGuides = new ArrayList<>(knownGuides);
        Collections.sort(sortedGuides, (o1, o2) -> {
            if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else {
                Guide g1 = repoGuides.get(o1) != null ? repoGuides.get(o1) : clientGuides.get(o1);
                Guide g2 = repoGuides.get(o2) != null ? repoGuides.get(o2) : clientGuides.get(o2);
                return g1.getMetadata().getTitle().compareTo(g2.getMetadata().getTitle());
            }
        });
        for (String id : sortedGuides) {
            Guide repoGuide = repoGuides.get(id);
            Guide clientGuide = clientGuides.get(id);
            if (repoGuide != null && clientGuide != null) {
                long repoUpdate = repoGuide.getLastUpdate().getTime();
                long clientUpdate = clientGuide.getLastUpdate().getTime();
                long diff = repoUpdate - clientUpdate;
                if (Math.abs(diff) < 500) {
                    // We tolerate 0.5s difference as "same".
                    repoItems.add(generateModel(repoGuide, "ok"));
                    clientItems.add(generateModel(clientGuide, "ok"));
                } else if (diff < 0) {
                    repoItems.add(generateModel(repoGuide, "outdated"));
                    clientItems.add(generateModel(clientGuide, "ok"));
                } else {
                    repoItems.add(generateModel(repoGuide, "ok"));
                    clientItems.add(generateModel(clientGuide, "outdated"));
                }

            } else if (repoGuide != null) {
                // Guide only in repo.
                repoItems.add(generateModel(repoGuide, "new"));
                clientItems.add(generateModel(repoGuide, "unknown"));
            } else {
                // Guide only on client.
                repoItems.add(generateModel(clientGuide, "unknown"));
                clientItems.add(generateModel(clientGuide, "new"));
            }
        }

        repoItemsProperty.set(repoItems);
        clientItemsProperty.set(clientItems);

        repositoryGuideList.itemsProperty().bind(repoItemsProperty);
        clientGuideList.itemsProperty().bind(clientItemsProperty);

        Node n1 = repositoryGuideList.lookup(".scroll-bar");
        if (n1 instanceof ScrollBar) {
            ScrollBar scrollBar1 = (ScrollBar) n1;
            Node n2 = clientGuideList.lookup(".scroll-bar");
            if (n2 instanceof ScrollBar) {
                ScrollBar scrollBar2 = (ScrollBar) n2;
                scrollBar1.valueProperty().bindBidirectional(scrollBar2.valueProperty());
            }
        }
    }

    private GuideModel generateModel(Guide guide, String status) {
        GuideModel model = new GuideModel(guide, MainApp.LANG);
        model.syncStatusProperty().set(status);
        return model;
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void copyToClient(ActionEvent event) {
        GuideModel guide = repositoryGuideList.getSelectionModel().getSelectedItem();
        copyGuideToClient(guide);
    }

    private void copyGuideToClient(GuideModel guide) {
        File dirToCopy = repoPersistenceHandler.getGuideDir(guide.getId());
        boolean guideExistsOnClient = clientGuideManager.getBean().getGuide(guide.getId()) != null;
        if (guideExistsOnClient) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Anleitung überschreiben");
            alert.setHeaderText("Wollen sie die Anleitung \"" + guide.titleProperty().get() + "\" auf dem Client wirklich überschreiben?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() != ButtonType.OK) {
                return;
            }
        }
        new Thread() {
            public void run() {
                try {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setLoading(true);
                        }
                    });
                    GuideManager newManager = clientPersistenceHandler.importGuide(dirToCopy);
                    clientGuideManager = new GuideManagerModel(newManager, MainApp.LANG);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setLoading(false);
                            updateLists(session.getGuideManager().getBean(), newManager);
                        }
                    });
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.WARNING, "Failed to copy guide to client: " + guide.getId(), ex);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to import guide.", ex);
                    showAlert("Das Kopieren der Anleitung ist fehlgeschlagen!");
                    updateLists(session.getGuideManager().getBean(), clientGuideManager.getBean());
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setLoading(false);
                        }
                    });
                }
            }
        }.start();
    }

    @FXML
    public void copyToRepo(ActionEvent event) {
        setLoading(true);
        GuideModel guide = clientGuideList.getSelectionModel().getSelectedItem();
        copyGuideToRepository(guide);
        setLoading(false);
    }

    private void copyGuideToRepository(GuideModel guide) {
        File dirToCopy = clientPersistenceHandler.getGuideDir(guide.getId());
        boolean guideExistsInRepo = session.getGuideManager().getBean().getGuide(guide.getId()) != null;
        if (guideExistsInRepo) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Anleitung überschreiben");
            alert.setHeaderText("Wollen sie die Anleitung \"" + guide.titleProperty().get() + "\" im Repository wirklich überschreiben?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() != ButtonType.OK) {
                return;
            }
        }
        new Thread() {
            public void run() {
                try {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setLoading(true);
                        }
                    });
                    GuideManager newManager = repoPersistenceHandler.importGuide(dirToCopy);
                    session.setGuideManager(new GuideManagerModel(newManager, MainApp.LANG));
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setLoading(false);
                            updateLists(newManager, clientGuideManager.getBean());
                        }
                    });
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.WARNING, "Failed to copy guide to repository: " + guide.getId(), ex);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to import guide.", ex);
                    showAlert("Das Kopieren der Anleitung ist fehlgeschlagen!");
                    updateLists(session.getGuideManager().getBean(), clientGuideManager.getBean());
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setLoading(false);
                        }
                    });
                }
            }
        }.start();
    }

    @FXML
    private void removeGuide(ActionEvent event) {
        if (selectedGuide.get() == null) {
            return;
        }

        GuideManagerModel responsibleGuideManager;
        PersistenceHandler responsiblePersistenceHandler;
        String source;
        if (activeList == repositoryGuideList) {
            responsibleGuideManager = session.getGuideManager();
            responsiblePersistenceHandler = repoPersistenceHandler;
            source = "aus dem Repository";
        } else if (activeList == clientGuideList) {
            responsibleGuideManager = clientGuideManager;
            responsiblePersistenceHandler = clientPersistenceHandler;
            source = "vom Client";
        } else {
            return;
        }

        GuideModel guide = selectedGuide.get();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Anleitung löschen");
        alert.setHeaderText("Wollen sie die Anleitung \"" + guide.titleProperty().get() + "\" wirklich " + source + " löschen?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            for (GuideModel gm : responsibleGuideManager.guidesProperty()) {
                if (gm.getId().equals(guide.getId())) {
                    responsibleGuideManager.guidesProperty().remove(gm);
                    responsiblePersistenceHandler.deleteGuide(guide.getId());
                    break;
                }
            }
        }
        updateLists(session.getGuideManager().getBean(), clientGuideManager.getBean());
    }

    @FXML
    public void synchronizeAll(ActionEvent event) {
        for (int i = 0; i < repoItemsProperty.getSize(); i++) {
            GuideModel repoModel = repoItemsProperty.get(i);
            GuideModel clientModel = clientItemsProperty.get(i);

            if ("outdated".equals(repoModel.syncStatusProperty().get())) {
                setLoading(true);
                copyGuideToRepository(clientModel);
                setLoading(false);
            } else if ("outdated".equals(clientModel.syncStatusProperty().get())) {
                setLoading(true);
                copyGuideToClient(repoModel);
                setLoading(false);
            }
        }
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
