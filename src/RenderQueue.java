import java.util.*;

public class RenderQueue {

    // Renders images by order of priority or z Index
    // I implemented this so I could explicitly choose how the game objects were rendered
    // I particularly ran into issues rendering the planes as older planes would pass under newer towers

    // stores objects by priority
    private static final Map<Integer, ArrayList<Object>> Queue = new TreeMap<>();

    /**
     * Adds object to queue
     * @param z_index Z index of object to be rendered at
     * @param object object to be rendered
     */
    public static void addToQueue(int z_index, Object object) {
        // if no objects exists with z index, create the key and intialise array
        if (Queue.get(z_index) == null) {
            Queue.put(z_index, new ArrayList<>(Collections.singletonList(object)));
        } else {
            Queue.get(z_index).add(object);
        }
    }

    /**
     * render all objects in order of priority (z index)
     */
    public static void renderObjects() {

        // run through objects with each priority
        Queue.forEach((priority, objects) -> {

            // iterate through objects
            for(Iterator<Object> listIterator = objects.listIterator(); listIterator.hasNext(); ) {
                Object o = listIterator.next();
                // draw object
                if (o instanceof Panel) {
                    ((Panel) o).draw();
                }
                if (o instanceof RenderImage) {
                    ((RenderImage) o).draw();
                }
                if (o instanceof Shape) {
                    ((Shape) o).draw();
                }

                // remove object from queue
                listIterator.remove();
            }
        });
    }
}
