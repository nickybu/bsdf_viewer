package com.nicky.viewer;

import com.nicky.brdfs.BRDF;
import com.nicky.engine.*;
import com.nicky.resources.Utilities;
import com.nicky.viewer.Items.Rectangle;
import com.nicky.viewer.Items.Sphere;
import javafx.util.Pair;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;

/**
 * <h1>Viewer</h1>
 * Manages viewer window.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Viewer {

    private static final Logger LOGGER = Logger.getLogger(Viewer.class.getName());
    private static final Vector3f pointOnSurface = new Vector3f(0f, 0f, 0f);
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;
    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private Vector3f incidentRaySource = new Vector3f(1f, 1f, 0f).normalize();
    private Properties configProperties;
    private Sphere unitSphere;
    private Rectangle plane;
    private Rectangle incidentRay;

    private ViewerItem[] viewerItems;
    private ViewerItem unitSphereItem;
    private ViewerItem planeItem;
    private ViewerItem incidentRayItem;

    private Map<String, BRDF> brdfs;
    private Pair<String, BRDF> currentBRDF;

    private Map<String, String> sunflowScenes;

    private int renderCounter = 0;
    private boolean incidentRayUpdated = false;
    private boolean sphereUpdated = false;
    private boolean cameraTopView = false;

    public Viewer() throws IOException {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0f, 0f, 0f);

        unitSphere = new Sphere(0, 0, 0);
        plane = new Rectangle(0.8f, 0.8f, 0.8f, getPlaneVertices(), getRectangleIndices());
        incidentRay = new Rectangle(0, 1, 0, getIncidentRayVertices(), getRectangleIndices());
        brdfs = new HashMap<>();

        sunflowScenes = new HashMap<String, String>() {{
            put("Aliens", "aliens_shiny.sc");
            put("Cornell Box", "cornell_box_jensen.sc");
            put("Julia", "julia.sc");
            put("Sphere Flake", "sphereflake.sc");
            put("Standford Bunny", "bunny_ibl.sc");
            put("Teapot", "gumbo_and_teapot.sc");
        }};

        configProperties = new Properties();
        try {
            InputStream in = Viewer.class.getResourceAsStream("/app.properties");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = br.readLine()) != null) {
                String values[] = line.split("=", 2);
                configProperties.setProperty(values[0], values[1]);
            }
        } catch (IOException e) {
            throw e;
        }
        LOGGER.info("Point on Surface: " + pointOnSurface.toString());
    }

    public void init(Window window, Map<String, BRDF> brdfs) throws Exception {
        renderer.init(window);
        this.brdfs = brdfs;

        // Initialise unit sphere
        unitSphere.init();
        updateSphereItem();
        buildLobe("ShinyDiffuseBRDF");

        // Initialise plane
        plane.init();
        planeItem = getViewerItemFromItem(plane);
        Vector3f spherePos = unitSphereItem.getPosition();
        planeItem.setPosition(spherePos.x, spherePos.y, spherePos.z);

        // Initialise incident ray
        incidentRay.init();
        updateIncidentRayItem();
        incidentRayItem = getViewerItemFromItem(incidentRay);

        viewerItems = new ViewerItem[]{
                unitSphereItem,
                planeItem,
                incidentRayItem,
        };

        LOGGER.info("Initialised Viewer...");
    }

    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);

        // Up: Zoom in
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            if (camera.getRotation().x == 90) {
                cameraInc.y = -0.5f;
            } else {
                cameraInc.z = -0.5f;
            }
            // Down: Zoom out
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            if (camera.getRotation().x == 90) {
                cameraInc.y = 0.5f;
            } else {
                cameraInc.z = 0.5f;
            }
            // Left: Rotate lobe left
        } else if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            unitSphereItem.setRotation(0, unitSphereItem.getRotation().y - 0.5f, 0);
            incidentRayItem.setRotation(0, unitSphereItem.getRotation().y - 0.5f, 0);
            // Right: rotate lobe right
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            unitSphereItem.setRotation(0, unitSphereItem.getRotation().y + 0.5f, 0);
            incidentRayItem.setRotation(0, unitSphereItem.getRotation().y + 0.5f, 0);
            // T: Top view
        } else if (window.isKeyPressedOnce(GLFW_KEY_RIGHT_SHIFT)) {
            if (cameraTopView) {
                camera.setPosition(0, 0, 2);
                camera.setRotation(0, 0, 0);
                cameraTopView = false;
            } else {
                camera.setPosition(0, 0.5f, 0);
                camera.setRotation(90, 0, 0);
                cameraTopView = true;
            }
            // A: Adjust incident ray x axis
        } else if (window.isKeyPressed(GLFW_KEY_A)) {
//            if(isIncidentRayMalformed()) {
//                LOGGER.warning("Incident ray is malformed!");
//            }
            if (incidentRaySource.normalize().x >= -1.0f) {
                incidentRaySource.x -= 0.05f;
                incidentRayUpdated = true;
            }
            // D: Adjust incident ray x axis
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            if (incidentRaySource.normalize().x <= 1.0f) {
                incidentRaySource.x += 0.05f;
                incidentRayUpdated = true;
            }
            // W: Adjust incident ray z axis
        } else if (window.isKeyPressed(GLFW_KEY_W)) {
            if (incidentRaySource.normalize().z <= 1.0f) {
                incidentRaySource.z += 0.05f;
                incidentRayUpdated = true;
            }
            // S: Adjust incident ray z axis
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            if (incidentRaySource.normalize().z >= -1.0f) {
                incidentRaySource.z -= 0.05f;
                incidentRayUpdated = true;
            }
        }

    }

    public void update(MouseInput mouseInput) {
        // Update incident ray
        if (incidentRayUpdated) {
            updateIncidentRayItem();
            buildLobe(currentBRDF.getKey());
        }

        // Update camera position
        camera.movePosition(
                cameraInc.x * CAMERA_POS_STEP,
                cameraInc.y * CAMERA_POS_STEP,
                cameraInc.z * CAMERA_POS_STEP
        );

        // Update camera position based on mouse (left)
        // Rotate camera
        if (mouseInput.isLeftButtonPressed()) {
            Vector2f rotationVec = mouseInput.getDisplVec();
            float rotation = unitSphereItem.getRotation().y + rotationVec.y;
            if (rotation > 360) {
                rotation = 0;
            }
            unitSphereItem.setRotation(0, rotation, 0);
            incidentRayItem.setRotation(0, rotation, 0);
//            camera.moveRotation(
//                    rotationVec.x * MOUSE_SENSITIVITY,
//                    rotationVec.y * MOUSE_SENSITIVITY,
//                    0);
        }

        // Update camera position based on mouse (right)
        // Zoom Camera
        if (mouseInput.isRightButtonPressed()) {
            Vector2f positionVec = mouseInput.getDisplVec();
//            camera.movePosition(0, 0, positionVec.x * CAMERA_POS_STEP);
            camera.zoom(positionVec.x * CAMERA_POS_STEP);
        }

        if (incidentRayUpdated || sphereUpdated) {
            updateViewerItems();
        }

//         Rotate automatically
        // Update rotation angle
//        float rotation = unitSphereItem.getRotation().y + 0.2f;
//        if ( rotation > 360 ) {
//            rotation = 0;
//        }
//        unitSphereItem.setRotation(0, rotation, 0);
    }

    public void render(Window window) {
        window.setClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        renderer.render(window, viewerItems, camera);
    }

    public void cleanup() {
        renderer.cleanup();
        for (ViewerItem viewerItem : viewerItems) {
            viewerItem.getMesh().cleanup();
        }
    }

    public void updateIncidentRayItem() {
        incidentRay.setVertices(getIncidentRayVertices());
        incidentRayItem = getViewerItemFromItem(incidentRay);
        incidentRayItem.setPosition(pointOnSurface.x, pointOnSurface.y, pointOnSurface.z);
    }

    public void updateSphereItem() {

        float[] unitSphereVertices = unitSphere.getVertices();
        int[] unitSphereIndices = unitSphere.getIndices();
        float[] unitSphereColours = unitSphere.getColours();

        // Update mesh for unit sphere
        Mesh unitSphereMesh = new Mesh(unitSphereVertices, unitSphereIndices, unitSphereColours);
        // Update ViewerItem for unit sphere
        unitSphereItem = new ViewerItem(unitSphereMesh);

        // Adjust unit sphere properties
        unitSphereItem.setPosition(pointOnSurface.x, pointOnSurface.y, pointOnSurface.z);
    }

    public void buildLobe(String brdfAlias) {
        if (brdfAlias != null) {
            LOGGER.info("Incident Ray: " + incidentRaySource.normalize().toString());
            LOGGER.info("Evaluating BRDF: " + brdfAlias);
            unitSphere.sampleBRDF(incidentRaySource.normalize(), brdfs.get(brdfAlias));
            updateSphereItem();
            LOGGER.info("Built lobe...");
            currentBRDF = new Pair<>(brdfAlias, brdfs.get(brdfAlias));
            sphereUpdated = true;
        } else {
            updateSphereItem();
        }

    }

    private void updateViewerItems() {
        viewerItems = new ViewerItem[]{
                unitSphereItem,
                planeItem,
                incidentRayItem,
        };

        incidentRayUpdated = false;
        sphereUpdated = false;

        if (viewerItems[0] != unitSphereItem &&
                viewerItems[1] != planeItem &&
                viewerItems[2] != incidentRayItem) {
            LOGGER.warning("Viewer Items not updated correctly!");
        }
    }

    private ViewerItem getViewerItemFromItem(Rectangle item) {
        float[] vertices = item.getVertices();
        int[] indices = item.getIndices();
        float[] colours = item.getColours();

        Mesh mesh = new Mesh(vertices, indices, colours);
        return new ViewerItem(mesh);
    }

    private float[] getPlaneVertices() {
        return new float[]{
                -1.5f, 0f, -1.5f,
                -1.5f, 0f, 1.5f,
                1.5f, 0f, 1.5f,
                1.5f, 0f, -1.5f,
        };
    }

    private int[] getRectangleIndices() {
        return new int[]{
                0, 1, 2, 3, 0, 2
        };
    }

    private float[] getIncidentRayVertices() {
        incidentRaySource.normalize();
        return new float[]{
                incidentRaySource.x, incidentRaySource.y, incidentRaySource.z,
                pointOnSurface.x, pointOnSurface.y, pointOnSurface.z,
                incidentRaySource.x, incidentRaySource.y, incidentRaySource.z,
                pointOnSurface.x, pointOnSurface.y, pointOnSurface.z,
        };
    }

    private boolean isIncidentRayMalformed() {
        return new Vector3f(1, 0, 0).normalize().dot(incidentRaySource) < 0;
    }

    public void updateCurrentBRDF(String alias) {
        BRDF brdf = brdfs.get(alias);
        currentBRDF = new Pair<>(alias, brdf);
        buildLobe(alias);
        System.out.println(currentBRDF.getKey());
    }

    public List<String> getSunflowSceneNames() {
        List<String> l = new ArrayList<>();
        for (Map.Entry<String, String> entry : sunflowScenes.entrySet()) {
            l.add(entry.getKey());
        }
        return l;
    }

    public void renderInSunflow(String brdfAlias, String scene) {

        LOGGER.info("Exporting BRDF...");
        String jsonString = Utilities.serialiseBRDFJson(new Pair<>(currentBRDF.getKey(), currentBRDF.getValue()));
//        System.out.println(jsonString);
        String filepath = configProperties.getProperty("brdf_output_path") + "sunflow/" + currentBRDF.getKey() + ".json";
        boolean saved = Utilities.saveJsonToFile(filepath, jsonString);
        if (saved) {
            LOGGER.info("Rendering scene...");

            String sunflowPath = configProperties.getProperty("sunflow_jar_path");
            String outputName = configProperties.getProperty("sunflow_output_path") + "render_" + currentBRDF.getKey()
                    + "_" + renderCounter + ".jpg";
            String sunflowRAM = configProperties.getProperty("sunflow_ram");
            String sceneFolderPath = configProperties.getProperty("sunflow_scenes_path");
            String sunflowScene = "cornell_box_jensen.sc";

            // Build command
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-Xmx" + sunflowRAM);
            command.add("-server");
            command.add("-jar");
            command.add(sunflowPath);
            command.add(sceneFolderPath + sunflowScene);
            command.add("-o");
            command.add(outputName);
            command.add("-brdfAlias");
            command.add(brdfAlias);
//            command.add("-nogui");


            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Map<String, String> env = processBuilder.environment();

            System.out.println(processBuilder.command().toString());

            Process process = null;
            try {
                process = processBuilder.start();
                InputStream inputStream = process.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            renderCounter++;
        } else {
            LOGGER.warning("Error when saving BRDF to " + filepath);
        }
    }

    public void saveBRDF(String alias) {
        String jsonString = Utilities.serialiseBRDFJson(new Pair<>(alias, currentBRDF.getValue()));
        String filepath = configProperties.getProperty("brdf_output_path") + "custom/" + alias + ".json";
        boolean saved = Utilities.saveJsonToFile(filepath, jsonString);
        if (saved) {
            LOGGER.info("Saved BRDF.");
        } else {
            LOGGER.warning("Could not save BRDF...");
        }
    }

    public Vector3f getIncidentRaySource() {
        return incidentRaySource;
    }
}
