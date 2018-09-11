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

import java.util.Arrays;
import java.util.List;
import java.awt.*;
import java.text.*;

import javax.swing.JPanel;
import javax.swing.JFrame;

public class sparm_chart extends JPanel implements Runnable
{

  dft DFT;
  Simulation simulation;
  double start_freq = 0;
  double stop_freq = 3.1e9;
  double port1_ref=50;
  double port2_ref=50;
  double[] freq_mhz=null;
  boolean do_write_1p;
  boolean do_write_2p;
  boolean do_update_now=false;
  smithChartPanel smith_panel;
  sparmChartPanel sparm_panel;



  public sparm_chart(Simulation simulation, smithChartPanel smch, sparmChartPanel sparmch)
  {
    this.smith_panel = smch;
    this.sparm_panel = sparmch;
    this.simulation = simulation;
    this.stop_freq = Double.valueOf(simulation.fdtd_fmax) * Double.valueOf(simulation.sparm_chart_fft_stretch_factor);
    this.port1_ref= Double.valueOf(simulation.port1_resistance);
    this.port2_ref= Double.valueOf(simulation.port2_resistance);
    DFT = new dft();
  }

  public void setSimulation(Simulation sim)
  {
    this.simulation = sim;
  }

  public void updateNow()
  {
    do_update_now=true;
  }

  public void run()
  {
    while(true) {
      try {
        do_update_now=false;

        simulation.fftUpdateStarted();

        this.stop_freq = Double.valueOf(simulation.fdtd_fmax) * Double.valueOf(simulation.sparm_chart_fft_stretch_factor);
        this.start_freq = 0;
        this.port1_ref= Double.valueOf(simulation.port1_resistance);
        this.port2_ref= Double.valueOf(simulation.port2_resistance);

        double[][] sparms = DFT.get_sparm2p(DFT, simulation, start_freq, stop_freq, port1_ref, port2_ref);
        double delta_freq = (stop_freq-start_freq)/sparms[0].length;

        simulation.setStatus("Processing FFT on TD data...");
        update_dataset( sparms, delta_freq );

        if( do_write_2p && simulation!=null && simulation.do_touchstone_output) {
          simulation.setStatus("Updating touchstone files...");
          do_write_2p=false;
          if( freq_mhz!=null && sparms!=null ) {
            writeTouchStone wts = new writeTouchStone(simulation);
            wts.write2p(freq_mhz, sparms);
          }
        }
        if( do_write_1p && simulation!=null && simulation.do_touchstone_output) {
          do_write_1p=false;
          simulation.setStatus("Updating touchstone files...");
          if( freq_mhz!=null && sparms!=null ) {
            writeTouchStone wts = new writeTouchStone(simulation);
            wts.write1p(freq_mhz, sparms);
          }
        }

          simulation.setStatus("");
          simulation.fftUpdateCompleted();

      } catch(Exception e) {
        //e.printStackTrace();
      }


      try {
        int count=100;
        for(int i=0; i<count; i++) {
          Thread.sleep( new Double(simulation.sparm_update_freq_sec).intValue() * 10);
          if(do_update_now) break;
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void update_dataset(double[][] sparms, double delta_freq)
  {

    if(sparms!=null) {
      do_update_now=false;

      boolean do_both=false;
      if(openEMSWriter.getPortCount(simulation) > 1) do_both=true;

      if(do_both) {
        do_write_2p=true;

        if(sparms!=null) {
          freq_mhz = new double[sparms[0].length];

          for(int i=0; i<sparms[0].length; i++) {
            double x = (start_freq+(double)i*delta_freq)/1.0e9;
            freq_mhz[i] = x * 1.0e3;//to mhz
          }
        }

        sparm_panel.plotS11_S21(freq_mhz, sparms);
        smith_panel.plotS11_S21(freq_mhz, sparms);
      } else {
        do_write_1p=true;

        if(sparms!=null) {
          freq_mhz = new double[sparms[0].length];
          for(int i=0; i<sparms[0].length; i++) {
            double x = (start_freq+i*delta_freq)/1e9;
            freq_mhz[i] = x * 1.0e3;//to mhz
          }
        }

        sparm_panel.plotS11(freq_mhz, sparms);
        smith_panel.plotS11(freq_mhz, sparms);
      }

    }
  }

}
