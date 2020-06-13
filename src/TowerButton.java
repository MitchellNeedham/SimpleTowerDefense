import bagel.*;
import bagel.util.Colour;


public class TowerButton implements Clickable {

    private static final double PRICE_INCREMENT = 25;
    private static final Colour COLOUR_GREEN = new Colour(0, 255, 0);
    private static final Colour COLOUR_RED = new Colour(255, 0, 0);
    private static final double Y_OFFSET = 50;
    private static final int FONT_SIZE = 20;
    private Colour hoverColour = new Colour(255, 255, 255, 0.5);
    private final Image btnImage;
    private final String towerType;
    private final double x;
    private final double y;
    private final BoundingBox bb;
    private final Text cost;
    private double price;
    private boolean purchasable;
    private final Text info;


    /**
     * Constructor for Tower Button
     * @param towerType String containing tower type
     * @param x x-coordinate at centre of button position
     * @param y y-coordinate at centre of button position
     */
    public TowerButton(String towerType, double x, double y, double price) {
        this.towerType = towerType;
        btnImage = new Image("res/images/" + towerType + "/main.png");
        this.x = x;
        this.y = y;
        // get bounding box
        bb = new BoundingBox(x - btnImage.getHeight() / 2, y - btnImage.getHeight() / 2,
                btnImage.getWidth(), btnImage.getHeight());
        this.price = price;

        Font font = new Font("res/fonts/DejaVuSans-Bold.ttf", FONT_SIZE);
        cost = new Text(font, "$" +(int)price, x, y + Y_OFFSET);

        RenderQueue.addToQueue(0, this);
        info = null;
    }

    public TowerButton(String info, Image image, double x, double y, double price) {
        btnImage = image;
        this.x = x;
        this.y = y;
        this.price = price;
        bb = new BoundingBox(x - btnImage.getHeight() / 2, y - btnImage.getHeight() / 2,
                btnImage.getWidth(), btnImage.getHeight());
        this.price = price;
        towerType = "";

        Font font = new Font("res/fonts/DejaVuSans-Bold.ttf", FONT_SIZE);
        this.info = new Text(font, info, x, y);
        cost = new Text(font, "$" +(int)price, x, y + Y_OFFSET);
    }

    public void draw() {
        btnImage.draw(x, y);
        if (price > 0) cost.draw();
    }


    public double[] getPos() {
        return new double[]{x, y};
    }

    @Override
    public void hover(Input input) {
        if (bb.isMouseOver(input)) {
            btnImage.draw(x, y, new DrawOptions().setBlendColour(hoverColour));
        }
    }

    @Override
    public void click(Input input) {

    }

    public BoundingBox getBoundingBox() {
        return bb;
    }

    public void setClicked() { }

    public String getTowerType() {

        return towerType;
    }

    public void setPurchasable(double money) {
        if (money < price) {
            purchasable = false;
            cost.updateColour(COLOUR_RED);
        } else if (money >= price){
            purchasable = true;
            cost.updateColour(COLOUR_GREEN);
        }
    }

    public void showInfo() {
        info.boxDraw();
    }

    public boolean isPurchasable() {
        return purchasable;
    }


    public double getPrice() {
        return price;
    }
}
