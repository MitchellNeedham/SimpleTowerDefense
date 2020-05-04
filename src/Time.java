public final class Time {

    private final long initialTime;
    private long currentTime;
    private long previousTime;
    private float totalGameTime;

    //constructor for time
    public Time() {
        this.initialTime = System.currentTimeMillis();
        this.currentTime = initialTime;
    }

    /**
     * update time, gets called every frame
     */
    public void updateTime(float timeScale) {
        previousTime = this.currentTime;
        this.currentTime = System.currentTimeMillis();
        totalGameTime += (currentTime - previousTime)/1000f * timeScale;
    }

    /**
     * @return total in game time since Time object creared
     */
    public float getTotalGameTime() {
        return totalGameTime;
    }
}
