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

public class Simulation
{

  Properties prop;

  PCBSimClient parent;

//Defaults
  public String fdtd_fmax;
  public String fdtd_maxsteps;
  public String fdtd_oversample;
  public String fdtd_endlevel;

  public String excitation_waveform_type;
  public String excitation_f0;
  public String excitation_fc;
  public String boundary_condition_xmin;
  public String boundary_condition_xmax;
  public String boundary_condition_ymin;
  public String boundary_condition_ymax;
  public String boundary_condition_zmin;
  public String boundary_condition_zmax;

//FR-4.  These values were arrived at from lots of testing.  should be good defaults for FR-4
  public String pcb_prop_epsilon;
  public String pcb_prop_mue;
  public String pcb_prop_kappa;
  public String pcb_prop_plasmafreq;
  public String pcb_prop_lorentz_pole_freq;
  public String pcb_prop_relaxation_time;
  public String pcb_thickness_inches;

//TODO:
//need sparm chart variables here
//e.g. 1.033;
//adversly affecting dispersive match on Lorentz test.
  public double sparm_chart_fft_stretch_factor;
  public double sparm_update_freq_sec;
  public boolean do_touchstone_output;

//TODO:
  public boolean do_airbox;
  public boolean do_mesh;
  public boolean do_pml;
  public boolean do_dual_edge_mesh;
  public boolean do_box_vias;
  public String airbox_dist;
  public String mesh_resolution;
  public String mesh_pcb_z_lines;
  public String airbox_epsilon;
  public String airbox_mue;
  public String airbox_kapa;

  public int port_count;

  public String port1_excitation_voltage;
  public String port1_resistance;
  public String port1_capacitance;
  public String port1_inductance;
  public String port2_resistance;
  public String port2_capacitance;
  public String port2_inductance;

  public boolean do_dump_efield;
  public boolean do_dump_hfield;
  public boolean do_debug_pec;
  public boolean do_sim_verbose;
  public boolean do_lorentz;
  public boolean do_filter_edges;
  boolean do_fixed_threads;
  public String sim_threads;
  public String sim_path;
  public String sim_name;

  public Simulation()
  {
    prop = new Properties();
    reset_to_defaults();
    readConfig();
  }

  public Simulation(PCBSimClient client, String sim_name)
  {
    parent = client;
    prop = new Properties();
    reset_to_defaults();
    this.sim_name = sim_name;
    readConfig();
  }

  public void setStatus(String str) {
    parent.setStatus(str);
  }
  public void fftUpdateCompleted() {
    parent.fftCompleted();
  }
  public void fftUpdateStarted() {
    parent.fftStarted();
  }

