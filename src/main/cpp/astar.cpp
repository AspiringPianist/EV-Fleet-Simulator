#include "astar.h"
#include <bits/stdc++.h>

std::vector<Node> Dijkstra::findPathInNetwork(const std::vector<std::pair<int, int>>& nodes,
                                   const std::vector<std::vector<int>>& neighbors,
                                   int startX, int startY, int endX, int endY) {
    // Create nodes and set up neighbor relationships only for valid road nodes
    std::vector<Node*> nodeList;
    std::unordered_map<std::string, int> nodeIndex;
    
    // Map coordinates to indices for quick lookup
    for (size_t i = 0; i < nodes.size(); i++) {
        nodeList.push_back(new Node(nodes[i].first, nodes[i].second));
        nodeIndex[std::to_string(nodes[i].first) + "," + std::to_string(nodes[i].second)] = i;
    }

    // Set up neighbors only between valid road nodes
    for (size_t i = 0; i < nodes.size(); i++) {
        for (int neighborIdx : neighbors[i]) {
            nodeList[i]->neighbors.push_back(nodeList[neighborIdx]);
        }
    }

    // Find start and end nodes within valid road nodes
    Node* startNode = nullptr;
    Node* endNode = nullptr;
    std::string startKey = std::to_string(startX) + "," + std::to_string(startY);
    std::string endKey = std::to_string(endX) + "," + std::to_string(endY);
    
    if (nodeIndex.count(startKey) && nodeIndex.count(endKey)) {
        startNode = nodeList[nodeIndex[startKey]];
        endNode = nodeList[nodeIndex[endKey]];
    }

    if (!startNode || !endNode) return std::vector<Node>();

    // Rest of Dijkstra implementation remains the same
    // Since we're only working with valid road nodes now
    std::priority_queue<std::pair<double, Node*>, 
                       std::vector<std::pair<double, Node*>>, 
                       std::greater<>> pq;
    
    startNode->distance = 0;
    pq.push({0, startNode});

    while (!pq.empty()) {
        Node* current = pq.top().second;
        pq.pop();

        if (current == endNode) {
            std::vector<Node> path;
            while (current) {
                path.push_back(*current);
                current = current->parent;
            }
            std::reverse(path.begin(), path.end());
            return path;
        }

        for (Node* neighbor : current->neighbors) {
            double newDist = current->distance + 1;
            if (newDist < neighbor->distance) {
                neighbor->distance = newDist;
                neighbor->parent = current;
                pq.push({newDist, neighbor});
            }
        }
    }

    return std::vector<Node>();
}
