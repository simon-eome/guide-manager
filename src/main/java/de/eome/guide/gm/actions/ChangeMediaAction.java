package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.model.ContentModel;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.eome.guide.gm.model.GuideModel;
import java.util.UUID;

/**
 * Replace the media content in a step.
 * @author simon.schwantzer(at)im-c.de
 */
public class ChangeMediaAction extends BaseAction {
    private final static Logger LOGGER = Logger.getLogger(ChangeMediaAction.class.getName());
    
    private final GuideModel guide;
    private final PersistenceHandler persistenceHandler;
    private final File selectedFile;
    private final ContentModel content;
    private File backupPackage;
    private ContentModel.Media oldMedia;
    
    public ChangeMediaAction(GuideModel guide, ContentModel content, PersistenceHandler persistenceHandler, File selectedFile) {
        this.guide = guide;
        this.persistenceHandler = persistenceHandler;
        this.content = content;
        this.selectedFile = selectedFile;
    }

    @Override
    public void perform() {
        final File contentDir = persistenceHandler.getContentPackageDir(guide.getId(), content.getId());
            
        // Backup old content package.
        backupPackage = persistenceHandler.backupContentPackage(guide.getId(), content.getId());

        // Remove old media.
        oldMedia = content.mediaProperty().get();
        File oldMediaFile = null;
        if (oldMedia != null) {
            oldMediaFile = new File(contentDir, oldMedia.getPath());
            if (oldMediaFile.exists()) {
                boolean isOldDeleted = oldMediaFile.delete();
                if (!isOldDeleted) {
                    /*LOGGER.log(Level.WARNING, "Failed to delete file: " + oldMediaFile.getAbsolutePath());
                    notifyActionFailed(new ActionFailedException("Failed to remove old media file."));
                    return;*/
                    oldMediaFile.deleteOnExit();
                } 
            }   
        }
        // Copy selected file into content package directory.
        File targetFile;
        if (oldMediaFile != null && oldMediaFile.exists() && oldMediaFile.getName().equalsIgnoreCase(selectedFile.getName())) {
            targetFile = new File(contentDir, UUID.randomUUID().toString() + "-" + selectedFile.getName());
        } else {
            targetFile = new File(contentDir, selectedFile.getName());
        }
        try {
            Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to copy media file.", ex);
            notifyActionFailed(new ActionFailedException("Die Datei konnte nicht kopiert werden: " + selectedFile.getAbsolutePath(), ex));
            return;
        }

        // Update content descriptor
        String newMediaPath = targetFile.toURI().toString().substring(contentDir.toURI().toString().length());
        try {
            newMediaPath = URLDecoder.decode(newMediaPath, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, "Failed to decode media path.", ex);
        }
        String newMimeType;
        String targetFileName = targetFile.getName();
        Optional<String> suffix = Arrays.stream(targetFileName.split("[.]")).reduce((first, second) -> second);
        if (suffix.isPresent()) {
            switch (suffix.get().toUpperCase()) {
                case "PNG":
                    newMimeType = "image/png";
                    break;
                case "JPG":
                case "JPEG":
                    newMimeType = "image/jpeg";
                    break;
                case "MPG":
                case "MPEG":
                case "MP4":
                    newMimeType = "video/mp4";
                    break;
                default:
                    newMimeType = "unknown";
            }
        } else {
            newMimeType = "unknown";
        }
        content.mediaProperty().set(new ContentModel.Media(newMimeType, newMediaPath));
        try {
            persistenceHandler.writeContentDescriptor(guide.getBean(), content.getBean());
            notifyActionPerformed();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write content descriptor.", ex);
            notifyActionFailed(new ActionFailedException("Failed to write content descriptor.", ex));
        }
    }

    @Override
    public void undo() {
        File contentDir = persistenceHandler.getContentPackageDir(guide.getId(), content.getId());
        
        // Delete new media file.
        ContentModel.Media newMedia = content.mediaProperty().get();
        File newMediaFile = new File(contentDir, newMedia.getPath());
        boolean isDeleted = newMediaFile.delete();
        if (!isDeleted) {
            // Videos may not be closed.
            newMediaFile.deleteOnExit();
        }
        
        // Copy old media file from backup.
        if (oldMedia != null) {
            File oldMediaFile = new File(backupPackage, oldMedia.getPath());
            try {
                Files.copy(oldMediaFile.toPath(), new File(contentDir, oldMediaFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to restore media file.", ex);
                notifyUndoFailed(new ActionFailedException("Die Datei konnte nicht kopiert werden: " + oldMediaFile.getAbsolutePath(), ex));
                return;
            }
        }
        
        // Update content descriptor
        content.mediaProperty().set(oldMedia);
        try {
            persistenceHandler.writeContentDescriptor(guide.getBean(), content.getBean());
            notifyActionUndone();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write content descriptor.", ex);
            notifyUndoFailed(new ActionFailedException("Failed to write content descriptor.", ex));
        }
    }

    @Override
    public String getDescription() {
        return "Medienobjekt ändern";
    }
    
}
