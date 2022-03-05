// Jakob Stickles
// 12/10/2021
// RacetrackGame.java

/**
 * Runs the racetrack game, creating navigation menus and loading tracks when selected.
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class RacetrackGame {

    public static final int PANEL_WIDTH = 1280;
    public static final int PANEL_HEIGHT = 720;
    public static final double FPS = 60.0;
    public static Leaderboard leaderboard;

    private static final DrawingPanel panel = new DrawingPanel(PANEL_WIDTH, PANEL_HEIGHT);
    private static final BufferedImage screenBuf = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT,
    BufferedImage.TYPE_INT_ARGB);
    private static final KeyInput keyInput = new KeyInput();
    private static final MouseInput mouseInput = new MouseInput();

    private static Track currentTrack;
    private static Menu currentMenu;

    private static final double MILLIS_PER_FRAME = 1000 / FPS;
    private static long frameCount = 0;

    private static Vehicle raceCar;
    private static RaceUI raceUI;
    private static Camera raceCam;

    private static boolean isRacing = false;
    private static boolean restartLock = false;
    private static boolean readyUp;

    private static boolean done = false;
    public static void main(String[] args) {

        // General program setup
        initFrame();
        leaderboard = new Leaderboard();
        currentMenu = new MainMenu(keyInput, mouseInput);
        long appStart = System.currentTimeMillis();

        // Main program loop
        while (!done) {

            if (currentMenu.hasNextMenu()) {
                // Change the menu whenever necessary.
                currentMenu = currentMenu.getNextMenu();
            } else if (currentMenu.hasTrack()) {
                // Start a new track when selected.
                initTrack(currentMenu.getNewTrack());
                isRacing = true;
            } else if (isRacing) {
                // Main race loop.

                // Go back to previous menu if Escape is pressed.
                if (keyInput.getEscape()) {
                    isRacing = false;
                    currentTrack = null;
                    continue;
                }

                // Restarts track if R is pressed.
                if (keyInput.getR()) {
                    if (!restartLock) {
                        restartLock = true;
                        initTrack(currentTrack);
                    }
                } else {
                    restartLock = false;
                }

                raceCam.updatePosition();
                
                if (!readyUp) {
                    if (keyInput.getSpace()) {
                        readyUp = true;
                        raceUI.readyUp();
                    }
                } else if (raceUI.countdownComplete()) {
                    raceCar.updateVehicle(keyInput.getUp(), keyInput.getDown(), 
                    keyInput.getSpace(), 
                    keyInput.getLeft(), keyInput.getRight());
                    raceCar.manageCollisions();
                }

                raceCam.draw(screenBuf);
                raceUI.drawUI(screenBuf);

            } else {
                // Menu loop.
                currentMenu.drawMenu(screenBuf);
            }

            // Draw elements to screen buffer to prevent flickering
            panel.getGraphics().drawImage(screenBuf, 0, 0, null);

            // Sleep long enough to maintain the target FPS.
            long targetTime = appStart + (long)(++frameCount * MILLIS_PER_FRAME);
            panel.sleep((int)(targetTime - System.currentTimeMillis()));
        }
    }

    /**
     * Prepares the given track to play.
     * 
     * @param track the track to play.
     */
    private static void initTrack(Track track) {
        currentTrack = track;
        raceCar = new Vehicle(600, 300, Math.toRadians(270), Color.BLUE);
        raceCar.setTrack(currentTrack);
        int[] carPos = track.getCarStartPosition();
        raceCar.setPosition(carPos[0], carPos[1]);
        raceCar.setRotation(Math.toRadians(carPos[2]));
        raceCam = new Camera(currentTrack, raceCar);
        raceUI = new RaceUI(currentTrack, raceCar);
        readyUp = false;
    }

    /**
     * Initializes the JFrame, JPanel and KeyInput objects.
     */
    private static void initFrame() {
        JPanel jPanel = panel.getJPanel();
        JFrame jFrame = panel.getJFrame();

        // Add WindowListener that closes program when window closes.
        jFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Unused
            }

            @Override
            public void windowClosing(WindowEvent e) {
                done = true;
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // Unused
            }

            @Override
            public void windowIconified(WindowEvent e) {
                // Unused
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                // Unused
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // Unused
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                // Unused
            }
        });

        jPanel.setFocusable(true);
        jPanel.requestFocusInWindow();
        jPanel.addKeyListener(keyInput);
        jPanel.addMouseListener(mouseInput);
        jPanel.addMouseMotionListener(mouseInput);
    }

}