//Not clear on who the original author(s) of this code is/are
//I may have modified is slightly, but am not the original author
//I believe it to be public domain.  Please contact me if I should
//change the attributions or remove from the repository.
//Thanks, tvelliott

package jPCBSim;


/**
 * This code is a First Person Shooter (FPS) camera control code that allows
 * the user to move in a 3D world using the forward/back/left/right keys and
 * the mouse.  Like FPS games, the person can basically fly through the scene
 * by moving the mouse in the direction they wish to travel and using the
 * keyboard keys for actual motion.
 *
 * This part of the code was inspired by the following OpenGL codes:
 *   http://www.codesampler.com/oglsrc/oglsrc_5.htm#ogl_fps_controls
 *   http://www.geocities.com/fragtheplanet/OpenGL/Camera.html
 *
 * @version $Id$
 * @since CTS 1.0
 */
public class Camera
{
  boolean _debug=false;

  /** The look vector/direction. */
  private Vector3d look  = new Vector3d(-0.05, -0.05, -0.05);

  /** The coordinates of the camera eye position. */
  private Vector3d eye   = new Vector3d(0, 0, 0);

  /** The up pointing vector at the eye point. */
  private Vector3d up    = new Vector3d(0, 1, 0);

  /** The right pointing vector. */
  private Vector3d right = new Vector3d(1, 0, 0);

  /** View matrix for the Camera. */
  private double view[] = { 1.0, 0.0, 0.0, 0.0,
                            0.0, 1.0, 0.0, 0.0,
                            0.0, 0.0, 1.0, 0.0,
                            0.0, 0.0, 0.0, 0.0
                          };

  /** Axes matrix for the Camera. */
  private double axes[] = { 1.0, 0.0, 0.0, 0.0,
                            0.0, 1.0, 0.0, 0.0,
                            0.0, 0.0, 1.0, 0.0,
                            0.0, 0.0, 0.0, 1.0
                          };

  /** Move increment/step size. */
  private double moveSpeed = 0.001;

  /**
   * Flag that tracks whether an update calculation is necessary when
   * camera parameters have changed.
   */
  private boolean needUpdate = false;

  /**
   * Flag that tracks if the rotations should be performed around an
   * orbit point that the user can set.
   */
  private boolean orbit = true;

  /** A point in space around which the viewer can orbit. */
  private Vector3d orbitPoint = new Vector3d(0, 0, 0);

  /** Zoom distance around orbiting point. */
  private double orbitDistance = 1.0;

  /** Pseudo orbit eye position used internally for orbiting purposes. */
  private Vector3d orbitEye = new Vector3d();


  /**
   * Camera constructor that uses the default camera settings.
   *
   */
  public Camera()
  {
    needUpdate = true;
    apply();
  }

  public void doUpdate()
  {
    needUpdate=true;
  }

  /**
   * Camera constructor that takes the look and up vectors and the eye
   * position.
   *
   * @param lookVector The look/direction vector from the eye to the focus
   *                   point.
   * @param eyePoint The eye (or camera) position in 3D space.
   * @param upVector The up vector of the camera.
   */
  public Camera( Vector3d lookVector, Vector3d eyePoint, Vector3d upVector )
  {
    setLook(lookVector);
    setEye(eyePoint);
    setUp(upVector);

    apply();
  }

  /**
   * Sets the look vector for the camera.  This vector corresponds to the
   * direction for looking at the point which is to be the focus of the scene.
   * A call to the apply() method is required to properly recalculate the
   * camera system.
   *
   * @param vec The look vector.
   */
  public void setLook( Vector3d vec )
  {
    // Make sure the look vector is not null
    if (vec == null) {
      throw new RuntimeException("Look vector cannot be null.");
    }

    // Make sure the look vector has some length
    if (Math.abs(vec.length()) < 1.0e-6) {
      throw new RuntimeException("Look vector cannot have zero length.");
    }

    look.x = vec.x;
    look.y = vec.y;
    look.z = vec.z;

    // Normalize the vector
    look.normalize();

    needUpdate = true;
  }

