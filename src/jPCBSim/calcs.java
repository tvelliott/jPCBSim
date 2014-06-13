
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

public class calcs
{

  public static final double ER_FR4 = 4.2;
  public static final double ER_ROG4003C = 3.38;
  public static final double ER_ROG4350B = 3.48;
  public static final double ER_ALUMINA = 9.5;

  public double Zo;   //characteristic impedance of microstrip
  public double W;     //width of microstrip, same units as h
  public double h;     //thickness of the substrate, same units as W
  public double Er;   //dielectric constant of the substrate material
  public double Eeff; //effective dielectric constant for air/substrate combo
  public double Vp;   //propogation velocity
  public double Freq; //frequency of operation
  public double WLvac;    //wavelength in vacuum
  public double WLvp;   //wavelength of signal in effective dielectric

  public double angle;
  public double comp_val;
  public double Xz;
  public double length;
  public double Xlc;
  public String warnings;

  private microStrip parent;

  public calcs(microStrip parent)
  {
    this.parent = parent;
  }

  //input: (Equivalent component value)
  //output: length
  public void use_comp(double Er,
                       double h,
                       double Freq,
                       double Xz,
                       double comp_val
                      )
  {
    this.Er = Er;
    this.h = h;
    this.Freq = Freq;
    this.Xz = Xz;
    this.comp_val = comp_val;

    do_calcs_from_comp();
  }
  //input: angle
  //output: comp and length
  public void use_angle(double Er,
                        double h,
                        double Freq,
                        double Xz,
                        double angle
                       )
  {
    this.Er = Er;
    this.h = h;
    this.Freq = Freq;
    this.Xz = Xz;
    this.angle = angle;

    do_calcs_from_angle();
  }
  //input: length
  //output: comp and angle
  public void use_length(double Er,
                         double h,
                         double Freq,
                         double Xz,
                         double length
                        )
  {
    this.Er = Er;
    this.h = h;
    this.Freq = Freq;
    this.Xz = Xz;
    this.length = length;

    do_calcs_from_length();
  }

  public double get_Zohm_W(double z, double er, double height)
  {
    double w;
    w = 377.0/z;
    w = w / Math.pow( er + Math.pow(er,0.5), 0.5);
    w = w -1;
    w = w * height;

    return w;
  }

  public void do_calcs_from_comp()
  {

    warnings="";

    W = get_Zohm_W( Xz, Er, h );

    double Ersqr = Math.pow(Er, 0.5);
    Zo = 377.0 / ( ( W/h + 1) * Math.pow( Ersqr + Er, 0.5) );

    System.out.println(" Zo = "+Zo);

    Eeff = ((Er+1.0)/2.0) + ( (Er-1.0)/2.0 ) * ( 1.0 / Math.pow( 1.0 + (12.0*h/W), 0.5) );

    System.out.println(" Eeff = "+Eeff);

    Vp = 1.0 / Math.pow( Eeff, 0.5);

    System.out.println(" Vp = "+Vp);

    WLvac = 11800.0 / Freq;   //freq should be in GHz

    WLvp = WLvac * Vp;        //adjusted wavelength
    System.out.println(" WLvp = "+WLvp);

    if(Xz == 30.0) {
      comp_val *= 1.0e-12; //pF
      Xlc = 1.0 / ( 2.0 * 3.14 * (Freq*1e9) * comp_val);
      System.out.println(" Xlc = "+Xlc);
      length = ( ( 57.2957 * Math.atan( 30.0 / Xlc)) / 360.0) * WLvp;
      //length /= 0.95;  //take open-stub "end-effect into account, 5% more than calculated

      angle = (length/WLvp)*360.0;
      System.out.println(String.format(" cap_shunt len mils: W: %3.1f x L: %3.1f, %3.1f degrees",W, length, angle));
      if( length > WLvp*0.083 ) warnings = "Warning! cap_shunt len >30 deg: " + String.format("(%3.1f)",angle);
    } else if(Xz == 100.0) {
      comp_val *= 1.0e-9; //nH
      Xlc = ( 2.0 * 3.14 * (Freq*1e9) * comp_val);
      System.out.println(" Xlc = "+Xlc);
      length = ( ( 57.2957 * Math.atan( Xlc/100.0)) / 360.0) * WLvp;
      angle = (length/WLvp)*360.0;
      System.out.println(String.format(" induct_ser len mils: W: %3.1f x L: %3.1f, %3.1f degrees",W, length, angle));
      if( length > WLvp*0.083 ) warnings = "Warning! induct_ser_len >30 deg: " + String.format("(%3.1f)",angle);
    } else {
      length = WLvp/4.0;
      angle = 90.0;
      System.out.println("no valid capacitive/inductive value for Zo, assuming transmission line");
    }

    System.out.println(String.format("1/4 wave length: %3.2f",(WLvp/4.0)));
  }

