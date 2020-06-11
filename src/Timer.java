public final class Timer {

    private long currentTime;
    private long totalGameTime;

    //constructor for time
    public Timer() {
        this.currentTime = System.currentTimeMillis();
    }

    /**
     * update time, gets called every frame
     */
    public void updateTime() {
        long previousTime = currentTime;
        currentTime = System.currentTimeMillis();
        totalGameTime += (currentTime - previousTime) * ShadowDefend.getTimescale();
    }

    /**
     * @return total in game time since Timer object created
     */
    public long getTotalGameTime() {
        return totalGameTime;
    }

    /**
     * Resets timer
     */
    public void reset() {
        this.totalGameTime = 0;
        this.currentTime = System.currentTimeMillis();
    }
}
