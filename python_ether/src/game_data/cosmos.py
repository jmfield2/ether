import os
import xml.etree.ElementTree as ET # Using standard library ElementTree for now. lxml.etree can be swapped in.
from dataclasses import dataclass, field
from typing import List, Dict, Any, Optional

from .cosmos_enums import ExitDirection, Terrain, RoomFlags, DoorType
from .doors import Door, PrivateRoomDoor, HasRuneDoor, LevelRequirementDoor, KeyRequirementDoor, create_door_from_type

# If lxml is strictly required and available:
# import lxml.etree as ET

@dataclass
class ExitData:
    to_room_id: int
    direction: ExitDirection
    door: Optional[Door] = None

@dataclass
class RoomData:
    room_id: int
    default_description_id: int = 0
    alt_description_id: int = 0 # Often 0 if not used
    room_flags: RoomFlags = RoomFlags.NONE
    service_level: int = 0 # Meaning depends on game logic (e.g. shop level, inn quality)
    exits: List[ExitData] = field(default_factory=list)
    terrain: Optional[Terrain] = None # Should always be set from XML
    npc_ids: List[int] = field(default_factory=list)
    lair_ids: List[Any] = field(default_factory=list) # Placeholder for Lair objects/IDs
    trigger_ids: List[Any] = field(default_factory=list) # Placeholder for Trigger objects/IDs

    # Runtime populated fields (not directly from area XML, but via RoomDescriptionDatabase)
    short_description: str = ""
    long_description: str = "" # Usually default_description_id's content

    def __repr__(self) -> str:
        return (f"RoomData(id={self.room_id}, def_desc={self.default_description_id}, "
                f"flags={self.room_flags.name if self.room_flags else 'NONE'}, terrain={self.terrain.name if self.terrain else 'NONE'}, "
                f"num_exits={len(self.exits)}, num_npcs={len(self.npc_ids)})")


@dataclass
class AreaData:
    name: str
    rooms: Dict[int, RoomData] = field(default_factory=dict)

    def __repr__(self) -> str:
        return f"AreaData(name='{self.name}', num_rooms={len(self.rooms)})"

# Helper to get text from an XML element, returning a default if not found or empty
def get_element_text_or_default(element: Optional[ET.Element], tag: str, default: str = "") -> str:
    if element is None: return default
    child = element.find(tag)
    return child.text if child is not None and child.text is not None else default

def get_element_int_or_default(element: Optional[ET.Element], tag: str, default: int = 0) -> int:
    text = get_element_text_or_default(element, tag, "")
    return int(text) if text.isdigit() or (text.startswith('-') and text[1:].isdigit()) else default


def load_area_xml(xml_file_path: str) -> Optional[AreaData]:
    """
    Parses an area XML file (e.g., town.xml) and returns an AreaData object.
    """
    try:
        tree = ET.parse(xml_file_path)
        root = tree.getroot() # Should be <org.tdod.ether.taimpl.cosmos.DefaultArea>
    except ET.ParseError as e:
        print(f"Error parsing XML file {xml_file_path}: {e}")
        return None
    except FileNotFoundError:
        print(f"Error: Area XML file not found at {xml_file_path}")
        return None

    area_name_from_file = os.path.splitext(os.path.basename(xml_file_path))[0]
    # The XML might also contain a __name field for the area, prefer that if available
    # <__name>Town</__name>
    area_name_from_xml = get_element_text_or_default(root, "__name", area_name_from_file)
    area_data = AreaData(name=area_name_from_xml)

    rooms_element = root.find("__rooms")
    if rooms_element is None:
        print(f"Warning: No __rooms element found in {xml_file_path}")
        return area_data # Return area with no rooms

    for entry_element in rooms_element.findall("entry"):
        room_id_element = entry_element.find("int")
        if room_id_element is None or room_id_element.text is None:
            print(f"Warning: Skipping room entry with missing ID in {xml_file_path}")
            continue

        room_id = int(room_id_element.text)
        room_content_element = entry_element.find("org.tdod.ether.taimpl.cosmos.DefaultRoom")
        if room_content_element is None:
            print(f"Warning: Skipping room ID {room_id} due to missing DefaultRoom content in {xml_file_path}")
            continue

        room = RoomData(room_id=room_id)
        room.default_description_id = get_element_int_or_default(room_content_element, "__defaultDescription")
        room.alt_description_id = get_element_int_or_default(room_content_element, "__altDescription")

        room_flags_int = get_element_int_or_default(room_content_element, "__roomFlags")
        room.room_flags = RoomFlags.from_int(room_flags_int) # RoomFlags(room_flags_int) also works

        room.service_level = get_element_int_or_default(room_content_element, "__serviceLevel")

        terrain_str = get_element_text_or_default(room_content_element, "__terrain")
        if terrain_str:
            try:
                room.terrain = Terrain.from_string(terrain_str)
            except ValueError as e:
                print(f"Warning: Room {room_id} in {area_data.name} - {e}. Setting terrain to None.")
                room.terrain = None # Or a default like Terrain.NONE
        else:
            room.terrain = None # Or Terrain.NONE

        # Parse Exits
        exits_element = room_content_element.find("__exits")
        if exits_element:
            for exit_xml_element in exits_element.findall("org.tdod.ether.taimpl.cosmos.DefaultExit"):
                to_room_id = get_element_int_or_default(exit_xml_element, "__toRoom")
                direction_str = get_element_text_or_default(exit_xml_element, "__exitDirection")

                try:
                    exit_direction_enum = ExitDirection.from_string(direction_str)
                except ValueError as e:
                    print(f"Warning: Room {room_id} in {area_data.name} - {e} for an exit. Skipping exit.")
                    continue

                # Parse Door
                door_object = None
                door_element = exit_xml_element.find("__door")
                if door_element:
                    # The actual door class is an attribute of the __door element itself in some XStream versions
                    # <__door class="org.tdod.ether.taimpl.cosmos.doors.PrivateRoomDoor"> or nested inside.
                    # Let's assume it's an attribute on __door for now, or a child tag named 'class'
                    door_class_name = door_element.get("class") # Check attribute first
                    if not door_class_name: # Check for a child element <class>
                         class_tag = door_element.find("class")
                         if class_tag is not None:
                            door_class_name = class_tag.text

                    if door_class_name:
                        try:
                            door_type_enum = DoorType.from_class_name(door_class_name)
                            v0 = get_element_int_or_default(door_element, "__v0")
                            v3 = get_element_int_or_default(door_element, "__v3") # Note: XStream uses __v1, __v2 etc.
                            v4 = get_element_int_or_default(door_element, "__v4") # but prompt used v0,v3,v4,v5,v6
                            v5 = get_element_int_or_default(door_element, "__v5") # Adjusting to convention if needed
                            v6 = get_element_int_or_default(door_element, "__v6")
                            door_object = create_door_from_type(door_type_enum, v0, v3, v4, v5, v6)
                        except ValueError as e:
                            print(f"Warning: Room {room_id} in {area_data.name}, Exit {direction_str} - {e}. No door created.")
                    else:
                        print(f"Warning: Room {room_id} in {area_data.name}, Exit {direction_str} - Door element present but no class found.")


                exit_data = ExitData(to_room_id=to_room_id, direction=exit_direction_enum, door=door_object)
                room.exits.append(exit_data)

        # Parse NPCs
        npcs_element = room_content_element.find("__npcs")
        if npcs_element:
            for npc_id_element in npcs_element.findall("int"):
                if npc_id_element.text and npc_id_element.text.isdigit():
                    room.npc_ids.append(int(npc_id_element.text))

        # Placeholders for Lairs and Triggers
        # lairs_element = room_content_element.find("__lairs")
        # if lairs_element: # ... parse lair data ...
        # triggers_element = room_content_element.find("__triggers")
        # if triggers_element: # ... parse trigger data ...

        area_data.rooms[room_id] = room

    return area_data


