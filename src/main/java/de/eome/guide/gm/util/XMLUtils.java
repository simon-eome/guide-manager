package de.eome.guide.gm.util;

import java.io.IOException;
import java.io.StringReader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XMLUtils {
    
    private final static SAXBuilder SAX_BUILDER = new SAXBuilder();
    
    public static Element getOrCreateElement(String elementName, Namespace ns, Element parent) {
        Element element = parent.getChild(elementName, ns);
        if (element == null) {
            element = new Element(elementName, ns);
            parent.addContent(element);
        }
        return element;
    }
    
    /**
     * Exports an XML element as string.
     * @param element Element to export as string.
     * @param format Compact or pretty print format.
     * @return String representing the XML element.
     */
    public static String exportAsString(Element element, Format format) {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(format);
        return outputter.outputString(element);
    }
    
    /**
     * Exports an XML document as string.
     * @param document XML document to export as string.
     * @param format Compact or pretty print format.
     * @return String representing the XML element.
     */
    public static String exportAsString(Document document, Format format) {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(format);
        return outputter.outputString(document);
    }
    
    /**
     * Imports a XML document.
     * @param xmlString String encoding an XML document.
     * @return Root element of an XML document.
     * @throws IllegalArgumentException The given string does not encode a valid XML document.
     */
    public static Element importFromString(String xmlString) throws IllegalArgumentException {
        Element rootElement;
        try {
            Document doc = SAX_BUILDER.build(new StringReader(xmlString));
            rootElement = doc.getRootElement();
        } catch (JDOMException | IOException e) {
            throw new IllegalArgumentException("Failed to parse XML string.", e);
        }
        return rootElement;
    }
    
    
    /**
     * Checks if an element contains the given element as descendant.
     * @param parent Element to check descendants for.
     * @param cname Name of the descendant.
     * @param ns Namespace of the descendant.
     * @return <code>true</code> if the element contains at least one descendant with the given name/namespace, otherwise <code>false</code>.
     */
    public static boolean containsElement(Element parent, String cname, Namespace ns) {
        if (parent.getChildren(cname, ns).size() > 0) {
            return true;
        } else {
            for (Element child : parent.getChildren(cname, ns)) {
                if (containsElement(child, cname, ns)) return true;
            }
            return false;
        }
    }
    
    /**
     * Returns a attribute value or throws an exception if the attribute is not available.
     * @param element Element to retrieve attribute value from.
     * @param attributeName Name of the attribute.
     * @return Attribute value.
     * @throws IllegalArgumentException The given attribute does not exist.
     */
    public static String getRequiredAttribute(Element element, String attributeName) throws IllegalArgumentException {
        String value = element.getAttributeValue(attributeName);
        if (value == null) {
            StringBuilder builder = new StringBuilder()
                .append("Missing required attribute \"")
                .append(attributeName)
                .append("\" of element \"")
                .append(element.getName())
                .append("\".");
            throw new IllegalArgumentException(builder.toString());
        }
        return value;
    }
    
    /**
     * Returns an child element or throws an exception the element is not available.
     * @param parent Parent element to retrieve child of.
     * @param cname Name of the element to retrieve.
     * @param ns Namespace of the element to retrieve.
     * @return Element.
     * @throws IllegalArgumentException No child element with the given name and namespace exists.
     */
    public static Element getRequiredElement(Element parent, String cname, Namespace ns) throws IllegalArgumentException {
        Element element = parent.getChild(cname, ns);
        if (element == null) {
            StringBuilder builder = new StringBuilder()
                .append("Missing required child element \"")
                .append(cname)
                .append("\" of element \"")
                .append(parent.getName())
                .append("\".");
            throw new IllegalArgumentException(builder.toString());
        }
        return element;
    }
}
