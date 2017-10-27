package de.eome.guide.gm.model;

import de.eome.guide.gm.MainApp;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Model for a node in a VR scene.
 * @author simon.schwantzer(at)im-c.de
 */
public class VRNode {
    private final Namespace ns;
    private final Element xml;
    private final List<VRNode> subNodes;
    private final List<Parameter> params;
    
    /**
     * Creates a new node wrapping the given XML element.
     * @param xml XML element representing a VR node.
     * @throws IllegalArgumentException The given XML element is no valid representation of a VR node.
     */
    public VRNode(Element xml) throws IllegalArgumentException {
        validate(xml);
        this.xml = xml;
        this.ns = xml.getNamespace();
        this.subNodes = new ArrayList<>();
        params = new ArrayList<>();
        
        Element nodesElement = xml.getChild("nodes", ns);
        if (nodesElement != null) {
            nodesElement.getChildren("node", ns).forEach(nodeElement -> subNodes.add(new VRNode(nodeElement))); 
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
            throw new IllegalArgumentException("Missing attribte [id].");
        }
    }
    
    /**
     * Returns the name in the default language.
     * @return Localized name if set, otheriwse the ID of the node.
     */
    public String getName() {
        return getName(MainApp.LANG.replace("_", "-"));
    }
    
    /**
     * Returns the name in a specific language.
     * @param langId ISO language idendeifier, e.g., de-DE
     * @return Localized name if set, otheriwse the ID of the node.
     */
    public String getName(String langId) {
        String name = null;
        for (Element nameElement : xml.getChildren("name", ns)) {
            String lang = nameElement.getAttributeValue("lang");
            if (langId.equals(lang)) {
                name = nameElement.getText();
                break;
            }
        }
        return name != null ? name : getId();
    }
    
    /**
     * Retrurn the node identifier.
     * @return Node identifier.
     */
    public String getId() {
        return xml.getAttributeValue("id");
    }
    
    /**
     * Returns all subnodes of this node.
     * @return List of nodes. May be empty.
     */
    public List<VRNode> getSubNodes() {
        return subNodes;
    }
    
    /**
     * Searches recursivly for a sub node.
     * @param nodeId ID of the node to find.
     * @return A node with the given ID or <code>null</code> if no such node exists.
     */
    public VRNode findSubNode(String nodeId) {
        for (VRNode subNode : subNodes) {
            if (subNode.getId().equals(nodeId)) {
                return subNode;
            } else {
                VRNode result = subNode.findSubNode(nodeId);
                if (result != null) return result;
            }
        }
        return null;
    }
    
    /**
     * Returns the list of parameters for the VR node.
     * @return List of parameters. May be empty.
     */
    public List<Parameter> getParameters() {
        return params;
    }
    
    /**
     * returns the XML element representing the node.
     * @return XML element.
     */
    public Element asXML() {
        return xml;
    }
    
    @Override
    public String toString() {
        return new StringBuilder(200)
            .append(getName())
            .append(" (").append(getId()).append(")")
            .toString();
    }
}
