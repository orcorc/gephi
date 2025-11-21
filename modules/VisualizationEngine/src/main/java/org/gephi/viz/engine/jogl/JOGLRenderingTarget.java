package org.gephi.viz.engine.jogl;

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
import com.jogamp.opengl.util.GLBuffers;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.jogl.util.gl.capabilities.Profile;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.gephi.viz.engine.util.TimeUtils;

import java.awt.*;
import java.nio.IntBuffer;
import java.util.concurrent.CompletableFuture;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2GL3.GL_UNSIGNED_INT_8_8_8_8_REV;

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
    private boolean requestScreenshot = false;
    private CompletableFuture<int[]> screenshotFuture = null;

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
        // Get Drawable Frame Size
        int height = drawable.getSurfaceHeight();
        int width = drawable.getSurfaceWidth();

        int[] framedata = frameDump(gl, width, height);
        screenshotFuture.complete(framedata);
    }

    @Override
    public int[] frameDump(GL3ES3 gl, int width, int height) {


        // Size of the Frame buffer
        int size = width * height * 4;
        IntBuffer buffer = GLBuffers.newDirectIntBuffer(size);

        // Prepare Framebuffer capture
        gl.glReadBuffer(GL_BACK); // Some say GL_FRONT, some say GL_BACK
        gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
        gl.glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

        // Buffer back to 0
        buffer.rewind();

        // Dump buffer to Array
        int[] pixelInts = new int[size];
        buffer.get(pixelInts);

        // Flip vertically (OpenGL origin is bottom-left, BufferedImage is top-left)
        int[] flipped = new int[size];
        for (int y = 0; y < height; y++) {
            int srcPos = y * width;
            int dstPos = (height - 1 - y) * width;
            System.arraycopy(pixelInts, srcPos, flipped, dstPos, width);
        }

        return flipped;
        // Maybe we need a listener architecture to send frame (for recording video, aka multiple image /

        //


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
        if (requestScreenshot) { // You can't call screenShort when you want as it's picking what's on the frame buffer
            // so you need to do this when you are sure the Framebuffer has been drawn so you got actual data.
            // Otherwise, it's during when buffer is empty and you have empty black image.
            doScreenshot((GL3ES3) gl);
            requestScreenshot = false;
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

    public CompletableFuture<int[]> requestScreenshot() {
        this.requestScreenshot = true;
        this.screenshotFuture = new CompletableFuture<>();
        return this.screenshotFuture;
    }
}
