package de.eome.guide.gm.model;

import de.glassroom.gpe.content.Hint;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Wrapper to enable bindings for hints.
 * @author simon.schwantzer(at)im-c.de
 */
public class HintModel {
    private final Hint hint;
    private final StringProperty textProperty;
    
    public HintModel(Hint hint) {
        this.hint = hint;
        textProperty = new SimpleStringProperty(hint.getText()) {
            @Override
            public void set(String newValue) {
                super.set(newValue);
                hint.setText(newValue);
            }
        };
    }
    
    public Hint getBean() {
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
