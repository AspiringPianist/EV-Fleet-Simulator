package tesla.demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class Map {
    private static Map instance;
    private List<List<GameObject>> grid;
    private int width;
    private int height;

    public Map() {
        loadMapFromCSV("src/main/java/tesla/demo/map.csv");
    }

    private void loadMapFromCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            // Set dimensions from CSV
            this.height = lines.size();
            this.width = lines.get(0).split(",").length;

            // Initialize grid with determined size
            grid = new ArrayList<>(height);
            for (int i = 0; i < height; i++) {
                grid.add(new ArrayList<>(Collections.nCopies(width, null)));
            }

            // Load obstacles and verify each cell
            for (int y = 0; y < lines.size(); y++) {
                String[] values = lines.get(y).split(",");
                for (int x = 0; x < values.length; x++) {
                    if (values[x].trim().equals("-1")) {
                        setObject(x, y, new Obstacle(x, y));
                        // Verify obstacle placement
                        if (!isWalkable(x, y)) {
                            System.out.println("Obstacle placed at: " + x + "," + y);
                        }
                    } else {
                        setObject(x, y, new GameObject(x, y));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map getInstance() {
        if (instance == null) {
            instance = new Map();
        }
        return instance;
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
                System.out.print((object != null ? object.symbol : "[.]") + " ");
            }
            System.out.println();
        }
    }
}
