#include "astar.h"
#include <bits/stdc++.h>
std::vector<Node> AStar::findPath(int startX, int startY, int endX, int endY, const std::vector<std::vector<bool>> &map)
{
    Node startNode(startX, startY);
    Node endNode(endX, endY);

    std::priority_queue<std::pair<int, Node *>, std::vector<std::pair<int, Node *>>, std::greater<std::pair<int, Node *>>> openSet;
    std::unordered_map<int, Node> allNodes;

    startNode.hCost = heuristic(startNode, endNode);
    startNode.fCost = startNode.hCost;

    openSet.push({startNode.fCost, &startNode});
    allNodes[nodeToKey(startNode)] = startNode;

    while (!openSet.empty())
    {
        Node *current = openSet.top().second;
        openSet.pop();

        if (current->x == endNode.x && current->y == endNode.y)
        {
            return reconstructPath(current);
        }

        for (const Node &neighbor : getNeighbors(*current, map))
        {
            int tentativeGCost = current->gCost + 1;
            int neighborKey = nodeToKey(neighbor);

            if (allNodes.find(neighborKey) == allNodes.end() || tentativeGCost < allNodes[neighborKey].gCost)
            {
                Node &neighborNode = allNodes[neighborKey];
                neighborNode = neighbor;
                neighborNode.gCost = tentativeGCost;
                neighborNode.hCost = heuristic(neighborNode, endNode);
                neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;
                neighborNode.parent = current;

                openSet.push({neighborNode.fCost, &allNodes[neighborKey]});
            }
        }
    }

    return {}; // No path found
}

int AStar::heuristic(const Node &a, const Node &b)
{
    //return std::sqrt(std::pow(a.x - b.x, 2) + std::pow(a.y - b.y, 2));
    //optimized the heuristic
    int dstX = abs(a.x - b.x);
	int dstY = abs(a.y - b.y);
	if (dstX > dstY)
		return 14*dstY + 10* (dstX-dstY);
	return 14*dstX + 10 * (dstY-dstX);
}

std::vector<Node> AStar::getNeighbors(const Node &node, const std::vector<std::vector<bool>> &map)
{
    std::vector<Node> neighbors;
    int dx[] = {0, 0, 1, -1, 1, 1, -1, -1};
    int dy[] = {1, -1, 0, 0, 1, -1, 1, -1};

    for (int i = 0; i < 8; ++i)
    {
        int newX = node.x + dx[i];
        int newY = node.y + dy[i];

        if (newX >= 0 && newX < map.size() && newY >= 0 && newY < map[0].size() && !map[newX][newY])
        {
            neighbors.emplace_back(newX, newY);
        }
    }

    return neighbors;
}

std::vector<std::vector<Node>> AStar::findPathWithSteps(int startX, int startY, int endX, int endY, const std::vector<std::vector<bool>> &map)
{
    Node startNode(startX, startY);
    Node endNode(endX, endY);
    std::vector<std::vector<Node>> steps;

    std::priority_queue<std::pair<int, Node *>, std::vector<std::pair<int, Node *>>, std::greater<std::pair<int, Node *>>> openSet;
    std::unordered_map<int, Node> allNodes;

    startNode.hCost = heuristic(startNode, endNode);
    startNode.fCost = startNode.hCost;

    openSet.push({startNode.fCost, &startNode});
    allNodes[nodeToKey(startNode)] = startNode;

    while (!openSet.empty())
    {
        Node *current = openSet.top().second;
        openSet.pop();

        if (current->x == endNode.x && current->y == endNode.y)
        {
            steps.push_back(reconstructPath(current));
            return steps;
        }

        for (const Node &neighbor : getNeighbors(*current, map))
        {
            int tentativeGCost = current->gCost + 1;
            int neighborKey = nodeToKey(neighbor);

            if (allNodes.find(neighborKey) == allNodes.end() || tentativeGCost < allNodes[neighborKey].gCost)
            {
                Node &neighborNode = allNodes[neighborKey];
                neighborNode = neighbor;
                neighborNode.gCost = tentativeGCost;
                neighborNode.hCost = heuristic(neighborNode, endNode);
                neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;
                neighborNode.parent = current;

                openSet.push({neighborNode.fCost, &allNodes[neighborKey]});
            }
        }

        // Store the current step
        std::vector<Node> currentStep;
        for (const auto &pair : allNodes)
        {
            currentStep.push_back(pair.second);
        }
        steps.push_back(currentStep);
    }

    return steps; // Return all steps if no path is found
}

std::vector<Node> AStar::reconstructPath(Node *endNode)
{
    std::vector<Node> path;
    Node *current = endNode;

    while (current != nullptr)
    {
        path.push_back(*current);
        current = current->parent;
    }

    std::reverse(path.begin(), path.end());
    return path;
}