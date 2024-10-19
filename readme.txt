"""
GameObject Class
----------------
A base class representing objects in the game world.

Attributes:
    x (int): The x-coordinate of the object's position.
    y (int): The y-coordinate of the object's position.

This class serves as a foundation for various game entities such as obstacles,
electric vehicles (EVs), charging stations, etc. It simplifies rendering these
objects on the game map by providing a common structure for position data.

Methods:
    get_details(): An abstract method to be implemented by child classes.
                   This method should return specific information about the object.

Usage:
    Inherit from this class to create specific game object types. Implement
    the get_details() method in each child class to provide object-specific
    information for rendering or other purposes.

Example:
    class Obstacle(GameObject):
        def get_details(self):
            return "Obstacle at ({}, {})".format(self.x, self.y)

Note:
    When rendering GameObjects on the map, use the get_details() method
    to retrieve the necessary information for each object type.
"""