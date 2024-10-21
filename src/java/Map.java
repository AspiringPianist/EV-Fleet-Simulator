import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class Map {
    // map is a 2d grid of game objects of certain specified x, y size (usually
    // square only)

    private List<List<GameObject>> grid;
    private int width;
    private int height;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        grid = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            grid.add(new ArrayList<>(Collections.nCopies(width, null)));
        }
    }

    public GameObject getObject(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        return grid.get(y).get(x);
    }

    public void setObject(int x, int y, GameObject object) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid.get(y).set(x, object);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isWalkable(int x, int y) {
        GameObject object = getObject(x, y);
        return object == null || !(object instanceof Obstacle);
    }

    public boolean[][] getObstacleMap() {
        boolean[][] obstacleMap = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                obstacleMap[y][x] = !isWalkable(x, y);
            }
        }
        return obstacleMap;
    }

    public void printMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                GameObject object = getObject(x, y);
                System.out.print(object.symbol + " ");
            }
            System.out.println();
        }
    }

}