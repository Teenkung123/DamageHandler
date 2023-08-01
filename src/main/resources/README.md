# Damage Handler

------------

###### A Minecraft Plugin that modifies Minecraft combat mechanics by handle all types of damage and adding elemental systems and elemental reaction systems into the game's combat system.

## Commands

---

- `/damagehandle reload` reload plugin configuration

## MythicMobs Mechanics

---


### elemental_damage
Deal elemental damage or physical damage


| Attribute  | Aliases | Data Type | Description           | Default |
|------------|---------|-----------|-----------------------|---------|
| amount     | a       | Double    | Amount of damage      |         | 
| element    |         | String    | Element of the damage |         | 
| gauge_unit | gu      | Double    | Aura gauge unit       | 1       |

Examples:
```yml
test:
  Skills:
  - elemental_damage{amount=10;element=GEO;gu=1} @Target
```

### elemental_shield
Create elemental shields according to the specified quantity

| Attribute | Aliases | Data Type | Description                               | Default |
|-----------|---------|-----------|-------------------------------------------|---------|
| amount    | a       | Double    | Quantity of the elemental shield          |         | 
| element   |         | String    | Element of the shield                     |         | 
| duration  |         | Long      | Duration of the elemental shield in ticks |         |

Examples:
```yml
test:
  Skills:
  - elemental_shield{amount=10;element=GEO;duration=100} @Target # apply geo shield for 5 seconds
```

### reduce_defense
Reduce defensive power according to the specified quantity and duration

| Attribute | Aliases | Data Type | Description                             | Default |
|-----------|---------|-----------|-----------------------------------------|---------|
| amount    | a       | Double    | The amount of defense to be reduced (%) |         | 
| duration  |         | Long      | The duration for the reduction          |         |

Examples:
```yml
test:
  Skills:
  - reduce_defense{amount=10;duration=100} @Target # reduce 10 defense for 5 seconds
```

### reduce_resistance
Reduce the resistance of the specified element

| Attribute | Aliases | Data Type | Description                                | Default |
|-----------|---------|-----------|--------------------------------------------|---------|
| amount    | a       | Double    | The amount of resistance to be reduced (%) |         | 
| element   |         | String    | The element of resistance                  |         |
| duration  |         | Long      | The duration for the reduction             |         |

Examples:
```yml
test:
  Skills:
  - reduce_resistance{amount=50;element=ANEMO;duration=100} @Target # reduce 50% of anemo resistance
```

## Installations

---

