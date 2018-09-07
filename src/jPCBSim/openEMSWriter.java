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

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.*;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBContext;
import javax.xml.stream.*;

import java.io.*;
import java.util.*;
import java.awt.geom.*;

public class openEMSWriter
{

  private int port_count=0;
  boolean do_swap_ports=false;
  String openems_path="";

  Simulation simulation;

  public openEMSWriter(Simulation sim)
  {
    this.simulation = sim;
    fileLocationDiaglog fld = new fileLocationDiaglog();
    openems_path = fld.getPath(fileLocationDiaglog.JPCBSIM_OPENEMS_BIN);
  }

  //public static void main(String[] args) throws Exception {
  // new openEMSWriter().writeOpenEMSConfig(System.out, null);
  //}

  public void writeOpenEMSConfig(pcb_model pcbmodel)
  {

    port_count=0;

    mesh_model meshmodel = pcbmodel.getMeshModel();

    ObjectFactory of =new ObjectFactory();

    //Top level OpenEMS
    OpenEMS openems = of.createOpenEMS();

    //FDTD
    OpenEMS.FDTD fdtd = of.createOpenEMSFDTD();
    fdtd.setFMax(simulation.fdtd_fmax);
    fdtd.setNumberOfTimesteps(simulation.fdtd_maxsteps);
    fdtd.setOverSampling(simulation.fdtd_oversample);
    fdtd.setEndCriteria(simulation.fdtd_endlevel);
    openems.getExcitationOrFillColorOrEdgeColor().add(fdtd);

    //Excitation
    Excitation fdtd_ex = of.createExcitation();
    fdtd_ex.setType(simulation.excitation_waveform_type);
    //fdtd_ex.setType("0"); //gaussian

    fdtd_ex.setF0(simulation.excitation_f0);
    fdtd_ex.setFc(simulation.excitation_fc);
    fdtd.getExcitation().add(fdtd_ex);

    //Boundary Condition
    OpenEMS.FDTD.BoundaryCond bc = of.createOpenEMSFDTDBoundaryCond();
    bc.setXmin(simulation.boundary_condition_xmin);
    bc.setXmax(simulation.boundary_condition_xmax);
    bc.setYmin(simulation.boundary_condition_ymin);
    bc.setYmax(simulation.boundary_condition_ymax);
    bc.setZmin(simulation.boundary_condition_zmin);
    bc.setZmax(simulation.boundary_condition_zmax);
    fdtd.getBoundaryCond().add(bc);


    //Mesh
    OpenEMS.ContinuousStructure.RectilinearGrid mesh = of.createOpenEMSContinuousStructureRectilinearGrid();
    OpenEMS.ContinuousStructure.RectilinearGrid.XLines xlines = of.createOpenEMSContinuousStructureRectilinearGridXLines();
    OpenEMS.ContinuousStructure.RectilinearGrid.YLines ylines = of.createOpenEMSContinuousStructureRectilinearGridYLines();
    OpenEMS.ContinuousStructure.RectilinearGrid.ZLines zlines = of.createOpenEMSContinuousStructureRectilinearGridZLines();
    mesh.setDeltaUnit("1");

    if(meshmodel!=null) {
      xlines.setQty( new Integer( meshmodel.getNumberXLines()).toString() );
      ylines.setQty( new Integer( meshmodel.getNumberYLines()).toString() );
      zlines.setQty( new Integer( meshmodel.getNumberZLines()).toString() );
      xlines.setValue( meshmodel.getXLines() );
      ylines.setValue( meshmodel.getYLines() );
      zlines.setValue( meshmodel.getZLines() );
      mesh.getXLines().add(xlines);
      mesh.getYLines().add(ylines);
      mesh.getZLines().add(zlines);
    }

    //ContinuousStructure
    OpenEMS.ContinuousStructure cs = of.createOpenEMSContinuousStructure();
    cs.setCoordSystem("0");
    cs.getRectilinearGrid().add(mesh);
    openems.getExcitationOrFillColorOrEdgeColor().add(cs);

    //Properties
    OpenEMS.ContinuousStructure.Properties props = of.createOpenEMSContinuousStructureProperties();
    cs.getProperties().add(props);


    int id = 0;


    Vector pcb_obj_v = pcbmodel.getPCBObjectVector();
    Enumeration<pcb_object> e = pcb_obj_v.elements();
    while(e.hasMoreElements()) {
      pcb_object po = e.nextElement();

      if(po.getMaterial().getMaterialType() == Material.DIELECTRIC_BOX) {
        //pcb material dielectric
        double xc = 0.0;
        double yc = 0.0;
        double zc = 0.0;
        double xd = 0.0;
        double yd = 0.0;
        double zd = 0.0;

        if(simulation.do_lorentz) {
          OpenEMS.ContinuousStructure.Properties.LorentzMaterial pcb_dielectric = of.createOpenEMSContinuousStructurePropertiesLorentzMaterial();
          props.getLorentzMaterial().add(pcb_dielectric);

          pcb_dielectric.setIsotropy("1");
          FillColor pcb_fill_color = of.createFillColor();
          pcb_fill_color.setR("0");
          pcb_fill_color.setG("100");
          pcb_fill_color.setB("0");
          pcb_fill_color.setA("255");
          pcb_dielectric.getFillColor().add(pcb_fill_color);
          EdgeColor pcb_edge_color = of.createEdgeColor();
          pcb_edge_color.setR("0");
          pcb_edge_color.setG("150");
          pcb_edge_color.setB("0");
          pcb_edge_color.setA("255");
          pcb_dielectric.getEdgeColor().add(pcb_edge_color);
          Primitives pcb_geom = of.createPrimitives();
          pcb_dielectric.getPrimitives().add(pcb_geom);
          Primitives.Box pcb_box = of.createPrimitivesBox();
          pcb_geom.getBox().add(pcb_box);


          if(pcbmodel!=null) {
            pcb_dielectric.setID(Integer.toString(id++));
            pcb_dielectric.setName(po.getMaterial().getName());
            pcb_box.setPriority("100");

            Primitives.Box.P1 p1 = of.createPrimitivesBoxP1();
            Primitives.Box.P2 p2 = of.createPrimitivesBoxP2();
            pcb_box.getP1().add(p1);
            pcb_box.getP2().add(p2);

            pcb_object pobj = pcbmodel.getDielectricObject();
            xc = pobj.getMaterial().box_xc;
            yc = pobj.getMaterial().box_yc;
            zc = pobj.getMaterial().box_zc;
            xd = pobj.getMaterial().box_xd;
            yd = pobj.getMaterial().box_yd;
            zd = pobj.getMaterial().box_zd;


            p1.setX(new Double(xc-xd/2.0).toString());
            p1.setY(new Double(yc-yd/2.0).toString());
            p1.setZ(new Double(zc-zd/2.0).toString());
            p2.setX(new Double(xc+xd/2.0).toString());
            p2.setY(new Double(yc+yd/2.0).toString());
            p2.setZ(new Double(zc+zd/2.0).toString());
          }

          OpenEMS.ContinuousStructure.Properties.LorentzMaterial.Property pcb_property = of.createOpenEMSContinuousStructurePropertiesLorentzMaterialProperty();

          pcb_dielectric.getProperty().add(pcb_property);
          pcb_property.setEpsilon( simulation.pcb_prop_epsilon );
          pcb_property.setMue( simulation.pcb_prop_mue );
          pcb_property.setKappa( simulation.pcb_prop_kappa );

          //t6
          pcb_property.setEpsilonPlasmaFrequency( simulation.pcb_prop_plasmafreq );
          pcb_property.setEpsilonLorPoleFrequency( simulation.pcb_prop_lorentz_pole_freq );
          pcb_property.setEpsilonRelaxTime( simulation.pcb_prop_relaxation_time );
        } else {
          OpenEMS.ContinuousStructure.Properties.Material pcb_dielectric = of.createOpenEMSContinuousStructurePropertiesMaterial();
          props.getMaterial().add(pcb_dielectric);
          pcb_dielectric.setIsotropy("1");
          FillColor pcb_fill_color = of.createFillColor();
          pcb_fill_color.setR("0");
          pcb_fill_color.setG("100");
          pcb_fill_color.setB("0");
          pcb_fill_color.setA("255");
          pcb_dielectric.getFillColor().add(pcb_fill_color);
          EdgeColor pcb_edge_color = of.createEdgeColor();
          pcb_edge_color.setR("0");
          pcb_edge_color.setG("150");
          pcb_edge_color.setB("0");
          pcb_edge_color.setA("255");
          pcb_dielectric.getEdgeColor().add(pcb_edge_color);
          Primitives pcb_geom = of.createPrimitives();
          pcb_dielectric.getPrimitives().add(pcb_geom);
          Primitives.Box pcb_box = of.createPrimitivesBox();
          pcb_geom.getBox().add(pcb_box);

          if(pcbmodel!=null) {
            pcb_dielectric.setID(Integer.toString(id++));
            pcb_dielectric.setName(po.getMaterial().getName());
            pcb_box.setPriority("100");

            Primitives.Box.P1 p1 = of.createPrimitivesBoxP1();
            Primitives.Box.P2 p2 = of.createPrimitivesBoxP2();
            pcb_box.getP1().add(p1);
            pcb_box.getP2().add(p2);

            pcb_object pobj = pcbmodel.getDielectricObject();
            xc = pobj.getMaterial().box_xc;
            yc = pobj.getMaterial().box_yc;
            zc = pobj.getMaterial().box_zc;
            xd = pobj.getMaterial().box_xd;
            yd = pobj.getMaterial().box_yd;
            zd = pobj.getMaterial().box_zd;


            p1.setX(new Double(xc-xd/2.0).toString());
            p1.setY(new Double(yc-yd/2.0).toString());
            p1.setZ(new Double(zc-zd/2.0).toString());
            p2.setX(new Double(xc+xd/2.0).toString());
            p2.setY(new Double(yc+yd/2.0).toString());
            p2.setZ(new Double(zc+zd/2.0).toString());
          }

          OpenEMS.ContinuousStructure.Properties.Material.Property pcb_property = of.createOpenEMSContinuousStructurePropertiesMaterialProperty();

          pcb_dielectric.getProperty().add(pcb_property);
          pcb_property.setEpsilon( simulation.pcb_prop_epsilon );
          pcb_property.setMue( simulation.pcb_prop_mue );
          pcb_property.setKappa( simulation.pcb_prop_kappa );
        }


        if( simulation.do_dump_efield ) {
          //add E/H field dumpbox
          OpenEMS.ContinuousStructure.Properties.DumpBox dump_box = of.createOpenEMSContinuousStructurePropertiesDumpBox();
          Primitives dump_geom = of.createPrimitives();
          dump_box.getPrimitives().add(dump_geom);
          Primitives.Box dbox = of.createPrimitivesBox();
          dump_geom.getBox().add(dbox);
          props.getDumpBox().add(dump_box);
          dump_box.setID(Integer.toString(id++));
          dump_box.setName("Et_");
          dump_box.setDumpMode("2");
          dbox.setPriority("0");

          Primitives.Box.P1 det_p1 = of.createPrimitivesBoxP1();
          Primitives.Box.P2 det_p2 = of.createPrimitivesBoxP2();
          det_p1.setX(new Double(xc-xd/2.0).toString());
          det_p1.setY(new Double(yc-yd/2.0).toString());
          det_p1.setZ(new Double(zc+zd/2.0).toString());
          det_p2.setX(new Double(xc+xd/2.0).toString());
          det_p2.setY(new Double(yc+yd/2.0).toString());
          det_p2.setZ(new Double(zc+zd/2.0).toString());

          dbox.getP1().add(det_p1);
          dbox.getP2().add(det_p2);
        }
        if( simulation.do_dump_hfield ) {
        }
      } else if(po.getMaterial().getMaterialType() == Material.COPPER_POLYGON) {
        //top copper metal
        OpenEMS.ContinuousStructure.Properties.Metal pcb_copper = of.createOpenEMSContinuousStructurePropertiesMetal();
        if(pcbmodel!=null) {
          pcb_copper.setID(Integer.toString(id++));
          pcb_copper.setName(po.getMaterial().getName());
        }
        props.getMetal().add(pcb_copper);
        FillColor copper_fill_color = of.createFillColor();
        copper_fill_color.setR("156");
        copper_fill_color.setG("120");
        copper_fill_color.setB("19");
        copper_fill_color.setA("255");
        pcb_copper.getFillColor().add(copper_fill_color);
        EdgeColor copper_edge_color = of.createEdgeColor();
        copper_edge_color.setR("156");
        copper_edge_color.setG("120");
        copper_edge_color.setB("19");
        copper_edge_color.setA("255");
        pcb_copper.getEdgeColor().add(copper_edge_color);

        Primitives copper_geom = of.createPrimitives();
        pcb_copper.getPrimitives().add(copper_geom);

        if(pcbmodel!=null) {
          Primitives.Polygon copper_poly1 = of.createPrimitivesPolygon();
          copper_geom.getPolygon().add(copper_poly1);
          copper_poly1.setPriority("200");
          copper_poly1.setElevation(new Double(po.getMaterial().getElevation()).toString());
          copper_poly1.setNormDir("2");

          copper_poly1.setQtyVertices(new Integer(po.getSize()).toString());

          Enumeration<Point2D.Double> ev = po.getGeometry();
          while(ev.hasMoreElements()) {
            Point2D.Double p = ev.nextElement();
            Primitives.Polygon.Vertex v1 = of.createPrimitivesPolygonVertex();
            v1.setX1(new Double(p.getX()).toString());
            v1.setX2(new Double(p.getY()).toString());
            copper_poly1.getVertex().add(v1);
          }
        }

      } else if(po.getMaterial().getMaterialType() == Material.EXCITATION_BOX) {


        /*
                    port_count++;
                    int use_port = port_count;
                    if(do_swap_ports) {
                      if(port_count==1) use_port=2;
                      if(port_count==2) use_port=1;
                    }

                    //use name preferably
                    if(po.getMaterial().getName().indexOf("1")>=0) {
                      use_port=1;
                      port_count=1;
                    }
                    else if(po.getMaterial().getName().indexOf("2")>=0 || port_count==2) {
                      use_port=2;
                    }
        */

        //force use of names TP1 and TP2
        if(po.getMaterial().getName().indexOf("TP1")>=0) port_count=1;
        else if(po.getMaterial().getName().indexOf("TP2")>=0) port_count=2;
        else continue;

        //lumped element termination resistor
        OpenEMS.ContinuousStructure.Properties.LumpedElement term_le = of.createOpenEMSContinuousStructurePropertiesLumpedElement();
        Primitives term_geom = of.createPrimitives();
        term_le.getPrimitives().add(term_geom);
        Primitives.Box term_box = of.createPrimitivesBox();
        term_geom.getBox().add(term_box);
        props.getLumpedElement().add(term_le);
        if(port_count==1) {
          term_le.setR(simulation.port1_resistance);
          term_le.setC(simulation.port1_capacitance);
          term_le.setL(simulation.port1_inductance);
        } else {
          term_le.setR(simulation.port2_resistance);
          term_le.setC(simulation.port2_capacitance);
          term_le.setL(simulation.port2_inductance);
        }
        term_le.setCaps("1");
        term_le.setDirection("2");

        String r = "";
        String g = "";
        String b = "";
        String a = "";


        if(port_count==1) {
          r = "255";
          g = "0";
          b = "0";
          a = "255";
        } else {
          r = "0";
          g = "0";
          b = "255";
          a = "255";
        }

        FillColor fill_color = of.createFillColor();
        fill_color.setR(r);
        fill_color.setG(g);
        fill_color.setB(b);
        fill_color.setA(a);
        term_le.getFillColor().add(fill_color);
        EdgeColor edge_color = of.createEdgeColor();
        edge_color.setR(r);
        edge_color.setG(g);
        edge_color.setB(b);
        edge_color.setA(a);
        term_le.getEdgeColor().add(edge_color);

        //one of the ports has to be excitation, well pick the first one for now.  let the user swap in the front-end
        if(port_count==1) {
          Excitation port_ex = of.createExcitation();
          port_ex.setName("port_excite_1");
          port_ex.setType("0");
          port_ex.setExcite("0,0,"+simulation.port1_excitation_voltage);  //need to determine z-direction??
          port_ex.getPrimitives().add(term_geom);
          port_ex.getFillColor().add(fill_color);
          port_ex.getEdgeColor().add(edge_color);
          props.getExcitation().add(port_ex);
        }

        term_le.setID(Integer.toString(id++));
        term_le.setName("termination_resistor_"+Integer.toString(port_count));
        term_box.setPriority("999");

        Primitives.Box.P1 p1 = of.createPrimitivesBoxP1();
        Primitives.Box.P2 p2 = of.createPrimitivesBoxP2();
        term_box.getP1().add(p1);
        term_box.getP2().add(p2);

        double xc = po.getMaterial().box_xc;
        double yc = po.getMaterial().box_yc;
        double zc = po.getMaterial().box_zc;
        double xd = po.getMaterial().box_xd;
        double yd = po.getMaterial().box_yd;
        double zd = po.getMaterial().box_zd;


        p1.setX(new Double(xc-xd/2.0).toString());
        p1.setY(new Double(yc-yd/2.0).toString());
        p1.setZ(new Double(zd).toString());
        p2.setX(new Double(xc+xd/2.0).toString());
        p2.setY(new Double(yc+yd/2.0).toString());
        p2.setZ(new Double(0).toString());

        //add voltage probe boxes that are inside the lumped elements by +/- 0.0005 meters (0.5 millimeters)
        OpenEMS.ContinuousStructure.Properties.ProbeBox probe_box = of.createOpenEMSContinuousStructurePropertiesProbeBox();
        Primitives probe_geom = of.createPrimitives();
        probe_box.getPrimitives().add(probe_geom);
        Primitives.Box pbox = of.createPrimitivesBox();
        probe_geom.getBox().add(pbox);
        props.getProbeBox().add(probe_box);
        probe_box.setID(Integer.toString(id++));
        probe_box.setName("port_ut"+Integer.toString(port_count));
        probe_box.setType("0"); //voltage
        probe_box.setWeight("1");
        pbox.setPriority("999");

        Primitives.Box.P1 port_u_p1 = of.createPrimitivesBoxP1();
        Primitives.Box.P2 port_u_p2 = of.createPrimitivesBoxP2();
        port_u_p1.setX(new Double((xc-xd/2.0)).toString());
        port_u_p1.setY(new Double((yc-yd/2.0)).toString());
        port_u_p1.setZ(new Double(zd).toString());
        port_u_p2.setX(new Double((xc+xd/2.0)).toString());
        port_u_p2.setY(new Double((yc+yd/2.0)).toString());
        port_u_p2.setZ(new Double(0).toString());
        pbox.getP1().add(port_u_p1);
        pbox.getP2().add(port_u_p2);

        //add current probe boxes that are centered in the z-axis
        probe_box = of.createOpenEMSContinuousStructurePropertiesProbeBox();
        probe_geom = of.createPrimitives();
        probe_box.getPrimitives().add(probe_geom);
        pbox = of.createPrimitivesBox();
        probe_geom.getBox().add(pbox);
        props.getProbeBox().add(probe_box);
        probe_box.setID(Integer.toString(id++));
        probe_box.setName("port_it"+Integer.toString(port_count));
        probe_box.setType("1"); //current
        probe_box.setWeight("-1");
        pbox.setPriority("999");

        Primitives.Box.P1 port_i_p1 = of.createPrimitivesBoxP1();
        Primitives.Box.P2 port_i_p2 = of.createPrimitivesBoxP2();
        port_i_p1.setX(new Double((xc-xd/2.0)).toString());
        port_i_p1.setY(new Double((yc-yd/2.0)).toString());
        port_i_p1.setZ(new Double(zd/2.0).toString());
        port_i_p2.setX(new Double((xc+xd/2.0)).toString());
        port_i_p2.setY(new Double((yc+yd/2.0)).toString());
        port_i_p2.setZ(new Double(zd/2.0).toString());
        pbox.getP1().add(port_i_p1);
        pbox.getP2().add(port_i_p2);

      } else if(po.getMaterial().getMaterialType() == Material.COPPER_VIA) {

        if(simulation.do_box_vias) {
          //via box
          OpenEMS.ContinuousStructure.Properties.Metal via_metal = of.createOpenEMSContinuousStructurePropertiesMetal();
          via_metal.setID(Integer.toString(id++));
          via_metal.setName(po.getMaterial().getName());
          props.getMetal().add(via_metal);

          Primitives via_geom = of.createPrimitives();
          via_metal.getPrimitives().add(via_geom);
          Primitives.Box via_box = of.createPrimitivesBox();
          via_geom.getBox().add(via_box);
          via_box.setPriority("300");

          Material m = po.getMaterial();

          Primitives.Box.P1 via_boxp1 = of.createPrimitivesBoxP1();
          Primitives.Box.P2 via_boxp2 = of.createPrimitivesBoxP2();
          via_boxp1.setX(new Double(m.via_sx-m.via_radius*0.707).toString());
          via_boxp1.setY(new Double(m.via_sy-m.via_radius*0.707).toString());
          //via_boxp1.setZ(new Double(0-0.00001).toString());
          via_boxp1.setZ(new Double(0).toString());
          via_boxp2.setX(new Double(m.via_ex+m.via_radius*0.707).toString());
          via_boxp2.setY(new Double(m.via_ey+m.via_radius*0.707).toString());
          //via_boxp2.setZ(new Double(m.via_ez+0.00001).toString());
          via_boxp2.setZ(new Double(m.via_ez).toString());
          via_box.getP1().add(via_boxp1);
          via_box.getP2().add(via_boxp2);

          FillColor copper_fill_color = of.createFillColor();
          copper_fill_color.setR("240");
          copper_fill_color.setG("240");
          copper_fill_color.setB("240");
          copper_fill_color.setA("255");
          via_metal.getFillColor().add(copper_fill_color);
          EdgeColor copper_edge_color = of.createEdgeColor();
          copper_edge_color.setR("240");
          copper_edge_color.setG("240");
          copper_edge_color.setB("240");
          copper_edge_color.setA("255");
          via_metal.getEdgeColor().add(copper_edge_color);
        } else {
          //via
          OpenEMS.ContinuousStructure.Properties.Metal via_metal = of.createOpenEMSContinuousStructurePropertiesMetal();

          if(pcbmodel!=null) {
            via_metal.setID(Integer.toString(id++));
            via_metal.setName(po.getMaterial().getName());
            props.getMetal().add(via_metal);

            Primitives via_geom = of.createPrimitives();
            via_metal.getPrimitives().add(via_geom);
            Primitives.Cylinder via_cylinder = of.createPrimitivesCylinder();

            Material m = po.getMaterial();

            via_cylinder.setRadius( new Double(m.via_radius).toString() );
            via_cylinder.setPriority("300");
            via_geom.getCylinder().add(via_cylinder);
            Primitives.Cylinder.P1 vp1 = of.createPrimitivesCylinderP1();
            Primitives.Cylinder.P2 vp2 = of.createPrimitivesCylinderP2();
            via_cylinder.getP1().add(vp1);
            via_cylinder.getP2().add(vp2);
            vp1.setX( new Double(m.via_sx).toString());
            vp1.setY( new Double(m.via_sy).toString());
            vp1.setZ( new Double(m.via_sz).toString());
            vp2.setX( new Double(m.via_ex).toString());
            vp2.setY( new Double(m.via_ey).toString());
            vp2.setZ( new Double(m.via_ez).toString());

            FillColor copper_fill_color = of.createFillColor();
            copper_fill_color.setR("240");
            copper_fill_color.setG("240");
            copper_fill_color.setB("240");
            copper_fill_color.setA("255");
            via_metal.getFillColor().add(copper_fill_color);
            EdgeColor copper_edge_color = of.createEdgeColor();
            copper_edge_color.setR("240");
            copper_edge_color.setG("240");
            copper_edge_color.setB("240");
            copper_edge_color.setA("255");
            via_metal.getEdgeColor().add(copper_edge_color);
          }
        }
      }

    }

    try {
      //write the XML configuration out
      File file_out = new File(simulation.sim_path+simulation.sim_name+"/openems_simulation.xml");
      FileOutputStream ostream = new FileOutputStream(file_out);

      //XMLStreamWriter xmlStreamWriter =
       //            XMLOutputFactory.newInstance().createXMLStreamWriter( ostream );


      JAXBContext context = JAXBContext.newInstance(OpenEMS.class);
      Marshaller m = context.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

      m.marshal(openems, ostream);

      System.out.println("wrote configuration.");

      File file_del = new File(simulation.sim_path+simulation.sim_name+"/port_ut1");
      if(file_del.exists()) file_del.delete();
      file_del = new File(simulation.sim_path+simulation.sim_name+"/port_ut2");
      if(file_del.exists()) file_del.delete();
      file_del = new File(simulation.sim_path+simulation.sim_name+"/port_it1");
      if(file_del.exists()) file_del.delete();
      file_del = new File(simulation.sim_path+simulation.sim_name+"/port_it2");
      if(file_del.exists()) file_del.delete();
      file_del = new File(simulation.sim_path+simulation.sim_name+"/ABORT");
      if(file_del.exists()) file_del.delete();

      String openems_options = "";
      if( simulation.do_sim_verbose ) openems_options = openems_options.concat(" -v");
      if( simulation.do_debug_pec )  openems_options = openems_options.concat(" --debug-PEC");
      if( simulation.do_fixed_threads) openems_options = openems_options.concat(" --numThreads="+simulation.sim_threads);
      new exec_external().execute(0, openems_path+" "+simulation.sim_path+simulation.sim_name+"/openems_simulation.xml "+openems_options,
                                  simulation.sim_path+simulation.sim_name );
    } catch(Exception ee) {
      ee.printStackTrace();
    }

  }

