package com.nicky.resources;

import com.nicky.brdfs.BRDF;
import com.nicky.brdfs.CompositeBRDF;
import javafx.util.Pair;
import org.lwjgl.BufferUtils;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.BufferUtils.createByteBuffer;

/**
 * <h1>Utilities</h1>
 * A set of utility methods.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Utilities {

    // Load source code from file
    public static String loadResource(String path) throws IOException {
        String sourceCode = "";

        try {
            sourceCode = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw e;
        }

        return sourceCode;
    }

    /**
     * Serialise a BRDF into JSON
     * @param brdfPair BRDF Name and Instance to be serialised
     * @return String Returns serialised BRDF.
     */
    public static String serialiseBRDFJson(Pair<String, BRDF> brdfPair) {
        String alias = brdfPair.getKey();
        BRDF brdf = brdfPair.getValue();
        boolean simple = brdf.getClass() != CompositeBRDF.class;
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"alias\": ").append("\"").append(alias).append("\",");
        if (simple) {
            sb.append("\"type\": ").append("\"simple\",");
            sb.append("\"components\": [");
            sb.append("{");
            sb.append("\"name\": ").append("\"").append(brdf.getName()).append("\",");
            sb.append("\"type\": ").append("\"simple\",");
            sb.append(brdf.serialise());
            sb.append("}");
            sb.append("]");
        } else {
            sb.append("\"type\": ").append("\"composite\",");
            sb.append(brdf.serialise());
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Save string to a file
     * @param filepath The filepath
     * @param json File content
     * @return boolean Returns true if the file was successfully saved.
     */
    public static boolean saveJsonToFile(String filepath, String json) {
        BufferedWriter writer = null;
        boolean success = false;
        try {
            File file = new File(filepath);

            System.out.println("Writing to " + file.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(json);
            success = true;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                writer.close();
                return success;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param resource   the resource to read
     * @param bufferSize the initial buffer size
     * @return the resource data
     * @throws IOException if an IO error occurs
     */
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1) {
                    ;
                }
            }
        } else {
            try (
                    InputStream source = IOUtil.class.getClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }
        }

        ((Buffer) buffer).flip();
        return buffer.slice();
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

}
