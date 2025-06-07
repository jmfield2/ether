import os
from typing import Dict, Optional

from .player_enums import PlayerClass
from .player_class_data import PlayerClassData
from .stats import BaseStats # Though not directly used here, good for context

# Expected structure of classes.dat (based on typical MUDs and the 14 fields):
# 0: Class Index (matches PlayerClass enum index)
# 1: Intellect
# 2: Knowledge
# 3: Physique
# 4: Stamina
# 5: Agility
# 6: Charisma
# 7: Vitality (Base HP)
# 8: Weapon (Starting Weapon ID)
# 9: Armor (Starting Armor ID)
# 10: AttacksPerLevel (Max attacks at 1st level) - field 10
# 11: MaxBaseAttacks (Max attacks at max level) - field 11
# 12: MinGold
# 13: MaxGold

class PlayerClassDatabase:
    def __init__(self, classes_dat_path: str):
        self.classes_dat_path = classes_dat_path
        self._player_class_data_map: Dict[PlayerClass, PlayerClassData] = {}

    def load(self) -> bool:
        """
        Loads player class data from the classes.dat file.
        Returns True if loading was successful, False otherwise.
        """
        if not os.path.exists(self.classes_dat_path):
            print(f"Error: classes.dat file not found at {self.classes_dat_path}")
            return False

        try:
            with open(self.classes_dat_path, 'r') as f:
                for line_number, line in enumerate(f, 1):
                    line = line.strip()
                    if not line or line.startswith('#'): # Skip empty lines and comments
                        continue

                    parts = line.split()
                    if len(parts) != 14:
                        print(f"Warning: Malformed line {line_number} in {self.classes_dat_path}. Expected 14 fields, got {len(parts)}. Line: '{line}'")
                        continue

                    try:
                        class_idx = int(parts[0])

                        player_class_enum = PlayerClass.get_by_index(class_idx)
                        if not player_class_enum: # Should raise ValueError in get_by_index if not found
                            # This check is redundant if get_by_index raises error, but good for safety.
                            print(f"Warning: Unknown class index {class_idx} on line {line_number}. Skipping.")
                            continue

                        data = PlayerClassData()
                        data.player_class = player_class_enum

                        # Stats (parts[1] to parts[6])
                        data.stat_modifiers.get_intellect().set_value(int(parts[1]))
                        data.stat_modifiers.get_knowledge().set_value(int(parts[2]))
                        data.stat_modifiers.get_physique().set_value(int(parts[3]))
                        data.stat_modifiers.get_stamina().set_value(int(parts[4]))
                        data.stat_modifiers.get_agility().set_value(int(parts[5]))
                        data.stat_modifiers.get_charisma().set_value(int(parts[6]))

                        # Other data
                        data.vitality = int(parts[7])
                        data.weapon = int(parts[8])
                        data.armor = int(parts[9])
                        # Renamed for clarity based on PlayerClassData field names
                        data.max_attacks_at_first_level = int(parts[10])
                        data.max_base_attacks = int(parts[11])
                        data.min_starting_gold = int(parts[12])
                        data.max_starting_gold = int(parts[13])

                        self._player_class_data_map[player_class_enum] = data

                    except ValueError as ve:
                        print(f"Warning: Error parsing data on line {line_number}: {ve}. Line: '{line}'. Skipping.")
                        continue
                    except IndexError:
                        # This case should be caught by len(parts) check, but as a safeguard.
                        print(f"Warning: Not enough fields on line {line_number}. Line: '{line}'. Skipping.")
                        continue

            print(f"Successfully loaded {len(self._player_class_data_map)} classes from {self.classes_dat_path}")
            return True

        except IOError as e:
            print(f"Error reading file {self.classes_dat_path}: {e}")
            return False
        except Exception as ex: # Catch any other unexpected errors during loading
            print(f"An unexpected error occurred during loading of {self.classes_dat_path}: {ex}")
            return False


    def get_player_class_data(self, player_class: PlayerClass) -> Optional[PlayerClassData]:
        """
        Retrieves the PlayerClassData for a given PlayerClass enum.
        Returns None if the class data is not found.
        """
        return self._player_class_data_map.get(player_class)

