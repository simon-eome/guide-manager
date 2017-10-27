package de.eome.guide.gm.controller;

import de.eome.guide.gm.MainApp;
import de.eome.guide.gm.Session;
import de.eome.guide.gm.model.Parameter;
import de.eome.guide.gm.model.Tool;
import de.glassroom.gpe.annotations.ToolAnnotation;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Controller for a tool selection dialog.
 * @author simon.schwantzer(at)im-c.de
 */
public class SelectToolController {
    private static final Logger LOGGER = Logger.getLogger(SelectToolController.class.getName());
    private static final Tool NEW_TOOL = new Tool.Builder("-").addName("de-DE", "<Neues Werkzeug>").build();
    
    @FXML Label toolParamsLabel;
    @FXML TextField toolIdInput, toolNameInput;
    @FXML ComboBox<Tool> toolComboBox;
    @FXML VBox toolParameterPane;
    
    private final Stage stage;
    private final Session session;
    private boolean isIdChanged;
    
    public SelectToolController(Window owner, String title, Session session) {
        stage = new Stage();
        this.session = session;
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SelectToolDialog.fxml"));
        fxmlLoader.setController(this);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load tool selection dialog.", ex);
            throw new RuntimeException("Failed to load tool selection dialog.", ex);
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
        List<Tool> tools = MainApp.CONFIG.getTools().stream()
            .sorted((tool1, tool2) -> {
                if (tool1 == null && tool2 == null) {
                    return 0;
                } else if (tool1 == null) {
                    return -1;
                } else if (tool2 == null) {
                    return 1;
                } else {
                    return tool1.getName().compareToIgnoreCase(tool2.getName());
                }
            }).collect(Collectors.toList());
        ObservableList<Tool> observableTools = FXCollections.observableArrayList();
        observableTools.add(NEW_TOOL);
        observableTools.addAll(tools);
        toolComboBox.setItems(observableTools);
        toolComboBox.valueProperty().addListener((observer, oldTool, newTool) -> {
            if (newTool == NEW_TOOL) {
                createTool();
            } else {
                selectTool(newTool);
            }
        });
        toolComboBox.setValue(NEW_TOOL);
    }
    
    private void selectTool(Tool tool) {
        toolIdInput.setText(tool.getId());
        toolIdInput.setDisable(true);
        toolNameInput.setText(tool.getName());
        toolNameInput.setDisable(true);
        
        toolParameterPane.getChildren().clear();
        List<Parameter<?>> params = tool.getParameters();
        if (!params.isEmpty()) {
            toolParamsLabel.setVisible(true);
            params.forEach(param -> {
                ParameterItem item = new ParameterItem(param);
                toolParameterPane.getChildren().add(item);
            });
        } else {
            toolParamsLabel.setVisible(false);
        }
    }
    
    private void createTool() {
        toolParamsLabel.setVisible(false);
        toolIdInput.setDisable(false);
        toolIdInput.focusedProperty().addListener((observer, oldValue, newValue) -> isIdChanged = true);
        toolIdInput.setText("");
        isIdChanged = false;
        toolNameInput.setDisable(false);
        toolNameInput.setText("");
        toolNameInput.textProperty().addListener((observer, oldValue, newValue) -> {
            if (isIdChanged) return;
            String newId = newValue
                    .replace(" ", "-")
                    .toLowerCase();
            toolIdInput.setText(newId);
        });
        toolNameInput.requestFocus();
        
        toolParameterPane.getChildren().clear();
    }
    
    @FXML
    protected void accept(ActionEvent event) {
        try {
            validateInput();
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            return;
        }
        Tool selectedTool = toolComboBox.getValue();
        if (selectedTool == NEW_TOOL) {
            String toolId = toolIdInput.getText();
            String toolName = toolNameInput.getText();
            Tool tool = new Tool.Builder(toolId)
                    .addName(MainApp.LANG.replace("_", "-"), toolName)
                    .build();
            MainApp.CONFIG.addTool(tool);
            stage.setUserData(new ToolAnnotation(toolId));
        } else {
            ToolAnnotation toolAnnotation = new ToolAnnotation(selectedTool.getId());
            Map<Parameter, ParameterItem> controllerMap = toolParameterPane.getChildren().stream()
                    .map(node -> (ParameterItem) node)
                    .collect(Collectors.toMap(ParameterItem::getParameter, Function.identity()));
            selectedTool.getParameters().forEach(param -> {
                
                String valueString = controllerMap.get(param).getValueAsString();
                toolAnnotation.addParameter(param.getId(), valueString);
            });
            stage.setUserData(toolAnnotation);
        }
        stage.close();
    }
    
    private void validateInput() throws IllegalArgumentException {
        Tool selectedTool = toolComboBox.getValue();
        if (selectedTool == NEW_TOOL) {
            String toolId = toolIdInput.getText();
            if (toolId == null || toolId.isEmpty()) {
                throw new IllegalArgumentException("Bitte geben Sie eine ID für das Werkzeug ein.");
            }
            if (MainApp.CONFIG.getTools().stream().filter(tool -> tool.getId().equals(toolId)).count() > 0) {
                throw new IllegalArgumentException("Es gibt bereits ein Werkzeug mit der eingegebenen ID. Bitte Ändern Sie die ID.");
            }
            String toolName = toolNameInput.getText();
            if (toolName == null || toolName.isEmpty()) {
                throw new IllegalArgumentException("Bitte geben Sie eine Bezeichnung für das Werkzeug ein.");
            }
        } else {
            Map<Parameter, ParameterItem> controllerMap = toolParameterPane.getChildren().stream()
                    .map(node -> (ParameterItem) node)
                    .collect(Collectors.toMap(ParameterItem::getParameter, Function.identity()));
            selectedTool.getParameters().forEach(param -> {
                String valueString = controllerMap.get(param).getValueAsString();
                if (!param.validateValueString(valueString)) {
                    throw new IllegalArgumentException("Ungültiger Wert für Parameter " + param.getName() + " (" + param.getId() + ").");
                }
            });
        }
    }
    
    @FXML
    protected void cancel(ActionEvent event) {
        stage.setUserData(null);
        stage.close();
    }
    
    public Optional<ToolAnnotation> showAndWait() {
        stage.showAndWait();
        return Optional.ofNullable((ToolAnnotation) stage.getUserData());
    }
}
