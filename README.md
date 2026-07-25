# Questing Librarians

Questing Librarians is a Fabric mod for Minecraft 26.2 that replaces the tedious "lectern reroll" grind with an exploration-based system for getting enchanted books from villager trading.

Instead of breaking and replacing a lectern hundreds of times until you get the right trade, Librarians no longer generate random enchanted books. Instead, you find enchanted books in the world and teach them directly to Librarians, or earn a random max-level book trade by curing a Master (Level 5) Zombie Villager.

---

## Features

- **No Random Book Rerolling**: Vanilla Librarian trade pools are overridden so random enchanted books never appear. Standard items like paper, bookshelves, lanterns, glass, clocks, compasses, and candles remain intact.
- **🏷️ Found vs Traded Book Tooltips**: Hovering over ANY Enchanted Book in your inventory displays its status:
  - `✔ Can Teach Librarians (Found Book)` (in Green) for naturally found, enchanted, or looted books.
  - `✖ Cannot Teach Librarians (Traded Book)` (in Red) for books bought from villagers.
  - `★ Cured Reward Trade` (in Gold) for cured reward trade offers.
- **✨ Magical Feedback & Particles**: Teaching a Librarian a trade triggers an enchantment chime and spawns glowing enchantment glyph particles around the villager. Using a Grindstone plays a grindstone scraping sound with poof particles.
- **First-Time Interactive Tutorial**: A clean, elegant tutorial card appears the very first time you open a villager menu to explain how teaching, curing rewards, and Grindstone resets work. Click anywhere on the card to dismiss it permanently, or click the blue `[?]` button to re-open it anytime.
- **Trading GUI Book Slots Indicator**: A clean label inside the villager trading GUI displays the villager's current and maximum book slots (e.g. `Book Slots: 1/2`, `Book Slots: 2/3`, or `Book Slots: 3/4`).
- **Guaranteed Zombification**: Villagers killed by zombies are 100% guaranteed to turn into Zombie Villagers rather than dying, even on Normal or Easy difficulty.
- **Fixed Trade Prices**: Vanilla curing discounts and dynamic price shifts are disabled so trade prices remain strictly fixed at their base costs.
- **Master Curing Trade Reward**: Curing a **Level 5 (Master)** Zombie Villager adds one random max-level Enchanted Book trade directly to the villager's trade list. This reward is granted **once per villager** and only applies to Master-level villagers.
- **Dynamic Book Capacity**:
  - Levels 1 to 4: Up to 2 taught books per villager.
  - Level 5 (Master): Unlocks 3 taught book slots.
  - **Cured Master Villagers**: Expands to **4 total book slots** (1 cured trade + up to 3 taught trades).
- **Grindstone Reset & Cured Trade Protection**: Right-clicking a Librarian while holding a Grindstone wipes all player-taught Enchanted Book trades and refunds regular Books into your inventory, while **protecting the cured reward trade** from being erased.
- **Interactive Trade Learning (Found Books Only)**: Right-click a Librarian while holding a naturally found or cured Enchanted Book to teach them that trade permanently. Books bought from Librarians are marked as traded and cannot be used to teach other villagers.
- **Level-Based Discounts**: Higher level Librarians charge fewer emeralds for taught books:
  - Level 1 (Novice): 15 Emeralds
  - Level 2 (Apprentice): 13 Emeralds
  - Level 3 (Journeyman): 11 Emeralds
  - Level 4 (Expert): 9 Emeralds
  - Level 5 (Master): 7 Emeralds
- **Top of the List**: Taught and cured book trades always appear at the top of the villager's trade menu for convenience.

---

## How It Works

1. Open a villager trading screen to see the **First-Time Guide** (click anywhere on the card to dismiss it, or click `[?]` to view it again).
2. Find Enchanted Books through exploration, loot chests, enchanting, or by curing Level 5 Master Zombie Villagers.
3. Hover over Enchanted Books in your inventory to see their status: `✔ Can Teach Librarians (Found Book)` in Green vs `✖ Cannot Teach Librarians (Traded Book)` in Red.
4. Curing a **Level 5 Master Zombie Villager** generates a new, random max-level Enchanted Book trade offer directly in the villager's trade list (one-time reward per villager) and expands total capacity to **4 book slots**.
5. Open the trading GUI to see the `Book Slots: X/Y` note at the top left of the window.
6. Right-click a Librarian villager with a found book in hand to teach them the trade—enjoy the magical chime and glowing enchantment particles!
7. You can hold a Grindstone and right-click a villager to wipe all player-taught trades and get regular Books back. The Grindstone will **never** wipe cured reward trades.

---

## Building and Installation

### Requirements
- Minecraft 26.2
- Fabric Loader 0.19.3 or higher
- Fabric API

### Building from Source

To build the mod `.jar` file:

```cmd
.\gradlew.bat build
```

The compiled mod will be generated in `build/libs/questing-librarians-4.4.0.jar`.

### Installation

Drop `questing-librarians-4.4.0.jar` into your `.minecraft/mods` directory alongside Fabric API.

---

## Development and Testing

To launch the test client:

```cmd
.\gradlew.bat runClient
```

---

## License

This mod is available under the CC0-1.0 license. Feel free to use, modify, or include it in modpacks.