if __name__ == '__main__':
    # Determine the path to classes.dat relative to this script
    # This assumes the script is run from python_ether/src/game_data/ or similar
    # For a robust test, use an absolute path or more sophisticated relative path logic

    # Assuming the script is in python_ether/src/game_data
    # then project_root is ../../
    # then classes_dat is project_root / data / classes.dat

    # More robust way to find project root if script is in known location:
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, "..", ".."))
    classes_dat_file = os.path.join(project_root, "data", "classes.dat")

    print(f"Attempting to load classes.dat from: {classes_dat_file}")

    if not os.path.exists(classes_dat_file):
        print(f"Test Error: classes.dat not found at expected location: {classes_dat_file}")
        print("Please ensure classes.dat exists in python_ether/data/")
        # Try a fallback for typical execution from /app directory
        fallback_path = "/app/python_ether/data/classes.dat"
        if os.path.exists(fallback_path):
            print(f"Trying fallback path: {fallback_path}")
            classes_dat_file = fallback_path
        else:
            print("Fallback path also not found. Test cannot proceed.")
            exit(1)


    db = PlayerClassDatabase(classes_dat_file)
    if db.load():
        print("\n--- Verification ---")
        warrior_data = db.get_player_class_data(PlayerClass.WARRIOR)
        if warrior_data:
            print("\nWARRIOR Data:")
            print(f"  Class Name: {warrior_data.player_class.class_name}")
            print(f"  Intellect: {warrior_data.stat_modifiers.get_intellect().get_value()}")
            print(f"  Knowledge: {warrior_data.stat_modifiers.get_knowledge().get_value()}")
            print(f"  Physique: {warrior_data.stat_modifiers.get_physique().get_value()}")
            print(f"  Stamina: {warrior_data.stat_modifiers.get_stamina().get_value()}")
            print(f"  Agility: {warrior_data.stat_modifiers.get_agility().get_value()}")
            print(f"  Charisma: {warrior_data.stat_modifiers.get_charisma().get_value()}")
            print(f"  Vitality: {warrior_data.vitality}")
            print(f"  Weapon ID: {warrior_data.weapon}")
            print(f"  Armor ID: {warrior_data.armor}")
            print(f"  Attacks at L1: {warrior_data.max_attacks_at_first_level}")
            print(f"  Max Base Attacks: {warrior_data.max_base_attacks}")
            print(f"  Min Gold: {warrior_data.min_starting_gold}")
            print(f"  Max Gold: {warrior_data.max_starting_gold}")
        else:
            print("\nWARRIOR data not found!")

        sorceror_data = db.get_player_class_data(PlayerClass.SORCEROR) # Index 10
        if sorceror_data:
            print("\nSORCEROR Data:")
            print(f"  Class Name: {sorceror_data.player_class.class_name}")
            print(f"  Intellect: {sorceror_data.stat_modifiers.get_intellect().get_value()}")
            print(f"  Vitality: {sorceror_data.vitality}")
            # Example of specific stat
            print(f"  Sorceror Knowledge: {sorceror_data.stat_modifiers.get_knowledge().get_value()}")
        else:
            print("\nSORCEROR data not found!")

        mage_data = db.get_player_class_data(PlayerClass.MAGE) # Index 3
        if mage_data:
            print("\nMAGE Data:")
            print(f"  Class Name: {mage_data.player_class.class_name}")
            print(f"  Intellect: {mage_data.stat_modifiers.get_intellect().get_value()}")
            print(f"  Vitality: {mage_data.vitality}")
        else:
            print("\nMAGE data not found!")

        print(f"\nTotal classes loaded: {len(db._player_class_data_map)}")
        # Check if all defined PlayerClass members were loaded (if they exist in classes.dat)
        for pc_enum_member in PlayerClass:
            if pc_enum_member not in db._player_class_data_map:
                print(f"Data for class {pc_enum_member.class_name} (Index: {pc_enum_member.index}) not found in classes.dat or failed to load.")

    else:
        print("Failed to load classes.dat.")

```
