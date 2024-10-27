package tesla.demo;

import java.util.Scanner;

public class PathfindingVisualizer {
    static {
        System.setProperty("java.library.path", "C:/Users/Unnath Ch/Desktop/EV-Fleet-Simulator/");
        System.loadLibrary("astar_jni");
    }

    private native long[] findPath(int startX, int startY, int endX, int endY, boolean[][] obstacleMap);

    private native long[][] findPathWithSteps(int startX, int startY, int endX, int endY, boolean[][] obstacleMap);

    private Map map;

    public PathfindingVisualizer(Map map) {
        this.map = map;
    }

    public void runPathfindingSimulation() {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Enter start point (x y):");
            int startX = scanner.nextInt();
            int startY = scanner.nextInt();

            System.out.println("Enter end point (x y):");
            int endX = scanner.nextInt();
            int endY = scanner.nextInt();

            // Validate start and end points
            if (!map.isWalkable(startX, startY)) {
                System.out.println("Start point is inside an obstacle!");
                return;
            }
            if (!map.isWalkable(endX, endY)) {
                System.out.println("End point is inside an obstacle!");
                return;
            }

            boolean[][] obstacleMap = map.getObstacleMap();
            long[] path = findPath(startX, startY, endX, endY, obstacleMap);

            if (path == null || path.length == 0) {
                System.out.println("No valid path found!");
                return;
            }

            visualizePath(path);
        } finally {
            scanner.close();
        }
    }

    private void visualizePath(long[] path) {
        // Check if path exists
        if (path == null || path.length == 0) {
            System.out.println("No valid path found!");
            return;
        }

        String[][] visualMap = new String[map.getHeight()][map.getWidth()];

        // Initialize the map
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                visualMap[y][x] = map.isWalkable(x, y) ? "[_] " : "[X] ";
            }
        }

        // Mark the path
        for (int i = 0; i < path.length - 1; i += 2) {
            int x = (int) path[i];
            int y = (int) path[i + 1];
            visualMap[y][x] = "[P] ";
        }

        // Mark start and end points if path exists
        if (path.length >= 4) {
            visualMap[(int) path[1]][(int) path[0]] = "[S] ";
            visualMap[(int) path[path.length - 1]][(int) path[path.length - 2]] = "[G] ";
        }

        // Print the map
        for (String[] row : visualMap) {
            for (String cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }

    public void visualizePathfindingStepByStep() {
        // ... (get start and end points)
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Enter start point (x y):");
            int startX = scanner.nextInt();
            int startY = scanner.nextInt();

            System.out.println("Enter end point (x y):");
            int endX = scanner.nextInt();
            int endY = scanner.nextInt();

            long[][] steps = findPathWithSteps(startX, startY, endX, endY, map.getObstacleMap());

            for (int i = 0; i < steps.length; i++) {
                System.out.println("Step " + (i + 1) + ":");
                visualizePath(steps[i]);
                try {
                    Thread.sleep(500); // Pause between steps
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            scanner.close();
        }
    }
}
