
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

import java.io.*;
import java.util.*;

public class dft
{

  public double[][] get_sparm2p(dft DFT, Simulation simulation, double start_freq, double end_freq, double port1_ref, double port2_ref)
  {

    double[][] sparms = null;

    Complex[] port1_u = DFT.get_fft(simulation.sim_path+simulation.sim_name+"/port_ut1");
    Complex[] port2_u = DFT.get_fft(simulation.sim_path+simulation.sim_name+"/port_ut2");
    Complex[] port1_i = DFT.get_fft(simulation.sim_path+simulation.sim_name+"/port_it1");
    Complex[] port2_i = DFT.get_fft(simulation.sim_path+simulation.sim_name+"/port_it2");


    int len = port1_u.length;
    if(port1_i.length < len) len = port1_i.length;
    if(port2_u==null || port2_u.length ==0) port2_u = port1_u;
    if(port2_i==null || port2_i.length ==0) port2_i = port1_i;

    Complex[] u_f1 = new Complex[len];
    Complex[] u_f2 = new Complex[len];
    Complex[] i_f1 = new Complex[len];
    Complex[] i_f2 = new Complex[len];

    Complex[] uf_inc1 = new Complex[len];
    Complex[] uf_inc2 = new Complex[len];
    Complex[] uf_ref1 = new Complex[len];
    Complex[] uf_ref2 = new Complex[len];

    Complex[] if_inc1 = new Complex[len];
    Complex[] if_inc2 = new Complex[len];
    Complex[] if_ref1 = new Complex[len];
    Complex[] if_ref2 = new Complex[len];

    double[] s21 = new double[len];
    double[] s11 = new double[len];
    double[] z1 = new double[len];

    sparms = new double[7][len];

    if( port1_u!=null && port2_u!=null && port1_i!=null && port2_i!=null) {
      for(int i=0; i<len; i++) {
        u_f1[i] = port1_u[i];
        u_f2[i] = port2_u[i];

        i_f1[i] = port1_i[i];
        i_f2[i] = port2_i[i];

        Complex zrefp1 = new Complex(port1_ref,0);  //most likely 50 ohms
        Complex zrefp2 = new Complex(port2_ref,0);  //most likely 50 ohms
        Complex scale = new Complex(0.5,0);

        uf_inc1[i] =  (u_f1[i].plus(i_f1[i].times(zrefp1))).times(scale);
        uf_inc2[i] =  (u_f2[i].plus(i_f2[i].times(zrefp2))).times(scale);
        if_inc1[i] =  (i_f1[i].plus(u_f1[i].div(zrefp1))).times(scale);
        if_inc2[i] =  (i_f2[i].plus(u_f2[i].div(zrefp2))).times(scale);

        uf_ref1[i] = u_f1[i].minus(uf_inc1[i]);
        uf_ref2[i] = u_f2[i].minus(uf_inc2[i]);

        if_ref1[i] = if_inc1[i].minus(i_f1[i]);
        if_ref2[i] = if_inc2[i].minus(i_f2[i]);

        s21[i] = 20.0 * Math.log10( uf_ref2[i].div(uf_inc1[i]).mod() );
        s11[i] = 20.0 * Math.log10( uf_ref1[i].div(uf_inc1[i]).mod() );

        sparms[0][i] = s21[i];  //20log10(abs(s21))
        sparms[1][i] = s11[i];  //20log10(abs(s11))
        sparms[2][i] = u_f1[i].div(i_f1[i]).mod(); //impedance

        sparms[3][i] = uf_ref1[i].div(uf_inc1[i]).real(); //s11 real
        sparms[4][i] = uf_ref1[i].div(uf_inc1[i]).imag(); //s11 imag
        sparms[5][i] = uf_ref2[i].div(uf_inc1[i]).real(); //s21 real
        sparms[6][i] = uf_ref2[i].div(uf_inc1[i]).imag(); //s21 imag

      }
    }

    return sparms;
  }

