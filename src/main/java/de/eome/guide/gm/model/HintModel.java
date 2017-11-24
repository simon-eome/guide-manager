package de.eome.guide.gm.model;

import de.eome.guide.api.IconizedMessage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Wrapper to enable bindings for hints.
 * @author simon.schwantzer(at)im-c.de
 */
public class HintModel {
    private final IconizedMessage hint;
    private final StringProperty textProperty;
    
    public HintModel(IconizedMessage hint) {
        this.hint = hint;
        textProperty = new SimpleStringProperty(hint.getText()) {
            @Override
            public void set(String newValue) {
                super.set(newValue);
                hint.setText(newValue);
            }
        };
    }
    
    public IconizedMessage getBean() {
        return hint;
    }
    
    public StringProperty textProperty() {
        return textProperty;
    }
    
    @Override
    public String toString() {
        return textProperty.get();
    }
}
