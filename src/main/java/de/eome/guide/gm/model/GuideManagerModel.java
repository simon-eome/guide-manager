package de.eome.guide.gm.model;

import de.glassroom.gpe.Guide;
import de.glassroom.gpe.GuideManager;
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
public class GuideManagerModel {
    private final GuideManager guideManager;
    private final ListProperty<GuideModel> guidesProperty;
    
    public GuideManagerModel(GuideManager guideManager, String lang) {
        this.guideManager = guideManager;
        
        ObservableList<GuideModel> guides = FXCollections.observableArrayList((GuideModel param) -> new Observable[]{param.titleProperty()});
        guides.addAll(guideManager.getGuides(null, (Guide g1, Guide g2) -> {
            String compStr1 = g1.getMetadata().getTitle();
            String compStr2 = g2.getMetadata().getTitle();
            return compStr1.compareTo(compStr2);
        }).stream().map(guide -> new GuideModel(guide, lang)).collect(Collectors.toList()));
        
        guidesProperty = new SimpleListProperty<GuideModel>(guides) {
            @Override
            public void set(ObservableList<GuideModel> newValue) {
                throw new UnsupportedOperationException("Setting the guides list is permitted.");
            }

            @Override
            public boolean remove(Object obj) {
                boolean isRemoved = super.remove(obj);
                if (isRemoved) {
                    GuideModel model = (GuideModel) obj;
                    guideManager.deleteGuide(model.getId());
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean add(GuideModel element) {
                boolean isAdded = super.add(element);
                if (isAdded) {
                    guideManager.addGuide(element.getBean());
                    return true;
                } else {
                    return false;
                }
            }
        };
        
        
    }
    
    public ListProperty<GuideModel> guidesProperty() {
        return guidesProperty;
    }
    
    public GuideManager getBean() {
        return guideManager;
    }
}
