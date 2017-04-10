# LabStitcher
A program for converting pictures into Cross Stitch patterns using the LAB color space.

AS OF 8/18/2016 ALL BASELINE FEATURES HAVE BEEN ADDED.  FULLY USEABLE PATTERNS ARE NOW GENERATED FROM ANY INPUT PICTURE.
HAPPY STITCHING!

Requirements for use:
- iTextpdf (https://github.com/itext/itextpdf)
- Java

To use:

- This program is run in command line Java.  A MakeFile is included in the /build/ directory that will compile all the necessary source files.
- Run the program from the /build/ directory with "java color/ColorConv"
- You will be prompted to view the current settings.
  - Dithering will improve the look of colors not accessible by the current color palette.
    - LabStitcher uses Floyd-Steinberg dithering
  - SSO (Single Stitch Optimization) is a process that ensures stitches have neighbors of the same color.
  - Symbols is the name of the file containing the symbols used for the pattern
    - Symbol files must be located in /symbols/
  - Palette is the name of the desired color palette
    - Palette files must be located in /palettes/
- Enter the full file name of the image you want converted
  - e.g. "test.png"
  - Images must be located in the /images/ directory
- Enter the name of the resulting pattern
  - All output images will be .png so you just need the name without the tag
    - e.g. "test_dmc"
- An image of the color compressed image will be saved to the /images/ directory
- The .pdf pattern will be saved in the /images/ directory as well
