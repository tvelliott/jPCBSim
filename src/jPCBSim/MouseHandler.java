package jPCBSim;


import java.awt.Point;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

/**
 * This class handles the mouse events. It extends the MouseInputAdapter Class.
 * The mouse invents are then used to zoom in/out, translate and rotate a 3D
 * scene
 * <p>
 */
public class MouseHandler extends MouseInputAdapter
{

  /** The Renderer object on which key events are to act. */
  private Renderer renderer;

  /** The PCBPanel (JPanel) object on which key events are to act. */
  private PCBPanel thePanel;

  /** Flag to track if a left mouse button is being held down. */
  private boolean leftButtonDown = false;

  /** Flag to track if a middle mouse button is being held down. */
  private boolean middleButtonDown = false;

  /** Flag to track if a right mouse button is being held down. */
  private boolean rightButtonDown = false;

  /**
   * Default Constructor that accepts the render for call back purposes as
   * well as the originating JPanel object
   *
   * @param renderer A Renderer object used for callbacks
   * @param thePanel A Prox3D (JPanel) object used for callbacks
   */
  public MouseHandler( Renderer renderer, PCBPanel thePanel )
  {
    this.renderer = renderer;
    this.thePanel = thePanel;
  }

  /**
   * Checks if a mouse is clicked.
   *
   * @param e A mouse clicked event.
   */
  public void mouseClicked( MouseEvent e )
  {
    thePanel.requestFocusInWindow();

    // Update the scene - redraw the display following a mouse event, when
    // not using an animator, else the animator automatically performs updates.
    if (!thePanel.useAnimator) {
      thePanel.drawabledisplay();
    }
  }

  /**
   * Checks if a mouse is pressed.
   *
   * @param e A mouse pressed event.
   */
  public void mousePressed( MouseEvent e )
  {
    thePanel.requestFocusInWindow();

    if (SwingUtilities.isLeftMouseButton(e)) {
      leftButtonDown = true;
    } else if (SwingUtilities.isMiddleMouseButton(e)) {
      middleButtonDown = true;
    } else if (SwingUtilities.isRightMouseButton(e)) {
      rightButtonDown = true;
    }

    // Update the scene - redraw the display following a mouse event, when
    // not using an animator, else the animator automatically performs updates.
    if (!thePanel.useAnimator) {
      thePanel.drawabledisplay();
    }
  }

  /**
   * Checks if the mouse was released.
   *
   * @param e A mouse released event.
   */
  public void mouseReleased( MouseEvent e )
  {
    if (SwingUtilities.isLeftMouseButton(e)) {
      leftButtonDown = false;
    } else if (SwingUtilities.isMiddleMouseButton(e)) {
      middleButtonDown = false;
    } else if (SwingUtilities.isRightMouseButton(e)) {
      rightButtonDown = false;
    }

    // Update the scene - redraw the display following a mouse event, when
    // not using an animator, else the animator automatically performs updates.
    if (!thePanel.useAnimator) {
      thePanel.drawabledisplay();
    }
  }

  /**
   * Checks if the mouse was moved.  This mouse point is continually updated
   * so that there are no jumps in the camera control when the mouse is
   * finally dragged.
   *
   * @param e A mouse moved event.
   */
  public void mouseMoved(MouseEvent e)
  {
    // Get the current mouse point
    Point mousePoint = e.getPoint();
    renderer.currentMousePoint.x = mousePoint.x;
    renderer.currentMousePoint.y = mousePoint.y;

    // swap the mouse points to use for the next comparison
    renderer.lastMousePoint.x = renderer.currentMousePoint.x;
    renderer.lastMousePoint.y = renderer.currentMousePoint.y;

    // Update the scene - redraw the display following a mouse event, when
    // not using an animator, else the animator automatically performs updates.
    if (!thePanel.useAnimator) {
      thePanel.drawabledisplay();
    }
  }

  /**
   * Checks if a mouse is dragged.
   *
   * @param e A mouse dragged event.
   */
  public void mouseDragged(MouseEvent e)
  {
    thePanel.requestFocusInWindow();

    if (SwingUtilities.isLeftMouseButton(e)
        || SwingUtilities.isMiddleMouseButton(e)
        || SwingUtilities.isRightMouseButton(e)) {

      // Get the current mouse point
      Point mousePoint = e.getPoint();
      renderer.currentMousePoint.x = mousePoint.x;
      renderer.currentMousePoint.y = mousePoint.y;

      // Calculate the difference in mouse movement
      int xDiff = renderer.currentMousePoint.x - renderer.lastMousePoint.x;
      int yDiff = renderer.currentMousePoint.y - renderer.lastMousePoint.y;

      if (leftButtonDown) {
        // If the mouse moved a little along the y direction (up/down) then
        // cause the view to rotate about the 'right' vector of the view - ie
        // pitch the view up/down.
        if (yDiff!=0 && Math.abs(yDiff) > Math.abs(xDiff)) {
          renderer.camera.rotateAroundRightVector(-yDiff / 3.0);
        }

        // If the moouse moved a little along the x direction (left/right) then
        // cause the view to rotate about the 'up' vector of the view - ie
        // yaw the view left/right
        else if (xDiff != 0 && Math.abs(xDiff)> Math.abs(yDiff)) {
          renderer.camera.rotateAroundUpVector(-xDiff / 3.0);
        }
      }


      if (middleButtonDown) {
        if( !renderer.isOrtho()) {
          if (-yDiff != 0) {
            renderer.camera.moveFwd(-yDiff*renderer.camera.getMoveSpeed());
          }
        } else {
          if (-yDiff != 0) {
            renderer.zoomOrtho(yDiff*renderer.camera.getMoveSpeed());
          }
        }
      }

      // This only works for the modified FPS mode to get proper motion up/down.
      if(rightButtonDown) {
        if (xDiff != 0) {
          renderer.camera.moveLeft(xDiff*renderer.camera.getMoveSpeed()*0.1);
        }

        if (yDiff != 0) {
          renderer.camera.moveUp(yDiff*renderer.camera.getMoveSpeed()*0.1);
        }
      }

      // swap the mouse points to use for the next comparison
      renderer.lastMousePoint.x = renderer.currentMousePoint.x;
      renderer.lastMousePoint.y = renderer.currentMousePoint.y;
    }

    // Update the scene - redraw the display following a mouse event, when
    // not using an animator, else the animator automatically performs updates.
    if (!thePanel.useAnimator) {
      thePanel.drawabledisplay();
    }
  }
}
