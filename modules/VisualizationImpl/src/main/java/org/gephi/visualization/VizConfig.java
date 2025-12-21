/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.visualization;

import java.awt.Color;
import java.awt.Font;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.ui.utils.FontUtils;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.openide.util.NbPreferences;

/**
 * @author Mathieu Bastian
 */
public class VizConfig {

    private VizConfig() {
        // Only static methods and fields
    }

    //Const Default Config
    public static final String BACKGROUND_COLOR = "VizConfig.defaultBackgroundColor";
    public static final String BACKGROUND_COLOR_DARK = "VizConfig.defaultDarkBackgroundColor";
    public static final String NODE_LABELS = "VizConfig.defaultShowNodeLabels";
    public static final String EDGE_LABELS = "VizConfig.defaultShowEdgeLabels";
    public static final String SHOW_EDGES = "VizConfig.defaultShowEdges";
    public static final String HIGHLIGHT = "VizConfig.defaultLightenNonSelectedAuto";
    public static final String HIGHLIGHT_FACTOR = "VizConfig.defaultLightenNonSelectedFactor";
    public static final String NEIGHBOUR_SELECT = "VizConfig.defaultAutoSelectNeighbor";
    public static final String HIDE_NONSELECTED_EDGES = "VizConfig.defaultHideNonSelectedEdges";
    public static final String EDGE_COLOR_MODE = "VizConfig.defaultEdgeColorMode";
    public static final String NODE_LABEL_FONT = "VizConfig.defaultNodeLabelFont";
    public static final String EDGE_LABEL_FONT = "VizConfig.defaultEdgeLabelFont";
    public static final String SELECTEDEDGE_HAS_COLOR = "VizConfig.defaultEdgeSelectionColor";
    public static final String SELECTEDEDGE_IN_COLOR = "VizConfig.defaultEdgeInSelectedColor";
    public static final String SELECTEDEDGE_OUT_COLOR = "VizConfig.defaultEdgeOutSelectedColor";
    public static final String SELECTEDEDGE_BOTH_COLOR = "VizConfig.defaultEdgeBothSelectedColor";
    public static final String EDGE_SCALE = "VizConfig.defaultEdgeScale";
    public static final String NODE_SCALE = "VizConfig.defaultNodeScale";
    public static final String EDGE_WEIGHTED = "VizConfig.defaultUseEdgeWeight";
    public static final String EDGE_RESCALE_WEIGHT = "VizConfig.defaultRescaleEdgeWeight";
    public static final String NODE_LABEL_SIZE_MODE = "VizConfig.defaultNodeLabelSizeMode";
    public static final String NODE_LABEL_COLOR_MODE = "VizConfig.defaultNodeLabelColorMode";
    public static final String NODE_LABEL_SCALE = "VizConfig.defaultNodeLabelScale";
    public static final String EDGE_LABEL_SCALE = "VizConfig.defaultEdgeLabelScale";
    public static final String EDGE_LABEL_SIZE_MODE = "VizConfig.defaultEdgeLabelSizeMode";
    public static final String EDGE_LABEL_COLOR_MODE = "VizConfig.defaultEdgeLabelColorMode";
    public static final String ZOOM = "VizConfig.defaultZoom";
    public static final String HIDE_NONSELECTED_NODE_LABELS = "VizConfig.hideNonSelectedNodeLabels";
    public static final String HIDE_NONSELECTED_EDGE_LABELS = "VizConfig.hideNonSelectedEdgeLabels";
    public static final String FIT_NODE_LABELS_TO_NODE_SIZE = "VizConfig.fitNodeLabelsToNodeSize";
    public static final String AVOID_NODE_LABEL_OVERLAP = "VizConfig.avoidNodeLabelOverlap";
    public static final String MOUSE_SELECTION_DIAMETER = "VizConfig.mouseSelectionDiameter";
    public static final String SCREENSHOT_SCALE_FACTOR = "VizConfig.screenshotScaleFactor";
    public static final String SCREENSHOT_TRANSPARENT_BACKGROUND = "VizConfig.screenshotTransparentBackground";
    public static final String SCREENSHOT_AUTO_SAVE = "VizConfig.screenshotAutoSave";
    //Const Prefs
    public static final String ANTIALIASING = "VizConfig.antialiasing";
    public static final String SHOW_FPS = "VizConfig.showFPS";
    public static final String CONTEXT_MENU = "VizConfig.contextMenu";
    //Default values
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public static final Color DEFAULT_DARK_BACKGROUND_COLOR = new Color(52, 55, 57, 255);
    public static final boolean DEFAULT_NODE_LABELS = false;
    public static final boolean DEFAULT_EDGE_LABELS = false;
    public static final boolean DEFAULT_SHOW_EDGES = true;
    public static final boolean DEFAULT_HIGHLIGHT = true;
    public static final float DEFAULT_HIGHLIGHT_FACTOR = 0.9f;
    public static final boolean DEFAULT_NEIGHBOUR_SELECT = true;
    public static final EdgeColorMode DEFAULT_EDGE_COLOR_MODE = EdgeColorMode.SOURCE;
    public static final boolean DEFAULT_HIDE_NONSELECTED_EDGES = false;
    public static final Font DEFAULT_NODE_LABEL_FONT = new Font("Arial", Font.BOLD, 32);
    public static final Font DEFAULT_EDGE_LABEL_FONT = new Font("Arial", Font.BOLD, 32);
    public static final float DEFAULT_ZOOM = 0.3f;
    public static final boolean DEFAULT_HIDE_NONSELECTED_NODE_LABELS = false;
    public static final boolean DEFAULT_HIDE_NONSELECTED_EDGE_LABELS = false;
    public static final boolean DEFAULT_FIT_NODE_LABELS_TO_NODE_SIZE = false;
    public static final boolean DEFAULT_AVOID_NODE_LABEL_OVERLAP = true;
    public static final boolean DEFAULT_SELECTEDEDGE_HAS_COLOR = false;
    public static final Color DEFAULT_SELECTEDEDGE_IN_COLOR = new Color(32, 95, 154, 255);
    public static final Color DEFAULT_SELECTEDEDGE_OUT_COLOR = new Color(196, 66, 79, 255);
    public static final Color DEFAULT_SELECTEDEDGE_BOTH_COLOR = new Color(248, 215, 83, 255);
    public static final int DEFAULT_ANTIALIASING = 4;
    public static final boolean DEFAULT_SHOW_FPS = false;
    public static final boolean DEFAULT_CONTEXT_MENU = true;
    public static final int DEFAULT_MOUSE_SELECTION_DIAMETER = 1;
    public static final float DEFAULT_EDGE_SCALE = 2f;
    public static final float DEFAULT_NODE_SCALE = 1f;
    public static final float DEFAULT_NODE_LABEL_SCALE = 0.5f;
    public static final float DEFAULT_EDGE_LABEL_SCALE = 0.5f;
    public static final String DEFAULT_NODE_LABEL_SIZE_MODE = LabelSizeMode.ZOOM.name();
    public static final String DEFAULT_NODE_LABEL_COLOR_MODE = LabelColorMode.SELF.name();
    public static final String DEFAULT_EDGE_LABEL_SIZE_MODE = LabelSizeMode.ZOOM.name();
    public static final String DEFAULT_EDGE_LABEL_COLOR_MODE = LabelColorMode.SELF.name();
    public static final boolean DEFAULT_EDGE_WEIGHTED = true;
    public static final boolean DEFAULT_EDGE_RESCALE_WEIGHTED = true;
    public static final int DEFAULT_SCREENSHOT_SCALE_FACTOR = 1;
    public static final boolean DEFAULT_SCREENSHOT_TRANSPARENT_BACKGROUND = false;
    public static final boolean DEFAULT_SCREENSHOT_AUTO_SAVE = false;

