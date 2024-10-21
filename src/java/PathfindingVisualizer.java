import java.util.Scanner;

public class PathfindingVisualizer {
    static {
        System.setProperty("java.library.path", "C:/Users/Unnath Ch/Desktop/EV-Fleet-Simulator/");
        System.loadLibrary("astar_jni");
    }

    private native long[] findPath(int startX, int startY, int endX, int endY, boolean[][] map);

    private native long[][] findPathWithSteps(int startX, int startY, int endX, int endY, boolean[][] map);


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

            boolean[][] obstacleMap = map.getObstacleMap();
            long[] path = findPath(startX, startY, endX, endY, obstacleMap);

            visualizePath(path);
        } finally {
            scanner.close();
        }
    }

    private void visualizePath(long[] path) {
        String[][] visualMap = new String[map.getHeight()][map.getWidth()];

        // Initialize the map
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                visualMap[y][x] = map.isWalkable(x, y) ? "[_] " : "[X] ";
            }
        }

        // Mark the path
        for (int i = 0; i < path.length; i += 2) {
            int x = (int) path[i];
            int y = (int) path[i + 1];
            visualMap[y][x] = "[P] ";
        }

        // Mark start and end points
        visualMap[(int) path[1]][(int) path[0]] = "[S] ";
        visualMap[(int) path[path.length - 1]][(int) path[path.length - 2]] = "[G] ";

        // Print the map
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                System.out.print(visualMap[y][x]);
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
