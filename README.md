Minecrift Mod for Minecraft
===========================

Current Version: 1.0 beta

StellaArtois, mabrowning 2013

With thanks to:

- Palmer Luckey and his team for creating the Oculus Rift. The future is
  finally here (well for some people anyway; mine hasn't arrived yet).
- Markus "Notch" Persson for creating Minecraft. What has it grown into?
- The team behind the MCP coders' pack, and the Minecraft community - why
  Mojang bother obfuscating the source when you guys have done such a fantastic
  job of de-obfuscating it is beyond me!
- Powback for his initial work on the Java JNI wrapper to the SDK. Seeing this
  inspired me to get off my arse and get modding. See
  [this Reddit thread](http://www.reddit.com/r/oculus/comments/1c1vh0/java_wrapper_for_devs/)
- shakesoda and Ben (and others?) at MTBS for creating the GLSL version of the
  Oculus distortion shader.
- The guys at Valve for giving some good advice on updating a game for VR.
- @PyramidHead76 for building the MacOS libs, and toiling to produce the
  installation guide!!
- Brad Larson and his GPUImage library, for the Lanczos GLSL shader
  implementation for the FSAA.
- All the feedback and support of the folks in the MTBS3D forums!

What is Minecrift?
------------------

The cheesy name apart, Minecrift attempts to update Minecraft to support the
Oculus Rift. Initially this means allowing head-tracking input and using the
correct stereo rendering parameters for the Rift. We also are in the progress
of supporting different control schemes and positional head tracking. Minecraft
for various control schemes. Minecrift is also meant as a kick up the arse to
Mojang, so that they can add official Oculus support in the near future. If and
when Minecraft officially supports the Rift, Minecrift development might cease
(unless they make a complete hash of it), but probably not.

Disclaimer
----------

I recommend using a vanilla Minecraft.jar file for this. Forge compatibility is
mostly in place, but there may be a bugs.  BACK UP your original minecraft.jar
and any maps you care about before installing this mod. I've gotten FTB 1.5.2
to start up and run, but haven't tested all the nooks and crannies of the mod.
Caveat Modder.

---
Where to get it?
----------------

We make regular releases and update the MTBS3D forum thread when a release is 
ready for general use. However, if you can't wait that long, we do have a 
continuous integration service generously provided by 
[CloudBees](http://www.cloudbees.com). 

Click the button below to go to our Jenkins page where you can download the
latest build hot-off-the-presses.

[![Powered By CloudBees](http://www.cloudbees.com/sites/default/files/Button-Powered-by-CB.png)
](https://minecraftvr.ci.cloudbees.com/job/minecrift/)

---

Installation
------------

REQUIRES Minecraft 1.5.2 With [Optifine HD D3](http://www.minecraftforum.net/topic/249637-152-optifine-hd-d3-fps-boost-hd-textures-aa-af-and-much-more/)

Magic Launcher
--------------
The recommended way to install Minecrift is use the [magic
launcher](http://www.minecraftforum.net/topic/939149-launcher-magic-launcher-114-mods-options-profiles-news/),
which is available for Windows, OSX, and Linux.

- Download Optifine HD D3, but don't extract.
- Extract the minecrift\_1.0\_beta.zip
- Open the Magic Launcher.
- Click the 'Setup' configuration button.
- Create a new Configuration and call it "minecrift" (or whatever you prefer)
- Add these zips, in order:
  - OptiFine\_1.5.2\_HD\_U\_D3.zip 
  - JRift.jar 
  - SixenseJava.jar (if you have a Razer Hydra)
  - minecrift\_1.0\_beta\_classes.zip 
- Click 'Test' to make sure it works.
- When satisfied, click 'OK' to Save the configuration.
- From now on, just start Magic Launcher and use the "minecrift" configuration to play!


In addition, you will need to [update LWJGL to the latest version](http://www.minecraftwiki.net/wiki/LWJGL). Older versions are unsupported.

Manual
------

It is possible to install Minecrift without using the Magic launcher, but this
way hasn't been tested as well. Use the steps below according to your operating
system. You'll still need to update LWJGL according to the tutorial linked above.

Windows
-------

Minecrift for Windows requires Vista or above and a graphics card & driver capable of at least OpenGL 3.3 support.

- Download [Optifine HD D3](http://www.minecraftforum.net/topic/249637-152-optifine-hd-d3-fps-boost-hd-textures-aa-af-and-much-more/)
- Change directory to %APPDATA%\\.minecraft\bin
- Open your minecraft.jar file using 7-zip, winzip etc. 
- Select all, and drag and drop in the *entire contents* of the
  OptiFine\_1.5.2\_HD\_U\_D3.zip into the minecraft.jar.
- Select all, and drag and drop in the *entire contents* of the
  /minecrift\_1.0\_beta\_classes.zip (but not the zip itself) from the Minecrift
  zip into the minecraft.jar archive.
- Select all, and drag and drop in the *entire contents* of the
  /JRift.jar (but not the zip itself) from the Minecrift
  zip into the minecraft.jar archive.
- If you have a Razer Hydra and would like to use it, Select all, and drag and
  drop in the *entire contents* of the /SixenseJava.jar (but not the zip
  itself) from the Minecrift zip into the minecraft.jar archive.
- Make sure to delete the META-INF folder in minecraft.jar. Close 7zip /
  winzip.
- *IMPORTANT* (but only required once). Install the Microsoft VS2012 C++
  redists (both x86 and x64) from
  [here](http://www.microsoft.com/visualstudio/11/en-us/downloads/vc-redist#vc-redist)
- Start up Minecraft and off you go. If you get a black screen on login, trying
  running an admin command prompt, cd to your minecraft.exe dir and enter the
  command 
>java -cp Minecraft.exe net.minecraft.LauncherFrame
This should allow any exceptions or errors on Minecraft startup to show up in
the console.

MacOS
-----

Follow the same steps for Windows, but use ~/Library/Application
Support/minecraft instead of <Path to %APPDATA%>\\.minecraft.
- The VS2012 C++ redistribute is not required.

Linux
-----

Follow the same steps for MacOS, but use ~/.minecraft/ instead of
~/Library/Application Support/minecraft.

----

Razer Hydra
-----------
Version 1.0 is the first to include full Razer Hydra support. If you have one, 
include SixenseJava.jar in your modlist to enable the functionality.
 - OrientationTracker: If you don't have an Oculus Rift, you can use the left
   controler for head orientation (direction).
 - PositionTracker: This makes a huge immersion difference. The Hydra can feed
   position data the game engine to allow you to look up, down, around corners,
   squat, and generally move around. Use one or two controllers attached to
   your head and adjust the offsets from your eye center on the VR Options.
 - Controller: Use the right controller to turn the view left/right, move
   forward and backwards, place blocks, mine, select item, jump, sneak, access
   your inventory and navigate menus. The controls are currently hardcoded:
     Joystick X: Turn left/right
	 Joystick Y: Move forward/back
	 1: Drop item
	 2: Jump
	 3: Select next left item (mousewheel up)
	 4: Select next right item (mousewheel down)
	 JOYSTICK: Sneak
	 Bumper: Place block/use item/interact (right mouse)
	 Trigger: Mine block (left mouse)
	 Start: Access Inventory

	 In Menus/Inventories:
	 Joystick: mouse mouse up/down/left/right (don't use the actual mouse at
			   the same time: known issue)
	 Trigger: Left Click
	 Bumper: Right Click
	 JOYSTICK: "Shift"

	 You should be able to take advantage of the new 1.5 inventory management
	 controls with this joystick mapping.

	 Joystick sensitivity can be set in VR Options.

Controls/Usage
--------

Here are some other hotkeys that allow quick access to changing VR settings.

- All Minecrift settings are present in the Options->Minecrift screen, but
  keyboard shortcuts are also available for convenience
- Make sure to read the tool-tips on each setting in VR Options to get an
  understanding for what it is adjusting. VR is best when you tune the experience
  to your setup.
- Pressing space-bar while in a menu will reset the orientation of the head tracker 
  to make the current direction "forward"

- F1 to bring up the game HUD / overlay if it isn't already up. 
- Ctrl and - / = for IPD adjustment. Hold ALT as well for fine adjustment. The
  IPD setting should be saved between sessions.
- Ctrl O to attempt to reinitialise the Rift (including head tracking).
- Ctrl P while not in a menu to turn distortion on / off. Sometimes useful if
  the offset mouse pointer is a pain in the menus. Ctrl-Alt P to toggle
  chromatic aberration correction.
- Ctrl L toggles head-tracking ON/OFF. Ctrl-Alt L toggles tracking prediction
  ON/OFF. It is OFF by default.
- Ctrl U changes the HUD distance. Ctrl-Alt U changes the HUD scale. Ctrl-Alt Y
  toggles opacity on the HUD.
- Ctrl-M toggles rendering of the player's mask ON/OFF.
- FOV adjustment within Minecraft will have no effect - I use the FOV as
  calculated from within the Oculus SDK.
- Allow user to use mouse pitch input as well as yaw. Use Ctrl-N to toggle.
- Large or Auto GUI size recommended.
- Use Ctrl-B to turn Full Scene Anti-aliasing (FSAA) on/off. Use Ctrl-Alt B to
  cycle the FSAA renderscale. Be warned; this feature is a resource hog!! If
  you cannot get 60fps at your desired FSAA level, cycle it to a lower scale
  factor. Anyone with a nVidia GTX Titan please let me know what average FPS
  you get at scale factor 4.0!
- Ctrl , or . decreases or increases the FOV scale factor. This can be used to
  fine tune FOV if it doesn't look quite right to you.
- Ctrl-Alt , or . decreases or increases various sizes of distortion border.
  This can be used to improve rendering speed, at a potential loss of FOV.
- Ctrl V cycles through head track sensitivity multipliers. Try this at your
  own risk!

Known Issues
------------

- FSAA (Super Sampling) doesn't work on OSX and is disabled.
- Linux doesn't support Oculus Rift head tracker (yet).
- A white line can sometimes be seen at the top or bottom edge of the HUD. No
  known workaround.
- When using both the joystick and the mouse to navigate menus, the cursor 
  does not respond to clicks where it appears. For now, only use one or the other.
- Sometimes, the Hydra calibration text does not appear... if the hydra isn't 
  responding, try doing the calibration steps: point at base with left, click, 
  point at base with right, click.

Feedback, bug reporting
-----------------------

Please post feedback, bug reports etc. to the [GitHub issue
tracker](https://github.com/mabrowning/minecrift/issues). Please search before
posting to see if the issue has already been reported

There is also discussion happening at this [forum thread at
MTBS](http://www.mtbs3d.com/phpbb/viewtopic.php?f=140&t=17146)

Roadmap
-------

- Investigate gamepad support.
- Make controls remappable.
- Add more natural VR interfaces.
- Fix bugs.

Release Notes
=============

The change-list can be seen [here](CHANGES.md)

---

Building
========


The installation process has been tested on Linux, but was written with
cross-platform support in mind, so should be usable on other platforms.
Known issues: OSX fernflower doesn't decompile cleanly. Decompile on Windows
or Linux and copy over the .minecraft\_orig folder.

Download [mcp 751](http://mcp.ocean-labs.de/index.php/MCP_Releases) and
extract into /mcp (only needed once)

Run install.sh (or install.bat) to download minecraft, download Optifine,
deobfuscate the base system, and apply the patches and new files.

Use the MCP environment in /mcp to modify, test, and recompile.

Run build.sh (or build.bat) to create a release classes.zip.

Run getchanges.sh (or getchanges.bat) to diff the modified /mcp/src files into
version controlled /patches and copy the new classes into the /src/
directory.

License
-------

See [The License File](LICENSE.md) for more information.
