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
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class PCBSimClient extends javax.swing.JFrame
{

  private java.util.Timer utimer;
  private volatile boolean do_load_pcb=false;
  private volatile boolean tree_node_selected=false;
  private pcb_model pcbmodel=null;
  private static PCBSimClient frame=null;
  private sparm_chart sparmchart;
  private JPanel sparm_panel;
  private fileLocationDiaglog fld;
  private Simulation simulation;
  private smithChartPanel smithchart_panel;
  private sparmChartPanel sparmchart_panel;
  private microStrip mstrip_frame;
  private javax.swing.tree.DefaultMutableTreeNode RootNode = new javax.swing.tree.DefaultMutableTreeNode("Projects");
  private java.util.List<Image> icons;

  public PCBSimClient(final String[] args)
  {
    initComponents();

    fld = new fileLocationDiaglog();


    if(args.length>0) {
      loadSimulation(args[0]);
    } else {
      loadSimulation("default");
    }
    updateTreeList();



    mstrip_frame = new microStrip();


    modelView.requestFocus();
    utimer = new java.util.Timer();
    utimer.schedule(new updateTask(this), 1000, 60);

    // Get the tree's cell renderer. If it is a default
    // cell renderer, customize it.
    TreeCellRenderer cr = proj_tree.getCellRenderer();
    if (cr instanceof DefaultTreeCellRenderer) {
      DefaultTreeCellRenderer dtcr =
        (DefaultTreeCellRenderer)cr;

      // Set the various colors
      dtcr.setBackgroundNonSelectionColor(Color.black);
      dtcr.setBackgroundSelectionColor(Color.gray);
      dtcr.setTextSelectionColor(Color.white);
      dtcr.setTextNonSelectionColor(Color.black);

      // Finally, set the tree's background color
      proj_tree.setBackground(Color.white);
    }
    modelView.getRenderer().camera.moveFwd(modelView.getRenderer().camera.getMoveSpeed() * 10.0);

    smithchart_panel = new smithChartPanel();
    sparmchart_panel = new sparmChartPanel();

    sparmchart = new sparm_chart(simulation, smithchart_panel, sparmchart_panel);
    //sparm_panel = sparmchart.createDemoPanel(null,0,0);
    //sparm_panel.setPreferredSize( new Dimension(1024,768) );
    new Thread(sparmchart).start();


    jTabbedPane1.addTab("S-Parameters", sparmchart_panel);
    jTabbedPane1.addTab("Smith Chart", smithchart_panel);
    jTabbedPane1.addTab("Console", new jPCBSim.Console());

    for(int i=0; i<proj_tree.getRowCount(); i++) {
      proj_tree.expandRow(i);
    }


    icons = new ArrayList<Image>();
    icons.add( new ImageIcon(getClass().getResource("/jPCBSim/icons/icon1.png")).getImage() );
    icons.add( new ImageIcon(getClass().getResource("/jPCBSim/icons/icon2.png")).getImage() );
    icons.add( new ImageIcon(getClass().getResource("/jPCBSim/icons/icon3.png")).getImage() );
    icons.add( new ImageIcon(getClass().getResource("/jPCBSim/icons/icon4.png")).getImage() );
    setIconImages(icons);

    pack();
    setSize(1400,1100);
    jSplitPane2.setDividerLocation(750);
  }

  ///////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////
  public void updateFromSimFields(Simulation sim)
  {
    //FDTD tab
    sim.fdtd_maxsteps = fdtd_number_of_timesteps.getText();
    sim.fdtd_endlevel = fdtd_end_level.getText();
    sim.fdtd_fmax = fdtd_max_freq.getText();

    //Excitation tab
    sim.excitation_f0 = exc_f0_tf.getText();
    sim.excitation_fc = exc_fc_tf.getText();
    sim.port1_resistance = tp1_resistance.getText();
    sim.port2_resistance = tp2_resistance.getText();

    //boundary condition tab
    if( xmin_mur.isSelected()) sim.boundary_condition_xmin="MUR";
    if( xmin_pec.isSelected()) sim.boundary_condition_xmin="PEC";
    if( xmin_pmc.isSelected()) sim.boundary_condition_xmin="PMC";
    if( xmin_pml8.isSelected()) sim.boundary_condition_xmin="PML_8";

    if( xmax_mur.isSelected()) sim.boundary_condition_xmax="MUR";
    if( xmax_pec.isSelected()) sim.boundary_condition_xmax="PEC";
    if( xmax_pmc.isSelected()) sim.boundary_condition_xmax="PMC";
    if( xmax_pml8.isSelected()) sim.boundary_condition_xmax="PML_8";

    if( ymin_mur.isSelected()) sim.boundary_condition_ymin="MUR";
    if( ymin_pec.isSelected()) sim.boundary_condition_ymin="PEC";
    if( ymin_pmc.isSelected()) sim.boundary_condition_ymin="PMC";

    if( ymin_pml8.isSelected()) sim.boundary_condition_ymin="PML_8";
    if( ymax_mur.isSelected()) sim.boundary_condition_ymax="MUR";
    if( ymax_pec.isSelected()) sim.boundary_condition_ymax="PEC";
    if( ymax_pmc.isSelected()) sim.boundary_condition_ymax="PMC";
    if( ymax_pml8.isSelected()) sim.boundary_condition_ymax="PML_8";

    if( zmin_mur.isSelected()) sim.boundary_condition_zmin="MUR";
    if( zmin_pec.isSelected()) sim.boundary_condition_zmin="PEC";
    if( zmin_pmc.isSelected()) sim.boundary_condition_zmin="PMC";
    if( zmin_pml8.isSelected()) sim.boundary_condition_zmin="PML_8";

    if( zmax_mur.isSelected()) sim.boundary_condition_zmax="MUR";
    if( zmax_pec.isSelected()) sim.boundary_condition_zmax="PEC";
    if( zmax_pmc.isSelected()) sim.boundary_condition_zmax="PMC";
    if( zmax_pml8.isSelected()) sim.boundary_condition_zmax="PML_8";

    //pcb / dielectric tab
    sim.pcb_prop_epsilon = pcb_er.getText();
    sim.pcb_prop_kappa = pcb_kappa.getText();
    sim.pcb_prop_lorentz_pole_freq = pcb_lorentzpolefreq.getText();
    sim.pcb_prop_mue = pcb_mue.getText();
    sim.pcb_prop_plasmafreq = pcb_plasmafreq.getText();
    sim.pcb_prop_relaxation_time = pcb_relaxtime.getText();
    sim.pcb_thickness_inches = pcb_thickness_inches.getText();
    if( use_lorentz.isSelected() ) sim.do_lorentz=true;
    else sim.do_lorentz=false;
    if( do_box_vias.isSelected() ) sim.do_box_vias=true;
    else sim.do_box_vias=false;



    //airbox / mesh tab
    if( view_airbox.isSelected() ) sim.do_airbox=true;
    else sim.do_airbox=false;
    if( view_mesh.isSelected() ) sim.do_mesh=true;
    else sim.do_mesh=false;
    if( view_pml.isSelected() ) sim.do_pml=true;
    else sim.do_pml=false;
    if( filter_edges.isSelected() ) sim.do_filter_edges=true;
    else sim.do_filter_edges=false;

    sim.mesh_resolution = mesh_res_wavelength.getText();
    sim.airbox_dist = airbox_wavelength1.getText();
    sim.mesh_pcb_z_lines = mesh_zlines.getText();
    sim.airbox_epsilon = airbox_epsilon.getText();
    sim.airbox_mue = airbox_mue.getText();
    sim.airbox_kapa = airbox_kappa.getText();

    //sim output tab
    if( do_touchstone.isSelected() ) sim.do_touchstone_output = true;

    sim.sparm_update_freq_sec = new Double(sparm_update_freq.getText()).doubleValue();

    //sim.sparm_chart_fft_stretch_factor = new Double( sparm_stretch_factor.getText()).doubleValue();
    sim.sparm_chart_fft_stretch_factor = 1.0; 

    if( dump_efield_vtr.isSelected()) sim.do_dump_efield=true;
    else sim.do_dump_efield=false;
    if( do_pec_debug.isSelected()) sim.do_debug_pec=true;
    else sim.do_debug_pec=false;

    //openems tab
    if( number_threads.isSelected()) sim.do_fixed_threads=true;
    else sim.do_fixed_threads=false;
    sim.sim_threads = openems_nthreads.getText();
  }

  ///////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////
  public void updateSimFields(Simulation sim)
  {

    try {
      modelView.getRenderer().setSimulation(sim);
      modelView.getRenderer().setClient(this);
    } catch(Exception e) {
    }
    try {
      sparmchart.setSimulation(simulation);
    } catch(Exception e) {
    }

    //FDTD tab
    fdtd_number_of_timesteps.setText( sim.fdtd_maxsteps );
    fdtd_end_level.setText( sim.fdtd_endlevel );
    fdtd_max_freq.setText( sim.fdtd_fmax );

    //Excitation tab
    exc_f0_tf.setText( sim.excitation_f0 );
    exc_fc_tf.setText( sim.excitation_fc );
    tp1_resistance.setText( sim.port1_resistance );
    tp2_resistance.setText( sim.port2_resistance );

    //boundary condition tab
    if( sim.boundary_condition_xmin.equals("MUR") ) xmin_mur.setSelected(true);
    if( sim.boundary_condition_xmin.equals("PEC") ) xmin_pec.setSelected(true);
    if( sim.boundary_condition_xmin.equals("PMC") ) xmin_pmc.setSelected(true);
    if( sim.boundary_condition_xmin.equals("PML_8") ) xmin_pml8.setSelected(true);
    if( sim.boundary_condition_xmax.equals("MUR") ) xmax_mur.setSelected(true);
    if( sim.boundary_condition_xmax.equals("PEC") ) xmax_pec.setSelected(true);
    if( sim.boundary_condition_xmax.equals("PMC") ) xmax_pmc.setSelected(true);
    if( sim.boundary_condition_xmax.equals("PML_8") ) xmax_pml8.setSelected(true);

    if( sim.boundary_condition_ymin.equals("MUR") ) ymin_mur.setSelected(true);
    if( sim.boundary_condition_ymin.equals("PEC") ) ymin_pec.setSelected(true);
    if( sim.boundary_condition_ymin.equals("PMC") ) ymin_pmc.setSelected(true);
    if( sim.boundary_condition_ymin.equals("PML_8") ) ymin_pml8.setSelected(true);
    if( sim.boundary_condition_ymax.equals("MUR") ) ymax_mur.setSelected(true);
    if( sim.boundary_condition_ymax.equals("PEC") ) ymax_pec.setSelected(true);
    if( sim.boundary_condition_ymax.equals("PMC") ) ymax_pmc.setSelected(true);
    if( sim.boundary_condition_ymax.equals("PML_8") ) ymax_pml8.setSelected(true);

    if( sim.boundary_condition_zmin.equals("MUR") ) zmin_mur.setSelected(true);
    if( sim.boundary_condition_zmin.equals("PEC") ) zmin_pec.setSelected(true);
    if( sim.boundary_condition_zmin.equals("PMC") ) zmin_pmc.setSelected(true);
    if( sim.boundary_condition_zmin.equals("PML_8") ) zmin_pml8.setSelected(true);
    if( sim.boundary_condition_zmax.equals("MUR") ) zmax_mur.setSelected(true);
    if( sim.boundary_condition_zmax.equals("PEC") ) zmax_pec.setSelected(true);
    if( sim.boundary_condition_zmax.equals("PMC") ) zmax_pmc.setSelected(true);
    if( sim.boundary_condition_zmax.equals("PML_8") ) zmax_pml8.setSelected(true);


    //pcb / dielectric tab
    pcb_er.setText(sim.pcb_prop_epsilon );
    pcb_kappa.setText(sim.pcb_prop_kappa );
    pcb_lorentzpolefreq.setText(sim.pcb_prop_lorentz_pole_freq );
    pcb_mue.setText(sim.pcb_prop_mue );
    pcb_plasmafreq.setText(sim.pcb_prop_plasmafreq );
    pcb_relaxtime.setText(sim.pcb_prop_relaxation_time );
    pcb_thickness_inches.setText(sim.pcb_thickness_inches );

    if(sim.do_lorentz) use_lorentz.setSelected(true);
    else use_lorentz.setSelected(false);

    if(sim.do_box_vias) do_box_vias.setSelected(true);
    else do_box_vias.setSelected(false);

    //airbox / mesh tab
    if( sim.do_airbox ) view_airbox.setSelected(true);
    else view_airbox.setSelected(false);
    if( sim.do_mesh ) view_mesh.setSelected(true);
    else view_mesh.setSelected(false);
    if( sim.do_pml ) view_pml.setSelected(true);
    else view_pml.setSelected(false);
    if( sim.do_filter_edges ) filter_edges.setSelected(true);
    else filter_edges.setSelected(false);
    mesh_res_wavelength.setText( sim.mesh_resolution );
    airbox_wavelength1.setText( sim.airbox_dist );
    mesh_zlines.setText( sim.mesh_pcb_z_lines );
    airbox_epsilon.setText( sim.airbox_epsilon );
    airbox_mue.setText( sim.airbox_mue );
    airbox_kappa.setText( sim.airbox_kapa );

    //sim output tab
    if( sim.do_touchstone_output ) do_touchstone.setSelected(true);
    sparm_update_freq.setText( new Double(sim.sparm_update_freq_sec).toString() );
    if(sim.do_dump_efield) dump_efield_vtr.setSelected(true);
    else dump_efield_vtr.setSelected(false);
    if(sim.do_debug_pec) do_pec_debug.setSelected(true);
    else do_pec_debug.setSelected(false);

    //openems tab
    if( sim.do_fixed_threads) number_threads.setSelected(true);
    else number_threads.setSelected(false);
    openems_nthreads.setText(sim.sim_threads);

  }

  /////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////
  class updateTask extends java.util.TimerTask
  {
    PCBSimClient parent;
    public updateTask(PCBSimClient p) {
      parent = p;
    }

    public void run()
    {
      try {
        if(do_load_pcb || tree_node_selected) {

          do_load_pcb=false;

          if(tree_node_selected) {
            tree_node_selected=false;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) proj_tree.getLastSelectedPathComponent();
            TreePath tp = proj_tree.getSelectionPath();

            if (node == null) return;

            Object nodeInfo = node.getUserObject();
            if (node.isLeaf()) {
              loadSimulation(node.toString());
            } else {
            }

            Thread.sleep(100);
            sparmchart.updateNow();
            return;

          }




          modelView.getRenderer().setPCBModel(null);
          Thread.sleep(100);

          modelView.getRenderer().camera.setEye(new Vector3d(0, 0, -2));
          modelView.getRenderer().camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
          modelView.getRenderer().camera.rotateAroundLookVector(90.0);
          modelView.getRenderer().camera.updateView();
          modelView.getRenderer().camera.setEye(new Vector3d(0, 0, -2));
          modelView.getRenderer().camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
          modelView.getRenderer().camera.rotateAroundLookVector(90.0);
          modelView.getRenderer().camera.updateView();
          modelView.getRenderer().camera.rotateAroundRightVector(-45);
          modelView.getRenderer().camera.updateView();

          FileDialog fd = new FileDialog(frame, "Select .hyp model to load", FileDialog.LOAD);
          fd.setDirectory(System.getProperty("user.home"));
          fd.setFile("*.hyp");
          fd.setVisible(true);
          String file = fd.getFile();

          String sim_name = file.substring(0, file.indexOf("."));
          simulation = new Simulation(parent,sim_name);
          updateSimFields(simulation);

          if (file != null) {
            file = fd.getDirectory()+"/"+file;


            exec_external ee = new exec_external();
            String hyp2mat_path = fld.getPath(fileLocationDiaglog.JPCBSIM_HYP2MAT_BIN);
            if(hyp2mat_path==null || hyp2mat_path.trim().length()==0) {
              fld.show();
              hyp2mat_path = fld.getPath(fileLocationDiaglog.JPCBSIM_HYP2MAT_BIN);
            } else {

              String pcb_height = "";
              if( simulation.pcb_thickness_inches.length()==0 || new Double(simulation.pcb_thickness_inches).doubleValue()==0.0) {
                pcb_height = JOptionPane.showInputDialog(frame, "Translate thickness of pcb (Inches)? Cancel to use height defined in file.");
              } else {
                pcb_height = simulation.pcb_thickness_inches;
              }
              if(pcb_height==null) pcb_height="";
              else simulation.pcb_thickness_inches = pcb_height;

              ee.execute_and_wait(1,
                                  hyp2mat_path+" --arc-precision=1.0 -v -o "+simulation.sim_path+simulation.sim_name+"/"+simulation.sim_name+"_pcb.m -f csxcad "+file,
                                  simulation.sim_path+simulation.sim_name);

              loadSimulation(sim_name);
              updateSimFields(simulation);

              updateTreeList();
              modelView.requestFocus();
            }
          }
          fd.dispose();

        }

      } catch(Exception e) {
        e.printStackTrace(System.out);
      }
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        bc_bg_xmin = new javax.swing.ButtonGroup();
        bc_bg_xmax = new javax.swing.ButtonGroup();
        bc_bg_ymin = new javax.swing.ButtonGroup();
        bc_bg_ymax = new javax.swing.ButtonGroup();
        bc_bg_zmin = new javax.swing.ButtonGroup();
        bc_bg_zmax = new javax.swing.ButtonGroup();
        bc_gpulse = new javax.swing.ButtonGroup();
        bg_port_mode = new javax.swing.ButtonGroup();
        main_panel = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        modelView = new jPCBSim.PCBPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        proj_tree = new javax.swing.JTree();
        config_panel = new javax.swing.JPanel();
        status_panel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        fdtd_panel = new javax.swing.JPanel();
        fdtd_reset_default = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        fdtd_number_of_timesteps = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        fdtd_end_level = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        fdtd_max_freq = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        excitation_panel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        gaussian_pulse_rb = new javax.swing.JRadioButton();
        excitation_reset_default = new javax.swing.JButton();
        sinusoidal_rb = new javax.swing.JRadioButton();
        custom_exc_rb = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        exc_f0_tf = new javax.swing.JTextField();
        exc_fc_tf = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        tp1_resistance = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        tp2_resistance = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        microstrip = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        boundary_panel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        xmin_pml8 = new javax.swing.JRadioButton();
        xmin_pec = new javax.swing.JRadioButton();
        xmin_pmc = new javax.swing.JRadioButton();
        xmin_mur = new javax.swing.JRadioButton();
        ymin_pec = new javax.swing.JRadioButton();
        ymin_pmc = new javax.swing.JRadioButton();
        ymin_mur = new javax.swing.JRadioButton();
        ymin_pml8 = new javax.swing.JRadioButton();
        zmin_pec = new javax.swing.JRadioButton();
        zmin_pmc = new javax.swing.JRadioButton();
        zmin_mur = new javax.swing.JRadioButton();
        zmin_pml8 = new javax.swing.JRadioButton();
        xmax_pec = new javax.swing.JRadioButton();
        xmax_pmc = new javax.swing.JRadioButton();
        xmax_mur = new javax.swing.JRadioButton();
        xmax_pml8 = new javax.swing.JRadioButton();
        ymax_pec = new javax.swing.JRadioButton();
        ymax_pmc = new javax.swing.JRadioButton();
        ymax_mur = new javax.swing.JRadioButton();
        ymax_pml8 = new javax.swing.JRadioButton();
        zmax_pec = new javax.swing.JRadioButton();
        zmax_pmc = new javax.swing.JRadioButton();
        zmax_mur = new javax.swing.JRadioButton();
        zmax_pml8 = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        bc_reset_default = new javax.swing.JButton();
        pcb_panel = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        pcb_presets = new javax.swing.JComboBox();
        er_presets = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        use_lorentz = new javax.swing.JCheckBox();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        pcb_relaxtime = new javax.swing.JTextField();
        pcb_er = new javax.swing.JTextField();
        pcb_mue = new javax.swing.JTextField();
        pcb_kappa = new javax.swing.JTextField();
        pcb_plasmafreq = new javax.swing.JTextField();
        pcb_lorentzpolefreq = new javax.swing.JTextField();
        pcb_reset_default = new javax.swing.JButton();
        jLabel47 = new javax.swing.JLabel();
        pcb_thickness_inches = new javax.swing.JTextField();
        do_box_vias = new javax.swing.JCheckBox();
        jLabel39 = new javax.swing.JLabel();
        mesh_panel = new javax.swing.JPanel();
        view_pml = new javax.swing.JCheckBox();
        view_airbox = new javax.swing.JCheckBox();
        view_mesh = new javax.swing.JCheckBox();
        filter_edges = new javax.swing.JCheckBox();
        jLabel40 = new javax.swing.JLabel();
        mesh_res_wavelength = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        airbox_wavelength1 = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        mesh_zlines = new javax.swing.JTextField();
        mesh_reset_default = new javax.swing.JButton();
        jLabel43 = new javax.swing.JLabel();
        airbox_epsilon = new javax.swing.JTextField();
        airbox_mue = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        airbox_kappa = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        mesh_apply = new javax.swing.JButton();
        sp_panel = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        dump_efield_vtr = new javax.swing.JCheckBox();
        plot_efield_modelview = new javax.swing.JCheckBox();
        dump_td_volt_current = new javax.swing.JCheckBox();
        do_pec_debug = new javax.swing.JCheckBox();
        jLabel36 = new javax.swing.JLabel();
        sparm_update_freq = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        simout_reset_default = new javax.swing.JButton();
        do_touchstone = new javax.swing.JCheckBox();
        openems_panel = new javax.swing.JPanel();
        start_sim = new javax.swing.JButton();
        abort_sim = new javax.swing.JButton();
        number_threads = new javax.swing.JCheckBox();
        openems_nthreads = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        write_openems_log = new javax.swing.JCheckBox();
        view_pec = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        filemenu = new javax.swing.JMenu();
        import_menu = new javax.swing.JMenu();
        import_hyp2mat = new javax.swing.JMenuItem();
        export_menu = new javax.swing.JMenu();
        export_qucs = new javax.swing.JMenuItem();
        Exit = new javax.swing.JMenuItem();
        editmenu = new javax.swing.JMenu();
        Preferences = new javax.swing.JMenuItem();
        tools = new javax.swing.JMenu();
        microstripcalc = new javax.swing.JMenuItem();
        kappacalc = new javax.swing.JMenuItem();
        help = new javax.swing.JMenu();
        about = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("jPCBSimClient");

        main_panel.setLayout(new javax.swing.BoxLayout(main_panel, javax.swing.BoxLayout.Y_AXIS));

        jSplitPane2.setDividerLocation(700);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(1.0);

        jSplitPane1.setPreferredSize(new java.awt.Dimension(220, 768));

        modelView.setPreferredSize(new java.awt.Dimension(1024, 1768));
        modelView.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                modelViewMouseWheelMoved(evt);
            }
        });
        jTabbedPane1.addTab("Model View", modelView);

        jSplitPane1.setRightComponent(jTabbedPane1);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(222, 22));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(98, 150));

        proj_tree.setForeground(new java.awt.Color(0, 0, 0));
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Projects");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("File Sources");
        javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Simulation/RF_Filter1");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Network Sources");
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("172.16.1.20/RF_Filter2");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        proj_tree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        proj_tree.setCellRenderer(null);
        proj_tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                proj_treeValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(proj_tree);

        jSplitPane1.setLeftComponent(jScrollPane2);

        jSplitPane2.setTopComponent(jSplitPane1);

        config_panel.setLayout(new java.awt.BorderLayout());

        status_panel.setBackground(new java.awt.Color(0, 0, 0));
        status_panel.setForeground(new java.awt.Color(255, 255, 255));
        status_panel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        status_panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Status:");
        status_panel.add(jLabel1);

        config_panel.add(status_panel, java.awt.BorderLayout.SOUTH);

        jTabbedPane2.setPreferredSize(new java.awt.Dimension(458, 130));

        fdtd_panel.setLayout(null);

        fdtd_reset_default.setText("Reset To Defaults");
        fdtd_reset_default.setEnabled(false);
        fdtd_reset_default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fdtd_reset_defaultActionPerformed(evt);
            }
        });
        fdtd_panel.add(fdtd_reset_default);
        fdtd_reset_default.setBounds(20, 130, 200, 25);

        jLabel16.setText("Simulation End Criteria");
        fdtd_panel.add(jLabel16);
        jLabel16.setBounds(20, 20, 200, 15);

        jLabel17.setText("Number Of Time Steps: ");
        fdtd_panel.add(jLabel17);
        jLabel17.setBounds(20, 50, 180, 15);

        fdtd_number_of_timesteps.setColumns(10);
        fdtd_number_of_timesteps.setText("1000000000");
        fdtd_number_of_timesteps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fdtd_number_of_timestepsActionPerformed(evt);
            }
        });
        fdtd_panel.add(fdtd_number_of_timesteps);
        fdtd_number_of_timesteps.setBounds(190, 39, 114, 30);

        jLabel18.setText("Level: ");
        fdtd_panel.add(jLabel18);
        jLabel18.setBounds(20, 100, 46, 15);

        fdtd_end_level.setColumns(4);
        fdtd_end_level.setText("1e-4");
        fdtd_panel.add(fdtd_end_level);
        fdtd_end_level.setBounds(80, 90, 60, 30);

        jLabel20.setText("Or");
        fdtd_panel.add(jLabel20);
        jLabel20.setBounds(70, 70, 17, 15);

        jLabel21.setText("Sim Max Frequency:");
        fdtd_panel.add(jLabel21);
        jLabel21.setBounds(380, 50, 160, 15);

        fdtd_max_freq.setColumns(8);
        fdtd_max_freq.setText("20.0e9");
        fdtd_panel.add(fdtd_max_freq);
        fdtd_max_freq.setBounds(540, 39, 70, 30);

        jLabel22.setText("Hz");
        fdtd_panel.add(jLabel22);
        jLabel22.setBounds(620, 50, 18, 15);

        jTabbedPane2.addTab("FDTD", fdtd_panel);

        excitation_panel.setLayout(null);

        jLabel9.setText("Field Source Properties");
        excitation_panel.add(jLabel9);
        jLabel9.setBounds(60, 20, 180, 15);

        bc_gpulse.add(gaussian_pulse_rb);
        gaussian_pulse_rb.setSelected(true);
        gaussian_pulse_rb.setText("Gaussian Pulse");
        excitation_panel.add(gaussian_pulse_rb);
        gaussian_pulse_rb.setBounds(60, 50, 150, 20);

        excitation_reset_default.setText("Reset To Defaults");
        excitation_reset_default.setEnabled(false);
        excitation_reset_default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excitation_reset_defaultActionPerformed(evt);
            }
        });
        excitation_panel.add(excitation_reset_default);
        excitation_reset_default.setBounds(30, 140, 190, 25);

        bc_gpulse.add(sinusoidal_rb);
        sinusoidal_rb.setText("Sinusoidal");
        sinusoidal_rb.setEnabled(false);
        excitation_panel.add(sinusoidal_rb);
        sinusoidal_rb.setBounds(60, 80, 99, 23);

        bc_gpulse.add(custom_exc_rb);
        custom_exc_rb.setText("Custom");
        custom_exc_rb.setEnabled(false);
        excitation_panel.add(custom_exc_rb);
        custom_exc_rb.setBounds(60, 110, 78, 23);

        jLabel10.setText("Center Frequency (F0)");
        jLabel10.setToolTipText("Center Frequency");
        excitation_panel.add(jLabel10);
        jLabel10.setBounds(230, 90, 160, 15);

        jLabel11.setText("20 dB Cutoff Freq (Fc)");
        jLabel11.setToolTipText("20dB Frequency Cutoff");
        excitation_panel.add(jLabel11);
        jLabel11.setBounds(230, 50, 160, 20);

        exc_f0_tf.setColumns(8);
        excitation_panel.add(exc_f0_tf);
        exc_f0_tf.setBounds(390, 80, 92, 30);

        exc_fc_tf.setColumns(8);
        excitation_panel.add(exc_fc_tf);
        exc_fc_tf.setBounds(390, 39, 92, 30);

        jLabel12.setText("Hz");
        excitation_panel.add(jLabel12);
        jLabel12.setBounds(490, 90, 40, 20);

        jLabel13.setText("Hz");
        excitation_panel.add(jLabel13);
        jLabel13.setBounds(490, 45, 30, 30);

        jLabel14.setText("Hint:  For PCB Filter Simulations, Use Gaussian Pulse With Cutoff of Highest Frequency Of Interest, and F0=0Hz");
        excitation_panel.add(jLabel14);
        jLabel14.setBounds(230, 140, 810, 15);

        jLabel15.setText("TP1 -> E-Field, Soft Excitation,  Excite[0,0,-1] (-1V in Z-dir)");
        jLabel15.setToolTipText("Excitation Port Pad In Eaglecad must be named TP1");
        excitation_panel.add(jLabel15);
        jLabel15.setBounds(600, 20, 420, 15);

        jLabel24.setText("Port 1 (TP1) Resistance:");
        excitation_panel.add(jLabel24);
        jLabel24.setBounds(550, 50, 180, 15);

        tp1_resistance.setColumns(3);
        tp1_resistance.setText("50");
        excitation_panel.add(tp1_resistance);
        tp1_resistance.setBounds(730, 39, 50, 30);

        jLabel30.setText("Ohms");
        excitation_panel.add(jLabel30);
        jLabel30.setBounds(790, 50, 40, 15);

        jLabel31.setText("Port 2 (TP2) Resistance:");
        excitation_panel.add(jLabel31);
        jLabel31.setBounds(550, 90, 180, 15);

        tp2_resistance.setColumns(3);
        tp2_resistance.setText("50");
        excitation_panel.add(tp2_resistance);
        tp2_resistance.setBounds(730, 80, 50, 30);

        jLabel32.setText("Ohms");
        excitation_panel.add(jLabel32);
        jLabel32.setBounds(790, 90, 40, 15);

        jLabel33.setText("(Not used for antennas)");
        excitation_panel.add(jLabel33);
        jLabel33.setBounds(840, 90, 180, 15);

        bg_port_mode.add(microstrip);
        microstrip.setSelected(true);
        microstrip.setText("Microstrip");
        microstrip.setEnabled(false);
        excitation_panel.add(microstrip);
        microstrip.setBounds(250, 10, 96, 23);

        bg_port_mode.add(jRadioButton1);
        jRadioButton1.setText("Co-Planar Over Ground");
        jRadioButton1.setEnabled(false);
        excitation_panel.add(jRadioButton1);
        jRadioButton1.setBounds(360, 10, 200, 23);

        jTabbedPane2.addTab("Excitation", excitation_panel);

        boundary_panel.setLayout(null);

        jLabel2.setText("Z Min");
        boundary_panel.add(jLabel2);
        jLabel2.setBounds(110, 90, 51, 15);

        jLabel3.setText("X Min");
        boundary_panel.add(jLabel3);
        jLabel3.setBounds(110, 30, 38, 15);

        jLabel4.setText("Y Min");
        boundary_panel.add(jLabel4);
        jLabel4.setBounds(110, 60, 51, 15);

        jLabel5.setText("X Max");
        boundary_panel.add(jLabel5);
        jLabel5.setBounds(520, 30, 50, 20);

        jLabel6.setText("Y Max");
        boundary_panel.add(jLabel6);
        jLabel6.setBounds(520, 60, 60, 20);

        jLabel7.setText("Z Max");
        boundary_panel.add(jLabel7);
        jLabel7.setBounds(520, 90, 60, 15);

        bc_bg_xmin.add(xmin_pml8);
        xmin_pml8.setSelected(true);
        xmin_pml8.setText("PML_8");
        xmin_pml8.setToolTipText("Perfectly Matchted Layer absorbing boundary condition, using x number of cells");
        xmin_pml8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xmin_pml8ActionPerformed(evt);
            }
        });
        boundary_panel.add(xmin_pml8);
        xmin_pml8.setBounds(350, 30, 70, 23);

        bc_bg_xmin.add(xmin_pec);
        xmin_pec.setText("PEC");
        xmin_pec.setToolTipText("Perfect electric conductor ");
        boundary_panel.add(xmin_pec);
        xmin_pec.setBounds(170, 30, 60, 23);

        bc_bg_xmin.add(xmin_pmc);
        xmin_pmc.setText("PMC");
        xmin_pmc.setToolTipText("Perfect magnetic conductor");
        boundary_panel.add(xmin_pmc);
        xmin_pmc.setBounds(230, 30, 60, 23);

        bc_bg_xmin.add(xmin_mur);
        xmin_mur.setText("MUR");
        xmin_mur.setToolTipText("A simple absorbing boundary condition (ABC)");
        xmin_mur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xmin_murActionPerformed(evt);
            }
        });
        boundary_panel.add(xmin_mur);
        xmin_mur.setBounds(290, 30, 60, 23);

        bc_bg_ymin.add(ymin_pec);
        ymin_pec.setText("PEC");
        ymin_pec.setToolTipText("Perfect electric conductor");
        boundary_panel.add(ymin_pec);
        ymin_pec.setBounds(170, 60, 60, 23);

        bc_bg_ymin.add(ymin_pmc);
        ymin_pmc.setText("PMC");
        ymin_pmc.setToolTipText("Perfect magnetic conductor");
        boundary_panel.add(ymin_pmc);
        ymin_pmc.setBounds(230, 60, 60, 23);

        bc_bg_ymin.add(ymin_mur);
        ymin_mur.setText("MUR");
        ymin_mur.setToolTipText("A simple absorbing boundary condition (ABC)");
        ymin_mur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ymin_murActionPerformed(evt);
            }
        });
        boundary_panel.add(ymin_mur);
        ymin_mur.setBounds(290, 60, 60, 23);

        bc_bg_ymin.add(ymin_pml8);
        ymin_pml8.setSelected(true);
        ymin_pml8.setText("PML_8");
        ymin_pml8.setToolTipText("Perfectly Matchted Layer absorbing boundary condition, using x number of cells");
        ymin_pml8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ymin_pml8ActionPerformed(evt);
            }
        });
        boundary_panel.add(ymin_pml8);
        ymin_pml8.setBounds(350, 60, 70, 23);

        bc_bg_zmin.add(zmin_pec);
        zmin_pec.setText("PEC");
        zmin_pec.setToolTipText("Perfect electric conductor ");
        boundary_panel.add(zmin_pec);
        zmin_pec.setBounds(170, 90, 60, 23);

        bc_bg_zmin.add(zmin_pmc);
        zmin_pmc.setText("PMC");
        zmin_pmc.setToolTipText("Perfect magnetic conductor");
        boundary_panel.add(zmin_pmc);
        zmin_pmc.setBounds(230, 90, 60, 23);

        bc_bg_zmin.add(zmin_mur);
        zmin_mur.setText("MUR");
        zmin_mur.setToolTipText("A simple absorbing boundary condition (ABC)");
        zmin_mur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zmin_murActionPerformed(evt);
            }
        });
        boundary_panel.add(zmin_mur);
        zmin_mur.setBounds(290, 90, 60, 23);

        bc_bg_zmin.add(zmin_pml8);
        zmin_pml8.setSelected(true);
        zmin_pml8.setText("PML_8");
        zmin_pml8.setToolTipText("Perfectly Matchted Layer absorbing boundary condition, using x number of cells");
        zmin_pml8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zmin_pml8ActionPerformed(evt);
            }
        });
        boundary_panel.add(zmin_pml8);
        zmin_pml8.setBounds(350, 90, 70, 23);

        bc_bg_xmax.add(xmax_pec);
        xmax_pec.setText("PEC");
        xmax_pec.setToolTipText("Perfect electric conductor ");
        boundary_panel.add(xmax_pec);
        xmax_pec.setBounds(580, 30, 60, 23);

        bc_bg_xmax.add(xmax_pmc);
        xmax_pmc.setText("PMC");
        xmax_pmc.setToolTipText("Perfect magnetic conductor");
        boundary_panel.add(xmax_pmc);
        xmax_pmc.setBounds(640, 30, 60, 23);

        bc_bg_xmax.add(xmax_mur);
        xmax_mur.setText("MUR");
        xmax_mur.setToolTipText("A simple absorbing boundary condition (ABC)");
        xmax_mur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xmax_murActionPerformed(evt);
            }
        });
        boundary_panel.add(xmax_mur);
        xmax_mur.setBounds(700, 30, 60, 23);

        bc_bg_xmax.add(xmax_pml8);
        xmax_pml8.setSelected(true);
        xmax_pml8.setText("PML_8");
        xmax_pml8.setToolTipText("Perfectly Matchted Layer absorbing boundary condition, using x number of cells");
        xmax_pml8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xmax_pml8ActionPerformed(evt);
            }
        });
        boundary_panel.add(xmax_pml8);
        xmax_pml8.setBounds(760, 30, 70, 23);

        bc_bg_ymax.add(ymax_pec);
        ymax_pec.setText("PEC");
        ymax_pec.setToolTipText("Perfect electric conductor");
        boundary_panel.add(ymax_pec);
        ymax_pec.setBounds(580, 60, 60, 23);

        bc_bg_ymax.add(ymax_pmc);
        ymax_pmc.setText("PMC");
        ymax_pmc.setToolTipText("Perfect magnetic conductor");
        boundary_panel.add(ymax_pmc);
        ymax_pmc.setBounds(640, 60, 60, 23);

        bc_bg_ymax.add(ymax_mur);
        ymax_mur.setText("MUR");
        ymax_mur.setToolTipText("A simple absorbing boundary condition (ABC)");
        ymax_mur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ymax_murActionPerformed(evt);
            }
        });
        boundary_panel.add(ymax_mur);
        ymax_mur.setBounds(700, 60, 60, 23);

        bc_bg_ymax.add(ymax_pml8);
        ymax_pml8.setSelected(true);
        ymax_pml8.setText("PML_8");
        ymax_pml8.setToolTipText("Perfectly Matchted Layer absorbing boundary condition, using x number of cells");
        ymax_pml8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ymax_pml8ActionPerformed(evt);
            }
        });
        boundary_panel.add(ymax_pml8);
        ymax_pml8.setBounds(760, 60, 70, 23);

        bc_bg_zmax.add(zmax_pec);
        zmax_pec.setText("PEC");
        zmax_pec.setToolTipText("Perfect electric conductor ");
        boundary_panel.add(zmax_pec);
        zmax_pec.setBounds(580, 90, 60, 23);

        bc_bg_zmax.add(zmax_pmc);
        zmax_pmc.setText("PMC");
        zmax_pmc.setToolTipText("Perfect magnetic conductor");
        boundary_panel.add(zmax_pmc);
        zmax_pmc.setBounds(640, 90, 60, 23);

        bc_bg_zmax.add(zmax_mur);
        zmax_mur.setText("MUR");
        zmax_mur.setToolTipText("A simple absorbing boundary condition (ABC)");
        zmax_mur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zmax_murActionPerformed(evt);
            }
        });
        boundary_panel.add(zmax_mur);
        zmax_mur.setBounds(700, 90, 60, 23);

        bc_bg_zmax.add(zmax_pml8);
        zmax_pml8.setSelected(true);
        zmax_pml8.setText("PML_8");
        zmax_pml8.setToolTipText("Perfectly Matchted Layer absorbing boundary condition, using x number of cells");
        zmax_pml8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zmax_pml8ActionPerformed(evt);
            }
        });
        boundary_panel.add(zmax_pml8);
        zmax_pml8.setBounds(760, 90, 70, 23);

        jLabel8.setText("Hint:  To absorb all fields beyond the AIRBOX surrounding the PCB, make all BC walls  PML_8");
        boundary_panel.add(jLabel8);
        jLabel8.setBounds(260, 140, 670, 15);

        bc_reset_default.setText("Reset To Defaults");
        bc_reset_default.setEnabled(false);
        bc_reset_default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bc_reset_defaultActionPerformed(evt);
            }
        });
        boundary_panel.add(bc_reset_default);
        bc_reset_default.setBounds(30, 140, 180, 25);

        jTabbedPane2.addTab("Boundary Conditions", boundary_panel);

        pcb_panel.setLayout(null);

        jLabel23.setText("Relaxation Time: (R = L/Trelax)");
        pcb_panel.add(jLabel23);
        jLabel23.setBounds(360, 130, 230, 20);

        pcb_presets.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "FR-4", "FR408", "FR408HR" }));
        pcb_presets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pcb_presetsActionPerformed(evt);
            }
        });
        pcb_panel.add(pcb_presets);
        pcb_presets.setBounds(90, 10, 160, 24);

        er_presets.setText("Presets");
        pcb_panel.add(er_presets);
        er_presets.setBounds(20, 10, 59, 15);

        jLabel25.setText("Epsilon (Es, FR4=4.178)");
        pcb_panel.add(jLabel25);
        jLabel25.setBounds(40, 50, 190, 20);

        jLabel26.setText("S/m");
        pcb_panel.add(jLabel26);
        jLabel26.setBounds(310, 120, 50, 30);

        jLabel27.setText("Kappa (FR4=0.00175)");
        pcb_panel.add(jLabel27);
        jLabel27.setBounds(40, 130, 150, 20);

        use_lorentz.setSelected(true);
        use_lorentz.setText("Use Dispersive Modeling (Es to Einf)");
        use_lorentz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                use_lorentzActionPerformed(evt);
            }
        });
        pcb_panel.add(use_lorentz);
        use_lorentz.setBounds(370, 10, 330, 23);

        jLabel28.setText("Plasma Frequency: (L=1/Fpl^2)");
        pcb_panel.add(jLabel28);
        jLabel28.setBounds(360, 50, 220, 20);

        jLabel29.setText("Lorentz Pole Freq: (C=1/Fpo^2*L)");
        pcb_panel.add(jLabel29);
        jLabel29.setBounds(340, 90, 250, 20);

        pcb_relaxtime.setColumns(8);
        pcb_panel.add(pcb_relaxtime);
        pcb_relaxtime.setBounds(600, 120, 110, 30);

        pcb_er.setColumns(8);
        pcb_panel.add(pcb_er);
        pcb_er.setBounds(230, 40, 110, 30);

        pcb_mue.setColumns(8);
        pcb_mue.setEnabled(false);
        pcb_panel.add(pcb_mue);
        pcb_mue.setBounds(190, 80, 110, 30);

        pcb_kappa.setColumns(8);
        pcb_panel.add(pcb_kappa);
        pcb_kappa.setBounds(190, 120, 110, 30);

        pcb_plasmafreq.setColumns(8);
        pcb_panel.add(pcb_plasmafreq);
        pcb_plasmafreq.setBounds(600, 40, 110, 30);

        pcb_lorentzpolefreq.setColumns(8);
        pcb_panel.add(pcb_lorentzpolefreq);
        pcb_lorentzpolefreq.setBounds(600, 80, 110, 30);

        pcb_reset_default.setText("Reset To Defaults");
        pcb_reset_default.setEnabled(false);
        pcb_reset_default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pcb_reset_defaultActionPerformed(evt);
            }
        });
        pcb_panel.add(pcb_reset_default);
        pcb_reset_default.setBounds(820, 140, 190, 25);

        jLabel47.setText("PCB Thickness (Inches)");
        pcb_panel.add(jLabel47);
        jLabel47.setBounds(720, 50, 170, 20);

        pcb_thickness_inches.setEditable(false);
        pcb_thickness_inches.setColumns(8);
        pcb_panel.add(pcb_thickness_inches);
        pcb_thickness_inches.setBounds(890, 40, 110, 30);

        do_box_vias.setSelected(true);
        do_box_vias.setText("Use Box Vias");
        pcb_panel.add(do_box_vias);
        do_box_vias.setBounds(710, 10, 140, 23);

        jLabel39.setText("Mue");
        pcb_panel.add(jLabel39);
        jLabel39.setBounds(40, 90, 140, 20);

        jTabbedPane2.addTab("PCB/Dielectric", pcb_panel);

        mesh_panel.setLayout(null);

        view_pml.setText("View Absorbing Boundary In Model View  (P)");
        view_pml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view_pmlActionPerformed(evt);
            }
        });
        mesh_panel.add(view_pml);
        view_pml.setBounds(40, 100, 340, 23);

        view_airbox.setSelected(true);
        view_airbox.setText("View Air Box In Model View (A)");
        view_airbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view_airboxActionPerformed(evt);
            }
        });
        mesh_panel.add(view_airbox);
        view_airbox.setBounds(40, 20, 280, 23);

        view_mesh.setSelected(true);
        view_mesh.setText("View Mesh In Model View (M)");
        view_mesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view_meshActionPerformed(evt);
            }
        });
        mesh_panel.add(view_mesh);
        view_mesh.setBounds(40, 60, 270, 23);

        filter_edges.setSelected(true);
        filter_edges.setText("Filter Out Close Mesh Lines (faster sims)");
        filter_edges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filter_edgesActionPerformed(evt);
            }
        });
        mesh_panel.add(filter_edges);
        filter_edges.setBounds(390, 20, 320, 23);

        jLabel40.setText("AirBox Distance From PCB : ");
        mesh_panel.add(jLabel40);
        jLabel40.setBounds(390, 60, 210, 15);

        mesh_res_wavelength.setColumns(10);
        mesh_res_wavelength.setText("16.0");
        mesh_panel.add(mesh_res_wavelength);
        mesh_res_wavelength.setBounds(590, 90, 114, 30);

        jLabel41.setText("Mesh Resolution  >=10");
        mesh_panel.add(jLabel41);
        jLabel41.setBounds(390, 95, 190, 20);

        airbox_wavelength1.setColumns(10);
        airbox_wavelength1.setText("16.0");
        mesh_panel.add(airbox_wavelength1);
        airbox_wavelength1.setBounds(590, 50, 114, 30);

        jLabel42.setText("Z-axis PCB Mesh Lines: >=4");
        mesh_panel.add(jLabel42);
        jLabel42.setBounds(370, 130, 200, 30);

        mesh_zlines.setColumns(8);
        mesh_zlines.setText("4");
        mesh_panel.add(mesh_zlines);
        mesh_zlines.setBounds(590, 130, 110, 30);

        mesh_reset_default.setText("Reset To Defaults");
        mesh_reset_default.setEnabled(false);
        mesh_reset_default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mesh_reset_defaultActionPerformed(evt);
            }
        });
        mesh_panel.add(mesh_reset_default);
        mesh_reset_default.setBounds(40, 140, 180, 25);

        jLabel43.setText("Epsilon (Er) (permittivity):");
        jLabel43.setToolTipText("Relative To Vacuum");
        mesh_panel.add(jLabel43);
        jLabel43.setBounds(730, 50, 190, 20);

        airbox_epsilon.setColumns(8);
        airbox_epsilon.setText("1.0");
        airbox_epsilon.setEnabled(false);
        mesh_panel.add(airbox_epsilon);
        airbox_epsilon.setBounds(920, 40, 110, 30);

        airbox_mue.setColumns(8);
        airbox_mue.setText("1.0");
        airbox_mue.setEnabled(false);
        mesh_panel.add(airbox_mue);
        airbox_mue.setBounds(880, 80, 110, 30);

        jLabel44.setText("Mue  (permeability)");
        jLabel44.setToolTipText("Relative To Hydrogen");
        mesh_panel.add(jLabel44);
        jLabel44.setBounds(730, 90, 140, 20);

        jLabel45.setText("Kappa (conductivity)");
        jLabel45.setToolTipText("S/m");
        mesh_panel.add(jLabel45);
        jLabel45.setBounds(730, 130, 150, 20);

        airbox_kappa.setColumns(8);
        airbox_kappa.setText("5.5e-15");
        airbox_kappa.setEnabled(false);
        mesh_panel.add(airbox_kappa);
        airbox_kappa.setBounds(880, 120, 110, 30);

        jLabel46.setText("Air Box Material Properties");
        mesh_panel.add(jLabel46);
        jLabel46.setBounds(730, 10, 210, 15);

        mesh_apply.setText("Apply");
        mesh_apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mesh_applyActionPerformed(evt);
            }
        });
        mesh_panel.add(mesh_apply);
        mesh_apply.setBounds(250, 140, 73, 25);

        jTabbedPane2.addTab("Airbox / Mesh", mesh_panel);

        sp_panel.setLayout(null);

        jLabel34.setText("S-Parameter Plot");
        sp_panel.add(jLabel34);
        jLabel34.setBounds(30, 20, 160, 15);

        jLabel35.setText("Field Dumps");
        sp_panel.add(jLabel35);
        jLabel35.setBounds(430, 20, 110, 15);

        dump_efield_vtr.setText("Dump E-Field VTR format (can be large datasets)");
        dump_efield_vtr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dump_efield_vtrActionPerformed(evt);
            }
        });
        sp_panel.add(dump_efield_vtr);
        dump_efield_vtr.setBounds(430, 40, 390, 23);

        plot_efield_modelview.setText("Plot Simulation-Time E-Field In Model View (not yet)");
        plot_efield_modelview.setEnabled(false);
        plot_efield_modelview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plot_efield_modelviewActionPerformed(evt);
            }
        });
        sp_panel.add(plot_efield_modelview);
        plot_efield_modelview.setBounds(430, 70, 430, 23);

        dump_td_volt_current.setSelected(true);
        dump_td_volt_current.setText("Dump Time Domain Voltage/Current");
        dump_td_volt_current.setEnabled(false);
        dump_td_volt_current.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dump_td_volt_currentActionPerformed(evt);
            }
        });
        sp_panel.add(dump_td_volt_current);
        dump_td_volt_current.setBounds(30, 40, 300, 23);

        do_pec_debug.setSelected(true);
        do_pec_debug.setText("Debug PEC output");
        sp_panel.add(do_pec_debug);
        do_pec_debug.setBounds(430, 100, 190, 23);

        jLabel36.setText("S-Parm Plot Update Frequency");
        sp_panel.add(jLabel36);
        jLabel36.setBounds(30, 80, 230, 15);

        sparm_update_freq.setColumns(3);
        sparm_update_freq.setText("15");
        sp_panel.add(sparm_update_freq);
        sparm_update_freq.setBounds(260, 70, 50, 30);

        jLabel37.setText("Seconds");
        sp_panel.add(jLabel37);
        jLabel37.setBounds(320, 80, 80, 15);

        simout_reset_default.setText("Reset To Defaults");
        simout_reset_default.setEnabled(false);
        simout_reset_default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simout_reset_defaultActionPerformed(evt);
            }
        });
        sp_panel.add(simout_reset_default);
        simout_reset_default.setBounds(770, 130, 170, 25);

        do_touchstone.setSelected(true);
        do_touchstone.setText("Output Touchstone (.s1p, .s2p, .snp)");
        sp_panel.add(do_touchstone);
        do_touchstone.setBounds(30, 120, 290, 23);

        jTabbedPane2.addTab("Simulation Output", sp_panel);

        openems_panel.setLayout(null);

        start_sim.setText("Start Simulation");
        start_sim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start_simActionPerformed(evt);
            }
        });
        openems_panel.add(start_sim);
        start_sim.setBounds(450, 130, 170, 25);

        abort_sim.setText("Abort Simulation");
        abort_sim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abort_simActionPerformed(evt);
            }
        });
        openems_panel.add(abort_sim);
        abort_sim.setBounds(630, 130, 170, 25);

        number_threads.setSelected(true);
        number_threads.setText("Use Fixed Number Of CPU Cores:");
        number_threads.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                number_threadsActionPerformed(evt);
            }
        });
        openems_panel.add(number_threads);
        number_threads.setBounds(60, 20, 260, 23);

        openems_nthreads.setColumns(3);
        openems_nthreads.setText("3");
        openems_nthreads.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openems_nthreadsActionPerformed(evt);
            }
        });
        openems_panel.add(openems_nthreads);
        openems_nthreads.setBounds(320, 20, 50, 30);

        jLabel38.setText("(UnCheck For Auto-Detect)");
        openems_panel.add(jLabel38);
        jLabel38.setBounds(80, 40, 200, 15);

        write_openems_log.setText("Write log file for openEMS console output");
        write_openems_log.setEnabled(false);
        openems_panel.add(write_openems_log);
        write_openems_log.setBounds(60, 80, 340, 23);

        view_pec.setText("View PEC (paraview)");
        view_pec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view_pecActionPerformed(evt);
            }
        });
        openems_panel.add(view_pec);
        view_pec.setBounds(810, 130, 190, 25);

        jTabbedPane2.addTab("OpenEMS", openems_panel);

        config_panel.add(jTabbedPane2, java.awt.BorderLayout.CENTER);

        jSplitPane2.setRightComponent(config_panel);

        main_panel.add(jSplitPane2);

        getContentPane().add(main_panel, java.awt.BorderLayout.CENTER);

        filemenu.setText("File");

        import_menu.setText("Import");

        import_hyp2mat.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.ALT_MASK));
        import_hyp2mat.setText("Import HyperLynx .HYP File (uses: hyp2mat -f csxcad -o /tmp/pcb.m)");
        import_hyp2mat.setActionCommand("");
        import_hyp2mat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                import_hyp2matActionPerformed(evt);
            }
        });
        import_menu.add(import_hyp2mat);

        filemenu.add(import_menu);

        export_menu.setText("Export");

        export_qucs.setText("Export QUCS S-Parm Simulation");
        export_qucs.setEnabled(false);
        export_qucs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                export_qucsActionPerformed(evt);
            }
        });
        export_menu.add(export_qucs);

        filemenu.add(export_menu);

        Exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        Exit.setText("Exit");
        Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitActionPerformed(evt);
            }
        });
        filemenu.add(Exit);

        jMenuBar1.add(filemenu);

        editmenu.setText("Edit");

        Preferences.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
        Preferences.setText("Preferences");
        Preferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PreferencesActionPerformed(evt);
            }
        });
        editmenu.add(Preferences);

        jMenuBar1.add(editmenu);

        tools.setText("Tools");

        microstripcalc.setText("Microstrip Calculator");
        microstripcalc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                microstripcalcActionPerformed(evt);
            }
        });
        tools.add(microstripcalc);

        kappacalc.setText("Kappa Calculator");
        kappacalc.setEnabled(false);
        kappacalc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kappacalcActionPerformed(evt);
            }
        });
        tools.add(kappacalc);

        jMenuBar1.add(tools);

        help.setText("Help");

        about.setText("About jPCBSim");
        about.setEnabled(false);
        about.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutActionPerformed(evt);
            }
        });
        help.add(about);

        jMenuBar1.add(help);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void PreferencesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_PreferencesActionPerformed
  {
    fld.show();
  }//GEN-LAST:event_PreferencesActionPerformed

  private void ExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ExitActionPerformed
  {
    // TODO add your handling code here:
    System.exit(0);
  }//GEN-LAST:event_ExitActionPerformed

  private void import_hyp2matActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_import_hyp2matActionPerformed
  {
    do_load_pcb=true;
    if(pcbmodel!=null) pcbmodel.setLoading();
  }//GEN-LAST:event_import_hyp2matActionPerformed

  private void export_qucsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_export_qucsActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_export_qucsActionPerformed

  private void xmin_pml8ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_xmin_pml8ActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_xmin_pml8ActionPerformed

  private void xmin_murActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_xmin_murActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_xmin_murActionPerformed

  private void ymin_murActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ymin_murActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_ymin_murActionPerformed

  private void ymin_pml8ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ymin_pml8ActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_ymin_pml8ActionPerformed

  private void zmin_murActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zmin_murActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_zmin_murActionPerformed

  private void zmin_pml8ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zmin_pml8ActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_zmin_pml8ActionPerformed

  private void xmax_murActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_xmax_murActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_xmax_murActionPerformed

  private void xmax_pml8ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_xmax_pml8ActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_xmax_pml8ActionPerformed

  private void ymax_murActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ymax_murActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_ymax_murActionPerformed

  private void ymax_pml8ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ymax_pml8ActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_ymax_pml8ActionPerformed

  private void zmax_murActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zmax_murActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_zmax_murActionPerformed

  private void zmax_pml8ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zmax_pml8ActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_zmax_pml8ActionPerformed

  private void bc_reset_defaultActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_bc_reset_defaultActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_bc_reset_defaultActionPerformed

  private void excitation_reset_defaultActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_excitation_reset_defaultActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_excitation_reset_defaultActionPerformed

  private void fdtd_reset_defaultActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fdtd_reset_defaultActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_fdtd_reset_defaultActionPerformed

  private void fdtd_number_of_timestepsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fdtd_number_of_timestepsActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_fdtd_number_of_timestepsActionPerformed

  private void pcb_reset_defaultActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pcb_reset_defaultActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_pcb_reset_defaultActionPerformed

  private void pcb_presetsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pcb_presetsActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_pcb_presetsActionPerformed

  private void aboutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_aboutActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_aboutActionPerformed

  private void modelViewMouseWheelMoved(java.awt.event.MouseWheelEvent evt)//GEN-FIRST:event_modelViewMouseWheelMoved
  {
    // TODO add your handling code here:
    //System.out.println(evt);
    if( modelView.isControlDown() ) {
      modelView.getRenderer().camera.rotateAroundLookVector(4*evt.getWheelRotation());
    } else {
      modelView.getRenderer().camera.moveFwd(-1*evt.getWheelRotation()*modelView.getRenderer().camera.getMoveSpeed() * 10.0);
      modelView.getRenderer().zoomOrtho( evt.getWheelRotation() );
    }
  }//GEN-LAST:event_modelViewMouseWheelMoved

  private void plot_efield_modelviewActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_plot_efield_modelviewActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_plot_efield_modelviewActionPerformed

  private void dump_efield_vtrActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dump_efield_vtrActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_dump_efield_vtrActionPerformed

  private void start_simActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_start_simActionPerformed
  {
    modelView.getRenderer().runSimulation();
  }//GEN-LAST:event_start_simActionPerformed

  private void abort_simActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_abort_simActionPerformed
  {
    modelView.getRenderer().abortSimulation();
  }//GEN-LAST:event_abort_simActionPerformed

  private void dump_td_volt_currentActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dump_td_volt_currentActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_dump_td_volt_currentActionPerformed

  private void view_meshActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_view_meshActionPerformed
  {
    modelView.getRenderer().setMesh( view_mesh.isSelected() );
  }//GEN-LAST:event_view_meshActionPerformed

  private void view_airboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_view_airboxActionPerformed
  {
    modelView.getRenderer().setAirBox( view_airbox.isSelected() );
  }//GEN-LAST:event_view_airboxActionPerformed

  private void view_pmlActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_view_pmlActionPerformed
  {
    modelView.getRenderer().setPML( view_pml.isSelected() );
  }//GEN-LAST:event_view_pmlActionPerformed

  private void openems_nthreadsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openems_nthreadsActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_openems_nthreadsActionPerformed

  private void number_threadsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_number_threadsActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_number_threadsActionPerformed

  private void filter_edgesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_filter_edgesActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_filter_edgesActionPerformed

  private void mesh_reset_defaultActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mesh_reset_defaultActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_mesh_reset_defaultActionPerformed

  private void simout_reset_defaultActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_simout_reset_defaultActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_simout_reset_defaultActionPerformed

  private void mesh_applyActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mesh_applyActionPerformed
  {
    modelView.getRenderer().meshApplySettings();
  }//GEN-LAST:event_mesh_applyActionPerformed

  private void microstripcalcActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_microstripcalcActionPerformed
  {
    mstrip_frame.setVisible(true);
  }//GEN-LAST:event_microstripcalcActionPerformed

  private void kappacalcActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_kappacalcActionPerformed
  {
    // TODO add your handling code here:
  }//GEN-LAST:event_kappacalcActionPerformed

  private void view_pecActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_view_pecActionPerformed
  {
    // TODO add your handling code here:
    exec_external ee = new exec_external();
    String paraview_path = fld.getPath(fileLocationDiaglog.JPCBSIM_PARAVIEW_BIN);

    ee.execute(1,
               paraview_path+" "+simulation.sim_path+simulation.sim_name+"/PEC_dump.vtp",
               simulation.sim_path+simulation.sim_name);

  }//GEN-LAST:event_view_pecActionPerformed

  private void proj_treeValueChanged(javax.swing.event.TreeSelectionEvent evt)//GEN-FIRST:event_proj_treeValueChanged
  {
    tree_node_selected=true;

  }//GEN-LAST:event_proj_treeValueChanged

    private void use_lorentzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_use_lorentzActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_use_lorentzActionPerformed

  private void loadSimulation(String simname)
  {
    try {
      setStatus("Loading project "+simname);
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      //first time loading software, prompt for locations
      String project_dir = fld.getPath(fileLocationDiaglog.JPCBSIM_PROJECT_DIR);
      if(project_dir==null || project_dir.length()==0) {
        fld.show();
        //try again
        project_dir = fld.getPath(fileLocationDiaglog.JPCBSIM_PROJECT_DIR);
        if(project_dir==null || project_dir.length()==0) {
          System.exit(0);
        }
      }

      if(simname.equals("default")) {
          //need to copy default geometry from resource file
          try {
            File file = new File(project_dir+"/default");
            if(!file.exists()) file.mkdir(); 
  
            file = new File(project_dir+"/default/default_pcb.m");
            if(!file.exists()) {
              System.out.println("copying default project");
              InputStream is = getClass().getResourceAsStream("/jPCBSim/default_pcb.m");
              FileWriter fw = new FileWriter( project_dir+"/default/default_pcb.m");
              int dat = 0;
              while(dat>=0) {
                dat = is.read();
                fw.write(dat);
              }
              is.close();
              fw.close();
            }
            file = new File(project_dir+"/default/port_ut1");
            if(!file.exists()) {
              System.out.println("copying default project");
              InputStream is = getClass().getResourceAsStream("/jPCBSim/port_ut1");
              FileWriter fw = new FileWriter( project_dir+"/default/port_ut1");
              int dat = 0;
              while(dat>=0) {
                dat = is.read();
                fw.write(dat);
              }
              is.close();
              fw.close();
            }
            file = new File(project_dir+"/default/port_it1");
            if(!file.exists()) {
              System.out.println("copying default project");
              InputStream is = getClass().getResourceAsStream("/jPCBSim/port_it1");
              FileWriter fw = new FileWriter( project_dir+"/default/port_it1");
              int dat = 0;
              while(dat>=0) {
                dat = is.read();
                fw.write(dat);
              }
              is.close();
              fw.close();
            }
          } catch(Exception e) {
            e.printStackTrace();
          }
      }

      simulation = new Simulation(this,simname);
      modelView.getRenderer().setPCBModel(null);
      Thread.sleep(100);



      modelView.getRenderer().camera.setEye(new Vector3d(0, 0, -2));
      modelView.getRenderer().camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
      modelView.getRenderer().camera.rotateAroundLookVector(90.0);
      modelView.getRenderer().camera.updateView();
      modelView.getRenderer().camera.setEye(new Vector3d(0, 0, -2));
      modelView.getRenderer().camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
      modelView.getRenderer().camera.rotateAroundLookVector(90.0);
      modelView.getRenderer().camera.updateView();
      modelView.getRenderer().camera.rotateAroundRightVector(-45);
      modelView.getRenderer().camera.updateView();

      pcbmodel = new pcb_model(simulation.sim_path+simulation.sim_name+"/"+simulation.sim_name+"_pcb.m", simulation);
      if(pcbmodel!=null) modelView.getRenderer().setPCBModel(pcbmodel);

      setTitle("jPCBSim 2018-09-15  "+simname);
    } catch(Exception e) {
    }
    finally {
      updateSimFields(simulation);
    }


  }

  public void fftCompleted() {
      setCursor(Cursor.getDefaultCursor());
  }
  public void fftStarted() {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  private void updateTreeList()
  {
    try {
      RootNode = new javax.swing.tree.DefaultMutableTreeNode("Projects");
      File[] files = simulation.getProjectList();
      if(files!=null) {
        for(int i=0; i<files.length; i++) {
          File f = files[i];
          if(f!=null && f.isDirectory()) {
            javax.swing.tree.DefaultMutableTreeNode treeNode = new javax.swing.tree.DefaultMutableTreeNode(f.getName());
            RootNode.add(treeNode);
          }
        }
      }
      proj_tree.setModel(new javax.swing.tree.DefaultTreeModel(RootNode));

    } catch(Exception e) {
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(final String args[])
  {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(PCBSimClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(PCBSimClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(PCBSimClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(PCBSimClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        frame = new PCBSimClient(args);
        frame.setVisible(true);
      }
    });
  }

  public void setStatus(String status) {
    jLabel1.setText("Status:  "+status);
  }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Exit;
    private javax.swing.JMenuItem Preferences;
    private javax.swing.JButton abort_sim;
    private javax.swing.JMenuItem about;
    private javax.swing.JTextField airbox_epsilon;
    private javax.swing.JTextField airbox_kappa;
    private javax.swing.JTextField airbox_mue;
    private javax.swing.JTextField airbox_wavelength1;
    private javax.swing.ButtonGroup bc_bg_xmax;
    private javax.swing.ButtonGroup bc_bg_xmin;
    private javax.swing.ButtonGroup bc_bg_ymax;
    private javax.swing.ButtonGroup bc_bg_ymin;
    private javax.swing.ButtonGroup bc_bg_zmax;
    private javax.swing.ButtonGroup bc_bg_zmin;
    private javax.swing.ButtonGroup bc_gpulse;
    private javax.swing.JButton bc_reset_default;
    private javax.swing.ButtonGroup bg_port_mode;
    private javax.swing.JPanel boundary_panel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel config_panel;
    private javax.swing.JRadioButton custom_exc_rb;
    private javax.swing.JCheckBox do_box_vias;
    private javax.swing.JCheckBox do_pec_debug;
    private javax.swing.JCheckBox do_touchstone;
    private javax.swing.JCheckBox dump_efield_vtr;
    private javax.swing.JCheckBox dump_td_volt_current;
    private javax.swing.JMenu editmenu;
    private javax.swing.JLabel er_presets;
    private javax.swing.JTextField exc_f0_tf;
    private javax.swing.JTextField exc_fc_tf;
    private javax.swing.JPanel excitation_panel;
    private javax.swing.JButton excitation_reset_default;
    private javax.swing.JMenu export_menu;
    private javax.swing.JMenuItem export_qucs;
    private javax.swing.JTextField fdtd_end_level;
    private javax.swing.JTextField fdtd_max_freq;
    private javax.swing.JTextField fdtd_number_of_timesteps;
    private javax.swing.JPanel fdtd_panel;
    private javax.swing.JButton fdtd_reset_default;
    private javax.swing.JMenu filemenu;
    private javax.swing.JCheckBox filter_edges;
    private javax.swing.JRadioButton gaussian_pulse_rb;
    private javax.swing.JMenu help;
    private javax.swing.JMenuItem import_hyp2mat;
    private javax.swing.JMenu import_menu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JMenuItem kappacalc;
    private javax.swing.JPanel main_panel;
    private javax.swing.JButton mesh_apply;
    private javax.swing.JPanel mesh_panel;
    private javax.swing.JTextField mesh_res_wavelength;
    private javax.swing.JButton mesh_reset_default;
    private javax.swing.JTextField mesh_zlines;
    private javax.swing.JRadioButton microstrip;
    private javax.swing.JMenuItem microstripcalc;
    private jPCBSim.PCBPanel modelView;
    private javax.swing.JCheckBox number_threads;
    private javax.swing.JTextField openems_nthreads;
    private javax.swing.JPanel openems_panel;
    private javax.swing.JTextField pcb_er;
    private javax.swing.JTextField pcb_kappa;
    private javax.swing.JTextField pcb_lorentzpolefreq;
    private javax.swing.JTextField pcb_mue;
    private javax.swing.JPanel pcb_panel;
    private javax.swing.JTextField pcb_plasmafreq;
    private javax.swing.JComboBox pcb_presets;
    private javax.swing.JTextField pcb_relaxtime;
    private javax.swing.JButton pcb_reset_default;
    private javax.swing.JTextField pcb_thickness_inches;
    private javax.swing.JCheckBox plot_efield_modelview;
    private javax.swing.JTree proj_tree;
    private javax.swing.JButton simout_reset_default;
    private javax.swing.JRadioButton sinusoidal_rb;
    private javax.swing.JPanel sp_panel;
    private javax.swing.JTextField sparm_update_freq;
    private javax.swing.JButton start_sim;
    private javax.swing.JPanel status_panel;
    private javax.swing.JMenu tools;
    private javax.swing.JTextField tp1_resistance;
    private javax.swing.JTextField tp2_resistance;
    private javax.swing.JCheckBox use_lorentz;
    private javax.swing.JCheckBox view_airbox;
    private javax.swing.JCheckBox view_mesh;
    private javax.swing.JButton view_pec;
    private javax.swing.JCheckBox view_pml;
    private javax.swing.JCheckBox write_openems_log;
    private javax.swing.JRadioButton xmax_mur;
    private javax.swing.JRadioButton xmax_pec;
    private javax.swing.JRadioButton xmax_pmc;
    private javax.swing.JRadioButton xmax_pml8;
    private javax.swing.JRadioButton xmin_mur;
    private javax.swing.JRadioButton xmin_pec;
    private javax.swing.JRadioButton xmin_pmc;
    private javax.swing.JRadioButton xmin_pml8;
    private javax.swing.JRadioButton ymax_mur;
    private javax.swing.JRadioButton ymax_pec;
    private javax.swing.JRadioButton ymax_pmc;
    private javax.swing.JRadioButton ymax_pml8;
    private javax.swing.JRadioButton ymin_mur;
    private javax.swing.JRadioButton ymin_pec;
    private javax.swing.JRadioButton ymin_pmc;
    private javax.swing.JRadioButton ymin_pml8;
    private javax.swing.JRadioButton zmax_mur;
    private javax.swing.JRadioButton zmax_pec;
    private javax.swing.JRadioButton zmax_pmc;
    private javax.swing.JRadioButton zmax_pml8;
    private javax.swing.JRadioButton zmin_mur;
    private javax.swing.JRadioButton zmin_pec;
    private javax.swing.JRadioButton zmin_pmc;
    private javax.swing.JRadioButton zmin_pml8;
    // End of variables declaration//GEN-END:variables
}