    //Default config - loaded in the VizModel
    protected static Color defaultBackgroundColor = ColorUtils.decode(
        NbPreferences.forModule(VizConfig.class).get(BACKGROUND_COLOR, ColorUtils.encode(DEFAULT_BACKGROUND_COLOR)));
    protected static Color defaultDarkBackgroundColor = ColorUtils.decode(
        NbPreferences.forModule(VizConfig.class)
            .get(BACKGROUND_COLOR_DARK, ColorUtils.encode(DEFAULT_DARK_BACKGROUND_COLOR)));
    protected static boolean defaultShowNodeLabels =
        NbPreferences.forModule(VizConfig.class).getBoolean(NODE_LABELS, DEFAULT_NODE_LABELS);
    protected static boolean defaultShowEdgeLabels =
        NbPreferences.forModule(VizConfig.class).getBoolean(EDGE_LABELS, DEFAULT_EDGE_LABELS);
    protected static boolean defaultShowEdges =
        NbPreferences.forModule(VizConfig.class).getBoolean(SHOW_EDGES, DEFAULT_SHOW_EDGES);
    protected static EdgeColorMode defaultEdgeColorMode =
        EdgeColorMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(EDGE_COLOR_MODE, DEFAULT_EDGE_COLOR_MODE.name()));
    protected static boolean defaultLightenNonSelectedAuto =
        NbPreferences.forModule(VizConfig.class).getBoolean(HIGHLIGHT, DEFAULT_HIGHLIGHT);
    protected static float defaultLightenNonSelectedFactor =
        NbPreferences.forModule(VizConfig.class).getFloat(HIGHLIGHT_FACTOR, DEFAULT_HIGHLIGHT_FACTOR);
    protected static boolean defaultAutoSelectNeighbor =
        NbPreferences.forModule(VizConfig.class).getBoolean(NEIGHBOUR_SELECT, DEFAULT_NEIGHBOUR_SELECT);
    protected static boolean defaultHideNonSelectedEdges =
        NbPreferences.forModule(VizConfig.class).getBoolean(HIDE_NONSELECTED_EDGES, DEFAULT_HIDE_NONSELECTED_EDGES);
    protected static Font defaultNodeLabelFont = Font.decode(
        NbPreferences.forModule(VizConfig.class).get(NODE_LABEL_FONT, FontUtils.encode(DEFAULT_NODE_LABEL_FONT)));
    protected static Font defaultEdgeLabelFont = Font.decode(
        NbPreferences.forModule(VizConfig.class).get(EDGE_LABEL_FONT, FontUtils.encode(DEFAULT_EDGE_LABEL_FONT)));
    protected static boolean defaultHideNonSelectedNodeLabels =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(HIDE_NONSELECTED_NODE_LABELS, DEFAULT_HIDE_NONSELECTED_NODE_LABELS);
    protected static boolean defaultHideNonSelectedEdgeLabels =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(HIDE_NONSELECTED_EDGE_LABELS, DEFAULT_HIDE_NONSELECTED_EDGE_LABELS);
    protected static boolean defaultFitNodeLabelsToNodeSize =
        NbPreferences.forModule(VizConfig.class).getBoolean(FIT_NODE_LABELS_TO_NODE_SIZE,
            DEFAULT_FIT_NODE_LABELS_TO_NODE_SIZE);
    protected static boolean defaultAvoidNodeLabelOverlap =
        NbPreferences.forModule(VizConfig.class).getBoolean(AVOID_NODE_LABEL_OVERLAP,
            DEFAULT_AVOID_NODE_LABEL_OVERLAP);
    protected static boolean defaultEdgeSelectionColor =
        NbPreferences.forModule(VizConfig.class).getBoolean(SELECTEDEDGE_HAS_COLOR, DEFAULT_SELECTEDEDGE_HAS_COLOR);
    protected static Color defaultEdgeInSelectedColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class)
        .get(SELECTEDEDGE_IN_COLOR, ColorUtils.encode(DEFAULT_SELECTEDEDGE_IN_COLOR)));
    protected static Color defaultEdgeOutSelectedColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class)
        .get(SELECTEDEDGE_OUT_COLOR, ColorUtils.encode(DEFAULT_SELECTEDEDGE_OUT_COLOR)));
    protected static Color defaultEdgeBothSelectedColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class)
        .get(SELECTEDEDGE_BOTH_COLOR, ColorUtils.encode(DEFAULT_SELECTEDEDGE_BOTH_COLOR)));
    protected static float defaultEdgeScale =
        NbPreferences.forModule(VizConfig.class).getFloat(EDGE_SCALE, DEFAULT_EDGE_SCALE);
    protected static float defaultNodeScale =
        NbPreferences.forModule(VizConfig.class).getFloat(NODE_SCALE, DEFAULT_NODE_SCALE);
    protected static LabelSizeMode defaultNodeLabelSizeMode =
        LabelSizeMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(NODE_LABEL_SIZE_MODE, DEFAULT_NODE_LABEL_SIZE_MODE));
    protected static LabelColorMode defaultNodeLabelColorMode =
        LabelColorMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(NODE_LABEL_COLOR_MODE, DEFAULT_NODE_LABEL_COLOR_MODE));
    protected static boolean defaultUseEdgeWeight =
        NbPreferences.forModule(VizConfig.class).getBoolean(EDGE_WEIGHTED, DEFAULT_EDGE_WEIGHTED);
    protected static boolean defaultRescaleEdgeWeight =
        NbPreferences.forModule(VizConfig.class).getBoolean(EDGE_RESCALE_WEIGHT, DEFAULT_EDGE_RESCALE_WEIGHTED);
    protected static float defaultNodeLabelScale =
        NbPreferences.forModule(VizConfig.class).getFloat(NODE_LABEL_SCALE, DEFAULT_NODE_LABEL_SCALE);
    protected static float defaultEdgeLabelScale =
        NbPreferences.forModule(VizConfig.class).getFloat(EDGE_LABEL_SCALE, DEFAULT_EDGE_LABEL_SCALE);
    protected static LabelSizeMode defaultEdgeLabelSizeMode =
        LabelSizeMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(EDGE_LABEL_SIZE_MODE, DEFAULT_EDGE_LABEL_SIZE_MODE));
    protected static LabelColorMode defaultEdgeLabelColorMode =
        LabelColorMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(EDGE_LABEL_COLOR_MODE, DEFAULT_EDGE_LABEL_COLOR_MODE));
    protected static float defaultZoom = NbPreferences.forModule(VizConfig.class).getFloat(ZOOM, DEFAULT_ZOOM);
    protected static int defaultMouseSelectionDiameter =
        NbPreferences.forModule(VizConfig.class).getInt(MOUSE_SELECTION_DIAMETER, DEFAULT_MOUSE_SELECTION_DIAMETER);
    protected static int screenshotScaleFactor =
        NbPreferences.forModule(VizConfig.class)
            .getInt(SCREENSHOT_SCALE_FACTOR, DEFAULT_SCREENSHOT_SCALE_FACTOR);
    protected static boolean screenshotTransparentBackground =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(SCREENSHOT_TRANSPARENT_BACKGROUND, DEFAULT_SCREENSHOT_TRANSPARENT_BACKGROUND);
    protected static boolean screenshotAutoSave =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(SCREENSHOT_AUTO_SAVE, DEFAULT_SCREENSHOT_AUTO_SAVE);
    //Preferences
    protected static int antialiasing =
        NbPreferences.forModule(VizConfig.class).getInt(ANTIALIASING, DEFAULT_ANTIALIASING);
    protected static boolean enableContextMenu =
        NbPreferences.forModule(VizConfig.class).getBoolean(CONTEXT_MENU, DEFAULT_CONTEXT_MENU);

    public static int getAntialiasing() {
        return antialiasing;
    }

    public static boolean isEnableContextMenu() {
        return enableContextMenu;
    }

    public static float getDefaultZoom() {
        return defaultZoom;
    }

    public static Vector2fc getDefaultPan() {
        return new Vector2f(0.0f, 0.0f);
    }

    public static boolean isDefaultAutoSelectNeighbor() {
        return defaultAutoSelectNeighbor;
    }

    public static Color getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    public static Color getDefaultDarkBackgroundColor() {
        return defaultDarkBackgroundColor;
    }

    public static Font getDefaultEdgeLabelFont() {
        return defaultEdgeLabelFont;
    }

    public static boolean isDefaultHideNonSelectedEdges() {
        return defaultHideNonSelectedEdges;
    }

    public static boolean isDefaultLightenNonSelectedAuto() {
        return defaultLightenNonSelectedAuto;
    }

    public static float getDefaultLightenNonSelectedFactor() {
        return defaultLightenNonSelectedFactor;
    }

    public static Font getDefaultNodeLabelFont() {
        return defaultNodeLabelFont;
    }

    public static boolean isDefaultShowEdgeLabels() {
        return defaultShowEdgeLabels;
    }

    public static boolean isDefaultHideNonSelectedNodeLabels() {
        return defaultHideNonSelectedNodeLabels;
    }

    public static boolean isDefaultHideNonSelectedEdgeLabels() {
        return defaultHideNonSelectedEdgeLabels;
    }

    public static boolean isDefaultFitNodeLabelsToNodeSize() {
        return defaultFitNodeLabelsToNodeSize;
    }

    public static boolean isDefaultAvoidNodeLabelOverlap() {
        return defaultAvoidNodeLabelOverlap;
    }

    public static boolean isDefaultShowNodeLabels() {
        return defaultShowNodeLabels;
    }

    public static boolean isDefaultShowEdges() {
        return defaultShowEdges;
    }

    public static boolean isDefaultEdgeSelectionColor() {
        return defaultEdgeSelectionColor;
    }

    public static Color getDefaultEdgeBothSelectedColor() {
        return defaultEdgeBothSelectedColor;
    }

    public static Color getDefaultEdgeInSelectedColor() {
        return defaultEdgeInSelectedColor;
    }

    public static Color getDefaultEdgeOutSelectedColor() {
        return defaultEdgeOutSelectedColor;
    }

    public static LabelSizeMode getDefaultNodeLabelSizeMode() {
        return defaultNodeLabelSizeMode;
    }

    public static LabelColorMode getDefaultNodeLabelColorMode() {
        return defaultNodeLabelColorMode;
    }

    public static boolean isDefaultUseEdgeWeight() {
        return defaultUseEdgeWeight;
    }

    public static boolean isDefaultRescaleEdgeWeight() {
        return defaultRescaleEdgeWeight;
    }

    public static float getDefaultNodeLabelScale() {
        return defaultNodeLabelScale;
    }

    public static float getDefaultEdgeLabelScale() {
        return defaultEdgeLabelScale;
    }

    public static LabelSizeMode getDefaultEdgeLabelSizeMode() {
        return defaultEdgeLabelSizeMode;
    }

    public static LabelColorMode getDefaultEdgeLabelColorMode() {
        return defaultEdgeLabelColorMode;
    }

    public static int getDefaultMouseSelectionDiameter() {
        return defaultMouseSelectionDiameter;
    }

    public static float getDefaultEdgeScale() {
        return defaultEdgeScale;
    }

    public static float getDefaultNodeScale() {
        return defaultNodeScale;
    }

    public static EdgeColorMode getDefaultEdgeColorMode() {
        return defaultEdgeColorMode;
    }

    public static int getDefaultScreenshotScaleFactor() {
        return screenshotScaleFactor;
    }

    public static boolean isDefaultScreenshotTransparentBackground() {
        return screenshotTransparentBackground;
    }

    public static boolean isDefaultScreenshotAutoSave() {
        return screenshotAutoSave;
    }
}
