import bagel.util.Point;
import org.lwjgl.system.CallbackI;

import java.util.*;

public class Slicer extends Enemy {

    private static final String IMG_PATH = "res/images/slicer.png";
    private static final double BASE_SPEED = 2.0f;
    private static final double BASE_HEALTH = 1.0f;
    private static final double BASE_REWARD = 1.0f;
    private final String type;


    private static final LinkedHashMap<String, double[]> SlicerData = new LinkedHashMap<> () {
        {
            put("slicer", new double[]{1 * BASE_SPEED, 1 * BASE_HEALTH, 2 * BASE_REWARD});
            put("superslicer", new double[]{3/4f * BASE_SPEED, 1 * BASE_HEALTH, 15 * BASE_REWARD});
            put("megaslicer", new double[]{3/4f * BASE_SPEED, 2 * BASE_HEALTH, 10 * BASE_REWARD});
            put("apexslicer", new double[]{3/8f * BASE_SPEED, 25 * BASE_HEALTH, 150 * BASE_REWARD});
        }
    };

    // TODO: implement health and create more enemies!
    // TODO: Can multiple slicers be done better?

    /**
     * Constructor for Slicer
     * @param spawnDelay time in milliseconds after start of waves before slicer spawns
     * @param type String containing name of slicer enemy
     */
    public Slicer(int spawnDelay, String type) {
        super(type, SlicerData.get(type)[0], spawnDelay, "res/images/" + type + ".png", SlicerData.get(type)[1],
                SlicerData.get(type)[2]);
        this.type = type;
    }

    /**
     * Constructor for slicers created upon destruction of parent slicers
     * @param position position of parent slicer
     * @param type type of parent slicer
     * @param pointsIndex point new slicer should move towards
     */
    public Slicer(Point position, String type, int pointsIndex) {
        super(type, SlicerData.get(type)[0], SlicerData.get(type)[1], SlicerData.get(type)[2],
                "res/images/" + type + ".png", position, pointsIndex);
        this.type = type;
    }


    public List<String> getTypes() {
        return new ArrayList<>(SlicerData.keySet());
    }

}