  /**
   * Sets the eye position for the camera.  This vector corresponds to the
   * coordinates of the eye point (or camera point).  A call to the
   * apply() method is required to properly recalculate the camera system.
   *
   * @param vec The eye position.
   */
  public void setEye( Vector3d pt )
  {
    // Make sure eye point is not null
    if (pt == null) {
      throw new RuntimeException("Eye point cannot be null.");
    }

    eye.x = pt.x;
    eye.y = pt.y;
    eye.z = pt.z;

    needUpdate = true;
  }

  /**
   * Sets the up vector for the camera.  This vector corresponds to the
   * vector that points along the local up direction from the camera.
   * A call to the apply() method is required to properly recalculate the
   * camera system.
   *
   * @param vec The up vector.
   */
  public void setUp( Vector3d vec )
  {
    // Make sure the up vector is not null
    if (vec == null) {
      throw new RuntimeException("Up vector cannot be null.");
    }

    // Make sure the up vector has some length
    if (Math.abs(vec.length()) < 1.0e-6) {
      throw new RuntimeException("Up vector cannot have zero length.");
    }

    up.x = vec.x;
    up.y = vec.y;
    up.z = vec.z;

    // Normalize the vector
    up.normalize();

    needUpdate = true;
  }

  /**
   * Returns a copy of the look vector.
   *
   * @return The look vector copy.
   */
  public Vector3d getLookCopy()
  {
    return new Vector3d(look);
  }

  /**
   * Returns the instance of the look vector for this camera - not simply
   * a copy.
   *
   * @return The look vector from this camera.
   */
  public Vector3d getLook()
  {
    return look;
  }

  /**
   * Returns a copy of the eye vector.
   *
   * @return The eye vector copy.
   */
  public Vector3d getEyeCopy()
  {
    return new Vector3d(eye);
  }

  /**
   * Returns the instance of the eye vector for this camera - not simply
   * a copy.
   *
   * @return The eye vector from this camera.
   */
  public Vector3d getEye()
  {
    return eye;
  }

  /**
   * Returns a copy of the up vector.
   *
   * @return The up vector copy.
   */
  public Vector3d getUpCopy()
  {
    return new Vector3d(up);
  }

  /**
   * Returns the instance of the up vector for this camera - not simply
   * a copy.
   *
   * @return The up vector from this camera.
   */
  public Vector3d getUp()
  {
    return up;
  }

  /**
   * Returns a copy of the right vector.
   *
   * @return The right vector copy.
   */
  public Vector3d getRightCopy()
  {
    return new Vector3d(right);
  }

  /**
   * This method accepts a look point that corresponds to the point that is
   * to be focused on, and an up vector.  The look point, in combination with
   * the already defined eye point, are used to calculate the new look vector.
   * The up vector is then used, with the newly calculated look vector, to
   * update the view matrix. A call to the apply() method is required to
   * properly recalculate the camera system.
   *
   * @param lookPoint The 3D coordinates of the point that is being focused on.
   * @param upVector The 3D vector defining the up direction.
   */
  public void lookAt( Vector3d lookPoint, Vector3d upVector )
  {
    // Make sure the look point and up vectors are not null
    if (lookPoint == null) {
      throw new RuntimeException("Look point cannot be null.");
    }

    if (upVector == null) {
      throw new RuntimeException("Up vector cannot be null.");
    }

    // Make sure the up vector has some length
    if (Math.abs(upVector.length()) < 1.0e-6) {
      throw new RuntimeException("Up vector cannot have zero length.");
    }

    // Calculate the look vector/direction by vector subtraction of the
    // the look at point and the current eye point
    Vector3d lookVector = new Vector3d();
    lookVector.sub(lookPoint, eye);

    if (_debug) {
      System.out.println("---- lookAt() ------------------------------------");
      System.out.println("Look Vector = look point - eye point "
                         + "\n   look point: " + lookPoint
                         + "\n    eye point: " + eye
                         + "\n  look vector: " + lookVector);
    }

    // Ensure the new look vector is not zero length, and if so
    // Make sure the new look vector has some length
    if (Math.abs(lookVector.length()) < 1.0e-6) {
      // Force the vector to some default direction and display a warning
      System.err.println("Camera look vector cannot have zero length.  "
                         + "This is likely caused by coincidant 'look at' "
                         + "and 'eye' point locations.  Forcing look vector "
                         + "to <1,0,0>");
      lookVector.x = 1.0;
      lookVector.y = 0.0;
      lookVector.z = 0.0;
    }

    // Update the look vector with the new look vector (nominally, the vector
    // would be normalized, but this occurs inside the code already.
    setLook(lookVector);

    // Update the up vector
    setUp(upVector);

    needUpdate = true;
  }

