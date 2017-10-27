package de.eome.guide.gm.model;

import de.eome.guide.gm.MainApp;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Model for a VR scene.
 * @author simon.schwantzer(at)im-c.de
 */
public class VRScene {
    private final Namespace ns;
    private final Element xml;
    private final List<VRNode> nodes;
    private final List<Parameter> params;
        
    public VRScene(Element xml) throws IllegalArgumentException {
        validate(xml);
        this.xml = xml;
        this.ns = xml.getNamespace();
        nodes = new ArrayList<>();
        params = new ArrayList<>();
        
        Element nodesElement = xml.getChild("nodes", ns);
        if (nodesElement != null) {
            nodesElement.getChildren("node", ns).forEach(nodeElement -> nodes.add(new VRNode(nodeElement)));
        }
        
        Element paramsElement = xml.getChild("params", ns);
        if (paramsElement != null) {
            paramsElement.getChildren("param", ns).forEach(paramElement -> {
                String paramType = paramElement.getAttributeValue("type", "<na>");
                Parameter<?> param;
                switch (paramType) {
                    case "string": 
                        param = StringParameter.parseXML(paramElement);
                        break;
                    case "int":
                        param = IntegerParameter.parseXML(paramElement);
                        break;
                    case "boolean":
                        param = BooleanParameter.parseXML(paramElement);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid or missing parameter type [type].");
                }
                params.add(param);
            });
        }
    }
    
    private static void validate(Element xml) throws IllegalArgumentException {
        if (xml.getAttribute("id") == null) {
            throw new IllegalArgumentException("Missing attribute [id].");
        }
    }
    
    /**
     * Returns the scene identifier.
     * @return Scene identifier.
     */
    public String getId() {
        return xml.getAttributeValue("id");
    }
    
    /**
     * Returns the title in the default language.
     * @return Localized title if set, otheriwse the ID of the scene.
     */
    public String getTitle() {
        return getTitle(MainApp.LANG.replace("_", "-"));
    }
    
    /**
     * Returns the title in a specific language.
     * @param langId ISO language idendeifier, e.g., de-DE
     * @return Localized title if set, otheriwse the ID of the scene.
     */
    public String getTitle(String langId) {
        String title = null;
        for (Element titleElement : xml.getChildren("title", ns)) {
            String lang = titleElement.getAttributeValue("lang");
            if (langId.equals(lang)) {
                title = titleElement.getText();
                break;
            }
        }
        return title != null ? title : getId();
    }
    
    /**
     * Returns a list of all direct nodes of the VR scene.
     * @return List of nodes. May be empty.
     */
    public List<VRNode> getNodes() {
        return nodes;
    }
    
    /**
     * Searches for a node in the scene graph.
     * @param nodeId Identifier for a node.
     * @return Node with the given identifier or <code>null</code> if no such node exists.
     */
    public VRNode findNode(String nodeId) {
        for (VRNode node : nodes) {
            if (node.getId().equals(nodeId)) {
                return node;
            } else {
                VRNode result = node.findSubNode(nodeId);
                if (result != null) return result;
            }
        }
        return null;
    }
    
    /**
     * Returns the list of parameters for the VR scene.
     * @return List of parameters. May be empty.
     */
    public List<Parameter> getParameters() {
        return params;
    }
    
    /**
     * Returns the XML representing the scene.
     * @return XML wrapped by this model.
     */
    public Element asXML() {
        return xml;
    }
}
