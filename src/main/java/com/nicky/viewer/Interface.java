package com.nicky.viewer;

import com.nicky.Spectrum;
import com.nicky.engine.ViewerEngine;
import javafx.util.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Logger;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * <h1>Interface</h1>
 * Represents User Interface implemented using Nuklear.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Interface {
    private static final Logger LOGGER = Logger.getLogger(Viewer.class.getName());
    private static NumberFormat vectorFormatter;
    public NkPluginFilter stringfilter = NkPluginFilter.create(Nuklear::nnk_filter_default);
    // RGB selector
    public NkColorf spectrum = NkColorf.create()
            .r(0.00f)
            .g(1.00f)
            .b(0.00f);
    public float floatEx = 0.5f;
    // BRDF Alias
    ByteBuffer aliasBuffer = BufferUtils.createByteBuffer(64);
    IntBuffer aliasIntBuffer = BufferUtils.createIntBuffer(1);
    private String title;
    private Viewer viewer;
    private ViewerEngine viewerEngine;
    private int height, width;
    // BRDFs
    private int currentBRDF;
    private List<String> brdfNames;
    private CharBuffer brdfNamesCharBuffer = CharBuffer.allocate(100);
    // Sunflow Scenes
    private List<String> sunflowScenes;
    private int currentSunflowScene;
    private CharBuffer sunflowScenesCharBuffer = CharBuffer.allocate(100);
    // Float
    private ByteBuffer byteBuffer = ByteBuffer.allocate(256);
    // BRDF Elements
    private Map<String, NkColorf> spectrumElements;
    private Map<String, FloatBuffer> floatElements;
    private FloatBuffer f = BufferUtils.createFloatBuffer(1).put(0, floatEx);

    public Interface(String title, Viewer viewer, ViewerEngine viewerEngine, int height, int width) {
        this.title = title;
        this.viewer = viewer;
        this.viewerEngine = viewerEngine;
        this.height = height;
        this.width = width;

        vectorFormatter = NumberFormat.getInstance();

        currentBRDF = 0;
        currentSunflowScene = 0;
        brdfNames = new ArrayList<>();
        sunflowScenes = new ArrayList<>();

        spectrumElements = new HashMap<>();
        floatElements = new HashMap<>();
    }

    public void init() {
        updateBRDFNamesBuffer();
        updateSunflowSceneNamesBuffer();

//        byteBuffer.clear();
//        byteBuffer.putInt(10);
//        byteBuffer.flip();


//        viewer.updateCurrentBRDF(brdfNames.get(currentBRDF));
    }

    public void layout(NkContext ctx, int x, int y) {
        try (MemoryStack stack = stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);

            if (nk_begin(ctx, title, nk_rect(x, y, width, height, rect),
                    NK_WINDOW_BORDER | NK_WINDOW_TITLE
            )) {
                // BRDF List
                nk_layout_row_dynamic(ctx, 30, 2);
                nk_label(ctx, "Select BRDF: ", NK_TEXT_LEFT);
                // Refresh BRDF List Button
                if (nk_button_label(ctx, "Refresh BRDF List")) {
                    LOGGER.info("Refreshed BRDF List.");
                }
                // BRDF Name Combobox
                nk_layout_row_static(ctx, 30, 370, 1);
                if (nk_combo_begin_label(ctx, brdfNamesCharBuffer, NkVec2.mallocStack(stack).set(nk_widget_width(ctx), 400))) {
                    nk_layout_row_dynamic(ctx, 25, 1);
                    for (int i = 0; i < brdfNames.size(); i++) {
                        if (nk_combo_item_label(ctx, brdfNames.get(i), NK_TEXT_LEFT)) {
                            currentBRDF = i;
                            updateBRDFNamesBuffer();
//                            viewerEngine.getInterfaceWindow().changeInterface(currentBRDF);
                        }
                    }
                    nk_combo_end(ctx);
                }

                // Incident Ray
                nk_layout_row_dynamic(ctx, 30, 2);
                nk_label(ctx, "Incident Ray: ", NK_TEXT_LEFT);
                nk_label(ctx,
                        "X:" + String.format("%.3f", viewer.getIncidentRaySource().x) + ", " +
                                "Y:" + String.format("%.3f", viewer.getIncidentRaySource().y) + ", " +
                                "Z:" + String.format("%.3f", viewer.getIncidentRaySource().z),
                        NK_TEXT_RIGHT);

                nk_layout_row_static(ctx, 10, 370, 1);
                nk_label(ctx, "__________________________________________________", NK_TEXT_CENTERED);
                nk_layout_row_static(ctx, 40, 370, 1);
                nk_label(ctx, brdfNames.get(currentBRDF) + " Properties:", NK_TEXT_LEFT);

                // BRDF parameters
                addElements(ctx, stack, viewerEngine.getInterfaceWindow().getBRDFParameters(brdfNames.get(currentBRDF)));

                nk_layout_row_static(ctx, 10, 370, 1);
                nk_layout_row_dynamic(ctx, 30, 2);
                // Build Lobe Button
                if (nk_button_label(ctx, "Build BRDF Lobe")) {
                    LOGGER.info("Building BRDF Lobe...");
                }
                // Reset to Default Button
                if (nk_button_label(ctx, "Reset BRDF")) {
                    LOGGER.info("Reset BRDF to default values.");
                }

                // Spacing row
                nk_layout_row_static(ctx, 10, 370, 1);
                nk_label(ctx, "__________________________________________________", NK_TEXT_CENTERED);
                nk_layout_row_static(ctx, 10, 370, 1);

                nk_layout_row_dynamic(ctx, 30, 2);
                // Save Custom BRDF Button
                nk_layout_row_dynamic(ctx, 30, 2);
                nk_label(ctx, "BRDF Alias:", NK_TEXT_LEFT);
                nk_edit_string(ctx, NK_EDIT_SIMPLE, aliasBuffer, aliasIntBuffer, 64, stringfilter);
                nk_layout_row_dynamic(ctx, 30, 1);
                if (nk_button_label(ctx, "Save BRDF")) {
                    LOGGER.info("BRDF saved.");
                    viewer.saveBRDF(getBRDF(currentBRDF));
                }

                // Spacing row
                nk_layout_row_dynamic(ctx, 20, 1);

                nk_layout_row_dynamic(ctx, 30, 2);
                // Render Scene Button
                nk_label(ctx, "Select Scene:", NK_TEXT_LEFT);
                if (nk_combo_begin_label(ctx, sunflowScenesCharBuffer, NkVec2.mallocStack(stack).set(nk_widget_width(ctx), 400))) {
                    nk_layout_row_dynamic(ctx, 25, 1);
                    for (int i = 0; i < sunflowScenes.size(); i++) {
                        if (nk_combo_item_label(ctx, sunflowScenes.get(i), NK_TEXT_LEFT)) {
                            currentSunflowScene = i;
                            updateSunflowSceneNamesBuffer();
                        }
                    }
                    nk_combo_end(ctx);
                }
                nk_layout_row_dynamic(ctx, 30, 1);
                if (nk_button_label(ctx, "Render Scene")) {
                    LOGGER.info("Rendering scene with BRDF...");
                    viewer.renderInSunflow("", "cornell_box_jensen.sc");
                }
            }
            nk_end(ctx);
        }
    }

    public void testElements(NkContext ctx, MemoryStack stack) {

        // Spectrum
        nk_layout_row_dynamic(ctx, 30, 2);
        nk_label(ctx, "Spectrum:", NK_TEXT_LEFT);
        if (nk_combo_begin_color(ctx, nk_rgb_cf(spectrum, NkColor.mallocStack(stack)), NkVec2.mallocStack(stack).set(nk_widget_width(ctx), 400))) {
            nk_layout_row_dynamic(ctx, 120, 1);
            nk_color_picker(ctx, spectrum, NK_RGB);
            nk_layout_row_dynamic(ctx, 25, 1);
            spectrum.r(nk_propertyf(ctx, "#R:", 0, spectrum.r(), 1.0f, 0.01f, 0.005f));
            spectrum.g(nk_propertyf(ctx, "#G:", 0, spectrum.g(), 1.0f, 0.01f, 0.005f));
            spectrum.b(nk_propertyf(ctx, "#B:", 0, spectrum.b(), 1.0f, 0.01f, 0.005f));
            nk_combo_end(ctx);
        }

        // Single value slider
        nk_layout_row_dynamic(ctx, 30, 1);
        nk_property_float(ctx, "Float Slider:", -5.0f, f, 5.0f, 0.5f, 1);
    }

    public void addElements(NkContext ctx, MemoryStack stack, LinkedHashMap<String, Pair<String, String>> brdf) {
        try {
            if (brdf != null) {
                for (Map.Entry<String, Pair<String, String>> entry : brdf.entrySet()) {
                    // If composite
                    if (entry.getValue() == null) {
                        // Add Title for BRDF Component
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_label(ctx, "Component: " + entry.getKey(), NK_TEXT_LEFT);
                    }
                    // Spectrum
                    if (entry.getKey().equals("Spectrum")) {
                        float[] spectrumValues = Spectrum.parseString(entry.getValue().getValue());
                        NkColorf spectrum = NkColorf.create()
                                .r(spectrumValues[0])
                                .g(spectrumValues[1])
                                .b(spectrumValues[2]);
                        spectrumElements.put(brdfNames.get(currentBRDF), spectrum);
                        nk_layout_row_dynamic(ctx, 30, 2);
                        nk_label(ctx, entry.getValue().getKey(), NK_TEXT_LEFT);
                        if (nk_combo_begin_color(ctx, nk_rgb_cf(spectrum, NkColor.mallocStack(stack)), NkVec2.mallocStack(stack).set(nk_widget_width(ctx), 400))) {
                            nk_layout_row_dynamic(ctx, 120, 1);
                            nk_color_picker(ctx, spectrum, NK_RGB);
                            nk_layout_row_dynamic(ctx, 25, 1);
                            spectrum.r(nk_propertyf(ctx, "#R:", 0, spectrumElements.get(brdfNames.get(currentBRDF)).r(), 1.0f, 0.01f, 0.005f));
                            spectrum.g(nk_propertyf(ctx, "#G:", 0, spectrumElements.get(brdfNames.get(currentBRDF)).g(), 1.0f, 0.01f, 0.005f));
                            spectrum.b(nk_propertyf(ctx, "#B:", 0, spectrumElements.get(brdfNames.get(currentBRDF)).b(), 1.0f, 0.01f, 0.005f));
                            nk_combo_end(ctx);
                        }
                    } else if (entry.getKey().equals("float")) {
                        float value = Float.valueOf(entry.getValue().getValue());
                        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(1).put(0, value);
                        floatElements.put(brdfNames.get(currentBRDF), floatBuffer);
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_property_float(ctx, entry.getValue().getKey(), -5.0f, floatElements.get(brdfNames.get(currentBRDF)), 5.0f, 0.5f, 1);
                    } else {
                        throw new Exception("Incorrect BRDF UI Parameters");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getCurrentBRDF() {
        return currentBRDF;
    }

    public void setBrdfNames(List<String> brdfNames) {
        this.brdfNames = brdfNames;
    }

    public String getBRDF(int i) {
        return brdfNames.get(i);
    }

    public String getSunflowScene(int i) {
        return sunflowScenes.get(i);
    }

    public void setSunflowScenes(List<String> sunflowScenes) {
        this.sunflowScenes = sunflowScenes;
    }

    public void updateBRDFNamesBuffer() {
        brdfNamesCharBuffer.clear();
        brdfNamesCharBuffer.put(brdfNames.get(currentBRDF));
        brdfNamesCharBuffer.flip();
    }

    public void updateSunflowSceneNamesBuffer() {
        sunflowScenesCharBuffer.clear();
        sunflowScenesCharBuffer.put(sunflowScenes.get(currentSunflowScene));
        sunflowScenesCharBuffer.flip();
    }
}
