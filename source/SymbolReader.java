package color;

import java.util.ArrayList;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class SymbolReader {


	public static void main(String[] args) {
		readSymbolFile("../symbols/defaultSymbols.txt");
	}

	public static ArrayList<String> readSymbolFile(String filename){
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			while (reader.ready()) {
				String code = reader.readLine();
				symbols.add(code);
			}
		} catch (IOException e) {
			System.out.println("File: " + filename + " not found.  Aborting");
			System.exit(1);
		}
		return symbols;
	}
}