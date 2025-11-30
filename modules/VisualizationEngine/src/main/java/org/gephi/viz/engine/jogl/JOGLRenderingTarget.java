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
import com.jogamp.opengl.GLES3;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.TileRendererBase;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.concurrent.CompletableFuture;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.util.ScreenshotTaker;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.jogl.util.gl.capabilities.Profile;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.gephi.viz.engine.util.TimeUtils;

/**
 *
 * @author Eduardo Ramos
 */
public class JOGLRenderingTarget implements RenderingTarget, GLEventListener, com.jogamp.newt.event.KeyListener,
    com.jogamp.newt.event.MouseListener, TileRendererBase.TileRendererListener {

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
    private volatile ScreenshotRequest screenshotRequest;

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

        engine.getBackgroundColor(backgroundColor);
        gl.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
        gl.glClear(GL_COLOR_BUFFER_BIT);

        updateFPS();
        engine.display();

        ScreenshotRequest request = screenshotRequest;
        if (request != null && request.scaleFactor == 1) {
            // You can't call screenShort when you want as it's picking what's on the frame buffer
            // so you need to do this when you are sure the Framebuffer has been drawn so you got actual data.
            // Otherwise, it's during when buffer is empty and you have empty black image.
            CompletableFuture<BufferedImage> future = request.future;
            future.complete(ScreenshotTaker.takeSimpleScreenshot(drawable.getGL(), engine.getWidth(), engine.getHeight(), request.transparentBackground));

            float[] bgColor = engine.getBackgroundColor();
            bgColor[3] = 1f;
            engine.setBackgroundColor(bgColor);

            screenshotRequest = null; // Resets
        }
    }

    @Override
    public synchronized void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        engine.reshape(width, height);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (screenshotRequest != null) {
            return;
        }
        engine.queueEvent(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (screenshotRequest != null) {
            return;
        }
        engine.queueEvent(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (screenshotRequest != null) {
            return;
        }
        engine.queueEvent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (screenshotRequest != null) {
            return;
        }
        engine.queueEvent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (screenshotRequest != null) {
            return;
        }
        engine.queueEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (screenshotRequest != null) {
            return;
        }
        engine.queueEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (screenshotRequest != null) {
            return;
        }
        engine.queueEvent(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (screenshotRequest != null) {
            return;
        }
        engine.queueEvent(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (screenshotRequest != null) {
            return;
        }
        engine.queueEvent(e);
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        if (screenshotRequest != null) {
            return;
        }
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

    /**
     * Captures a high-resolution screenshot using tile-based rendering.
     * <p>
     * This method pauses the animator, renders the scene in tiles at the specified
     * resolution, and then restores normal rendering.
     *
     * @param scaleFactor The factor by which to scale the current drawable size for the screenshot, must be 2 or greater
     * @param transparentBackground Whether the screenshot should have a transparent background
     * @return BufferedImage containing the high-resolution screenshot
     */
    public CompletableFuture<BufferedImage> requestScreenshot(int scaleFactor, boolean transparentBackground) {
        if (scaleFactor < 1) {
            throw new IllegalArgumentException("Scale factor must be 1 or greater");
        }

        // Prepare screenshot request
        CompletableFuture<BufferedImage> future = new CompletableFuture<>();
        ScreenshotRequest request = new ScreenshotRequest(scaleFactor, transparentBackground, future);
        if (scaleFactor > 1) {
            // With scale factor > 1 we need to do tiled screenshot, and it can be done on the same thread
            try {
                // Pause animation and  update
                boolean wasAnimating = animator.isAnimating();
                if (wasAnimating) {
                    animator.pause();
                }
                engine.pauseUpdating();

                // Locks key and mouse events processing
                this.screenshotRequest = request;

                // Take tiled screenshot
                future.complete(ScreenshotTaker.takeTiledScreenshot(engine, scaleFactor, transparentBackground));

                // Resume update and animation
                engine.resumeUpdating();
                if (wasAnimating) {
                    animator.resume();
                }
            } finally {
                this.screenshotRequest = null;
            }
        } else {
            if (transparentBackground) {
                float[] bgColor = engine.getBackgroundColor();
                bgColor[3] = 0f;
                engine.setBackgroundColor(bgColor);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // Set request, to be completed in display()
            this.screenshotRequest = request;
        }

        return future;
    }

    @Override
    public void addTileRendererNotify(TileRendererBase tr) {

    }

    @Override
    public void removeTileRendererNotify(TileRendererBase tr) {

    }

    @Override
    public void reshapeTile(TileRendererBase tr, int tileX, int tileY, int tileWidth, int tileHeight, int imageWidth,
                            int imageHeight) {
        engine.centerOnTile(tileX, tileY, imageWidth, imageHeight);
    }

    @Override
    public void startTileRendering(TileRendererBase tr) {

    }

    @Override
    public void endTileRendering(TileRendererBase tr) {

    }

    public record ScreenshotRequest(
        int scaleFactor,
        boolean transparentBackground,
        CompletableFuture<BufferedImage> future
    ) {}
}
