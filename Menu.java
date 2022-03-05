import java.awt.image.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public interface Menu {

    public static final int FONT_SIZE = 40;
    public static final Font BUTTON_FONT = new Font("Calibri", Font.BOLD, FONT_SIZE);
    public static final Color BUTTON_COLOR = new Color(64, 64, 64);
    public static final Color BUTTON_HOVER_COLOR = new Color(40, 40, 40);
    public static final Color BUTTON_CLICK_COLOR = new Color(16, 16, 16);
    public static final Color TEXT_COLOR_DISABLED = Color.GRAY;
    public static final Color TEXT_COLOR = Color.WHITE;

    /**
     * Creates a row of buttons on the screen with the given spacing and sizing values.
     * 
     * @param count the number of buttons to create.
     * @param y the y-coordinate of the top of the buttons.
     * @param width the width of each button.
     * @param height the height of each button.
     * @param screenPadding padding on the left and right sides of the screen.
     * 
     * @return an array of button coordinates.
     */
    public static Rectangle[] createRowButtons(int count, int y, int width, int height, 
    int screenPadding) {
        Rectangle[] buttons = new Rectangle[count];
        int spacing = 0;
        if (count == 1) {
            screenPadding = (RacetrackGame.PANEL_WIDTH - width) / 2;
        } else {
            spacing = (RacetrackGame.PANEL_WIDTH - 2 * screenPadding - width * count) / 
            (count - 1);
        }

        for (int i = 0; i < count; i++) {
            buttons[i] = new Rectangle(screenPadding + (spacing + width) * i, y, width, height);
        }
        return buttons;
    }

    /**
     * Shrinks the font size to fit the given text, and sets it as the current font.
     * 
     * @param buf the Graphics2D object of the screen buffer.
     * @param currentFont the font to use.
     * @param text the text to fit.
     * @param maxWidth the maximum width of the text.
     * @return the given font with fontsize scaled to fit the text.
     */
    public static Font shrinkFontToFit(Graphics2D buf, Font currentFont, String text, 
    int maxWidth) {
        buf.setFont(currentFont);
        double[] textBounds = getTextBounds(buf, text);
        while (textBounds[0] > maxWidth) {
            currentFont = buf.getFont();
            int nextFontSize = currentFont.getSize() - 1;
            buf.setFont(new Font(
                currentFont.getFontName(), 
                currentFont.getStyle(), 
                nextFontSize
            ));
            textBounds = getTextBounds(buf, text);
        }
        return currentFont;
    }

    /**
     * Creates a row of cells containing buttons on the screen with the given spacing and sizing 
     * values.
     * 
     * @param count the number of cells to create.
     * @param y the y-coordinate of the top of the cells.
     * @param wCell the width of each cell.
     * @param hCell the height of each cell.
     * @param marginButton the button margin.
     * @param hButton the height of each button.
     * @param screenPadding padding on the left and right sides of the screen.
     * 
     * @return an array of cells and an array of buttons.
     */
    public static Rectangle[][] createRowCells(int count, int y, int wCell, int hCell, 
    int marginButton, int hButton, int screenPadding) {
        Rectangle[] tempCells = Menu.createRowButtons(
            count, 
            y, 
            wCell, 
            hCell, 
            screenPadding
        );

        Rectangle[] tempButtons = Menu.createRowButtons(
            count, 
            y + hCell - marginButton - hButton, 
            wCell - 2 * marginButton, 
            hButton, 
            screenPadding + marginButton
        );
        
        return new Rectangle[][] {tempCells, tempButtons};
    }

    /**
     * Calculates the boundaries of the given String on screen.
     * 
     * @param buf the Graphics2D object of the screen buffer.
     * @param text the text to measure.
     * @return the width and height of the text on screen.
     */
    public static double[] getTextBounds(Graphics2D buf, String text) {
        Rectangle2D bounds = buf.getFont().getStringBounds(text, buf.getFontRenderContext());
        return new double[] {bounds.getWidth(), bounds.getHeight()};
    }

    /**
     * Draws the current menu to the screen.
     */
    public void drawMenu(BufferedImage buf);

    /**
     * Gets whether the navigation is ready to move to a new menu.
     * 
     * @return whether a new menu is available.
     */
    public boolean hasNextMenu();

    /**
     * Gets whether the navigation is ready to start a new track.
     * 
     * @return whether a new track is available.
     */
    public boolean hasTrack();

    /**
     * Returns an instance of the new track (or null, if no track exists).
     * 
     * @return the new track.
     */
    public Track getNewTrack();

    /**
     * Returns an instance of the next menu (or null, if the next menu doesn't exist).
     * 
     * @return the next menu.
     */
    public Menu getNextMenu();

}
