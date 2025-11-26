package org.gephi.viz.engine.jogl;

import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_BGRA;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL2GL3.GL_UNSIGNED_INT_8_8_8_8_REV;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.concurrent.CompletableFuture;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.jogl.util.gl.capabilities.Profile;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.gephi.viz.engine.util.TimeUtils;

/**
 *
 * @author Eduardo Ramos
 */
public class JOGLRenderingTarget implements RenderingTarget, GLEventListener, com.jogamp.newt.event.KeyListener,
    com.jogamp.newt.event.MouseListener {

    private final GLAutoDrawable drawable;

    //Animators
    private final AnimatorBase animator;
    private VizEngine<JOGLRenderingTarget, NEWTEvent> engine;

    //For displaying FPS in window title (optional)
    private String windowTitleFormat = null;
    private Frame frame;

    // States
    private boolean listenersSetup = false;
    private final float[] backgroundColor = new float[4];

    // FPS States
    private long lastFpsTime = 0;

    // Screenshot
    private CompletableFuture<BufferedImage> screenshotFuture = null;

    public JOGLRenderingTarget(GLAutoDrawable drawable) {
        this.drawable = drawable;
        this.animator = new FPSAnimator(drawable, VizEngine.DEFAULT_FPS, true);
        this.animator.setExclusiveContext(false);
        this.animator.setUpdateFPSFrames(VizEngine.DEFAULT_FPS, null);
    }

    @Override
    public void setup(VizEngine engine) {
        this.engine = engine;

        setupEventListeners();
    }

    public void doScreenshot(GL3ES3 gl) {
        CompletableFuture<BufferedImage> future = screenshotFuture;
        screenshotFuture = null; // Reset future to avoid multiple screenshots

        // Get Drawable Frame Size
        int height = drawable.getSurfaceHeight();
        int width = drawable.getSurfaceWidth();

        try {
            int[] frameData = frameDump(gl, width, height);

            BufferedImage screenshot =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            screenshot.setRGB(0, 0, width, height, frameData, 0, width);

            future.complete(screenshot);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }

    @Override
    public int[] frameDump(GL3ES3 gl, int width, int height) {
        // Create array to hold pixel data
        int[] pixelData = new int[width * height];

        // Wrap the array in an IntBuffer for OpenGL
        IntBuffer buffer = IntBuffer.wrap(pixelData);

        // Prepare Framebuffer capture
        gl.glReadBuffer(GL_BACK); // Some say GL_FRONT, some say GL_BACK
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

        return pixelData;
    }

    private synchronized void setupEventListeners() {
        if (listenersSetup) {
            return;
        }

        drawable.addGLEventListener(this);

        if (drawable instanceof GLWindow) {
            setup((GLWindow) drawable);
        } else if (drawable instanceof GLJPanel) {
            setup((GLJPanel) drawable);
        } else if (drawable instanceof GLCanvas) {
            setup((GLCanvas) drawable);
        } else {
            throw new RuntimeException("Drawable of type " + drawable.getClass() + " not supported");
        }

        listenersSetup = true;
    }

    private void setup(GLWindow gLWindow) {
        gLWindow.addKeyListener(this);
        gLWindow.addMouseListener(this);
    }

    private void setup(GLJPanel glJpanel) {
        new AWTKeyAdapter(this, glJpanel).addTo(glJpanel);
        new AWTMouseAdapter(this, glJpanel).addTo(glJpanel);
    }

    private void setup(GLCanvas glCanvas) {
        new AWTKeyAdapter(this, glCanvas).addTo(glCanvas);
        new AWTMouseAdapter(this, glCanvas).addTo(glCanvas);
    }

    public GLAutoDrawable getDrawable() {
        return drawable;
    }

    @Override
    public synchronized void init(GLAutoDrawable drawable) {
        final GL gl = drawable.getGL();

        engine.getOpenGLOptions().setGlCapabilitiesSummary(new GLCapabilitiesSummary(gl, Profile.CORE));

        gl.setSwapInterval(0);//Disable Vertical synchro

        gl.glDisable(GL.GL_DEPTH_TEST);//Z-order is set by the order of drawing

        //Disable blending for better performance
        gl.glDisable(GL.GL_BLEND);

        engine.initPipeline();

        lastFpsTime = TimeUtils.getTimeMillis();

        // Start animator
        animator.start();
    }

    @Override
    public synchronized void dispose(GLAutoDrawable drawable) {
        // Stop animator
        animator.stop();

        // Dispose pipeline
        engine.disposePipeline();
    }


    @Override
    public void display(GLAutoDrawable drawable) {
        final GL gl = drawable.getGL().getGL();
        if (screenshotFuture !=
            null) { // You can't call screenShort when you want as it's picking what's on the frame buffer
            // so you need to do this when you are sure the Framebuffer has been drawn so you got actual data.
            // Otherwise, it's during when buffer is empty and you have empty black image.
            doScreenshot((GL3ES3) gl);
        }
        engine.getBackgroundColor(backgroundColor);
        gl.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
        gl.glClear(GL_COLOR_BUFFER_BIT);

        updateFPS();
        engine.display();
    }

    @Override
    public synchronized void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        engine.reshape(width, height);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        engine.queueEvent(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        engine.queueEvent(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        engine.queueEvent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        engine.queueEvent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        engine.queueEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        engine.queueEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        engine.queueEvent(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        engine.queueEvent(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        engine.queueEvent(e);
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        engine.queueEvent(e);
    }

    public String getWindowTitleFormat() {
        return windowTitleFormat;
    }

    public void setWindowTitleFormat(String windowTitleFormat) {
        this.windowTitleFormat = windowTitleFormat;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public Frame getFrame() {
        return frame;
    }

    private void updateFPS() {
        if (animator != null && TimeUtils.getTimeMillis() - lastFpsTime > 1000) {
            if (frame != null && windowTitleFormat != null && windowTitleFormat.contains("$FPS")) {
                int measuredFps = (int) animator.getLastFPS();
                frame.setTitle(windowTitleFormat.replace("$FPS", String.valueOf(measuredFps)));
            }
            lastFpsTime += 1000;
        }
    }

    public int getFps() {
        return animator != null ? (int) animator.getLastFPS() : 0;
    }

    public CompletableFuture<BufferedImage> requestScreenshot() {
        this.screenshotFuture = new CompletableFuture<>();
        return this.screenshotFuture;
    }
}
