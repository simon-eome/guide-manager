package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.model.ContentModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.glassroom.gpe.Guide;

/**
 * Action to remove media object from a step.
 * @author simon.schwantzer(at)im-c.de
 */
public class RemoveMediaAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(RemoveMediaAction.class.getName());
    
    private final Guide guide;
    private final PersistenceHandler persistenceHandler;
    
    private final ContentModel content;
    private File backupPackage;
    private ContentModel.Media media;
    
    public RemoveMediaAction(Guide guide, ContentModel content, PersistenceHandler persistenceHandler) {
        this.guide = guide;
        this.persistenceHandler = persistenceHandler;
        this.content = content;
    }

    @Override
    public void perform() {
        String guideId = guide.getId();
        media = content.mediaProperty().get();
        if (media == null) {
            notifyActionFailed(new ActionFailedException("Der aktelle Schritt enthält keine Medien."));
            return;
        }
        backupPackage = persistenceHandler.backupContentPackage(guideId, content.getId());
        
        File contentDir = persistenceHandler.getContentPackageDir(guideId, content.getId());
        File mediaFile = new File(contentDir, media.getPath());
        boolean isDeleted = mediaFile.delete();
        if (!isDeleted) {
            LOGGER.log(Level.INFO, "Failed to delete media file: {0}", mediaFile.getAbsolutePath());
            mediaFile.deleteOnExit();
        }
        
        content.mediaProperty().set(null);
        try {
            persistenceHandler.writeContentDescriptor(guide, content.getBean());
            notifyActionPerformed();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write content descriptor.", ex);
            notifyActionFailed(new ActionFailedException("Failed to write content descriptor.", ex));
        }
    }

    @Override
    public void undo() {
        File contentDir = persistenceHandler.getContentPackageDir(guide.getId(), content.getId());
                
        // Copy old media file from backup.
        File oldMediaFile = new File(backupPackage, media.getPath());
        try {
            Files.copy(oldMediaFile.toPath(), new File(contentDir, oldMediaFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to restore media file.", ex);
            notifyUndoFailed(new ActionFailedException("Die Datei konnte nicht kopiert werden: " + oldMediaFile.getAbsolutePath(), ex));
            return;
        }
        
        // Update content descriptor
        content.mediaProperty().set(media);
        try {
            persistenceHandler.writeContentDescriptor(guide, content.getBean());
            notifyActionUndone();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write content descriptor.", ex);
            notifyUndoFailed(new ActionFailedException("Failed to write content descriptor.", ex));
        }
    }

    @Override
    public String getDescription() {
        return "Medienobjekt löschen";
    }
    
}
