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


public class mesh_model
{
  private pcb_model pcbmodel;
  private boolean do_pml_display=false;
  private boolean do_airbox_disp=false;
  private double min_x;
  private double min_y;
  private double min_z;
  private double max_x;
  private double max_y;
  private double max_z;
  private double center_x;
  private double center_y;
  private double center_z;

  private Vector xlines_v;
  private Vector ylines_v;
  private Vector zlines_v;
  private Vector xcells_v;
  private Vector ycells_v;
  private Vector zcells_v;
  private Vector edge_xcells_v;
  private Vector edge_ycells_v;
  private Vector edge_zcells_v;

  private double c0 = 299792458; //meters per second
  private double fmax = 5.0e9;
  private double fcenter = fmax;
  private double sqr_epr = Math.pow( 4.35, 0.5 ); //sqrt(epr)
  private double unit  = 1.0; //meters
  private double mesh_resolution;
  private double airbox_dist;

  //1/15th wavelength in dielectric
  private double resolution = c0 / fmax / sqr_epr / unit / 16.0;

  //1/4 wavelength in air
  //private double quarter_wavelen = c0 / fcenter / unit / 4.0;
  private double quarter_wavelen = c0 / fcenter / unit / 2.0;
  private int n_pml_cells = 8;
  private Simulation simulation;
  private Simulation new_simulation;
  private boolean do_update_sim;

  public mesh_model(pcb_model model, Simulation simulation, double minx, double miny, double minz, double maxx, double maxy, double maxz)
  {
    this.simulation = simulation;
    this.pcbmodel = model;
    this.min_x = minx;
    this.min_y = miny;
    this.min_z = minz;
    this.max_x = maxx;
    this.max_y = maxy;
    this.max_z = maxz;
    new_simulation=null;
  }

  public void setPCBModel(pcb_model model )
  {
    this.pcbmodel = model;
  }

  public void render(GL2 gl, boolean do_pml_display, boolean do_airbox)
  {
    if(pcbmodel.is_loading) return;
    if(do_update_sim) {
      do_update_sim=false;
      simulation = new_simulation;
      update_sim_settings();
    }
    this.do_pml_display = do_pml_display;
    this.do_airbox_disp = do_airbox;
    if(do_airbox) render_airbox(gl);
    render_mesh(gl);
  }

  public void updateSim(Simulation newsim)
  {
    if(simulation!=null) {
      this.new_simulation = newsim;
      do_update_sim=true;
    }
  }

  private void update_sim_settings()
  {
    if(simulation==null) return;
    fmax = Double.valueOf(simulation.fdtd_fmax);
    fcenter = fmax;
    mesh_resolution = Double.valueOf(simulation.mesh_resolution);
    airbox_dist = Double.valueOf(simulation.airbox_dist);
    resolution = c0 / fmax / sqr_epr / unit / mesh_resolution;
    quarter_wavelen = c0 / fcenter / unit / airbox_dist;
  }

  //////////////////////////////////////////////////////
  //////////////////////////////////////////////////////
  public void render_airbox(GL2 gl)
  {
    GLU glu = new GLU();
    GLUT glut = new GLUT();

    if(simulation==null) return;


    try {
      //AIR Box
      gl.glPushMatrix();
      gl.glPushAttrib(GL_ALL_ATTRIB_BITS);


      //translate to geometry center for orbit view
      center_x = (min_x+max_x)/2;
      center_y = (min_y+max_y)/2;
      center_z = (min_z+max_z)/2;
      //System.out.println(center_x+" "+center_y+" "+center_z);
      gl.glTranslated(-center_x, -center_z, -center_y);


      gl.glEnable (GL_LINE_SMOOTH);
      gl.glEnable(GL2.GL_POLYGON_SMOOTH);
      gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
      gl.glEnable(GL2.GL_BLEND);
      gl.glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
      gl.glColor4d( 0f, 0.0f, 1.0f, 0.3f);

      double z1 = min_z-quarter_wavelen;
      double z2 = max_z+quarter_wavelen;
      double x1 = min_x-quarter_wavelen;
      double x2 = max_x+quarter_wavelen;
      double y1 = min_y-quarter_wavelen;
      double y2 = max_y+quarter_wavelen;

      double xd = (x2-x1);
      double yd = (y2-y1);
      double zd = (z2-z1);
      double xc = (x2+x1)/2;
      double yc = (y2+y1)/2;
      double zc = (z2+z1)/2;

      int xcells = (int) (( xd / resolution ));
      if((xcells&0x01)==1) xcells++;
      int ycells = (int) (( yd / resolution ));
      if((ycells&0x01)==1) ycells++;
      int zcells = (int) (( zd / resolution ));
      if((zcells&0x01)==1) zcells++;

      //System.out.println("number of cells: "+xcells*ycells*zcells);

      double x_st = (xcells/2) * resolution * -1.0;
      double y_st = (ycells/2) * resolution * -1.0;
      double z_st = (zcells/2) * resolution * -1.0;
      double x_end = (xcells/2) * resolution;
      double y_end = (ycells/2) * resolution;
      double z_end = (zcells/2) * resolution;

      gl.glTranslated(xc, zc, yc);
      gl.glScaled( (x_end-x_st)*0.999, (z_end-z_st)*0.999, (y_end-y_st)*0.999);
      glut.glutSolidCube(1.0f);

    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      gl.glPopAttrib();
      gl.glPopMatrix();
    }
  }

