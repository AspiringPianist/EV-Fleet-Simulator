package tesla.demo;

class Obstacle extends GameObject {
    Obstacle(int x, int y) {
        super(x, y);
        this.symbol = "[X]";
    }
}

class StationaryObstacle extends Obstacle {
    StationaryObstacle(int x, int y) {
        super(x, y);
    }

    public Object getDetails() {
        return null;
    }

}

class NPC extends Obstacle {
    NPC(int x, int y) {
        super(x, y);
        this.symbol = "[N]";
    }

    public Object getDetails() {
        return null;
    }
}