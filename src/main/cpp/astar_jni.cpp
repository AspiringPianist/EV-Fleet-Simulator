#include <jni.h>
#include "astar.h"

extern "C"
{

    JNIEXPORT jlongArray JNICALL Java_tesla_demo_PathfindingVisualizer_findPath(JNIEnv *env, jobject obj, jint startX, jint startY, jint endX, jint endY, jobjectArray obstacleMap)
    {
        // Convert Java boolean[][] to C++ vector<vector<bool>>
        jsize rows = env->GetArrayLength(obstacleMap);
        jsize cols = env->GetArrayLength((jbooleanArray)env->GetObjectArrayElement(obstacleMap, 0));

        std::vector<std::vector<bool>> map(rows, std::vector<bool>(cols));

        for (jsize i = 0; i < rows; i++)
        {
            jbooleanArray row = (jbooleanArray)env->GetObjectArrayElement(obstacleMap, i);
            jboolean *elements = env->GetBooleanArrayElements(row, nullptr);

            for (jsize j = 0; j < cols; j++)
            {
                map[i][j] = elements[j];
            }

            env->ReleaseBooleanArrayElements(row, elements, JNI_ABORT);
        }

        // Call A* algorithm
        std::vector<Node> path = AStar::findPath(startX, startY, endX, endY, map);

        // Convert path to jlongArray (x and y coordinates interleaved)
        jlongArray result = env->NewLongArray(path.size() * 2);
        jlong *elements = env->GetLongArrayElements(result, nullptr);

        for (size_t i = 0; i < path.size(); i++)
        {
            elements[i * 2] = path[i].x;
            elements[i * 2 + 1] = path[i].y;
        }

        env->ReleaseLongArrayElements(result, elements, 0);
        return result;
    }

    JNIEXPORT jobjectArray JNICALL Java_tesla_demo_PathfindingVisualizer_findPathWithSteps(JNIEnv *env, jobject obj, jint startX, jint startY, jint endX, jint endY, jobjectArray obstacleMap)
    {
        // Convert Java boolean[][] to C++ vector<vector<bool>>
        jsize rows = env->GetArrayLength(obstacleMap);
        jsize cols = env->GetArrayLength((jbooleanArray)env->GetObjectArrayElement(obstacleMap, 0));

        std::vector<std::vector<bool>> map(rows, std::vector<bool>(cols));

        for (jsize i = 0; i < rows; i++)
        {
            jbooleanArray row = (jbooleanArray)env->GetObjectArrayElement(obstacleMap, i);
            jboolean *elements = env->GetBooleanArrayElements(row, nullptr);

            for (jsize j = 0; j < cols; j++)
            {
                map[i][j] = elements[j];
            }

            env->ReleaseBooleanArrayElements(row, elements, JNI_ABORT);
        }

        // Call A* algorithm with step tracking
        std::vector<std::vector<Node>> steps = AStar::findPathWithSteps(startX, startY, endX, endY, map);

        // Convert steps to jobjectArray (array of long[])
        jclass longArrayClass = env->FindClass("[J");
        jobjectArray result = env->NewObjectArray(steps.size(), longArrayClass, nullptr);

        for (size_t i = 0; i < steps.size(); i++)
        {
            jlongArray stepArray = env->NewLongArray(steps[i].size() * 2);
            jlong *elements = env->GetLongArrayElements(stepArray, nullptr);

            for (size_t j = 0; j < steps[i].size(); j++)
            {
                elements[j * 2] = steps[i][j].x;
                elements[j * 2 + 1] = steps[i][j].y;
            }

            env->ReleaseLongArrayElements(stepArray, elements, 0);
            env->SetObjectArrayElement(result, i, stepArray);
        }

        return result;
    }
}
