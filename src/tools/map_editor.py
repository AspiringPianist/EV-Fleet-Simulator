import tkinter as tk
from tkinter import ttk, messagebox, filedialog
import csv
import time

class RoadMapEditor:
    def __init__(self, root):
        self.root = root
        self.root.title("Road Network Editor")
        
        self.selected_node = None
        self.grid_size = 50
        self.cell_size = 20
        self.connections = {}
        self.dragging = False
        self.last_drag_cell = None
        self.mouse_position = None
        self.last_update_time = 0
        self.update_interval = 16  # ~60 FPS
        #self.signals = set()  # Store traffic signal locations
        # In __init__ method, change signals to store type:
        self.signals = {}  # Dictionary to store {(x,y): signal_type}

        
        # Main frame
        self.frame = ttk.Frame(root)
        self.frame.pack(expand=True, fill='both', padx=10, pady=10)
        
        # Toolbar
        self.toolbar = ttk.Frame(self.frame)
        self.toolbar.pack(fill='x', pady=5)
        
        self.selected_node = None
        self.mode = tk.StringVar(value="draw")
        ttk.Radiobutton(self.toolbar, text="Draw Roads", variable=self.mode, value="draw").pack(side='left', padx=5)
        ttk.Radiobutton(self.toolbar, text="Connect Nodes", variable=self.mode, value="connect").pack(side='left', padx=5)
        ttk.Radiobutton(self.toolbar, text="Erase", variable=self.mode, value="erase").pack(side='left', padx=5)
        
        self.road_type = tk.StringVar(value="two_way")
        ttk.Radiobutton(self.toolbar, text="Two-Way Road", variable=self.road_type, value="two_way").pack(side='left', padx=5)
        ttk.Radiobutton(self.toolbar, text="One-Way Road", variable=self.road_type, value="one_way").pack(side='left', padx=5)
        
        self.signal_type = tk.StringVar(value="1")
        ttk.Radiobutton(self.toolbar, text="Signal Type 1", variable=self.signal_type, value="1").pack(side='left', padx=5)
        ttk.Radiobutton(self.toolbar, text="Signal Type 2", variable=self.signal_type, value="2").pack(side='left', padx=5)
        ttk.Radiobutton(self.toolbar, text="Signal Type 3", variable=self.signal_type, value="3").pack(side='left', padx=5)
        ttk.Radiobutton(self.toolbar, text="Signal Type 4", variable=self.signal_type, value="4").pack(side='left', padx=5)
        ttk.Radiobutton(self.toolbar, text="Place Signals", variable=self.mode, value="signal").pack(side='left', padx=5)

        
        # File operations        
        ttk.Button(self.toolbar, text="Load CSV", command=self.load_map).pack(side='right', padx=5)
        ttk.Button(self.toolbar, text="Load Signals", command=self.load_signals).pack(side='right', padx=5)

        ttk.Button(self.toolbar, text="Save", command=self.save_map).pack(side='right', padx=5)
        ttk.Button(self.toolbar, text="Clear All", command=self.clear_map).pack(side='right', padx=5)
        ttk.Button(self.toolbar, text="Auto Place Signals", command=self.auto_place_signals).pack(side='left', padx=5)
        
        # Canvas setup
        self.canvas_frame = ttk.Frame(self.frame)
        self.canvas_frame.pack(expand=True, fill='both')
        
        self.canvas = tk.Canvas(self.canvas_frame, 
                              width=self.grid_size*self.cell_size,
                              height=self.grid_size*self.cell_size,
                              bg='white')
        self.canvas.pack(side='left', fill='both', expand=True)
        
        self.draw_grid()
        self.setup_events()

    def auto_place_signals(self):
        self.signals.clear()
        for key, connections in self.connections.items():
            x, y = map(int, key.split(','))
            if len(connections) > 2:  # Intersection detected
                # Add the intersection point
                self.signals.add((x, y))
                
                # Add the eight surrounding squares
                surrounding = [
                    (x-1, y-1), (x-1, y), (x-1, y+1),
                    (x, y-1),             (x, y+1),
                    (x+1, y-1), (x+1, y), (x+1, y+1)
                ]
                
                # Add valid surrounding squares (within grid bounds)
                for sx, sy in surrounding:
                    if 1 <= sx <= self.grid_size and 1 <= sy <= self.grid_size:
                        self.signals.add((sx, sy))
                        
        self.redraw_roads()

    def save_signals(self):
        with open('src/main/resources/static/signals.csv', 'w', newline='') as file:
            writer = csv.writer(file)
            for i in range(self.grid_size):
                row = []
                for j in range(self.grid_size):
                    pos = (i+1, j+1)
                    row.append(self.signals.get(pos, '0'))
                writer.writerow(row)
    def load_signals(self):
        filepath = filedialog.askopenfilename(
            defaultextension=".csv",
            filetypes=[("CSV files", "*.csv"), ("All files", "*.*")],
            initialdir="src/main/resources/static/"
        )
        if filepath:
            self.signals.clear()
            with open(filepath, 'r') as file:
                reader = csv.reader(file)
                for i, row in enumerate(reader):
                    for j, cell in enumerate(row):
                        if cell != '0':
                            self.signals[(i+1, j+1)] = cell
            self.redraw_roads()
            messagebox.showinfo("Success", "Signals loaded successfully!")

    def save_map(self):
        with open('map.csv', 'w', newline='') as file:
            writer = csv.writer(file)
            for i in range(self.grid_size):
                row = []
                for j in range(self.grid_size):
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
        self.save_signals()
        messagebox.showinfo("Success", "Map and signals saved successfully!")

    def load_map(self):
        filepath = filedialog.askopenfilename(
            defaultextension=".csv",
            filetypes=[("CSV files", "*.csv"), ("All files", "*.*")],
            initialdir="./"
        )
        if filepath:
            self.connections.clear()
            with open(filepath, 'r') as file:
                reader = csv.reader(file)
                for i, row in enumerate(reader):
                    for j, cell in enumerate(row):
                        if cell != '0':
                            coords = self.parse_cell_data(cell)
                            if coords:
                                key = f"{i+1},{j+1}"
                                self.connections[key] = coords
            self.redraw_roads()
            messagebox.showinfo("Success", "Map loaded successfully!")

    def setup_events(self):
        self.canvas.bind('<Button-1>', self.on_canvas_click)
        self.canvas.bind('<B1-Motion>', self.handle_drag)
        self.canvas.bind('<ButtonRelease-1>', self.end_drag)

    def on_canvas_click(self, event):
        if self.mode.get() == "connect":
            self.handle_click(event)
        else:
            self.start_drag(event)

    def handle_click(self, event):
        clicked_cell = self.get_cell_coords(event)
        if self.selected_node is None:
            self.selected_node = clicked_cell
            x = (clicked_cell[1] - 1) * self.cell_size
            y = (clicked_cell[0] - 1) * self.cell_size
            self.canvas.create_rectangle(
                x, y, x + self.cell_size, y + self.cell_size,
                fill='yellow', outline='orange', width=2,
                tags='node_highlight'
            )
        else:
            self.connect_cells(self.selected_node, clicked_cell)
            self.canvas.delete('node_highlight')
            self.selected_node = None
            self.redraw_roads()

    def start_drag(self, event):
        self.dragging = True
        self.last_drag_cell = self.get_cell_coords(event)
        self.handle_cell(self.last_drag_cell)

    def handle_drag(self, event):
        current_time = time.time()
        if current_time - self.last_update_time < self.update_interval/1000:
            return
        current_cell = self.get_cell_coords(event)
        if self.last_drag_cell and current_cell != self.last_drag_cell:
            self.handle_cell(current_cell)
            if self.mode.get() == "draw":
                self.connect_cells(self.last_drag_cell, current_cell)
            self.last_drag_cell = current_cell
        self.last_update_time = current_time
        self.redraw_roads()

    def connect_cells(self, cell1, cell2):
        key1 = f"{cell1[0]},{cell1[1]}"
        key2 = f"{cell2[0]},{cell2[1]}"
        
        if key1 not in self.connections:
            self.connections[key1] = []
        if key2 not in self.connections:
            self.connections[key2] = []
            
        if self.road_type.get() == "two_way":
            if cell2 not in self.connections[key1]:
                self.connections[key1].append(cell2)
            if cell1 not in self.connections[key2]:
                self.connections[key2].append(cell1)
        else:
            if cell2 not in self.connections[key1]:
                self.connections[key1].append(cell2)
        self.redraw_roads()

    def handle_cell(self, cell):
        if self.mode.get() == "erase":
            self.erase_cell(cell)
        elif self.mode.get() == "signal":
            self.signals[cell] = self.signal_type.get()
            self.redraw_roads()

    def erase_cell(self, cell):
        key = f"{cell[0]},{cell[1]}"
        if key in self.connections:
            del self.connections[key]
        for connections in self.connections.values():
            if cell in connections:
                connections.remove(cell)
        self.redraw_roads()

    def end_drag(self, event):
        self.dragging = False
        self.last_drag_cell = None

    # Update the redraw_roads method to add visual indicators
    def redraw_roads(self):
        self.canvas.delete('road', 'intersection', 'signal', 'signal_text')
        # Draw existing roads first
        for start, ends in self.connections.items():
            x1, y1 = map(int, start.split(','))
            for end in ends:
                self.draw_road_segment((x1, y1), end)
                
        # Draw signals with enhanced visual feedback
        for pos, signal_type in self.signals.items():
            x = (pos[1] - 1) * self.cell_size
            y = (pos[0] - 1) * self.cell_size
            if signal_type == '1':
                color = 'green'
            elif signal_type == '2':
                color = 'orange'
            elif signal_type == '3':
                color = 'red'
            elif signal_type == '4':
                color = 'blue'
            else:
                color = 'gray'  # Default color for unknown types
            # Draw signal background
            self.canvas.create_rectangle(
                x, y, x + self.cell_size, y + self.cell_size,
                fill=color, tags='signal'
            )
            
            # Draw signal type number
            self.canvas.create_text(
                x + self.cell_size/2,
                y + self.cell_size/2,
                text=signal_type,
                fill='white',
                font=('Arial', 12, 'bold'),
                tags='signal_text'
            )
            
            # Add circular indicator
            self.canvas.create_oval(
                x + 2, y + 2,
                x + self.cell_size - 2, y + self.cell_size - 2,
                outline='white',
                width=2,
                tags='signal'
            )


    def draw_road_segment(self, start, end):
        start_x = (start[1] - 1) * self.cell_size + self.cell_size//2
        start_y = (start[0] - 1) * self.cell_size + self.cell_size//2
        end_x = (end[1] - 1) * self.cell_size + self.cell_size//2
        end_y = (end[0] - 1) * self.cell_size + self.cell_size//2
        
        self.canvas.create_line(
            start_x, start_y, end_x, end_y,
            arrow=tk.LAST, fill='blue', width=2,
            tags='road'
        )
        self.canvas.create_oval(
            start_x-3, start_y-3,
            start_x+3, start_y+3,
            fill='red', tags='intersection'
        )

    def clear_map(self):
        self.connections.clear()
        self.signals.clear()
        self.redraw_roads()

    def draw_grid(self):
        for i in range(self.grid_size):
            for j in range(self.grid_size):
                x1 = j * self.cell_size
                y1 = i * self.cell_size
                self.canvas.create_rectangle(
                    x1, y1,
                    x1+self.cell_size,
                    y1+self.cell_size,
                    fill='white',
                    tags='cell'
                )

    def get_cell_coords(self, event):
        x = event.y // self.cell_size + 1
        y = event.x // self.cell_size + 1
        return (x, y)

    def parse_cell_data(self, cell):
        if not cell or cell == '0':
            return []
        coords = []
        parts = cell.split('),(')
        for part in parts:
            part = part.strip('()')
            if ',' in part:
                x, y = map(int, part.split(','))
                coords.append((x, y))
        return coords

if __name__ == "__main__":
    root = tk.Tk()
    app = RoadMapEditor(root)
    root.mainloop()
