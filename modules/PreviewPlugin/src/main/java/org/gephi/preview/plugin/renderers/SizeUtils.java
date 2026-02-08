package org.gephi.preview.plugin.renderers;

import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.items.NodeItem;

public class SizeUtils {

    public static float getNodeSize(Item item, PreviewProperties properties) {
        float scale = properties.getFloatValue(PreviewProperty.NODE_SCALE_FACTOR);
        Float size = item.getData(NodeItem.SIZE);
        return size * scale;
    }
}
