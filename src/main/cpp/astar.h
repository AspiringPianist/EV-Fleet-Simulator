#ifndef ASTAR_H
#define ASTAR_H

#include <vector>
#include <queue>
#include <unordered_map>
#include <string>

struct Node {
    int x, y;
    double distance;
    Node* parent;
    std::vector<Node*> neighbors;
    
    Node(int x, int y) : x(x), y(y), distance(INT_MAX), parent(nullptr) {}
};

class Dijkstra {
public:
    static std::vector<Node> findPathInNetwork(const std::vector<std::pair<int, int>>& nodes,
                                    const std::vector<std::vector<int>>& neighbors,
                                    int startX, int startY, int endX, int endY);
};

#endif

