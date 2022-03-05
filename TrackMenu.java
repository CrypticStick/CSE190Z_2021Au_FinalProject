import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import java.awt.*;

public class TrackMenu implements Menu {

    private KeyInput key;
    private MouseInput mouse;
    private Rectangle[] cells;
    private Rectangle[] buttons;
    private String[] buttonsText;
    private ArrayList<Track> tracks;
    private int selectedTrackIndex;

    private BufferedImage backgroundTexture;

    public static final int FONT_SIZE = 24;
    public static final Font BUTTON_FONT = new Font("Calibri", Font.BOLD, FONT_SIZE);
    public static final Font TITLE_FONT = new Font("Calibri", Font.BOLD, FONT_SIZE);
    private static final int HEADER_HEIGHT = 120;
    private static final Color HEADER_COLOR = new Color(32, 32, 32);
    private static final int SCREEN_XPADDING = 20;
    private static final int SCREEN_YPADDING = 20;
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 56;
    private static final Color CELL_COLOR = new Color(32, 32, 32);
    private static final int CELL_HEIGHT = 240;
    private static final int CELL_MARGIN = 40;
    private static final int CELL_PADDING = 20;

    private static final int COL_COUNT = 3;

    public TrackMenu(KeyInput key, MouseInput mouse) {
        this.key = key;
        this.mouse = mouse;
        selectedTrackIndex = -1;

        try {
            backgroundTexture = ImageIO.read(new File("textures/trackTex.png"));

            // Load tracks into the tracks array
            File trackDir = new File("tracks/");
            File[] trackList = trackDir.listFiles();
            if (trackList != null) {
                tracks = new ArrayList<>();
                for (int i = 0; i < trackList.length; i++) {
                    if (trackList[i].toString().endsWith(".track")) {
                        tracks.add(new Track(trackList[i].toPath()));
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        
        // Initializing elements of the track list screen.
        cells = new Rectangle[tracks.size()];
        buttons = new Rectangle[tracks.size() + 1];
        buttonsText = new String[tracks.size() + 1];
        buttons[tracks.size()] = new Rectangle(SCREEN_XPADDING,
        (HEADER_HEIGHT - BUTTON_HEIGHT) / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        buttonsText[tracks.size()] = "Back";
        
        // Create rows of cells containing track info.
        int totalRows = (tracks.size() - 1) / COL_COUNT;
        int cellWidth = (RacetrackGame.PANEL_WIDTH - SCREEN_XPADDING - (COL_COUNT - 1) * 
        CELL_MARGIN) / COL_COUNT;
        for (int row = 0; row <= totalRows; row++) {

            // Calculate some cell dimensions.
            int colCount = (row == totalRows && tracks.size() % COL_COUNT != 0) ? 
            tracks.size() % COL_COUNT : COL_COUNT;
            
            int cellY = HEADER_HEIGHT + CELL_MARGIN + row * (CELL_HEIGHT + CELL_MARGIN);

            // Create row of cells.
            Rectangle[][] rowCells = Menu.createRowCells(colCount, cellY, cellWidth, CELL_HEIGHT,
            CELL_PADDING, BUTTON_HEIGHT, SCREEN_XPADDING);

            // Add row to the list of cells and buttons.
            for (int cell = 0; cell < rowCells[0].length; cell++) {
                int index = row * COL_COUNT + cell;
                cells[index] = rowCells[0][cell];
                buttons[index] = rowCells[1][cell];
                buttonsText[index] = "Play";
            }
        }
    }

    @Override
    public void drawMenu(BufferedImage buf) {
        Graphics2D graphics = buf.createGraphics();
        Point mousePos = mouse.getMousePosition();
        boolean mouseDown = mouse.getLeftClick();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setFont(BUTTON_FONT);

        // Draw header
        graphics.setColor(HEADER_COLOR);
        graphics.fillRect(0, 0, RacetrackGame.PANEL_WIDTH, HEADER_HEIGHT);

        //Draw background (tiles the track texture)
        for (int y = HEADER_HEIGHT; y < RacetrackGame.PANEL_HEIGHT; y++) {
            for (int x = 0; x < RacetrackGame.PANEL_WIDTH; x++) {
                buf.setRGB(x, y, backgroundTexture.getRGB(
                    x % backgroundTexture.getWidth(),
                    y % backgroundTexture.getHeight()
                ));
            }
        }

        for (int i = 0; i < buttons.length; i++) {
            boolean hover = buttons[i].contains(mousePos);
            boolean click = mouseDown && hover;

            // Color cells
            if (i < cells.length) {
                graphics.setColor(CELL_COLOR);
                graphics.fill(cells[i]);

                // Draw track preview
                int previewHeight = CELL_HEIGHT - BUTTON_HEIGHT - CELL_PADDING * 3;
                BufferedImage trackPreview = tracks.get(i).getTexturedTrack();
                graphics.drawImage(
                    trackPreview, 
                    cells[i].x + CELL_PADDING, 
                    cells[i].y + CELL_PADDING, 
                    cells[i].x + cells[i].width / 2, 
                    cells[i].y + CELL_PADDING + previewHeight,
                    0,
                    0,
                    trackPreview.getWidth(),
                    trackPreview.getHeight(),
                    null
                );

                // Draw track info
                String trackName = tracks.get(i).getName();
                graphics.setFont(TITLE_FONT);

                // Get text dimensions
                int xName = cells[i].x + cells[i].width / 2 + CELL_PADDING;
                int maxTextWidth = cells[i].x + cells[i].width - CELL_PADDING - xName;
                
                // Ensure title in the cell
                Menu.shrinkFontToFit(graphics, TITLE_FONT, trackName, maxTextWidth);
                double[] nameBounds = Menu.getTextBounds(graphics, trackName);
                int yName = cells[i].y + CELL_PADDING + (int)nameBounds[1];
                
                // Draw title
                graphics.setColor(TEXT_COLOR);
                graphics.drawString(
                    trackName, 
                    xName,
                    yName
                );

                // Ensure creator name fits in the cell
                String creatorName = "By " + tracks.get(i).getCreator();
                Font creatorFont = new Font(TITLE_FONT.getName(), Font.PLAIN, 
                TITLE_FONT.getSize());
                Menu.shrinkFontToFit(graphics, creatorFont, creatorName, maxTextWidth);
                double[] creatorBounds = Menu.getTextBounds(graphics, creatorName);

                // Draw creator
                graphics.drawString(
                    creatorName, 
                    xName,
                    yName + (int)creatorBounds[1]
                );

                // Get the best time
                List<LeaderboardEntry> leaderboard = tracks.get(i).getLeaderboard();
                String bestTime;
                if (!leaderboard.isEmpty()) {
                    bestTime = RaceUI.millisToFormattedTime(leaderboard.get(0).getTime());
                } else {
                    bestTime = "--:--:---";
                }

                // Ensure best time fits in the cell
                Font bestTimeFont = new Font(TITLE_FONT.getName(), Font.PLAIN, 
                TITLE_FONT.getSize());
                Menu.shrinkFontToFit(graphics, bestTimeFont, bestTime, maxTextWidth);
                double[] bestTimeBounds = Menu.getTextBounds(graphics, bestTime);

                // Gap between creator and best time looks better
                int lilGap = 6;

                // Draw best time
                graphics.drawString(
                    "Best time:", 
                    xName,
                    yName + (int)creatorBounds[1] + lilGap + (int)bestTimeBounds[1]
                );
                graphics.drawString(
                    bestTime, 
                    xName,
                    yName + (int)creatorBounds[1] + lilGap + (int)bestTimeBounds[1] * 2
                );
                
            }

            // Draw button box
            if (click) {
                graphics.setColor(BUTTON_CLICK_COLOR);
            } else {
                graphics.setColor(hover ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
            }
            graphics.fill(buttons[i]);

            // Draw button text
            graphics.setFont(BUTTON_FONT);
            graphics.setColor(TEXT_COLOR);
            double[] strBounds = Menu.getTextBounds(graphics, buttonsText[i]);
            graphics.drawString(
                buttonsText[i], 
                (int)(buttons[i].x + (buttons[i].width - strBounds[0]) / 2), 
                (buttons[i].y + buttons[i].height - (buttons[i].height - FONT_SIZE) / 2));
        }
        graphics.dispose();
    }

    @Override
    public boolean hasNextMenu() {
        // the last button in the array is the "back" button
        return mouse.hasClickOccured(false) && buttons[buttons.length - 1]
        .contains(mouse.getClickPosition());
    }

    @Override
    public boolean hasTrack() {
        for (int i = 0; i < tracks.size(); i++) {
            if (mouse.hasClickOccured(false) && buttons[i].contains(mouse.getClickPosition())) {
                mouse.hasClickOccured(true);
                selectedTrackIndex = i;
                return true;
            }
        } 
        mouse.hasClickOccured(true);
        return false;
    }

    @Override
    public Track getNewTrack() {
        if (selectedTrackIndex != -1) {
            return tracks.get(selectedTrackIndex);
        }
        return null;
    }

    @Override
    public Menu getNextMenu() {
        return new MainMenu(key, mouse);
    }

}
