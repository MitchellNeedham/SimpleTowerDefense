import java.util.HashMap;
import java.util.Map;

public class Slicer extends Enemy {

    private static final String IMG_PATH = "res/images/slicer.png";

    public Slicer(float movementSpeed, float spawnDelay) {
        super(movementSpeed, spawnDelay, IMG_PATH);
    }
}
