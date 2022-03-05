import java.awt.event.*;
import java.awt.*;

/**
 * Records user mouse inputs.
 */
public class MouseInput implements MouseListener, MouseMotionListener {

    private boolean mouseLeftDown = false;
    private boolean mouseRightDown = false;
    private boolean mouseOnScreen = true;
    private boolean clickOccured = false;
    private Point clickPosition = new Point();
    private Point mousePosition = new Point();

    @Override
    public void mouseClicked(MouseEvent e) {
        // Unused.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftDown = true;
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            mouseRightDown = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftDown = false;
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            mouseRightDown = false;
        }
        clickPosition = e.getPoint();
        clickOccured = true;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseOnScreen = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseOnScreen = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePosition = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
    }

    /**
     * Returns whether left click is pressed.
     * 
     * @return the state of the left mouse button.
     */
    public boolean getLeftClick() {
        return mouseLeftDown;
    }

    /**
     * Returns whether right click is pressed.
     * 
     * @return the state of the right mouse button.
     */
    public boolean getRightClick() {
        return mouseRightDown;
    }

    /**
     * Returns whether the mouse is above the panel.
     * 
     * @return whether the mouse is above the panel.
     */
    public boolean isMouseVisible() {
        return mouseOnScreen;
    }

    /**
     * Returns whether a new click has occured.
     * 
     * @param clearMem whether to clear click memory after checking.
     * @return whether a new click has occured.
     */
    public boolean hasClickOccured(boolean clearMem) {
        boolean locClickOccured = clickOccured;
        if (clearMem) {
            clickOccured = false;
        }
        return locClickOccured; 
    }

    /**
     * Returns the last position of the mouse after clicking.
     * 
     * @return the point where the mouse last clicked.
     */
    public Point getClickPosition() {
        return clickPosition;
    }

    /**
     * Gets the last known coordinates of the mouse.
     * 
     * @return the current mouse coordinates.
     */
    public Point getMousePosition() {
        return mousePosition;
    }
}
