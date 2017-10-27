package de.eome.guide.gm.controller;

import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.control.textfield.TextFields;

/**
 * Controller for a text edit dialog.
 * @author simon.schwantzer(at)im-c.de
 */
public class EditTextController {
    
    @FXML private TextField textInput;
    
    private final String text;
    private final Stage stage;
    
    /**
     * Creates a new text editing dialog.
     * @param owner Owner of the dialog.
     * @param title Title of the dialog.
     * @param text Text to be pre-filled in the text area.
     */
    public EditTextController(Window owner, String title, String text) {
        stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/EditTextDialog.fxml"));
        fxmlLoader.setController(this);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(EditTextController.class.getName()).log(Level.SEVERE, "Failed to load text editing dialog.", ex);
            throw new RuntimeException("Failed to load text input dialog.", ex);
        }
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/lc_editdoc.png")));
        stage.initOwner(owner);
        
        this.text = text;
    }
    
    public void setSuggestions(List<String> suggestions) {
        TextFields.bindAutoCompletion(textInput, suggestions.toArray());
    }
    
    @FXML
    private void initialize() {
        textInput.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.TAB) {
                TextAreaSkin skin = (TextAreaSkin) textInput.getSkin();
                if (skin.getBehavior() instanceof TextAreaBehavior) {
                    TextAreaBehavior behavior = (TextAreaBehavior) skin.getBehavior();
                    if (event.isControlDown()) {
                        behavior.callAction("InsertTab");
                    } else if (event.isShiftDown()) {
                        behavior.callAction("TraversePrevious");
                    } else {
                        behavior.callAction("TraverseNext");
                    }
                    event.consume();
                }
            }
        });
    }
    
    @FXML
    private void cancel(ActionEvent event) {
        stage.setUserData(null);
        stage.close();
    }
    
    @FXML
    private void accept(ActionEvent event) {
        String newText = textInput.getText().trim();
        stage.setUserData(newText.isEmpty() || newText.equals(text) ? null : newText);
        stage.close();
    }

    /**
     * Displays the dialog and waits for a user interaction.
     * @return Option between a new non-empty text if applied or NULL.
     */
    public Optional<String> showAndWait() {
        textInput.setText(text);
        textInput.requestFocus();
        textInput.selectAll();
        stage.showAndWait();
        return Optional.ofNullable((String) stage.getUserData());
    }
}
