package de.eome.guide.gm.controller;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.ActionManager;
import de.eome.guide.gm.MainApp;
import de.eome.guide.gm.model.ChapterItem;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import de.eome.guide.gm.MainApp.AppScene;
import de.eome.guide.gm.model.NodeItem;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.Session;
import de.eome.guide.gm.actions.AddHintAction;
import de.eome.guide.gm.actions.AddWarningAction;
import de.eome.guide.gm.actions.ChangeMediaAction;
import de.eome.guide.gm.actions.EditHintAction;
import de.eome.guide.gm.actions.EditWarningAction;
import de.eome.guide.gm.actions.RemoveHintAction;
import de.eome.guide.gm.actions.RemoveMediaAction;
import de.eome.guide.gm.actions.RemoveWarningAction;
import de.eome.guide.gm.model.ContentModel;
import de.eome.guide.gm.model.GuideModel;
import de.eome.guide.gm.model.HintModel;
import de.eome.guide.gm.model.ActionItem;
import de.eome.guide.gm.model.WarningModel;
import de.glassroom.gpe.Chapter;
import de.glassroom.gpe.Guide;
import de.glassroom.gpe.Node;
import de.glassroom.gpe.Step;
import de.glassroom.gpe.content.ContentDescriptor;
import de.glassroom.gpe.content.Hint;
import de.glassroom.gpe.content.Warning;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import de.eome.guide.gm.Action;
import de.eome.guide.gm.ActionListener;
import de.eome.guide.gm.actions.AddChapterAction;
import de.eome.guide.gm.actions.AddStepAction;
import de.eome.guide.gm.actions.AddToolAction;
import de.eome.guide.gm.actions.BaseAction;
import de.eome.guide.gm.actions.DeleteNodeAction;
import de.eome.guide.gm.actions.EditStepInfoAction;
import de.eome.guide.gm.actions.MoveNodeAction;
import de.eome.guide.gm.actions.RemoveToolAction;
import de.eome.guide.gm.actions.SelectVRNodeAction;
import de.eome.guide.gm.model.IntegerParameter;
import de.eome.guide.gm.model.Parameter;
import de.eome.guide.gm.model.StepModel;
import de.eome.guide.gm.model.Tool;
import de.eome.guide.gm.model.VRMethod;
import de.eome.guide.gm.model.VRNode;
import de.eome.guide.gm.model.VRScene;
import de.glassroom.gpe.GuideManager;
import de.glassroom.gpe.annotations.ContentAnnotation;
import de.glassroom.gpe.annotations.SceneAnnotation;
import de.glassroom.gpe.annotations.ToolAnnotation;
import de.glassroom.gpe.utils.XMLUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;