if __name__ == '__main__':
    # Setup path assuming this script is in python_ether/src/game_data/
    script_dir = os.path.dirname(os.path.abspath(__file__))
    # Project root is two levels up from .../src/game_data/
    project_root = os.path.abspath(os.path.join(script_dir, "..", ".."))
    area_xml_file = os.path.join(project_root, "area", "town.xml") # Example area

    print(f"Attempting to load area data from: {area_xml_file}")

    if not os.path.exists(area_xml_file):
        print(f"Test Error: Area XML file not found at {area_xml_file}")
        # Fallback for typical execution from /app directory
        fallback_path = "/app/python_ether/area/town.xml"
        if os.path.exists(fallback_path):
            print(f"Trying fallback path: {fallback_path}")
            area_xml_file = fallback_path
        else:
            print("Fallback path also not found. Test cannot proceed.")
            exit(1)

    town_area = load_area_xml(area_xml_file)

    if town_area:
        print(f"\nLoaded Area: {town_area.name}")
        print(f"Number of rooms: {len(town_area.rooms)}")

        # Find a specific room to display, e.g., room 1 (Town Square typically)
        # Room IDs in XML might not start from 1 or be contiguous.
        # Let's try to find a known room ID from town.xml or just pick one if available.
        # From looking at a typical town.xml, room ID 1 is common for Town Square.
        example_room_id = 1
        if not town_area.rooms:
            print("No rooms loaded to display.")
        elif example_room_id not in town_area.rooms:
            # If room 1 isn't there, pick the first one loaded for demonstration
            example_room_id = next(iter(town_area.rooms))
            print(f"Room 1 not found, displaying first available room: {example_room_id}")

        if example_room_id in town_area.rooms:
            test_room = town_area.rooms[example_room_id]
            print(f"\n--- Details for Room {test_room.room_id} ---")
            print(f"  Default Description ID: {test_room.default_description_id}")
            print(f"  Room Flags: {test_room.room_flags} (Value: {test_room.room_flags.value})")
            print(f"  Terrain: {test_room.terrain.name if test_room.terrain else 'None'}")
            print(f"  NPC IDs: {test_room.npc_ids}")
            print(f"  Exits ({len(test_room.exits)}):")
            for ex_data in test_room.exits:
                door_info = f", Door: {ex_data.door}" if ex_data.door else ""
                print(f"    - Direction: {ex_data.direction.name}, To Room ID: {ex_data.to_room_id}{door_info}")
        else:
            print(f"Could not find room {example_room_id} or any room to display.")

        # Example: Find a room with a door, if any
        for r_id, r_data in town_area.rooms.items():
            for ex in r_data.exits:
                if ex.door:
                    print(f"\n--- Room {r_id} has an exit with a door ---")
                    print(f"  Exit to {ex.to_room_id} ({ex.direction.name}) has door: {ex.door}")
                    break
            if any(ex.door for ex in r_data.exits):
                break
    else:
        print("Failed to load area data.")

```
