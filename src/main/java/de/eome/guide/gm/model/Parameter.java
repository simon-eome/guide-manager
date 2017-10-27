package de.eome.guide.gm.model;

import de.eome.guide.gm.MainApp;
import java.util.HashMap;
import java.util.Map;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Model for a tool parameter.
 * @author simon.schwantzer(at)im-c.de
 * @param <T> Type of the parameter.
 */
public abstract class Parameter<T> {
    public enum Type {
        BOOLEAN,
        INT,
        FLOAT,
        STRING;
        
        public static Type fromString(String value) {
            return Type.valueOf(value.toUpperCase());
        }
    }
    
    private final String id;
    private final T defaultValue;
    private final Type type;
    private Map<String, String> localizedNames;
    private boolean isRequired;
    
    protected Parameter(Type type, String id, Map<String, String> localizedNames, T defaultValue) {
        this.type = type;
        this.id = id;
        this.defaultValue = defaultValue;
        this.localizedNames = localizedNames;
    }
    
    protected Parameter(Type type, String id, Map<String, String> localizedNames) {
        this(type, id, localizedNames, null);
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return localizedNames.get(MainApp.LANG.replace("_", "-"));
    }
    
    public String getName(String languageId) {
        return localizedNames.get(languageId);
    }
    
    public Type getType() {
        return type;
    }
    
    public T getDefaultValue() {
        return defaultValue;
    }
    
    protected void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }
    
    public boolean isRequired() {
        return isRequired;
    }
    
    public abstract boolean validateValueString(String valueString);
    
    public Element toXML(Namespace ns) {
        Element element = new Element("param", ns);
        element.setAttribute("id", id);
        if (defaultValue != null) element.setAttribute("default", String.valueOf(defaultValue));
        if (isRequired) element.setAttribute("required", "true");
        localizedNames.forEach((langId, name) -> {
            Element nameElement = new Element("name", ns);
            nameElement.setAttribute("lang", langId);
            nameElement.setText(name);
            element.addContent(nameElement);
        });
        return element;
    }
    
    protected static Map<String, String> parseNameXML(Element element) throws IllegalArgumentException {
        Map<String, String> localizedNames = new HashMap<>();
        element.getChildren("name", element.getNamespace()).forEach((nameElement) -> {
            String langId = nameElement.getAttributeValue("lang");
            if (langId == null || langId.isEmpty()) {
                throw new IllegalArgumentException("Missing language attribute [lang] in name element.");
            }
            localizedNames.put(langId, nameElement.getText());
        });
        if (localizedNames.isEmpty()) {
            throw new IllegalArgumentException("Missing [name] element for tool.");
        }
        return localizedNames;
    }
}
