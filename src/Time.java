public final class Time {

    private long currentTime;
    private float totalGameTime;

    //constructor for time
    public Time() {
        this.currentTime = System.currentTimeMillis();
    }

    /**
     * update time, gets called every frame
     */
    public void updateTime(float timeScale) {
        long previousTime = currentTime;
        currentTime = System.currentTimeMillis();
        totalGameTime += (currentTime - previousTime)/1000f * timeScale;
    }

    /**
     * @return total in game time since Time object creared
     */
    public float getTotalGameTime() {
        return totalGameTime;
    }
}
