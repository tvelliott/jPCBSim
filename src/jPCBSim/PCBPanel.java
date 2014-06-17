
//Not clear on who the original author(s) of this code is/are
//I may have modified is slightly, but am not the original author
//I believe it to be public domain.  Please contact me if I should
//change the attributions or remove from the repository.
//Thanks, tvelliott

package jPCBSim;

/*
 * This code is a First Person Shooter (FPS) control code that allows the
 * user to move in a 3D world using the forward/back/left/right keys and the
 * mouse.  Like FPS games, the person can basically fly through the scene by
 * moving the mouse in the direction they wish to travel and using the
 * keyboard keys for actual motion.
 *
 * This part of the code was inspired by the following OpenGL codes:
 *   http://www.codesampler.com/oglsrc/oglsrc_5.htm#ogl_fps_controls
 *   http://www.geocities.com/fragtheplanet/OpenGL/Camera.html
 *
 * The first code is a true FPS code while the second inspired me to add the
 * ability to roll the view around the look vector.  The second code also has
 * that capability but once the look is rolled the subsequent mouse motions
 * are not about the new axis system but still about the old so the motion
 * get confusing.  I improved on this through my code and have provided this
 * project which allows the user to try both control modes by toggling them
 * with the 'F' key.
 *
 * In order to provide the roll capability I need to calculate the rotation
 * of the up vector about the look vector and I used the following equations,
 * using quaternions, to do these calculations:
 *   http://local.wasp.uwa.edu.au/~pbourke/geometry/rotate/
 *
 *   CONTROLS
 *   ========
 *     Keyboard F - toggle between FPS and Modified FPS modes
 *
 *     Keyboard UP - moves the camera into the screen
 *     Keyboard DOWN - moves the camera back
 *     Keyboard LEFT - moves the camera to the left (teapot goes to the right)
 *     Keyboard RIGHT - moves the camera to the right (teapot to left)
 *     Keyboard PAGE_UP - moves the camera up along global Y axis
 *                        (teapot goes down) (not "current" y axis of the view)
 *     Keyboard PAGE_DOWN - moves the camera down along global Y axis
 *
 *     Keyboard COMMA - roll left  (valid only in the modified FPS view)
 *     Keyboard PERIOD - roll right  (valid only in the modified FPS view)
 *
 *     Mouse Controls - Click and Hold left mouse button to orient the
 *                      view vector.
 *
 *     Combination of holding down mouse button and keyboard up/down/left/right
 *                      keys makes you fly through the scene.
 *
 *     If you rotate the view in the FPS mode then the left-right mouse motion
 *     causes a rotation about the fixed vertical axis of the original scene.
 *
 *     If you rotate the view in the Modified FPS mode then the left-right
 *     mouse motion causes a rotation about the current UP vector of the view.
 *     ...this lets you basically fly through the scene and rotate in all
 *     different directions...it is awesome (I think)
 *
 *
 * The code has been update to be able to hold down two keys to allow for
 * better and smoother motion.  The following Java code was used to help
 * implement this capability into the current code.
 *   http://forum.java.sun.com/thread.jspa?threadID=5149852&messageID=9563056
 *
 */


// Java Swing imports
import javax.swing.*;

// Java Event imports
import java.awt.*;
import java.awt.event.*;
import java.text.*;

// JOGL imports
import javax.media.opengl.*;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import static javax.media.opengl.GL.*;  // GL constants
import static javax.media.opengl.GL2.*; // GL2 constants
import com.jogamp.opengl.util.*;



/**
 * The main class which is used to create the JPanel.
 *
 */
public class PCBPanel extends JPanel
{

  /** GLJPanel where the displayed is drawn */
  private GLJPanel canvas;

  private int fps_rate = 30;

  /** Renderer used to handle the drawing */
  private Renderer renderer;

  /** Font to use for displaying the info */
  private Font infoFont = new Font("MonoSpaced", Font.BOLD, 14);

  /** Default panel width */
  private static final int DEFAULT_WIDTH = 640;

  /** Default panel height */
  private static final int DEFAULT_HEIGHT = 480;

  /** Current panel width */
  private int width;

  /** Current panel height */
  private int height;


  private KeyHandler inputKeyHandler;

  /** Decimal format used to output the frames-per-second result */
  private DecimalFormat format = new DecimalFormat("#######0.00000");

  /** An animator...oh yeah */
  private FPSAnimator animator;

  boolean display_view_matrix=false;

  public static final boolean ANIMATOR_OFF = false;
  public static final boolean ANIMATOR_ON = true;

  /** Flag that determines if animator is used */
  protected boolean useAnimator = ANIMATOR_ON;

  static final String ZEROES = "000000000000";
  static final String BLANKS = "            ";

  public void toggle_display_viewmatrix()
  {
    display_view_matrix = !display_view_matrix;
  }

  static String format( double val, int n, int w)
  {
    //  rounding
    double incr = 0.5;
    for( int j=n; j>0; j--) incr /= 10;
    val += incr;

    if (Math.abs(val) < 1E-5) {
      val = 0.0;
    }
    String s = Double.toString(val);
    int n1 = s.indexOf('.');
    int n2 = s.length() - n1 - 1;

    if (n>n2)      s = s+ZEROES.substring(0, n-n2);
    else if (n2>n) s = s.substring(0,n1+n+1);

    if( w>0 & w>s.length() ) s = BLANKS.substring(0,w-s.length()) + s;
    else if ( w<0 & (-w)>s.length() ) {
      w=-w;
      s = s + BLANKS.substring(0,w-s.length()) ;
    }
    return s;
  }

