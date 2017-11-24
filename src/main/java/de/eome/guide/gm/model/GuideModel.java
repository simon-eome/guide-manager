package de.eome.guide.gm.model;

import de.eome.guide.api.Guide;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Wrapper to enable bindings on fields of a guide object.
 * @author simon.schwantzer(at)im-c.de
 */
public class GuideModel {
    private final Guide guide;
    private final String lang;
    private final StringProperty titleProperty, descriptionProperty;
    private final StringProperty syncStatusProperty;
    // private final ObjectProperty<MetadataAnnotation> metadataProperty;
    
    /**
     * Creates a new wrapper for the given guide.
     * @param guide Guide to wrap.
     * @param lang Language code for the fields, e.g. "de_DE".
     */
    public GuideModel(Guide guide, String lang) {
        this.guide = guide;
        this.lang = lang;
        this.titleProperty = new SimpleStringProperty(guide.getMetadata().getTitle(lang)) {
            @Override
            public void set(String newValue) {
                super.set(newValue);
                guide.getMetadata().setTitle(lang, newValue);
            }
        };
        
        this.descriptionProperty = new SimpleStringProperty(guide.getMetadata().getDescription(lang)) {
            @Override
            public void set(String newValue) {
                super.set(newValue);
                guide.getMetadata().setDescription(lang, newValue);
            }
        };
        this.metadataProperty = new SimpleObjectProperty<>(guide.getMetadata());
        
        this.syncStatusProperty = new SimpleStringProperty("none");
    }
    
    /**
     * Returns the guide wrapped by this model.
     * @return Guide object.
     */
    public Guide getBean() {
        return guide;
    }
    
    public StringProperty titleProperty() {
        return titleProperty;
    }
    
    public StringProperty descriptionProperty() {
        return descriptionProperty;
    }
    
    public StringProperty syncStatusProperty() {
        return syncStatusProperty;
    }
    
    public ObjectProperty<MetadataAnnotation> metadataProperty() {
        return metadataProperty;
    }
    
    public String getId() {
        return guide.getId();
    }
}
