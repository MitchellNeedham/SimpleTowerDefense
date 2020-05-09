import java.util.HashMap;
import java.util.Map;

public class Slicer extends Enemy {

    private static final String IMG_PATH = "res/images/slicer.png";
    private static final float BASE_SPEED = 2.0f;
    private static final float BASE_HEALTH = 1.0f;
    private static final float BASE_REWARD = 1.0f;


    private static final Map<String, float[]> SlicerData = new HashMap<String, float[]> () {
        {
            put("slicer", new float[]{1 * BASE_SPEED, 1 * BASE_HEALTH, 2 * BASE_REWARD});
            put("superslicer", new float[]{3/4f * BASE_SPEED, 1 * BASE_HEALTH, 15 * BASE_REWARD});
            put("megaslicer", new float[]{3/4f * BASE_SPEED, 2 * BASE_HEALTH, 10 * BASE_REWARD});
            put("apexslicer", new float[]{3/8f * BASE_SPEED, 25 * BASE_HEALTH, 150 * BASE_REWARD});
        }
    };


    public Slicer(int spawnDelay, String type) {
        super(SlicerData.get(type)[0], spawnDelay, "res/images/" + type + ".png");

    }
}
