// package tesla.demo;
// import java.io.*;
// import java.util.*;
// public class TrafficManager {
//     public static ArrayList<TrafficNode> trafficLights; //array of pairs of traffic
//     public TrafficManager(Map<String,EV> evMap){ 
//         trafficLights=new ArrayList<TrafficNode>();
//     }

//     public void addTrafficNode(TrafficNode node){
//         trafficLights.add(node);
//     }

//     public void changeSignals(){
//         for(int i=0;i<trafficLights.size();i++){
//             trafficLights.get(i).changeSignal();
//         }
//     }

//     //check all evs and check if they are in the traffic light range...and also create 
//     //for each traffic road create a vehicle queue and add evs to the queue.
//     //make them move based on priority of time of arrival
//     // if two EVs are arriving at same time, then choose at random which has earlier time
//     // integrate this with main.js (phaser) move function for each ev
//     //which will check if it is possible to move the vehicle based on traffic condition

//     //the traffic node signals at 4 way intersections are in 2 groups, each group has a pair which are in the same signal state
//     // a vehicle cannot go towards the red signal things
//     //there is a vehicle queue for each 8 roads at the intersection
// }

package tesla.demo;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class TrafficManager {
    public static TrafficManager instance;
    private static final long SIGNAL_CHANGE_INTERVAL = 5000; // 5 seconds in milliseconds
    public static ArrayList<TrafficNode> trafficLights = new ArrayList<TrafficNode>();
    // private Map<TrafficNode, Deque<EV>> vehicleDeques = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Map<TrafficNode, Node> trafficNodeToCompletionNode;
    private static long nextSignalChangeTime;

    //GameMap gameMap = GameMap.getInstance();

    // private Map<String, EV> evMap;

    public TrafficManager() {
        // trafficLights = new ArrayList<TrafficNode>();
        // vehicleDeques = new HashMap<>();
        // this.evMap = evMap;
        trafficNodeToCompletionNode = new HashMap<>(); // Or initialize in constructor

    }

    public static TrafficManager getInstance() {
        if (instance == null) {
            instance = new TrafficManager();
            instance.startTrafficCycle(); // Start the traffic cycle immediately
        }
        return instance;
    }

    public void addTrafficNode(TrafficNode node) {
        trafficLights.add(node);
        // vehicleDeques.put(node, new LinkedList<>());
        // node.addEv();
    }

    public void updateSignals() {
        trafficLights.forEach(TrafficNode::changeSignal);
    }

    // public boolean canMoveToPosition(EV ev, int targetX, int targetY) {
    // TrafficNode currentTrafficNode =
    // GameMap.getInstance().getTrafficNode(targetX, targetY);

    // if (currentTrafficNode != null) {
    // if (!currentTrafficNode.isGreen() ||
    // !currentTrafficNode.getDeque().isEmpty()) {
    // currentTrafficNode.addToDeque(ev);
    // return false;
    // }

    // Node completionNode = GameMap.getInstance().getTrafficNode(
    // ev.getPath().get(ev.currentPathIndex + 3).getX(),
    // ev.getPath().get(ev.currentPathIndex + 3).getY()
    // );
    // trafficNodeToCompletionNode.put(currentTrafficNode, completionNode);
    // return true;
    // }

    // int currentPathIndex = ev.currentPathIndex;
    // for (int pathIndex = currentPathIndex; pathIndex < ev.getPath().size();
    // pathIndex++) {
    // PathNode position = ev.getPath().get(pathIndex);
    // TrafficNode trafficNode =
    // GameMap.getInstance().getTrafficNode(position.getX(), position.getY());
    // if (trafficNode != null) {
    // currentTrafficNode = trafficNode;
    // break;
    // }
    // }

    // if (currentTrafficNode != null) {
    // Deque<EV> deque = currentTrafficNode.getDeque();
    // if (!deque.isEmpty()) {
    // EV firstEV = deque.pollFirst();
    // EV secondEV = deque.peekFirst();
    // deque.addFirst(firstEV);

    // if (ev == secondEV &&
    // GameMap.getInstance().getRoadNode(firstEV.getPath().get(firstEV.currentPathIndex).getX(),
    // firstEV.getPath().get(firstEV.currentPathIndex).getY()) !=
    // trafficNodeToCompletionNode.get(currentTrafficNode)) {
    // return false;
    // }

    // if (ev == secondEV &&
    // GameMap.getInstance().getRoadNode(firstEV.getPath().get(firstEV.currentPathIndex).getX(),
    // firstEV.getPath().get(firstEV.currentPathIndex).getY()) ==
    // trafficNodeToCompletionNode.get(currentTrafficNode)) {
    // deque.removeFirst();
    // trafficNodeToCompletionNode.put(currentTrafficNode, null);
    // return true;
    // }
    // }

    // if (!deque.isEmpty()) {
    // EV lastEV = deque.peekLast();
    // PathNode lastEVInDequePosition =
    // lastEV.getPath().get(lastEV.currentPathIndex);
    // PathNode nextEVPosition = ev.getPath().get(ev.currentPathIndex + 1);
    // if (lastEVInDequePosition.equals(nextEVPosition)) {
    // currentTrafficNode.addToDeque(ev);
    // return false;
    // }
    // }
    // }
    // return true;
    // }
    

    public boolean canMoveToPosition(EV ev, int targetX, int targetY) {
        // Get current position
        PathNode currentPos = ev.getPath().get(ev.currentPathIndex);
        // if (ev.currentPathIndex == ev.getPath().size() - 1) {
        //     return true;
        // }
        // Check if current position is at a traffic node
        TrafficNode currentTrafficNode = GameMap.getInstance().getTrafficNode(
                currentPos.getX(),
                currentPos.getY());
        if (currentTrafficNode == null) {
            //System.out.println("sdkgbwrjbfwijcb");
            // not at traffic node, atleast check if ev is there ahead
            //System.out.println(GameMap.getInstance().getRoadNode(ev.getPath().get(ev.currentPathIndex).getX(),
            //ev.getPath().get(ev.currentPathIndex).getY()));
            if (GameMap.getInstance().getRoadNode(ev.getPath().get(ev.currentPathIndex + 1).getX(),
                    ev.getPath().get(ev.currentPathIndex + 1).getY()).isStalled()) {
                //System.out.println("Stalled");
                return false;
            } else {
                //System.out.println("Not stalled");
                GameMap.getInstance().getRoadNode(ev.getCurrentX(), ev.getCurrentY()).setStalled(false);//mark current position as unstalled
                GameMap.getInstance().getRoadNode(ev.getPath().get(ev.currentPathIndex + 1).getX(),
                    ev.getPath().get(ev.currentPathIndex + 1).getY()).setStalled(true); //mark next position ev is moving to as stalled
                return true;
            }
        }
        // Check if target position is at a traffic node
        TrafficNode targetTrafficNode = GameMap.getInstance().getTrafficNode(targetX, targetY);
        // Case 1: Moving to a traffic node
        if (targetTrafficNode != null) {
            if (!targetTrafficNode.isGreen()) {
                // targetTrafficNode.addToDeque(ev);
                return false;
            }
        }

        // Case 2: Currently at a traffic node
        if (currentTrafficNode != null) {
            if (!currentTrafficNode.isGreen()) {
                return false;
            }
            // if (!currentTrafficNode.isGreen() ||
            // !currentTrafficNode.getDeque().isEmpty()) {
            // return false;
            // }
        }

        // Case 3: Check if path between current and target crosses any red lights
        // int pathDistance = Math.abs(targetX - currentPos.getX()) + Math.abs(targetY - currentPos.getY());
        // if (pathDistance > 1) {
        //     for (int i = ev.currentPathIndex; i < ev.getPath().size(); i++) {
        //         PathNode checkPos = ev.getPath().get(i);
        //         TrafficNode checkNode = GameMap.getInstance().getTrafficNode(
        //                 checkPos.getX(),
        //                 checkPos.getY());
        //         if (checkNode != null && !checkNode.isGreen()) {
        //             // return handleUpcomingTrafficNode(targetTrafficNode, ev);
        //             return false;
        //         }
        //     }
        // }
        GameMap.getInstance().getRoadNode(ev.getCurrentX(), ev.getCurrentY()).setStalled(false);//mark current position as unstalled
        GameMap.getInstance().getRoadNode(ev.getPath().get(ev.currentPathIndex + 1).getX(),
            ev.getPath().get(ev.currentPathIndex + 1).getY()).setStalled(true); //mark next position ev is moving to as stalled
        return true;
    }

    // private boolean handleUpcomingTrafficNode(TrafficNode trafficNode, EV ev) {
    // Deque<EV> deque = trafficNode.getDeque();
    // if (!deque.isEmpty()) {
    // EV firstEV = deque.peekFirst();
    // if (firstEV != null && trafficNodeToCompletionNode.get(trafficNode) != null)
    // {
    // Node currentFirstEVNode = GameMap.getInstance().getRoadNode(
    // firstEV.getPath().get(firstEV.currentPathIndex).getX(),
    // firstEV.getPath().get(firstEV.currentPathIndex).getY());
    // if (currentFirstEVNode == trafficNodeToCompletionNode.get(trafficNode)) {
    // deque.removeFirst();
    // trafficNodeToCompletionNode.remove(trafficNode);
    // }
    // }
    // return false;
    // }
    // return true;
    // }

    // private TrafficNode getCurrentTrafficNode(PathNode position) {
    // for(TrafficNode node : trafficLights) {
    // if(node.isAtIntersection(position.getX(), position.getY())) {
    // return node;
    // }
    // }
    // return null;
    // }
    /*
     * private void addToDeque(TrafficNode node, EV ev) {
     * vehicleDeques.get(node).offer(ev);
     * }
     * 
     * private boolean isDequeEmpty(TrafficNode node) {
     * return vehicleDeques.get(node).isEmpty();
     * }
     */

    // public void processDeques() {
    // for (TrafficNode node : trafficLights) {
    // if (node.isGreen()) {
    // Deque<EV> Deque = vehicleDeques.get(node);
    // while (!Deque.isEmpty() && node.canMove()) {
    // EV ev = Deque.poll();
    // ev.moveToNextPosition();
    // }
    // }
    // }
    // }

    public void startTrafficCycle() {
        scheduler.scheduleAtFixedRate(() -> {
            changeSignals();
        }, 0, SIGNAL_CHANGE_INTERVAL, TimeUnit.MILLISECONDS); // Changed to 3 seconds

    }

    public static void changeSignals() {
        long currentTime = System.currentTimeMillis();
        nextSignalChangeTime = currentTime + SIGNAL_CHANGE_INTERVAL;
        for (TrafficNode node : trafficLights) {
            node.changeSignal();
            // System.out.println("Signal change at " + currentTime + ": (" + node.x + "," +
            // node.y +
            // "): " + (node.isGreen() ? "GREEN" : "RED"));
        }
    }

    // private void processQueuedEVs(TrafficNode node) {
    // Deque<EV> queue = node.getDeque();
    // while (!queue.isEmpty()) {
    // EV ev = queue.peek();
    // PathNode nextPos = ev.getPath().get(ev.currentPathIndex + 1);
    // if (canMoveToPosition(ev, nextPos.getX(), nextPos.getY())) {
    // queue.poll(); // Remove EV from queue if it can move
    // } else {
    // break; // Stop processing if an EV can't move
    // }
    // }
    // }

    // Handle random priority for simultaneous arrivals
    // private void handleSimultaneousArrivals(TrafficNode node) {
    // Deque<EV> Deque = vehicleDeques.get(node);
    // List<EV> sameTimeArrivals = new ArrayList<>();

    // // Group EVs that arrived at the same time
    // while (!Deque.isEmpty()) {
    // sameTimeArrivals.add(Deque.poll());
    // }

    // // Shuffle for random priority
    // Collections.shuffle(sameTimeArrivals);

    // // Add back to Deque in random order
    // for (EV ev : sameTimeArrivals) {
    // Deque.offer(ev);
    // }
    // }

    public void shutdown() {
        scheduler.shutdown();
    }
}
