package tesla.demo;

import java.util.*;
public class TaskAssigner{
    private static TaskAssigner instance;
    public static Queue<Task> buffer;
    TaskAssigner(){
        buffer=new LinkedList<>();

    }

    public static TaskAssigner getInstance(){
        if(instance==null){
            return new TaskAssigner();
        }
        return instance;
    }

    public void addTask(String string_task, int start_x,int end_x,int start_y,int end_y,Map<String, EV> evMap){
        Task task=new Task(string_task,start_x,start_y,end_x,end_y);
        buffer.add(task);
        giveTask(evMap);
    }  


    public static Task assignTask(){
        Task task= buffer.poll();
        // buffer.poll();
        return task;
    }

    public static void giveTask(Map<String, EV> evMap){
        for(Map.Entry<String,EV> entry : evMap.entrySet()){
            EV ev=entry.getValue();
            if(!ev.isMoving()){
                Task task= buffer.poll();
                ev.task=task;
                ev.setMoving(true);
                break;
            }
        }
    }
}
// public class TaskAssigner {
//     public List<EV> evFleet;
//     public GameMap map;
//     private Queue<Task> globalTaskBuffer;

//     public TaskAssigner(List<EV> evFleet, GameMap map) {
//         this.evFleet = evFleet;
//         this.map = map;
//         this.globalTaskBuffer = new LinkedList<>();
//     }

//     public void assignTask(Task task) {
//         if (!globalTaskBuffer.isEmpty()) {
//             assignTasksFromBuffer();
//         }

//         EV bestEV = null;
//         double bestScore = Double.MAX_VALUE;

//         for (EV ev : evFleet) {
//             double score = calculateAssignmentScore(ev, task);
//             if (score < bestScore) {
//                 bestScore = score;
//                 bestEV = ev;
//             }
//         }

//         if (bestEV != null) {
//             bestEV.assignTask(task);
//         } else {
//             // Handle case when no EV can accept the task
//             System.out.println("No suitable EV found for the task. Adding to global task buffer.");
//             addToGlobalTaskBuffer(task);
//         }
//     }

//     private void assignTasksFromBuffer() {
//         Iterator<Task> iterator = globalTaskBuffer.iterator();
//         while (iterator.hasNext()) {
//             Task bufferTask = iterator.next();
//             EV bestEV = null;
//             double bestScore = Double.MAX_VALUE;

//             for (EV ev : evFleet) {
//                 double score = calculateAssignmentScore(ev, bufferTask);
//                 if (score < bestScore) {
//                     bestScore = score;
//                     bestEV = ev;
//                 }
//             }

//             if (bestEV != null) {
//                 bestEV.assignTask(bufferTask);
//                 iterator.remove();
//             }
//         }
//     }

//     private double calculateAssignmentScore(EV ev, Task task) {
//         // Implement scoring logic based on EV's current location, battery level, and
//         // current load
//         // Lower score is better

//         //uncomment
//         // double distanceScore = ev.getLocation().distanceTo(task.getStartLocation());
//         // double batteryScore = 100.0 / ev.charge;
//         // double loadScore = ev.capacity;

//         //return distanceScore + batteryScore + loadScore;
//         return 0.0;
//     }

//     private void addToGlobalTaskBuffer(Task task) {
//         globalTaskBuffer.offer(task);
//     }

//     public void displayTaskAssignmentUI() {
//         // Implement UI for user to assign tasks
//     }
// }
