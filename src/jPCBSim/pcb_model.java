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

import java.io.*;
import java.util.*;
import java.awt.geom.*;


public class pcb_model
{
  private String file;
  private pcb_object pcbobj = null;
  private Vector pcb_obj_v;
  private tessellCallBack tessCallback;
  private GLUtessellator tobj;
  double min_x;
  double min_y;
  double min_z;
  double max_x;
  double max_y;
  double max_z;
  private double center_x;
  private double center_y;
  private double center_z;
  private boolean did_excitation=false;
  private mesh_model meshmodel;
  private boolean is_ortho=true;
  private double zoom_ortho=1.0;
  private String height_user="";
  private Simulation simulation;

  public boolean is_loading=false;

  public pcb_model(String path, Simulation simulation)
  {
    this.file = path;
    this.height_user = simulation.pcb_thickness_inches;
    this.simulation = simulation;
    pcb_obj_v = new Vector();
    loadPCBModel(this.file);
  }

  public mesh_model getMeshModel()
  {
    return meshmodel;
  }

  public Vector getPCBObjectVector()
  {
    return pcb_obj_v;
  }

  public boolean isOrtho()
  {
    return is_ortho;
  }
  public void setOrtho(boolean ortho, double zoom_ortho)
  {
    this.is_ortho = ortho;
    this.zoom_ortho = zoom_ortho;
  }
  public double getZoomOrtho()
  {
    return this.zoom_ortho;
  }

  public void setLoading()
  {
    is_loading=true;
  }

  public void updateMesh(Simulation simulation)
  {
    if(simulation!=null) this.simulation = simulation;
    if(meshmodel!=null) meshmodel.updateSim(simulation);
  }

