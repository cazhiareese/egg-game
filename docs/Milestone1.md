# Milestone 1: Core Gameplay Loop & Environment Initialization

This document summarizes the progress achieved in the first milestone of the **egg-game** project. The foundation for the 2D top-down game built with JavaFX has been established, focusing on the core mechanics, entity interactions, and map rendering.

## 1. Project Architecture & Setup
- Built a standard JavaFX project structure managed by Maven (`pom.xml`).
- Structured code into logical packages: `entities`, `map`, `rules`, and `scene`.
- `App.java` serves as the entry point, while `SceneManager` handles scene transitions and unified keyboard input tracking.

## 2. Map & Environment Rendering
- Implemented a `Farm` class that acts as the primary game world.
- Configured a dynamic tile-based map system that parses `map_layout.txt` to draw the farm's terrain.
- Added collidable objects parsing from `obstacle_layout.txt`.
- Generated solid boundaries (vertical and horizontal walls) dynamically to constrain player movement within the window.

## 3. Entity System & Physics
- Developed a foundational `Sprite` class to manage position, velocity, hitboxes, and rendering.
- Integrated accurate AABB (Axis-Aligned Bounding Box) collision detection with tightened bounding boxes for realistic physical interactions.
- Added specific game entities:
  - **Villager (Player)**: Supports multi-directional movement (Up, Down, Left, Right) with dynamically updating sprites based on the velocity vector.
  - **Egg**: Contains logic to track its target `Nest`, whether it's collected, or already returned.
  - **Nest**: Static drop-off points instantiated across the farm grid.
  - **EggTray**: A functional inventory system attached to the player for holding collected eggs.

## 4. Core Game Logic
- **Initialization**: `Logic.initRound` dynamically spawns 5 specific nests and randomizes the location of 20 eggs (4 per nest) while guaranteeing that eggs do not spawn overlapping nests or obstacles.
- **Interactions**:
  - Picking up eggs by intersecting their hitboxes (`checkEggPickup`).
  - Delivering collected eggs specifically to their matching nests (`checkNestDelivery`).
- **Collisions**: Player physics resolve collisions by pushing back the `Villager` when running into walls, obstacles, or nests.
- **Game Loop**: A continuous `AnimationTimer` in the `Game` scene updates the physical state, handles drawing layers, and evaluates the `isRoundOver` condition (achieved when all scattered eggs are securely returned).

## 5. UI & Game State
- Defined a `GameState` enumeration (`PLAYING`, `ROUND_OVER`).
- Built a post-round popup (using JavaFX `Alert`) that announces the player's performance (Eggs Returned vs Total) and provides a "Play Again" functionality to reset the entities and map seamlessly.

---

## Screenshots & Visual Progress

<!-- 
Please insert screenshots below demonstrating the features achieved in this milestone.
-->

### 1. Farm Environment & Tiles
> *[Insert Screenshot of the initialized farm and boundaries here]*

### 2. Egg Collection
> *[Insert Screenshot of the player picking up an egg here]*

### 3. Nest Delivery
> *[Insert Screenshot of the player returning an egg to a nest here]*

### 4. Round Over Popup
> *[Insert Screenshot of the JavaFX Alert Round Over popup here]*
