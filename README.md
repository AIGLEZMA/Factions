# Factions

[Factions](https://github.com/drtshock/Factions) fork, it's not mean to be in production

## APIs

I've added some events:

- FactionHeartPreSetEvent: Fired when a faction heart is about to be placed
- FactionHeartSetEvent: Fired when a faction heart is placed (Post FactionHeartPreSetEvent)
- FactionHeartHealthChangeEvent: Fired when a faction heart's health has increased or decreased
- FactionHeartRegenItemBoughtEvent

There is also a **HeartDamageProvider**, so any plugin can override the default provider
using `FactionsPlugin#setHeartDamageProvider`, the provider is used to calculate the amount of damage the attacked
faction will get see `HeartDamageProvider#provide`

Example:

```kotlin
class CustomHeartDamageProvider : HeartDamageProvider() {

    /**
     * Return a random damage between 0 and 10
     */
    override fun provide(damager: FPlayer, damaged: Faction): Double {
        return ThreadLocalRandom.current().nextDouble(10.0)
    }

}

class Main : JavaPlugin() {

    @Override
    fun onEnable() {
        // stuff
        FactionsPlugin.getInstance().heartDamageProvider = CustomHeartDamageProvider()
    }

}
```