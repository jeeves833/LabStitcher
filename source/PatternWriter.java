package color;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.PriorityQueue;
import java.util.Collections;
 
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Font;

public class PatternWriter {

	private static final String FONT = "../fonts/DejaVuSans.ttf";
	private static final int ROWS_PER_PAGE = 117;
	private static final int COLUMNS_PER_PAGE = 83;
	private static final float BORDER_WIDTH = .75f;

	public static Boolean write(Color[][] colors, String filename, String symbolsFilename, String paletteName) {
		ArrayList<String> symbols = SymbolReader.readSymbolFile("../symbols/" + symbolsFilename + ".txt");
		HashSet<Color> presentColors = new HashSet<Color>();
		float longestId = 0.0f;
		Random rand = new Random();
		Document document = new Document();
		try
		{
			// Calculate the number of pages needed
			int numPageRows = (int) Math.ceil((float)colors.length / (float)ROWS_PER_PAGE);
			int numPageCols = (int) Math.ceil((float)colors[0].length / (float)COLUMNS_PER_PAGE);
			int totalPages = numPageCols * numPageRows;
			polling:
			while (true) {
				String poll = System.console().readLine("Pattern will be " + totalPages 
				+ " pages long (Not including color chart).  Continue? (y/n): ");
				switch (poll) {
					case "y":
						break polling;
					case "n":
						System.out.println("Aborting pattern Write");
						return false;
					default:
						break;
				}
			}
			System.out.println("Writing Pattern");

			// Set up document
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("../images/" + filename + ".pdf"));
			document.open();

			// Create fonts
			Font mainFont = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 5.2f);
			Font titleFont = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10f);
			// document.add(new Paragraph("Test", mainf))

			// The marker cell is used for enumerating important rows and columns
			PdfPCell marker = new PdfPCell();

			marker.setBorder(Rectangle.LEFT & Rectangle.RIGHT & Rectangle.BOTTOM);
			marker.setHorizontalAlignment(Element.ALIGN_CENTER);
			/*table.addCell(marker);
			table.addCell(marker);
			marker.setColspan(10);
			marker.setHorizontalAlignment(Element.ALIGN_RIGHT);
			for (int i = 0; i < (int)(numCols/10); i++) {
				marker.setPhrase(new Paragraph(new Integer((i+1)*10).toString(), mainFont));
				table.addCell(marker);
			}
			marker.setPhrase(new Paragraph());
			table.addCell(marker);
			table.completeRow();*/

			// symbolCell is used for the stitch symbols themselves
			PdfPCell symbolCell = new PdfPCell();
			// symbolCell.setPhrase(new Paragraph("\u264F", mainFont));
			symbolCell.setPadding(0);
			symbolCell.setPaddingBottom(1f);
			symbolCell.setPaddingTop(-1.5f);
			symbolCell.setFixedHeight(6.2f);
			symbolCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			symbolCell.setHorizontalAlignment(Element.ALIGN_CENTER);

			/*marker.setPadding(0);
			marker.setRotation(90);
			marker.setColspan(1);
			marker.setRowspan(10);
			marker.setVerticalAlignment(Element.ALIGN_TOP);
			marker.setHorizontalAlignment(Element.ALIGN_LEFT);*/

			int currPage = 0;
			System.out.print("\r0%");
			// First iterate over pages rowWise
			for (int pageRow = 0; pageRow < numPageRows; pageRow++) {
				for (int pageCol = 0; pageCol < numPageCols; pageCol++) {
					currPage++;
					document.add(new Paragraph(filename, titleFont));
					// The row and column of the top left square in the page
					int startRow = pageRow * ROWS_PER_PAGE;
					int startCol = pageCol * COLUMNS_PER_PAGE;

					// The number of rows and columns on the page
					int rowsOnPage = Math.min(ROWS_PER_PAGE, colors.length - startRow);
					int columnsOnPage = Math.min(COLUMNS_PER_PAGE, colors[0].length - startCol);

					// Set up the table for this page
					PdfPTable table = new PdfPTable(columnsOnPage+1);
					table.setWidthPercentage(columnsOnPage *100 / COLUMNS_PER_PAGE);

					// Set marker up for enumerating Columns
					marker.setPhrase(new Paragraph());
					marker.setNoWrap(true);
					marker.setRotation(0);
					marker.setPadding(2);

					// Place Markers at every tenth column
					for (int i = startCol; i < startCol + columnsOnPage; i++) {
						if (i%10 == 0 && i != 0) {
			               marker.setColspan(2);
			               marker.setPhrase(new Paragraph(new Integer(i).toString(), mainFont));
			               table.addCell(marker);
			               marker.setPhrase(new Paragraph());
			               marker.setColspan(1);
			               i++;
			            } else {
			               table.addCell(marker);
			            }
					}

					/*table.addCell(marker);
					table.addCell(marker);

					while (startCol%10 != 0) {
						table.addCell(marker);
						startCol += 1;
					}
					marker.setColspan(10);
					marker.setHorizontalAlignment(Element.ALIGN_RIGHT);
					while (startCol < Math.min((pageCol+1)*COLUMNS_PER_PAGE, colors[0].length)) {
						marker.setPhrase(new Paragraph(new Integer(startCol+10).toString(), mainFont));
						table.addCell(marker);
					}*/

					// Complete the marker row at the top of the page
					marker.setPhrase(new Paragraph());
					marker.setRotation(90);
					marker.setPadding(0);
					table.addCell(marker);
					table.completeRow();

					// MarkerFlag is true when we just added a row marker to the left of the pattern
					boolean markerFlag = false;

					// Iterate over indices of COLORS present on the current page
					// Rows first
					for (int row = startRow; row < startRow + rowsOnPage; row++) {

						// Determine contents of left margin
						if (row%10 == 9) {

							// Add row marker if we are on the ninth row in a set of ten
							marker.setRowspan(2);
							marker.setPhrase(new Paragraph(new Integer(row+1).toString(), mainFont));
							table.addCell(marker);
							markerFlag = true;

			            } else if (markerFlag) {

			            	// Don't add a new margin if we just added a marker
			            	markerFlag = false;

			            } else {

			            	// Add an empty margin otherwise
			            	marker.setRowspan(1);
			            	marker.setPhrase(new Paragraph());
			            	table.addCell(marker);
			            }

			            // Set up top and bottom cell borders for the current row
			            if (row%10 == 0) {

			            	// Current row has a thicker top border
			            	symbolCell.setBorderWidthTop(BORDER_WIDTH/2);
							symbolCell.setBorderWidthBottom(BORDER_WIDTH/8);

			            } else if (row%10 == 9) {

			            	// Current row has a thicker bottom border
			            	symbolCell.setBorderWidthTop(BORDER_WIDTH/8);
							symbolCell.setBorderWidthBottom(BORDER_WIDTH/2);

			            } else {

			            	// Current row has no thick borders
			            	symbolCell.setBorderWidthBottom(BORDER_WIDTH/8);
							symbolCell.setBorderWidthTop(BORDER_WIDTH/8);

			            }

			            // Set top border of whole pattern
			            if (row == 0) {
			            	symbolCell.setBorderWidthTop(BORDER_WIDTH);
			            }

			            // Set bottom border of whole pattern
			            if (row == colors.length - 1) {
			            	symbolCell.setBorderWidthBottom(BORDER_WIDTH);
			            }

			            // Iterate over columns in the current row on the current page
						for (int col = startCol; col < startCol + columnsOnPage; col++) {

							// Set left and right borders for current column
							if (col%10 == 0) {

								// Current column has thicker left border
								symbolCell.setBorderWidthLeft(BORDER_WIDTH/2);
								symbolCell.setBorderWidthRight(BORDER_WIDTH/8);

							} else if (col%10 == 9) {

								// Current column has a thicker right border
								symbolCell.setBorderWidthLeft(BORDER_WIDTH/8);
								symbolCell.setBorderWidthRight(BORDER_WIDTH/2);

							} else {

								// Current column has no thick borders
								symbolCell.setBorderWidthRight(BORDER_WIDTH/8);
								symbolCell.setBorderWidthLeft(BORDER_WIDTH/8);

							}

							// Set left border of whole pattern
							if (col == 0) {
								symbolCell.setBorderWidthLeft(BORDER_WIDTH);
							}

							// Set right border of whole pattern
							if (col == colors[0].length - 1) {
								symbolCell.setBorderWidthRight(BORDER_WIDTH);
							}

							// Get symbol for this cell
							// System.out.println("Writing:");
							// System.out.println("	Row: " + row);
							// System.out.println("	Col: " + col);
							Color currColor = colors[row][col];
							if (currColor.getSymbol() == "") {
								currColor.setSymbol(symbols.remove(rand.nextInt(symbols.size())));
							}
							symbolCell.setPhrase(new Paragraph(currColor.getSymbol(), mainFont));
							// System.out.println(currColor.getSymbol());
							// Increment total stitches of this color by 1
							// currColor.incrementStitchNum();

							// Add cell to the table
							table.addCell(symbolCell);

							// Add color to set of found colors
							if (presentColors.add(currColor)) {
								float currWidth = titleFont.getBaseFont().getWidthPoint(currColor.getName(), 10f);
								if (longestId < currWidth) {
									longestId = currWidth;
								}
							}
						}
						table.completeRow();
					}
					document.add(table);
					Paragraph pageNum = new Paragraph("Page " + new Integer(pageRow * numPageCols + pageCol + 1).toString(), titleFont);
					pageNum.setAlignment(Element.ALIGN_RIGHT);
					document.add(pageNum);
					document.newPage();
					System.out.print("\r" + currPage*100/totalPages + "%");
				}
			}

			// Write the Thread Chart after the pattern
			ArrayList<Color> presentList = new ArrayList<Color>(presentColors);
			Collections.sort(presentList, new idSorter());

			// PriorityQueue<Color> colorQueue = new PriorityQueue<Color>(presentColors.size(), new idSorter());
			// Move elements of PRESENTCOLORS into the queue
			// for (int i = 0; i<presentList.size(); i++) {
				// colorQueue.offer(presentList.get(i));
			// }

			// Set up Cells

			symbolCell.setBorderWidthRight(BORDER_WIDTH/2);
			symbolCell.setBorderWidthBottom(BORDER_WIDTH/2);
			symbolCell.setBorderWidthTop(BORDER_WIDTH/2);
			symbolCell.setBorderWidthLeft(BORDER_WIDTH/2);
			symbolCell.setFixedHeight(11f);
			symbolCell.setPaddingTop(-2f);

			marker.setRowspan(1);
			marker.setPhrase(new Paragraph());

			PdfPCell colorCell = new PdfPCell(symbolCell);
	        colorCell.setPhrase(new Paragraph());

	        PdfPCell paletteCell = new PdfPCell(new Paragraph(paletteName, titleFont));
	        paletteCell.setNoWrap(true);
	        paletteCell.setPaddingTop(-1);
	        paletteCell.setBorder(Rectangle.LEFT & Rectangle.RIGHT & Rectangle.TOP & Rectangle.BOTTOM);
	        float paletteWidth = titleFont.getBaseFont().getWidthPoint(paletteName, 10f)/symbolCell.getFixedHeight();

	        PdfPCell idCell = new PdfPCell(paletteCell);
	        float idWidth = longestId/symbolCell.getFixedHeight();

	        PdfPCell countCell = new PdfPCell(paletteCell);

	        // Set up table
	        PdfPTable chartTable = new PdfPTable(9);
	        chartTable.setHorizontalAlignment(Element.ALIGN_LEFT);
	        float[] widths = new float[]{1f, .5f, 1f, 2f, paletteWidth, 2f, idWidth, 2f, 1f};
	        float totalWidth = 0.0f;
	        for (float w : widths) {
	           totalWidth += w;
	        }
	        chartTable.setTotalWidth(totalWidth*symbolCell.getFixedHeight());
	        chartTable.setWidths(widths);
	        chartTable.setLockedWidth(true);

	        Paragraph p = new Paragraph();
	        p.setIndentationLeft(36);

	        // Iterate through colors in order of their ID
			for (int i = 0; i < presentList.size(); i++) {
				Color currColor = presentList.get(i);
				symbolCell.setPhrase(new Paragraph(currColor.getSymbol(), titleFont));
				chartTable.addCell(symbolCell);
				chartTable.addCell(marker);
				colorCell.setBackgroundColor(new BaseColor(Color.toRGBint(Color.LABtoRGB(currColor.getChannels()))));
				chartTable.addCell(colorCell);
				chartTable.addCell(marker);
				chartTable.addCell(paletteCell);
				chartTable.addCell(marker);
				idCell.setPhrase(new Paragraph(currColor.getName(), titleFont));
				chartTable.addCell(idCell);
				chartTable.addCell(marker);
				countCell.setPhrase(new Paragraph("(" + currColor.getStitchNum() + " stitches)", titleFont));
				chartTable.addCell(countCell);
				chartTable.completeRow();
			}
			p.add(chartTable);
			document.add(p);


			document.close();
			writer.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}
}