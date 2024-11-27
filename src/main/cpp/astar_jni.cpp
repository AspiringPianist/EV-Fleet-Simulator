#include <jni.h>
#include "astar.h"

extern "C" {
    JNIEXPORT jlongArray JNICALL Java_tesla_demo_PathfindingVisualizer_findPathInNetwork
    (JNIEnv* env, jobject obj, jint startX, jint startY, jint endX, jint endY, 
     jobjectArray nodeCoords, jobjectArray neighborLists) {
        
        // Convert node coordinates
        jsize numNodes = env->GetArrayLength(nodeCoords);
        std::vector<std::pair<int, int>> nodes(numNodes);
        
        for (jsize i = 0; i < numNodes; i++) {
            jintArray coordPair = (jintArray)env->GetObjectArrayElement(nodeCoords, i);
            jint* coords = env->GetIntArrayElements(coordPair, nullptr);
            nodes[i] = {coords[0], coords[1]};
            env->ReleaseIntArrayElements(coordPair, coords, 0);
        }

        // Convert neighbor lists
        std::vector<std::vector<int>> neighbors(numNodes);
        for (jsize i = 0; i < numNodes; i++) {
            jintArray neighborArray = (jintArray)env->GetObjectArrayElement(neighborLists, i);
            jsize neighborCount = env->GetArrayLength(neighborArray);
            jint* neighborIndices = env->GetIntArrayElements(neighborArray, nullptr);
            
            neighbors[i].assign(neighborIndices, neighborIndices + neighborCount);
            env->ReleaseIntArrayElements(neighborArray, neighborIndices, 0);
        }

        // Find path
        std::vector<Node> path = Dijkstra::findPathInNetwork(nodes, neighbors, startX, startY, endX, endY);

        // Convert result to Java array
        jlongArray result = env->NewLongArray(path.size() * 2);
        std::vector<jlong> coords(path.size() * 2);
        
        for (size_t i = 0; i < path.size(); i++) {
            coords[i * 2] = path[i].x;
            coords[i * 2 + 1] = path[i].y;
        }
        
        env->SetLongArrayRegion(result, 0, coords.size(), coords.data());
        return result;
    }
}
