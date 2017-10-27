package de.eome.guide.gm.model;

import de.eome.guide.gm.MainApp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;
import org.jdom2.Namespace;
/**
 * Model for a tool used to perform a task.
 * @author simon.schwantzer(at)im-c.de
 */
public class Tool {
    public static class Builder {
        private final String id;
        private final Map<String, String> localizedNames;
        private final Map<String, Parameter<?>> parameters;
        
        public Builder(String id) {
            this.id = id;
            this.localizedNames = new HashMap<>();
            this.parameters = new LinkedHashMap<>();
        }
        
        public Builder addName(String languageId, String name) {
            localizedNames.put(languageId, name);
            return this;
        }
        
        public Builder addParameter(Parameter<?> param) {
            this.parameters.put(param.getId(), param);
            return this;
        }
        
        public Tool build() {
            return new Tool(id, localizedNames, parameters);
        }
    }
            
    private final String id;
    private final Map<String, String> localizedNames;
    private final Map<String, Parameter<?>> parameters;
    
    private Tool(String id, Map<String, String> localizedNames, Map<String, Parameter<?>> parameters) {
        this.id = id;
        this.localizedNames = localizedNames;
        this.parameters = parameters;
        
    }
    
    public static Tool parseXML(Element toolElement) throws IllegalArgumentException {
        Namespace ns = toolElement.getNamespace();
        String id = toolElement.getAttributeValue("id");
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Missing tool identifier [id].");
        }
        
        Map<String, String> localizedNames = new HashMap<>();
        toolElement.getChildren("name", ns).forEach((nameElement) -> {
            String langId = nameElement.getAttributeValue("lang");
            if (langId == null || langId.isEmpty()) {
                throw new IllegalArgumentException("Missing language attribute [lang] in name element.");
            }
            localizedNames.put(langId, nameElement.getText());
        });
        if (localizedNames.isEmpty()) {
            throw new IllegalArgumentException("Missing [name] element for tool.");
        }
        
        Map<String, Parameter<?>> toolParameters = new LinkedHashMap<>();
        Element paramsElement = toolElement.getChild("params", ns);
        if (paramsElement != null) for (Element paramElement : paramsElement.getChildren("param", ns)) {
            String paramType = paramElement.getAttributeValue("type", "<na>");
            Parameter<?> toolParameter;
            switch (paramType) {
                case "string": 
                    toolParameter = StringParameter.parseXML(paramElement);
                    break;
                case "int":
                    toolParameter = IntegerParameter.parseXML(paramElement);
                    break;
                case "boolean":
                    toolParameter = BooleanParameter.parseXML(paramElement);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid or missing parameter type [type].");
            }
            toolParameters.put(toolParameter.getId(), toolParameter);
        }
        return new Tool(id, localizedNames, toolParameters);
    }
    
    public Element toXML() {
        Namespace ns = Namespace.getNamespace("glassroom:model:tools");
        Element toolElement = new Element("tool", ns);
        toolElement.setAttribute("id", id);
        localizedNames.entrySet().forEach(entry -> {
            Element nameElement = new Element("name", ns);
            nameElement.setAttribute("lang", entry.getKey());
            nameElement.setText(entry.getValue());
            toolElement.addContent(nameElement);
        });
        if (!parameters.isEmpty()) {
            Element paramsElement = new Element("params", ns);
            parameters.values().forEach(param -> paramsElement.addContent(param.toXML(ns)));
            toolElement.addContent(paramsElement);
        }
        return toolElement;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        String localizedName = localizedNames.get(MainApp.LANG.replace("_", "-"));
        return localizedName != null ? localizedName : getId();
    }
    
    public String getName(String langId) {
        String localizedName = localizedNames.get(langId);
        return localizedName != null ? localizedName : getId();
    }
    
    public Parameter<?> getParameter(String id) {
        return parameters.get(id);
    }
    
    public List<Parameter<?>> getParameters() {
        return new ArrayList<>(parameters.values());
    }
    
    @Override
    public String toString() {
        return new StringBuilder(200)
                .append(getName())
                .append(" (").append(id).append(")")
                .toString();
    }
}