  public void loadPCBModel(String file)
  {

    is_loading=true;

    try {
      Thread.sleep(1000);
    } catch(Exception e) {
    }

    int mode = 0;
    min_x = 9999;
    min_y = 9999;
    min_z = 9999;
    max_x = -9999;
    max_y = -9999;
    max_z = -9999;


    pcbobj = new pcb_object(null,null);

    try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line="";
      while(line!=null) {
        line = br.readLine();

        if(line!=null) {

          if(line.indexOf("AddPolygon")>=0) {
            //make sure it is copper
            if(line.indexOf("_copper")<0) {
              pcbobj = new pcb_object(null, null);
              continue;
            }
            StringTokenizer st = new StringTokenizer(line,",");
            st.nextToken();
            String matname = st.nextToken();
            String priority = st.nextToken();
            String normal = st.nextToken();

            String elevation = st.nextToken();
            if(height_user.length()>0 && new Double(height_user).doubleValue()!=0.0) {
              //inches
              double e = new Double(elevation).doubleValue();
              double h = new Double(height_user).doubleValue();
              if(e==0.0) h=0.0;
              h *= 25.4; //convert mm
              h /= 1000.0;  //convert to meters
              if(e<0) h*=-1.0;
              elevation = new Double(h).toString();
            } else if(new Double(elevation).doubleValue()!=0.0) {
              double pcb_thick_meters = new Double(elevation).doubleValue();
              pcb_thick_meters *= 1000.0; //mm
              pcb_thick_meters /= 25.4; //inches
              simulation.pcb_thickness_inches = String.format("%3.3f", new Double(pcb_thick_meters) );
            }

            Material material = new Material(matname, priority, normal, elevation, Material.COPPER_POLYGON);

            pcbobj.setMaterial( material );

            pcb_obj_v.insertElementAt( pcbobj,0 );

            pcbobj = new pcb_object(null, null);
          }
          //excitation box
          else if(line.indexOf("HyperLynxPort")>=0 && line.indexOf(".TP")>=0 ) {
            StringTokenizer st = new StringTokenizer(line,",");
            st.nextToken();
            String matname = st.nextToken();
            String priority = "999";

            st.nextToken();
            double xc = Double.valueOf(st.nextToken());
            st.nextToken();
            double yc = Double.valueOf(st.nextToken());
            st.nextToken();
            double zc = Double.valueOf(st.nextToken());
            if(height_user.length()>0 && new Double(height_user).doubleValue()!=0.0) {
              //inches
              double z = new Double(zc).doubleValue();
              double h = new Double(height_user).doubleValue();
              h *= 25.4; //convert mm
              h /= 1000.0;  //convert to meters
              if(z<0) h*=-1.0;
              zc = new Double(h).doubleValue();
            }

            st.nextToken();
            double x1 = Double.valueOf(st.nextToken());
            st.nextToken();
            double y1 = Double.valueOf(st.nextToken());
            st.nextToken();
            double x2 = Double.valueOf(st.nextToken());
            st.nextToken();
            double y2 = Double.valueOf(st.nextToken());

            double xd = (x2-x1);
            double yd = (y2-y1);
            double zd = zc;

            Material material = new Material(matname, priority, "", "", Material.EXCITATION_BOX);
            material.setBoxCoords( xc,yc,zc, xd,yd,zd );

            Point2D.Double p = new Point2D.Double(x1+xd*0.01,y1+yd*0.01);
            pcbobj.addPoint(p);
            p = new Point2D.Double(x2-xd*0.01,y2-yd*0.01);
            pcbobj.addPoint(p);

            pcbobj.setMaterial( material );

            pcb_obj_v.insertElementAt( pcbobj,0 );

            pcbobj = new pcb_object(null, null);
          }

          //dielectric
          else if(line.indexOf("AddBox")>=0 && line.indexOf("Dielectric")>=0 ) {
            StringTokenizer st = new StringTokenizer(line,",");
            st.nextToken();
            String matname = st.nextToken();
            String priority = st.nextToken();
            String normal = "2";
            String xx1 = st.nextToken();
            String yy1 = st.nextToken();
            String zz1 = st.nextToken();
            String xx2 = st.nextToken();
            String yy2 = st.nextToken();
            String zz2 = st.nextToken();
            xx1 = xx1.substring( xx1.indexOf("[")+1, xx1.length() );
            zz1 = zz1.substring( 0, zz1.indexOf("]") );
            xx2 = xx2.substring( xx2.indexOf("[")+1, xx2.length() );
            zz2 = zz2.substring( 0, zz2.indexOf("]") );
            if(height_user.length()>0 && new Double(height_user).doubleValue()!=0.0) {
              //inches
              double z = new Double(zz2).doubleValue();
              double h = new Double(height_user).doubleValue();
              h *= 25.4; //convert mm
              h /= 1000.0;  //convert to meters
              if(z<0) h*=-1.0;
              zz2 = new Double(h).toString();
            }

            double x1 = Double.valueOf(xx1);
            double y1 =  Double.valueOf(yy1);
            double z1 =  Double.valueOf(zz1);
            double x2 = Double.valueOf(xx2);
            double y2 =  Double.valueOf(yy2);
            double z2 =  Double.valueOf(zz2);

            double xd = (x2-x1);
            double yd = (y2-y1);
            double zd = (z2-z1);
            double xc = (x2+x1)/2;
            double yc = (y2+y1)/2;
            double zc = (z2+z1)/2;

            if(x1>max_x) max_x = x1;
            if(y1>max_y) max_y = y1;
            if(z1>max_y) max_z = z1;
            if(x2>max_x) max_x = x2;
            if(y2>max_y) max_y = y2;
            if(z2>max_y) max_z = z2;
            if(x1<min_x) max_x = x1;
            if(y1<min_y) min_y = y1;
            if(z1<min_y) min_z = z1;
            if(x2<min_x) min_x = x2;
            if(y2<min_y) min_y = y2;
            if(z2<min_y) min_z = z2;

            String elevation = new Double(zc).toString();
            Material material = new Material(matname, priority, normal, elevation, Material.DIELECTRIC_BOX);
            material.setBoxCoords( xc,yc,zc, xd,yd,zd );

            Point2D.Double p = new Point2D.Double(x1+xd*0.01,y1+yd*0.01);
            pcbobj.addPoint(p);
            p = new Point2D.Double(x2-xd*0.01,y2-xd*0.01);
            pcbobj.addPoint(p);

            pcbobj.setMaterial( material );

            pcb_obj_v.addElement( pcbobj );

            pcbobj = new pcb_object(null, null);
          }
          //via
          else if(line.indexOf("AddCylinder")>=0 && line.indexOf("via")>=0 ) {
            StringTokenizer st = new StringTokenizer(line,",");
            st.nextToken();
            String matname = st.nextToken();
            String priority = st.nextToken();
            String normal = "2";
            String xx1 = st.nextToken();
            String yy1 = st.nextToken();
            String zz1 = st.nextToken();
            String xx2 = st.nextToken();
            String yy2 = st.nextToken();
            String zz2 = st.nextToken();
            String radius = st.nextToken();
            xx1 = xx1.substring( xx1.indexOf("[")+1, xx1.length() );
            zz1 = zz1.substring( 0, zz1.indexOf("]") );
            xx2 = xx2.substring( xx2.indexOf("[")+1, xx2.length() );
            zz2 = zz2.substring( 0, zz2.indexOf("]") );
            radius = radius.substring( 0, radius.indexOf(")") );
            if(height_user.length()>0 && new Double(height_user).doubleValue()!=0.0) {
              //inches
              double z = new Double(zz2).doubleValue();
              double h = new Double(height_user).doubleValue();
              h *= 25.4; //convert mm
              h /= 1000.0;  //convert to meters
              if(z<0) h*=-1.0;
              zz2 = new Double(h).toString();
            }

            double x1 = Double.valueOf(xx1);
            double y1 =  Double.valueOf(yy1);
            double z1 =  Double.valueOf(zz1);
            double x2 = Double.valueOf(xx2);
            double y2 =  Double.valueOf(yy2);
            double z2 =  Double.valueOf(zz2);
            double rad =  Double.valueOf(radius);

            if(x1>max_x) max_x = x1;
            if(y1>max_y) max_y = y1;
            if(z1>max_y) max_z = z1;
            if(x2>max_x) max_x = x2;
            if(y2>max_y) max_y = y2;
            if(z2>max_y) max_z = z2;
            if(x1<min_x) max_x = x1;
            if(y1<min_y) min_y = y1;
            if(z1<min_y) min_z = z1;
            if(x2<min_x) min_x = x2;
            if(y2<min_y) min_y = y2;
            if(z2<min_y) min_z = z2;

            Material material = new Material(matname, priority, normal, "", Material.COPPER_VIA);
            material.setViaCoords(x1,y1,z1,x2,y2,z2,rad);

            //force mesh lines inside of via by 5%
            Point2D.Double p = new Point2D.Double(x1-(rad*0.707),y1-(rad*0.707));
            pcbobj.addPoint(p);
            p = new Point2D.Double(x1+(rad*0.707),y1+(rad*0.707));
            pcbobj.addPoint(p);

            pcbobj.setMaterial( material );

            pcb_obj_v.insertElementAt( pcbobj,0 );

            pcbobj = new pcb_object(null, null);
          }

          else if(line.indexOf("pgon(")>=0) {
            int start = line.indexOf("[")+1;
            int end = line.indexOf("]");
            String xy = line.substring(start,end);
            StringTokenizer st = new StringTokenizer(xy,";");
            double x = new Double(st.nextToken()).doubleValue();
            double y = new Double(st.nextToken()).doubleValue();
            if(x>max_x) max_x = x;
            if(y>max_y) max_y = y;
            if(x<min_x) min_x = x;
            if(y<min_y) min_y = y;
            Point2D.Double p = new Point2D.Double(x,y);
            pcbobj.addPoint(p);
            //System.out.println("xy: "+p);
          }
        }
      }

      meshmodel = null;
    } catch(Exception e) {
      e.printStackTrace();
    }

