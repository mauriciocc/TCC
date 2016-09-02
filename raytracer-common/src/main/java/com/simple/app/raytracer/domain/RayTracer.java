package com.simple.app.raytracer.domain;

import javafx.scene.image.PixelFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class RayTracer {

    private final String[] pattern = {
            "******* ****** *       *",
            "   *    *       *     * ",
            "   *    *        *   *  ",
            "   *    *         * *   ",
            "   *    *****      *    ",
            "   *    *         * *   ",
            "   *    *        *   *  ",
            "   *    *       *     * ",
            "****    *      *       *"
    };

    public byte[] render(int w, int h) {
        RenderConfig config = new RenderConfig();
        config.setLines(pattern);

        int cores = Runtime.getRuntime().availableProcessors();
        config.setThreads(cores);

        config.setBrightness(10);
        config.setCamDirection(new Vector3f(-2, -12, 0));
        config.setEvenColour(new Vector3f(3, 1, 1));
        config.setOddColour(new Vector3f(3, 3, 3));
        config.setRayOrigin(new Vector3f(16, 18, 8));
        config.setSkyColour(new Vector3f(.4f, .4f, 1f));
        config.setSphereReflectivity(0.5f);
        config.setRays(22);
        config.setImageWidth(w);
        config.setImageHeight(h);


        JFXRay ray = new JFXRay();
        ray.render(config);
        return ray.getImageData();

    }

    public byte[] renderToPng(int w, int h) {
        ByteBuffer wrap = ByteBuffer.wrap(render(w, h));
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                image.setRGB(x, y, pixelFormat.getArgb(wrap, x, y, w * 3));
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }
}
