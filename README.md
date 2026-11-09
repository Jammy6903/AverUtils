# AverUtils
A utility plugin for Spigot/Paper Minecraft servers adding various chat commands, teleportation commands, discord integrations, and more

## Features
### Social:
- Chat formatting
- Private messaging
- Player blocking (of `CHAT`, `MESSAGES`, `TPA`, `PVP`)
- PvP toggling
### Player Profiles:
- Nicknames
- Name choice (of `PLAYER_NAME`, `NICKNAME`, `DISCORD_GLOBAL`, `DISCORD_NICKNAME`)
- Name styling (solid colours, gradients, bold, italic, underlined)
- Pronouns
### Teleportation:
- Homes
- Warps
- Spawn command
- Teleport requests
### Administration:
- Rolling MOTDs
- Rolling Player List Footers/Headers
- Doas command (like sudo)
### Discord:
- Chat links
- Role links (with LuckPerms groups)
- Discord account verification (can be set to required)

## Permissions
### Commands:
| Command               | Permission                                                                                                     | Aliases                       |
|-----------------------|----------------------------------------------------------------------------------------------------------------|-------------------------------|
| /msg                  | averutils.msg                                                                                                  | /message, /tell, /whisper, /w |
| /r                    | averutils.msg                                                                                                  | /reply                        |
| /tpa                  | averutils.tpa                                                                                                  |                               |
| /tpahere              | averutils.tpa                                                                                                  |                               |
| /tpaccept             | averutils.tpa                                                                                                  |                               |
| /tpdeny               | averutils.tpa                                                                                                  |                               |
| /home                 | averutils.homes                                                                                                |                               |
| /sethome              | averutils.homes                                                                                                |                               |
| /delhome              | averutils.homes                                                                                                |                               |
| /homes                | averutils.homes                                                                                                |                               |
| /warp                 | averutils.warp                                                                                                 |                               |
| /warps                | averutils.warp                                                                                                 |                               |
| /setwarp              | averutils.editwarps                                                                                            |                               |
| /delwarp              | averutils.editwarps                                                                                            |                               |
| /spawn                | averutils.spawn                                                                                                |                               |
| /nickname             | averutils.nickname                                                                                             | /nick                         |
| /namechoice           | averutils.namechoice                                                                                           | /name                         |
| /pronouns             | averutils.pronouns                                                                                             |                               |
| /namestyle            | averutils.namestyle, averutils.namestyle.color,  averutils.namestyle.gradient,  averutils.namestyle.decoration | /style                        |
| /togglepvp            | averutils.pvp                                                                                                  | /pvp                          |
| /toggletpa            | averutils.tpa                                                                                                  |                               |
| /block                | averutils.block                                                                                                |                               |
| /unblock              | averutils.unblock                                                                                              |                               |
| /verify               | averutils.verify                                                                                               |                               |
| /motd                 | averutils.motd                                                                                                 |                               |
| /playerlistdecoration | averutils.playerlistdecoration                                                                                 | /pld, /listdecoration         |
| /doas                 | averutils.doas                                                                                                 |                               |
| /reload               | op                                                                                                             |                               |
|                       | averutils.bypass-block                                                                                         |                               |
