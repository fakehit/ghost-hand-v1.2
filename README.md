# Ghost Hand Cam

Client-side Fabric mod for Minecraft Java 26.3.

While your camera is detached from your body (Freecam, or any other camera-entity mod),
holding right-click targets the first *usable* block under the crosshair and skips every
block in between: chests, barrels, furnaces, signs, doors, trapdoors, fence gates, buttons,
levers, crafting stations and a few more. Blocks that can't be used are passed through.

## Use it with Freecam
In Freecam's config, turn interactions on and set them to come from the camera
(the "prevent interaction" option must be off). Ghost Hand Cam does the rest.

## Long reach (worlds you host)
Your normal reach is untouched, except while Freecam is on. While the camera is detached, your real body
may be up to 64 blocks from the camera (singleplayer, or a world you host, for example with Essential).
The camera itself still only reaches a normal distance (about 4.5 blocks), so fly it right up to what you
want to open or break. On a world someone else hosts, their server still enforces normal reach.

## Limits
- The server checks reach from your real body, so the camera has to stay within 64 blocks of your player.
- Left-click (breaking blocks) is not affected.
- Interacting through walls is treated as cheating on most multiplayer servers. Use it in
  singleplayer or where it's allowed.

## Build
`./gradlew build` (JDK 25). The jar ends up in `build/libs/`. Pushing to GitHub also builds it via the included workflow.
