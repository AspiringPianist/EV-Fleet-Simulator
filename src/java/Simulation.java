import java.util.Random;

public class Simulation {
    private Map map;

    public Simulation() {
        initializeMap();
    }

    private void initializeMap() {
        map = new Map(20, 20);
        addObstacles();
        addGameObjects();
    }

    private void addObstacles() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int x = random.nextInt(20);
            int y = random.nextInt(20);
            Obstacle obstacle = new Obstacle(x, y);
            map.setObject(x, y, obstacle);
        }
    }

    private void addGameObjects() {
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            int x = random.nextInt(20);
            int y = random.nextInt(20);
            GameObject object = new GameObject(x, y);
            map.setObject(x, y, object);
        }
    }

    public void displayMap() {
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                GameObject obj = map.getObject(x, y);
                if (obj != null) {
                    System.out.print(obj.symbol+ " ");
                } else {
                    System.out.print("[_] ");
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Simulation sim = new Simulation();
        sim.displayMap();
    }
}