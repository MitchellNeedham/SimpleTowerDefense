public final class Time {

    private long currentTime;
    private long totalGameTime;

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
        totalGameTime += (currentTime - previousTime) * timeScale;
    }

    /**
     * @return total in game time since Time object creared
     */
    public long getTotalGameTime() {
        return totalGameTime;
    }
}