  /**
   * Applies the changes made in the camera settings by calling the method
   * to update the view calculations.
   *
   * This method will only call the update method if necessary.  If
   * necessary, the update can be forced by directly calling the updateView()
   * method.
   */
  public void apply()
  {
    if (!needUpdate) {
      return;
    }

    updateView();

    // If 'orbiting' then offset the rotation point based on the 'orbitPoint'
    double pre[] = { 1.0, 0.0, 0.0, 0.0,
                     0.0, 1.0, 0.0, 0.0,
                     0.0, 0.0, 1.0, 0.0,
                     orbitPoint.x, orbitPoint.y, orbitPoint.z, 1.0
                   };
    double R1[] = matmult(pre, view);
    // Update the [view] matrix
    for (int i=0; i<16; i++) {
      view[i] = R1[i];
    }

    // Multiply the view matrix by any other matrix to alter the matrix axes
    double R[] = matmult(axes, view);
    // Update the [view] matrix
    for (int i=0; i<16; i++) {
      view[i] = R[i];
    }

    // Store the current view matrix position for orbiting purposes
    orbitEye.x = view[12];
    orbitEye.y = view[13];
    orbitEye.z = view[14];

    // If 'orbiting' then offset back the rotation point based
    // on the 'orbitPoint'
    double post[] = { 1.0, 0.0, 0.0, 0.0,
                      0.0, 1.0, 0.0, 0.0,
                      0.0, 0.0, 1.0, 0.0,
                      -orbitPoint.x, -orbitPoint.y, -orbitPoint.z, 1.0
                    };

    double R2[] = matmult(post, view);
    // Update the [view] matrix
    for (int i=0; i<16; i++) {
      view[i] = R2[i];
    }
  }

  /**
   * Method to update the multiplication axes by which to alter the view
   * matrix.
   *
   * @param a The axes matrix.
   */
  public void updateAxes( double a[] )
  {
    for (int i=0; i<16; i++) {
      axes[i] = a[i];
    }
  }

  /**
   * Clear/Reset the axes matrix to an identity matrix effectively removing
   * any rotations from the system.
   *
   */
  public void resetAxes()
  {
    setIdentity(axes);

    needUpdate = true;
  }

  /**
   * Internal method that set a passed in 16 element matrix to the identity
   * matrix.
   *
   * @param mat The matrix to reset.
   */
  private void setIdentity( double mat[] )
  {
    for (int i=0; i<16; i++) {
      mat[i] = 0.0;
    }

    mat[0] = mat[5] = mat[10] = mat[15] = 1.0;
  }

