from typing import List

class Stat:
    def __init__(self, value: int = 0, boost: int = 0, drain: int = 0, boost_timer: int = 0, drain_timer: int = 0):
        self._value: int = value  # Base value from classes.dat or character creation
        self.boost: int = boost
        self.drain: int = drain
        self.boost_timer: int = boost_timer
        self.drain_timer: int = drain_timer
        # Other potential attributes like racial_modifier, item_modifier, spell_modifier etc. can be added later

    def get_value(self) -> int:
        """Returns the base value of the stat."""
        return self._value

    def set_value(self, value: int) -> None:
        """Sets the base value of the stat. Used by loader."""
        self._value = value

    def get_modified_stat(self) -> int:
        """Calculates the stat including temporary boosts and drains."""
        # This will become more complex with equipment, spells, racial bonuses etc.
        return self._value + self.boost - self.drain

    def decrease_timers(self) -> None:
        """Decreases timers for boosts and drains. Resets if timer reaches zero."""
        if self.boost_timer > 0:
            self.boost_timer -= 1
            if self.boost_timer == 0:
                self.boost = 0
        if self.drain_timer > 0:
            self.drain_timer -= 1
            if self.drain_timer == 0:
                self.drain = 0

    def reset_enchants(self) -> None:
        """Resets temporary modifications like boosts and drains."""
        self.boost = 0
        self.drain = 0
        self.boost_timer = 0
        self.drain_timer = 0

    def __repr__(self) -> str:
        return f"{self.__class__.__name__}(Value: {self._value}, Modified: {self.get_modified_stat()})"


# --- Primary Stat Classes ---
# They can hold bonus tables as class attributes if needed later.
# For now, they are simple extensions of Stat.

class Intellect(Stat):
    # Example bonus table (not used by classes.dat loading directly)
    _melee_hit_bonus: List[int] = [
        -5, -4, -4, -3, -3, -2, -2, -1, -1, 0, 0, 0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6
    ] # For stat values 0-25
    pass

class Knowledge(Stat):
    # Example: _spell_damage_bonus
    pass

class Physique(Stat):
    # Example: _carry_capacity_bonus, _hp_regen_bonus
    _hp_per_level: List[int] = [ # Based on physique score 0-25
        1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 10 # Example values
    ]
    pass

class Stamina(Stat):
    # Example: _fatigue_recovery_bonus
    _stam_per_level: List[int] = [ # Based on stamina score 0-25
        1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 10 # Example values
    ]
    pass

class Agility(Stat):
    # Example: _defense_bonus, _dodge_chance_bonus
    _ac_bonus: List[int] = [ # Bonus to Armor Class, example values
        -30, -25, -20, -15, -12, -10, -8, -6, -4, -2, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30
    ]
    pass

class Charisma(Stat):
    # Example: _merchant_price_modifier, _reaction_bonus
    _reaction_adj: List[int] = [ # Reaction adjustment with NPCs
        -5, -4, -4, -3, -3, -2, -2, -1, -1, 0, 0, 0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6
    ]
    pass


class BaseStats:
    def __init__(self):
        self._intellect: Intellect = Intellect()
        self._knowledge: Knowledge = Knowledge()
        self._physique: Physique = Physique()
        self._stamina: Stamina = Stamina()
        self._agility: Agility = Agility()
        self._charisma: Charisma = Charisma()

    def get_intellect(self) -> Intellect:
        return self._intellect

    def get_knowledge(self) -> Knowledge:
        return self._knowledge

    def get_physique(self) -> Physique:
        return self._physique

    def get_stamina(self) -> Stamina:
        return self._stamina

    def get_agility(self) -> Agility:
        return self._agility

    def get_charisma(self) -> Charisma:
        return self._charisma

    def __repr__(self) -> str:
        return (
            f"BaseStats(\n"
            f"  Intellect: {self._intellect.get_value()},\n"
            f"  Knowledge: {self._knowledge.get_value()},\n"
            f"  Physique: {self._physique.get_value()},\n"
            f"  Stamina: {self._stamina.get_value()},\n"
            f"  Agility: {self._agility.get_value()},\n"
            f"  Charisma: {self._charisma.get_value()}\n"
            f")"
        )

if __name__ == '__main__':
    # Basic test for Stat
    my_stat = Stat(value=10)
    print(my_stat)
    my_stat.boost = 5
    my_stat.boost_timer = 2
    print(f"Modified stat: {my_stat.get_modified_stat()}") # Should be 15
    my_stat.decrease_timers()
    print(f"After 1 tick: {my_stat.get_modified_stat()}, boost_timer: {my_stat.boost_timer}") # 15, 1
    my_stat.decrease_timers()
    print(f"After 2 ticks: {my_stat.get_modified_stat()}, boost_timer: {my_stat.boost_timer}") # 10, 0 (boost removed)
    my_stat.boost = 3
    my_stat.reset_enchants()
    print(f"After reset: {my_stat.get_modified_stat()}, boost: {my_stat.boost}") # 10, 0

    # Test BaseStats
    base_stats = BaseStats()
    base_stats.get_intellect().set_value(12)
    base_stats.get_physique().set_value(15)
    print(base_stats)
    print(f"Intellect value: {base_stats.get_intellect().get_value()}")
    print(f"Physique (modified): {base_stats.get_physique().get_modified_stat()}")
    # Example of accessing a class attribute (bonus table)
    print(f"Intellect melee hit bonus for 10 INT: {Intellect._melee_hit_bonus[10]}")
    print(f"Physique HP per level for 15 PHYS: {Physique._hp_per_level[15]}")

```
