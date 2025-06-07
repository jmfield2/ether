from enum import Enum, IntFlag

class ExitDirection(Enum):
    NORTH = "NORTH"
    EAST = "EAST"
    SOUTH = "SOUTH"
    WEST = "WEST"
    NORTHEAST = "NORTHEAST"
    NORTHWEST = "NORTHWEST"
    SOUTHEAST = "SOUTHEAST"
    SOUTHWEST = "SOUTHWEST"
    UP = "UP"
    DOWN = "DOWN"
    UNKNOWN = "UNKNOWN" # Should not typically be in data, but good for default

    @classmethod
    def from_string(cls, s: str) -> 'ExitDirection':
        for direction in cls:
            if direction.value == s.upper():
                return direction
        # Fallback for "North", "East", etc. if data is not all caps
        for direction in cls:
            if direction.name == s.upper():
                return direction
        raise ValueError(f"Unknown exit direction: {s}")

class Terrain(Enum):
    # Values from Terrain.java (ensure these match the strings in XML)
    NONE = "NONE" # Default or placeholder
    BEACH = "BEACH"
    BRIDGE = "BRIDGE"
    CAVE = "CAVE"
    CITY_STREETS = "CITY_STREETS"
    DESERT = "DESERT"
    DIRT_ROAD = "DIRT_ROAD"
    DUNGEON1 = "DUNGEON1" # Example, adjust if actual names differ
    DUNGEON2 = "DUNGEON2"
    DUNGEON3 = "DUNGEON3"
    FIELD = "FIELD"
    FOREST = "FOREST"
    HILLS = "HILLS"
    INSIDE_BUILDING = "INSIDE_BUILDING"
    JUNGLE = "JUNGLE"
    LAKE = "LAKE"
    LAVA = "LAVA"
    MAGIC_FOREST = "MAGIC_FOREST"
    MOUNTAINS = "MOUNTAINS"
    OCEAN = "OCEAN"
    PAVED_ROAD = "PAVED_ROAD"
    PLANE_OF_AIR = "PLANE_OF_AIR"
    PLANE_OF_EARTH = "PLANE_OF_EARTH"
    PLANE_OF_FIRE = "PLANE_OF_FIRE"
    PLANE_OF_WATER = "PLANE_OF_WATER"
    RIVER = "RIVER"
    RUINS = "RUINS"
    SHALLOW_WATER = "SHALLOW_WATER"
    SNOW = "SNOW"
    SWAMP = "SWAMP"
    TOWN = "TOWN" # As per prompt
    TUNNEL = "TUNNEL"
    UNDERWATER = "UNDERWATER"
    WOODS = "WOODS"
    # Add all terrains from Terrain.java, ensuring string value matches XML
    # For now, these are common ones + what's in prompt.
    # The XML parser will reveal if any are missing.

    @classmethod
    def from_string(cls, s: str) -> 'Terrain':
        s_upper = s.upper()
        for terrain_type in cls:
            if terrain_type.value == s_upper:
                return terrain_type
        # Some XML might use mixed case or different naming, adjust as needed
        # For example, if XML has "City Streets", but enum is CITY_STREETS = "CITY_STREETS"
        # This basic from_string assumes exact match of the value after uppercasing.
        # A more robust solution might involve a mapping if names differ significantly.
        # Check common variations
        if s_upper == "CITY STREETS": return cls.CITY_STREETS
        if s_upper == "DIRT ROAD": return cls.DIRT_ROAD
        if s_upper == "INSIDE BUILDING": return cls.INSIDE_BUILDING
        if s_upper == "MAGIC FOREST": return cls.MAGIC_FOREST
        if s_upper == "PLANE OF AIR": return cls.PLANE_OF_AIR
        if s_upper == "PLANE OF EARTH": return cls.PLANE_OF_EARTH
        if s_upper == "PLANE OF FIRE": return cls.PLANE_OF_FIRE
        if s_upper == "PLANE OF WATER": return cls.PLANE_OF_WATER
        if s_upper == "SHALLOW WATER": return cls.SHALLOW_WATER

        raise ValueError(f"Unknown terrain type: {s}")