  /**
   * Update camera setttings based on any changes made to the look, eye or
   * up vectors and build a view matrix suitable for OpenGL.
   *
   * Here's what the final view matrix should look like:
   *
   *  |  rx   ry   rz  -(r.e) |
   *  |  ux   uy   uz  -(u.e) |
   *  | -lx  -ly  -lz   (l.e) |
   *  |   0    0    0     1   |
   *
   * Where r = Right vector
   *       u = Up vector
   *       l = Look vector
   *       e = Eye position in world space
   *       . = Dot-product operation
   *
   */
  public void updateView()
  {
    // Normalize the look vector. setLook() method does this already but
    // we can't assume that the reference wasn't changed so this is likely
    // precautionary and redundant....but can't take chances.
    // Make sure the look vector has some length
    if (Math.abs(look.length()) < 1.0e-6) {
      throw new RuntimeException("Look vector cannot have zero length.");
    }
    look.normalize();

    // Cross product of the 'look' and 'up' vectors gets the 'right' vector.
    Vector3d.cross(right, look, up);

    // Make sure the right vector has some length
    if (Math.abs(right.length()) < 1.0e-6) {
      throw new RuntimeException("Right vector cannot have zero length.");
    }
    right.normalize();

    // Cross product of the 'right' and 'look' vectors gets the 'up' vector.
    Vector3d.cross(up, right, look);

    // Make sure the up vector has some length
    if (Math.abs(up.length()) < 1.0e-6) {
      throw new RuntimeException("Up vector cannot have zero length.");
    }
    up.normalize();

    // Place the results into a matrix format for use with the OpenGL call
    // to glMultMatrixd().
    view[0] = right.x;
    view[1] = up.x;
    view[2] = -look.x;
    view[3] = 0.0;

    view[4] = right.y;
    view[5] = up.y;
    view[6] = -look.y;
    view[7] = 0.0;

    view[8] = right.z;
    view[9] = up.z;
    view[10] = -look.z;
    view[11] = 0.0;

    view[12] = orbitEye.x;
    view[13] = orbitEye.y;
    view[14] = orbitEye.z;
    view[15] = 1.0;

    // multiply the view matrix by the orbit point translation.
    double matrotation[] = { 1.0, 0.0, 0.0, 0.0,
                             0.0, 1.0, 0.0, 0.0,
                             0.0, 0.0, 1.0, 0.0,
                             -orbitPoint.x, -orbitPoint.y, -orbitPoint.z, 1.0
                           };
    double R[] = matmult(matrotation, view);
    // Update the [view] matrix
    for (int i=0; i<16; i++) {
      view[i] = R[i];
    }

    if (_debug) {
      System.out.println("---- updateView() --------------------------------");
      System.out.println("   look: " + look
                         + "\n    eye: " + eye
                         + "\n     up: " + up
                         + "\n  right: " + right);

      System.out.println("   view matrix:");
      for (int i=0; i<4; i++) {
        for (int j=0; j<4; j++) {
          System.out.print("  " + view[4*i+j]);
        }
        System.out.println("");
      }
    }

    needUpdate = false;
  }

  /**
   * Returns a copy of the view matrix 16 elements) of this camera.  The view
   * matrix is used in JOGL (OpenGL) by calling: gl.glMultMatrixd(view, 0)
   *
   * @return Array of matrix values.
   */
  public double[] getViewCopy()
  {
    // First do a check if an update is needed
    checkUpdate();

    double viewCopy[] = new double[16];

    for (int i=0; i<16; i++) {
      viewCopy[i] = view[i];
    }

    return viewCopy;
  }

  /**
   * Returns the array (16 elements) of matrix values for this camera.  The
   * view matrix is used in JOGL (OpenGL) by calling: gl.glMultMatrixd(view, 0)
   *
   * @return Array of matrix values.
   */
  public double[] getView()
  {
    // First do a check if an update is needed
    checkUpdate();

    return view;
  }

  /**
   * Checks if an update is required and performs one as necessary.
   *
   */
  private void checkUpdate()
  {
    if (needUpdate) {
      apply();
    }
  }

  /**
   * Convenience method to perform a rotation [degrees] about the current
   * look vector.  A call to the apply() method is required to properly
   * recalculate the camera system.
   *
   * @param angle Angle [degrees] to rotate about the look vector.
   */
  public void rotateAroundLookVector( double angle )
  {
    // First calculate the updated up vector that was rotated about the
    // look vector.
    setUp( rotateVector(angle, up, look) );

    // The last vector of the axis triple, the right vector, will be
    // automatically calculated when the apply() method is called.
  }

  /**
   * Convenience method to perform a rotation [degrees] about the current
   * up vector.  A call to the apply() method is required to properly
   * recalculate the camera system.
   *
   * @param angle Angle [degrees] to rotate about the up vector.
   */
  public void rotateAroundUpVector( double angle )
  {

    double matrotation[] = { 1.0, 0.0, 0.0, 0.0,
                             0.0, 1.0, 0.0, 0.0,
                             0.0, 0.0, 1.0, 0.0,
                             0.0, 0.0, 0.0, 1.0
                           };

    matrotation = matRotate(matrotation, angle, up);
    look = transformVector(matrotation, look);
    up = transformVector(matrotation, up);
    right = transformVector(matrotation, right);

    needUpdate = true;
  }

