
//Not clear on who the original author(s) of this code is/are
//I may have modified is slightly, but am not the original author
//I believe it to be public domain.  Please contact me if I should
//change the attributions or remove from the repository.
//Thanks, tvelliott

package jPCBSim;


// General Imports
import java.awt.*;
import java.io.*;

// JOGL Imports
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.FPSAnimator;
import static javax.media.opengl.GL.*;  // GL constants
import static javax.media.opengl.GL2.*; // GL2 constants
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.*;


/**
 * Renderer Class
 *
 * @version $Id: Renderer.java 813 2008-06-03 16:31:35Z mzaczek $
 * @since CTS 1.0
 */
public class Renderer implements GLEventListener
{

  /** GLU object. */
  private GLU glu = new GLU();

  private pcb_model pcbmodel;

  private boolean is_ortho=false;
  private boolean do_mesh=true;
  private boolean do_pml=false;
  private boolean do_airbox=true;

  /**
   * GLUquadric instance of a star sphere used for displaying a sphere textured
   * with stars to represent the star field.
   */
  private GLUquadric starSphere;

  private double zoom_ortho=1.0;

  /** Ambient light array. */
  private float lightAmbient[] = {0.38f, 0.38f, 0.38f, 0.0f};

  /** Diffuse light array. */
  private float lightDiffuse[] = {1.0f, 1.0f, 1.0f, 0.0f};

  /** Specular light array. */
  //private float lightSpecular[] = {0.15f, 0.15f, 0.15f, 0.0f};
  private float lightSpecular[] = {0.0f, 0.0f, 0.0f, 0.0f};

  /** Ambient/Diffuse array. */
  private float lightAmbientDiffuse[] = {0.65f, 0.65f, 0.65f, 0.0f};

  /** Position of the light. */
  private float lightPosition[] = {1.0f, 1.0f, 1.0f, 1.0f};
  private float lightPosition1[] = {1.0f, 1.0f, 1.0f, 1.0f};

  /** Near plane clipping distance. */
  public double nearPlane = 0.0001;
  //public double nearPlane = 1.0;

  /** Far plane clipping distance. */
  public double farPlane = 200000000.0;

  /** Zoom factor based on distance of eye to object. */
  public double zoomFactor = 0.01;

  /** Width. */
  private int _width;

  /** Height. */
  private int _height;

  private Simulation simulation;
  private PCBSimClient client;

  /** Reset flag that when true will ask the display method to perform a reshape. */
  public boolean reset = false;

  public Vector3d vEye = new Vector3d(1.0, 1.0, 1.0);

  public Vector3d vLook = new Vector3d(-0.05, -0.05, -0.05);

  public Vector3d vUp = new Vector3d(0.0, 1.0, 0.0);

  public Vector3d vRight = new Vector3d(1.0, 0.0, 0.0);

  public Point lastMousePoint = new Point();

  public Point currentMousePoint = new Point();

  public Camera camera = new Camera();

  /**
   * Renderer constructor that initializes the rotation strings and sets the
   * width/height.
   *
   * @param width The width.
   * @param height The height.
   */
  Renderer(int width, int height)
  {
    this._width = width;
    this._height = height;
    camera.setEye(new Vector3d(0, 0, 100));
    camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
    camera.rotateAroundLookVector(90.0);
    camera.moveBack(0.5);
  }

  public void setPCBModel(pcb_model model)
  {
    this.pcbmodel = model;
  }

  public void setSimulation(Simulation sim)
  {
    this.simulation = sim;
  }
  public void setClient(PCBSimClient client)
  {
    this.client = client;
  }

