//TODO: Implement A* search algorithm tailored for my project following Sebastian Lague's tutorial
#ifndef ASTAR_H
#define ASTAR_H

#include <vector>
#include <queue>
#include <unordered_map>
#include <cmath>

struct Node {
    int x, y;
    int gCost, hCost, fCost;
    Node* parent;

    Node() : x(0), y(0), gCost(0), hCost(0), fCost(0), parent(nullptr) {}
    Node(int x, int y) : x(x), y(y), gCost(0), hCost(0), fCost(0), parent(nullptr) {}
};


class AStar {
public:
    static std::vector<Node> findPath(int startX, int startY, int endX, int endY, const std::vector<std::vector<bool>>& map);
    static std::vector<std::vector<Node>> findPathWithSteps(int startX, int startY, int endX, int endY, const std::vector<std::vector<bool>>& map);


private:
    static int heuristic(const Node& a, const Node& b);
    static std::vector<Node> getNeighbors(const Node& node, const std::vector<std::vector<bool>>& map);
    static std::vector<Node> reconstructPath(Node* endNode);
    private:
        static int nodeToKey(const Node& node) {
            return node.x * 1000 + node.y; // Assuming map size is less than 1000x1000
        }

};

#endif // ASTAR_H
