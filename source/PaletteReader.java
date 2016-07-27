package color;

import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
import java.util.Arrays;

public class PaletteReader {

	String paletteName;
	String colorspaceName;
	String[] channelNames;
	Color[] colors;

	public PaletteReader(File f) {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse(f);
	        doc.getDocumentElement().normalize();

	        NodeList listOfPaletteNames = doc.getElementsByTagName("paletteName");
	        int numOfPaletteNames = listOfPaletteNames.getLength();
	        if (numOfPaletteNames != 1) {
	        	System.out.println("Found " + numOfPaletteNames + " Palette names.  Required, 1.  Aborting");
	        	System.exit(1);
	        }
	        paletteName = listOfPaletteNames.item(0).getChildNodes().item(0).getNodeValue().trim();


	        NodeList listOfColorspaces = doc.getElementsByTagName("colorspace");
	        int numOfColorspaces = listOfColorspaces.getLength();
	        if (numOfColorspaces != 1) {
	        	System.out.println("Found " + numOfColorspaces + " Colorpsaces.  Required, 1.  Aborting");
	        	System.exit(1);
	        }
	        Element colorspaceElement = (Element)listOfColorspaces.item(0);
	        NodeList spaceNameList = colorspaceElement.getElementsByTagName("name");
	        colorspaceName = spaceNameList.item(0).getChildNodes().item(0).getNodeValue().trim();

	        if (colorspaceName.equals("RGB")) {
	        	System.out.println("Found RGB palette, converting to LAB.");
	        }


	        NodeList channelNameList = colorspaceElement.getElementsByTagName("channel");
	        int numChannels = channelNameList.getLength();
	        channelNames = new String[numChannels];
	        for (int i = 0; i < numChannels; i++) {
	        	channelNames[i] = channelNameList.item(i).getChildNodes().item(0).getNodeValue().trim();
	        }

	        NodeList colorList = doc.getElementsByTagName("color");
	        int numOfColors = colorList.getLength();
	        colors = new Color[numOfColors];
	        for (int i = 0; i < numOfColors; i++) {
	        	Element colorElement = (Element) colorList.item(i);
	        	String id = colorElement.getAttribute("id");
	        	double[] channels = new double[channelNames.length];
	        	for (int j = 0; j < channelNames.length; j++) {
	        		NodeList channelList = colorElement.getElementsByTagName(channelNames[j]);
	        		channels[j] = Double.parseDouble(channelList.item(0).getChildNodes().item(0).getNodeValue().trim());
	        	}
	        	if (colorspaceName.equals("RGB")) {
	        		channels = Color.RGBtoLAB(channels);
	        	}
	        	colors[i] = new Color(id, channels);
	        }

	    } catch (SAXParseException e) {
	    	System.out.println("SAXParseException encountered.");
	    } catch (SAXException e) {
	    	System.out.println("SAXException encountered.");
	    } catch (Throwable e) {
	    	System.out.println(e);
	    	System.exit(1);
	    }
	}

	public Color[] getColors() {
		return colors;
	}

	public String getName() {
		return paletteName;
	}

	public String getColorspace() {
		return colorspaceName;
	}
}