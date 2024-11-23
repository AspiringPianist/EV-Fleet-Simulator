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
    public static ArrayList<TrafficNode> trafficLights  = new ArrayList<TrafficNode>();
    // private  Map<TrafficNode, Deque<EV>> vehicleDeques = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Map<TrafficNode, Node> trafficNodeToCompletionNode;
    //private Map<String, EV> evMap;

    public TrafficManager() {
        //trafficLights = new ArrayList<TrafficNode>();
        // vehicleDeques = new HashMap<>();
        //this.evMap = evMap;
    }

    public static TrafficManager getInstance() {
        if (instance == null) {
            instance = new TrafficManager();
        }
        return instance;
    }

    public void addTrafficNode(TrafficNode node) {
        trafficLights.add(node);
        // vehicleDeques.put(node, new LinkedList<>());
        // node.addEv();
    }

    public boolean canEVMove(EV ev) {
        // System.out.println("I AM DYING");
        //remove from deque if first EV reaches completion node
        //--------------------------------Section for adding evs to queue-----------------------------------------------
            Node currentNode = GameMap.getInstance().getTrafficNode(ev.getPath().get(ev.currentPathIndex).getX(),
                    ev.getPath().get(ev.currentPathIndex).getY()); 
            //this means we go to else case
            System.out.println(currentNode.type);
            if (currentNode.type.equals("TrafficNode") ) {
                System.out.println("ENTERED WHAT IS THIS FUCK OFF");
                TrafficNode trafficNode = (TrafficNode)currentNode;
                if (!trafficNode.isGreen() || !trafficNode.getDeque().isEmpty()) {
                    trafficNode.addToDeque(ev);
                    return false;
                }
        
                if(trafficNode.isGreen()) {
                    Node completionNode = GameMap.getInstance().getTrafficNode(ev.getPath().get(ev.currentPathIndex + 3).getX(),
                            ev.getPath().get(ev.currentPathIndex + 3).getY());
                    trafficNodeToCompletionNode.put(trafficNode, completionNode);
                    return false;
                }
            }else {
                int currentPathIndex = ev.currentPathIndex;
                for (int pathIndex = currentPathIndex; pathIndex < ev.getPath().size(); pathIndex++) {
                    PathNode position = ev.getPath().get(pathIndex);
                    Node trafficNode = GameMap.getInstance().getTrafficNode(position.getX(), position.getY());
                    if (trafficNode instanceof TrafficNode) {
                        currentNode = trafficNode;
                        break;
                    }
                }
                
                if (currentNode instanceof TrafficNode) {
                    TrafficNode trafficNode = (TrafficNode)currentNode;
                    // Deque<EV> Deque = vehicleDeques.get(trafficNode);
                    Deque<EV> Deque=trafficNode.getDeque();
                    // if(Deque!=null && !Deque.isEmpty()) {
                    if(!Deque.isEmpty()) {
                        EV firstEV = Deque.pollFirst();
                        EV secondEV = Deque.peekFirst();
                        Deque.addFirst(firstEV);
                        
                        if(ev == secondEV && GameMap.getInstance().getRoadNode(firstEV.getPath().get(firstEV.currentPathIndex).getX(),
                                firstEV.getPath().get(firstEV.currentPathIndex).getY()) != trafficNodeToCompletionNode.get(trafficNode)) {
                            return false;
                        }
                        
                        if(ev == secondEV && GameMap.getInstance().getRoadNode(firstEV.getPath().get(firstEV.currentPathIndex).getX(),
                                firstEV.getPath().get(firstEV.currentPathIndex).getY()) == trafficNodeToCompletionNode.get(trafficNode)) {
                            Deque.removeFirst();
                            trafficNodeToCompletionNode.put(trafficNode, null);
                            return false;
                        }
                    }
                    
                    // if (Deque!=null && !Deque.isEmpty()) {
                    if (!Deque.isEmpty()) {
                        EV lastEV = Deque.peekLast();
                        PathNode lastEVInDequePosition = lastEV.getPath().get(lastEV.currentPathIndex);
                        PathNode nextEVPosition = ev.getPath().get(ev.currentPathIndex + 1);
                        if (lastEVInDequePosition == nextEVPosition) {
                            // addToDeque(trafficNode, ev);
                            trafficNode.addToDeque(ev);
                            return false;
                        }
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
/*
    private void addToDeque(TrafficNode node, EV ev) {
        vehicleDeques.get(node).offer(ev);
    }

    private boolean isDequeEmpty(TrafficNode node) {
        return vehicleDeques.get(node).isEmpty();
    }*/

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

    public void startTrafficCycle() {
        scheduler.scheduleAtFixedRate(() -> {
            changeSignals();
        }, 0, 10, TimeUnit.SECONDS);
    }
    
    public void changeSignals() {
        for (TrafficNode node : trafficLights) {
            node.changeSignal();
        }
    }
    

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
