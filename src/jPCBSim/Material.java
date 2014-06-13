// Copyright (c) 2014, tvelliott
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
//   list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// * Neither the name of the {organization} nor the names of its
//   contributors may be used to endorse or promote products derived from
//   this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package jPCBSim;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import static javax.media.opengl.GL.*;  // GL constants
import static javax.media.opengl.GL2.*; // GL2 constants
import com.jogamp.opengl.util.*;

import java.io.*;
import java.util.*;

public class Material
{

  static public final int DIELECTRIC_BOX = 1;
  static public final int COPPER_POLYGON = 2;
  static public final int COPPER_VIA = 3;
  static public final int EXCITATION_BOX = 4;

  private String name="";
  private String priority="";
  private String normal="";
  private String elevation="";
  private double kappa = 0.0;
  private double epsilon = 1.0;
  private double mue = 1.0;
  private int material_type;

  public double box_xc;
  public double box_yc;
  public double box_zc;
  public double box_xd;
  public double box_yd;
  public double box_zd;

  public double via_sx;
  public double via_sy;
  public double via_sz;
  public double via_ex;
  public double via_ey;
  public double via_ez;
  public double via_radius;

  public Material(String name, double kappa, double epsilon, double mue)
  {
    this.name = name;
    this.kappa = kappa;
    this.epsilon = epsilon;
    this.mue = mue;
  }

  public Material(String name)
  {
    this.name = name;
    kappa = 0.0;
    epsilon = 1.0;
    mue = 1.0;
    //TODO should adjust properties based on known material names
    if(name.equals("")) {
    }
  }

  public Material(String name, String priority, String normal, String elevation, int type)
  {
    this.name = name;
    this.priority = priority;
    this.normal = normal;
    this.elevation = elevation;
    this.material_type = type;
    kappa = 0.0;
    epsilon = 1.0;
    mue = 1.0;
    //TODO should adjust properties based on known material names
    if(name.equals("")) {
    }
  }

  public void setViaCoords(double x1, double y1, double z1, double x2, double y2, double z2, double radius)
  {
    this.via_sx = x1;
    this.via_sy = y1;
    this.via_sz = z1;
    this.via_ex = x2;
    this.via_ey = y2;
    this.via_ez = z2;
    this.via_radius = radius;
  }

  public void setBoxCoords(double xc, double yc, double zc, double xd, double yd, double zd)
  {
    this.box_xc = xc;
    this.box_yc = yc;
    this.box_zc = zc;
    this.box_xd = xd;
    this.box_yd = yd;
    this.box_zd = zd;
  }

  public int getMaterialType()
  {
    return this.material_type;
  }

  public String getElevation()
  {
    return elevation;
  }
  public String getNormal()
  {
    return normal;
  }
  public String getPriority()
  {
    return priority;
  }

  double getEpsilon()
  {
    return this.epsilon;
  }
  double getKappa()
  {
    return this.kappa;
  }
  double getMUE()
  {
    return this.mue;
  }
  public String getName()
  {
    return this.name;
  }

}