  public void meshApplySettings()
  {
    try {
      client.updateFromSimFields(simulation);
      simulation.writeConfig();
      pcbmodel.updateMesh(simulation);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void abortSimulation()
  {
    try {
      simulation.abort();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void runSimulation()
  {
    openEMSWriter writer = new openEMSWriter(simulation);
    try {
      this.is_ortho = false;
      this.do_mesh = true;
      this.do_pml = false;
      this.do_airbox = true;
      reset=true;

      client.updateFromSimFields(simulation);
      simulation.writeConfig();
      pcbmodel.updateMesh(simulation);
      writer.writeOpenEMSConfig(pcbmodel);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void readEMSConfig()
  {
    openEMSWriter writer = new openEMSWriter(simulation);
    try {
      writer.readOpenEMSConfig(pcbmodel);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  public void setMesh(boolean b)
  {
    this.do_mesh = b;
    reset=true;
  }
  public void setAirBox(boolean b)
  {
    this.do_airbox = b;
    reset=true;
  }
  public void setPML(boolean b)
  {
    this.do_pml = b;
    reset=true;
  }

  public void toggleOrtho()
  {
    this.is_ortho = !this.is_ortho;
    reset=true;
  }
  public void toggleMesh()
  {
    this.do_mesh = !this.do_mesh;
    reset=true;
  }
  public void togglePML()
  {
    this.do_pml = !this.do_pml;
    reset=true;
  }
  public void toggleAirBox()
  {
    this.do_airbox = !this.do_airbox;
    reset=true;
  }

  public boolean isOrtho()
  {
    return is_ortho;
  }

  public void zoomOrtho(double val)
  {
    if( val < 0.0 ) {
      if( zoom_ortho >= 0.05 ) zoom_ortho-=0.05;
    } else {
      zoom_ortho+=0.05;
    }
    camera.doUpdate();
    reset=true;
  }

  public double getZoomOrtho()
  {
    return zoom_ortho;
  }

  /**
   * The initialization function that configures the view, defines the lights,
   * loads the star and sun textures, and initializes the system and its
   * children.
   *
   * @param drawable A GLAutoDrawable instance.
   */
  public void init(GLAutoDrawable drawable)
  {
    final GL2 gl = drawable.getGL().getGL2();

    gl.setSwapInterval(1);

    //
    // View Settings
    //
    gl.glViewport(0, 0, 320, 240);
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();

    // Set the Field-of-View, Aspect Ratio, Near & Far clipping planes
    glu.gluPerspective(65.0, 320.0 / 240.0, nearPlane, farPlane);
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();
    //scale meters to display units
    gl.glTranslated(254.0,254.0,254.0);

    //
    // Lighting
    //
    // Enable Smooth Shading
    gl.glShadeModel(GL2.GL_SMOOTH);
    gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_SPECULAR, lightSpecular, 0);
    gl.glMaterialfv(gl.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, lightAmbientDiffuse, 0);
    // Background - black
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    // Depth Buffer Setup
    gl.glClearDepth(1.0f);
    // Clear The Stencil Buffer To 0
    gl.glClearStencil(0);
    // Enables Depth Testing
    gl.glEnable(GL2.GL_DEPTH_TEST);
    // Really Nice Perspective Calculations
    gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
    // Set The Ambient Lighting For Light0
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
    // Set The Diffuse Lighting For Light0
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
    // Set The Position For Light0
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);
    gl.glLightf(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, 1.0f);
    gl.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, 0.0f);
    gl.glLightf(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, 0.0f);

    // Set The Ambient Lighting For Light0
    gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
    // Set The Diffuse Lighting For Light0
    gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
    // Set The Position For Light0
    gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPosition, 0);
    gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightSpecular, 0);
    gl.glLightf(GL2.GL_LIGHT1, GL2.GL_CONSTANT_ATTENUATION, 1.0f);
    gl.glLightf(GL2.GL_LIGHT1, GL2.GL_LINEAR_ATTENUATION, 0.0f);
    gl.glLightf(GL2.GL_LIGHT1, GL2.GL_QUADRATIC_ATTENUATION, 0.0f);


    //gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_FALSE);
    //gl.glLightModelfv(gl.GL_LIGHT_MODEL_AMBIENT, lightAmbient, 0);
    gl.glLightModelfv(gl.GL_LIGHT_MODEL_TWO_SIDE, lightAmbient, 0);
    // Enable Lighting
    gl.glEnable(GL2.GL_LIGHTING);
    // Enable Light 0
    gl.glEnable(GL2.GL_LIGHT0);
    gl.glEnable(GL2.GL_LIGHT1);
    gl.glDisable(GL2.GL_CULL_FACE);
    gl.glEnable(GL2.GL_NORMALIZE);
    gl.glLightModelf(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);


    // Enable lighting on both side...especially needed for certain models
    // where the normal calculations would give the wrong face.
    // Note that this failed for some models being loaded...test again.
    //gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE,1);



  }

  /**
   * The primary display function that is called by JOGL to draw the defined
   * scene - it is called for every frame.  It first clears the background of
   * the previous scene.  Next the rotation parameters are corrected/updated.
   * The star background is then displayed, which is then followed by the
   * display of the planets and models attached to the system.  And finally,
   * the sun texture is drawn and pointed at the user.
   *
   * @param drawable A GLAutoDrawable instance.
   */
  public void display(GLAutoDrawable drawable)
  {
    GL2 gl = drawable.getGL().getGL2();

    if (reset) {

      if(!this.is_ortho) {
        int width = _width;
        int height = _height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        // Set the Field-of-View, Aspect Ratio, Near & Far clipping planes
        glu.gluPerspective(65.0, width / height, nearPlane, farPlane);
      } else {
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(pcbmodel.getZoomOrtho()*-1.2*pcbmodel.max_x,pcbmodel.getZoomOrtho()*1.2*pcbmodel.max_x, pcbmodel.getZoomOrtho()*-1.2*pcbmodel.max_x,
                   pcbmodel.getZoomOrtho()*1.2*pcbmodel.max_x,-1, 1);
      }


      gl.glMatrixMode(GL2.GL_MODELVIEW);
      gl.glLoadIdentity();
      reset = false;
    }

    // Clear Screen And Depth Buffer
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    gl.glLoadIdentity();

    updateView(gl);

    // Update light position
    // 0 in the last component causes jagged terminator on planets, 1 results
    // in a smooth terminator but the location of the sunlight is no longer
    // correct...not sure why.  Old code used a 0 (zero) very successfully,
    // what changed?!
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
    gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPosition1, 0);

    GLUT glut = new GLUT();

    gl.glEnable(GL2.GL_LIGHTING);
    gl.glEnable(GL2.GL_COLOR_MATERIAL);

    /*
        //draw the grid
        int x = 0;
        gl.glPushMatrix();
        gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glLineWidth(1.0f);
        gl.glColor3f(0.1f, 0.1f, 0.1f);
        double offset = -100.0;
        for(x=-4990;x<5000;x+=10) {
          gl.glBegin(gl.GL_LINES);
            gl.glVertex3d(x, offset, -5000);
            gl.glVertex3d(x, offset, 5000);

            gl.glVertex3d(-5000, offset, x);
            gl.glVertex3d(5000, offset, x);
          gl.glEnd();
        }
        gl.glPopAttrib();
        gl.glPopMatrix();
    */

    //draw the pcb model
    if(pcbmodel!=null) {
      pcbmodel.render(gl);
    }

    //draw the mesh model
    if(pcbmodel!=null) {
      pcbmodel.setOrtho(this.is_ortho, this.zoom_ortho);
      if(do_mesh) pcbmodel.renderMesh(gl, do_pml, do_airbox);
    }

    gl.glPushMatrix();
    gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
    gl.glEnable (GL_LINE_SMOOTH);
    gl.glEnable(GL2.GL_POLYGON_SMOOTH);
    gl.glEnable(GL2.GL_BLEND);
    gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
    gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
    gl.glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    //draw origin
    gl.glLineWidth(1.0f);
    gl.glBegin(gl.GL_LINES);
    gl.glColor4f(0.5f, 0.0f, 0.0f, 0.5f);
    gl.glVertex3d(-5000, 0, 0);
    gl.glVertex3d(5000, 0, 0);

    gl.glColor4f(0.0f, 0.5f, 0.0f, 0.5f);
    gl.glVertex3d(0, -0500, 0);
    gl.glVertex3d(0, 5000, 0);

    gl.glColor4f(0.0f, 0.0f, 0.5f, 0.5f);
    gl.glVertex3d(0, 0, -5000);
    gl.glVertex3d(0, 0, 5000);
    gl.glEnd();
    gl.glPopAttrib();
    gl.glPopMatrix();

    // Flush The GL Rendering Pipeline
    gl.glFlush();
  }


  /**
   * This method affects the display if the window is reshaped.
   *
   * @param drawable A GLAutoDrawable instance.
   * @param x The x coordinate of the top-left corner of the window.
   * @param y The y coordinate of the top-left corner of the window.
   * @param width The width of the window.
   * @param height The height of the window.
   */
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
  {
    GL2 gl = drawable.getGL().getGL2();

    // Prevent division by 0 in aspect ratio
    if (height <= 0) {
      height = 1;
    }

    this._width = width;
    this._height = height;

    if(!this.is_ortho) {
      gl.glViewport(0, 0, width, height);
      gl.glMatrixMode(GL2.GL_PROJECTION);
      gl.glLoadIdentity();
      // Set the Field-of-View, Aspect Ratio, Near & Far clipping planes
      glu.gluPerspective(65.0, width / height, nearPlane, farPlane);
    } else {
      gl.glMatrixMode(GL_PROJECTION);
      gl.glLoadIdentity();
      gl.glOrtho(pcbmodel.getZoomOrtho()*-1.2*pcbmodel.max_x,pcbmodel.getZoomOrtho()*1.2*pcbmodel.max_x, pcbmodel.getZoomOrtho()*-1.2*pcbmodel.max_x,
                 pcbmodel.getZoomOrtho()*1.2*pcbmodel.max_x,-1, 1);
    }
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();
  }

  /**
   * This method is not completed in the current JOGL specification (1.1.0)
   * but is is required because of the implementation specification.
   *
   * @param drawable A GLAutoDrawable instance.
   * @param modeChanged Flag for mode change.
   * @param deviceChanged Flag for device change (ie if window is moved to another
   *     screen, etc).
   */
  public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
  {
    // Nothing
  }

  private void updateView(GL2 gl)
  {
    gl.glMultMatrixd(camera.getView(), 0);
  }

  /**
   * Builds a view matrix.
   *
   * The final view matrix will take the form of:
   * <pre>
   *  |   rx    ry    rz   -(r.e)  |
   *  |   ux    uy    uz   -(u.e)  |
   *  |  -lx   -ly   -lz    (l.e)  |
   *  |    0     0     0       1   |
   *
   * Where r = Right vector
   *       u = Up vector
   *       l = Look vector
   *       e = Eye position in world space
   *       . = Dot-Product operation
   * </pre>
   *
   */
  private void updateViewMatrix(GL2 gl)
  {
    double view[] = {1.0, 0.0, 0.0, 0.0,
                     0.0, 1.0, 0.0, 0.0,
                     0.0, 0.0, 1.0, 0.0,
                     0.0, 0.0, 0.0, 1.0
                    };
    vLook.normalize();

    Vector3d.cross(vRight, vLook, vUp);
    vRight.normalize();

    Vector3d.cross(vUp, vRight, vLook);
    vUp.normalize();

    view[0] = vRight.x;
    view[1] = vUp.x;
    view[2] = -vLook.x;
    view[3] = 0.0;

    view[4] = vRight.y;
    view[5] = vUp.y;
    view[6] = -vLook.y;
    view[7] = 0.0;

    view[8] = vRight.z;
    view[9] = vUp.z;
    view[10] = -vLook.z;
    view[11] = 0.0;

    view[12] = -Vector3d.dot(vRight, vEye);
    view[13] = -Vector3d.dot(vUp, vEye);
    view[14] = Vector3d.dot(vLook, vEye);
    view[15] = 1.0;

//    System.out.println("  vEye: " + vEye);
//    System.out.println(" vLook: " + vLook);
//    System.out.println("   vUp: " + vUp);
//    System.out.println("vRight: " + vRight);

    gl.glMultMatrixd(view, 0);
  }

  public double[] matRotate(double mat[], double angle, Vector3d axis)
  {
    double s = Math.sin(angle * Math.PI / 180.0);
    double c = Math.cos(angle * Math.PI / 180.0);

    axis.normalize();

    mat[0] = c + (1 - c) * axis.x;
    mat[1] = (1 - c) * axis.x * axis.y + s * axis.z;
    mat[2] = (1 - c) * axis.x * axis.z - s * axis.y;
    mat[3] = 0.0;

    mat[4] = (1 - c) * axis.y * axis.x - s * axis.z;
    mat[5] = c + (1 - c) * Math.pow(axis.y, 2);
    mat[6] = (1 - c) * axis.y * axis.z + s * axis.x;
    mat[7] = 0.0;

    mat[8] = (1 - c) * axis.z * axis.x + s * axis.y;
    mat[9] = (1 - c) * axis.z * axis.z - s * axis.x;
    mat[10] = c + (1 - c) * Math.pow(axis.z, 2);
    mat[11] = 0.0;

    mat[12] = 0.0;
    mat[13] = 0.0;
    mat[14] = 0.0;
    mat[15] = 1.0;

    return mat;
  }

  public Vector3d transformVector(double mat[], Vector3d vec)
  {
    double x = vec.x;
    double y = vec.y;
    double z = vec.z;

    vec.x = x * mat[0] +
            y * mat[4] +
            z * mat[8];

    vec.y = x * mat[1] +
            y * mat[5] +
            z * mat[9];

    vec.z = x * mat[2] +
            y * mat[6] +
            z * mat[10];

    return vec;
  }

  /**
   * Source: http://local.wasp.uwa.edu.au/~pbourke/geometry/rotate/
   * s
   * @param angle
   */
  public void rotateLookVector( double angle )
  {
    double a = angle * Math.PI/180.0;
    Quat4f q1 = new Quat4f((float)vUp.x, (float)vUp.y, (float)vUp.z, 0.0f);
    Quat4f q2 = new Quat4f((float)(vLook.x * Math.sin(a/2.0)),
                           (float)(vLook.y * Math.sin(a/2.0)),
                           (float)(vLook.z * Math.sin(a/2.0)),
                           (float)(Math.cos(a/2)));
    Quat4f q3 = new Quat4f();
    q3 = Quat4f.mult(q2, Quat4f.mult(q1, Quat4f.conjugate(q2)));

//    System.out.println("--------------angle: " + a + " ---------------------");
//    System.out.println("   vUp: " + vUp);
//    System.out.println("    q3: " + q3);

    vUp.x = q3.x;
    vUp.y = q3.y;
    vUp.z = q3.z;
  }

  public void dispose(GLAutoDrawable arg0)
  {

  }
}
