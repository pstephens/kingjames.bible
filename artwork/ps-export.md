# Export an Icon from Photoshop

Note to self :-)

## SVG/PNG Icons

* Select the layer group the represents the icon.
* Execute the "Export As" command.
* SVG
    * Use the default 1x size
    * Set the format to SVG
    * Set the scale to 100%, resample to Bicubic Auto
    * Canvas width, height to 128px
    * No metadata
    * Execute "Export All"
    * The resulting file will have a <metadata /> section that should be removed
* PNG
    * Use the default 1x size
    * Set the format to PNG
    * Transparency: Yes
    * Smaller File: Yes
    * Set the scale to 23.44%, resample to Bicubic Auto
    * Canvas width, height to 30px
    * No metadata
    * Convert to sRGB: Yes
    * Embed color profile: No (we're doing B+W after all)
    * Execute "Export All"

## FavIcon

* favicon.png
    * Select the "Icon Layers" layer group
    * Execute the "Export As" command.
    * Use the default 1x size
    * Set the format to PNG
    * Transparency: Yes
    * Smaller File: Yes
    * All Widths/Heights at 16px
    * Scale: 100%
    * No Metadata
    * Convert to sRGB: Yes
    * Embed color profile: No
    * Execute "Export All"
* favicon.ico
    * This uses the [Photoshop icon plugin by Toby Thain](http://www.telegraphics.com.au/svn/icoformat/trunk/dist/README.html)
    * Select the "Icon Layers" layer group
    * Execute the "Save As" command
    * Select "Standard ICO" (non png) format
