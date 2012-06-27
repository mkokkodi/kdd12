package kokkodis.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;



public class PrintToFile {
	private FileOutputStream out; // declare a file output object
	private PrintStream p; // declare a print stream object

	public PrintToFile() {
	}


	/**
	 * 
	 * @param file 
	 */
	public void openFile(File file) {
		try {
			out = new FileOutputStream(file);
			p = new PrintStream(out);

		} catch (Exception e) {
			System.err.println("Error writing to file");
			e.printStackTrace();
		}

	}

	public void closeFile() {
		try {
			p.close();
		} catch (Exception e) {
			System.err.println("Error Closing file");
			e.printStackTrace();
		}
	}

	public void writeToFile(String str) {
		try {
			p.println(str);
		} catch (Exception e) {
			System.err.println("Error writing to file");
			e.printStackTrace();
		}
	}

	public void writeNoLN_ToFile(String str) {
		try {
			p.print(str);
		} catch (Exception e) {
			System.err.println("Error writing to file");
			e.printStackTrace();
		}
	}

}