  /**
   * Matrix rotation about a given axis.
   *
   * @param mat The matrix to rotate.
   * @param angle The angle [in degrees] to rotate.
   * @param axis The axis about which to rotate.
   * @return The rotated matrix.
   */
  private double[] matRotate( double mat[], double angle, Vector3d axis )
  {
    double s = Math.sin(angle * Math.PI / 180.0);
    double c = Math.cos(angle * Math.PI / 180.0);

    axis.normalize();

    //ERROR: mat[0] = c + (1 - c) * axis.x;
    mat[0] = c + (1 - c) * Math.pow(axis.x, 2);
    mat[1] = (1 - c) * axis.x * axis.y + s * axis.z;
    mat[2] = (1 - c) * axis.x * axis.z - s * axis.y;
    mat[3] = 0.0;

    mat[4] = (1 - c) * axis.y * axis.x - s * axis.z;
    mat[5] = c + (1 - c) * Math.pow(axis.y, 2);
    mat[6] = (1 - c) * axis.y * axis.z + s * axis.x;
    mat[7] = 0.0;

    mat[8] = (1 - c) * axis.z * axis.x + s * axis.y;
    //ERROR: mat[9] = (1 - c) * axis.z * axis.z - s * axis.x;
    mat[9] = (1 - c) * axis.y * axis.z - s * axis.x;
    mat[10] = c + (1 - c) * Math.pow(axis.z, 2);
    mat[11] = 0.0;

    mat[12] = 0.0;
    mat[13] = 0.0;
    mat[14] = 0.0;
    mat[15] = 1.0;

    return mat;
  }

  /**
   * Transform a vector by the matrix that is passed in.
   *
   * @param mat The transformation matrix.
   * @param vec The vector to transform.
   * @return The transformed vector.
   */
  private Vector3d transformVector( double mat[], Vector3d vec )
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
   * Matrix multiplication of two matrices returns the resulting matrix.
   *
   * @param one The left matrix to multiply with.
   * @param two The right matrix which is to be mutliplied.
   * @return The result of the two matrix multiplication.
   */
  private double[] matmult( double one[], double two[] )
  {
    double R[] = { 1.0, 0.0, 0.0, 0.0,
                   0.0, 1.0, 0.0, 0.0,
                   0.0, 0.0, 1.0, 0.0,
                   0.0, 0.0, 0.0, 1.0
                 };

    R[0] = one[0]*two[0] + one[1]*two[4] + one[2]*two[8] + one[3]*two[12];
    R[1] = one[0]*two[1] + one[1]*two[5] + one[2]*two[9] + one[3]*two[13];
    R[2] = one[0]*two[2] + one[1]*two[6] + one[2]*two[10] + one[3]*two[14];
    R[3] = one[0]*two[3] + one[1]*two[7] + one[2]*two[11] + one[3]*two[15];

    R[4] = one[4]*two[0] + one[5]*two[4] + one[6]*two[8] + one[7]*two[12];
    R[5] = one[4]*two[1] + one[5]*two[5] + one[6]*two[9] + one[7]*two[13];
    R[6] = one[4]*two[2] + one[5]*two[6] + one[6]*two[10] + one[7]*two[14];
    R[7] = one[4]*two[3] + one[5]*two[7] + one[6]*two[11] + one[7]*two[15];

    R[8] = one[8]*two[0] + one[9]*two[4] + one[10]*two[8] + one[11]*two[12];
    R[9] = one[8]*two[1] + one[9]*two[5] + one[10]*two[9] + one[11]*two[13];
    R[10] = one[8]*two[2] + one[9]*two[6] + one[10]*two[10] + one[11]*two[14];
    R[11] = one[8]*two[3] + one[9]*two[7] + one[10]*two[11] + one[11]*two[15];

    R[12] = one[12]*two[0] + one[13]*two[4] + one[14]*two[8] + one[15]*two[12];
    R[13] = one[12]*two[1] + one[13]*two[5] + one[14]*two[9] + one[15]*two[13];
    R[14] = one[12]*two[2] + one[13]*two[6] + one[14]*two[10] + one[15]*two[14];
    R[15] = one[12]*two[3] + one[13]*two[7] + one[14]*two[11] + one[15]*two[15];

    return R;
  }

  /**
   * Convenience method to perform a rotation [degrees] about the current
   * right vector.  A call to the apply() method is required to properly
   * recalculate the camera system.
   *
   * @param angle Angle [degrees] to rotate about the right vector.
   */
  public void rotateAroundRightVector( double angle )
  {
    // First calculate the updated look vector that was rotated about the
    // right vector.
    setLook( rotateVector(angle, look, right) );

    // Using the new look vector (ie the one rotated about the right vector)
    // calculate the new up vector.  Cross product of the 'right' and 'look'
    // vectors gets the 'up' vector.
    Vector3d.cross(up, right, look);

    // Make sure the look vector has some length
    if (Math.abs(up.length()) < 1.0e-6) {
      throw new RuntimeException("Up vector cannot have zero length.");
    }
    up.normalize();
  }

