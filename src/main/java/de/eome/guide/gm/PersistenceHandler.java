package de.eome.guide.gm;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import de.glassroom.gpe.Guide;
import de.glassroom.gpe.GuideManager;
import de.glassroom.gpe.content.ContentDescriptor;
import de.glassroom.gpe.utils.ContentSerializer;
import de.glassroom.gpe.utils.GuideSerializer;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler to store guides and media.
 */
public class PersistenceHandler {
    private static final Logger LOGGER = Logger.getLogger(PersistenceHandler.class.getName());
    
    private File guidesDir;
    private File tmpDir;
    
    public PersistenceHandler() {
        try {
            tmpDir = Files.createTempDirectory("glassroom").toFile();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to create temp folder: " + tmpDir.getAbsolutePath(), ex);
        }
    }
    
    /**
     * Initializes a directory as repository of guides.
     * @return Manager for the imported guides.
     * @param guidesDir Directory to use as repository.
     */
    public GuideManager initializeRepository(File guidesDir) {
        this.guidesDir = guidesDir;
        
        GuideManager gm = new GuideManager();
        for (File file : guidesDir.listFiles()) {
            if (!file.isDirectory()) continue;
            File manifest = new File(file, "guide.bpmn");
            if (!manifest.exists()) continue;
            try {
                String bpmnString = readFile(manifest);
                Guide guide = GuideSerializer.readFromBPMN(bpmnString);
                guide.getMetadata().setLastUpdate(new Date(manifest.lastModified()));
                gm.addGuide(guide);
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Failed to import guide " + file.getName() + ": ", e);
            }
        }
        
        return gm;
    }
    
