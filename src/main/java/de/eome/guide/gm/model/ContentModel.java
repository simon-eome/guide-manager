package de.eome.guide.gm.model;

import de.glassroom.gpe.content.ContentDescriptor;
import de.glassroom.gpe.content.Hint;
import de.glassroom.gpe.content.Warning;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    public static class Media {
        private final String path;
        private final String mimeType;
        
        public Media(String mimeType, String path) {
            this.path = path;
            this.mimeType = mimeType;
        }
        
        public String getPath() {
            return path;
        }
        
        public String getMimeType() {
            return mimeType;
        }
    }
    
    private final ContentDescriptor content;
    
    private final StringProperty infoProperty;
    private final ObjectProperty<Media> mediaProperty;
    private final BooleanProperty isRoutineProperty;
    private final ListProperty<WarningModel> warningsProperty;
    private final ListProperty<HintModel> hintsProperty;
    
    public ContentModel(ContentDescriptor content) {
        this.content = content;
        
        infoProperty = new SimpleStringProperty(content.getInfo()) {
            @Override
            public void set(String newValue) {
                super.set(newValue);
                content.setInfo(newValue);
            }
        };
        
        mediaProperty = new SimpleObjectProperty<Media>() {
            @Override
            public void set(Media newValue) {
                super.set(newValue);
                if (newValue != null) {
                    content.setMedia(newValue.getMimeType(), newValue.getPath());
                } else {
                    content.setMedia(null, null);
                }
            }
        };
        if (content.getMediaPath() != null) {
            mediaProperty.set(new Media(content.getMimeType(), content.getMediaPath()));
        }
        
        isRoutineProperty = new SimpleBooleanProperty(content.isRoutineTask()) {
            @Override
            public void set(boolean newValue) {
                super.set(newValue);
                content.setRoutineTask(newValue);
            }
        };
        
        ObservableList<WarningModel> warnings = FXCollections.observableArrayList((WarningModel param) -> new Observable[]{param.textProperty()});
        warnings.addAll(content.getWarnings().stream().map(warning -> new WarningModel(warning)).collect(Collectors.toList()));
        warningsProperty = new SimpleListProperty<WarningModel>(warnings) {
            @Override
            public void set(ObservableList<WarningModel> newValue) {
                super.set(newValue);
                List<Warning> warningList = content.getWarnings();
                warningList.clear();
                newValue.forEach(warningModel -> warningList.add(warningModel.getBean()));
            }

            @Override
            public boolean remove(Object obj) {
                boolean isRemoved = super.remove(obj);
                if (isRemoved) {
                    WarningModel model = (WarningModel) obj;
                    content.getWarnings().remove(model.getBean());
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean add(WarningModel element) {
                boolean isAdded = super.add(element);
                if (isAdded) {
                    content.getWarnings().add(element.getBean());
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
                List<Hint> hintList = content.getHints();
                hintList.clear();
                newValue.forEach(hintModel -> hintList.add(hintModel.getBean()));
            }

            @Override
            public boolean remove(Object obj) {
                boolean isRemoved = super.remove(obj);
                if (isRemoved) {
                    HintModel model = (HintModel) obj;
                    content.getHints().remove(model.getBean());
                    return true;
                } else {
                    return false;
                }
            }
            
            @Override
            public boolean add(HintModel element) {
                boolean isAdded = super.add(element);
                if (isAdded) {
                    content.getHints().add(element.getBean());
                    return true;
                } else {
                    return false;
                }
            }
        };
    }
    
    public ContentDescriptor getBean() {
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
    
    public BooleanProperty isRoutineProperty() {
        return isRoutineProperty;
    }
    
    public ListProperty<WarningModel> warningsProperty() {
        return warningsProperty;
    }
    
    public ListProperty<HintModel> hintsProperty() {
        return hintsProperty;
    }
}
