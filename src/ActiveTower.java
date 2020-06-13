import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

import java.io.FileNotFoundException;
import java.util.*;

import java.io.File;

public abstract class ActiveTower implements Tower, Clickable {

    //-------------------------RENDER PRIORITIES-------------------------//

    private final static int BASE_Z = 4;
    private final static int TOP_Z = 5;
    private final static int OVERLAY_Z = 6;
    private final static int UNDERLAY_Z = 3;


    //-------------------------IMAGE FILE LOCATION AND FORMAT-------------------------//

    private static final String RES_PATH = "res/images/"; // image(s) path
    private static final String IMAGE_EXT = ".png"; // image extension


    //-------------------------TOWER STATE COLOURS-------------------------//

    private static final Colour RANGE_COLOUR = new Colour(0, 200, 0, 0.2); // colour of range radius
    private static final Colour HOVER_COLOUR = new Colour(255, 255, 255, 0.2); // colour of hover overlay
    private static final Colour BLOCKED_COLOUR = new Colour(255, 0, 0, 0.5); // colour of blocked overlay


    //-------------------------UPGRADE PANEL-------------------------//

    private static final String UPGRADE_PANEL = "res/images/panels/upgradepanel.png";
    private static final Point UPGRADE_PANEL_POS = new Point(Window.getWidth()-200f, 100);
    private Panel upgradePanel;

    //-------------------------TOWER FILES AND DATA-------------------------//
    //TODO: implement upgrades
    // all of these will all be editable when upgrades are implemented
    private Point towerPos;
    private Point towerTopPos;
    private final String type;
    private String tower;
    private String towerBase;
    private String towerTop;
    private String projectileImage;
    private double projectileSpeed;
    private double projectileDamage;
    private double range;
    private double fireRate;
    private double cost;
    private double turretAngle;
    private double bodyAngle;
    private int[] streams = {0, 0};

    private boolean sold = false;

    private boolean clicked = false; // has tower been clicked?
    private boolean placing; // is tower active or placing?
    private Time time; //TODO: fix all times to be instance variables

    private boolean blocked = false; // is tower over a blocked position?

    private int attackPattern;
    private ArrayList<TowerButton> upgradeButtons = new ArrayList<>();

    private final static double GUN_SPACING = 20;
    private final static double MAX_GUN_WIDTH = 20;



    /**
     * Constructor for Tank
     * @param x x-coordinate at centre of tank position
     * @param y y-coordinate at centre of tank position
     */
    public ActiveTower(double x,
                       double y,
                       String type,
                       double projectileSpeed,
                       double projectileDamage,
                       double range,
                       double fireRate,
                       double cost) {
        this.towerPos = new Point(x, y);
        this.towerTopPos = new Point(x, y);
        this.type = type;
        this.tower = RES_PATH + type + "/main" +IMAGE_EXT;
        this.towerBase = RES_PATH + type + "/bottom" + IMAGE_EXT;
        this.towerTop = RES_PATH + type + "/top" + IMAGE_EXT;
        this.projectileImage = RES_PATH + type + "/projectile" + IMAGE_EXT;
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
        this.range = range;
        this.fireRate = fireRate;
        this.cost = cost;

        this.placing = true;
        turretAngle = new Random().nextDouble() * Math.PI * 2;
        bodyAngle = turretAngle;
        createUpgradePanel();
        upgrade(0,0);


    }

    public ActiveTower(double x,
                       double y,
                       String type,
                       double angle) {
        this.towerPos = new Point(x, y);
        this.towerTopPos = new Point(x, y);
        this.type = type;
        this.tower = RES_PATH + type + "/main" +IMAGE_EXT;
        this.towerTop = RES_PATH + type + "/main" +IMAGE_EXT;
        this.towerBase = RES_PATH + type + "/runway" +IMAGE_EXT;
        this.projectileImage = RES_PATH + type + "/projectile" + IMAGE_EXT;

        this.placing = true;
        turretAngle = angle;
        bodyAngle = turretAngle;
        createUpgradePanel();
        upgrade(0,0);


    }

    /**
     * Update tank position and turret rotation
     * @param input user defined input
     * @param timeScale game speed multiplier
     */
    @Override
    public void update(Input input, float timeScale) {
        // show upgrade panel when clicked
        if (clicked) {
            upgradePanel.update();
        }

        // determines what to do when clicked on or hovered over
        // TODO: fix these?
        click(input);
        hover(input);

        //
        if (placing) {
            towerPos = new Point(input.getMouseX(), input.getMouseY());
            towerTopPos = towerPos;
        } else {
            time.updateTime(timeScale);
            draw(timeScale);
        }
    }

    /**
     * place tower at x and y position and start tower functioning
     * @param x x-coordinate to place tower at
     * @param y y-coordinate to place tower at
     */
    @Override
    public void place(double x, double y) {
        placing = false;
        time = new Time();
        this.towerPos = new Point(x, y);
        this.towerTopPos = new Point(x, y);
    }