  public void do_calcs_from_angle()
  {

    warnings="";

    W = get_Zohm_W( Xz, Er, h );

    double Ersqr = Math.pow(Er, 0.5);
    Zo = 377.0 / ( ( W/h + 1) * Math.pow( Ersqr + Er, 0.5) );

    System.out.println(" Zo = "+Zo);

    Eeff = ((Er+1.0)/2.0) + ( (Er-1.0)/2.0 ) * ( 1.0 / Math.pow( 1.0 + (12.0*h/W), 0.5) );

    System.out.println(" Eeff = "+Eeff);

    Vp = 1.0 / Math.pow( Eeff, 0.5);

    System.out.println(" Vp = "+Vp);

    WLvac = 11800.0 / Freq;   //freq should be in GHz

    WLvp = WLvac * Vp;        //adjusted wavelength
    System.out.println(" WLvp = "+WLvp);

    if(Xz == 30.0) {

      length = (angle/360.0) * WLvp;
      //length *= 0.95;  //take open-stub "end-effect into account, 5% less than calculated

      Xlc = Math.pow( 1.0 / Math.cos( ((length/WLvp) * 360.0) / 57.2957), 2.0);
      Xlc = Xlc - 1.0;
      Xlc = Math.pow( Xlc, 0.5) / 30.0;
      Xlc = 1.0 / Xlc;

      System.out.println(" Xlc = "+Xlc);

      comp_val = (1.0 / Xlc) / ( 2.0 * 3.14 * (Freq*1e9) );
      comp_val /= 1.0e-12; //convert to pF

      System.out.println(" comp_val = "+comp_val);
      System.out.println(String.format(" cap_shunt len mils: W: %3.1f x L: %3.1f, %3.1f degrees",W, length, angle));
      if( length > WLvp*0.083 ) warnings = "Warning! cap_shunt len >30 deg: " + String.format("(%3.1f)",angle);
    } else if(Xz == 100.0) {

      length = (angle/360.0) * WLvp;

      Xlc = Math.pow( 1.0 / Math.cos( ((length/WLvp) * 360.0) / 57.2957), 2.0);
      Xlc = Xlc - 1.0;
      Xlc = Math.pow( Xlc, 0.5) * 100.0;

      System.out.println(" Xlc = "+Xlc);

      comp_val = Xlc / ( 2.0 * 3.14 * (Freq*1e9));
      comp_val /= 1.0e-9; //convert to nH

      System.out.println(" comp_val = "+comp_val);
      System.out.println(String.format(" induct_ser len mils: W: %3.1f x L: %3.1f, %3.1f degrees",W, length, angle));
      if( length > WLvp*0.083 ) warnings = "Warning! induct_ser_len >30 deg: " + String.format("(%3.1f)",angle);


    } else {
      length = WLvp/4.0;
      angle = 90.0;
      System.out.println("no valid capacitive/inductive value for Zo, assuming transmission line");
    }

    System.out.println(String.format("1/4 wave length: %3.2f",(WLvp/4.0)));
  }

  public void do_calcs_from_length()
  {

    warnings="";

    W = get_Zohm_W( Xz, Er, h );

    double Ersqr = Math.pow(Er, 0.5);
    Zo = 377.0 / ( ( W/h + 1) * Math.pow( Ersqr + Er, 0.5) );

    System.out.println(" Zo = "+Zo);

    Eeff = ((Er+1.0)/2.0) + ( (Er-1.0)/2.0 ) * ( 1.0 / Math.pow( 1.0 + (12.0*h/W), 0.5) );

    System.out.println(" Eeff = "+Eeff);

    Vp = 1.0 / Math.pow( Eeff, 0.5);

    System.out.println(" Vp = "+Vp);

    WLvac = 11800.0 / Freq;   //freq should be in GHz

    WLvp = WLvac * Vp;        //adjusted wavelength
    System.out.println(" WLvp = "+WLvp);

    if(Xz == 30.0) {

      //length /= 0.95;  //take open-stub "end-effect into account, 5% more than calculated
      angle = (length/WLvp)*360.0;

      Xlc = Math.pow( 1.0 / Math.cos( ((length/WLvp) * 360.0) / 57.2957), 2.0);
      Xlc = Xlc - 1.0;
      Xlc = Math.pow( Xlc, 0.5) / 30.0;
      Xlc = 1.0 / Xlc;

      System.out.println(" Xlc = "+Xlc);

      comp_val = (1.0 / Xlc) / ( 2.0 * 3.14 * (Freq*1e9) );
      comp_val /= 1.0e-12; //convert to pF

      System.out.println(" comp_val = "+comp_val);
      System.out.println(String.format(" cap_shunt len mils: W: %3.1f x L: %3.1f, %3.1f degrees",W, length, angle));
      if( length > WLvp*0.083 ) warnings = "Warning! cap_shunt len >30 deg: " + String.format("(%3.1f)",angle);
    } else if(Xz == 100.0) {

      angle = (length/WLvp)*360.0;

      Xlc = Math.pow( 1.0 / Math.cos( ((length/WLvp) * 360.0) / 57.2957), 2.0);
      Xlc = Xlc - 1.0;
      Xlc = Math.pow( Xlc, 0.5) * 100.0;

      System.out.println(" Xlc = "+Xlc);

      comp_val = Xlc / ( 2.0 * 3.14 * (Freq*1e9));
      comp_val /= 1.0e-9; //convert to nH

      System.out.println(" comp_val = "+comp_val);
      System.out.println(String.format(" induct_ser len mils: W: %3.1f x L: %3.1f, %3.1f degrees",W, length, angle));
      if( length > WLvp*0.083 ) warnings = "Warning! induct_ser_len >30 deg: " + String.format("(%3.1f)",angle);
    } else {
      length = WLvp/4.0;
      angle = 90.0;
      System.out.println("no valid capacitive/inductive value for Zo, assuming transmission line");
    }

    System.out.println(String.format("1/4 wave length: %3.2f",(WLvp/4.0)));
  }

}
