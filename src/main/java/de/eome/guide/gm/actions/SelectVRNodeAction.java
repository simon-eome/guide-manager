package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.model.GuideModel;
import de.eome.guide.gm.model.StepModel;
import de.glassroom.gpe.annotations.SceneAnnotation;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class SelectVRNodeAction extends BaseAction {
    private final static Logger LOGGER = Logger.getLogger(SelectVRNodeAction.class.getName());
    
    private final SceneAnnotation sceneAnnotation;
    private SceneAnnotation oldSceneAnnotation;
    private final PersistenceHandler persistenceHandler;
    private final GuideModel guide;
    private final StepModel step;
    
    public SelectVRNodeAction(SceneAnnotation sceneAnnotation, StepModel step, GuideModel guideModel, PersistenceHandler persistenceHandler) {
        this.sceneAnnotation = sceneAnnotation;
        this.persistenceHandler = persistenceHandler;
        this.guide = guideModel;
        this.step = step;
    }

    @Override
    public void perform() {
        oldSceneAnnotation = step.getScene();
        step.setScene(sceneAnnotation);
        try {
            writeChange();
            notifyActionPerformed();
        } catch (ActionFailedException ex) {
            notifyActionFailed(ex);
        }
    }

    @Override
    public void undo() {
        step.setScene(oldSceneAnnotation);
        try {
            writeChange();
            notifyActionUndone();
        } catch (ActionFailedException ex) {
            notifyUndoFailed(ex);
        }
    }
    
    private void writeChange() throws ActionFailedException {
        try {
            persistenceHandler.writeGuide(guide.getBean());
        } catch (IOException ex) {
           LOGGER.log(Level.SEVERE, "Failed to update guide descriptor.", ex);
           throw new ActionFailedException("Failed to update guide descriptor.", ex);
        }
    }

    @Override
    public String getDescription() {
        return ("VR-Knoten " + (sceneAnnotation != null ? "auswählen" : "entfernen"));
    }
   
}
