import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.*;

public class MainMenu implements Menu {

    private KeyInput key;
    private MouseInput mouse;
    private Rectangle[] buttons;
    private String[] buttonsText;

    private BufferedImage backgroundImage;

    private static final int SCREEN_XPADDING = 120;
    private static final int SCREEN_YPADDING = 100;
    private static final int BUTTON_WIDTH = 240;
    private static final int BUTTON_HEIGHT = 120;

    public MainMenu(KeyInput key, MouseInput mouse) {
        this.key = key;
        this.mouse = mouse;

        try {
            backgroundImage = ImageIO.read(new File("textures/mainMenu.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        buttons = Menu.createRowButtons(
            2, 
            RacetrackGame.PANEL_HEIGHT - SCREEN_YPADDING - BUTTON_HEIGHT, 
            BUTTON_WIDTH, 
            BUTTON_HEIGHT, 
            SCREEN_XPADDING
        );
        buttonsText = new String[] {
            "Race",
            "Track Builder"
        };
    }

    @Override
    public void drawMenu(BufferedImage buf) {
        Graphics2D graphics = buf.createGraphics();
        Point mousePos = mouse.getMousePosition();
        boolean mouseDown = mouse.getLeftClick();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setFont(BUTTON_FONT);

        //Draw background
        graphics.drawImage(backgroundImage, 
        0, 0, RacetrackGame.PANEL_WIDTH, RacetrackGame.PANEL_HEIGHT, 
        0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), 
        null);

        for (int i = 0; i < buttons.length; i++) {
            boolean hover = buttons[i].contains(mousePos);
            boolean click = mouseDown && hover;

            // Draw button box
            if (click) {
                graphics.setColor(BUTTON_CLICK_COLOR);
            } else {
                graphics.setColor(hover ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
            }

            // REMOVE WHEN "Track Builder" IS CREATED
            if (i == 1) {
                graphics.setColor(BUTTON_COLOR);
            }

            graphics.fill(buttons[i]);

            // Draw button text
            graphics.setColor(TEXT_COLOR);

            // REMOVE WHEN "Track Builder" IS CREATED
            if (i == 1) {
                graphics.setColor(TEXT_COLOR_DISABLED);
            }

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
        return mouse.hasClickOccured(true) && buttons[0].contains(mouse.getClickPosition());
    }

    @Override
    public boolean hasTrack() {
        return false;
    }

    @Override
    public Track getNewTrack() {
        return null;
    }

    @Override
    public Menu getNextMenu() {
        return new TrackMenu(key, mouse);
    }

}
