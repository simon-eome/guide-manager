package de.eome.guide.gm.model;

import de.eome.guide.api.IconizedMessage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Wrapper to enable bindings for warnings.
 * @author simon.schwantzer(at)im-c.de
 */
public class WarningModel {
    private final IconizedMessage warning;
    private final StringProperty textProperty;
    
    public WarningModel(IconizedMessage warning) {
        this.warning = warning;
        textProperty = new SimpleStringProperty(warning.getText()) {
            @Override
            public void set(String newValue) {
                super.set(newValue);
                warning.setText(newValue);
            }
        };
    }
    
    public IconizedMessage getBean() {
        return warning;
    }
    
    public StringProperty textProperty() {
        return textProperty;
    }
    
    @Override
    public String toString() {
        return textProperty.get();
    }
}
