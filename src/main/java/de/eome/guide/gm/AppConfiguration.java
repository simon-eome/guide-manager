package de.eome.guide.gm;

import de.eome.guide.gm.model.Tool;
import de.eome.guide.gm.model.VRMethod;
import de.eome.guide.gm.util.XMLUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;

/**
 * Container for the app configuration.
 * @author simon.schwantzer(at)im-c.de
 */
public final class AppConfiguration {
    private static final Logger LOGGER = Logger.getLogger(AppConfiguration.class.getName());
    private static final String PREFNODE = "glassroom";
    
    private final Preferences prefs;
    private final Path configDir;
    private final List<VRMethod> vrMethods;
    private final List<Tool> tools;
    private final Set<String> cachedWarnings;
    private final Set<String> cachedHints;
    
    public AppConfiguration() {
        prefs = Preferences.userRoot().node(PREFNODE);
        configDir = Paths.get(System.getProperty("user.home"), ".glassroom");
        vrMethods = new ArrayList<>();
        tools = new ArrayList<>();
        cachedWarnings = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        cachedHints = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectory(configDir);
                copyToFile(
                        new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/config/default-methods.xml"))),
                        Files.newBufferedWriter(configDir.resolve("methods.xml")));
                copyToFile(
                        new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/config/default-tools.xml"))),
                        Files.newBufferedWriter(configDir.resolve("tools.xml")));
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to create configuration directory", ex);
            }
        }
        
        try {
            Path toolsFile = configDir.resolve("tools.xml");
            String toolsXmlString = PersistenceHandler.readFile(toolsFile.toFile());
            initializeTools(toolsXmlString);
        } catch (IOException | IllegalArgumentException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load default tools configuration.", ex);
        }
        
        try {
            Path methodsFile = configDir.resolve("methods.xml");
            String methodsXmlString = PersistenceHandler.readFile(methodsFile.toFile());
            initializeVRMethods(methodsXmlString);
        } catch (IOException | IllegalArgumentException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load default methods configuration.", ex);
        }
        
        loadCache();
    }
    
    public void importTools(Path toolsFile) throws IllegalArgumentException, IOException {
        String toolsXmlString = PersistenceHandler.readFile(toolsFile.toFile());
        initializeTools(toolsXmlString);
        Path to = configDir.resolve("tools.xml");
        Files.copy(toolsFile, to, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public void exportTools(Path toolsFile) {
        Element toolsElement = new Element("tools", "glassroom:model:tools");
        Document doc = new Document(toolsElement);
        tools.forEach(tool -> {
            toolsElement.addContent(tool.toXML());
        });
        String xmlString = XMLUtils.exportAsString(doc, Format.getPrettyFormat());
        try {
            PersistenceHandler.writeFile(toolsFile.toFile(), xmlString);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write tools descriptor.", ex);
        }
    }
    
    public void importVRMethods(Path methodsFile) throws IllegalArgumentException, IOException {
        String methodsXmlString = PersistenceHandler.readFile(methodsFile.toFile());
        initializeVRMethods(methodsXmlString);
        Path to = configDir.resolve("methods.xml");
        Files.copy(methodsFile, to, StandardCopyOption.REPLACE_EXISTING);
    }
    
    private void initializeTools(String toolsXmlString) throws IllegalArgumentException {
        List<Tool> newTools = new ArrayList<>();
        try {
            Element rootElement = XMLUtils.importFromString(toolsXmlString);
            rootElement.getChildren("tool", rootElement.getNamespace()).forEach((toolElement) -> {
                newTools.add(Tool.parseXML(toolElement));
            });
            tools.clear();
            tools.addAll(newTools);
            LOGGER.log(Level.INFO, "Imported {0} tools.", newTools.size());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse tools XML string.", e);
        }
    }
    
    private void initializeVRMethods(String methodsXmlString) throws IllegalArgumentException {
        List<VRMethod> newMethods = new ArrayList<>();
        try {
            Element rootElement = XMLUtils.importFromString(methodsXmlString);
            rootElement.getChildren("method", rootElement.getNamespace()).forEach((methodElement) -> {
                newMethods.add(VRMethod.parseXML(methodElement));
            });
            vrMethods.clear();
            vrMethods.addAll(newMethods);
            LOGGER.log(Level.INFO, "Imported {0} VR methods.", newMethods.size());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse VR methods XML string.", e);
        }
    }
    
    private static void copyToFile(BufferedReader in, BufferedWriter out) throws IOException {
        in.lines().forEach(line -> {
            try {
                out.append(line);
                out.newLine();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to copy default configuration file.", ex);
            }
        });
        out.flush();
        out.close();
    }
    
    /**
     * Returns the path of the repository.
     * @return Absolute directory path or <code>null</code> if not set.
     */
    public String getRepoDir() {
        return prefs.get("repoDir", null);
    }
    
    /**
     * Returns the path of the repository.
     * @param def Default to return if no configuration is available.
     * @return Absolute directory path.
     */
    public String getRepoDir(String def) {
        return prefs.get("repoDir", def);
    }
    
    /**
     * Sets the path of the repository.
     * @param absolutePath Absolute directory path. May be <code>null</code>.
     */
    public void setRepoDir(String absolutePath) {
        if (absolutePath != null) {
            prefs.put("repoDir", absolutePath);
        } else {
            prefs.remove("repoDir");
        }
    }
    
    /**
     * Returns the last directory a media file has been imported from.
     * @return Absolute directory path or <code>null</code> if not set.
     */
    public String getMediaDir() {
        return prefs.get("mediaDir", null);
    }
    
    /**
     * Sets the directory to be opened when a media file is imported.
     * @param absolutePath Absolute directory path. May be <code>null</code>.
     */
    public void setMediaDir(String absolutePath) {
        if (absolutePath != null) {
            prefs.put("mediaDir", absolutePath);
        } else {
            prefs.remove("mediaDir");
        }
    }
    
    /**
     * Returns the configured video editor.
     * @return Absolute path of an executable or <code>null</code> if not set.
     */
    public String getVideoEditor() {
        return prefs.get("videoEditor", null);
    }
    
    /**
     * Sets the video editor.
     * @param absolutePath Absolute path of an executable. May be <code>null</code>.
     */
    public void setVideoEditor(String absolutePath) {
        if (absolutePath != null) {
            prefs.put("videoEditor", absolutePath);
        } else {
            prefs.remove("videoEditor");
        }
    }
    
    /**
     * Returns the configured image editor.
     * @return Absolute path of an executable or <code>null</code> if not set.
     */
    public String getImageEditor() {
        return prefs.get("imageEditor", null);
    }
    
    /**
     * Sets the image editor.
     * @param absolutePath Absolute path of an executable. May be <code>null</code>.
     */
    public void setImageEditor(String absolutePath) {
        if (absolutePath != null) {
            prefs.put("imageEditor", absolutePath);
        } else {
            prefs.remove("imageEditor");
        }
    }
    
    /**
     * Returns the last directory opened as synchronization client.
     * @return Absolute directory path or <code>null</code> if not set.
     */
    public String getClientDir() {
        return prefs.get("clientDir", null);
    }
    
    /**
     * Sets the directory acting as synchronization client.
     * @param absolutePath Absolute directory path. May be <code>null</code>.
     */
    public void setClientDir(String absolutePath) {
        if (absolutePath != null) {
            prefs.put("clientDir", absolutePath);
        } else {
            prefs.remove("clientDir");
        }
    }
    
    public List<VRMethod> getVRMethods() {
        return vrMethods;
    }
    
    public List<Tool> getTools() {
        return tools;
    }
    
    /**
     * Returns a specific tool definition.
     * @param id Tool identifier.
     * @return Tool definition or <code>null</code> if no tool with the given id is registered.
     */
    public Tool getTool(String id) {
        return tools.stream()
                .filter(tool -> tool.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Adds a tool to the tool database.
     * @param tool Tool to add.
     */
    public void addTool(Tool tool) {
        tools.add(tool);
        exportTools(configDir.resolve("tools.xml"));
    }
    
    public Set<String> getCachedWarnings() {
        return cachedWarnings;
    }
    
    public void addWarningToCache(String warning) {
        cachedWarnings.add(warning);
        storeCache();
    }
    
    public Set<String> getCachedHints() {
        return cachedHints;
    }
    
    public void addHintToCache(String hint) {
        cachedHints.add(hint);
        storeCache();
    }
    
    private void loadCache() {
        File cacheFile = configDir.resolve("cache.xml").toFile();
        if (cacheFile.exists()) {
            try {
                String xmlString = PersistenceHandler.readFile(cacheFile);
                Element rootElement = XMLUtils.importFromString(xmlString);
                Namespace ns = rootElement.getNamespace();
                rootElement.getChild("warnings", ns).getChildren("warning", ns).forEach((warningElement) -> {
                    cachedWarnings.add(warningElement.getText());
                });
                rootElement.getChild("hints", ns).getChildren("hint", ns).forEach((hintElement) -> {
                    cachedHints.add(hintElement.getText());
                });
            } catch (IllegalArgumentException | IOException e) {
                LOGGER.log(Level.WARNING, "Failed to load cache.", e);
            }
        }
    }
    
    private void storeCache() {
        Namespace ns = Namespace.getNamespace("glassroom:model:cache");
        Element cacheElement = new Element("cache", ns);
        Element warningsElement = new Element("warnings", ns);
        for (String warning : cachedWarnings) {
            warningsElement.addContent(new Element("warning", ns).setText(warning));
        }
        cacheElement.addContent(warningsElement);
        Element hintsElement = new Element("hints", ns);
        for (String hint : cachedHints) {
            hintsElement.addContent(new Element("hint", ns).setText(hint));
        }
        cacheElement.addContent(hintsElement);
        Document doc = new Document(cacheElement);
        String xmlString = XMLUtils.exportAsString(doc, Format.getPrettyFormat());
        Path cacheFile = configDir.resolve("cache.xml");
        try {
            PersistenceHandler.writeFile(cacheFile.toFile(), xmlString);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write cache.", ex);
        }
    }
}