    /**
     * Creates projectile targeted at enemy
     * @param target enemy that tower is pointing at
     * @return Projectile launched at enemy
     */
    @Override
    public List<Projectile> fire(Enemy target) {
        if (target == null) { return Collections.emptyList(); }
        ArrayList<Projectile> projectilesFired = new ArrayList<>();
        time = new Time();
        double standardCount = 0;
        double directCount = 0;

        switch (attackPattern) {
            case 0:
                standardCount = 1;
                break;
            case 1:
                directCount = 2;
                break;
            case 2:
                standardCount = 2;
                break;
            case 3:
                standardCount = 4;
                break;
            case 4:
                directCount = 3;
                break;
            case 5:
                directCount = 2;
                standardCount = 2;
                break;
            case 6:
                standardCount = 8;
                break;
        }

        for (int i = 0; i < directCount; i++) {
            Vector2 path = new Vector2(Math.cos(turretAngle-Math.PI/2), Math.sin(turretAngle-Math.PI/2)).normalised();
            Vector2 firePos = getTowerTopPosition().asVector().add(new Vector2(path.y, -path.x).normalised().mul(MAX_GUN_WIDTH/2 - MAX_GUN_WIDTH*i/(directCount-1)));
            double v = -GUN_SPACING * Math.abs(i - (directCount - 1) / 2);
            Vector2 spacing = path.normalised().mul(v);

            if (directCount > 2) {
                projectilesFired.add(new StandardProjectile(projectileImage, firePos.add(spacing).asPoint(), projectileSpeed, projectileDamage, target, path));
            } else {
                projectilesFired.add(new StandardProjectile(projectileImage, firePos.add(spacing).asPoint(), projectileSpeed, projectileDamage, path));
            }

        }

        for (int i = 0; i < standardCount; i++) {
            projectilesFired.add(new StandardProjectile(projectileImage, getTowerTopPosition(), projectileSpeed, projectileDamage, target));
        }
        return projectilesFired;
    }