    public static String readFile(File file) throws IOException {
        Scanner scanner = new Scanner(file, "UTF-8").useDelimiter("\r\n");
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNext()) {
            builder.append(scanner.next());
        }
        scanner.close();
        return builder.toString();
    }

    /**
     * Writes a guide manifest to the external storage.
     * @param guide Guide to persist.
     * @throws IOException Writing to the external storage failed.
     */
    public void writeGuide(Guide guide) throws IOException {
        File guideDir = new File(guidesDir.getAbsolutePath() + "/" + guide.getId());
        if (!guideDir.exists()) guideDir.mkdir();

        File contentDir = new File(guideDir.getAbsolutePath() + "/content");
        if (!contentDir.exists()) contentDir.mkdir();

        File guideDescriptorFile = new File(guideDir, "guide.bpmn");
        String serializedGuide = GuideSerializer.writeAsBPMN(guide, false);
        writeFile(guideDescriptorFile, serializedGuide);
    }
    
    public GuideManager importGuide(File dirToImport) throws IllegalArgumentException, IOException {
        if (!dirToImport.exists() || !dirToImport.isDirectory()) {
            throw new IllegalArgumentException("The given file entry does not exist or is no directory.");
        }
        String guideId = dirToImport.getName();
        File localDir = new File(guidesDir.getAbsolutePath() + "/" + guideId);
        if (localDir.exists()) {
            File backup = backupGuide(guideId);
            deleteDir(localDir);
            if (localDir.exists()) {
                restoreGuide(guideId, backup);
                throw new IllegalStateException("Failed to delete guide directory: " + guideId);
            }
        }
        
        copyDirectory(dirToImport, localDir);
        return initializeRepository(guidesDir);
    }

    /**
     * Persists a content descriptor.
     * @param guideId ID of the guide the content is related to.
     * @param contentDescriptor Content descriptor to persist.
     * @throws IOException Failed to generate
     */
    public void writeContentDescriptor(Guide guide, ContentDescriptor contentDescriptor) throws IOException {
        File contentPackageDir = new File(guidesDir.getAbsolutePath() + "/" + guide.getId() + "/content/" + contentDescriptor.getId());
        if (!contentPackageDir.exists()) contentPackageDir.mkdirs();

        String serializedContentDescriptor = ContentSerializer.writeAsXML(contentDescriptor, false);

        File contentDescriptorFile = new File(contentPackageDir, "content.xml");
        writeFile(contentDescriptorFile, serializedContentDescriptor);
        
        // Update guide
        guide.update();
        writeGuide(guide);
    }
    
    public static void writeFile(File file, String content) throws IOException {
        Writer writer;
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        writer.write(content);
        writer.flush();
        writer.close();
    }
    
    public ContentDescriptor readContentDescriptor(String guideId, String packageId) throws IOException {
        File contentPackageDir = new File(guidesDir.getAbsolutePath() + "/" + guideId + "/content/" + packageId);
        if (!contentPackageDir.exists()) {
            throw new IOException("Content package " + packageId + " does not exist.");
        }
        
        File contentDescriptorFile = new File(contentPackageDir, "content.xml");
        if (!contentDescriptorFile.exists()) {
            throw new IOException("Content descriptor for package " + packageId + " is missing.");
        }
        
        String descriptorString = readFile(contentDescriptorFile);
        ContentDescriptor descriptor = ContentSerializer.readFromXML(descriptorString);
        return descriptor;
    }
    
    public boolean deleteContentPackage(String guideId, String packageId) throws IOException {
        File contentPackageDir = new File(guidesDir.getAbsolutePath() + "/" + guideId + "/content/" + packageId);
        return contentPackageDir.delete();
    }
    
    public File getContentPackageDir(String guideId, String packageId) {
       return new File(guidesDir.getAbsolutePath() + "/" + guideId + "/content/" + packageId); 
    }
    
    public File getGuideDir(String guideId) {
        return new File(guidesDir.getAbsolutePath() + "/" + guideId);
    }

    /**
     * Deletes a guide with all of its content.
     * @param guideId Identifier of the guide to delete.
     */
    public void deleteGuide(String guideId) {
        File guideDir = new File(guidesDir.getAbsolutePath() + "/" + guideId);
        if (guideDir.exists()) {
            deleteDir(guideDir);
        }
    }
    
    public File backupGuide(String guideId) {
        String dirName = guideId + "-" + new Date().getTime();
        File dest = new File(tmpDir, dirName);
        File src = new File(guidesDir.getAbsolutePath(), guideId);
        try {
            copyDirectory(src, dest);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to perform backup.", ex);
        }
        LOGGER.log(Level.FINE, "Target for guide backup: " + dest.getAbsolutePath());
        return dest;
    }
    
    public void restoreGuide(String guideId, File backupDirectory) throws IllegalArgumentException {
        if (!backupDirectory.exists() || !backupDirectory.isDirectory()) {
            throw new IllegalArgumentException("The given backup file does not exist or is no directory.");
        }
        File dest = new File(guidesDir.getAbsolutePath(), guideId);
        deleteDir(dest);
        backupDirectory.renameTo(dest);
    }
    
    public File backupContentPackage(String guideId, String contentId) {
        String dirName = contentId + "-" + new Date().getTime();
        File dest = new File(tmpDir, dirName);
        File src = getContentPackageDir(guideId, contentId);
        try {
            copyDirectory(src, dest);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to perform backup.", ex);
        }
        LOGGER.log(Level.FINE, "Target for content backup: " + dest.getAbsolutePath());
        return dest;
    }
    
    /**
     * Restores a backup by deleting the existing content package and move the
     * backup directory at its place.
     * @param guideId Guide the content package belongs to.
     * @param contentId Identifier of the content package.
     * @param backupDirectory Content pacjage to restore. The directory will be moved.
     * @throws IllegalArgumentException <code>backupDirectory</code> does not exist or is no directory.
     */
    public void restoreContentPackage(String guideId, String contentId, File backupDirectory) throws IllegalArgumentException {
        if (!backupDirectory.exists() || !backupDirectory.isDirectory()) {
            throw new IllegalArgumentException("The given backup file does not exist or is no directory.");
        }
        File dest = getContentPackageDir(guideId, contentId);
        deleteDir(dest);
        backupDirectory.renameTo(dest);
    }

    private static void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for(File f : files) {
                if(f.isDirectory()) {
                    deleteDir(f);
                } else {
                    boolean success = f.delete();
                    if (!success) {
                        LOGGER.warning("Failed to delete: " + f.getAbsolutePath());
                    }
                }
            }
        }
        dir.delete();
    }
    
    private static void copyDirectory(File src, File dest) throws IOException {
        // checks
        if (src == null || dest == null) return;
        if (!src.isDirectory()) return;
        if (dest.exists()) {
            if (!dest.isDirectory()) return;
        } else {
            dest.mkdir();
        }

        if (src.listFiles() == null || src.listFiles().length == 0) {
            return;
        }

        for (File file : src.listFiles()) {
            File fileDest = new File(dest, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, fileDest);
            } else {
                if (fileDest.exists()) continue;
                Files.copy(file.toPath(), fileDest.toPath());
            }
        }
    }
    
    public void moveContentPackage(String contentId, String sourceGuideId, String targetGuideId) throws IOException {
        File src = getContentPackageDir(sourceGuideId, contentId);
        File dest = getContentPackageDir(targetGuideId, contentId);
        src.renameTo(dest);
    }
}
