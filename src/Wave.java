public class Wave {
    private int wave;
    private boolean waveComplete = true;

    public Wave(int wave) {
        this.wave = wave;
        this.waveComplete = false;
    }

    public boolean isWaveComplete() {
        return waveComplete;
    }
}
