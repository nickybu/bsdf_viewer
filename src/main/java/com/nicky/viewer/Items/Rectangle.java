package com.nicky.viewer.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Rectangle</h1>
 * Represents a rectanglur object.
 * Also used to draw lines in OpenGL.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Rectangle {

    private float red;
    private float green;
    private float blue;

    private float[] vertices;
    private int[] indices;
    private float[] colours;

    public Rectangle(float red, float green, float blue, float[] vertices, int[] indices) {

        this.red = red;
        this.green = green;
        this.blue = blue;

        this.vertices = vertices;
        this.indices = indices;
        colours = new float[0];
    }

    public void init() {
        // Colour
        int counter = 0;

        List<Float> coloursList = getRectangleColours(vertices.length);
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

    private List<Float> getRectangleColours(int verticesNum) {
        List<Float> colours = new ArrayList<>();

        for (int i = 0; i < verticesNum; i++) {
            colours.add(this.red);
            colours.add(this.green);
            colours.add(this.blue);
        }

        return colours;
    }

    public float[] getVertices() {
        return vertices;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getColours() {
        return colours;
    }

}
