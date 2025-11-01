package org.gephi.viz.engine.util.text;

import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphView;

public class TextLabelBuilder {

    public static String buildText(Element element, GraphView view, Column[] columns) {
        if (columns.length == 0) {
            return "";
        } else if (columns.length == 1) {
            return buildText(element, view, columns[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Column c : columns) {
                if (i++ > 0) {
                    sb.append(" - ");
                }
                sb.append(buildText(element, view, c));
            }
            return sb.toString();
        }
    }

    public static String buildText(Element element, GraphView view, Column column) {
        Object val = element.getAttribute(column, view);
        if (val == null) {
            return "";
        }
        if (column.isArray()) {
            return AttributeUtils.printArray(val);
        } else {
            return val.toString();
        }
    }
}
