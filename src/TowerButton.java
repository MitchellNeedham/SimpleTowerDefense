import bagel.DrawOptions;
import bagel.Font;
import bagel.Image;
import bagel.Input;
import bagel.util.Colour;


public class TowerButton {

    //-------------------------TOWER BUTTON PROPERTIES-------------------------//

    private static final double Y_OFFSET = 50;
    private static final int FONT_SIZE = 20;
    private final static String FONT_FILE = "res/fonts/DejaVuSans-Bold.ttf";
    private final static String IMAGE_LOCATION = "res/images/";
    private final static String IMAGE_EXT = ".png";
    private final static String CURRENCY_SYMBOL = "$";

    private final Image btnImage;
    private final String towerType;
    private final double x;
    private final double y;
    private final BoundingBox bb;
    private final Text cost;
    private final double price;
    private boolean purchasable;


    /**
     * Constructor for Tower Button
     * @param towerType String containing tower type
     * @param x x-coordinate at centre of button position
     * @param y y-coordinate at centre of button position
     * @param price cost of tower
     */
    public TowerButton(String towerType, double x, double y, double price) {
        this.towerType = towerType;
        btnImage = new Image(IMAGE_LOCATION + towerType + IMAGE_EXT);
        this.x = x;
        this.y = y;
        // create bounding box
        bb = new BoundingBox(x - btnImage.getHeight() / 2, y - btnImage.getHeight() / 2,
                btnImage.getWidth(), btnImage.getHeight());
        this.price = price;

        Font font = new Font(FONT_FILE, FONT_SIZE);
        double width = font.getWidth(CURRENCY_SYMBOL + (int)price); // get width of text to centre it

        cost = new Text(font, CURRENCY_SYMBOL + (int)price, x - width / 2, y + Y_OFFSET);
        btnImage.draw(x, y);
    }

    /**
     * draws tower button and price
     */
    public void draw() {
        btnImage.draw(x, y);
        cost.draw();
    }

    /**
     * Updates if a tower is able to be purchased
     * @param money total money the player has
     */
    public void setPurchasable(double money) {
        if (money < price) {
            purchasable = false;
            cost.updateColour(Colour.RED);
        } else if (money >= price){
            purchasable = true;
            cost.updateColour(Colour.GREEN);
        }
    }


    public BoundingBox getBoundingBox() {
        return bb;
    }

    public String getTowerType() { return towerType; }

    public boolean isPurchasable() { return purchasable; }

}
