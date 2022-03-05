import java.awt.*;
import java.awt.image.*;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JOptionPane;

public class RaceUI {

    private Track track;
    private Vehicle vehicle;

    private boolean ready;
    private long countdownStart;

    private long timerStart;
    private int checkpointReached;
    private int lapsDone;
    private long finalTime;

    private static final int SEC_COUNTDOWN = 3;
    private static final int SEC_GO_MSG = 1;

    private static final String FONT_NAME = "Calibri";
    private static final int UI_FONTSIZE = 32;
    private static final int SCREEN_PADDING = 16;
    private static final int TIMER_YPOS = UI_FONTSIZE;
    private static final int LAPS_YPOS = UI_FONTSIZE * 2;

    private static final int LEADERBOARD_WIDTH = 480;
    private static final int LEADERBOARD_HEIGHT = 480;
    private static final int LEADERBOARD_PADDING = 40;
    private static final Color LEADERBOARD_COLOR = new Color(32, 32, 32, 200);

    private static final String CONTROLS_MSG_1 = "Brake and accelerate at the same time to drift!";
    private static final String CONTROLS_MSG_2 = 
    "Up/Down - Accelerate | Left/Right - Turn | Space - Brake | R - Restart | Esc - Exit";

    private List<LeaderboardEntry> finalLeaderboard = null;

    /**
     * Creates a new UI object that tracks track and vehicle stats.
     * 
     * @param track the track to monitor.
     * @param vehicle the vehicle to monitor.
     */
    public RaceUI(Track track, Vehicle vehicle) {
        this.track = track;
        this.vehicle = vehicle;
        countdownStart = 0;
        timerStart = 0;
        checkpointReached = 0;
        lapsDone = 0;
        finalTime = 0;
    }

    /**
     * Draws the overall UI to the screen.
     * 
     * @param buf the Graphics2D object of the screen buffer.
     */
    public void drawUI(BufferedImage buf) {
        Graphics2D graphics = buf.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
        if (!ready) {
            // Set smaller font.
            graphics.setFont(new Font(FONT_NAME, Font.BOLD, UI_FONTSIZE * 5/4));
            graphics.setColor(Color.WHITE);
            drawCenteredMessage(graphics, -1, "Press space to start the race!");

            graphics.setFont(new Font(FONT_NAME, Font.ITALIC, UI_FONTSIZE * 4/5));
            drawCenteredMessage(graphics, 
            RacetrackGame.PANEL_HEIGHT - (int)Menu.getTextBounds(graphics, CONTROLS_MSG_2)[1] -
            SCREEN_PADDING * 2, CONTROLS_MSG_1);

            graphics.setFont(new Font(FONT_NAME, Font.PLAIN, UI_FONTSIZE));
            drawCenteredMessage(graphics, 
            RacetrackGame.PANEL_HEIGHT - SCREEN_PADDING, CONTROLS_MSG_2);
        } else if (drawCountdown(graphics)) {
            // RACE UI
            drawLapTimer(graphics);
            drawVehicleStats(graphics);
        }
        graphics.dispose();
    }

    /**
     * Indicate that the racer is ready to start.
     */
    public void readyUp() {
        ready = true;
    }

    /**
     * Gets whether the countdown has completed and the user has control of their vehicle.
     * 
     * @return whether the countdown has completed.
     */
    public boolean countdownComplete() {
        long countdownEnd = countdownStart + SEC_COUNTDOWN * 1000;
        return countdownEnd <= System.currentTimeMillis();
    }

    /**
     * Draws a message in the center of the screen.
     * 
     * @param buf the Graphics2D object of the screen buffer.
     * @param msg the text to draw centered.
     */
    public void drawCenteredMessage(Graphics2D buf, int y, String msg) {
        double[] bounds = Menu.getTextBounds(buf, msg);
        int centeredX = (int)(RacetrackGame.PANEL_WIDTH - bounds[0]) / 2;
        int centeredY = y < 0 ? (int)(RacetrackGame.PANEL_HEIGHT - bounds[1]) / 2 : y;
        buf.drawString(msg, centeredX, centeredY);
    }