  /**
   * Default Constructor that creates a new instace of Prox3D
   *
   */
  public PCBPanel()
  {
    this(true);
  }

  /**
   * Constructor that creates a new instace of Prox3D with the desired
   * width and height.  This Constructor instantiates all of the handler
   * functions (Mouse and Key) and enables the overlay to be drawn and
   * finally invokes the renderer and animator and adds the 3D panel to
   * the current JPanel.
   *
   * @param width Desired width of the panel
   * @param height Desired width of the panel
   */
  public PCBPanel( boolean useAnimator )
  {
    this.useAnimator = useAnimator;


    width = DEFAULT_WIDTH;
    height = DEFAULT_HEIGHT;
    renderer = new Renderer(width, height);

    MouseHandler inputMouseHandler = new MouseHandler(renderer, this);
    addMouseListener(inputMouseHandler);
    addMouseMotionListener(inputMouseHandler);

    inputKeyHandler = new KeyHandler(renderer, this);
    setFocusable(true);
    addKeyListener(inputKeyHandler);

    setLayout(new BorderLayout());

    GLProfile glprofile = GLProfile.getDefault();
    GLCapabilities caps = new GLCapabilities(glprofile);
    caps.setAlphaBits(8);
    caps.setSampleBuffers(true);
    caps.setNumSamples(8);


    canvas = new GLJPanel(caps) {

      public void paintComponent( Graphics g ) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.setFont(infoFont);

        String tmpString = "";

        if(display_view_matrix) {
          g.setColor(Color.WHITE);
          tmpString = "   eye point: " + format.format(renderer.camera.getEye().x) + ",  " + format.format(renderer.camera.getEye().y) + ",  " + format.format(renderer.camera.getEye().z);
          g.drawString(tmpString, 25, getHeight() - 55);

          tmpString = " look vector: " + format.format(renderer.camera.getLook().x) + ",  " + format.format(renderer.camera.getLook().y) + ",  " + format.format(renderer.camera.getLook().z);
          g.drawString(tmpString, 25, getHeight() - 40);

          tmpString = "   up vector: " + format.format(renderer.camera.getUp().x) + ",  " + format.format(renderer.camera.getUp().y) + ",  " + format.format(renderer.camera.getUp().z);
          g.drawString(tmpString, 25, getHeight() - 25);

          tmpString = "right vector: " + format.format(renderer.camera.getRightCopy().x) + ",  " + format.format(renderer.camera.getRightCopy().y) + ",  " + format.format(renderer.camera.getRightCopy().z);
          g.drawString(tmpString, 25, getHeight() - 10);

          g.setColor(Color.WHITE);

          String proj_mode="";
          if(renderer.isOrtho()) proj_mode = "Orthographic";
          else proj_mode =  "Perspective";
          g.drawString( "Projection Mode: "+proj_mode, 20, 30);

          double v[] = renderer.camera.getViewCopy();
          g.drawString( "View Matrix: ",  20, 60);
          g.drawString( format(v[0], 6, 10) + ", " + format(v[1], 6, 10) + ", " + format(v[2], 6, 10) + ", " + format(v[3], 6, 10),  20, 75);
          g.drawString( format(v[4], 6, 10) + ", " + format(v[5], 6, 10) + ", " + format(v[6], 6, 10) + ", " + format(v[7], 6, 10),  20, 90);
          g.drawString( format(v[8], 6, 10) + ", " + format(v[9], 6, 10) + ", " + format(v[10], 6, 10)+ ", " + format(v[11], 6, 10), 20, 105);
          g.drawString( format(v[12], 6, 10)+ ", " + format(v[13], 6, 10)+ ", " + format(v[14], 6, 10)+ ", " + format(v[15], 6, 10), 20, 120);

        }

      }
    };
    canvas.setOpaque(false);

    canvas.addGLEventListener(renderer);

    canvas.setIgnoreRepaint(true);

    add(canvas);
    setPreferredSize(new Dimension(width, height));


    if (useAnimator) {
      animator = new FPSAnimator(canvas, fps_rate, true);
      start();
    }
  }

  public boolean isControlDown()
  {
    return inputKeyHandler.isControlDown();
  }

  public Renderer getRenderer()
  {
    return this.renderer;
  }

  public void start()
  {
    try {
      canvas.requestFocus();
      animator.start();
    } catch (Exception e) {
      System.out.println("Caught Exception: " + e);
    }
  }

  public void stop()
  {
    try {
      animator.stop();
    } catch (Exception e) {
      System.out.println("Caught Exception: " + e);
    } finally {
      System.exit(0);
    }
  }

  /**
   * Calls the drawable to display the scene and is typically invoked following
   * a scene change either because of a mouse event or a keyboard event.  Other
   * scene changes or requests should also calls this method so that updates
   * are reflected in the scene.
   *
   */
  public void drawabledisplay()
  {
    try {
      renderer.camera.apply();
      canvas.display();
    } catch (Exception e) {
      System.out.println("drawabledisplay(): Exception caught: " + e);
    }
  }

}