class RoomFlags(IntFlag):
    NONE = 0
    SAFE = 1         # Cannot fight here
    DARK = 2         # Need light source
    NO_MAGIC = 4     # Cannot cast spells
    ARENA = 8        # Player Killable zone
    NO_SUMMON = 16   # Cannot summon mobs/pets
    NO_TELEPORT = 32 # Cannot teleport into or out of
    TAVERN = 64      # For specific tavern related events/commands
    HEALING_REGEN = 128 # Increased HP regen
    MANA_REGEN = 256    # Increased Mana regen
    NO_NPC = 512        # NPCs cannot enter
    PRIVATE = 1024      # Private room, often instanced or owned
    BANK = 2048
    SHOP = 4096
    NO_MOUNT = 8192
    # Add other flags from RoomFlags.java with their bit values

    @classmethod
    def from_int(cls, i: int) -> 'RoomFlags':
        # This will create a RoomFlags instance with all flags that match the bits in i
        return RoomFlags(i)


class DoorType(Enum):
    # These are examples; actual types will be derived from door class names in XML
    # e.g., "org.tdod.ether.taimpl.cosmos.doors.PrivateRoomDoor" -> DoorType.PRIVATE_ROOM
    GENERIC_DOOR = "GenericDoor" # A default if no specific type matches
    PRIVATE_ROOM = "PrivateRoomDoor"
    HAS_RUNE = "HasRuneDoor"
    LEVEL_REQUIREMENT = "LevelRequirementDoor"
    KEY_REQUIREMENT = "KeyRequirementDoor"
    ALIGNMENT_REQUIREMENT = "AlignmentRequirementDoor"
    CLASS_REQUIREMENT = "ClassRequirementDoor"
    RACE_REQUIREMENT = "RaceRequirementDoor"
    STAT_REQUIREMENT = "StatRequirementDoor"
    # Add more as discovered or mapped from Java class names.

    @classmethod
    def from_class_name(cls, class_name_str: str) -> 'DoorType':
        # Example: class_name_str might be "org.tdod.ether.taimpl.cosmos.doors.PrivateRoomDoor"
        simple_class_name = class_name_str.split('.')[-1]
        for door_type_enum in cls:
            if door_type_enum.value == simple_class_name:
                return door_type_enum
        # Fallback or error for unknown door types
        print(f"Warning: Unknown door class name {class_name_str}. Defaulting to GENERIC_DOOR.")
        return cls.GENERIC_DOOR


if __name__ == '__main__':
    print("--- ExitDirection ---")
    print(ExitDirection.NORTH)
    print(ExitDirection.from_string("east"))
    try:
        ExitDirection.from_string("INVALID")
    except ValueError as e:
        print(e)

    print("\n--- Terrain ---")
    print(Terrain.TOWN)
    print(Terrain.from_string("forest"))
    print(Terrain.from_string("City Streets"))
    try:
        Terrain.from_string("UNKNOWN_TERRAIN")
    except ValueError as e:
        print(e)

    print("\n--- RoomFlags ---")
    flags = RoomFlags.SAFE | RoomFlags.DARK
    print(flags)
    print(f"Is SAFE set? {RoomFlags.SAFE in flags}")
    print(f"Is ARENA set? {RoomFlags.ARENA in flags}")
    print(f"Flags from int 3 (SAFE | DARK): {RoomFlags.from_int(3)}")
    print(f"Flags from int 10 (DARK | ARENA): {RoomFlags.from_int(10)}") # 2 | 8

    print("\n--- DoorType ---")
    print(DoorType.PRIVATE_ROOM)
    print(DoorType.from_class_name("org.tdod.ether.taimpl.cosmos.doors.PrivateRoomDoor"))
    print(DoorType.from_class_name("org.tdod.ether.taimpl.cosmos.doors.SomeOtherDoor"))

```
