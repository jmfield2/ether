from .cosmos_enums import DoorType # Assuming cosmos_enums.py is in the same directory or path

# Placeholder for MoveFailCode, will be defined properly later
class MoveFailCode: # TODO: Define this as an Enum based on Java's MoveFailCode
    NONE = 0
    DOOR_CLOSED = 1
    DOOR_LOCKED = 2
    DOOR_NEEDS_KEY = 3
    DOOR_NEEDS_RUNE = 4
    # ... other failure reasons

class Door:
    """Base class for all doors."""
    def __init__(self, door_type: DoorType, v0: int = 0, v3: int = 0, v4: int = 0, v5: int = 0, v6: int = 0):
        self.door_type: DoorType = door_type
        self.v0: int = v0  # Often related to key ID, lock difficulty, or specific room ID
        self.v3: int = v3  # Custom parameter, meaning varies by door type
        self.v4: int = v4  # Custom parameter
        self.v5: int = v5  # Custom parameter
        self.v6: int = v6  # Custom parameter (e.g. associated spell for some doors)

        # Transient runtime state
        self.is_locked: bool = True # Most doors start locked by default
        self.is_closed: bool = True # Most doors start closed

        # Some doors might be initially open or unlocked based on their type or vX parameters,
        # but that logic would typically be in a more specific constructor or game setup.

    def get_move_fail_code(self, player) -> int: # player type hint later
        """
        Determines if a player can move through this door.
        For now, returns a simple code. Will be expanded with game logic.
        """
        if self.is_closed:
            # Further checks if it's locked, needs key, etc.
            if self.is_locked:
                # This is a simplified check. Real logic will be more complex.
                if self.door_type == DoorType.HAS_RUNE and self.v0 > 0: # v0 might be rune ID
                    return MoveFailCode.DOOR_NEEDS_RUNE
                # Other lock types would be checked here
                return MoveFailCode.DOOR_LOCKED
            return MoveFailCode.DOOR_CLOSED # It's closed but not locked
        return MoveFailCode.NONE # Open

    def __repr__(self) -> str:
        return (f"{self.__class__.__name__}(type={self.door_type.name}, v0={self.v0}, v3={self.v3}, v4={self.v4}, v5={self.v5}, v6={self.v6}, "
                f"locked={self.is_locked}, closed={self.is_closed})")

class PrivateRoomDoor(Door):
    """Door for private rooms, v0 might be room ID or owner ID."""
    def __init__(self, v0: int = 0, v3: int = 0, v4: int = 0, v5: int = 0, v6: int = 0):
        super().__init__(DoorType.PRIVATE_ROOM, v0, v3, v4, v5, v6)
        # Private rooms might start locked and only openable by owner or key.

class HasRuneDoor(Door):
    """Door that requires a specific rune (v0 might be rune ID)."""
    def __init__(self, v0: int = 0, v3: int = 0, v4: int = 0, v5: int = 0, v6: int = 0):
        super().__init__(DoorType.HAS_RUNE, v0, v3, v4, v5, v6)

class LevelRequirementDoor(Door):
    """Door that requires a certain player level (v0 might be min level)."""
    def __init__(self, v0: int = 0, v3: int = 0, v4: int = 0, v5: int = 0, v6: int = 0):
        super().__init__(DoorType.LEVEL_REQUIREMENT, v0, v3, v4, v5, v6)

class KeyRequirementDoor(Door):
    """Door that requires a specific key item (v0 might be key item ID)."""
    def __init__(self, v0: int = 0, v3: int = 0, v4: int = 0, v5: int = 0, v6: int = 0):
        super().__init__(DoorType.KEY_REQUIREMENT, v0, v3, v4, v5, v6)

# ... other door types would follow the same pattern ...
# AlignmentRequirementDoor, ClassRequirementDoor, RaceRequirementDoor, StatRequirementDoor etc.

# Factory function to create door instances based on DoorType enum or class name string
def create_door_from_type(door_type: DoorType, v0: int, v3: int, v4: int, v5: int, v6: int) -> Door:
    """
    Factory to create specific door instances from DoorType enum.
    """
    door_class_map = {
        DoorType.PRIVATE_ROOM: PrivateRoomDoor,
        DoorType.HAS_RUNE: HasRuneDoor,
        DoorType.LEVEL_REQUIREMENT: LevelRequirementDoor,
        DoorType.KEY_REQUIREMENT: KeyRequirementDoor,
        # Add other mappings here
    }

    target_class = door_class_map.get(door_type)

    if target_class:
        return target_class(v0, v3, v4, v5, v6)
    else:
        # Fallback for unmapped or generic doors
        print(f"Warning: No specific Python class found for DoorType {door_type.name}. Creating generic Door instance.")
        return Door(door_type, v0, v3, v4, v5, v6)

if __name__ == '__main__':
    # Test Door creation
    generic_door = Door(DoorType.GENERIC_DOOR, 1, 2, 3, 4, 5)
    print(generic_door)

    private_door = PrivateRoomDoor(v0=101) # e.g. Room 101 is private
    print(private_door)
    print(f"Private door fail code (closed, locked): {private_door.get_move_fail_code(None)}")
    private_door.is_locked = False
    print(f"Private door fail code (closed, unlocked): {private_door.get_move_fail_code(None)}")
    private_door.is_closed = False
    print(f"Private door fail code (open, unlocked): {private_door.get_move_fail_code(None)}")


    rune_door = HasRuneDoor(v0=77) # Requires rune 77
    print(rune_door)
    print(f"Rune door fail code: {rune_door.get_move_fail_code(None)}")


    # Test factory
    created_door = create_door_from_type(DoorType.HAS_RUNE, 123, 0,0,0,0)
    print(f"Created via factory: {created_door}")
    assert isinstance(created_door, HasRuneDoor)

    created_generic = create_door_from_type(DoorType.ALIGNMENT_REQUIREMENT, 1,0,0,0,0) # Assuming ALIGNMENT_REQUIREMENT not in map yet
    print(f"Created generic via factory: {created_generic}")
    assert not isinstance(created_generic, PrivateRoomDoor) # Should be generic Door instance
    assert isinstance(created_generic, Door)
    assert created_generic.door_type == DoorType.ALIGNMENT_REQUIREMENT

```