  public void readOpenEMSConfig(pcb_model pcbmodel)
  {
    try {

      /*
                JAXBContext context = JAXBContext.newInstance("jPCBSim");
                Unmarshaller unmarshaller = context.createUnmarshaller();
                OpenEMS o = (OpenEMS)unmarshaller.unmarshal(new File(filepath));

                  List<Object> top = o.getExcitationOrFillColorOrEdgeColor();
                  for (Object l : top) {

                    //read FDTD objects
                    if(l instanceof OpenEMS.FDTD) {
                      //read FDTD initializer
                      OpenEMS.FDTD fdtd = (OpenEMS.FDTD) l;
                      System.out.println("ntimesteps: "+fdtd.getNumberOfTimesteps());
                      System.out.println("end_criteria: "+fdtd.getEndCriteria());
                      System.out.println("f_max: "+fdtd.getFMax());

                      //read excitation
                      List<Excitation> ex_top = fdtd.getExcitation();
                      for (Excitation ex : ex_top) {
                        System.out.println("type: "+ex.getType());
                        System.out.println("f0: "+ex.getF0());
                        System.out.println("fc: "+ex.getFc());
                      }


                      //read boundary conditions
                      List<OpenEMS.FDTD.BoundaryCond> bc_top = fdtd.getBoundaryCond();
                      for (OpenEMS.FDTD.BoundaryCond bc : bc_top) {
                        System.out.println("xmin: "+bc.getXmin());
                        System.out.println("xmax: "+bc.getXmax());
                        System.out.println("ymin: "+bc.getYmin());
                        System.out.println("ymax: "+bc.getYmax());
                        System.out.println("zmin: "+bc.getZmin());
                        System.out.println("zmax: "+bc.getZmax());
                      }
                    }

                    //read ContinuousStructure geometry objects
                    if(l instanceof OpenEMS.ContinuousStructure) {
                      System.out.println("cs coord system: "+((OpenEMS.ContinuousStructure) l).getCoordSystem());

                      //read mesh lines
                      List<OpenEMS.ContinuousStructure.RectilinearGrid> rg_top = ((OpenEMS.ContinuousStructure)l).getRectilinearGrid();
                      for (OpenEMS.ContinuousStructure.RectilinearGrid rg : rg_top) {
                        //mesh xlines
                        List<OpenEMS.ContinuousStructure.RectilinearGrid.XLines> xl_top = rg.getXLines();
                        for (OpenEMS.ContinuousStructure.RectilinearGrid.XLines xl : xl_top) {
                          System.out.println("xlines qty: "+xl.getQty());
                          System.out.println("xlines : "+xl.getValue());
                        }
                        //mesh ylines
                        List<OpenEMS.ContinuousStructure.RectilinearGrid.YLines> yl_top = rg.getYLines();
                        for (OpenEMS.ContinuousStructure.RectilinearGrid.YLines yl : yl_top) {
                          System.out.println("ylines qty: "+yl.getQty());
                          System.out.println("ylines : "+yl.getValue());
                        }
                        //mesh zlines
                        List<OpenEMS.ContinuousStructure.RectilinearGrid.ZLines> zl_top = rg.getZLines();
                        for (OpenEMS.ContinuousStructure.RectilinearGrid.ZLines zl : zl_top) {
                          System.out.println("zlines qty: "+zl.getQty());
                          System.out.println("zlines : "+zl.getValue());
                        }
                      }

                      //read properties
                      List<OpenEMS.ContinuousStructure.Properties> prop_top = ((OpenEMS.ContinuousStructure)l).getProperties();
                      for (OpenEMS.ContinuousStructure.Properties prop : prop_top) {
                        //get material
                        List<OpenEMS.ContinuousStructure.Properties.Material> mat_top = prop.getMaterial();
                        for (OpenEMS.ContinuousStructure.Properties.Material mat : mat_top) {
                          System.out.println("material name: "+mat.getName());
                        }
                        //get lorentz material
                        List<OpenEMS.ContinuousStructure.Properties.LorentzMaterial> lmat_top = prop.getLorentzMaterial();
                        for (OpenEMS.ContinuousStructure.Properties.LorentzMaterial mat : lmat_top) {
                          System.out.println("lorentz material name: "+mat.getName());
                          System.out.println("lorentz material id: "+mat.getID());
                        }
                        //get metal
                        List<OpenEMS.ContinuousStructure.Properties.Metal> metal_top = prop.getMetal();
                        for (OpenEMS.ContinuousStructure.Properties.Metal metal : metal_top) {
                          System.out.println("metal name: "+metal.getName());
                          System.out.println("metal id: "+metal.getID());
                        }
                        //get excitation
                        List<Excitation> pexc_top = prop.getExcitation();
                        for (Excitation exc : pexc_top) {
                          System.out.println("excitation name: "+exc.getName());
                        }
                        //get probebox
                        List<OpenEMS.ContinuousStructure.Properties.ProbeBox> pb_top = prop.getProbeBox();
                        for (OpenEMS.ContinuousStructure.Properties.ProbeBox pb : pb_top) {
                          System.out.println("probebox name: "+pb.getName());
                        }
                        //get lumptedelement
                        List<OpenEMS.ContinuousStructure.Properties.LumpedElement> le_top = prop.getLumpedElement();
                        for (OpenEMS.ContinuousStructure.Properties.LumpedElement le : le_top) {
                          System.out.println("lumpedelement name: "+le.getName());
                        }
                      }
                    }

                  }
      */
    } catch(Exception ee) {
      ee.printStackTrace();
    }
  }

}