  //////////////////////////////////////////////////////
  //////////////////////////////////////////////////////
  public void render_mesh(GL2 gl)
  {

    GLU glu = new GLU();
    GLUT glut = new GLUT();

    gl.glPushMatrix();
    gl.glPushAttrib(GL_ALL_ATTRIB_BITS);

    gl.glEnable(GL_LINE_SMOOTH);
    gl.glEnable(GL2.GL_POLYGON_SMOOTH);
    gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
    gl.glEnable(GL2.GL_BLEND);
    gl.glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    gl.glDisable(GL2.GL_LIGHTING);
    gl.glLineWidth(1.0f);

    gl.glColor4d(1.0, 1.0, 1.0, 0.1);

    try {
      if( gl!=null && pcbmodel != null ) {


        //translate to geometry center for orbit view
        center_x = (min_x+max_x)/2;
        center_y = (min_y+max_y)/2;
        center_z = (min_z+max_z)/2;
        //System.out.println(center_x+" "+center_y+" "+center_z);
        gl.glTranslated(-center_x, -center_z, -center_y);


        double z1 = min_z-quarter_wavelen;
        double z2 = max_z+quarter_wavelen;
        double x1 = min_x-quarter_wavelen;
        double x2 = max_x+quarter_wavelen;
        double y1 = min_y-quarter_wavelen;
        double y2 = max_y+quarter_wavelen;

        double xd = (x2-x1);
        double yd = (y2-y1);
        double zd = (z2-z1);
        double xc = (x2+x1)/2;
        double yc = (y2+y1)/2;
        double zc = (z2+z1)/2;


        gl.glPushMatrix();
        gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
        gl.glTranslated(xc, zc, yc);


        /////////////////////////////////////////////////////////////////////////////////////////////////
        //regular mesh with required resolution  e.g. resolution = c0 / fmax / sqr_epr / unit / 12.0;
        /////////////////////////////////////////////////////////////////////////////////////////////////
        gl.glBegin(gl.GL_LINES);

        double pml=0.0;
        if(do_pml_display) pml = 1.0;

        int xcells = (int) (( xd / resolution ) + (2.0 * n_pml_cells * pml));
        if((xcells&0x01)==1) xcells++;
        int ycells = (int) (( yd / resolution ) + (2.0 * n_pml_cells * pml));
        if((ycells&0x01)==1) ycells++;
        int zcells = (int) (( zd / resolution ) + (2.0 * n_pml_cells * pml));
        if((zcells&0x01)==1) zcells++;


        double x_st = (xcells/2) * resolution * -1.0;
        double y_st = (ycells/2) * resolution * -1.0;
        double z_st = (zcells/2) * resolution * -1.0;
        double x_end = (xcells/2) * resolution;
        double y_end = (ycells/2) * resolution;
        double z_end = (zcells/2) * resolution;

        xlines_v = new Vector();
        ylines_v = new Vector();
        zlines_v = new Vector();
        xcells_v = new Vector();
        ycells_v = new Vector();
        zcells_v = new Vector();
        edge_xcells_v = new Vector();
        edge_ycells_v = new Vector();
        edge_zcells_v = new Vector();

        Hashtable xe = new Hashtable();
        Hashtable ye = new Hashtable();


        //constant z length
        for(int i=0; i<=xcells; i++) {
          for(int j=0; j<=ycells; j++) {
            meshLine meshline = new meshLine( x_st +i*resolution, z_st, y_st+j*resolution,
                                              x_st +i*resolution, z_end, y_st+j*resolution );
            zcells_v.addElement(meshline);
          }
        }

        //constant y length
        for(int i=0; i<=xcells; i++) {
          for(int j=0; j<=zcells; j++) {
            meshLine meshline = new meshLine ( x_st +i*resolution, z_st+j*resolution, y_st,
                                               x_st +i*resolution, z_st+j*resolution, y_end );
            ycells_v.addElement(meshline);
          }
        }

        //constant x length
        for(int i=0; i<=zcells; i++) {
          for(int j=0; j<=ycells; j++) {
            meshLine meshline = new meshLine( x_st, z_st +i*resolution, y_st+j*resolution,
                                              x_end, z_st +i*resolution, y_st+j*resolution );
            xcells_v.addElement(meshline);
          }
        }

        Enumeration <pcb_object> po = pcbmodel.getPCBObjectVector().elements();

        /////////////////////////////////////////////////////////////////////////////////////
        //detect structure edges where higher detail is required for materials
        //TODO still need to weed out regular grid lines that fall close to detected edges
        /////////////////////////////////////////////////////////////////////////////////////
        while(po.hasMoreElements()) {
          pcb_object pobj = po.nextElement();
          Enumeration <Point2D.Double> e = pobj.getGeometry();
          if( e!=null) {
            while(e.hasMoreElements()) {
              Point2D.Double p = e.nextElement();
              double x = p.x;
              double y = p.y;
              if(xe.get(String.format("%4.4f", new Double(y).doubleValue()))==null) {
                if(pobj.getMaterial().getMaterialType() != Material.COPPER_VIA) {

                  double wl_dist = 1.0 / (Double.valueOf(simulation.fdtd_fmax) * 2.5e-6);

                  if(simulation.do_dual_edge_mesh) {
                    /* 2 lines */
                    xlines_v.addElement(new Double(y+wl_dist));
                    xlines_v.addElement(new Double(y-wl_dist));
                  } else {
                    //*1 lines */
                    xlines_v.addElement(new Double(y));
                  }

                  /* */
                  //double yy1 = pobj.getMaterial().via_sy+0.0005;
                  //double yy2 = pobj.getMaterial().via_sy-0.0005;
                  //xlines_v.addElement(yy1);
                  //xlines_v.addElement(yy2);
                  xe.put( String.format("%4.4f", new Double(y).doubleValue()), "" );
                } else {
                  xlines_v.addElement(new Double(y));
                }
                xe.put( String.format("%4.4f", new Double(y).doubleValue()), "" );
              }
              if(ye.get(String.format("%4.4f", new Double(x).doubleValue()))==null) {
                if(pobj.getMaterial().getMaterialType() != Material.COPPER_VIA) {

                  double wl_dist = 1.0 / (Double.valueOf(simulation.fdtd_fmax) * 2.5e-6);
                  if(simulation.do_dual_edge_mesh) {
                    /* 2 lines */
                    ylines_v.addElement(new Double(x+wl_dist));
                    ylines_v.addElement(new Double(x-wl_dist));
                  } else {
                    /* 1 line */
                    ylines_v.addElement(new Double(x));
                  }
                  /* */
                  //double xx1 = pobj.getMaterial().via_sx+0.0005;
                  //double xx2 = pobj.getMaterial().via_sx-0.0005;
                  //ylines_v.addElement(xx1);
                  //ylines_v.addElement(xx2);
                  ye.put( String.format("%4.4f", new Double(x).doubleValue()), "" );
                } else {
                  ylines_v.addElement(new Double(x));
                }
              }

              int do_all = 0;
              //if( pcbmodel.isOrtho() ) do_all = ycells;

              //don't add duplicate edges
              //if( xe.get( String.format("4.6f", new Double(y).doubleValue())) == null ){
              for(int i=0; i<=zcells; i++) {
                for(int j=0; j<=do_all; j++) {
                  meshLine meshline = new meshLine( x_st+xc, z_st +i*resolution, y,
                                                    x_end+xc, z_st +i*resolution, y);
                  edge_xcells_v.addElement(meshline);
                }
              }
              // xe.put( String.format("4.6f", new Double(y).doubleValue()), "" );
              //}

              do_all = 0;
              //if( pcbmodel.isOrtho() ) do_all = xcells;

              //don't add duplicate edges
              //if( ye.get(String.format("4.6f", new Double(x).doubleValue())) == null ) {
              for(int i=0; i<=do_all; i++) {
                for(int j=0; j<=zcells; j++) {
                  meshLine meshline = new meshLine ( x, z_st+j*resolution, y_st+yc,
                                                     x, z_st+j*resolution, y_end+yc );
                  edge_ycells_v.addElement(meshline);
                }
              }
              // ye.put( String.format("4.6f", new Double(x).doubleValue()), "" );
              //}


            }
          }
          //found dielectric substrate, get z-lines
          if(pobj.getMaterial().getMaterialType() == Material.DIELECTRIC_BOX) {
            double center = pobj.getMaterial().box_zc;
            double delta = pobj.getMaterial().box_zd;
            //TODO: make these values line up with simulation parameters
            double top = center+delta/2.0;
            double bottom = center-delta/2.0;
            double m1 = center+delta/4.0;
            double m2 = center-delta/4.0;

            //3-4 cells through substrate thickness is recommended
            //for(int j=0; j<ycells;j++) {
            int j=0;

            //meshLine meshline = new meshLine( x_st+xc, top+35e-6, y_st+yc+j*resolution,
            //edge_zcells_v.addElement(meshline);
            //zlines_v.addElement(new Double(top+35e-6));


            meshLine meshline = new meshLine( x_st+xc, top, y_st+yc+j*resolution,
                                              x_end+xc, top, y_st+yc+j*resolution);
            edge_zcells_v.addElement(meshline);
            zlines_v.addElement(new Double(top));

            meshline = new meshLine( x_st+xc, bottom, y_st+yc+j*resolution,
                                     x_end+xc, bottom, y_st+yc+j*resolution);
            edge_zcells_v.addElement(meshline);
            zlines_v.addElement(new Double(bottom));

            meshline = new meshLine( x_st+xc, m1, y_st+yc+j*resolution,
                                     x_end+xc, m1, y_st+yc+j*resolution);
            edge_zcells_v.addElement(meshline);
            zlines_v.addElement(new Double(m1));

            meshline = new meshLine( x_st+xc, m2, y_st+yc+j*resolution,
                                     x_end+xc, m2, y_st+yc+j*resolution);
            edge_zcells_v.addElement(meshline);
            zlines_v.addElement(new Double(m2));
            //}
          }
          /*
                    //found copper
                    if(pobj.getMaterial().getMaterialType() == Material.COPPER_POLYGON) {
                      double elevation = new Double(pobj.getMaterial().getElevation()).doubleValue();
                      int j=0;
                      //for(int j=0; j<ycells;j++) {
                        meshLine meshline = new meshLine( x_st+xc, elevation, y_st+yc+(double)j*resolution,
                                                          x_end+xc, elevation, y_st+yc+(double)j*resolution);
                        edge_zcells_v.addElement(meshline);
                      //}
                    }
          */
        }

        for(int i=0; i<=zcells; i++) {
          if(checkDistance(zlines_v, center_z+z_st+i*resolution)) {
            zlines_v.addElement(new Double(center_z+z_st+i*resolution));
          }
        }
        for(int i=0; i<=ycells; i++) {
          if(xe.get(String.format("%4.4f", new Double(center_y+y_st+i*resolution).doubleValue()))==null) {
            if(checkDistance(xlines_v, center_y+y_st+i*resolution)) {
              xlines_v.addElement(new Double(center_y+y_st+i*resolution));
            }
          }
        }
        for(int i=0; i<=xcells; i++) {
          if(ye.get(String.format("%4.4f", new Double(center_x+x_st+i*resolution).doubleValue()))==null) {
            if(checkDistance(ylines_v, center_x+x_st+i*resolution)) {
              ylines_v.addElement(new Double(center_x+x_st+i*resolution));
            }
          }
        }

        Enumeration<meshLine> ex = xcells_v.elements();
        Enumeration<meshLine> ey = ycells_v.elements();
        Enumeration<meshLine> ez = zcells_v.elements();

        //draw regular grid
        while(ex.hasMoreElements()) {
          meshLine ml = ex.nextElement();
          gl.glVertex3d(ml.st.x,ml.st.y,ml.st.z);
          gl.glVertex3d(ml.end.x,ml.end.y,ml.end.z);
        }
        while(ey.hasMoreElements()) {
          meshLine ml = ey.nextElement();
          gl.glVertex3d(ml.st.x,ml.st.y,ml.st.z);
          gl.glVertex3d(ml.end.x,ml.end.y,ml.end.z);
        }
        while(ez.hasMoreElements()) {
          meshLine ml = ez.nextElement();
          gl.glVertex3d(ml.st.x,ml.st.y,ml.st.z);
          gl.glVertex3d(ml.end.x,ml.end.y,ml.end.z);
        }
        gl.glEnd();
        gl.glPopAttrib();
        gl.glPopMatrix();

        //back to pcb transformation, draw detected edges
        Enumeration<meshLine> e_ex = edge_xcells_v.elements();
        Enumeration<meshLine> e_ey = edge_ycells_v.elements();
        Enumeration<meshLine> e_ez = edge_zcells_v.elements();


        gl.glPushMatrix();
        gl.glLineWidth(1.0f);
        gl.glBegin(gl.GL_LINES);
        while(e_ez.hasMoreElements()) {
          //draw z-lines
          meshLine ml = e_ez.nextElement();
          gl.glVertex3d(ml.st.x,ml.st.y,ml.st.z);
          gl.glVertex3d(ml.end.x,ml.end.y,ml.end.z);
        }
        gl.glEnd();
        gl.glPopMatrix();

        e_ez = edge_zcells_v.elements();
        while(e_ez.hasMoreElements()) {
          meshLine ml = e_ez.nextElement();
          double height = ml.st.y;

          ////////////////////////////////////////////////////////////////////////////
          // the x/y high-detail mesh should only extend through high-detail z-lines?
          //draw detected edges required for accurate simulation of structure
          ////////////////////////////////////////////////////////////////////////////
          gl.glPushMatrix();
          gl.glLineWidth(1.0f);
          gl.glBegin(gl.GL_LINES);
          gl.glColor4d(1.0, 1.0, 1.0, 0.3);
          e_ex = edge_xcells_v.elements();
          while(e_ex.hasMoreElements()) {
            ml = e_ex.nextElement();
            gl.glVertex3d(ml.st.x, height, ml.st.z);
            gl.glVertex3d(ml.end.x, height, ml.end.z);
          }
          gl.glEnd();
          gl.glPopMatrix();

          gl.glPushMatrix();
          gl.glLineWidth(1.0f);
          gl.glBegin(gl.GL_LINES);
          gl.glColor4d(1.0, 1.0, 1.0, 0.3);
          e_ey = edge_ycells_v.elements();
          while(e_ey.hasMoreElements()) {
            ml = e_ey.nextElement();
            gl.glVertex3d(ml.st.x, height, ml.st.z);
            gl.glVertex3d(ml.end.x, height, ml.end.z);
          }
          gl.glEnd();
          gl.glPopMatrix();
        }

        int totx = xcells + xe.size();
        int toty = ycells + ye.size();
        int totz = zcells + 4;
        //System.out.println("total cells: "+totx*toty*totz);
        //System.out.println("xcells_v: "+xcells_v.size());
        //System.out.println("ycells_v: "+ycells_v.size());
        //System.out.println("zcells_v: "+zcells_v.size());
        //System.out.println("e_xcells_v: "+edge_xcells_v.size());
        //System.out.println("e_ycells_v: "+edge_ycells_v.size());
        //System.out.println("e_zcells_v: "+edge_zcells_v.size());

      }
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      gl.glPopAttrib();
      gl.glPopMatrix();
    }
  }

