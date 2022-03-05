import java.awt.event.*;

/**
 * Records user key inputs for later use.
 */
public class KeyInput implements KeyListener {

    private boolean upState = false;
    private boolean downState = false;
    private boolean leftState = false;
    private boolean rightState = false;
    private boolean enterState = false;
    private boolean shiftState = false;
    private boolean spaceState = false;
    private boolean escapeState = false;
    private boolean rState = false;

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP:
                upState = true;
                break;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN:
                downState = true;
                break;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT:
                leftState = true;
                break;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT:
                rightState = true;
                break;
            case KeyEvent.VK_ENTER:
                enterState = true;
                break;
            case KeyEvent.VK_SHIFT:
                shiftState = true;
                break;
            case KeyEvent.VK_SPACE:
                spaceState = true;
                break;
            case KeyEvent.VK_ESCAPE:
                escapeState = true;
                break;
            case KeyEvent.VK_R:
                rState = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP:
                upState = false;
                break;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN:
                downState = false;
                break;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT:
                leftState = false;
                break;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT:
                rightState = false;
                break;
            case KeyEvent.VK_ENTER:
                enterState = false;
                break;
            case KeyEvent.VK_SHIFT:
                shiftState = false;
                break;
            case KeyEvent.VK_SPACE:
                spaceState = false;
                break;
            case KeyEvent.VK_ESCAPE:
                escapeState = false;
                break;
            case KeyEvent.VK_R:
                rState = false;
                break;
            default:
                break;
        }
    }

    /**
     * Returns whether the up key is pressed.
     * 
     * @return the state of the up key.
     */
    public boolean getUp() {
        return upState;
    }

    /**
     * Returns whether the down key is pressed.
     * 
     * @return the state of the down key.
     */
    public boolean getDown() {
        return downState;
    }

    /**
     * Returns whether the left key is pressed.
     * 
     * @return the state of the left key.
     */
    public boolean getLeft() {
        return leftState;
    }

    /**
     * Returns whether the right key is pressed.
     * 
     * @return the state of the right key.
     */
    public boolean getRight() {
        return rightState;
    }

    /**
     * Returns whether the enter key is pressed.
     * 
     * @return the state of the enter key.
     */
    public boolean getEnter() {
        return enterState;
    }

    /**
     * Returns whether the shift key is pressed.
     * 
     * @return the state of the shift key.
     */
    public boolean getShift() {
        return shiftState;
    }

    /**
     * Returns whether the space key is pressed.
     * 
     * @return the state of the space key.
     */
    public boolean getSpace() {
        return spaceState;
    }

    /**
     * Returns whether the escape key is pressed.
     * 
     * @return the state of the escape key.
     */
    public boolean getEscape() {
        return escapeState;
    }
    
    /**
     * Returns whether the R key is pressed.
     * 
     * @return the state of the R key.
     */
    public boolean getR() {
        return rState;
    }

}