  public Complex[] get_fft(String filename)
  {
    Complex[] fft_ret = null;
    try {
      BufferedReader br = new BufferedReader( new FileReader(filename) );
      String line="";
      int nlines=0;

      while(br!=null && line!=null) {
        line = br.readLine();
        if(line!=null && line.trim().length()>0) {
          StringTokenizer st = new StringTokenizer(line.trim());
          if(st.countTokens()==2) nlines++;
        }
      }
      //System.out.println(nlines);
      if(nlines==0) return null;

      int out_len = nlines;

      int power_of_two = 0;
      int n=1;
      while(power_of_two < nlines) {
        power_of_two = n;
        n *= 2;
      }

      power_of_two = power_of_two * 64;

      double scale_factor = (double) power_of_two / (double) out_len / ((double) 2 * (double) 64);

      //System.out.println("scale_factor: "+scale_factor);

      double[] time = new double[power_of_two];
      double[] real = new double[power_of_two];
      double[] imag = new double[power_of_two];
      //double[] out_real = new double[power_of_two];
      //double[] out_imag = new double[power_of_two];

      br = new BufferedReader( new FileReader(filename) );
      nlines=0;
      line="";


      while(line!=null) {
        line = br.readLine();
        if(line!=null && line.trim().length()>0) {
          try {
            StringTokenizer st = new StringTokenizer(line.trim());
            //System.out.println(st.countTokens());
            if(st.countTokens()==2) {
              double t = Double.valueOf(st.nextToken());
              double val = Double.valueOf(st.nextToken());
              time[nlines] = t;
              real[nlines] = val;
              imag[nlines] = 0;
              nlines++;
            }
          } catch(Exception e) {
          }
        }
      }

      double dt = time[2]-time[1];

      //computeDft(real,imag,out_real,out_imag);
      fft_ret = _fft(real,imag,true, (int)((double) out_len*(double) scale_factor));

    } catch(Exception e) {
    }

    return fft_ret;
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// http://www.wikijava.org/wiki/The_Fast_Fourier_Transform_in_Java_(part_1)
//
// * @author Orlando Selenu
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public static Complex[] _fft(final double[] inputReal, double[] inputImag, boolean DIRECT, int out_len)
  {
    // - n is the dimension of the problem
    // - nu is its logarithm in base e
    int n = inputReal.length;

    // If n is a power of 2, then ld is an integer (_without_ decimals)
    double ld = Math.log(n) / Math.log(2.0);

    // Here I check if n is a power of 2. If exist decimals in ld, I quit
    // from the function returning null.
    if (ld - ld != 0) {
      //System.out.println("The number of elements is not a power of 2.");
      return null;
    }

    // Declaration and initialization of the variables
    // ld should be an integer, actually, so I don't lose any information in
    // the cast
    int nu = (int) ld;
    int n2 = n / 2;
    int nu1 = nu - 1;
    double[] xReal = new double[n];
    double[] xImag = new double[n];
    double tReal, tImag, p, arg, c, s;

    // Here I check if I'm going to do the direct transform or the inverse
    // transform.
    double constant;
    if (DIRECT)
      constant = -2 * Math.PI;
    else
      constant = 2 * Math.PI;

    // I don't want to overwrite the input arrays, so here I copy them. This
    // choice adds \Theta(2n) to the complexity.
    for (int i = 0; i < n; i++) {
      xReal[i] = inputReal[i];
      xImag[i] = inputImag[i];
    }

    // First phase - calculation
    int k = 0;
    for (int l = 1; l <= nu; l++) {
      while (k < n) {
        for (int i = 1; i <= n2; i++) {
          p = bitreverseReference(k >> nu1, nu);
          // direct FFT or inverse FFT
          arg = constant * p / n;
          c = Math.cos(arg);
          s = Math.sin(arg);
          tReal = xReal[k + n2] * c + xImag[k + n2] * s;
          tImag = xImag[k + n2] * c - xReal[k + n2] * s;
          xReal[k + n2] = xReal[k] - tReal;
          xImag[k + n2] = xImag[k] - tImag;
          xReal[k] += tReal;
          xImag[k] += tImag;
          k++;
        }
        k += n2;
      }
      k = 0;
      nu1--;
      n2 /= 2;
    }

    // Second phase - recombination
    k = 0;
    int r;
    while (k < n) {
      r = bitreverseReference(k, nu);
      if (r > k) {
        tReal = xReal[k];
        tImag = xImag[k];
        xReal[k] = xReal[r];
        xImag[k] = xImag[r];
        xReal[r] = tReal;
        xImag[r] = tImag;
      }
      k++;
    }

    // Here I have to mix xReal and xImag to have an array (yes, it should
    // be possible to do this stuff in the earlier parts of the code, but
    // it's here to readibility).
    //Complex[] newArray = new Complex[xReal.length];
    Complex[] newArray = new Complex[out_len];
    double radice = 1 / Math.sqrt(n);
    for (int i = 0; i < newArray.length; i++) {
      // I used Stephen Wolfram's Mathematica as a reference so I'm going
      // to normalize the output while I'm copying the elements.
      newArray[i] = new Complex(xReal[i] * radice, xImag[i] * radice);
    }
    return newArray;
  }

  /**
   * The reference bitreverse function.
   */
  private static int bitreverseReference(int j, int nu)
  {
    int j2;
    int j1 = j;
    int k = 0;
    for (int i = 1; i <= nu; i++) {
      j2 = j1 / 2;
      k = 2 * k + j1 - 2 * j2;
      j1 = j2;
    }
    return k;
  }

}
