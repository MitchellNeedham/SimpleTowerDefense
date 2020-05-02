import bagel.map.TiledMap;

public class Level {

    private final static String MAP_PATH = "res/levels/";
    private final static String MAP_EXT = ".tmx";

    private final int level;
    private final int currentWave = 1;
    private Wave wave;

    /**
     * level constructor
     * @param level Number of level
     */
    public Level(int level) {
        this.level = level;
    }

    /**
     * creates map for level
     * @return TiledMap object with map file loaded
     */
    public TiledMap createMap() {
        return new TiledMap(MAP_PATH + level + MAP_EXT);
    }

    /**
     * begins wave if next wave exists
     * @return Wave object
     */
    public Wave startNextWave() {
        wave = new Wave(level, currentWave);
        return wave;
    }

    /**
     * Determines if a wave has completed (i.e no more enemies)
     * @return true or false depending on whether wave is nullified
     */
    public boolean isWaveComplete() {
        return wave == null;
    }

}
