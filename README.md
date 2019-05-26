### T-Zero makes communicating over different timezone as easy as it has never been!
T-Zero will scan all messages that it can recieve for times and lets you convert those times into your timezone easily
![Demonstration](http://kaleidox.de/share/img/bot/tzero-demonstration.png)

## Invite Link
[Click here](https://discordapp.com/oauth2/authorize?client_id=581831468381110273&scope=bot&permissions=85056) to invite T-Zero to your discord guild

## Setting up T-Zero
There is no such thing as "setup" required for T-Zero. It only requires a timezone being set by every user. This is done by issuing the following command: ```tzero!set <Timezone>```

#### If a user has once defined their timezone, it is global and does not need to be set for every server.

Timezones are parsed using [TimeZone#getTimeZone](https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html#getTimeZone(java.lang.String) in the Java runtime. Valid schemes can be found in the [TimeZone documentation](https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html) for the Oracle Java Runtime.

Basically, values based on the Greenwich Mean Time (GMT), like `GMT+2` are allowed. Locations, like cities or countries are not allowed.  

## All TempVoicer commands
### VoiceChannel commands
| Command                | Description                                       | Notes                                                                                                                     |
|------------------------|---------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `tzero!set <Timezone>` | Define your own timezone. This setting is global. | See [this documentation](https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html) for which values are allowed. |
### Other commands
| Command                                        | Description                                                           | Notes                                             |
|------------------------------------------------|-----------------------------------------------------------------------|---------------------------------------------------|
| `tzero!help [Command]`                         | Shows all available commands, or information about a specific command |                                                   |
| `tzero!property [<Property Name> [New Value]]` | Setup command                                                         | Requires the `Manage Server` permission for usage |
| `tzero!invite`                                 | Sends you an Invite-link for the bot via DM                           |                                                   |
