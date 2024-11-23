package tesla.demo;
// import java.util.*;
import java.util.*;
public class TrafficNode extends Node{
    // static ArrayList<ArrayList<Node>> trafficLights; //array of pairs of traffic
    private int signal;
    public int group;
    public TrafficNode(int x,int y,String type,int trafficType){
        super(x,y,type);
        this.signal=trafficType;
    }
    //green --1
    //red --0
    public void changeSignal(){
        // signal=!signal;
       if(signal==0){
            signal=1;
        }
        else{
            signal=0;
        }
    }
    public boolean isGreen() {
        return this.signal ==1;
    }
    public boolean isRed() {
        return !isGreen();
    }
    
    public TrafficNode get_pair(TrafficNode currentNode){
        TrafficNode pair_node;
        List<int[]>coordinates=new ArrayList<>();
        coordinates.add(new int[]{1, 3});
        coordinates.add(new int[]{-3, 1});
        coordinates.add(new int[]{-1, -3});
        coordinates.add(new int[]{3, -1});
        for(int[] coord:coordinates){
            // map.get('5,6') -> node
            pair_node=GameMap.getInstance().getTrafficNode(x+coord[0],y+coord[1]);  //currentNode.(x+coord[0]).(y+coord[1]);
            if(currentNode.type==pair_node.type && currentNode.type.equals("TrafficNode")){
                if(pair_node.signal==currentNode.signal){
                    return pair_node;
                }
            }
        }
        return null;
    }
}