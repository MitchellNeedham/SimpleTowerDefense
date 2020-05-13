import bagel.Image;

public class TowerButton implements Clickable {

    private final Image btnImage;
    private final String towerType;
    private final double x;
    private final double y;
    private final BoundingBox bb;

    //TODO: polish this class

    /**
     * Constructor for Tower Button
     * @param towerType String containing tower type
     * @param x x-coordinate at centre of button position
     * @param y y-coordinate at centre of button position
     */
    public TowerButton(String towerType, double x, double y) {
        this.towerType = towerType;
        btnImage = new Image("res/images/" + towerType + ".png");
        this.x = x;
        this.y = y;
        bb = new BoundingBox(x, y, btnImage.getWidth(), btnImage.getHeight());
    }

    public void draw() {
        btnImage.draw(x, y);
    }


    public double[] getPos() {
        return new double[]{x, y};
    }

    public BoundingBox getBoundingBox() {
        return bb;
    }

    public void setClicked() {
    }

    public String getTowerType() {
        return towerType;
    }
}
