import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class Vehicle {
    
    private final double TOP_SPEED;
    private final double ACCELERATION;
    private final double HANDLING;
    private final Color COLOR;

    // Maximum percent traction during drifts
    private static final double DRIFT_PCT = 0.04;
    // Represents the fraction of a second required to reach full turning speed.
    // Larger values cause sharper turns.
    private static final double TURN_SMOOTHING = 9;
    // Minimum speed (in mph) required to take damage.
    private static final double MIN_DAMAGE_SPEED = 10;
    // The multiplier for collision speed deducted from health.
    private static final double DAMAGE_MULTIPLIER = 1;
    // The maximum health of the vehicle.
    private static final double MAX_HEALTH = 100;
    // Heal speed (in HP per second).
    private static final double HEAL_SPEED = 50;
    
    private static final double FT_LENGTH_CAR = 14.7;


    private Track currentTrack;

    private BufferedImage vehicleImage;
    private BufferedImage vehicleMask;
    private int vehiclePixelCount;

    private double totVel;
    private double xVel;
    private double yVel;
    private double xPos;
    private double yPos;
    private double rotVel;
    private double rotation;
    private double health;

    private double xLastPos;
    private double yLastPos;
    private double lastRotation;

    private boolean isAtRepairPit;
    private int lastCheckpointReached;

    private long lastUpdateTime = 0;
    private boolean drifting = false;
    private boolean wasDrifting = false;

    /**
     * Creates a new vehicle.
     * 
     * @param topSpeed the top speed of the vehicle (in pixels/second)
     * @param acceleration the acceleration of the vehicle (in pixels/second^2)
     * @param handling the turning speed of the vehicle (in radians/second)
     * @param color the color of the vehicle.
     */
    public Vehicle(double topSpeed, double acceleration, double handling, Color color) {
        TOP_SPEED = topSpeed;
        ACCELERATION = acceleration;
        HANDLING = handling;
        COLOR = color;

        totVel = 0;
        xVel = 0;
        yVel = 0;
        xPos = 0;
        yPos = 0;

        xLastPos = 0;
        yLastPos = 0;

        isAtRepairPit = false;
        lastCheckpointReached = 0;
        health = MAX_HEALTH;

        try {
            vehicleImage = ImageIO.read(new File("textures/vehicle.png"));
            processVehicleImage();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Replaces every red pixel of the vehicle with the target color,
     * counts vehicle pixels, and generates a collision mask.
     */
    private void processVehicleImage() {
        vehiclePixelCount = 0;
        vehicleMask = new BufferedImage(vehicleImage.getWidth(), vehicleImage.getHeight(),
        BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < vehicleImage.getHeight(); y++) {
            for (int x = 0; x < vehicleImage.getWidth(); x++) {
                if (new Color(vehicleImage.getRGB(x, y), true).getAlpha() == 0) {
                    continue;
                } else {
                    vehicleMask.setRGB(x, y, Color.WHITE.getRGB());
                }
                if (vehicleImage.getRGB(x, y) == Color.RED.getRGB()) {
                    vehicleImage.setRGB(x, y, COLOR.getRGB());
                }
                vehiclePixelCount++;
            }
        }
    }

    /**
     * Updates the rotation, position, and health of the vehicle.
     * This method should run before each frame is drawn.
     * 
     * @param accelerate whether the vehicle is accelerating.
     * @param reverse whether the vehicle is accelerating in reverse.
     * @param brake whether the vehicle is braking.
     * @param turnLeft whether the vehicle is turning left.
     * @param turnRight whether the vehicle is turning right.
     */
    public void updateVehicle(boolean forward, boolean reverse, boolean brake, boolean turnLeft,
    boolean turnRight) {
        if (lastUpdateTime != 0) {
            double deltaSeconds = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;

            if (isAtRepairPit && health != MAX_HEALTH) {
                health += HEAL_SPEED * deltaSeconds;
                if (health > MAX_HEALTH) {
                    health = MAX_HEALTH;
                }
            }

            double terrainSpeed = getTerrainSpeed();
            if (terrainSpeed == 0) {
                // If we can't move, don't bother updating vehicle.
                return;
            }

            // Terrain impacts top speed by 100% and acceleration by 50%.
            double currentTopSpeed = TOP_SPEED * terrainSpeed;
            // No health will significantly hurt speed.
            if (health == 0) {
                currentTopSpeed /= 2;
            }
            double currentAcceleration = ACCELERATION * (1 + terrainSpeed) / 2;

            // Enable drift when both gas and brakes are applied.
            drifting = brake && (forward || reverse);
            if (drifting) {
                wasDrifting = true;
            }
            
            lastRotation = rotation;

            // Manage turning speeds. If drifting, vehicle turns faster but more gradually.
            double turnTopVel = HANDLING * (drifting ? 
            (1 + (totVel / currentTopSpeed)) / 2 : 
            (totVel / currentTopSpeed)) * deltaSeconds;
            // Snappier turning at high speeds, smoother turning when slower or drifting.
            double turnSmoothing = (drifting ? TURN_SMOOTHING / 2 : TURN_SMOOTHING) * terrainSpeed;
            if (turnLeft && !turnRight) {
                // Turn speed depends on how much time has gone by, 
                // predefined speed constants, and how fast the vehicle is already moving.
                rotVel += turnTopVel * turnSmoothing * deltaSeconds;
            } else if (turnRight && !turnLeft) {
                rotVel -= turnTopVel * turnSmoothing * deltaSeconds;
            } else {
                double rotDir = Math.signum(rotVel);
                rotVel = Math.abs(rotVel) - (HANDLING * turnSmoothing * deltaSeconds);
                if (rotVel < 0) {
                    rotVel = 0;
                } else {
                    rotVel *= rotDir;
                }
            }

            // Ensure turn speed doesn't exceed max handling value.
            if (rotVel != 0) {
                if (rotVel > Math.abs(turnTopVel)) {
                    rotVel = Math.abs(turnTopVel);
                } else if (rotVel < -Math.abs(turnTopVel)) {
                    rotVel = -Math.abs(turnTopVel);
                }
            }
            // Ensure rotation won't grow too large.
            rotation += rotVel;
            rotation %= 2 * Math.PI;

            // Don't launch the car if only the brakes are applied after drift.
            if (wasDrifting && !drifting) {
                if (!forward && !reverse) {
                    totVel = Math.sqrt(
                    Math.pow(Math.cos(rotation) * xVel, 2) + 
                    Math.pow(Math.sin(rotation) * yVel, 2)
                    ) * Math.signum(totVel);
                }
                wasDrifting = false;
            }

            // Manage acceleration.
            if (forward || reverse) {
                // Accelerate based on rotation and time passed since last update.
                totVel += currentAcceleration * deltaSeconds * (forward ? 1 : -1);
                if (totVel > currentTopSpeed) {
                    totVel = currentTopSpeed;
                } else if (totVel < -currentTopSpeed/2) {
                    totVel = -currentTopSpeed/2;
                }
            } else {
                int sigVel = (int)Math.signum(totVel);
                double brakeSpeed = brake ? 2 : 0.5;
                totVel -= currentAcceleration * deltaSeconds * brakeSpeed * sigVel;
                if (sigVel != (int)Math.signum(totVel)) {
                    totVel = 0;
                }
            }

            if (!drifting) {
                xVel = Math.cos(rotation) * totVel;
                yVel = Math.sin(rotation) * totVel;
            } else {
                xVel = xVel * (1 - DRIFT_PCT) + Math.cos(rotation) * totVel * DRIFT_PCT;
                yVel = yVel * (1 - DRIFT_PCT) + Math.sin(rotation) * totVel * DRIFT_PCT;
            }

            xLastPos = xPos;
            yLastPos = yPos;

            xPos += xVel * deltaSeconds;
            yPos -= yVel * deltaSeconds;
        }

        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Sets the current track that the vehicle is on.
     * @param track the track that the vehicle is on.
     */
    public void setTrack(Track track) {
        currentTrack = track;
    }

    /**
     * Gets the position of the vehicle, relative to its center.
     * 
     * @return the position of the center of the vehicle.
     */
    public double[] getPosition() {
        return new double[] {xPos, yPos};
    }

    /**
     * Sets the position of the vehicle, relative to its center.
     * 
     * @param x the x-coordinate of the center of the vehicle.
     * @param y the y-coordinate of the center of the vehicle.
     */
    public void setPosition(double x, double y) {
        xLastPos = x;
        yLastPos = y;

        xPos = x;
        yPos = y;
    }

    /**
     * Moves the vehicle the given distance.
     * 
     * @param x the distance to move along the x-axis.
     * @param y the distance to move along the y-axis.
     */
    public void translate(double x, double y) {
        xLastPos = xPos;
        yLastPos = yPos;

        xPos += x;
        yPos += y;
    }

    /**
     * Sets the rotation of the vehicle about its center.
     * 
     * @param rad the angle of rotation, in radians.
     */
    public void setRotation(double rad) {
        rotation = rad % (2 * Math.PI);
        lastRotation = rotation; 
    }

    /**
     * Rotates the vehicle about its center.
     * 
     * @param rad the angle of rotation, in radians.
     */
    public void rotate(double rad) {
        lastRotation = rotation;
        rotation += rad;
        rotation %= 2 * Math.PI;
    }

    /**
     * Gets the total velocity of the vehicle.
     * 
     * @return the velocity of the vehicle.
     */
    public double getVelocity() {
        return totVel;
    }

    /**
     * Gets the total speed of the vehicle in mph.
     * 
     * @return the speed of the vehicle (in mph).
     */
    public double getSpeed() {
        double feetPerPixel = FT_LENGTH_CAR / vehicleImage.getWidth();
        double feetPerSecond = feetPerPixel * Math.abs(totVel);
        // Return miles per hour
        return feetPerSecond / 5280 * 3600;
    }

    /**
     * Sets the velocity of the vehicle.
     * 
     * @param velocity the velocity of the vehicle.
     */
    public void setVelocity(double velocity) {
        totVel = velocity;
    }

    /**
     * Creates an image with an alpha channel that represents the vehicle's collision boundaries.
     * 
     * @return image of the vehicle's position.
     */
    public BufferedImage vehicleMask(int width, int height) {
        BufferedImage map = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = map.createGraphics();
        graphics.drawRenderedImage(vehicleMask, getDrawTransform(0, 0));
        graphics.dispose();
        return map;
    }

    /**
     * Calculates the current transform of the vehicle, used for drawing it to the screen.
     * 
     * @return the transformation of the vehicle (for drawing to the screen).
     */
    private AffineTransform getDrawTransform(int x, int y) {
        // First, center vehicle at origin.
        AffineTransform xformCenter = AffineTransform.getTranslateInstance(
        -vehicleImage.getWidth()/2.0, -vehicleImage.getHeight() / 2.0);
        // Next, rotate vehicle.
        AffineTransform xformRot = AffineTransform.getRotateInstance(-rotation);
        // Move center of vehicle to target location.
        AffineTransform xform = AffineTransform.getTranslateInstance(xPos, yPos);
        // Finally, shift vehicle according to camera coordinates.
        AffineTransform camTranslate = AffineTransform.getTranslateInstance(-x, -y);

        xformRot.concatenate(xformCenter);
        xform.concatenate(xformRot);
        camTranslate.concatenate(xform);
        
        return camTranslate;
    }

    /**
     * Returns a rotated rectangle representing the bounds of the vehicle.
     * 
     * @return the bounds of the vehicle.
     */
    public Polygon getBounds() {
        int halfheight = vehicleImage.getHeight() / 2;
        int halfWidth = vehicleImage.getWidth() / 2;

        int[] xCoords = new int[4];
        int[] yCoords = new int[4];
        // Add corners of non-rotated or translated vehicle
        for (int i = 0; i < 4; i++) {
            xCoords[i] = i < 2 ? halfWidth : -halfWidth;
            yCoords[i] = (0 < i && i < 3) ? -halfheight : halfheight;
        }

        // Rotate and translate corners
        for (int e = 0; e < 4; e++) {
            int newXCoord = (int)(Math.cos(-rotation) * xCoords[e] - Math.sin(-rotation) * 
            yCoords[e] + xPos);
            int newYCoord = (int)(Math.sin(-rotation) * xCoords[e] + Math.cos(-rotation) * 
            yCoords[e] + yPos);
            xCoords[e] = newXCoord;
            yCoords[e] =newYCoord;
        }

        return new Polygon(xCoords, yCoords, 4);
    }

    /**
     * Draws the current vehicle to the screen.
     */
    public void drawVehicle(Graphics2D buf, int x, int y) {
        buf.drawRenderedImage(vehicleImage, getDrawTransform(x, y));
    }

    /**
     * Gets the last change in movement of the vehicle.
     * 
     * @return an array of transformations {deltaX, deltaY, deltaRot}.
     */
    public double[] getLastMovement() {
        double rotChange = rotation - lastRotation;
        // If change in rotation transitions from 360 to 0 (or 0 to 360), 
        // we'll calculate the true difference in angle.
        if (rotChange < -Math.PI) {
            rotChange += Math.PI * 2;
        } else if (rotChange > Math.PI) {
            rotChange -= Math.PI * 2;
        }
        return new double[] {
            xPos - xLastPos,
            yPos - yLastPos,
            rotChange
        };
    }

    /**
     * Gets the health of the vehicle.
     * 
     * @return the health of the vehicle.
     */
    public double getHealth() {
        return health;
    }

    /**
     * Gets the last checkpoint that the vehicle has crossed.
     * 
     * @return the last checkpoint reached.
     */
    public int getLastCheckpoint() {
        return lastCheckpointReached;
    }

    /**
     * Handles potential collisions with the current track.
     * 
     */
    public void manageCollisions() {
        if (checkCollisions()) {
            // Take damage from impact.
            double impactSpeed = getSpeed();
            // If collision occurs, see if the vehicle can slip off the wall a bit.
            boolean canSlip = escapeCollision();
            if (canSlip) {
                totVel = totVel * 17/18;
                rotVel = 0;
            } else {
                totVel = 0;
                if (impactSpeed > MIN_DAMAGE_SPEED) {
                    health -= impactSpeed * DAMAGE_MULTIPLIER;
                    if (health < 0) {
                        health = 0;
                    }
                }
            }
        }
    }

    /**
     * Gets a speed multiplier based on the terrain beneath the vehicle.
     * @return
     */
    private double getTerrainSpeed() {
        BufferedImage trackDat = currentTrack.getTrackData();
            Polygon vehicleBounds = getBounds();
            Rectangle lazyScanRegion = vehicleBounds.getBounds();
            BufferedImage globalVehicleMask = vehicleMask(trackDat.getWidth(), 
            trackDat.getHeight());

            int grassCount = 0;
            int trackCount = 0;
            int boostCount = 0;

            for (int y = (int)lazyScanRegion.getMinY(); y <= (int)lazyScanRegion.getMaxY(); y++) {
                for (int x = (int)lazyScanRegion.getMinX(); x <= (int)lazyScanRegion.getMaxX(); 
                x++) {
                    if (0 > x || x >= trackDat.getWidth() || 
                    0 > y || y >= trackDat.getHeight()) {
                        continue;
                    }
                    if (globalVehicleMask.getRGB(x, y) == Color.WHITE.getRGB()) {
                        int color = trackDat.getRGB(x, y);
                        if (color == Track.GRASS_COLOR) {
                            grassCount++;
                        } else if (color == Track.TRACK_COLOR || color == Track.PIT_COLOR || 
                        color == Track.GOAL_COLOR) {
                            trackCount++;
                        } else if (color == Track.BOOST_COLOR) {
                            boostCount++;
                        } else {
                            // Default terrain is grass.
                            grassCount++;
                        }
                    }
                }
            }

            // Boosters affect speed by factor of 1.5, and grass by a factor of 1/2.5.
            return (1.5 * boostCount + trackCount + grassCount / 2.5) / vehiclePixelCount;
    }

    /**
     * Attempts to remove the vehicle from a collision, and reports if the vehicle is sliding 
     * against the wall.
     * 
     * @return if the vehicle slid against the wall.
     */
    private boolean escapeCollision() {
        double[] lastMove = getLastMovement();
        double totalDistance = Math.sqrt(lastMove[0] * lastMove[0] + lastMove[1] * lastMove[1]);
        if (totalDistance == 0) {
            if (lastMove[2] == 0) {
                return false;
            } else {
                totalDistance = Math.abs(lastMove[2]);
            }
        }
        double stepX = lastMove[0] / totalDistance;
        double stepY = lastMove[1] / totalDistance;
        double stepDeg = lastMove[2] / totalDistance;

        double[] initPos = new double[] {xLastPos, yLastPos, lastRotation};
        boolean checkedLeftSlip = false;
        boolean checkedRightSlip = false;
        
        // Walk backwards along the last movement, pixel-by-pixel, to check for collisions.
        // Very expensive to run, but ensures every collision is detected!
        // Sometimes still allows users to clip into walls, but collisions are still detected.
        double scannedDistance = totalDistance;
        while (scannedDistance > 0) {
            if (!checkedLeftSlip) {
                rotate(Math.toRadians(1));
            } else if (!checkedRightSlip) {
                rotate(Math.toRadians(-1));
            } else {
                xPos = initPos[0] + stepX * scannedDistance;
                yPos = initPos[1] + stepY * scannedDistance;
                rotation = initPos[2] + stepDeg * scannedDistance;
            }

            BufferedImage trackDat = currentTrack.getTrackData();
            Polygon vehicleBounds = getBounds();
            Rectangle lazyScanRegion = vehicleBounds.getBounds();
            BufferedImage globalVehicleMask = vehicleMask(trackDat.getWidth(), 
            trackDat.getHeight());

            boolean isColliding = false;

            for (int y = (int)lazyScanRegion.getMinY(); y <= (int)lazyScanRegion.getMaxY(); y++) {
                if (isColliding) {
                    break;
                }
                for (int x = (int)lazyScanRegion.getMinX(); x <= (int)lazyScanRegion.getMaxX(); 
                x++) {
                    if (0 > x || x >= trackDat.getWidth() || 
                    0 > y || y >= trackDat.getHeight()) {
                        continue;
                    } else if ((x == 0 || x == trackDat.getWidth() - 1 
                    || y == 0 || y == trackDat.getHeight() - 1) &&
                    globalVehicleMask.getRGB(x, y) == Color.WHITE.getRGB()) {
                        // if vehicle is crossing the track boundaries:
                        isColliding = true;
                        break;
                    }
                    if (trackDat.getRGB(x, y) == Track.WALL_COLOR && 
                    globalVehicleMask.getRGB(x, y) == Color.WHITE.getRGB()) {
                        isColliding = true;
                        break;
                    }
                }
            }
            if (isColliding) {
                if (!checkedLeftSlip) {
                    rotate(Math.toRadians(-1));
                    checkedLeftSlip = true;
                } else if (!checkedRightSlip) {
                    rotate(Math.toRadians(1));
                    checkedRightSlip = true;
                } else if (--scannedDistance < 0) {
                    xPos = initPos[0];
                    yPos = initPos[1];
                    rotation = initPos[2];
                    return false;
                }
            } else {
                return !checkedLeftSlip || !checkedRightSlip;
            }
        }
        return false;
    }

    /**
     * Checks whether a vehicle with the given offset is colliding with a wall.
     * If so, it corrects the vehicle's position and returns the point of impact 
     * (in global coordinates).
     * 
     * @return the point of impact, in global coordinates (or null, if no collusion is occuring).
     */
    private boolean checkCollisions() {
        BufferedImage trackDat = currentTrack.getTrackData();
        Polygon vehicleBounds = getBounds();
        Rectangle lazyScanRegion = vehicleBounds.getBounds();
        BufferedImage globalVehicleMask = vehicleMask(trackDat.getWidth(), 
        trackDat.getHeight());
        boolean isColliding = false;
        isAtRepairPit = false;

        for (int y = (int)lazyScanRegion.getMinY(); y <= (int)lazyScanRegion.getMaxY(); y++) {
            for (int x = (int)lazyScanRegion.getMinX(); x <= (int)lazyScanRegion.getMaxX(); 
            x++) {
                if (0 > x || x >= trackDat.getWidth() || 
                0 > y || y >= trackDat.getHeight()) {
                    break;
                } 
                
                // If this pixel overlaps the vehicle:
                if (globalVehicleMask.getRGB(x, y) == Color.WHITE.getRGB()) {

                    if (x == 0 || x == trackDat.getWidth() - 1 
                    || y == 0 || y == trackDat.getHeight() - 1) {
                        // if vehicle is going outside the track:
                        isColliding = true;
                    }
                    // If vehicle crosses checkpoint, we'll keep track of it.
                    int checkpoint = Track.isCheckpointColor(trackDat.getRGB(x, y));
                    if (checkpoint != -1) {
                        lastCheckpointReached = checkpoint;
                    } else if (trackDat.getRGB(x, y) == Track.PIT_COLOR) {
                        // If vehicle is touching repair pit, store state!
                        isAtRepairPit = true;
                    } else if (trackDat.getRGB(x, y) == Track.WALL_COLOR) {
                        // If hitting a wall, we are colliding!s
                        isColliding = true;
                    }
                }
            }
        }
        return isColliding;
    }
}
