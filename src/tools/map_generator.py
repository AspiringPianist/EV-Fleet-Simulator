import random
import csv

class MapGenerator:
    def __init__(self, size=40, intersection_spacing=6):
        self.size = size
        self.spacing = intersection_spacing
        self.connections = {}
        self.traffic_signals = set()

    def generate_grid(self):
        # Create main intersections
        for i in range(self.spacing, self.size-self.spacing, self.spacing):
            for j in range(self.spacing, self.size-self.spacing, self.spacing):
                # Create intersection and connect adjacent cells
                self.create_intersection_with_connections(i, j)
                self.traffic_signals.add((i, j))

        # Create connecting roads between intersections
        for i in range(self.spacing, self.size-self.spacing, self.spacing):
            for j in range(self.spacing, self.size-self.spacing, self.spacing):
                if j + self.spacing < self.size:
                    self.create_road_segment(i, j, i, j + self.spacing)
                if i + self.spacing < self.size:
                    self.create_road_segment(i, j, i + self.spacing, j)

    def create_intersection_with_connections(self, x, y):
        # Connect to adjacent cells in all four directions
        directions = [(0,1), (1,0), (0,-1), (-1,0)]
        for dx, dy in directions:
            next_x = x + dx
            next_y = y + dy
            if 0 < next_x < self.size and 0 < next_y < self.size:
                self.connect_one_way((x,y), (next_x,next_y))
                # Create two-way connection at intersections
                self.connect_one_way((next_x,next_y), (x,y))

    def create_road_segment(self, start_x, start_y, end_x, end_y):
        # Create cell-by-cell connections along the road
        dx = 1 if end_x > start_x else -1 if end_x < start_x else 0
        dy = 1 if end_y > start_y else -1 if end_y < start_y else 0
        
        current_x, current_y = start_x, start_y
        while (current_x, current_y) != (end_x, end_y):
            next_x = current_x + dx
            next_y = current_y + dy
            self.connect_one_way((current_x,current_y), (next_x,next_y))
            self.connect_one_way((next_x,next_y), (current_x,current_y))
            current_x, current_y = next_x, next_y

    def connect_one_way(self, p1, p2):
        key1 = f"{p1[0]},{p1[1]}"
        if key1 not in self.connections:
            self.connections[key1] = []
        if p2 not in self.connections[key1]:
            self.connections[key1].append(p2)

    def save_map(self):
        # Save road network
        with open('src/main/resources/static/map.csv', 'w', newline='') as file:
            writer = csv.writer(file)
            for i in range(self.size):
                row = []
                for j in range(self.size):
                    key = f"{i+1},{j+1}"
                    if key in self.connections:
                        connections = self.connections[key]
                        if connections:
                            coords = [f"({x},{y})" for x,y in connections]
                            cell = ','.join(coords)
                        else:
                            cell = '0'
                    else:
                        cell = '0'
                    row.append(cell)
                writer.writerow(row)

        # Save traffic signals
        with open('src/main/resources/static/signals.csv', 'w', newline='') as file:
            writer = csv.writer(file)
            for i in range(self.size):
                row = []
                for j in range(self.size):
                    if (i, j) in self.traffic_signals:
                        row.append('1')
                    else:
                        row.append('0')
                writer.writerow(row)

def generate_random_map():
    generator = MapGenerator(size=40, intersection_spacing=1)
    generator.generate_grid()
    generator.save_map()

if __name__ == "__main__":
    generate_random_map()