    is_loading=false;
  }


  public void render(GL2 gl)
  {

    if(is_loading) return;

    updateMesh(simulation);

    pcb_object pobj = null;
    GLU glu = new GLU();
    GLUT glut = new GLUT();

    gl.glPushMatrix();
    gl.glPushAttrib(GL_ALL_ATTRIB_BITS);

    try {
      if( gl!=null && pcbobj != null ) {

        if(tessCallback==null) {
          tessCallback = new tessellCallBack(gl, glu);
          tobj = glu.gluNewTess();
          glu.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tessCallback);// glVertex3dv);
          glu.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tessCallback);// beginCallback);
          glu.gluTessCallback(tobj, GLU.GLU_TESS_END, tessCallback);// endCallback);
          glu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tessCallback);// errorCallback);
        }

        Enumeration <pcb_object> po = pcb_obj_v.elements();


        //translate to geometry center for orbit view
        center_x = (min_x+max_x)/2;
        center_y = (min_y+max_y)/2;
        center_z = (min_z+max_z)/2;
        //System.out.println(center_x+" "+center_y+" "+center_z);
        gl.glTranslated(-center_x, -center_z, -center_y);


        while(po.hasMoreElements()) {
          pobj = po.nextElement();

          //copper polygons are tessellated
          if(pobj.getMaterial().getMaterialType() == Material.COPPER_POLYGON) {

            double elevation = new Double(pobj.getMaterial().getElevation()).doubleValue();
            if(elevation>max_z) max_z = elevation;
            if(elevation<min_z) min_z = elevation;

            Enumeration <Point2D.Double> e = pobj.getGeometry();
            if( e!=null) {

              gl.glPushMatrix();
              gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
              gl.glDisable(GL_LINE_SMOOTH);
              gl.glEnable(GL2.GL_POLYGON_SMOOTH);
              gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
              gl.glEnable(GL2.GL_BLEND);
              gl.glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
              gl.glDisable(GL2.GL_LIGHTING);
              gl.glLineWidth(1.0f);

              int line = 0;
              Point2D.Double start_point=null;
              Point2D.Double next_point=null;


              glu.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_POSITIVE);
              //gl.glCullFace(GL2.GL_FRONT);
              //copper
              gl.glColor4d(156.0/350.0, 120.0/350.0, 19/350.0, 1.0);

              glu.gluTessBeginPolygon(tobj,null);
              e = pobj.getGeometry();
              glu.gluTessBeginContour(tobj);
              if(e.hasMoreElements()) {
                double[][] tri = new double[pobj.getSize()][3];
                int index = 0;
                while(e.hasMoreElements()) {
                  Point2D.Double point = e.nextElement();
                  tri[index][0] = point.getX();
                  tri[index][1] = elevation;
                  tri[index][2] = point.getY();
                  gl.glNormal3d(0,1,0);
                  glu.gluTessVertex(tobj, tri[index], 0, tri[index]);

                  index++;
                }
              }
              glu.gluTessEndContour(tobj);
              glu.gluTessEndPolygon(tobj);

              gl.glPopAttrib();
              gl.glPopMatrix();
            }

          }
          //excitation scaled box
          else if(pobj.getMaterial().getMaterialType() == Material.EXCITATION_BOX) {
            gl.glPushMatrix();
            gl.glPushAttrib(GL_ALL_ATTRIB_BITS);

            gl.glEnable (GL_LINE_SMOOTH);
            gl.glEnable(GL2.GL_POLYGON_SMOOTH);
            gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
            gl.glEnable(GL2.GL_BLEND);
            gl.glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            gl.glLineWidth(1.0f);

            //first excitation port is source.  user can swap in interface
            if( pobj.getMaterial().getName().indexOf("1")>=0) {
              gl.glColor4d( 0.4f, 0.0f, 0f, 0.80f);
              did_excitation=true;
            } else {
              gl.glColor4d( 0.0f, 0.0f, 0.4f, 0.80f);
              did_excitation=false;
            }
            double xc = pobj.getMaterial().box_xc;
            double yc = pobj.getMaterial().box_yc;
            double zc = pobj.getMaterial().box_zc;
            double xd = pobj.getMaterial().box_xd;
            double yd = pobj.getMaterial().box_yd;
            double zd = pobj.getMaterial().box_zd*1.001;
            gl.glTranslated(xc, zc/2, yc);
            gl.glScaled( xd, zd, yd);
            glut.glutSolidCube(1.0f);

            gl.glPopAttrib();
            gl.glPopMatrix();
          }
          //dielectric scaled box
          else if(pobj.getMaterial().getMaterialType() == Material.DIELECTRIC_BOX) {
            gl.glPushMatrix();
            gl.glPushAttrib(GL_ALL_ATTRIB_BITS);

            gl.glEnable (GL_LINE_SMOOTH);
            gl.glEnable(GL2.GL_POLYGON_SMOOTH);
            gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
            gl.glEnable(GL2.GL_BLEND);
            gl.glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            gl.glLineWidth(1.0f);

            gl.glColor4d( 0f, 0.1f, 0f, 0.80f);
            double xc = pobj.getMaterial().box_xc;
            double yc = pobj.getMaterial().box_yc;
            double zc = pobj.getMaterial().box_zc;
            double xd = pobj.getMaterial().box_xd;
            double yd = pobj.getMaterial().box_yd;
            double zd = pobj.getMaterial().box_zd;
            gl.glTranslated(xc, zc, yc);
            //scale 90% so copper doesn't get buried in the dielectric when viewing
            gl.glScaled( xd, zd*0.90, yd);
            glut.glutSolidCube(1.0f);

            gl.glPopAttrib();
            gl.glPopMatrix();
          }
          //via
          else if(pobj.getMaterial().getMaterialType() == Material.COPPER_VIA) {
            gl.glPushMatrix();
            gl.glPushAttrib(GL_ALL_ATTRIB_BITS);

            gl.glEnable (GL_LINE_SMOOTH);
            gl.glEnable(GL2.GL_POLYGON_SMOOTH);
            gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
            gl.glEnable(GL2.GL_BLEND);
            gl.glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            gl.glLineWidth(1.0f);

            //gl.glColor4d(156.0/450.0, 120.0/450.0, 19/450.0, 1.0);
            gl.glColor4d(100.0/255.0,100.0/255.0,100.0/255.0, 1.0);
            double x1 = pobj.getMaterial().via_sx;
            double y1 = pobj.getMaterial().via_sy;
            double z1 = pobj.getMaterial().via_sz;
            double x2 = pobj.getMaterial().via_ex;
            double y2 = pobj.getMaterial().via_ey;
            double z2 = pobj.getMaterial().via_ez;
            double rad = pobj.getMaterial().via_radius;

            //draw the via cylinder
            gl.glTranslated(x1, z1, y1);
            gl.glRotated(90, 1,0,0);
            glut.glutSolidCylinder( rad, -Math.abs(z2-z1), 20, 2);

            //black hole drill
            gl.glColor4d(0,0,0,1);
            glut.glutSolidCylinder( rad*0.8, -Math.abs(z2-z1)*1.01, 20, 2);
            glut.glutSolidCylinder( rad*0.8, Math.abs(z2-z1)*0.01, 20, 2);

            gl.glPopAttrib();
            gl.glPopMatrix();
          }

        }//while



      }


    } catch(Exception e) {
      //e.printStackTrace();
    }

    gl.glPopAttrib();
    gl.glPopMatrix();
  }

  public void renderMesh(GL2 gl, boolean do_pml, boolean do_airbox)
  {
    if( meshmodel==null && simulation!=null) {
      meshmodel = new mesh_model( this, simulation, min_x, min_y, min_z, max_x, max_y, max_z );
      meshmodel.setPCBModel(this);
    }
    meshmodel.render(gl, do_pml, do_airbox);
  }


  public pcb_object getDielectricObject()
  {
    Enumeration <pcb_object> po = pcb_obj_v.elements();

    while(po.hasMoreElements()) {
      pcb_object pobj = po.nextElement();

      //excitation scaled box
      if(pobj.getMaterial().getMaterialType() == Material.DIELECTRIC_BOX) {
        return pobj;
      }
    }
    return null;
  }



}
