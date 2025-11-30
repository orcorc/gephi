package org.gephi.viz.engine.jogl.util;

import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_BGRA;
import static com.jogamp.opengl.GL2GL3.GL_UNSIGNED_INT_8_8_8_8_REV;

import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLES3;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.TileRenderer;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.TextureData;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import com.jogamp.nativewindow.util.PixelFormat;
import com.jogamp.opengl.util.GLPixelBuffer;
import com.jogamp.opengl.util.GLPixelBuffer.GLPixelAttributes;
import org.joml.Vector2fc;


public class ScreenshotTaker {

    public static BufferedImage takeSimpleScreenshot(GL gl, int width, int height, boolean transparentBackground) {
        // Create array to hold pixel data
        int[] pixelData = new int[width * height];

        // Wrap the array in an IntBuffer for OpenGL
        IntBuffer buffer = IntBuffer.wrap(pixelData);

        // Prepare Framebuffer capture
        gl.getGL3ES3().glReadBuffer(GL_BACK); // Some say GL_FRONT, some say GL_BACK
        gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
        gl.glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

        // Flip vertically in-place (OpenGL origin is bottom-left, BufferedImage is top-left)
        for (int y = 0; y < height / 2; y++) {
            int topRowStart = y * width;
            int bottomRowStart = (height - 1 - y) * width;

            // Swap rows
            for (int x = 0; x < width; x++) {
                int temp = pixelData[topRowStart + x];
                pixelData[topRowStart + x] = pixelData[bottomRowStart + x];
                pixelData[bottomRowStart + x] = temp;
            }
        }

        BufferedImage screenshot =
            new BufferedImage(width, height, transparentBackground ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        screenshot.setRGB(0, 0, width, height, pixelData, 0, width);
        return screenshot;
    }

    public static BufferedImage takeTiledScreenshot(VizEngine<JOGLRenderingTarget, NEWTEvent> engine, int scaleFactor, boolean transparentBackground) {

        float originalZoom = engine.getZoom();
        Vector2fc originalPan = engine.getRenderingOptions().getPan();

        GLAutoDrawable drawable = engine.getRenderingTarget().getDrawable();
        int tileWidth = drawable.getSurfaceWidth();
        int tileHeight = drawable.getSurfaceHeight();

        int imageWidth = tileWidth * scaleFactor;
        int imageHeight = tileHeight * scaleFactor;

        TileRenderer renderer = new TileRenderer();
        renderer.setImageSize(imageWidth, imageHeight);
        renderer.setTileSize(tileWidth, tileHeight, 0);
        renderer.attachAutoDrawable(drawable);

        final GLPixelBuffer.GLPixelBufferProvider pixelBufferProvider = GLPixelBuffer.defaultProviderWithRowStride;
        final boolean[] flipVertically = {false};
        final GLEventListener preTileGLEL = new GLEventListener() {
            @Override
            public void init(final GLAutoDrawable drawable) {
                final GL gl = drawable.getGL();
                final PixelFormat.Composition hostPixelComp =
                    pixelBufferProvider.getHostPixelComp(gl.getGLProfile(), transparentBackground ? 4 : 3);
                final GLPixelAttributes pixelAttribs = pixelBufferProvider.getAttributes(gl, transparentBackground ? 4 : 3, true);
                final GLPixelBuffer pixelBuffer =
                    pixelBufferProvider.allocate(gl, hostPixelComp, pixelAttribs, true, imageWidth, imageHeight, 1, 0);
                renderer.setImageBuffer(pixelBuffer);
                flipVertically[0] = !drawable.isGLOriented();
            }

            @Override
            public void dispose(final GLAutoDrawable drawable) {
            }

            @Override
            public void display(final GLAutoDrawable drawable) {
            }

            @Override
            public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width,
                                final int height) {
            }
        };
        renderer.setGLEventListener(preTileGLEL, null);

        float[] backgroundColor = engine.getBackgroundColor();
        if (transparentBackground) {
            backgroundColor[3] = 0f;
            engine.setBackgroundColor(backgroundColor);
        }

        while (!renderer.eot()) {
            renderer.display();
            engine.setZoom(originalZoom);
            engine.setTranslate(originalPan);
        }
        renderer.detachAutoDrawable();

        // Restore original view and background
        engine.setZoom(originalZoom);
        engine.setTranslate(originalPan);
        if (transparentBackground) {
            backgroundColor[3] = 1f;
            engine.setBackgroundColor(backgroundColor);
        }

        final GLPixelBuffer imageBuffer = renderer.getImageBuffer();

        final TextureData textureData = new TextureData(
            drawable.getChosenGLCapabilities().getGLProfile(),
            transparentBackground ? GL.GL_RGBA : GL.GL_RGB,
            imageWidth, imageHeight,
            0,
            imageBuffer.pixelAttributes,
            false, false,
            flipVertically[0],
            imageBuffer.buffer,
            null /* Flusher */);

        // Destroy?

        return toImage(textureData);
    }

    private static BufferedImage toImage(TextureData data) {
        final int pixelFormat = data.getPixelFormat();
        final int pixelType   = data.getPixelType();
        if ((pixelFormat == GL.GL_RGB ||
            pixelFormat == GL.GL_RGBA) &&
            (pixelType == GL.GL_BYTE ||
                pixelType == GL.GL_UNSIGNED_BYTE)) {
            BufferedImage image = new BufferedImage(data.getWidth(), data.getHeight(),
                (pixelFormat == GL.GL_RGB) ?
                    BufferedImage.TYPE_3BYTE_BGR :
                    BufferedImage.TYPE_4BYTE_ABGR);
            final byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            ByteBuffer buf = (ByteBuffer) data.getBuffer();
            if (buf == null) {
                buf = (ByteBuffer) data.getMipmapData()[0];
            }
            buf.rewind();
            buf.get(imageData);
            buf.rewind();

            // Swizzle image components to be correct
            if (pixelFormat == GL.GL_RGB) {
                for (int i = 0; i < imageData.length; i += 3) {
                    final byte red = imageData[i];
                    final byte blue = imageData[i + 2];
                    imageData[i] = blue;
                    imageData[i + 2] = red;
                }
            } else {
                for (int i = 0; i < imageData.length; i += 4) {
                    final byte red = imageData[i];
                    final byte green = imageData[i + 1];
                    final byte blue = imageData[i + 2];
                    final byte alpha = imageData[i + 3];
                    imageData[i] = alpha;
                    imageData[i + 1] = blue;
                    imageData[i + 2] = green;
                    imageData[i + 3] = red;
                }
            }

            // Flip image vertically for the user's convenience
            ImageUtil.flipImageVertically(image);

            return image;
        } else {
            throw new IllegalArgumentException("Unsupported pixel format/type: " +
                pixelFormat + "/" + pixelType);
        }
    }
}