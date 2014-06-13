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
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.FPSAnimator;
import static javax.media.opengl.GL.*;  // GL constants
import static javax.media.opengl.GL2.*; // GL2 constants
import com.jogamp.opengl.util.*;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
/*
   * Tessellator callback implemenation with all the callback routines. YOu
   * could use GLUtesselatorCallBackAdapter instead. But
   */
class tessellCallBack
  implements GLUtessellatorCallback
{
  private GL2 gl;
  private GLU glu;

  public tessellCallBack(GL2 gl, GLU glu)
  {
    this.gl = gl;
    this.glu = glu;
  }

  public void begin(int type)
  {
    gl.glBegin(type);
  }

  public void end()
  {
    gl.glEnd();
  }

  public void vertex(Object vertexData)
  {
    double[] pointer;
    if (vertexData instanceof double[]) {
      pointer = (double[]) vertexData;
      if (pointer.length == 6) gl.glColor3dv(pointer, 3);
      gl.glVertex3dv(pointer, 0);
    }

  }

  public void vertexData(Object vertexData, Object polygonData)
  {
  }

  /*
   * combineCallback is used to create a new vertex when edges intersect.
   * coordinate location is trivial to calculate, but weight[4] may be used to
   * average color, normal, or texture coordinate data. In this program, color
   * is weighted.
   */
  public void combine(double[] coords, Object[] data, //
                      float[] weight, Object[] outData)
  {
    double[] vertex = new double[6];
    int i;

    vertex[0] = coords[0];
    vertex[1] = coords[1];
    vertex[2] = coords[2];
    for (i = 3; i < 6/* 7OutOfBounds from C! */; i++)
      vertex[i] = weight[0] //
                  * ((double[]) data[0])[i] + weight[1]
                  * ((double[]) data[1])[i] + weight[2]
                  * ((double[]) data[2])[i] + weight[3]
                  * ((double[]) data[3])[i];
    outData[0] = vertex;
  }

  public void combineData(double[] coords, Object[] data, //
                          float[] weight, Object[] outData, Object polygonData)
  {
  }

  public void error(int errnum)
  {
    String estring;

    estring = glu.gluErrorString(errnum);
    System.err.println("Tessellation Error: " + estring);
    System.exit(0);
  }

  public void beginData(int type, Object polygonData)
  {
  }

  public void endData(Object polygonData)
  {
  }

  public void edgeFlag(boolean boundaryEdge)
  {
  }

  public void edgeFlagData(boolean boundaryEdge, Object polygonData)
  {
  }

  public void errorData(int errnum, Object polygonData)
  {
  }
}// tessellCallBack
