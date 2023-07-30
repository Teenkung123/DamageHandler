# Damage Handler

## Commands

- `/damagehandle reload` reload plugin config

## MythicMobs Mechanics


### elemental_damage
Deal elemental damage or physical damage


| Attribute | Aliases | Description           | Default |
|-----------|---------|-----------------------|---------|
| amount    | a       | Amount of damage      |         | 
| element   |         | Element of the damage |         | 

Examples:
```yml
test:
  Skills:
  - elemental_damage{amount=10;element=GEO} @Target
```

### elemental_shield
Create elemental shields according to the specified quantity

| Attribute | Aliases | Description                               | Default |
|-----------|---------|-------------------------------------------|---------|
| amount    | a       | Quantity of the elemental shield          |         | 
| element   |         | Element of the shield                     |         | 
| duration  |         | Duration of the elemental shield in ticks |         |

Examples:
```yml
test:
  Skills:
  - elemental_shield{amount=10;element=GEO;duration=100} @Target # apply geo shield for 5 seconds
```

### reduce_defense
Reduce defensive power according to the specified quantity and duration

| Attribute | Aliases | Description                             | Default |
|-----------|---------|-----------------------------------------|---------|
| amount    | a       | The amount of defense to be reduced     |         | 
| duration  |         | The duration for the reduction          |         |

Examples:
```yml
test:
  Skills:
  - reduce_defense{amount=10;duration=100} @Target # reduce 10 defense for 5 seconds
```

### reduce_resistance
Reduce the resistance of the specified element

| Attribute | Aliases | Description                                | Default |
|-----------|---------|--------------------------------------------|---------|
| amount    | a       | The amount of resistance to be reduced (%) |         | 
| element   |         | The element of resistance                  |         |
| duration  |         | The duration for the reduction             |         |

Examples:
```yml
test:
  Skills:
  - reduce_defense{amount=50;element=ANEMO;duration=100} @Target # reduce 50% of anemo resistance
```

