class Obstacles extends GameObject{
    Obstacles(int x,int y){
        super(x,y);
    }
}
class Stationary_obstacles extends Obstacles{
    Stationary_obstacles(int x,int y){
        super(x,y);
    }
    public Object getDetails(){
        return null;
    }

}
class Npc_movers extends Obstacles{
    Npc_movers(int x,int y){
        super(x,y);
    }
    public Object getDetails(){
        return null;
    }
}