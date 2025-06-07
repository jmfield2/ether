from typing import Optional
from .player_enums import PlayerClass # Relative import
from .stats import BaseStats         # Relative import

class PlayerClassData:
    def __init__(self):
        self.player_class: Optional[PlayerClass] = None
        self.stat_modifiers: BaseStats = BaseStats() # These are the base stats for the class at level 1
        self.vitality: int = 0          # Base HP
        self.weapon: int = 0            # Starting weapon item ID
        self.armor: int = 0             # Starting armor item ID (sum of all pieces)
        self.attacks_per_level: int = 0 # How many levels per new attack (e.g. 5 means 1 new attack at L5, L10...)
                                        # Or it could be a direct multiplier/divider for attacks.
                                        # The Java code suggests "attacksPerLevel" might be a misnomer
                                        # and it's actually related to max attacks.
                                        # For now, I'll assume it's related to gaining additional attacks.
                                        # From PlayerClass.java: `attacksPerLevel` seems to be `maxAttacksAtFirstLevel`
                                        # and `maxBaseAttacks` is `maxAttacksAtMaxLevel`.

        self.max_attacks_at_first_level: int = 0 # Renamed from attacks_per_level for clarity based on Java
        self.max_base_attacks: int = 0           # Max attacks at highest level for this class

        self.min_starting_gold: int = 0
        self.max_starting_gold: int = 0
        # skills: Map<Skill, Integer> in Java - this will be handled later
        # defaultSpells: List<Spell> in Java - this will be handled later

    def __repr__(self) -> str:
        return (
            f"<PlayerClassData Class: {self.player_class.class_name if self.player_class else 'None'}, "
            f"Stats: {self.stat_modifiers}, Vit: {self.vitality}, Weap: {self.weapon}, Armor: {self.armor}, "
            f"AttacksL1: {self.max_attacks_at_first_level}, MaxAttacks: {self.max_base_attacks}, "
            f"Gold: {self.min_starting_gold}-{self.max_starting_gold}>"
        )

if __name__ == '__main__':
    # Example Usage
    pcd = PlayerClassData()
    pcd.player_class = PlayerClass.WARRIOR
    pcd.stat_modifiers.get_intellect().set_value(10)
    pcd.stat_modifiers.get_physique().set_value(18)
    pcd.vitality = 25
    pcd.weapon = 101 # Example weapon ID
    pcd.armor = 201 # Example armor ID
    pcd.max_attacks_at_first_level = 2
    pcd.max_base_attacks = 5
    pcd.min_starting_gold = 50
    pcd.max_starting_gold = 150
    print(pcd)

    # Test with unset PlayerClass (should not crash)
    pcd_no_class = PlayerClassData()
    print(pcd_no_class)

```
