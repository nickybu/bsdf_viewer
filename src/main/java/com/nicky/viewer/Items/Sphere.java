package com.nicky.viewer.Items;

import com.nicky.Spectrum;
import com.nicky.brdfs.BRDF;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Sphere</h1>
 * Represents sphere item used to build BRDF lobe.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Sphere {

    private int SECTORS;
    private int RINGS;

    private float red;
    private float green;
    private float blue;

    private float[] unitSphereVertices;

    private float[] vertices;
    private int[] indices;
    private float[] colours;

    public Sphere(float red, float green, float blue) {
        SECTORS = 150;
        RINGS = 150;

        this.red = red;
        this.green = green;
        this.blue = blue;

        unitSphereVertices = new float[0];
        vertices = new float[0];
        indices = new int[0];
        colours = new float[0];
    }

    public void init() {
        setVertices();
        setIndices();
        setColours();
    }

    public void setVertices() {
        int counter = 0;

        // Sphere vertex positions
        // Counter-clockwise order
        List<Float> verticesList = getUnitSphereVertices(RINGS, SECTORS);

        unitSphereVertices = new float[verticesList.size()];
        for (Float v : verticesList) {
            if (v != null) {
                unitSphereVertices[counter] = v;
            } else {
                unitSphereVertices[counter] = Float.NaN;
            }
            counter++;
        }
        vertices = unitSphereVertices;
    }

    public void setVertices(Vector3f incident, BRDF brdf) {
        int counter = 0;

        // Sphere vertex positions
        // Counter-clockwise order
        List<Float> verticesList = getSphereVertices(incident, brdf);

        vertices = new float[verticesList.size()];
        for (Float v : verticesList) {
            if (v != null) {
                vertices[counter] = v;
            } else {
                vertices[counter] = Float.NaN;
            }
            counter++;
        }
    }

    public void setIndices() {
        int counter = 0;

        // Sphere indices making up the 12 triangles
        List<Integer> indicesList = getSphereIndices(RINGS, SECTORS);
        indices = new int[indicesList.size()];
        for (Integer i : indicesList) {
            indices[counter] = i;
            counter++;
        }
    }

    public void setColours() {
        int counter = 0;

        // Sphere colour
        List<Float> coloursList = getSphereColours(vertices.length);
        colours = new float[coloursList.size()];
        for (Float c : coloursList) {
            if (c != null) {
                colours[counter] = c;
            } else {
                colours[counter] = Float.NaN;
            }
            counter++;
        }
    }

    private List<Float> getUnitSphereVertices(int rings, int sectors) {
        List<Float> vertices = new ArrayList<>();
        List<Float> coloursList = new ArrayList<>();

        float R = 1.0f / (rings - 1.0f); // rings
        float S = 1.0f / (sectors - 1.0f); // sectors

        for (int r = 0; r < rings; r++) {
            float rR = r * R;
            for (int s = 0; s < sectors; s++) {
                float sS = s * S;
                float y = (float) Math.sin(-Math.PI * 0.5 + Math.PI * rR);
                float sin = (float) Math.sin(Math.PI * rR);
                float x = (float) (Math.cos(2 * Math.PI * sS) * sin);
                float z = (float) (Math.sin(2 * Math.PI * sS) * sin);

                float radius;
                if (y >= 0) {
                    radius = 1.0f;
                    vertices.add(x * radius);
                    vertices.add(y * radius);
                    vertices.add(z * radius);
                }
            }
        }
        setColours();
        return vertices;
    }

    private List<Float> getSphereVertices(Vector3f incident, BRDF brdf) {
        Vector3f out = new Vector3f(); //exitant light ray
        List<Float> vertices = new ArrayList<>();
        List<Float> coloursList = new ArrayList<>();

        for (int i = 0; i < unitSphereVertices.length; i += 3) {
            float x = unitSphereVertices[i];
            float y = unitSphereVertices[i + 1];
            float z = unitSphereVertices[i + 2];

            // Initialise out vector with this direction
            out.set(x, y, z).normalize();

            // Evaluate BRDF
            float radius;
            if (brdf != null && out.y >= 0) {
                // Only upper hemisphere
//                    System.out.println(r+","+s+ ": "+(brdf.f(incident, out)).toScalar());
//                    System.out.println("in:" + incident + " out: " + out);
                Spectrum brdfOutput = brdf.f(incident, out);
                radius = brdfOutput.toScalar();
                vertices.add(x * radius);
                vertices.add(y * radius);
                vertices.add(z * radius);

                coloursList.add(brdfOutput.getR());
                coloursList.add(brdfOutput.getG());
                coloursList.add(brdfOutput.getB());

                Vector3f v1 = new Vector3f(0, 0, 0);
                Vector3f v2 = new Vector3f(x * radius, y * radius, z * radius);
//                    System.out.println("Radius: " + (v1.sub(v2).length()));
            }

        }
        setColours(coloursList);
        return vertices;
    }

    private List<Integer> getSphereIndices(int rings, int sectors) {
        List<Integer> indices = new ArrayList<>();

        for (int r = 0; r < rings - 1; r++) {
            for (int s = 0; s < sectors - 1; s++) {
                indices.add(r * sectors + s);
                indices.add((r + 1) * sectors + s);
                indices.add((r + 1) * sectors + (s + 1));
                indices.add((r + 1) * sectors + (s + 1));
                indices.add(r * sectors + (s + 1));
                indices.add(r * sectors + s);
            }
        }
        return indices;
    }

    private List<Float> getSphereColours(int verticesNum) {
        List<Float> colours = new ArrayList<>();

        for (int i = 0; i < verticesNum; i++) {
            colours.add(this.red);
            colours.add(this.green);
            colours.add(this.blue);
        }

        return colours;
    }

    /**
     * Sample a BRDF and create its lobe
     * @param incident
     * @param brdf
     */
    public void sampleBRDF(Vector3f incident, BRDF brdf) {
        setVertices(incident, brdf);
        setIndices();
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getColours() {
        return colours;
    }

    public void setColours(List<Float> coloursList) {
        int counter = 0;

        // Sphere colour
        colours = new float[coloursList.size()];
        for (Float c : coloursList) {
            if (c != null) {
                colours[counter] = c;
            } else {
                colours[counter] = Float.NaN;
            }
            counter++;
        }
    }

    public void setRed(float red) {
        this.red = red;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }
}