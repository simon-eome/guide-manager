package de.eome.guide.gm;

import de.eome.guide.gm.model.ContentModel;
import de.eome.guide.gm.model.GuideManagerModel;
import de.eome.guide.gm.model.GuideModel;
import de.eome.guide.gm.model.NodeItem;
import de.eome.guide.gm.model.StepModel;
import de.eome.guide.gm.model.Tool;
import de.eome.guide.gm.model.VRMethod;
import de.eome.guide.gm.model.VRScene;
import de.glassroom.gpe.Chapter;
import java.util.Stack;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

/**
 * Model for a user session.
 * @author simon.schwantzer(at)im-c.de
 */
public class Session {
    public static class GuideStackItem {
        private final GuideModel guide;
        private final NodeItem selectedItem;
        
        public GuideStackItem(GuideModel guide, NodeItem selectedItem) {
            this.guide = guide;
            this.selectedItem = selectedItem;
        }
        
        public GuideModel getGuideModel() {
            return guide;
        }
        
        public NodeItem getSelectedItem() {
            return selectedItem;
        }
    }
    
    private final ObjectProperty<GuideManagerModel> guideManagerProperty;
    private final ObjectProperty<GuideModel> guideProperty;
    private final ObjectProperty<StepModel> stepProperty;
    private final ObjectProperty<ContentModel> contentProperty;
    private final ObjectProperty<Chapter> chapterProperty;
    private final ObjectProperty<VRScene> vrSceneProperty;
    
    private final Stack<GuideStackItem> guideStack;
    private ObservableList<Tool> tools;
    private ObservableList<VRMethod> vrMethods;
    
    public Session() {
        guideProperty = new SimpleObjectProperty<>();
        guideManagerProperty = new SimpleObjectProperty<>();
        stepProperty = new SimpleObjectProperty<>();
        contentProperty = new SimpleObjectProperty<>();
        chapterProperty = new SimpleObjectProperty<>();
        guideStack = new Stack<>();
        vrSceneProperty = new SimpleObjectProperty<>();
    }

    public void setGuide(GuideModel guide) {
        guideProperty.set(guide);
    }

    public GuideModel getGuide() {
        return guideProperty.get();
    }
    
    public ObjectProperty<GuideModel> guideProperty() {
        return guideProperty;
    }
    
    public void setVRScene(VRScene vrScene) {
         vrSceneProperty.set(vrScene);
    }
    
    public VRScene getVRScene() {
        return vrSceneProperty.get();
    }
    
    public ObjectProperty<VRScene> vrSceneProperty() {
        return vrSceneProperty;
    }
    
    public void setGuideManager(GuideManagerModel guideManagerModel) {
        guideManagerProperty.set(guideManagerModel);
    }
    
    public GuideManagerModel getGuideManager() {
        return guideManagerProperty.get();
    }
    
    public ObjectProperty<GuideManagerModel> guideManagerProperty() {
        return guideManagerProperty;
    }
    
    public Chapter getChapter() {
        return chapterProperty.get();
    }
    
    public void setChapter(Chapter chapter) {
        chapterProperty.set(chapter);
    }
    
    public ObjectProperty<Chapter> chapterProperty() {
        return chapterProperty;
    }
    
    public StepModel getStep() {
        return stepProperty.get();
    }
    
    public void setStep(StepModel step) {
        stepProperty.set(step);
    }
    
    public ObjectProperty<StepModel> stepProperty() {
        return stepProperty;
    }
    
    public ContentModel getContent() {
        return contentProperty.get();
    }
    
    public void setContent(ContentModel content) {
        contentProperty.set(content);
    }
    
    public ObjectProperty<ContentModel> contentProperty() {
        return contentProperty;
    }
    
    public Stack<GuideStackItem> getGuideStack() {
        return guideStack;
    }
    
    public void setTools(ObservableList<Tool> tools) {
        this.tools = tools;
    }
    
    public ObservableList<Tool> getTools() {
        return tools;
    }
    
    public void setVRMethods(ObservableList<VRMethod> methods) {
        this.vrMethods = methods;
    }
    
    public ObservableList<VRMethod> getVRMethods() {
        return vrMethods;
    }
}
