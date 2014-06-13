package jPCBSim;

/**
 * Three Dimensional (3D) vector with x,y and z components.
 * <p>
 */
public class Vector3d
{

  /** x coordinate of the vector. */
  public double x = 0.0;

  /** y coordinate of the vector. */
  public double y = 0.0;

  /** z coordinate of the vector. */
  public double z = 0.0;

  /**
   * Default constructor that creates an instance of the Vector3d object.
   *
   *
   */
  public Vector3d()
  {
    // Nothing
  }

  /**
   * Cconstructor that creates an instance of the Vector3d object using the
   * passed in values as doubles.
   *
   * @param x Double value of x.
   * @param y Double value of y.
   * @param z Double value of z.
   */
  public Vector3d( double x, double y, double z )
  {
    set(x, y, z);
  }

  /**
   * Constructor that creates an instance of the Vector3d object a
   * Vector3d object.
   *
   * @param vector A 3D Vector.
   */
  public Vector3d( Vector3d vector )
  {
    set(vector.x, vector.y, vector.z);
  }

  /**
   * Sets the current vector to the values that are passed in.
   *
   * @param x Value of x.
   * @param y Value of y.
   * @param z Value of z.
   */
  public void set( double x, double y, double z )
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Static method to calculates the cross product of Vector3d v1 and v2
   * and sets the result to Vector3d Result.  The Result is returned by
   * reference back to thecalling method.
   *
   * @param Result The result Vector3d of the cross product.
   * @param v1 The first vector.
   * @param v2 The second vector.
   */
  public static void cross( Vector3d Result, Vector3d v1, Vector3d v2 )
  {
    Result.x = v1.y*v2.z - v1.z*v2.y;
    Result.y = v1.z*v2.x - v1.x*v2.z;
    Result.z = v1.x*v2.y - v1.y*v2.x;
  }

  /**
   * Calculates the cross product of Vector3d v1 and v2 and sets the result
   * to the current vector.
   *
   * @param v1 The first vector.
   * @param v2 The second vector.
   */
  public void cross( Vector3d v1, Vector3d v2 )
  {
    this.x = v1.y*v2.z - v1.z*v2.y;
    this.y = v1.z*v2.x - v1.x*v2.z;
    this.z = v1.x*v2.y - v1.y*v2.x;
  }

  /**
   * Calculates the dot product of Vector3d v1 and v2 and returns the double
   * value to the calling method.
   *
   * @param v1 The first vector.
   * @param v2 The second vector.
   * @return The dot product result.
   */
  public static double dot( Vector3d v1, Vector3d v2 )
  {
    return (v1.x * v2.x) + (v1.y * v2.y) + (v1.z * v2.z);
  }

  /**
   * Calculates the length of the current vector and returns the result.
   *
   * @return Length of the current vector.
   */
  public double length()
  {
    return Math.sqrt(x*x + y*y + z*z);
  }

  /**
   * Subtracts two vectors and assigns the result to the current vector.
   *
   * @param one First vector to subtract from (represented as a Vector3d).
   * @param two The vector to subtract (represented as a Vector3d).
   */
  public void sub( Vector3d one, Vector3d two )
  {
    this.x = one.x - two.x;
    this.y = one.y - two.y;
    this.z = one.z - two.z;
  }

  /**
   * Normalizes the current vector.
   *
   */
  public void normalize()
  {
    double length = length();

    if (length < 1E-9) {
      throw new RuntimeException("Cannot normalize vector whose length is zero");
    }

    x /= length;
    y /= length;
    z /= length;
  }

  /**
   * Negate the vector (flip the direction).
   */
  public void negate()
  {
    x *= -1.0;
    y *= -1.0;
    z *= -1.0;
  }

  /**
   * Multiply the current vector by some amount.
   *
   * @param value The amount to multiply the components of the current vector.
   */
  public void mul( double value )
  {
    this.x *= value;
    this.y *= value;
    this.z *= value;
  }

  /**
   * Generates the String to represent a Vector3d object in a nice format for
   * output purposes.
   *
   * @return The String representation of a Vector3d object.
   */
  public String toString()
  {
    return "x: " + x + " y: " + y + " z: " + z;
  }
}
