package jPCBSim;


/**
 * Quaternion class.
 * <p>
 */
public class Quat4f
{

  /** x (first component) of the quaternion */
  public float x = 0.0f;

  /** y (second component) of the quaternion */
  public float y = 0.0f;

  /** z (third component) of the quaternion */
  public float z = 0.0f;

  /** w (fourth component) of the quaternion */
  public float w = 0.0f;

  /**
   * Default constructor that creates an instance of the quaternion
   *
   *
   */
  public Quat4f()
  {
    // Nothing
  }

  /**
   * Cconstructor that creates an instance of the Quat4f object using the
   * passed in values
   *
   * @param x Value of x
   * @param y Value of y
   * @param z Value of z
   * @param w Value of w
   */
  public Quat4f( float x, float y, float z, float w )
  {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public static Quat4f conjugate( Quat4f quat )
  {
    Quat4f C = new Quat4f();

    C.x = -quat.x;
    C.y = -quat.y;
    C.z = -quat.z;
    C.w = quat.w;

    return C;
  }
  public static Quat4f mult( Quat4f A, Quat4f B )
  {
    Quat4f C = new Quat4f();

    C.x = A.w*B.x + A.x*B.w + A.y*B.z - A.z*B.y;
    C.y = A.w*B.y - A.x*B.z + A.y*B.w + A.z*B.x;
    C.z = A.w*B.z + A.x*B.y - A.y*B.x + A.z*B.w;
    C.w = A.w*B.w - A.x*B.x - A.y*B.y - A.z*B.z;

    return C;
  }


  /**
   * Generates the String to represent a Quat4f object in a nice format for
   * output purposes.
   *
   * @return The String representation of a Quat4f object.
   */
  public String toString()
  {
    return "Quaternion: x: " + x + " y: " + y + " z: " + z + " w: " + w;
  }
}
