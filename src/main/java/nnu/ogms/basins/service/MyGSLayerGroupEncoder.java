package nnu.ogms.basins.service;

import it.geosolutions.geoserver.rest.encoder.utils.PropertyXMLEncoder;
import org.jdom.Element;

public class MyGSLayerGroupEncoder  extends PropertyXMLEncoder {
    protected Element nameElem;
    protected Element workspaceElem;
    protected Element boundsElem;
    protected Element publishablesElem;
    protected Element stylesElem;


    public MyGSLayerGroupEncoder() {
        super("layerGroup");
    }


    public void setWorkspace(String workspace) {
        workspaceElem = elem("workspace", elem("name", workspace));
    }

    public void setName(String name) {
        nameElem = elem("name", name);
    }

    public void addLayer(String layer) {
        addLayer(layer, null);
    }

    public void addLayer(String layer, String styleName) {
        initPublishables("layers");

        publishablesElem.addContent(elem("layer", elem("name", layer)));

        Element style = new Element("style");
        stylesElem.addContent(style);
        if (styleName != null) {
            style.addContent(elem("name", styleName));
        }
    }

    public void setBounds(String crs, double minx, double maxx, double miny, double maxy) {
        boundsElem = elem("bounds",
                elem("minx", Double.toString(minx)),
                elem("maxx", Double.toString(maxx)),
                elem("miny", Double.toString(miny)),
                elem("maxy", Double.toString(maxy)),
                elem("crs", "class", "projected").setText(crs));
    }

    protected void initPublishables(String publishablesTag) {
        if (publishablesElem == null) {
            publishablesElem = new Element(publishablesTag);
        }

        if (stylesElem == null) {
            stylesElem = new Element("styles");
        }
    }

    protected void addToRoot(Element ... elements) {
        for (Element e : elements) {
            if (e != null) {
                getRoot().addContent((Element)e.clone());
            }
        }
    }

    protected Element elem(String tag, String attributeName, String attributeValue) {
        return new Element(tag).setAttribute(attributeName, attributeValue);
    }

    protected Element elem(String tag, String text) {
        return new Element(tag).setText(text);
    }

    protected Element elem(String tag, Element ... children) {
        Element parent = new Element(tag);
        for (Element child : children) {
            parent.addContent(child);
        }
        return parent;
    }

    @Override
    public String toString() {
        addToRoot(nameElem, workspaceElem, boundsElem, publishablesElem, stylesElem);
        return super.toString();
    }
}
