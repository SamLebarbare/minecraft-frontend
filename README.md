Minecraft Web Front-end
=======================
The tricky way to manage your minecraft server! 

This front-end server template have a little set of features:

* Carousel images manager for public news
* Online player's public viewer with Minotar.net crafter avatar
* Whitelist manager

For logged crafter only :

* Whisp message to other
* Use Teleport
* Change gamemod

Technical infos:

* Use a custom Valve RCON for remote command from Java Code to Minecraft Server
* Play!Framework allow you to develop new features
* Use Twitter Bootstrap for easy CSS customization

Install
-------

This tool is based on the Java Play!Framework version 1.2.X

    https://github.com/playframework/play/

follow installation instruction,    
when the framework is ready, clone this repository and run : 
    
    play deps
    
Configure
---------
Enable RCON in your minecraft server.properties file and
edit the front-end config file in :

    conf/application.conf

Check DB lines for using MySql as backend for exemple

Run
---

    play run
    
