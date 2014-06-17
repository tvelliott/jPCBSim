//Not clear on who the original author(s) of this code is/are
//I may have modified is slightly, but am not the original author
//I believe it to be public domain.  Please contact me if I should
//change the attributions or remove from the repository.
//Thanks, tvelliott

package jPCBSim;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * This class handles the key events. It extends the KeyAdapter Class.
 * <p>
 *
 */
public class KeyHandler extends KeyAdapter
{

  /** The Renderer object on which key events are to act */
  private Renderer renderer;

  /** The PCBPanel (JPanel) object on which key events are to act */
  private PCBPanel thePanel;

  /** Camera forward movement flag. */
  private boolean up = false;

  /** Camera backward movement flag. */
  private boolean down = false;

  /** Camera left movement flag. */
  private boolean left = false;

  /** Camera right movement flag. */
  private boolean right = false;

  /** Camera up movement flag. */
  private boolean pageup = false;

  /** Camera down movement flag. */
  private boolean pagedown = false;

  /** Camera roll left movement flag. */
  private boolean rollleft = false;

  /** Camera roll right movement flag. */
  private boolean rollright = false;

  /** Tracks if the CTRL key is held down. */
  private boolean ctrldown = false;

  /**
   * Default Constructor that accepts the render for call back purposes as
   * well as the originating JPanel object
   *
   * @param renderer A Renderer object used for callbacks
   * @param thePanel A Prox3D (JPanel) object used for callbacks
   */
  public KeyHandler( Renderer renderer, PCBPanel thePanel )
  {
    this.thePanel = thePanel;
    this.renderer = renderer;
  }

  /**
   * Checks if a key is typed.  This method currently does not respond to
   * this event
   *
   * @param e A key typed event
   */
  public void keyTyped( KeyEvent e )
  {
    // Nothing
  }

  public boolean isControlDown()
  {
    return ctrldown;
  }

  /**
   * Tracks which keys have been pressed.  Specifically the control keys
   * are checked for pressed events and their corresponding boolean flags
   * are set if the keys are pressed.  These boolean flags determine which
   * action should be performed by the camera during each rendering/display
   * cycle.
   *
   * @param e The key event instance.
   */
  public void keyPressed( KeyEvent e )
  {
    int keyCode = e.getKeyCode();
    switch (keyCode) {
    case KeyEvent.VK_CONTROL:
      ctrldown = true;
      break;

    case KeyEvent.VK_W:
    case KeyEvent.VK_UP:
    case KeyEvent.VK_DOWN:
    case KeyEvent.VK_LEFT:
    case KeyEvent.VK_D:
    case KeyEvent.VK_RIGHT:
    case KeyEvent.VK_PAGE_UP:
    case KeyEvent.VK_PAGE_DOWN:
    case KeyEvent.VK_COMMA:
    case KeyEvent.VK_PERIOD:

      /*
              if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                up = true;
              }

              if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
                down = true;
              }

              if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                left = true;
              }

              if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                right = true;
              }

              if (keyCode == KeyEvent.VK_PAGE_UP) {
                pageup = true;
              }

              if (keyCode == KeyEvent.VK_PAGE_DOWN) {
                pagedown = true;
              }

              if (keyCode == KeyEvent.VK_COMMA) {
                rollleft = true;
              }

              if (keyCode == KeyEvent.VK_PERIOD) {
                rollright = true;
              }
      */

      e.consume();
      cameraUpdate();
      break;

    default:
      processKeyEvent(e);
    }
  }

  /**
   * Processes the keyevents when a key is released.  For the FPS control
   * keys, the control flags are cleared signifying that that action is
   * no longer to be performed during the rendering/display loop.
   *
   * @param e The key event instance.
   */
  public void keyReleased( KeyEvent e )
  {
    int keyCode = e.getKeyCode();
    switch (keyCode) {
    case KeyEvent.VK_CONTROL:
      ctrldown = false;
      break;

      // Process the FPS control actions for camera movement
    case KeyEvent.VK_W:
    case KeyEvent.VK_UP:
    case KeyEvent.VK_DOWN:
    case KeyEvent.VK_LEFT:
    case KeyEvent.VK_D:
    case KeyEvent.VK_RIGHT:
    case KeyEvent.VK_PAGE_UP:
    case KeyEvent.VK_PAGE_DOWN:
    case KeyEvent.VK_COMMA:
    case KeyEvent.VK_PERIOD:

      /*
              if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                up = false;
              } else if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
                down = false;
              } else if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                left = false;
              } else if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                right = false;
              } else if (keyCode == KeyEvent.VK_PAGE_UP) {
                pageup = false;
              } else if (keyCode == KeyEvent.VK_PAGE_DOWN) {
                pagedown = false;
              } else if (keyCode == KeyEvent.VK_COMMA) {
                rollleft = false;
              } else if (keyCode == KeyEvent.VK_PERIOD) {
                rollright = false;
              }
      */

      e.consume();

      cameraUpdate();
      break;

    default:
    }
  }