  /**
   * Convenience method that makes the camera take a single step to the left
   * using the predefined step increment.
   */
  public void stepLeft()
  {
    moveLeftRight(-moveSpeed);
  }

  /**
   * Convenience method that makes the camera take a single step to the right
   * using the predefined step increment.
   */
  public void stepRight()
  {
    moveLeftRight(moveSpeed);
  }

  /**
   * Convenience method that makes the camera take a single step to the left
   * using a passed in step increment in place of the predefined one.
   *
   * @param step Step increment.
   */
  public void moveLeft( double step )
  {
    moveLeftRight(-step);
  }

  /**
   * Convenience method that makes the camera take a single step to the right
   * using a passed in step increment in place of the predefined one.
   *
   * @param step Step increment.
   */
  public void moveRight( double step )
  {
    moveLeftRight(step);
  }

  /**
   * Method that makes the camera take a single step to the left or right
   * using a passed in step increment.  The step occurs along the local
   * right vector of the current view.
   *
   * @param step Step increment.
   */
  public void moveLeftRight( double step )
  {
    orbitEye.x -= step;
    needUpdate = true;
  }

  /**
   * Convenience method that makes the camera take a single step forward
   * using the predefined step increment.
   */
  public void stepFwd()
  {
    moveFwdBack(moveSpeed);
  }

  /**
   * Convenience method that makes the camera take a single step back
   * using the predefined step increment.
   */
  public void stepBack()
  {
    moveFwdBack(-moveSpeed);
  }

  /**
   * Convenience method that makes the camera take a single step forward
   * using a passed in step increment in place of the predefined one.
   *
   * @param step Step increment.
   */
  public void moveFwd( double step )
  {
    moveFwdBack(step);
  }

  /**
   * Convenience method that makes the camera take a single step back
   * using a passed in step increment in place of the predefined one.
   *
   * @param step Step increment.
   */
  public void moveBack( double step )
  {
    moveFwdBack(-step);
  }

  /**
   * Method that makes the camera take a single step forward or back
   * using a passed in step increment.  The step occurs along the local
   * look vector of the current view.
   *
   * @param step Step increment.
   */
  public void moveFwdBack( double step )
  {
    orbitEye.z += step;
    needUpdate = true;
  }

  /**
   * Convenience method that makes the camera take a single step up
   * using the predefined step increment.
   */
  public void stepUp()
  {
    moveUpDown(moveSpeed);
  }

  /**
   * Convenience method that makes the camera take a single step down
   * using the predefined step increment.
   */
  public void stepDown()
  {
    moveUpDown(-moveSpeed);
  }

  /**
   * Convenience method that makes the camera take a single step up
   * using a passed in step increment in place of the predefined one.
   *
   * @param step Step increment.
   */
  public void moveUp( double step )
  {
    moveUpDown(step);
  }

  /**
   * Convenience method that makes the camera take a single step down
   * using a passed in step increment in place of the predefined one.
   *
   * @param step Step increment.
   */
  public void moveDown( double step )
  {
    moveUpDown(-step);
  }

  /**
   * Method that makes the camera take a single step up or down
   * using a passed in step increment.  If the FPS mode is enabled
   * the step occurs along the global 'y' axis, otherwise the step
   * occurs along the local up axis of the current view.
   *
   * @param step Step increment.
   */
  public void moveUpDown( double step )
  {
    orbitEye.y -= step;
    needUpdate = true;
  }

