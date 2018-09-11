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


import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.lang.Math.*;

public class sparmChartPanel extends JPanel
{

  int w;
  int h;
  int hw;
  int hh;
  int cx;
  int cy;
  double[][] data;
  double freq_mhz[];
  boolean draw_s11=false;
  boolean draw_s21=false;
  boolean draw_zin=false;
  double scale_factor = 0.85;
  Color bg;
  Color fg;
  Color line_color;
  Color vswr_color;
  boolean do_antialias=false;
  double inset1 = 100.0;
  double inset2 = inset1*2.0;
  int inseth = (int) (inset1/4.0);
  int inseth2 = (int) (inset1/2.0);
  int inset3 = (int) (inset1);
  int inset4 = (int) (inset1*2);
  Line2D.Double l2d=null;

  public sparmChartPanel()
  {
    line_color = new Color(210,210,210);
    fg = Color.black;
    bg = new Color(200,200,200);
    vswr_color = Color.blue;
    do_antialias=true;
    setPreferredSize( new Dimension(1024,768) );
  }

  public void plotS11_Zin(double freq[], double[][] ri)
  {
    if(freq==null || freq.length==0) return;

    freq_mhz = freq;
    data = ri;
    draw_s11=true;
    draw_zin=true;
    draw_s21=false;
    repaint();
  }
  public void plotS11(double freq[], double[][] ri)
  {
    if(freq==null || freq.length==0) return;
    freq_mhz = freq;
    data = ri;
    draw_s11=true;
    draw_s21=false;
    repaint();
  }
  public void plotS11_S21(double freq[], double[][] ri)
  {
    if(freq==null || freq.length==0) return;

    freq_mhz = freq;
    data = ri;
    draw_s11=true;
    draw_s21=true;
    repaint();
  }