  /**
   * Processes key events outside of the control key events already processed
   * in the keyPressed method.
   *
   * @param e The key event instance.
   */
  private void processKeyEvent( KeyEvent e )
  {

    switch (e.getKeyCode()) {

    case KeyEvent.VK_V:
      thePanel.toggle_display_viewmatrix();
      break;

      /*
            case KeyEvent.VK_U:
              Vector3d pos1 = renderer.camera.getOrbitPointCopy();
              pos1.x -= 0.25;
              renderer.camera.setOrbitPoint(pos1);
              break;
            case KeyEvent.VK_I:
              Vector3d pos2 = renderer.camera.getOrbitPointCopy();
              pos2.x += 0.25;
              renderer.camera.setOrbitPoint(pos2);
              break;

            case KeyEvent.VK_J:
              Vector3d pos3 = renderer.camera.getOrbitPointCopy();
              pos3.y -= 0.25;
              renderer.camera.setOrbitPoint(pos3);
              break;
            case KeyEvent.VK_K:
              Vector3d pos4 = renderer.camera.getOrbitPointCopy();
              pos4.y += 0.25;
              renderer.camera.setOrbitPoint(pos4);
              break;

            case KeyEvent.VK_N:
              Vector3d pos5 = renderer.camera.getOrbitPointCopy();
              pos5.z -= 0.25;
              renderer.camera.setOrbitPoint(pos5);
              break;
            case KeyEvent.VK_M:
              Vector3d pos6 = renderer.camera.getOrbitPointCopy();
              pos6.z += 0.25;
              renderer.camera.setOrbitPoint(pos6);
              break;
      */
    case KeyEvent.VK_M:
      renderer.toggleMesh();
      break;
    case KeyEvent.VK_P:
      renderer.togglePML();
      break;
    case KeyEvent.VK_A:
      renderer.toggleAirBox();
      break;

    case KeyEvent.VK_R:
      renderer.readEMSConfig();
      break;

    case KeyEvent.VK_S:
      renderer.runSimulation();
      break;

    case KeyEvent.VK_O:
      renderer.toggleOrtho();
      break;

    case KeyEvent.VK_1:
      renderer.camera.setEye(new Vector3d(0, 2, 0));
      renderer.camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
      renderer.camera.rotateAroundLookVector(90.0);
      break;

    case KeyEvent.VK_2:
      renderer.camera.setEye(new Vector3d(0, 0, -2));
      renderer.camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0));
      renderer.camera.rotateAroundLookVector(90.0);
      break;

    case KeyEvent.VK_3:
      renderer.camera.setEye(new Vector3d(2, 0, 0));
      renderer.camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(0, 0, 1));
      renderer.camera.rotateAroundLookVector(90.0);
      break;

      //profile view
    case KeyEvent.VK_4:
      renderer.camera.setEye(new Vector3d(0, 0, -2));
      renderer.camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
      renderer.camera.rotateAroundLookVector(90.0);
      renderer.camera.updateView();
      renderer.camera.setEye(new Vector3d(0, 0, -2));
      renderer.camera.lookAt(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
      renderer.camera.rotateAroundLookVector(90.0);
      renderer.camera.updateView();
      renderer.camera.rotateAroundRightVector(-45);
      renderer.camera.updateView();
      break;

    default:
    }

    // Update the scene - redraw the display following a keyboard event, when
    // not using an animator, else the animator automatically performs updates.
    if (!thePanel.useAnimator) {
      thePanel.drawabledisplay();
    }
  }

  /**
   * Updates the camera based on which control flags have been set.
   *
   */
  private void cameraUpdate()
  {
    if (up) {
      if (ctrldown) {
        renderer.camera.rotateAroundRightVector(-1.0);
      } else {
        renderer.camera.stepFwd();
      }
    }

    if (down) {
      if (ctrldown) {
        renderer.camera.rotateAroundRightVector(1.0);
      } else {
        renderer.camera.stepBack();
      }
    }

    if (left) {
      if (ctrldown) {
        renderer.camera.rotateAroundUpVector(1.0);
      } else {
        renderer.camera.stepLeft();
      }
    }

    if (right) {
      if (ctrldown) {
        renderer.camera.rotateAroundUpVector(-1.0);
      } else {
        renderer.camera.stepRight();
      }
    }

    if (pageup) {
      renderer.camera.stepUp();
    }

    if (pagedown) {
      renderer.camera.stepDown();
    }

    if (rollleft) {
      renderer.camera.rotateAroundLookVector(-1.0);
    }

    if (rollright) {
      renderer.camera.rotateAroundLookVector(1.0);
    }

    // Update the scene - redraw the display following a keyboard event, when
    // not using an animator, else the animator automatically performs updates.
    if (!thePanel.useAnimator) {
      thePanel.drawabledisplay();
    }
  }

}
