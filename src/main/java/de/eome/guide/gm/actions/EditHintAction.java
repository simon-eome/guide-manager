package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.model.ContentModel;
import de.eome.guide.gm.model.GuideModel;
import de.eome.guide.gm.model.HintModel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class EditHintAction extends BaseAction {
    private final static Logger LOGGER = Logger.getLogger(EditHintAction.class.getName());
    
    private final HintModel hint;
    private final PersistenceHandler persistenceHandler;
    private final String newText;
    private final String oldText;
    private final GuideModel guide;
    private final ContentModel content;
    
    public EditHintAction(HintModel hint, String newText, ContentModel content, GuideModel guideModel, PersistenceHandler persistenceHandler) {
        this.hint  = hint;
        this.persistenceHandler = persistenceHandler;
        this.guide = guideModel;
        this.content = content;
        this.newText = newText;
        this.oldText = hint.textProperty().get();
    }

    @Override
    public void perform() {
        hint.textProperty().set(newText);
        try {
            writeChange();
            notifyActionPerformed();
        } catch (ActionFailedException ex) {
            notifyActionFailed(ex);            
        }
    }

    @Override
    public void undo() {
        hint.textProperty().set(oldText);
        try {
            writeChange();
            notifyActionUndone();
        } catch (ActionFailedException ex) {
            notifyUndoFailed(ex);
        }
    }
    
    private void writeChange() throws ActionFailedException {
        try {
            persistenceHandler.writeContentDescriptor(guide.getBean(), content.getBean());
        } catch (IOException ex) {
           LOGGER.log(Level.SEVERE, "Failed to update content descriptor.", ex);
           throw new ActionFailedException("Failed to update content descriptor.", ex);
        }
    }

    @Override
    public String getDescription() {
        return ("Hinweis bearbeiten");
    }
   
}
