TeamCity Stash Integration
===============
If you use TeamCity and Stash, then this plugin is for you.

This build feature sends build status updates from TeamCity to Stash.  You can then see build statuses against commits.

 ![Stash screenshot](http://farm9.staticflickr.com/8507/8529565648_cd74cc3b61_o.png)



Install
==========

Download the [.zip file](https://github.com/mendhak/teamcity-stash/blob/master/teamcity.stash.zip?raw=true) and place it in the `<TeamCity data directory>/plugins` folder, then restart TeamCity.


Set-up
==========

Under your build steps, click on `Add Build Feature`. It will appear in the dropdown list.

![Build Feature](http://farm9.staticflickr.com/8097/8529565338_3da443c808_o.png)


Simply enter your Stash server details and credentials to connect with. The plugin will now send build status updates to your Stash server.

![Configuration](http://farm9.staticflickr.com/8103/8528453179_53d61d97b2_o.png)


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

The project should now build.  The .zip is generated in `/out/artifacts/plugin_zip`.