package tesla.demo;
// import java.util.*;
import java.util.*;
public class TrafficNode extends Node{
    // static ArrayList<ArrayList<Node>> trafficLights; //array of pairs of traffic
    private int signal;
    public int group;
    //public Deque<EV> trafficNodeDeque;
    public TrafficNode(int x,int y,String type,int trafficType){
        super(x,y,type);
        this.signal=trafficType;
        //trafficNodeDeque=new LinkedList<>();
    }
    //green --1
    //red --0
    public void changeSignal(){
        // signal=!signal;
       signal= (signal+1)%4;
    }
    // public void addToDeque(EV ev){
    //     trafficNodeDeque.add(ev);
    // }
    // public Deque<EV> getDeque(){
    //     return this.trafficNodeDeque;
    // }
    public boolean isGreen() {
        // return this.signal ==1;
        return this.signal==0;
    }
    public boolean isRed() {
        return this.signal!=0;
    }
    
    public TrafficNode get_pair(TrafficNode currentNode) {
        Node pair_node;
        List<int[]> coordinates = new ArrayList<>();
        coordinates.add(new int[]{1, 3});
        coordinates.add(new int[]{-3, 1});
        coordinates.add(new int[]{-1, -3});
        coordinates.add(new int[]{3, -1});
        
        for(int[] coord : coordinates) {
            pair_node = GameMap.getInstance().getTrafficNode(x + coord[0], y + coord[1]);
            if (pair_node instanceof TrafficNode && 
                ((TrafficNode)pair_node).type.equals("TrafficNode") && 
                ((TrafficNode)pair_node).signal == currentNode.signal) {
                return (TrafficNode)pair_node;
            }
        }
        return null;
    }
    
}