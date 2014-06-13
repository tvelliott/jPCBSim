
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
import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import javax.swing.*;

public class Console extends JPanel implements ActionListener, Runnable
{
  private JTextArea textArea;
  private Thread reader;
  private Thread reader2;

  private final PipedInputStream pin=new PipedInputStream();
  private final PipedInputStream pin2=new PipedInputStream();


  public Console()
  {
    textArea=new JTextArea();
    textArea.setEditable(false);
    textArea.setBackground( Color.black );
    textArea.setForeground( Color.white );
    JButton button=new JButton("Clear Console");

    setLayout(new BorderLayout());
    add(new JScrollPane(textArea),BorderLayout.CENTER);
    JPanel button_panel = new JPanel();
    button_panel.setLayout(new FlowLayout());
    button_panel.add(button);
    add(button_panel,BorderLayout.SOUTH);

    button.addActionListener(this);

    try {
      PipedOutputStream pout=new PipedOutputStream(this.pin);
      System.setOut(new PrintStream(pout,true));
    } catch (java.io.IOException io) {
      textArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
    } catch (SecurityException se) {
      textArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
    }

    try {
      PipedOutputStream pout2=new PipedOutputStream(this.pin2);
      System.setErr(new PrintStream(pout2,true));
    } catch (java.io.IOException io) {
      textArea.append("Couldn't redirect STDERR to this console\n"+io.getMessage());
    } catch (SecurityException se) {
      textArea.append("Couldn't redirect STDERR to this console\n"+se.getMessage());
    }


    reader=new Thread(this);
    reader.setDaemon(true);
    reader.start();

    reader2=new Thread(this);
    reader2.setDaemon(true);
    reader2.start();

  }

  public synchronized void actionPerformed(ActionEvent evt)
  {
    textArea.setText("");
  }

  public synchronized void run()
  {
    try {
      while (Thread.currentThread()==reader) {
        try {
          this.wait(100);
        } catch(InterruptedException ie) {}
        if (pin.available()!=0) {
          String input=this.readLine(pin);
          textArea.append(input);

          textArea.setCaretPosition( textArea.getDocument().getLength() );

        }
      }

      while (Thread.currentThread()==reader2) {
        try {
          this.wait(100);
        } catch(InterruptedException ie) {}
        if (pin2.available()!=0) {
          String input=this.readLine(pin2);
          textArea.append(input);

          textArea.setCaretPosition( textArea.getDocument().getLength() );
        }
      }
    } catch (Exception e) {
    }


  }

  public synchronized String readLine(PipedInputStream in) throws IOException
  {
    String input="";
    do {
      int available=in.available();
      if (available==0) break;
      byte b[]=new byte[available];
      in.read(b);
      input=input+new String(b,0,b.length);
    } while( !input.endsWith("\n") &&  !input.endsWith("\r\n") );

    return input;
  }

}
