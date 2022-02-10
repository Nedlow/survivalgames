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
- MySQL Support for saving game statistics


### TODO:
- Sponsorship: Let's spectators buy special items for alive players.
- Sign-based leaderboard in lobby
- World Events: Player or location based, eg. zombies or toxic fog.
- SQLite Support to save game statistics locally
- Tournament mode: Manually selected maps, time duration and Player whitelist
### Setup
Setting up this plugin is quite simple. All you need is the plugin itself and some arenas (BungeeCord optional)
1. Copy all arenas into your server root folder, and make sure all the names are short and have no spaces. (sg1,sg2,sge)  
2. Go ingame and type '/addmap <filename> <Displayname>'. 
3. Type '/editarena <filename>'. 
4. Go to every pod/spawn and make sure the player can run straight ahead.
5. Type '/addspawn <filename> <index>'. For index put 0-23. Repeat step for every spawn.   
5. Find the center of the arena and type '/addspawn <filename> center'. This sets the center of arena (Decides sound locations, compass direction)
6. Do any other changes to the map if needed, and finally type /savearena <filename>.
7. Teleport to the location of your lobby by writing /tploc <world> <x> <y> <z>
8. Set lobby with '/setlobby'.
9. Restart the server.
