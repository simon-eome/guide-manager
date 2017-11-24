package de.eome.guide.gm.model;

import de.eome.guide.api.Content;
import de.eome.guide.api.Media;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

/**
 * Wrapper to enable bindings on a content descriptor.
 * @author simon.schwantzer(at)im-c.de
 */
public class ContentModel {
    
    private final Content content;
    private final StringProperty infoProperty;
    private final ObjectProperty<Media> mediaProperty;
    private final ListProperty<WarningModel> warningsProperty;
    private final ListProperty<HintModel> hintsProperty;
    
    public ContentModel(Content content) {
        this.content = content;
        
        infoProperty = new SimpleStringProperty(content.getDescription()) {
            @Override
            public void set(String newValue) {
                super.set(newValue);
                content.setDescription(newValue);
            }
        };
        
        mediaProperty = new SimpleObjectProperty<Media>(content.getMedia()) {
            @Override
            public void set(Media newValue) {
                super.set(newValue);
                if (newValue != null) {
                    content.setMedia(newValue);
                } else {
                    content.removeMedia();
                }
            }
        };

        ObservableList<WarningModel> warnings = FXCollections.observableArrayList((WarningModel param) -> new Observable[]{param.textProperty()});
        warnings.addAll(content.getWarnings().stream().map(warning -> new WarningModel(warning)).collect(Collectors.toList()));
        warningsProperty = new SimpleListProperty<WarningModel>(warnings) {
            @Override
            public void set(ObservableList<WarningModel> newValue) {
                super.set(newValue);
                content.getWarnings().forEach(content::removeWarning);
                newValue.forEach(warningModel -> content.addWarning(warningModel.getBean()));
            }

            @Override
            public boolean remove(Object obj) {
                boolean isRemoved = super.remove(obj);
                if (isRemoved) {
                    WarningModel model = (WarningModel) obj;
                    content.removeWarning(model.getBean());
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean add(WarningModel element) {
                boolean isAdded = super.add(element);
                if (isAdded) {
                    content.removeWarning(element.getBean());
                    return true;
                } else {
                    return false;
                }
            }
        };
        
        ObservableList<HintModel> hints = FXCollections.observableArrayList(new Callback<HintModel, Observable[]>() {
            @Override
            public Observable[] call(HintModel param) {
                return new Observable[]{param.textProperty()};
            }
        });
        hints.addAll(content.getHints().stream().map(hint -> new HintModel(hint)).collect(Collectors.toList()));
        hintsProperty = new SimpleListProperty<HintModel>(hints) {
            @Override
            public void set(ObservableList<HintModel> newValue) {
                super.set(newValue);
                content.getHints().forEach(content::removeHint);
                newValue.forEach(hintModel -> content.addHint(hintModel.getBean()));
            }

            @Override
            public boolean remove(Object obj) {
                boolean isRemoved = super.remove(obj);
                if (isRemoved) {
                    HintModel model = (HintModel) obj;
                    content.removeHint(model.getBean());
                    return true;
                } else {
                    return false;
                }
            }
            
            @Override
            public boolean add(HintModel element) {
                boolean isAdded = super.add(element);
                if (isAdded) {
                    content.addHint(element.getBean());
                    return true;
                } else {
                    return false;
                }
            }
        };
    }
    
    public Content getBean() {
        return content;
    }
    
    public StringProperty infoProperty() {
        return infoProperty;
    }
    
    public String getId() {
        return content.getId();
    }
    
    public ObjectProperty<Media> mediaProperty() {
        return mediaProperty;
    }
        
    public ListProperty<WarningModel> warningsProperty() {
        return warningsProperty;
    }
    
    public ListProperty<HintModel> hintsProperty() {
        return hintsProperty;
    }
}
