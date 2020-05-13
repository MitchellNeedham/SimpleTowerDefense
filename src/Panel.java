import bagel.Font;
import bagel.Image;
import bagel.Input;

import java.util.HashMap;
import java.util.Map;

public class Panel {

    private final int xPos;
    private final int yPos;
    private final Image backgroundImg;
    private final Map<String, int[]> text = new HashMap<>();
    private final Map<Clickable, String> clickable = new HashMap<>();
    private static final String FONT_FILE = "res/fonts/DejaVuSans-Bold.ttf";
    private final Map<String, Font> fonts = new HashMap<>();

    // TODO: improve panel using OOP

    /**
     * Constructor for panel
     * @param x x-coordinate at top left of panel
     * @param y y-coordinate at top left of panel
     * @param filePath background image of panel
     */
    protected Panel(int x, int y, String filePath) {
        this.xPos = x;
        this.yPos = y;
        this.backgroundImg = new Image(filePath);

        // initialise fonts
        // TODO: improve management of fonts
        fonts.put("small", new Font(FONT_FILE, 16));
        fonts.put("medium", new Font(FONT_FILE, 20));
        fonts.put("large", new Font(FONT_FILE, 48));
    }

    /**
     * Updates panel
     */
    protected void update() {

        // draw background image
        backgroundImg.drawFromTopLeft(xPos, yPos);

        // draw each text object in text map
        text.forEach((t, pos) -> {
            String[] textSplit = t.split(">");
            fonts.get(textSplit[0]).drawString(textSplit[1], pos[0], pos[1]);
        });

        // draw clickable objects and any text
        clickable.forEach((c, txt) -> {
            double[] pos = c.getPos();
            String[] textSplit = txt.split(">");
            double textWidth = fonts.get(textSplit[0]).getWidth(textSplit[1]);
            c.draw();
            fonts.get(textSplit[0]).drawString(textSplit[1], pos[0] - textWidth/2, pos[1] + 45);
        });
    }

    /**
     * Add clickable object to panel
     * @param object Clickable object
     * @param textContent text to be displayed underneath
     */
    protected void addClickable(Clickable object, String textContent) {
        clickable.put(object, textContent);
    }

    /**
     * Add text to panel
     * @param textContent String containing text
     * @param x centre x-coordinate of text
     * @param y centre y-coordinate of text
     */
    protected void addText(String textContent, int x, int y) {
        text.put(textContent, new int[]{x, y});
    }

    /**
     * Updates text (to be redesigned)
     * @param oldText String containing current text
     * @param newText String containing updated text
     */
    public void updateText(String oldText, String newText) {
        int[] pos = text.get(oldText);
        text.put(newText, pos);
        text.remove(oldText);
    }

}
