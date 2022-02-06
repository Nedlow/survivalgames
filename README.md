# sg_rewrite
Rewrite and upgrade of my previous SG plugin.


### Features
It is firstly made to run only one match per server, and is therefore supposed to be used with BungeeCord. There is a setting for this in the config.
- Easy setup
- Open source!


TODO:
- Sponsorship: Uses players points to buy items for a user ingame from a menu

### Setup
Setting up this plugin is quite simple. All you need is the plugin itself, some arenas, and a server that restarts itself or multicraft host.  
1. Copy all arenas into your server root folder, and make sure all the names are short and have no spaces. (sg1,sg2,sge)  
2. Go ingame and type /addmap <filename> <Displayname>. 
3. Type /editarena <filename>. 
4. Go to every pod/spawn and make sure the player can run straight ahead.
5. Type /addspawn <filename> <index>. For index put 0-23. Repeat step for every spawn.   
6. Do any other changes to the map if wanted, and finally type /savearena <filename>.
7. Teleport to the location of your lobby by writing /tploc <world> <x> <y> <z>
8. Set lobby with /setlobby.
9. Restart the server. This is considered save if no game is currently running. Reload will break the server.
