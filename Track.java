import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.*;
import java.awt.image.*;
import java.awt.*;
import java.util.List;

public class Track {

    public static final int BOOST_COLOR = Color.YELLOW.getRGB();
    public static final int GOAL_COLOR = Color.BLUE.getRGB();
    public static final int GRASS_COLOR = Color.BLACK.getRGB();
    public static final int PIT_COLOR = Color.GREEN.getRGB();
    public static final int TRACK_COLOR = Color.WHITE.getRGB();
    public static final int WALL_COLOR = Color.RED.getRGB();
    // Checkpoints are blue, with green value representing the checkpoint index.
    // (so, up to 255 checkpoints allowed).

    // Loaded from track file
    private String trackId;
    private short lapCount;
    private int carStartX;
    private int carStartY;
    private int carStartDeg;
    private String name;
    private String creator;
    private BufferedImage trackData;

    // Generated while scanning colors for textured track.
    private int checkpointCount;
    private BufferedImage texturedTrack;

    private BufferedImage boostTex;
    private BufferedImage chkptTex;
    private BufferedImage goalTex;
    private BufferedImage grassTex;
    private BufferedImage pitTex;
    private BufferedImage trackTex;
    private BufferedImage wallTex;

    public Track(Path track) {
        try {
            byte[] trackBytes = Files.readAllBytes(track);
            ByteArrayInputStream trackStream = new ByteArrayInputStream(trackBytes);
            
            // Check magic string
            char[] magicReq = new char[] {'T', 'R', 'A', 'C'};
            byte[] magicDat = trackStream.readNBytes(4);
            for (int i = 0; i < magicReq.length; i++) {
                if (magicReq[i] != (char)magicDat[i]) {
                    throw new IOException("The provided track file is invalid.");
                }
            }

            // Get trackId
            StringBuilder trackIdBuilder = new StringBuilder();
            byte[] trackIdDat = trackStream.readNBytes(16);
            for (int i = 0; i < trackIdDat.length; i++) {
                trackIdBuilder.append(Character.toString((char)trackIdDat[i]));
            }
            trackId = trackIdBuilder.toString();

            // Get lap count
            lapCount = ByteBuffer.wrap(trackStream.readNBytes(2)).getShort();

            // Get vehicle starting position
            carStartX = ByteBuffer.wrap(trackStream.readNBytes(4)).getInt();
            carStartY = ByteBuffer.wrap(trackStream.readNBytes(4)).getInt();
            carStartDeg = ByteBuffer.wrap(trackStream.readNBytes(4)).getInt();

            // Get track name
            int nameSize = ByteBuffer.wrap(trackStream.readNBytes(4)).getInt();
            StringBuilder nameBuilder = new StringBuilder();
            byte[] nameDat = trackStream.readNBytes(nameSize);
            for (int i = 0; i < nameDat.length; i++) {
                nameBuilder.append(Character.toString((char)nameDat[i]));
            }
            name = nameBuilder.toString();

            // Get creator name
            int creatorSize = ByteBuffer.wrap(trackStream.readNBytes(4)).getInt();
            StringBuilder creatorBuilder = new StringBuilder();
            byte[] creatorDat = trackStream.readNBytes(creatorSize);
            for (int i = 0; i < creatorDat.length; i++) {
                creatorBuilder.append(Character.toString((char)creatorDat[i]));
            }
            creator = creatorBuilder.toString();

            // The remaining data is the track image; load it in!
            trackData = ImageIO.read(trackStream);

            boostTex = ImageIO.read(new File("textures/boostTex.png"));
            chkptTex = ImageIO.read(new File("textures/chkptTex.png"));
            goalTex = ImageIO.read(new File("textures/goalTex.png"));
            grassTex = ImageIO.read(new File("textures/grassTex.png"));
            pitTex = ImageIO.read(new File("textures/pitTex.png"));
            trackTex = ImageIO.read(new File("textures/trackTex.png"));
            wallTex = ImageIO.read(new File("textures/wallTex.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        generateTexturedTrack();
    }

    /**
     * Gets the width of the given track.
     * 
     * @return the track width.
     */
    public int getWidth() {
        return trackData.getWidth();
    }

    /**
     * Gets the height of the given track.
     * 
     * @return the track height.
     */
    public int getHeight() {
        return trackData.getHeight();
    }

    /**
     * Gets the number of checkpoints on the track.
     * 
     * @return the number of checkpoints on the track.
     */
    public int getCheckpointCount() {
        return checkpointCount;
    }

    /**
     * Gets the name of the track.
     * 
     * @return the track name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the creator of the track.
     * 
     * @return the creator's name.
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Gets the starting position of the vehicle.
     * 
     * @return the starting position of the vehicle {x, y, deg}.
     */
    public int[] getCarStartPosition() {
        return new int [] {carStartX, carStartY, carStartDeg};
    }

    /**
     * Gets the required lap count for the track.
     * 
     * @return the total lap count.
     */
    public short getLapCount() {
        return lapCount;
    }

    /**
     * Gets the track ID.
     * 
     * @return the track ID.
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * Gets the leaderboard for the track.
     * 
     * @return the track's leaderboard.
     */
    public List<LeaderboardEntry> getLeaderboard() {
        return RacetrackGame.leaderboard.getLeaderboard(trackId);
    }

    /**
     * Saves the given entry to the leaderboard.
     * 
     * @param entry the leaderboard entry to save.
     */
    public void addLeaderboardEntry(LeaderboardEntry entry) {
        RacetrackGame.leaderboard.saveEntry(trackId, entry);
    }

    /**
     * Draws the track to the screen.
     * 
     * @param x the x-coordinate of the image to draw in the top-left corner.
     * @param y the y-coordinate of the image to draw in the top-left corner.
     */
    public void drawTrack(Graphics2D buf, int x, int y) {
        buf.drawImage(texturedTrack, -x, -y, null);
    }

    /**
     * Checks if a color belongs to a checkpoint, and returns its index.
     * @param color the color to check.
     * @return the index of the checkpoint (or -1, if not a checkpoint).
     */
    public static int isCheckpointColor(int color) {
        Color toCheck = new Color(color);
        if (toCheck.getRed() == 0 && toCheck.getBlue() == 255) {
            return toCheck.getGreen() + 1;
        }
        return -1;
    }

    private int getTextureColor(int x, int y, int trackColor) {
        if (trackColor == BOOST_COLOR) {
            return boostTex.getRGB(x % boostTex.getWidth(), y % boostTex.getHeight());
        } else if (trackColor == GOAL_COLOR) {
            return goalTex.getRGB(x % goalTex.getWidth(), y % goalTex.getHeight());
        } else if (trackColor == GRASS_COLOR) {
            return grassTex.getRGB(x % grassTex.getWidth(), y % grassTex.getHeight());
        } else if (trackColor == PIT_COLOR) {
            return pitTex.getRGB(x % pitTex.getWidth(), y % pitTex.getHeight());
        } else if (trackColor == TRACK_COLOR) {
            return trackTex.getRGB(x % trackTex.getWidth(), y % trackTex.getHeight());
        } else if (trackColor == WALL_COLOR) {
            return wallTex.getRGB(x % wallTex.getWidth(), y % wallTex.getHeight());
        } else {
            // if checkpoint
            int checkpoint = isCheckpointColor(trackColor);
            if (checkpoint != -1) {
                // store checkpoint if it's larger than the current count.
                checkpointCount = checkpoint > checkpointCount ? checkpoint : checkpointCount;
                return chkptTex.getRGB(x % chkptTex.getWidth(), y % chkptTex.getHeight());
            }
            // Default texture is grass.
            return grassTex.getRGB(x % grassTex.getWidth(), y % grassTex.getHeight());
        }
    }

    private void generateTexturedTrack() {
        texturedTrack = new BufferedImage(trackData.getWidth(), trackData.getHeight(), 
        BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < trackData.getHeight(); y++) {
            for (int x = 0; x < trackData.getWidth(); x++) {
                texturedTrack.setRGB(x, y, getTextureColor(x, y, trackData.getRGB(x, y)));
            }
        }
    }

    /**
     * Gets the track data.
     * 
     * @return the track data.
     */
    public BufferedImage getTrackData() {
        return trackData;
    }

    /**
     * Gets the textured track.
     * 
     * @return the textured track.
     */
    public BufferedImage getTexturedTrack() {
        return texturedTrack;
    }
}
