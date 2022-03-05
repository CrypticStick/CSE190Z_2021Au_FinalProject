import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.*;

public class Camera {

    private Track track;
    private Vehicle vehicle;

    // Top left corner of screen.
    private double xPos;
    private double yPos;

    public Camera(Track track, Vehicle vehicle) {
        this.track = track;
        this.vehicle = vehicle;
    }

    public void updatePosition() {
        double[] vehiclePos = vehicle.getPosition();

        xPos = vehiclePos[0] - RacetrackGame.PANEL_WIDTH / 2.0;
        if (xPos < 0) {
            xPos = 0;
        } else if (xPos >= track.getWidth() - RacetrackGame.PANEL_WIDTH) {
            xPos = track.getWidth() - RacetrackGame.PANEL_WIDTH - 1.0;
        }

        yPos = vehiclePos[1] - RacetrackGame.PANEL_HEIGHT / 2.0;
        if (yPos < 0) {
            yPos = 0;
        } else if (yPos >= track.getHeight() - RacetrackGame.PANEL_HEIGHT) {
            yPos = track.getHeight() - RacetrackGame.PANEL_HEIGHT - 1.0;
        }
    }

    public void draw(BufferedImage buf) {
        Graphics2D bufGraphics = buf.createGraphics();
        bufGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        track.drawTrack(bufGraphics, (int)xPos, (int)yPos);
        vehicle.drawVehicle(bufGraphics, (int)xPos, (int)yPos);
        bufGraphics.dispose();
    }


}