  public int getNumberXLines()
  {
    return ylines_v.size();
  }

  public String getXLines()
  {
    String xlines = "";

    Enumeration<Double> x = ylines_v.elements();
    while(x.hasMoreElements()) {
      double d = x.nextElement().doubleValue();
      xlines = xlines.concat( d+ "," );
    }

    return xlines;
  }

  public int getNumberYLines()
  {
    return xlines_v.size();
  }

  public String getYLines()
  {
    String ylines = "";
    Enumeration<Double> y = xlines_v.elements();
    while(y.hasMoreElements()) {
      double d = y.nextElement().doubleValue();
      ylines = ylines.concat( d+ "," );
    }

    return ylines;
  }

  public int getNumberZLines()
  {
    return zlines_v.size();
  }

  public boolean checkDistance( Vector v, double d)
  {
    Enumeration<Double> e = v.elements();
    while(e.hasMoreElements()) {
      double d1 = e.nextElement().doubleValue();
      double dist = Math.abs(d1-d);
      //if(dist<0.01) System.out.println("dist: "+dist);
      double wl_dist = 1.0 / (Double.valueOf(simulation.fdtd_fmax) * 5e-7);
      if(dist< wl_dist) return false;
    }
    return true;
  }

  public String getZLines()
  {
    String zlines = "";
    Enumeration<Double> z = zlines_v.elements();
    while(z.hasMoreElements()) {
      double d = z.nextElement().doubleValue();
      zlines = zlines.concat( d+ "," );
    }

    return zlines;
  }


}
