# Survival Games: Districts of Panem
Rewrite and upgrade of my previous SG plugin.


### Features
It is made to run one match per server, and is therefore designed to be used with BungeeCord. It can reset without restarting. If BungeeCord is disabled, players will be teleported back to lobby instead of server restart.
- Easy setup
- Open source
- BungeeCord support
- Supports game reset without server shutdown
- Deathmatch mode to finish maps in reasonable time
- District mode: Allows for 12 teams of 2 players
- Configurable chest contents
- Spectator menu for easy spectator movement across the arena
- MySQL and SQLite Support for saving game statistics


### TODO:
- Sign-based leaderboard in lobby (IN-PROGRESS)
- Items with special abilities (IN-PROGRESS)
- Mulitple chest tiers (Chest=normal,barrel=exclusive)
- Replacing deathmatch with dynamic world border (Using WorldEdit API)
- Sponsorship: Let's spectators buy special items for alive players.
- World Events: Player or location based, eg. zombies or toxic fog.
### Setup
Setting up this plugin is quite simple. All you need is the plugin itself and some arenas (BungeeCord optional)
1. Copy all arenas into your server root folder, and make sure all the names are short and have no spaces. (sg1,sg2,sge)  
3. Go ingame and type '/devmode' then '/addmap filename Displayname'. 
4. Type '/editarena filename'. 
5. Go to every pod/spawn and make sure the player can run straight ahead.
6. Type '/addspawn filename index'. For index put 0-23. Repeat step for every spawn.   
7. Find the center of the arena and type '/addspawn filename center'. This sets the center of arena (Decides sound locations, compass direction)
8. Do any other changes to the map if needed, and finally type /savearena.
9. Teleport to the location of your lobby by writing /tploc world x y z
10. Set lobby with '/setlobby'.
11. Restart the server.
