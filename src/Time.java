public final class Time {

    //TODO: remove this class
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
     * @return total in game time since Time object created
     */
    public long getTotalGameTime() {
        return totalGameTime;
    }
}
