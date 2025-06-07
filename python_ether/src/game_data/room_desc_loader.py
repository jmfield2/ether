import os
import xml.etree.ElementTree as ET # Using standard library ElementTree
from typing import Dict, Optional, List

# If lxml is strictly required and available:
# import lxml.etree as ET

class RoomDescriptionDatabase:
    def __init__(self, area_dir_path: str, room_desc_list_file: str = "room_desc.lst"):
        self.area_dir_path: str = area_dir_path
        self.room_desc_list_file: str = room_desc_list_file # e.g., "room_desc.lst"
        self._descriptions: Dict[int, str] = {}

    def load(self) -> bool:
        """
        Loads room descriptions from XML files listed in the room_desc_list_file.
        Returns True if loading was successful for at least one file, False otherwise.
        """
        list_file_full_path = os.path.join(self.area_dir_path, self.room_desc_list_file)

        if not os.path.exists(list_file_full_path):
            print(f"Error: Room description list file not found at {list_file_full_path}")
            return False

        loaded_at_least_one_file = False
        try:
            with open(list_file_full_path, 'r') as f_list:
                for desc_xml_filename in f_list:
                    desc_xml_filename = desc_xml_filename.strip()
                    if not desc_xml_filename or desc_xml_filename.startswith('#'):
                        continue

                    desc_xml_full_path = os.path.join(self.area_dir_path, desc_xml_filename)
                    if not os.path.exists(desc_xml_full_path):
                        print(f"Warning: Description XML file '{desc_xml_full_path}' listed in '{self.room_desc_list_file}' not found. Skipping.")
                        continue

                    try:
                        tree = ET.parse(desc_xml_full_path)
                        root = tree.getroot() # <org.tdod.ether.taimpl.cosmos.DefaultRoomDescriptions>

                        # The structure is <__roomDescriptions class="org.tdod.ether.util.LongStringHashMap"> then <entry>...
                        # or sometimes directly <entry> under root if simplified.
                        # Let's find __roomDescriptions first.
                        data_container = root.find("__roomDescriptions")
                        if data_container is None:
                            # If not found, perhaps entries are direct children of root for some files
                            data_container = root
                            # print(f"Warning: No '__roomDescriptions' element in {desc_xml_full_path}. Trying root.")
                            # If still no entries, this file might be empty or malformed for descriptions

                        for entry_element in data_container.findall("entry"):
                            id_element = entry_element.find("int")
                            text_element = entry_element.find("string")

                            if id_element is not None and id_element.text is not None and \
                               text_element is not None and text_element.text is not None:
                                try:
                                    desc_id = int(id_element.text)
                                    desc_text = text_element.text
                                    self._descriptions[desc_id] = desc_text
                                except ValueError:
                                    print(f"Warning: Invalid integer ID '{id_element.text}' in {desc_xml_full_path}. Skipping entry.")
                                    continue
                            else:
                                print(f"Warning: Incomplete entry (missing ID or string) in {desc_xml_full_path}. Skipping entry.")

                        print(f"Successfully parsed {desc_xml_full_path}")
                        loaded_at_least_one_file = True

                    except ET.ParseError as e:
                        print(f"Error parsing XML file {desc_xml_full_path}: {e}")
                        continue # continue to next file in list
                    except Exception as e:
                        print(f"An unexpected error occurred while processing {desc_xml_full_path}: {e}")
                        continue


            if loaded_at_least_one_file:
                print(f"Finished loading room descriptions. Total unique descriptions: {len(self._descriptions)}")
                return True
            else:
                print(f"No room description files were successfully loaded from {list_file_full_path}.")
                return False

        except IOError as e:
            print(f"Error reading room description list file {list_file_full_path}: {e}")
            return False

    def get_description(self, desc_id: int) -> Optional[str]:
        """
        Retrieves a room description by its ID.
        Returns the description string or None if not found.
        """
        return self._descriptions.get(desc_id)

if __name__ == '__main__':
    # Setup path assuming this script is in python_ether/src/game_data/
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, "..", ".."))
    area_directory = os.path.join(project_root, "area")

    print(f"Attempting to load room descriptions from directory: {area_directory}")

    # Check if area directory and room_desc.lst exist
    room_desc_list_path = os.path.join(area_directory, "room_desc.lst")
    if not os.path.exists(area_directory) or not os.path.exists(room_desc_list_path):
        print(f"Test Error: Area directory '{area_directory}' or list file '{room_desc_list_path}' not found.")
        # Fallback for typical execution from /app directory
        fallback_area_dir = "/app/python_ether/area"
        fallback_list_path = "/app/python_ether/area/room_desc.lst"
        if os.path.exists(fallback_area_dir) and os.path.exists(fallback_list_path):
            print(f"Trying fallback path: {fallback_area_dir}")
            area_directory = fallback_area_dir
        else:
            print("Fallback path also not found. Test cannot proceed.")
            exit(1)

    desc_db = RoomDescriptionDatabase(area_dir_path=area_directory)
    if desc_db.load():
        print("\n--- Room Description Verification ---")

        # Try to get some known description IDs. These IDs are area-specific.
        # For town.xml (which often uses town_room_desc.xml), ID 1 might be Town Square.
        # We'd need to know which description IDs are used by town.xml's rooms.
        # For now, just print a few available ones if any.

        if not desc_db._descriptions:
            print("No descriptions were loaded.")
        else:
            print(f"Total descriptions loaded: {len(desc_db._descriptions)}")

            # Print the first few loaded descriptions as examples
            count = 0
            for desc_id, text in desc_db._descriptions.items():
                if count < 3: # Print up to 3 examples
                    print(f"  ID {desc_id}: '{text[:100]}...'") # Print first 100 chars
                    count += 1
                else:
                    break

            # Example: Try to get a specific ID if we know one (e.g. from town.xml's Room 1)
            # This requires knowing what defaultDescriptionId room 1 in town.xml has.
            # Let's assume for testing that description ID 1 exists from town_room_desc.xml
            test_desc_id = 1
            description = desc_db.get_description(test_desc_id)
            if description:
                print(f"\n  Successfully retrieved description for ID {test_desc_id}: '{description[:100]}...'")
            else:
                print(f"\n  Description for ID {test_desc_id} not found (this may be normal if it's not in the loaded files or uses a different ID).")

    else:
        print("Failed to load any room descriptions.")

```
