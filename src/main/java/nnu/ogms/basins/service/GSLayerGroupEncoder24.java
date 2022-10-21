package nnu.ogms.basins.service;

import it.geosolutions.geoserver.rest.encoder.GSLayerGroupEncoder;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Set;

public class GSLayerGroupEncoder24 extends MyGSLayerGroupEncoder {
    public static final String MODE_SINGLE = "SINGLE";
    public static final String MODE_NAMED = "NAMED";
    public static final String MODE_CONTAINER = "CONTAINER";
    public static final String MODE_EO = "EO";
    private static final Set<String> modes;
    static {
        modes = new HashSet<String>();
        modes.add(MODE_SINGLE);
        modes.add(MODE_NAMED);
        modes.add(MODE_CONTAINER);
        modes.add(MODE_EO);
    }

    private Element titleElem;
    private Element abstractElem;
    private Element modeElem;
    private Element rootLayerElem;
    private Element rootLayerStyleElem;

    public void setTitle(String title) {
        titleElem = elem("title", title);
    }

    public void setAbstract(String abstractTxt) {
        abstractElem = elem("abstractTxt", abstractTxt);
    }

    public void setMode(String mode) {
        if (!modes.contains(mode)) {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }

        modeElem = elem("mode", mode);
    }

    public void setRootLayer(String layer, String style) {
        rootLayerElem = elem("rootLayer", elem("name", layer));
        rootLayerStyleElem = elem("rootLayerStyle", elem("name", style));
    }

    @Override
    public void addLayer(String layer, String styleName) {
        initPublishables("publishables");

        publishablesElem.addContent(
                new Element("published").setAttribute("type", "layer").addContent(
                        elem("name", layer)));

        Element style = new Element("style");
        stylesElem.addContent(style);
        if (styleName != null) {
            style.addContent(elem("name", styleName));
        }
    }

    public void addLayerGroup(String group) {
        initPublishables("publishables");

        publishablesElem.addContent(
                new Element("published").setAttribute("type", "layerGroup").addContent(
                        elem("name", group)));

        stylesElem.addContent(new Element("style"));
    }

    @Override
    public String toString() {
        addToRoot(titleElem, abstractElem, modeElem, rootLayerElem, rootLayerStyleElem);
        return super.toString();
    }
}
