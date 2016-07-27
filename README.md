# LabStitcher
A program for converting pictures into Cross Stitch patterns using the LAB color space.

PROGRAM IS NOT YET COMPLETE - WIP

Requirements for use:
- iTextpdf (https://github.com/itext/itextpdf)
- Java

To use:

- This program is run in command line Java.  A MakeFile is included in the /build/ directory that will compile all the necessary source files.
- Run the program from the /build/ directory with "java color/ColorConv"
- You will be prompted for a Palette File
- Enter the name of the desired palette file without the ".xml" tag
  - e.g. "dmc"
  - Palette files must be located in the /palettes/ directory
- Enter the full file name of the image you want converted
  - e.g. "test.png"
  - Images must be located in the /images/ directory
- Choose if you want the resulting pattern to be dithered ("y" or "n")
  - LabStitcher uses Floyd-Steinberg dithering
- Enter the name of the symbol file used for pattern writing
  - e.g. "defaultSymbols"
  - All symbol files must be located in /symbols/
  - LabStitcher does not yet output patterns so the symbol file will not be used
    - Will be updated in future releases
- Enter the name of the resulting pattern
  - All output images will be .png so you just need the name without the tag
    - e.g. "test_dmc"
- An image of the color compressed image will be saved to the /images/ directory
- Future releases will see a .pdf of the pattern saved as well