    /**
     * Draws the countdown if applicable and returns whether the user has control.
     * 
     * @param buf the Graphics2D object of the screen buffer.
     * @return whether the countdown is complete, and the user has control.
     */
    public boolean drawCountdown(Graphics2D buf) {
        if (countdownStart == 0) {
            countdownStart = System.currentTimeMillis();
        }
        // COUNDOWN UI
        long countdownEnd = countdownStart + SEC_COUNTDOWN * 1000;
        if (countdownEnd > System.currentTimeMillis()) {
            // Set larger font.
            buf.setFont(new Font(FONT_NAME, Font.BOLD, UI_FONTSIZE * 2));
            buf.setColor(Color.WHITE);
            int secondsLeft = (int)Math.ceil(
            (countdownEnd - System.currentTimeMillis()) / 1000.0);
            drawCenteredMessage(buf, -1, String.valueOf(secondsLeft));
        } else {
            if (countdownEnd + SEC_GO_MSG * 1000 > System.currentTimeMillis()) {
                // Go! Message after countdown ends.
                // Set larger font.
                buf.setFont(new Font(FONT_NAME, Font.BOLD, UI_FONTSIZE * 2));
                buf.setColor(Color.WHITE);
                drawCenteredMessage(buf, -1, "Go!");
            }
            return true;
        }
        return false;
    }

    /**
     * Draws the vehicle speed and health in the bottom right corner.
     * 
     * @param buf the Graphics2D object of the screen buffer.
     */
    public void drawVehicleStats(Graphics2D buf) {
        buf.setFont(new Font(FONT_NAME, Font.BOLD, UI_FONTSIZE));
        String speed = String.format("MPH: %.1f", vehicle.getSpeed());
        String health = String.format("Health: %.0f", vehicle.getHealth());
        double[] speedBounds = Menu.getTextBounds(buf, speed);
        double[] healthBounds = Menu.getTextBounds(buf, health);

        buf.drawString(speed, RacetrackGame.PANEL_WIDTH - (int)speedBounds[0] - SCREEN_PADDING, 
        RacetrackGame.PANEL_HEIGHT - SCREEN_PADDING);
        buf.drawString(health, RacetrackGame.PANEL_WIDTH - (int)healthBounds[0] - SCREEN_PADDING, 
        RacetrackGame.PANEL_HEIGHT - (int)speedBounds[1] - SCREEN_PADDING);
    }

    /**
     * Draws the leaderboard for the current track over the screen.
     * 
     * @param buf the Graphics2D object of the screen buffer.
     */
    public void drawLeaderboard(Graphics2D buf) {
        // Draw background
        buf.setColor(LEADERBOARD_COLOR);
        int boardX = (RacetrackGame.PANEL_WIDTH - LEADERBOARD_WIDTH) / 2;
        int boardY = (RacetrackGame.PANEL_HEIGHT - LEADERBOARD_HEIGHT) / 2;
        buf.fillRect(boardX, boardY, LEADERBOARD_WIDTH, LEADERBOARD_HEIGHT);
        
        // Draw header
        buf.setColor(Menu.TEXT_COLOR);
        buf.setFont(new Font(FONT_NAME, Font.BOLD, UI_FONTSIZE));
        String header = "Leaderboard";
        double[] headerBounds = Menu.getTextBounds(buf, header);
        int headerY = boardY + LEADERBOARD_PADDING + (int)headerBounds[1];
        drawCenteredMessage(buf, headerY, header);

        // Get the leaderboard
        if (finalLeaderboard == null) {
            finalLeaderboard = track.getLeaderboard();
        }
        
        // Calculate entry spacing
        buf.setFont(new Font(FONT_NAME, Font.PLAIN, UI_FONTSIZE));
        double[] entryBounds = Menu.getTextBounds(buf, header);
        int entrySpacing = (LEADERBOARD_HEIGHT - LEADERBOARD_PADDING * 3 - (int)headerBounds[1] -
        (int)entryBounds[1] * Leaderboard.MAX_LENGTH) / (Leaderboard.MAX_LENGTH - 1);

        while (entrySpacing < 0) {
            Font currentFont = buf.getFont();
            int nextFontSize = currentFont.getSize() - 1;
            if (nextFontSize <= -1) {
                break;
            }
            buf.setFont(new Font(
                currentFont.getFontName(), 
                currentFont.getStyle(), 
                nextFontSize
            ));
            entryBounds = Menu.getTextBounds(buf, header);
            entrySpacing = (LEADERBOARD_HEIGHT - LEADERBOARD_PADDING * 3 - (int)headerBounds[1] -
            (int)entryBounds[1] * Leaderboard.MAX_LENGTH) / (Leaderboard.MAX_LENGTH - 1);
        }

        // Draw each entry!
        for (int i = 1; i <= Leaderboard.MAX_LENGTH; i++) {
            String ranking = "";
            switch (i) {
                case 1:
                    ranking = "1st";
                    break;
                case 2:
                    ranking = "2nd";
                    break;
                case 3:
                    ranking = "3rd";
                    break;
                default:
                    ranking = i + "th";
            }

            // Calculate text dimensions
            entryBounds = Menu.getTextBounds(buf, ranking);
            int entryY = headerY + LEADERBOARD_PADDING + (int)entryBounds[1] + 
            ((int)entryBounds[1] + entrySpacing) * (i - 1);

            String name = "";
            String time = "";
            if (finalLeaderboard.size() >= i) {
                name = finalLeaderboard.get(i - 1).getName();
                time = millisToFormattedTime(finalLeaderboard.get(i - 1).getTime());
            } else {
                name = "---";
                time = "--:--:---";
            }
            // Draw ranking
            buf.drawString(ranking + ": ", boardX + LEADERBOARD_PADDING, entryY);
            entryBounds = Menu.getTextBounds(buf, ranking + ":   ");

            // Draw time
            double[] timeBounds = Menu.getTextBounds(buf, time);
            int timeX = boardX + LEADERBOARD_WIDTH - LEADERBOARD_PADDING - (int)timeBounds[0];
            buf.drawString(time, timeX, entryY);

            // Ensure name fits and draw it
            int maxNameWidth = timeX - (boardX + LEADERBOARD_PADDING + (int)entryBounds[0]);
            entryBounds = Menu.getTextBounds(buf, ranking + ": ");
            Menu.shrinkFontToFit(buf, buf.getFont(), name, maxNameWidth);
            buf.drawString(name, boardX + LEADERBOARD_PADDING + (int)entryBounds[0], entryY);
        }
    }