  /**
   * This code rotates the vector by the desired angle [degrees] from
   * its current direction - the rotations are cummulative.
   *
   * Reference: http://local.wasp.uwa.edu.au/~pbourke/geometry/rotate/
   *
   * @param angle Angle [degrees] to rotate the rotation vector.
   * @param rotationVector The vector to rotate.
   * @param rotateAboutVector The vector to rotate about.
   */
  private Vector3d rotateVector( double angle, Vector3d rotationVector,
                                 Vector3d rotateAboutVector )
  {
    // Convert angle to degrees
    double a = angle * Math.PI/180.0;

    // Create a quaternion from the vector to rotate
    Quat4f q1 = new Quat4f((float)rotationVector.x,
                           (float)rotationVector.y,
                           (float)rotationVector.z,
                           0.0f);

    // Create a quaternion from the vector to rotate about
    Quat4f q2 = new Quat4f((float)(rotateAboutVector.x * Math.sin(a/2.0)),
                           (float)(rotateAboutVector.y * Math.sin(a/2.0)),
                           (float)(rotateAboutVector.z * Math.sin(a/2.0)),
                           (float)(Math.cos(a/2)));

    // Create the 3rd quaternion that is the result of the multiplication
    // of the previous rotation vector:
    //
    //    q3 = q2 q1 q2*
    //
    //       where * = conjugate  (in this case, the conjugate of q2)
    //
    Quat4f q3 = new Quat4f();
    q3 = Quat4f.mult(q2, Quat4f.mult(q1, Quat4f.conjugate(q2)));

    // Extract the rotated vector from the calculated quaternion
    Vector3d rotatedVector = new Vector3d(q3.x, q3.y, q3.z);

    // Make sure the look vector has some length
    if (Math.abs(rotatedVector.length()) < 1.0e-6) {
      throw new RuntimeException("Rotated vector cannot have zero length.");
    }
    rotatedVector.normalize();

    return rotatedVector;
  }

  /**
   * Set the move speed/incremenent for camera movement.
   *
   * @param speed The speed to set the camera move speed/increment to.
   */
  public void setMoveSpeed( double speed )
  {
    moveSpeed = speed;
  }

  /**
   * Returns the camera move speed/increment.
   *
   * @return Camera move speed/increment.
   */
  public double getMoveSpeed()
  {
    return moveSpeed;
  }

  /**
   * Returns if the orbit mode is set.
   *
   * @return Status of orbit mode.
   */
  public boolean getOrbit()
  {
    return orbit;
  }

  /**
   * Set the orbit mode.
   *
   * @param state Status of the orbit mode to set.
   */
  public void setOrbit( boolean state )
  {
    //orbit = state;
    orbit = true;

    if (orbit) {
      orbitPoint.x = eye.x;
      orbitPoint.y = eye.y;
      orbitPoint.z = eye.z;

      needUpdate = true;
    }
  }

  /**
   * Returns the point around which the view is to orbit around when the
   * orbit mode is activated.
   *
   * @return Returns the orbit point reference.
   */
  public Vector3d getOrbitPoint()
  {
    return orbitPoint;
  }

  /**
   * Returns the copy of the point around which the view is to orbit around
   * when the orbit mode is activated.
   *
   * @return Returns the orbit point copy.
   */
  public Vector3d getOrbitPointCopy()
  {
    return new Vector3d(orbitPoint);
  }

  /**
   * Set the orbit point around which rotations occur when using the
   * orbiting mode.
   *
   * @param pt The orbit point around which to rotate about.
   */
  public void setOrbitPoint( Vector3d pt )
  {
    // Make sure eye point is not null
    if (pt == null) {
      throw new RuntimeException("Orbit point cannot be null.");
    }

    orbitPoint.x = pt.x;
    orbitPoint.y = pt.y;
    orbitPoint.z = pt.z;

    needUpdate = true;
  }

  /**
   * Returns the orbiting distance around a point.
   *
   * @return The current orbiting distance.
   */
  public double getOrbitDistance()
  {
    return -orbitEye.z;
  }

  /**
   * Sets the orbiting distance (applies only to the case when the orbit
   * mode is enabled).
   *
   * @param distance Distance to orbit an object.
   */
  public void setOrbitDistance( double distance )
  {
    orbitDistance = distance;

    orbitEye.x = 0.0;
    orbitEye.y = 0.0;
    orbitEye.z = -orbitDistance;

    needUpdate = true;
  }

  /**
   * Returns the string representation of the Camera that includes the
   * various camera properties such as the Look, Eye and Up vector values.
   *
   * @return String representation of the camera properties.
   */
  @Override
  public String toString()
  {
    return new String("Camera Properties:"
                      + "\n  Look Vector: " + look
                      + "\n   Eye Vector: " + eye
                      + "\n    Up Vector: " + up
                      + "\n Right Vector: " + right);
  }
}
