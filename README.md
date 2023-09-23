This repo is no longer maintained, please fork it if you wish to continue developing or fixing features. From what I can tell, Bitbucket Server has been pretty much abandoned, so this plugin _should_ continue working until it is End Of Life.

---

![Logo](http://farm9.staticflickr.com/8105/8529837904_79cafece82_c.jpg)

TeamCity Stash (Bitbucket Server) Integration
===============
If you use TeamCity and Bitbucket Server, then this plugin is for you.

This build feature sends build status updates from TeamCity to Bitbucket Server.  You can then see build statuses against commits.

 ![Stash screenshot](http://farm9.staticflickr.com/8096/8529642406_29e3e9a899_o.png)



Install
==========

Download the [.zip file](https://github.com/mendhak/teamcity-stash/blob/master/teamcity.stash.zip?raw=true) and place it in the `<TeamCity data directory>/plugins` folder, then restart TeamCity.


Set-up
==========

Under your build steps, click on `Add Build Feature`. It will appear in the dropdown list.

![Build Feature](http://farm9.staticflickr.com/8088/8529641968_ba34b8ebac_o.png)


Simply enter your Stash server details and credentials to connect with. The plugin will now send build status updates to your Stash server.

![Configuration](http://farm9.staticflickr.com/8090/8558753742_aa0655c92e_o.png)


How it works
======

This is a TeamCity Build Feature built using the [TeamCity Open API](http://confluence.jetbrains.com/display/TCD7/Developing+TeamCity+Plugins).

It listens for build statuses and posts them to the [Atlassian Stash Build API](https://developer.atlassian.com/static/rest/stash/latest/stash-build-integration-rest.html).



License
=======
GPL v2


______________


Code setup
=====
You will need [IntelliJ IDEA](http://www.jetbrains.com/idea/download/) as this project uses IDEA features to build artifacts.

You will also need to download and extract [TeamCity](http://www.jetbrains.com/teamcity/download/) which provides the required jars.

Open the project in Intellij IDEA, you should see a lot of unresolved references, this is normal.

Go to `File | Settings | Path Variables` and set the `TeamCityDistribution` variable, pointing it to your TeamCity location.

To **build** the project, click `Build | Build Artifacts...` and choose `plugin-zip`.  The .zip is generated in `/out/artifacts/plugin_zip`.


Troubleshooting
====
If the plugin doesn't seem to be working, you can find plugin messages in the `teamcity-server.log` file under your TeamCity installation. (Example: `/TeamCity/logs/teamcity-server.log`)
This usually gives you a good idea of why a call may have failed.

You can also look at Stash's `atlassian-stash.log` under STASH_HOME's log folder (Example: `/Stash-Home/log/atlassian-stash.log`) file to see what it did with the HTTP request sent by the plugin.  In the log file, search for `POST /rest/build-status` as a starting point.