    /**
     * Converts the millisecond duration to a String with the format MM:SS:mmm.
     * 
     * @param millis a duration in milliseconds.
     * @return a formatted string representing the time elapsed.
     */
    public static String millisToFormattedTime(long millis) {
        return String.format("%02d:%02d:%03d", 
        (int)(millis / 1000 / 60), (int)(millis / 1000 % 60), millis % 1000);
    }

    /**
     * Draws the timer and lap counter in the top-left corner.
     * 
     * @param buf the Graphics2D object of the screen buffer.
     */
    public void drawLapTimer(Graphics2D buf) {
        buf.setFont(new Font(FONT_NAME, Font.BOLD, UI_FONTSIZE));
        long timerDur;
        if (finalTime == 0) {
            // Race hasn't ended yet.
            if (timerStart == 0) {
                timerStart = System.currentTimeMillis();
            }
            timerDur = System.currentTimeMillis() - timerStart;
            if (vehicle.getLastCheckpoint() != checkpointReached) {
                // If lap complete
                if (vehicle.getLastCheckpoint() == 1 && 
                checkpointReached == track.getCheckpointCount()) {
                    checkpointReached = 1;
                    lapsDone++;
                // if next checkpoint reached
                } else if (vehicle.getLastCheckpoint() == checkpointReached + 1) {
                    checkpointReached++;
                } else if (vehicle.getLastCheckpoint() > checkpointReached + 1 ||
                vehicle.getLastCheckpoint() == 1) {
                    drawCenteredMessage(buf, -1, "You skipped a checkpoint!");
                }
            }
        } else {
            // Race has ended.
            timerDur = finalTime;
        }
        // Print timer.
        String timerStr = "Time: " + millisToFormattedTime(timerDur);
        buf.drawString(timerStr, SCREEN_PADDING, TIMER_YPOS);

        String lapStr;
        if (lapsDone == track.getLapCount()) {
            if (finalTime == 0) {
                finalTime = System.currentTimeMillis() - timerStart;
                if (finalLeaderboard == null) {
                    // Add time to leaderboards if it qualifies.
                    List<LeaderboardEntry> leaders = track.getLeaderboard();
                    if (leaders.size() < Leaderboard.MAX_LENGTH || 
                    leaders.get(leaders.size() - 1).getTime() > finalTime) {
                        String name = JOptionPane.showInputDialog("New record! Enter your name:");
                        track.addLeaderboardEntry(new LeaderboardEntry(1, name, finalTime));
                    }
                }
            }
            drawLeaderboard(buf);
            buf.setFont(new Font(FONT_NAME, Font.BOLD, UI_FONTSIZE));
            lapStr = String.format("Lap: %d/%d (Finished!)", track.getLapCount(), 
            track.getLapCount());
        } else {
            lapStr = String.format("Lap: %d/%d", lapsDone + 1, track.getLapCount());
        }
        buf.drawString(lapStr, SCREEN_PADDING, LAPS_YPOS);
    }
}