  public void paint(Graphics g)
  {
    Graphics2D g2d = (Graphics2D) g;
    Font font = new Font("Arial", Font.PLAIN, 18);
    g2d.setFont(font);

    if(do_antialias) {
      g2d.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }


    w = getWidth();
    h = getHeight();
    //h = (int) (0.75*(double)w);
    //w = (int)((double)h*1.33);

    cx = w/2;
    cy = h/2;
    hw = w/2;
    hh = h/2;

    //clear background
    g2d.setColor( bg );
    g2d.fillRect( 0,0,getWidth(),getHeight());

    g2d.setColor( Color.white );
    BasicStroke bs = new BasicStroke((float) inset1);
    g2d.setStroke(bs);
    Rectangle2D.Double bgr = new Rectangle2D.Double(inseth*2.0,inseth*2.0,w-inseth2*2.0,h-inseth2*2.0);
    g2d.draw(bgr);
    bs = new BasicStroke(2.0f);
    g2d.setStroke(bs);

    //draw inner border line
    g2d.setColor( Color.black );
    bgr = new Rectangle2D.Double(inset3,inset3,w-inset4,h-inset4);
    g2d.draw(bgr);

    //draw outer border line
    g2d.setColor( Color.black );
    bgr = new Rectangle2D.Double(1,1,w-2,h-2);
    g2d.draw(bgr);


    double yt = inset1;
    double yb = h-inset1;
    double stepy = (yb-yt)/20.0;
    double yst = yt+stepy;


    double hl = inset1;
    double hr = w-inset1;
    double stepx = ((double) hr-(double) hl)/10.0;
    double xst = (double) hl+(double) stepx;

    if(data==null || data.length==0) return;


    //find min in both datasets
    double min = 0.0;
    int len = freq_mhz.length;
    if(draw_s11 && draw_s21) {
      for(int j=0; j<len; j++) {
        if(data[0][j]<min) min = data[0][j];
      }
      for(int j=0; j<len; j++) {
        if(data[1][j]<min) min = data[1][j];
      }
    } else if(draw_s11) {
      for(int j=0; j<len; j++) {
        if(data[1][j]<min) min = data[1][j];
      }
    }


    //auto-range y-axis for dB mag
    double modval = min;
    if(modval > -0.5) modval = 1.0;
    else if(modval > -40.0) modval = 2.0;
    else if(modval > -80.0) modval = 5.0;
    else modval = 10.0;

    double modmin = (min-modval)%modval;
    modmin = min-modmin;
    modmin -= modval;
    double modstep = modval;

    //insertion loss / return loss
    font = new Font("Arial", Font.PLAIN, 12);
    g2d.setFont(font);
    bs = new BasicStroke(1.0f);
    g2d.setPaint(Color.black);

    yst = 0.0;
    double magdb = 0.0;
    int nsteps = (int) Math.abs((modmin/modstep));
    stepy = (yb-yt)/(double)nsteps-1;
    yst = yt+6;
    for( int x=0; x<nsteps+1; x++) {
      g2d.setPaint(Color.black);
      g2d.drawString(String.format("%3.1f",magdb), 60, (int)yst);

      g2d.setPaint(line_color);
      l2d = new Line2D.Double( (double) inset1+5.0, (double) yst, (double) (w-inset1)-5.0, (double) yst);
      g2d.draw(l2d);

      yst += stepy;
      magdb -= modstep;

    }

    int n_xtick=10;
    if( getWidth()>1024 ) n_xtick=20;
    stepx = (hr-hl)/(double)n_xtick;

    //draw grid lines
    for( int y=0; y<n_xtick-1; y++) {
      l2d = new Line2D.Double( (double) xst, (double) yt+5.0, (double) xst, (double) yb-5.0);
      g2d.setPaint(line_color);
      g2d.draw(l2d);
      xst += stepx;
    }

    //draw x / freq axis
    xst = hl-20.0;
    double fst = freq_mhz[0];
    double fend = freq_mhz[freq_mhz.length-1];
    double fstep = (fend-fst)/(double)n_xtick;
    //frequency
    for( int y=0; y<n_xtick+1; y++) {
      g2d.setPaint(Color.black);
      g2d.drawString(String.format("%3.3f",(fst+fstep*(double)y)/1e3), (int) xst, (int)(yb+20));
      xst+=stepx;
    }


    //draw lines last
    if(draw_s11) {
      double px=0.0;
      double py=0.0;

      bs = new BasicStroke(1.0f);
      g2d.setStroke(bs);

      g2d.setPaint(fg);
      font = new Font("Arial", Font.PLAIN, 22);
      g2d.setFont(font);
      g2d.drawString("S-Parameters",hw-70,75);
      g2d.drawString("Frequency (GHz)",hw-100,h-inseth);

      AffineTransform atorg = g2d.getTransform();

      AffineTransform at = new AffineTransform();
      if(draw_s11 && draw_s21) {
        at.translate(30,cy+150);
        at.rotate(Math.toRadians(-90));
        g2d.transform(at);
        g2d.drawString("Insertion Loss / Return Loss (dB)", 0, 0);
      } else if(draw_s11) {
        at.translate(30,cy+50);
        at.rotate(Math.toRadians(-90));
        g2d.transform(at);
        g2d.drawString("Return Loss (dB)", 0, 0);
      }

      g2d.setTransform(atorg);

      font = new Font("Arial", Font.PLAIN, 12);
      g2d.setFont(font);

      g2d.setPaint(Color.blue);
      g2d.drawString("S11 _____", 50,50);



      for(int i=0; i<len; i++) {
        double mag = data[1][i];

        //slope = (ah - al) / (mh-ml);
        //intercept = ((ah+al)/2.0) - (slope * ((mh+ml)/2.0));

        double slope = (yt-yb) / (0.0-modmin);
        double intercept = ((yt+yb)/2.0) - (slope * ((0.0+modmin)/2.0));

        mag *= slope;
        mag += intercept;

        double x = inset1 + (freq_mhz[i]/freq_mhz[len-1]) * (w-inset2);
        double y = mag;

        if(px>0) {
          l2d = new Line2D.Double( (double) px, (double) py, (double) x, (double) y);
          g2d.draw(l2d);
        }
        px = x;
        py = y;
      }
    }

    if(draw_s21) {
      len = freq_mhz.length;
      double px=0.0;
      double py=0.0;

      bs = new BasicStroke(1.0f);
      g2d.setStroke(bs);

      g2d.setPaint(fg);
      //g2d.drawString(String.format("Frequency Range: %1.1f MHz to %1.1f MHz",freq_mhz[0],freq_mhz[len-1]), 50,75);

      g2d.setPaint(Color.red);
      g2d.drawString("S21 _____", 50,75);


      for(int i=0; i<len; i++) {
        double mag = data[0][i];

        //slope = (ah - al) / (mh-ml);
        //intercept = ((ah+al)/2.0) - (slope * ((mh+ml)/2.0));

        double slope = (yt-yb) / (0.0-min);
        double intercept = ((yt+yb)/2.0) - (slope * ((0.0+min)/2.0));

        mag *= slope;
        mag += intercept;

        double x = inset1 + (freq_mhz[i]/freq_mhz[len-1]) * (w-inset2);
        double y = mag;

        if(px>0) {
          l2d = new Line2D.Double( (double) px, (double) py, (double) x, (double) y);
          g2d.draw(l2d);
        }
        px = x;
        py = y;
      }
    }



  }

}
