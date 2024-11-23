package tesla.demo;
import java.util.*;

public class TrafficNode extends Node{
    
    private int signal;
    public TrafficNode(int x,int y,String type){
        super(x,y,type);
        signal=1;
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
}