public class MainController implements SceneController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    
    @FXML private BorderPane headerChapterInfo;
    @FXML private SplitPane body;
    @FXML private ListView<NodeItem> stepListView;
    @FXML private ListView<WarningModel> warningsListView;
    @FXML private ListView<HintModel> hintsListView;
    @FXML private ListView<ToolAnnotation> toolListView;
    @FXML private TitledPane 
            vrPane;
    @FXML private Pane
            nodeDetailsPane,
            chapterPane,
            stepPane,
            noMediaPane,
            imageMediaPane,
            videoMediaPane,
            vrSceneInfoPane;
    @FXML private Label 
            guideTitle,
            guideDescription,
            chapterGuideTitle,
            parentGuideTitle,
            stepInfo,
            vrNodeLabel,
            vrMethodLabel,
            vrSceneLabel;
    @FXML private MediaView contentVideoView;
    @FXML private ImageView contentImageView;
    @FXML private CheckBox stepIsRoutine;
    @FXML private MenuItem undoMenuItem;
    @FXML private Button
            mediaRemoveButton,
            mediaEditButton,
            mediaRefreshButton,
            mediaFolderButton,
            editWarningButton,
            removeWarningButton,
            editHintButton,
            editGuideButton,
            deleteGuideButton,
            removeHintButton,
            deleteNodeButton,
            moveNodeUpButton,
            moveNodeDownButton,
            removeToolButton,
            removeVRNodeButton,
            combineStepsButton;
        
    private final Map<AppScene, Scene> scenes;
    private final Session session;
    private final PersistenceHandler persistenceHandler;
    private final ActionManager changeManager;
    private Scene scene;
    private MediaPlayer mediaPlayer;
    private final BooleanProperty singleStepSelectionProperty, multipleStepSelectionProperty, consecutiveStepSelectionProperty;
    
    public MainController(Map<AppScene, Scene> scenes, PersistenceHandler persistenceHandler, Session session) {
        this.scenes = scenes;
        this.session = session;
        this.persistenceHandler = persistenceHandler;
        this.changeManager = new ActionManager();
        this.singleStepSelectionProperty = new SimpleBooleanProperty(false);
        this.multipleStepSelectionProperty = new SimpleBooleanProperty(false);
        this.consecutiveStepSelectionProperty = new SimpleBooleanProperty(false);
    }

    @FXML
    private void initialize() {
        headerChapterInfo.managedProperty().bind(headerChapterInfo.visibleProperty());
        headerChapterInfo.setVisible(false);
        // nodeDetailsPane.managedProperty().bind(nodeDetailsPane.visibleProperty());
        nodeDetailsPane.setVisible(false);
        body.setDisable(true);
        undoMenuItem.disableProperty().bind(changeManager.hasChangesProperty().not());
        changeManager.descriptionProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            String text;
            if (newValue != null) {
                text = "\"" + newValue + "\" rückgängig machen";
            } else {
                text = "Rückgängig";
            }
            undoMenuItem.textProperty().set(text);
        });
        editWarningButton.disableProperty().bind(warningsListView.getSelectionModel().selectedIndexProperty().greaterThanOrEqualTo(0).not());
        removeWarningButton.disableProperty().bind(warningsListView.getSelectionModel().selectedIndexProperty().greaterThanOrEqualTo(0).not());
        editHintButton.disableProperty().bind(hintsListView.getSelectionModel().selectedIndexProperty().greaterThanOrEqualTo(0).not());
        removeHintButton.disableProperty().bind(hintsListView.getSelectionModel().selectedIndexProperty().greaterThanOrEqualTo(0).not());
        editGuideButton.disableProperty().bind(session.guideProperty().isNull());
        deleteGuideButton.disableProperty().bind(session.guideProperty().isNull());
        mediaFolderButton.disableProperty().bind(session.stepProperty().isNull().or(multipleStepSelectionProperty));
        removeToolButton.disableProperty().bind(toolListView.getSelectionModel().selectedIndexProperty().greaterThanOrEqualTo(0).not());
        vrPane.visibleProperty().bind(session.vrSceneProperty().isNotNull());
        nodeDetailsPane.visibleProperty().bind(session.guideProperty().isNotNull().and(stepListView.getSelectionModel().selectedIndexProperty().greaterThanOrEqualTo(0)).and(singleStepSelectionProperty));

        List<VRMethod> vrMethods = MainApp.CONFIG.getVRMethods();
        
        final MultipleSelectionModel<NodeItem> stepListSelectionModel = stepListView.getSelectionModel();
        deleteNodeButton.disableProperty().bind(multipleStepSelectionProperty);
        moveNodeUpButton.disableProperty().bind(stepListSelectionModel.selectedIndexProperty().lessThan(1).or(multipleStepSelectionProperty));
        combineStepsButton.disableProperty().bind(consecutiveStepSelectionProperty.not().or(singleStepSelectionProperty));
        
        stepIsRoutine.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            try {
                ContentModel content = session.getContent();
                content.isRoutineProperty().set(newValue);
                persistenceHandler.writeContentDescriptor(session.getGuide().getBean(), content.getBean());
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to write content descriptor.", ex);
            }
        });
                
        // Add session bindings.
        session.guideProperty().addListener((ObservableValue<? extends GuideModel> observable, GuideModel oldValue, GuideModel newValue) -> {
            if (newValue != null) {
                // nodeDetailsPane.setVisible(true);
                body.setDisable(false);

                guideTitle.textProperty().bind(newValue.titleProperty());
                guideDescription.textProperty().bind(newValue.descriptionProperty());
                openGuide(newValue);
            } else {
                // nodeDetailsPane.setVisible(false);
                body.setDisable(true);
                ObservableList<NodeItem> stepList = stepListView.itemsProperty().get();
                if (stepList != null) stepList.clear();
                
                guideTitle.textProperty().unbind();
                guideDescription.textProperty().unbind();
                
                guideTitle.setText("-");
                guideDescription.setText("-");
            }
        });
        
        /*session.chapterProperty().addListener((ObservableValue<? extends Chapter> observable, Chapter oldValue, Chapter chapter) -> {
            if (chapter != null) {
                Guide target = session.getGuideManager().getBean().getGuide(chapter.getCalledProcessId());
                chapterGuideTitle.setText(target.getMetadata().getTitle());
            } else {
                chapterGuideTitle.setText("n/a");
            }
        });*/
        
        session.contentProperty().addListener((ObservableValue<? extends ContentModel> observable, ContentModel previousContent, ContentModel content) -> {
            if (content != null) {
                stepInfo.textProperty().bind(content.infoProperty());
                mediaRemoveButton.disableProperty().bind(content.mediaProperty().isNull());
                mediaEditButton.disableProperty().bind(content.mediaProperty().isNull());
                mediaRefreshButton.disableProperty().bind(content.mediaProperty().isNull());
                
                updateMediaPreview(content.mediaProperty().get());
                content.mediaProperty().addListener((ObservableValue<? extends ContentModel.Media> observable1, ContentModel.Media oldValue, ContentModel.Media newValue) -> {
                    updateMediaPreview(newValue);
                });
                stepIsRoutine.setSelected(content.isRoutineProperty().get());
                
                // ObservableList<Warning> warningList = FXCollections.observableArrayList(content.getBean().getWarnings());
                // warningsListView.setItems(warningList);
                warningsListView.itemsProperty().bind(content.warningsProperty());
                
                // ObservableList<Hint> hintList = FXCollections.observableArrayList(content.getBean().getHints());
                // hintsListView.setItems(hintList);
                hintsListView.itemsProperty().bind(content.hintsProperty());
            }
        });
        
        stepListView.setCellFactory(listView -> new StepListItemController(listView));
        stepListSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        stepListSelectionModel.selectedItemProperty().addListener((observable, deselectedItem, selectedItem) -> {
            List<Integer> indicesList = stepListSelectionModel.getSelectedIndices();
            singleStepSelectionProperty.set(indicesList.size() == 1);
            multipleStepSelectionProperty.set(indicesList.size() > 1);
            if (indicesList.size() >= 2) {
                boolean isConsecutive = true;
                
                for (int i = 1; i < indicesList.size(); i++) {
                    if (indicesList.get(i) - indicesList.get(i-1) != 1) {
                        isConsecutive = false;
                        break;
                    }
                }
                consecutiveStepSelectionProperty.set(isConsecutive);
            } else {
                consecutiveStepSelectionProperty.set(false);
            }
            
            
            if (selectedItem != null && singleStepSelectionProperty.get()) {
                switch (selectedItem.getType()) {
                    case CHAPTER:
                        openChapter((Chapter) selectedItem.getNode());
                        chapterGuideTitle.textProperty().bind(selectedItem.labelProperty());
                        break;
                    case STEP:
                        openStep((Step) selectedItem.getNode());
                        break;
                }
            }
        });
        
        toolListView.setCellFactory((ListView<ToolAnnotation> param) -> {
            ListCell<ToolAnnotation> cell = new ListCell<ToolAnnotation>() {
                @Override
                protected void updateItem(ToolAnnotation toolAnnotation, boolean isEmpty) {
                    super.updateItem(toolAnnotation, isEmpty);
                    if (toolAnnotation != null) {
                        Tool tool = MainApp.CONFIG.getTool(toolAnnotation.getId());
                        StringBuilder display  = new StringBuilder(200)
                                .append(tool != null ? tool.getName() : toolAnnotation.getId());
                        if (!toolAnnotation.getParameteres().isEmpty()) {
                            display.append(" (")
                                    .append(String.join(", ", toolAnnotation.getParameteres().entrySet().stream()
                                        .map(param -> {
                                            StringBuilder builder = new StringBuilder();
                                            Parameter toolParam = tool != null ? tool.getParameter(param.getKey()) : null;
                                            if (toolParam != null) {
                                                switch (toolParam.getType()) {
                                                    case INT:
                                                        String unit = ((IntegerParameter) toolParam).getUnit();
                                                        builder.append(unit != null ? unit : toolParam.getName());
                                                        break;
                                                    default:
                                                        builder.append(toolParam.getName());
                                                }
                                            } else {
                                                builder.append(param.getKey());
                                            }
                                            builder.append("=").append(param.getValue());
                                            return builder.toString();
                                        }).collect(Collectors.toList())))
                                    .append(")");
                        }
                        setText(display.toString());
                    } else {
                        setGraphic(null);
                        setText(null);
                    }
                }
            };
            return cell;
        });
        
        session.stepProperty().addListener((observable, oldStep, newStep) -> {
            if (newStep != null) {
                updateToolList();
                removeVRNodeButton.disableProperty().bind(newStep.sceneProperty().isNull());
                updateSceneInformation(newStep.getScene());
                newStep.sceneProperty().addListener((sceneObservable, oldScene, newScene) -> {
                    updateSceneInformation(newScene);
                });
            }
        });
        
        vrSceneInfoPane.visibleProperty().bind(session.vrSceneProperty().isNotNull());
        session.vrSceneProperty().addListener((observable, oldScene, newScene) -> {
            vrSceneLabel.setText(newScene != null ? newScene.getTitle() : "-");
        });
    }
    
    private void updateSceneInformation(SceneAnnotation newScene) {
        VRScene vrScene = session.getVRScene();
        if (vrScene != null && newScene != null) {
            VRNode vrNode = vrScene.findNode(newScene.getNodeId());
            vrNodeLabel.setText(vrNode != null ? vrNode.toString() : "?");
            String methodId = newScene.getMethodId();
            VRMethod vrMethod = MainApp.CONFIG.getVRMethods().stream().filter(method -> method.getId().equals(methodId)).findAny().orElse(null);
            vrMethodLabel.setText(vrMethod != null ? vrMethod.toString() : "?");
        } else {
            vrNodeLabel.setText("-");
            vrMethodLabel.setText("-");
        }
    }
    
    private void updateMediaPreview(ContentModel.Media media) {
        if (mediaPlayer != null) mediaPlayer.dispose(); // Release video if necessary.

        if (media != null) {
            String guideId = session.getGuide().getId();
            String contentId = session.getContent().getId();
            File contentDir = persistenceHandler.getContentPackageDir(guideId, contentId);
            File mediaFile = new File(contentDir, media.getPath());
            switch (media.getMimeType()) {
                case "video/mp4":
                    videoMediaPane.toFront();
                    Media videoMedia = new Media(mediaFile.toURI().toString());
                    mediaPlayer = new MediaPlayer(videoMedia);
                    contentVideoView.setMediaPlayer(mediaPlayer);
                    break;
                case "image/jpeg":
                case "image/png":
                    imageMediaPane.toFront();
                    Image img = new Image(mediaFile.toURI().toString());
                    contentImageView.setImage(img);
                    break;
            }
        } else {
            noMediaPane.toFront();
        }
    }
    
    public void openChapter(Chapter chapter) {
        session.setStep(null);
        session.setChapter(chapter);
        chapterPane.toFront();
    }
    
    public void openStep(Step step) {
        session.setChapter(null);
        StepModel stepModel = new StepModel(step);
        session.setStep(stepModel);
        try {
            ContentDescriptor content = persistenceHandler.readContentDescriptor(step.getParentGuide().getId(), step.getContent().getContentPackage());
            session.setContent(new ContentModel(content));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read content package for step: " + step.getId(), ex);
        }
        
        stepPane.toFront();
    }
    
    private void updateToolList() {
        toolListView.getItems().clear();
        if (session.getStep() != null) {
            Step step = session.getStep().getBean();
            ContentAnnotation contentAnnotation = step.getContent();
            if (contentAnnotation != null) {
                ObservableList<ToolAnnotation> newList = FXCollections.observableArrayList(contentAnnotation.getTools());
                toolListView.setItems(newList);
            }
        }
    }
    
    public void showPreferences() {
        Stage stage = new Stage();
        stage.setScene(scenes.get(AppScene.SETTINGS));
        stage.setTitle("Einstellungen");
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/icon.png")));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(scene.getWindow());
        stage.setResizable(false);
        stage.showAndWait();
    }
    
    private void updateStepList(Guide guide) {
        ObservableList<NodeItem> items = FXCollections.observableArrayList();
        for (Node node : guide.getAllPaths().get(0)) {
            if (node instanceof Step) {
                Step step = (Step) node;
                String packageId  = step.getContent().getContentPackage("de_DE");
                ContentDescriptor content;
                try {
                    content = persistenceHandler.readContentDescriptor(guide.getId(), packageId);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to read content package. Skipping step : " + step.getId(), e);
                    continue;
                }
                items.add(new ActionItem(step, content));
            } else if (node instanceof Chapter) {
                Chapter chapter = (Chapter) node;
                Guide target = session.getGuideManager().getBean().getGuide(chapter.getCalledProcessId());
                if (target != null) {
                    items.add(new ChapterItem(chapter, target));
                } else {
                    LOGGER.log(Level.WARNING, "Missing guide referenced as chapter: {0}", chapter.getCalledProcessId());
                }
            }
        }
        stepListView.setItems(items);
        final ReadOnlyIntegerProperty selectedIndexProperty = stepListView.getSelectionModel().selectedIndexProperty();
        moveNodeDownButton.disableProperty().bind(selectedIndexProperty.lessThan(0).or(selectedIndexProperty.greaterThanOrEqualTo(items.size() - 1)).or(multipleStepSelectionProperty));
    }
    
    public void openGuide(GuideModel guideModel) {
        Guide guide = guideModel.getBean();
        updateStepList(guide);
        Stack<Session.GuideStackItem> guideStack = session.getGuideStack();
        if (guideStack.isEmpty()) {
            headerChapterInfo.setVisible(false);
        } else {
            GuideModel parentGuide = guideStack.peek().getGuideModel();
            parentGuideTitle.setText(parentGuide.titleProperty().get());
            headerChapterInfo.setVisible(true);
        }
        String vrSceneId = guide.getMetadata().getVRScene();
        if (vrSceneId != null) {
            File sceneFile = new File(persistenceHandler.getGuideDir(guide.getId()), "scene.xml");
            try {
                String sceneXml = PersistenceHandler.readFile(sceneFile);
                VRScene vrScene = new VRScene(XMLUtils.importFromString(sceneXml));
                if (!vrScene.getId().equals(vrSceneId)) throw new IllegalArgumentException("Verknüpfte und hinterlegte VR-Szene stimmen nicht überein.");
                session.setVRScene(vrScene);
            } catch (IOException | IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Failed to read scene descriptor.", ex);
                showAlert("Die Informationen zur verknüpften VR-Szene konnten nicht geladen werden.");
            }
            
        } else {
            session.setVRScene(null);
        }
    }
    
    @FXML
    private void showSettingsWindow(ActionEvent event) {
        showPreferences();
    }
    
    /*
    @FXML
    private void showSelectGuideWindow(ActionEvent event) {
        Stage stage = new Stage();
        stage.setScene(scenes.get(AppScene.OPEN_GUIDE));
        stage.setTitle("Anleitung öffnen");
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/icon.png")));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(scene.getWindow());
        stage.showAndWait();
    }
    */
    
    @FXML
    private void selectGuide(ActionEvent event) {
        Window window = scene.getWindow();
        SelectGuideController controller = new SelectGuideController(window, "Anleitung öffnen", session.getGuideManager());
        controller.showAndWait().ifPresent(selectedGuide -> {
            session.setGuide(selectedGuide);
        });
    }
    
    @FXML
    private void showSynchronizeWindow(ActionEvent event) {
        session.setGuide(null);
        
        Stage stage = new Stage();
        stage.setScene(scenes.get(AppScene.SYNCHRONIZE));
        stage.setTitle("Anleitungen synchronisizeren");
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/icon.png")));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(scene.getWindow());
        stage.show();
    }
    
    @FXML
    private void openChapter(ActionEvent event) {
        session.getGuideStack().push(new Session.GuideStackItem(session.getGuide(), stepListView.getSelectionModel().getSelectedItem()));
        String guideId = session.getChapter().getCalledProcessId();
        Guide guide = session.getGuideManager().getBean().getGuide(guideId);
        GuideModel guideModel = new GuideModel(guide, MainApp.LANG);
        session.setGuide(guideModel);
    }
    
    @FXML
    public void editChapter(ActionEvent event) {
        Guide guide = session.getGuide().getBean();
        
        Window window = scene.getWindow();
        SelectGuideController controller = new SelectGuideController(window, "Anleitung auswählen", session.getGuideManager());
        controller.showAndWait().ifPresent(selectedGuide -> {
            // Modify chapter 
            MultipleSelectionModel<NodeItem> selectionModel = stepListView.getSelectionModel();
            Chapter chapter = (Chapter) selectionModel.getSelectedItem().getNode();
            chapter.setCalledProcessId(selectedGuide.getId());
            session.setChapter(null);
            session.setChapter(chapter);
            
            // Update list.
            int selectedIndex = selectionModel.getSelectedIndex();
            updateStepList(guide);
            selectionModel.select(selectedIndex);
            
            // Persist changes of guide.
            try {
                persistenceHandler.writeGuide(guide);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to store change to guide.", ex);
            }
        });
    }
    
    @FXML
    public void returnToParentGuide(ActionEvent event) {
        Session.GuideStackItem stackItem = session.getGuideStack().pop();
        session.setGuide(stackItem.getGuideModel());
        stepListView.getSelectionModel().select(stackItem.getSelectedItem());
    }
    
    @FXML
    private void handleVideoPlay(ActionEvent event) {
        mediaPlayer.seek(mediaPlayer.getStartTime());
        mediaPlayer.play();
    }
    
    @FXML
    private void handleVideoStop(ActionEvent event) {
        mediaPlayer.stop();
    }
    
    @FXML
    private void undo(ActionEvent event) {
        if (changeManager.hasChanges()) {
            try {
                changeManager.undoLatestChange();
            } catch (IllegalStateException ex) {
                // nothing
            }
        }
    }
    
    @FXML
    private void showEditGuideWindow(ActionEvent event) {
        Stage stage = new Stage();
        stage.setScene(scenes.get(AppScene.EDIT_GUIDE));
        stage.setTitle("Informationen zur Anleitung");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/lc_editdoc.png")));
        stage.setResizable(false);
        stage.initOwner(scene.getWindow());
        stage.show();
    }
    
    @FXML
    private void editStepInfo(ActionEvent event) {
        ContentModel content = session.getContent();
        Window window = scene.getWindow();
        EditTextController dialog = new EditTextController(window, "Beschreibung des Schrittes", content.infoProperty().get());
        dialog.showAndWait().ifPresent((info) -> {
            String oldText = content.infoProperty().get();
            BaseAction change = new EditStepInfoAction(info, content, session.getGuide().getBean(), persistenceHandler);
            change.addActionListener(new ActionListener() {
                @Override
                public void changePerformed(Action change) {
                    stepListView.getSelectionModel().getSelectedItem().labelProperty().set(info);
                }

                @Override
                public void changeUndone(Action change) {
                    stepListView.getSelectionModel().getSelectedItem().labelProperty().set(oldText != null ? oldText : "[n/a]");
                }

                @Override
                public void changeFailed(Action change, ActionFailedException e) {
                    LOGGER.log(Level.WARNING, "Failed to edit step info.", e);
                }

                @Override
                public void undoneFailed(Action change, ActionFailedException e) {
                    showAlert("Die Beschreibung konnte nicht zurückgesetzt werden");
                }
            });
            changeManager.performChange(change);
        });
    }
    
    @FXML
    private void mediaSelectVideo(ActionEvent event) {
        Window window = scene.getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Video auswählen");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.mpg", "*.mpeg")
        );
        String mediaDir = MainApp.CONFIG.getMediaDir();
        if (mediaDir != null ) {
            File mediaDirFile = new File(mediaDir);
            if (mediaDirFile.exists()) {
                chooser.setInitialDirectory(mediaDirFile);
            }
        }
        
        File selectedFile = chooser.showOpenDialog(window);
        if (selectedFile != null) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } // Release video if necessary.
            GuideModel guide = session.getGuide();
            ContentModel content = session.getContent();
            BaseAction action = new ChangeMediaAction(guide, content, persistenceHandler, selectedFile);
            action.addActionListener(new ActionListener() {
                @Override
                public void changePerformed(Action change) {
                    // do nothing
                }

                @Override
                public void changeUndone(Action change) {
                    // do nothing
                }

                @Override
                public void changeFailed(Action change, ActionFailedException e) {
                    showAlert("Das Video konnte auf Grund eines Verarbeitungsfehlers leider nicht übernommen werden.");
                }

                @Override
                public void undoneFailed(Action change, ActionFailedException e) {
                    showAlert("Das Medienobjekt konnte auf Grund eines Fehlers nicht zurückgesetzt werden.");
                }
            });
            changeManager.performChange(action);
            
            String parentDir = selectedFile.getParent();
            if (parentDir != null) {
                MainApp.CONFIG.setMediaDir(parentDir);
            }
        }
    }
    
    @FXML
    private void mediaSelectImage(ActionEvent event) {
        Window window = scene.getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Bild auswählen");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg")
        );
        String mediaDir = MainApp.CONFIG.getMediaDir();
        if (mediaDir != null ) {
            File mediaDirFile = new File(mediaDir);
            if (mediaDirFile.exists()) {
                chooser.setInitialDirectory(mediaDirFile);
            }
        }
        
        File selectedFile = chooser.showOpenDialog(window);
        if (selectedFile != null) {
            if (mediaPlayer != null) mediaPlayer.dispose(); // Release video if necessary.
            GuideModel guide = session.getGuide();
            ContentModel content = session.getContent();
            BaseAction action = new ChangeMediaAction(guide, content, persistenceHandler, selectedFile);
            action.addActionListener(new ActionListener() {
                @Override
                public void changePerformed(Action change) {
                    // nothing more to do
                }

                @Override
                public void changeUndone(Action change) {
                    // nothing more to do
                }

                @Override
                public void changeFailed(Action change, ActionFailedException e) {
                    showAlert("Das Bild konnte auf Grund eines Dateifehlers leider nicht übernommen werden.");
                }

                @Override
                public void undoneFailed(Action change, ActionFailedException e) {
                    showAlert("Die Medienobjekt konnte auf Grund eines Fehlers nicht zurückgesetzt werden.");
                }
            });
            
            changeManager.performChange(action);
            
            
            String parentDir = selectedFile.getParent();
            if (parentDir != null) {
                MainApp.CONFIG.setMediaDir(parentDir);
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
    
    @FXML
    private void mediaRemove(ActionEvent event) {
        GuideModel guide = session.getGuide();
        ContentModel content = session.getContent();
        
        if (mediaPlayer != null) mediaPlayer.dispose();
        BaseAction action = new RemoveMediaAction(guide.getBean(), content, persistenceHandler);
        action.addActionListener(new ActionListener() {
            @Override
            public void changePerformed(Action change) {
                // do nothing
            }

            @Override
            public void changeUndone(Action change) {
                // do nothing
            }

            @Override
            public void changeFailed(Action change, ActionFailedException e) {
                showAlert("Das Medienobjekt konnte auf Grund eines Fehler nicht gelöscht werden.");
            }

            @Override
            public void undoneFailed(Action change, ActionFailedException e) {
                showAlert("Das Medienobjekt konnte auf Grund eines Fehlers nicht wiederhergestellt werden.");
            }
        });
        changeManager.performChange(action);
    }
    
    @FXML
    private void openContentFolder(ActionEvent event) {
        File contentDir = persistenceHandler.getContentPackageDir(session.getGuide().getId(), session.getContent().getId());
        try {
            Desktop.getDesktop().browse(contentDir.toURI());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to open system explorer.", ex);
        }
    }
    
    @FXML
    private void editWarning(ActionEvent event) {
        WarningModel warning = warningsListView.getSelectionModel().getSelectedItem();
        
        Window window = scene.getWindow();
        EditTextController dialog = new EditTextController(window, "Warnung bearbeiten", warning.textProperty().get());
        dialog.showAndWait().ifPresent((newText) -> {
            BaseAction change = new EditWarningAction(warning, newText, session.getContent(), session.getGuide(), persistenceHandler);
            change.addActionListener(new ActionListener() {
                @Override
                public void changePerformed(Action change) {
                    // nothing
                }

                @Override
                public void changeUndone(Action change) {
                    // nothing
                }

                @Override
                public void changeFailed(Action change, ActionFailedException e) {
                    showAlert("Die Änderung konnte auf Grund eines Dateifehlers leider nicht übernommen werden.");
                }

                @Override
                public void undoneFailed(Action change, ActionFailedException e) {
                    showAlert("Die Warnung konnte nicht zurückgesetzt werden.");
                }
            });
            changeManager.performChange(change);
        });
    }
    
    @FXML
    private void removeWarning(ActionEvent event) {
        WarningModel warning = warningsListView.getSelectionModel().getSelectedItem();
        warningsListView.getSelectionModel().clearSelection();
        
        BaseAction change = new RemoveWarningAction(warning, session.getContent(), session.getGuide(), persistenceHandler);
        change.addActionListener(new ActionListener() {
            @Override
            public void changePerformed(Action change) {
                // nothing
            }

            @Override
            public void changeUndone(Action change) {
                // nothing
            }

            @Override
            public void changeFailed(Action change, ActionFailedException e) {
                showAlert("Die Änderung konnte auf Grund eines Dateifehlers leider nicht übernommen werden.");
            }

            @Override
            public void undoneFailed(Action change, ActionFailedException e) {
                showAlert("Die Warnung konnte nicht wiederhergestellt werden.");
            }
        });
        changeManager.performChange(change);
    }
    
    @FXML
    private void addWarning(ActionEvent event) {
        Window window = scene.getWindow();
        EditTextController dialog = new EditTextController(window, "Neue Warnung erstellen", "");
        dialog.setSuggestions(new ArrayList<>(MainApp.CONFIG.getCachedWarnings()));
        dialog.showAndWait().ifPresent((text) -> {
            MainApp.CONFIG.addWarningToCache(text);
            WarningModel warningModel = new WarningModel(new Warning(text));
            
            BaseAction change = new AddWarningAction(warningModel, session.getContent(), session.getGuide(), persistenceHandler);
            change.addActionListener(new ActionListener() {
                @Override
                public void changePerformed(Action change) {
                    if (warningsListView.getItems().isEmpty()) {
                        warningsListView.itemsProperty().bind(session.getContent().warningsProperty());
                    }
                }

                @Override
                public void changeUndone(Action change) {
                    // nothing
                }

                @Override
                public void changeFailed(Action change, ActionFailedException e) {
                   showAlert("Die Änderung konnte auf Grund eines Dateifehlers leider nicht übernommen werden.");
                }

                @Override
                public void undoneFailed(Action change, ActionFailedException e) {
                    showAlert("Die Warnung konnte nicht entfernt werden.");
                }
            });
            changeManager.performChange(change);
        });
    }
    
    @FXML
    private void editHint(ActionEvent event) {
        HintModel hint = hintsListView.getSelectionModel().getSelectedItem();
        
        Window window = scene.getWindow();
        EditTextController dialog = new EditTextController(window, "Warnung bearbeiten", hint.textProperty().get());
        dialog.showAndWait().ifPresent((newText) -> {
            BaseAction change = new EditHintAction(hint, newText, session.getContent(), session.getGuide(), persistenceHandler);
            change.addActionListener(new ActionListener() {
                @Override
                public void changePerformed(Action change) {
                    // nothing
                }

                @Override
                public void changeUndone(Action change) {
                    // nothing
                }

                @Override
                public void changeFailed(Action change, ActionFailedException e) {
                    showAlert("Die Änderung konnte auf Grund eines Dateifehlers leider nicht übernommen werden.");
                }

                @Override
                public void undoneFailed(Action change, ActionFailedException e) {
                    showAlert("Der Hinweis konnte nicht zurückgesetzt werden.");
                }
            });
            changeManager.performChange(change);
        });
    }
    
    @FXML
    private void removeHint(ActionEvent event) {
        HintModel hint = hintsListView.getSelectionModel().getSelectedItem();
        hintsListView.getSelectionModel().clearSelection();
        
        BaseAction change = new RemoveHintAction(hint, session.getContent(), session.getGuide(), persistenceHandler);
        change.addActionListener(new ActionListener() {
            @Override
            public void changePerformed(Action change) {
                // nothing
            }

            @Override
            public void changeUndone(Action change) {
                // nothing
            }

            @Override
            public void changeFailed(Action change, ActionFailedException e) {
                showAlert("Die Änderung konnte auf Grund eines Dateifehlers leider nicht übernommen werden.");
            }

            @Override
            public void undoneFailed(Action change, ActionFailedException e) {
                showAlert("Der Hinweis konnte nicht wiederhergestellt werden.");
            }
        });
        changeManager.performChange(change);
    }
    
    @FXML
    private void addHint(ActionEvent event) {
        Window window = scene.getWindow();
        EditTextController dialog = new EditTextController(window, "Neuen Hinweis erstellen", "");
        dialog.setSuggestions(new ArrayList<>(MainApp.CONFIG.getCachedHints()));
        dialog.showAndWait().ifPresent((text) -> {
            MainApp.CONFIG.addHintToCache(text);
            HintModel hintModel = new HintModel(new Hint(text));
            
            BaseAction change = new AddHintAction(hintModel, session.getContent(), session.getGuide(), persistenceHandler);
            change.addActionListener(new ActionListener() {
                @Override
                public void changePerformed(Action change) {
                    if (hintsListView.getItems().isEmpty()) {
                        hintsListView.itemsProperty().bind(session.getContent().hintsProperty());
                    }
                }

                @Override
                public void changeUndone(Action change) {
                    // nothing
                }

                @Override
                public void changeFailed(Action change, ActionFailedException e) {
                    showAlert("Die Änderung konnte auf Grund eines Dateifehlers leider nicht übernommen werden.");
                }

                @Override
                public void undoneFailed(Action change, ActionFailedException e) {
                    showAlert("Der Hinweis konnte nicht entfernt werden.");
                }
            });
            changeManager.performChange(change);
        });
    }
    
    @FXML
    private void closeApplication(ActionEvent event) {
        Stage stage = (Stage) scene.getWindow();
        stage.close();
    }
    
    @FXML
    private void editMediaObject(ActionEvent event) {
        ContentModel content = session.getContent();
        File contentPackage = persistenceHandler.getContentPackageDir(session.getGuide().getId(), session.getContent().getId());
        ContentModel.Media media = content.mediaProperty().get();
        if (media != null) {
            File mediaFile = new File(contentPackage, media.getPath());
            switch (media.getMimeType()) {
                case "video/mp4":
                    String videoEditorExecutable = MainApp.CONFIG.getVideoEditor();
                    try {
                        if (videoEditorExecutable != null) {
                            Runtime.getRuntime().exec(videoEditorExecutable + " " + mediaFile.getAbsolutePath());
                        } else {
                            Desktop.getDesktop().edit(mediaFile);
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Failed to open media file for editing.", ex);
                        showAlert("Die Videobearbeitung konnte leider nicht gestartet werden.");
                    }
                    break;
                case "image/jpeg":
                case "image/png":
                    String imageEditorExecutable = MainApp.CONFIG.getImageEditor();
                    try {
                        if (imageEditorExecutable != null) {
                            Runtime.getRuntime().exec(imageEditorExecutable + " " + mediaFile.getAbsolutePath());
                        } else {
                            Desktop.getDesktop().edit(mediaFile);
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Failed to open image editor.", ex);
                        showAlert("Die Bildbearbeitung konnte leider nicht gestartet werden.");
                    }
            }
        }
    }
    
    @FXML
    private void refreshMedia(ActionEvent event) {
        updateMediaPreview(session.contentProperty().get().mediaProperty().get());
    }
    
    @FXML
    private void deleteNode(ActionEvent event) {
        final MultipleSelectionModel<NodeItem> selectionModel = stepListView.getSelectionModel();
        final int selectedIndex = selectionModel.getSelectedIndex();
        final NodeItem selectedItem = selectionModel.getSelectedItem();
        final Node selectedNode = selectedItem.getNode();
        final Guide guide = session.getGuide().getBean();
        
        // Remove step from guide.
        selectionModel.clearSelection();
        BaseAction change = new DeleteNodeAction(selectedNode, guide, persistenceHandler);
        change.addActionListener(new ActionListener() {
            @Override
            public void changePerformed(Action change) {
                // Update step list.
                updateStepList(guide);

                // Select nearest item.
                if (!stepListView.getItems().isEmpty()) {
                    selectionModel.select(Math.min(selectedIndex, stepListView.getItems().size() - 1));
                }
            }

            @Override
            public void changeUndone(Action change) {
                // Update step list.
                updateStepList(guide);
                selectionModel.select(selectedIndex);
            }

            @Override
            public void changeFailed(Action change, ActionFailedException e) {
                LOGGER.log(Level.WARNING, "Failed to perform change.", e);
                showAlert("Der Knoten konnte auf Grund eines Fehlers nicht gelöscht werden.");
            }

            @Override
            public void undoneFailed(Action change, ActionFailedException e) {
                showAlert("Der Knoten konnte nicht wiederhergestellt werden.");
            }
        });
        changeManager.performChange(change);
    }
    
    @FXML
    private void moveNodeUp(ActionEvent event) {
        MultipleSelectionModel<NodeItem> selectionModel = stepListView.getSelectionModel();
        int selectedIndex = selectionModel.getSelectedIndex();
        NodeItem selectedItem = selectionModel.getSelectedItem();
        Node selectedNode = selectedItem.getNode();
        Guide guide = session.getGuide().getBean();
        Node predecessor;
        if (selectedIndex > 1) {
            predecessor = stepListView.getItems().get(selectedIndex - 2).getNode();
        } else {
            predecessor = guide.getStart();
        }
        BaseAction change = new MoveNodeAction(selectedNode, predecessor, guide, persistenceHandler);
        change.addActionListener(new ActionListener() {
            @Override
            public void changePerformed(Action change) {
                updateStepList(guide);
                selectionModel.select(selectedIndex - 1);
            }

            @Override
            public void changeUndone(Action change) {
                updateStepList(guide);
                selectionModel.select(selectedIndex);
            }

            @Override
            public void changeFailed(Action change, ActionFailedException e) {
                LOGGER.log(Level.WARNING, "Failed move node.", e);
            }

            @Override
            public void undoneFailed(Action change, ActionFailedException e) {
                LOGGER.log(Level.WARNING, "Failed move node.", e);
            }
        });
        
        changeManager.performChange(change);
    }
    
    @FXML
    private void moveNodeDown(ActionEvent event) {
        MultipleSelectionModel<NodeItem> selectionModel = stepListView.getSelectionModel();
        int selectedIndex = selectionModel.getSelectedIndex();
        NodeItem selectedItem = selectionModel.getSelectedItem();
        Node selectedNode = selectedItem.getNode();
        Guide guide = session.getGuide().getBean();
        Node predecessor = stepListView.getItems().get(selectedIndex + 1).getNode();
        
        BaseAction change = new MoveNodeAction(selectedNode, predecessor, guide, persistenceHandler);
        change.addActionListener(new ActionListener() {
            @Override
            public void changePerformed(Action change) {
                updateStepList(guide);
                selectionModel.select(selectedIndex + 1);
            }

            @Override
            public void changeUndone(Action change) {
                updateStepList(guide);
                selectionModel.select(selectedIndex);
            }

            @Override
            public void changeFailed(Action change, ActionFailedException e) {
                LOGGER.log(Level.WARNING, "Failed move node.", e);
            }

            @Override
            public void undoneFailed(Action change, ActionFailedException e) {
                LOGGER.log(Level.WARNING, "Failed move node.", e);
            }
        });
        changeManager.performChange(change);
    }
    
    @FXML
    private void createNewStep(ActionEvent event) {
        Guide guide = session.getGuide().getBean();
        
        MultipleSelectionModel<NodeItem> selectionModel = stepListView.getSelectionModel();
        int selectedIndex = selectionModel.getSelectedIndex();
        Node predecessor;
        ObservableList<NodeItem> items = stepListView.getItems();
        if (selectedIndex < 0) {
            // no selection
            predecessor = items.size() > 0 ? items.get(items.size() - 1).getNode() : guide.getStart();
        } else {
            predecessor = selectionModel.getSelectedItem().getNode();
        }

        BaseAction change = new AddStepAction(predecessor, guide, persistenceHandler);
        change.addActionListener(new ActionListener() {
            @Override
            public void changePerformed(Action change) {
                updateStepList(guide);
                selectionModel.select(selectedIndex >= 0 ? selectedIndex + 1 : items.size());
                editStepInfo(event);
            }

            @Override
            public void changeUndone(Action change) {
                selectionModel.clearSelection();
                updateStepList(guide);
            }

            @Override
            public void changeFailed(Action change, ActionFailedException e) {
                LOGGER.log(Level.WARNING, "Failed to add step.", e);
            }

            @Override
            public void undoneFailed(Action change, ActionFailedException e) {
                LOGGER.log(Level.WARNING, "Failed to remove step.", e);
            }
        });
        changeManager.performChange(change);
    }
    
    @FXML
    private void createNewChapter(ActionEvent event) {
        Guide guide = session.getGuide().getBean();
        
        Window window = scene.getWindow();
        SelectGuideController controller = new SelectGuideController(window, "Anleitung auswählen", session.getGuideManager());
        controller.showAndWait().ifPresent(selectedGuide -> {
            // Add chapter to guide
            MultipleSelectionModel<NodeItem> selectionModel = stepListView.getSelectionModel();
            int selectedIndex = selectionModel.getSelectedIndex();
            Node predecessor;
            ObservableList<NodeItem> items = stepListView.getItems();
            if (selectedIndex < 0) {
                // no selection
                predecessor = items.size() > 0 ? items.get(items.size() - 1).getNode() : guide.getStart();
            } else {
                predecessor = selectionModel.getSelectedItem().getNode();
            }
            BaseAction change = new AddChapterAction(selectedGuide.getId(), predecessor, guide, persistenceHandler);
            change.addActionListener(new ActionListener() {
                @Override
                public void changePerformed(Action change) {
                    updateStepList(guide);
                    selectionModel.select(selectedIndex >= 0 ? selectedIndex + 1 : items.size());
                }

                @Override
                public void changeUndone(Action change) {
                    selectionModel.clearSelection();
                    updateStepList(guide);
                }

                @Override
                public void changeFailed(Action change, ActionFailedException e) {
                    LOGGER.log(Level.WARNING, "Failed to add chapter.", e);
                    showAlert("Das Kapitel konnte auf Grund eines Fehlers nicht eingebunden werden.");
                }

                @Override
                public void undoneFailed(Action change, ActionFailedException e) {
                    LOGGER.log(Level.WARNING, "Failed to remove chapter.", e);
                    showAlert("Das Kaptiel konnte auf Grund eines Fehler nicht wieder entfernt werden.");
                }
            });
            changeManager.performChange(change);
        });
    }
    
    @FXML
    private void createNewGuide(ActionEvent event) {
        Guide newGuide = new Guide(UUID.randomUUID().toString());
        newGuide.setTitle(MainApp.LANG, "[Neue Anleitung]");
        newGuide.setDescription(MainApp.LANG, "Derzeit ist keine Beschreibung der Anleitung verfügbar.");
        
        GuideModel guideModel = new GuideModel(newGuide, MainApp.LANG);
        session.getGuideManager().guidesProperty().add(guideModel);
        session.setGuide(guideModel);
        
        try {
            persistenceHandler.writeGuide(newGuide);
            showEditGuideWindow(event);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to store change to guide.", ex);
        }
    }
    
    @FXML
    private void deleteGuide(ActionEvent event) {
        GuideModel guide = session.getGuide();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Anleitung löschen");
        alert.setHeaderText("Wollen sie die Anleitung \"" + guide.titleProperty().get() + "\" wirklich löschen?");
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/icon.png")));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            session.getGuideManager().guidesProperty().remove(guide);
            session.setGuide(null);
            persistenceHandler.deleteGuide(guide.getId());
        }
    }
    
    @FXML
    private void addTool(ActionEvent event) {
        Window window = scene.getWindow();
        SelectToolController controller = new SelectToolController(window, "Werkzeug hinzufügen", session);
        controller.showAndWait().ifPresent(toolAnnotation -> {
            BaseAction change = new AddToolAction(toolAnnotation, session.getStep(), session.getGuide(), persistenceHandler);
            change.addActionListener(new ActionListener() {
                @Override
                public void changePerformed(Action change) {
                    updateToolList();
                }

                @Override
                public void changeUndone(Action change) {
                    updateToolList();
                }

                @Override
                public void changeFailed(Action change, ActionFailedException e) {
                    LOGGER.log(Level.WARNING, "Failed add tool.", e);
                }

                @Override
                public void undoneFailed(Action change, ActionFailedException e) {
                    LOGGER.log(Level.WARNING, "Failed remove tool.", e);
                }
            });
            changeManager.performChange(change);
        });
    }
    
    @FXML
    private void removeTool(ActionEvent event) {
        Step step = session.getStep().getBean();
        ToolAnnotation toolAnnotation = toolListView.getSelectionModel().getSelectedItem();
        BaseAction change = new RemoveToolAction(toolAnnotation, session.getStep(), session.getGuide(), persistenceHandler);
        change.addActionListener(new ActionListener() {
            @Override
            public void changePerformed(Action change) {
                updateToolList();
            }

            @Override
            public void changeUndone(Action change) {
                updateToolList();
            }

            @Override
            public void changeFailed(Action change, ActionFailedException e) {
                LOGGER.log(Level.WARNING, "Failed remove tool.", e);
            }

            @Override
            public void undoneFailed(Action change, ActionFailedException e) {
                LOGGER.log(Level.WARNING, "Failed add tool.", e);
            }
        });
        changeManager.performChange(change);
    }
    
    @FXML
    private void selectVRNode(ActionEvent event) {
        Window window = scene.getWindow();
        SelectVRNodeController controller = new SelectVRNodeController(window, "VR-Knoten auswählen", session);
        controller.showAndWait().ifPresent(sceneAnnotation -> {
            // TODO Refactor as action.
            BaseAction change = new SelectVRNodeAction(sceneAnnotation, session.getStep(), session.getGuide(), persistenceHandler);
            changeManager.performChange(change);
        });
    }
    
    @FXML
    private void removeVRNode(ActionEvent event) {
        BaseAction change = new SelectVRNodeAction(null, session.getStep(), session.getGuide(), persistenceHandler);
        changeManager.performChange(change);
    }
    
    @FXML
    private void combineSteps(ActionEvent event) {
        GuideManager guideManager = session.guideManagerProperty().get().getBean();
        List<Node> selectedNodes = stepListView.getSelectionModel().getSelectedItems().stream().map(item -> item.getNode()).collect(Collectors.toList());
        Guide currentGuide = session.getGuide().getBean();
        try {
            Guide newGuide = currentGuide.combineStepsToChapter(UUID.randomUUID().toString(), selectedNodes);
            newGuide.setTitle(MainApp.LANG, "Kapitel von: " + currentGuide.getMetadata().getTitle());
            newGuide.setDescription(MainApp.LANG, "Derzeit ist keine Beschreibung der Anleitung verfügbar.");
            persistenceHandler.writeGuide(newGuide);
            persistenceHandler.writeGuide(currentGuide);
            
            for (Node node : selectedNodes) {
                ContentAnnotation content = node.getContent();
                if (content != null && content.getContentPackage() != null) {
                    persistenceHandler.moveContentPackage(content.getContentPackage(), currentGuide.getId(), newGuide.getId());
                }
            }
            
            GuideModel guideModel = new GuideModel(newGuide, MainApp.LANG);
            session.getGuideManager().guidesProperty().add(guideModel);
            session.getGuideStack().push(new Session.GuideStackItem(session.getGuide(), null));
            session.setGuide(guideModel);
            
            showEditGuideWindow(event);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Failed to combine steps to chapter.", e);
            showAlert("Die Knoten konnten nicht zu einem Kapitel zusammengefasst werden.");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to store new guide.", e);
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
    
    
}