  public void abort()
  {
    try {
      File file = new File(sim_path+"/"+sim_name);
      FileOutputStream fos = new FileOutputStream(file.toString()+"/ABORT");
      fos.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public File[] getProjectList()
  {
    File[] files=null;
    try {
      File file = new File(sim_path);
      files = file.listFiles();
    } catch(Exception e) {
    }
    return files;
  }

  public void readConfig()
  {
    try {
      File file = new File(sim_path+"/"+sim_name);
      if(!file.exists()) {
        file.mkdirs();
        writeConfig();
      }
      File create_file = new File( file.toString()+"/sim_configuration.cfg");
      if(!create_file.exists()) {
        writeConfig();
      }

      FileInputStream fis = new FileInputStream(create_file);
      prop.load(fis);

      try {
        fdtd_fmax= prop.getProperty("fdtd_fmax");
        fdtd_maxsteps= prop.getProperty("fdtd_maxsteps");
        fdtd_oversample= prop.getProperty("fdtd_oversample");
        fdtd_endlevel= prop.getProperty("fdtd_endlevel");
        excitation_waveform_type= prop.getProperty("excitation_waveform_type");
        excitation_f0= prop.getProperty("excitation_f0");
        excitation_fc= prop.getProperty("excitation_fc");
        boundary_condition_xmin= prop.getProperty("boundary_condition_xmin");
        boundary_condition_xmax= prop.getProperty("boundary_condition_xmax");
        boundary_condition_ymin= prop.getProperty("boundary_condition_ymin");
        boundary_condition_ymax= prop.getProperty("boundary_condition_ymax");
        boundary_condition_zmin= prop.getProperty("boundary_condition_zmin");
        boundary_condition_zmax= prop.getProperty("boundary_condition_zmax");
        pcb_prop_epsilon= prop.getProperty("pcb_prop_epsilon");
        pcb_prop_mue= prop.getProperty("pcb_prop_mue");
        pcb_prop_kappa= prop.getProperty("pcb_prop_kappa");
        pcb_prop_plasmafreq= prop.getProperty("pcb_prop_plasmafreq");
        pcb_prop_lorentz_pole_freq= prop.getProperty("pcb_prop_lorentz_pole_freq");
        pcb_prop_relaxation_time= prop.getProperty("pcb_prop_relaxation_time");
        pcb_thickness_inches= prop.getProperty("pcb_thickness_inches");
        //sparm_chart_fft_stretch_factor = Double.valueOf(prop.getProperty("sparm_chart_fft_stretch_factor"));
        sparm_chart_fft_stretch_factor = 1.0; 
        sparm_update_freq_sec = Double.valueOf(prop.getProperty("sparm_update_freq_sec"));
        do_touchstone_output = Boolean.valueOf(prop.getProperty("do_touchstone_output"));
        do_airbox = Boolean.valueOf(prop.getProperty("do_airbox"));
        do_mesh = Boolean.valueOf(prop.getProperty("do_mesh"));
        do_pml = Boolean.valueOf(prop.getProperty("do_pml"));
        do_dual_edge_mesh = Boolean.valueOf(prop.getProperty("do_dual_edge_mesh"));
        airbox_dist= prop.getProperty("airbox_dist");
        mesh_resolution= prop.getProperty("mesh_resolution");
        mesh_pcb_z_lines= prop.getProperty("mesh_pcb_z_lines");
        airbox_epsilon= prop.getProperty("airbox_epsilon");
        airbox_mue= prop.getProperty("airbox_mue");
        airbox_kapa= prop.getProperty("airbox_kapa");
        port1_excitation_voltage= prop.getProperty("port1_excitation_voltage");
        port1_resistance= prop.getProperty("port1_resistance");
        port1_capacitance= prop.getProperty("port1_capacitance");
        port1_inductance= prop.getProperty("port1_inductance");
        port2_resistance= prop.getProperty("port2_resistance");
        port2_capacitance= prop.getProperty("port2_capacitance");
        port2_inductance= prop.getProperty("port2_inductance");
        do_dump_efield = Boolean.valueOf(prop.getProperty("do_dump_efield"));
        do_dump_hfield = Boolean.valueOf(prop.getProperty("do_dump_hfield"));
        do_debug_pec = Boolean.valueOf(prop.getProperty("do_debug_pec"));
        do_sim_verbose = Boolean.valueOf(prop.getProperty("do_sim_verbose"));
        do_lorentz = Boolean.valueOf(prop.getProperty("do_lorentz"));
        do_fixed_threads = Boolean.valueOf(prop.getProperty("do_fixed_threads"));
        do_box_vias = Boolean.valueOf(prop.getProperty("do_box_vias"));
        do_filter_edges = Boolean.valueOf(prop.getProperty("do_filter_edges"));
        sim_threads= prop.getProperty("sim_threads");
        sim_name= prop.getProperty("sim_name");

      } catch(Exception e) {
        e.printStackTrace();
      }



    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  public void writeConfig()
  {
    try {
      File file = new File(sim_path+"/"+sim_name);
      if(!file.exists()) file.mkdirs();
      FileOutputStream fos = new FileOutputStream(file.toString()+"/sim_configuration.cfg");

      try {
        prop.setProperty("fdtd_fmax",fdtd_fmax);
        prop.setProperty("fdtd_maxsteps",fdtd_maxsteps);
        prop.setProperty("fdtd_oversample",fdtd_oversample);
        prop.setProperty("fdtd_endlevel",fdtd_endlevel);
        prop.setProperty("excitation_waveform_type",excitation_waveform_type);
        prop.setProperty("excitation_f0",excitation_f0);
        prop.setProperty("excitation_fc",excitation_fc);
        prop.setProperty("boundary_condition_xmin",boundary_condition_xmin);
        prop.setProperty("boundary_condition_xmax",boundary_condition_xmax);
        prop.setProperty("boundary_condition_ymin",boundary_condition_ymin);
        prop.setProperty("boundary_condition_ymax",boundary_condition_ymax);
        prop.setProperty("boundary_condition_zmin",boundary_condition_zmin);
        prop.setProperty("boundary_condition_zmax",boundary_condition_zmax);
        prop.setProperty("pcb_prop_epsilon",pcb_prop_epsilon);
        prop.setProperty("pcb_prop_mue",pcb_prop_mue);
        prop.setProperty("pcb_prop_kappa",pcb_prop_kappa);
        prop.setProperty("pcb_prop_plasmafreq",pcb_prop_plasmafreq);
        prop.setProperty("pcb_prop_lorentz_pole_freq",pcb_prop_lorentz_pole_freq);
        prop.setProperty("pcb_prop_relaxation_time",pcb_prop_relaxation_time);
        prop.setProperty("pcb_thickness_inches",pcb_thickness_inches);
        prop.setProperty("sparm_chart_fft_stretch_factor",Double.toString(sparm_chart_fft_stretch_factor));
        prop.setProperty("sparm_update_freq_sec",Double.toString(sparm_update_freq_sec));
        prop.setProperty("do_touchstone_output",Boolean.toString(do_touchstone_output));
        prop.setProperty("do_airbox",Boolean.toString(do_airbox));
        prop.setProperty("do_mesh",Boolean.toString(do_mesh));
        prop.setProperty("do_pml",Boolean.toString(do_pml));
        prop.setProperty("do_dual_edge_mesh",Boolean.toString(do_dual_edge_mesh));
        prop.setProperty("airbox_dist",airbox_dist);
        prop.setProperty("mesh_resolution",mesh_resolution);
        prop.setProperty("mesh_pcb_z_lines",mesh_pcb_z_lines);
        prop.setProperty("airbox_epsilon",airbox_epsilon);
        prop.setProperty("airbox_mue",airbox_mue);
        prop.setProperty("airbox_kapa",airbox_kapa);
        prop.setProperty("port1_excitation_voltage",port1_excitation_voltage);
        prop.setProperty("port1_resistance",port1_resistance);
        prop.setProperty("port1_capacitance",port1_capacitance);
        prop.setProperty("port1_inductance",port1_inductance);
        prop.setProperty("port2_resistance",port2_resistance);
        prop.setProperty("port2_capacitance",port2_capacitance);
        prop.setProperty("port2_inductance",port2_inductance);
        prop.setProperty("do_dump_efield",Boolean.toString(do_dump_efield));
        prop.setProperty("do_dump_hfield",Boolean.toString(do_dump_hfield));
        prop.setProperty("do_debug_pec",Boolean.toString(do_debug_pec));
        prop.setProperty("do_sim_verbose",Boolean.toString(do_sim_verbose));
        prop.setProperty("do_lorentz",Boolean.toString(do_lorentz));
        prop.setProperty("do_box_vias",Boolean.toString(do_box_vias));
        prop.setProperty("do_filter_edges",Boolean.toString(do_filter_edges));
        prop.setProperty("sim_threads",sim_threads);
        prop.setProperty("sim_name",sim_name);
        prop.setProperty("do_fixed_threads",Boolean.toString(do_fixed_threads));
      } catch(Exception e) {
        e.printStackTrace();
      }

      prop.store(fos, null);
      fos.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void reset_to_defaults()
  {
    fileLocationDiaglog fld = new fileLocationDiaglog();
    sim_path = fld.getPath(fileLocationDiaglog.JPCBSIM_PROJECT_DIR)+"/";
    if(sim_path==null) sim_path="/tmp";

    sim_name = "default";

    fdtd_fmax = new String("3e9");
    fdtd_maxsteps = new String("1000000000");
    fdtd_oversample = new String("64");  //affects settings for sparm_chart / FFT
    fdtd_endlevel = new String("1e-04"); //-40dBm
    excitation_waveform_type = new String("0"); //gaussian
    excitation_f0 = new String("0");
    excitation_fc = new String("3.0e9"); //20dB cutoff
    boundary_condition_xmin = new String("PML_8");
    boundary_condition_xmax = new String("PML_8");
    boundary_condition_ymin = new String("PML_8");
    boundary_condition_ymax = new String("PML_8");
    boundary_condition_zmin = new String("PML_8");
    boundary_condition_zmax = new String("PML_8");

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //These default parameters appear to model the dispersive characteristics of FR4 very good up to 5 GHz.
    //There is good agreement with the lorentz material paramater extraction experiment described in:
    //
    //Journal of Electrical Engineering Vol 53. NO 9/S, 2002, 97-100
    //EXTRACTION OF LORENTZIAN AND DEBYE PARAMETERS OF DIELECTRIC AND MAGNETIC DISPERSIVE MATERIALS FOR FDTD MODELING
    //Marina Y. Koledintseva, et.al.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    pcb_prop_epsilon = new String("4.2");
    pcb_prop_mue = new String("1.0");
    pcb_prop_kappa = new String("0.0039");
    pcb_prop_plasmafreq = new String("3.8e9");
    pcb_prop_lorentz_pole_freq= new String("12.57e9");
    pcb_prop_relaxation_time= new String("7.0e-11");
    pcb_thickness_inches = new String("0.0"); //default of 0.0 indicates that user should be prompted for actual thickness
    do_lorentz=true;
    do_box_vias=true;
    do_filter_edges=true;

    //TODO:
    sparm_chart_fft_stretch_factor = 1.0; //don't use this anymore.  set to 1.0
    sparm_update_freq_sec = 15.0;
    do_touchstone_output=true;

    do_airbox=true;
    do_mesh=true;
    do_pml=false;
    do_dual_edge_mesh=false;
    airbox_dist=new String("3.0");
    mesh_resolution=new String("24.0");
    mesh_pcb_z_lines=new String("4.0");
    airbox_epsilon=new String("1.0");
    airbox_mue=new String("1.0");
    airbox_kapa=new String("5.5e-15");


    port1_excitation_voltage=new String("100"); //1V into Z direction
    port1_resistance = new String("50.0");
    port1_capacitance = new String("0.0");
    port1_inductance = new String("0.0");
    port2_resistance = new String("50.0");
    port2_capacitance = new String("0.0");
    port2_inductance = new String("0.0");

    do_dump_efield=false;
    do_dump_hfield=false;
    do_debug_pec=true;
    do_sim_verbose=true;
    do_fixed_threads=true;
    sim_threads = new String("3");
  }


}