    /**
     * TODO can this be in update?
     * @param input user defined input
     */
    @Override
    public void hover(Input input) {
        double p = 1;
        Point mousePos = new Point(input.getMouseX(), input.getMouseY());
        if (mousePos.distanceTo(towerPos) <= 25) {
            if (placing) {
                RenderQueue.addToQueue(BASE_Z, new RenderImage(towerPos.x, towerPos.y, towerBase));
                RenderQueue.addToQueue(TOP_Z, new RenderImage(towerTopPos.x, towerTopPos.y, towerTop));
                p = 0;
            }

            if (isBlocked()) {
                RenderQueue.addToQueue(UNDERLAY_Z, new Shape("circle",towerPos.x, towerPos.y, range, RANGE_COLOUR));
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, towerBase,
                        new DrawOptions().setRotation(bodyAngle * p).setBlendColour(200, 200, 200, 0.2)));
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerTopPos.x, towerTopPos.y, towerTop,
                        new DrawOptions().setRotation(turretAngle * p).setBlendColour(200, 200, 200, 0.2)));
            } else {
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, tower,
                        new DrawOptions().setBlendColour(BLOCKED_COLOUR)));
            }
        }
    }

    @Override
    public void click(Input input) {
        Point mousePos = new Point(input.getMouseX(), input.getMouseY());
        if (input.wasPressed(MouseButtons.LEFT) && blocked && !placing) {

            if (mousePos.distanceTo(towerPos) <= 22.5) {
                clicked = true;
            } else if (mousePos.distanceTo(towerPos) > 22.5 && !upgradePanel.getBoundingBox().isMouseOver(input)) {
                clicked = false;
            }
        }
    }

    public void draw() {

    }

    /**
     * checks blocked points and blocked lines to determine if tower can be placed.
     * @param blockedPoints list of points that tower can't be placed near
     * @param blockedLines list of lines that tower can't be placed on or near
     * @return boolean if tower can be placed
     */
    public boolean canBePlaced(List<Point> blockedPoints, List<Line> blockedLines) {
        for (Point p : blockedPoints) {
            if (p.distanceTo(towerPos) < 45) {
                return blocked = false;
            }
        }
        for (Line l : blockedLines) {
            if (l.DistanceToLine(towerPos) < 45) {
                return blocked = false;
            }
        }
        return blocked = true;
    }

    public void draw(float timeScale) {
        RenderQueue.addToQueue(BASE_Z, new RenderImage(towerPos.x, towerPos.y, towerBase,
                new DrawOptions().setRotation(bodyAngle)));
        RenderQueue.addToQueue(TOP_Z, new RenderImage(towerTopPos.x, towerTopPos.y, towerTop,
                new DrawOptions().setRotation(turretAngle)));
    }

    /**
     * creates and initialises upgrade panel for tower
     */
    private void createUpgradePanel() {
        upgradePanel = new Panel(UPGRADE_PANEL_POS.x, UPGRADE_PANEL_POS.y, UPGRADE_PANEL);
        String capitalisedType = type.substring(0, 1).toUpperCase() + type.substring(1);
        upgradePanel.addText("title", 100, 50, capitalisedType);
        updateButtons();

    }

    public void updateButtons() {
        System.out.println(upgradeButtons.size());
        upgradePanel.resetButtons();
        upgradeButtons = new ArrayList<>();
        String image = "";
        double cost = 0;

        for (int i = 0; i < streams.length; i++) {
            int upgradeStreams[] = {streams[0] + (int)Math.signum((float)streams[0]), streams[1] + (int)Math.signum((float)streams[1])};

            if (streams[0] == 0 && streams[1] == 0) {
                upgradeStreams[i]++;
            }


            System.out.printf("%d %d\n", streams[0], streams[1]);
            if (upgradeStreams[0] <= 3 && upgradeStreams[1] <= 3 && !(streams[i] == 0 && streams[(i+1)%2] > 0)  && streams[i] < 3) {
                try {
                    File fp = new File("res/upgrades/" + type + "/" + upgradeStreams[0] + "-" + upgradeStreams[1] + ".txt");
                    Scanner myReader = new Scanner(fp);
                    // read file
                    while (myReader.hasNextLine()) {
                        String[] upgradeData = myReader.nextLine().split(",");
                        if (upgradeData[0].equals("main_image")) {
                            image = upgradeData[1];
                            System.out.println(image);
                        } else if (upgradeData[0].equals("cost")) {
                            cost = Double.parseDouble(upgradeData[1]);
                        }
                    }
                    myReader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                image = "res/upgrades/noupgrades.png";
                cost = 0;
            }
            upgradeButtons.add(new TowerButton(type, new Image(image), UPGRADE_PANEL_POS.x + 100, UPGRADE_PANEL_POS.y + 150 + 150 * i, cost));

            upgradePanel.addClickable(upgradeButtons.get(i));
        }
    }


    public Point getPosition() { return towerPos; }

    @Override
    public double getRange() { return range; }

    @Override
    public void updateRotation(double turretAngle) { this.turretAngle = turretAngle; }

    public boolean isOffScreen() {
        return towerPos.x < 0 || towerPos.x > Window.getWidth() || towerPos.y < 0 || towerPos.y > Window.getHeight();
    }

    public boolean isReloaded() { return time.getTotalGameTime() > fireRate; }

    @Override
    public boolean isPlacing() { return placing; }


    public boolean isBlocked() { return blocked; }

    public void setTowerTopPos(Point pos) { towerTopPos = pos; }

    public Point getTowerTopPosition() { return towerTopPos; }

    public void upgrade(int stream0, int stream1) {
        try {
            File fp = new File("res/upgrades/" + type + "/" + stream0 + "-" + stream1 + ".txt");
            Scanner myReader = new Scanner(fp);
            while (myReader.hasNextLine()) {
                String[] upgradeData = myReader.nextLine().split(",");
                switch (upgradeData[0]) {
                    case "top_image":
                        towerTop = upgradeData[1];
                        break;
                    case "bottom_image":
                        towerBase = upgradeData[1];
                        break;
                    case "projectile_image":
                        projectileImage = upgradeData[1];
                        break;
                    case "range":
                        range = Double.parseDouble(upgradeData[1]);
                        break;
                    case "cost":
                        cost = Double.parseDouble(upgradeData[1]);
                        break;
                    case "damage":
                        projectileDamage = Double.parseDouble(upgradeData[1]);
                        break;
                    case "fire_rate":
                        fireRate = Double.parseDouble(upgradeData[1]);
                        break;
                    case "attack_pattern":
                        attackPattern = Integer.parseInt(upgradeData[1]);
                        break;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        streams = new int[]{stream0, stream1};
        updateButtons();
    }

    public int updateUpgradeButtons(Input input, int money) {
        int i = 0;
        if (clicked) {
            for (TowerButton tb : upgradeButtons) {
                tb.setPurchasable(money);
                if (input.wasPressed(MouseButtons.LEFT) && tb.getBoundingBox().isMouseOver(input) && tb.isPurchasable()) {
                    if (!(streams[i] == 0 && streams[(i+1)%2] > 0)  && streams[i] < 3) {
                        money -= tb.getPrice();
                        streams[i]++;
                        upgrade(streams[0], streams[1]);
                    }
                }
                i++;
            }
        }
        return money;
    }

    public void setStreams(int s0, int s1) {
        streams = new int[] {s0, s1};
    }

    public void resetTime() {
        time = new Time();
    }

    public Time getTime() {
        return time;
    }

    public void setFireRate(double fireRate) {
        this.fireRate = fireRate;
    }

    public boolean isSold() {
        return sold;
    }
}
