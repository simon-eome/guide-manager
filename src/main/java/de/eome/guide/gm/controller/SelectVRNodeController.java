package de.eome.guide.gm.controller;

import de.eome.guide.gm.MainApp;
import de.eome.guide.gm.Session;
import de.eome.guide.gm.model.Parameter;
import de.eome.guide.gm.model.VRMethod;
import de.eome.guide.gm.model.VRNode;
import de.glassroom.gpe.annotations.SceneAnnotation;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Controller for the VR node selection dialog.
 * @author simon.schwantzer(at)im-c.de
 */
public class SelectVRNodeController {
    
    private static final Logger LOGGER = Logger.getLogger(SelectVRNodeController.class.getName());
    
    @FXML private Pane
            nodeParameterPane,
            nodeParameterListPane,
            methodParameterPane,
            methodParameterListPane;
    @FXML private TreeView<VRNode> vrNodeTree;
    @FXML private ChoiceBox<VRMethod> vrMethodChoice;
    @FXML private Button selectButton;
    
    private final Stage stage;
    private final Session session;    
    
    public SelectVRNodeController(Window owner, String title, Session session) {
        stage = new Stage();
        this.session = session;
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SelectVRNodeDialog.fxml"));
        fxmlLoader.setController(this);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load VR node selection dialog.", ex);
            throw new RuntimeException("Failed to load VR node selection dialog.", ex);
        }
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(true);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icon.png")));
        stage.initOwner(owner);
    }
    
    @FXML
    protected void initialize() {
        selectButton.disableProperty().bind(vrNodeTree.getSelectionModel().selectedIndexProperty().lessThan(0).or(vrMethodChoice.valueProperty().isNull()));
        
        TreeItem<VRNode> root = new TreeItem<>();
        session.getVRScene().getNodes().forEach(vrNode -> root.getChildren().add(getNodeTree(vrNode)));
        vrNodeTree.setRoot(root);
        
        vrNodeTree.getSelectionModel().selectedItemProperty().addListener((observer, oldValue, newValue)-> {
            nodeParameterListPane.getChildren().clear();
            if (newValue != null) {
                VRNode vrNode = newValue.getValue();
                if (vrNode != null) {
                    nodeParameterPane.setVisible(!vrNode.getParameters().isEmpty());
                    vrNode.getParameters().forEach(param -> {
                        ParameterItem item = new ParameterItem(param);
                        nodeParameterListPane.getChildren().add(item);
                    });
                } else {
                    nodeParameterPane.setVisible(false);
                }
            } else {
                nodeParameterPane.setVisible(false);
            }
        });
        
        vrMethodChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
            methodParameterListPane.getChildren().clear();
            if (newValue != null) {
                methodParameterPane.setVisible(!newValue.getParameters().isEmpty());
                newValue.getParameters().forEach(param -> {
                    ParameterItem item = new ParameterItem(param);
                    methodParameterListPane.getChildren().add(item);
                });
            } else {
                methodParameterPane.setVisible(false);
            }
        });
        
        List<VRMethod> vrMethods = MainApp.CONFIG.getVRMethods();
        vrMethodChoice.setItems(FXCollections.observableArrayList(vrMethods));
    }
    
    private TreeItem<VRNode> getNodeTree(VRNode vrNode) {
        TreeItem<VRNode> treeNode = new TreeItem<>(vrNode);
        vrNode.getSubNodes().forEach(subNode -> treeNode.getChildren().add(getNodeTree(subNode)));
        return treeNode;
    }
    
    @FXML
    protected void select(ActionEvent event) {
        TreeItem<VRNode> selectedItem = vrNodeTree.getSelectionModel().getSelectedItem();
        VRNode vrNode = selectedItem.getValue();
        Map<String, String> vrNodeParameters = new LinkedHashMap<>();
        if (vrNode != null) {
            Map<Parameter, ParameterItem> controllerMap = nodeParameterListPane.getChildren().stream()
                    .map(node -> (ParameterItem) node)
                    .collect(Collectors.toMap(ParameterItem::getParameter, Function.identity()));
            for (Parameter param : vrNode.getParameters()) {
                String value = controllerMap.get(param).getValueAsString();
                boolean isValid = param.validateValueString(value);
                if (!isValid) {
                    showAlert("Ungültiger Wert für Parameter " + param.getName() + " (" + param.getId() + ").");
                    return;
                }
                vrNodeParameters.put(param.getId(), value);
            }
        } else {
            return;
        }
        VRMethod vrMethod = vrMethodChoice.getValue();
        Map<String, String> vrMethodParameters = new LinkedHashMap<>();
        Map<Parameter, ParameterItem> controllerMap = methodParameterListPane.getChildren().stream()
                .map(node -> (ParameterItem) node)
                .collect(Collectors.toMap(ParameterItem::getParameter, Function.identity()));
        for (Parameter param : vrMethod.getParameters()) {
            String value = controllerMap.get(param).getValueAsString();
            boolean isValid = param.validateValueString(value);
            if (!isValid) {
                showAlert("Ungültiger Wert für Parameter " + param.getName() + " (" + param.getId() + ").");
                return;
            }
            vrMethodParameters.put(param.getId(), value);
        }
        SceneAnnotation sceneAnnotation = new SceneAnnotation(vrNode.getId());
        sceneAnnotation.setNodeParameters(vrNodeParameters);
        sceneAnnotation.setMethod(vrMethod.getId(), vrMethodParameters);
        stage.setUserData(sceneAnnotation);
        stage.close();
    }
    
    @FXML
    protected void cancel(ActionEvent event) {
        stage.setUserData(null);
        stage.close();
    }
    
    public Optional<SceneAnnotation> showAndWait() {
        stage.showAndWait();
        return Optional.ofNullable((SceneAnnotation) stage.getUserData());
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setContentText(message);
        alert.show();
    }
}
