package de.eome.guide.gm.model;

import de.eome.guide.api.Guide;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Wrapper to enable bindings on the guide manager.
 * @author simon.schwantzer(at)im-c.de
 */
public class GuideManager {
    private final ListProperty<GuideModel> guidesProperty;
    
    public GuideManager(List<Guide> guides, String lang) {
        ObservableList<GuideModel> guideModels = FXCollections.observableArrayList((GuideModel param) -> new Observable[]{param.titleProperty()});
        guides.stream().map(guide -> new GuideModel(guide, lang)).collect(Collectors.toList());
        
        guidesProperty = new SimpleListProperty<>(guideModels);
    }
    
    public ListProperty<GuideModel> guidesProperty() {
        return guidesProperty;
    }
}
