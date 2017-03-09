# Export an Icon from Photoshop

Note to self :-)

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
