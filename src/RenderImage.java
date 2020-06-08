import bagel.DrawOptions;
import bagel.Image;

public class RenderImage extends Image {

    // intermediary class to store image until it is drawn by RenderQueue

    private final DrawOptions drawOptions;
    private final double x;
    private final double y;

    /**
     * Constructor for image without DrawOptions
     * @param x x-position to render image at
     * @param y y-position to render image add
     * @param filePath filePath of image
     */
    public RenderImage(double x, double y, String filePath) {
        this(x, y, filePath, new DrawOptions());
    }

    /**
     * Constructor for image with DrawOptions
     * @param x x-position to render image at
     * @param y y-position to render image at
     * @param filePath file path of image
     * @param drawOptions drawOptions to be applied to image
     */
    public RenderImage(double x, double y, String filePath, DrawOptions drawOptions) {
        super(filePath);
        this.drawOptions = drawOptions;
        this.x = x;
        this.y = y;
    }

    /**
     * render image
     */
    public void draw() {
        super.draw(x, y, drawOptions);
    }

}
