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
 * Model describing a VR method.
 * @author simon.schwantzer(at)im-c.de
 */
public class VRMethod {
            
    private final String id;
    private final Map<String, String> localizedNames;
    private final Map<String, Parameter<?>> parameters;
    
    private VRMethod(String id, Map<String, String> localizedNames, Map<String, Parameter<?>> parameters) {
        this.id = id;
        this.localizedNames = localizedNames;
        this.parameters = parameters;
    }
    
    public static VRMethod parseXML(Element methodElement) throws IllegalArgumentException {
        Namespace ns = methodElement.getNamespace();
        String id = methodElement.getAttributeValue("id");
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Missing tool identifier [id].");
        }
        
        Map<String, String> localizedNames = new HashMap<>();
        methodElement.getChildren("name", ns).forEach((nameElement) -> {
            String langId = nameElement.getAttributeValue("lang");
            if (langId == null || langId.isEmpty()) {
                throw new IllegalArgumentException("Missing language attribute [lang] in name element.");
            }
            localizedNames.put(langId, nameElement.getText());
        });
        if (localizedNames.isEmpty()) {
            throw new IllegalArgumentException("Missing [name] element for method.");
        }
        
        Map<String, Parameter<?>> toolParameters = new LinkedHashMap<>();
        Element paramsElement = methodElement.getChild("params", ns);
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
                    throw new IllegalArgumentException("Invalid or missing parameter [type].");
            }
            toolParameters.put(toolParameter.getId(), toolParameter);
        }
        return new VRMethod(id, localizedNames, toolParameters);
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
                .append(" (").append(getId()).append(")")
                .toString();
    }
}