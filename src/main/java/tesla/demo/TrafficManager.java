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
    public static ArrayList<TrafficNode> trafficLights;
    private Map<TrafficNode, Deque<EV>> vehicleDeques;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Map<TrafficNode, Node> trafficNodeToCompletionNode;
    //private Map<String, EV> evMap;

    public TrafficManager(Map<String, EV> evMap) {
        trafficLights = new ArrayList<TrafficNode>();
        vehicleDeques = new HashMap<>();
        //this.evMap = evMap;
    }

    public void addTrafficNode(TrafficNode node) {
        trafficLights.add(node);
        vehicleDeques.put(node, new LinkedList<>());
    }

    public boolean canEVMove(EV ev) {
        //remove from deque if first EV reaches completion node
        //--------------------------------Section for adding evs to queue-----------------------------------------------
            TrafficNode currentNode = GameMap.getInstance().getTrafficNode(ev.getPath().get(ev.currentPathIndex).getX(),
                    ev.getPath().get(ev.currentPathIndex).getY()); 

            if (currentNode.type.equals("TrafficNode")) {
                if (!currentNode.isGreen() || !isDequeEmpty(currentNode)) {
                    addToDeque(currentNode, ev);
                    return false;
                }

                if(currentNode.isGreen()) {
                    trafficNodeToCompletionNode.put(currentNode, GameMap.getInstance().getTrafficNode(ev.getPath().get(ev.currentPathIndex + 3).getX(),ev.getPath().get(ev.currentPathIndex + 3).getY()));
                    //this EV is at first position in queue, so it can move to next node and is kept in queue until it crosses end of intersection which is 3 + trafficNodePathIndex
                    return true;
                    // //find the queue of this node
                    // Queue<EV> queue = vehicleQueues.get(currentNode);
                    //until first ev reaches compleition node, keep it in queue and all subsequent evs in queue cannot move, once it reaches completion node, remove first ev frmo queue
                    
                }
            } else {
                // next node is not TrafficNode
                // check last item item in queue, if the last item in queue is the next node of
                // ev, then we have to add ev to queue, because it cannot move
                // if the last item in queue is not the next node of ev, then we dont have to
                // add ev to queue, because it can move
                // first get the traffic node, then find its queue

                // step 1. getting traffic node of the ev
                int currentPathIndex = ev.currentPathIndex;
                for (int pathIndex = currentPathIndex; pathIndex < ev.getPath().size(); pathIndex++) {
                    PathNode position = ev.getPath().get(pathIndex);
                    Node trafficNode = GameMap.getInstance().getTrafficNode(position.getX(), position.getY());
                    if (trafficNode.type.equals("TrafficNode")) {
                        currentNode = (TrafficNode) trafficNode;
                        break;
                    }
                }
                // step 2. getting queue of the traffic node
                Deque<EV> Deque = vehicleDeques.get(currentNode);
                //check if the first ev in the Deque has reached completion or not
                //check if this is  EV in queue
                EV firstEV = Deque.pollFirst();
                EV secondEV = Deque.peekFirst();
                //add firstEV back to Dequeu start
                Deque.addFirst(firstEV);
                if(ev == secondEV && GameMap.getInstance().getRoadNode(firstEV.getPath().get(firstEV.currentPathIndex).getX(),firstEV.getPath().get(firstEV.currentPathIndex).getY()) != trafficNodeToCompletionNode.get(currentNode)) {
                    //addToDeque(currentNode, ev);
                    return false;
                }
                if(ev == secondEV && GameMap.getInstance().getRoadNode(firstEV.getPath().get(firstEV.currentPathIndex).getX(),firstEV.getPath().get(firstEV.currentPathIndex).getY()) == trafficNodeToCompletionNode.get(currentNode)) {
                    //addToDeque(currentNode, ev);
                    Deque.removeFirst();
                    trafficNodeToCompletionNode.put(currentNode, null); //reset completion node
                    return true;
                    //this will move the second EV to the trafficnode and set the completion node accordingly
                }
                // step 3. checking if the last item in Deque is the next node of ev
                if (!Deque.isEmpty()) {
                    EV lastEV = Deque.peekLast();
                    PathNode lastEVInDequePosition = lastEV.getPath().get(lastEV.currentPathIndex);
                    PathNode nextEVPosition = ev.getPath().get(ev.currentPathIndex + 1);
                    if (lastEVInDequePosition == nextEVPosition) {
                        addToDeque(currentNode, ev);
                        return false;
                    }
                }
                return true;
            }


            return true;
    }

    // private TrafficNode getCurrentTrafficNode(PathNode position) {
    // for(TrafficNode node : trafficLights) {
    // if(node.isAtIntersection(position.getX(), position.getY())) {
    // return node;
    // }
    // }
    // return null;
    // }

    private void addToDeque(TrafficNode node, EV ev) {
        vehicleDeques.get(node).offer(ev);
    }

    private boolean isDequeEmpty(TrafficNode node) {
        return vehicleDeques.get(node).isEmpty();
    }

    // public void processDeques() {
    //     for (TrafficNode node : trafficLights) {
    //         if (node.isGreen()) {
    //             Deque<EV> Deque = vehicleDeques.get(node);
    //             while (!Deque.isEmpty() && node.canMove()) {
    //                 EV ev = Deque.poll();
    //                 ev.moveToNextPosition();
    //             }
    //         }
    //     }
    // }

    // public void changeSignals() {
    //     for (int i = 0; i < trafficLights.size(); i++) {
    //         trafficLights.get(i).changeSignal();
    //     }
    //     processDeques();
    // }

    // public void startTrafficCycle() {
    //     scheduler.scheduleAtFixedRate(() -> {
    //         changeSignals();
    //     }, 0, 5, TimeUnit.SECONDS);
    // }

    // Handle random priority for simultaneous arrivals
    // private void handleSimultaneousArrivals(TrafficNode node) {
    //     Deque<EV> Deque = vehicleDeques.get(node);
    //     List<EV> sameTimeArrivals = new ArrayList<>();

    //     // Group EVs that arrived at the same time
    //     while (!Deque.isEmpty()) {
    //         sameTimeArrivals.add(Deque.poll());
    //     }

    //     // Shuffle for random priority
    //     Collections.shuffle(sameTimeArrivals);

    //     // Add back to Deque in random order
    //     for (EV ev : sameTimeArrivals) {
    //         Deque.offer(ev);
    //     }
    // }

    public void shutdown() {
        scheduler.shutdown();
    }
}
