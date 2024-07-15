package sheep.features.files;

import sheep.features.Feature;
import sheep.sheets.CellLocation;
import sheep.sheets.Sheet;
import sheep.ui.Perform;
import sheep.ui.UI;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A class representing loading a sheet from a file.
 */
public class FileLoading implements Feature {

    /**
     * The sheet that the file is to be loaded to
     */
    private final Sheet sheet;

    /**
     * The number of rows the sheet must have
     */
    private int rows;

    /**
     * The number of columns the sheet must have
     */
    private int cols;

    /**
     * The constructor method of this class.
     *
     * @param sheet the sheet which the contents of the file are to be loaded to
     */
    public FileLoading(Sheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public void register(UI ui) {
        ui.addFeature("load-file", "Load File", load());
    }

    /**
     * Creates a new CellLocation object from the file contents.
     *
     * @param encoding the encoded string from which the object can be created
     * @return a new CellLocation object
     * @throws FileFormatException if the number of parts of the string is not equal to 3
     */
    public CellLocation fromFileLocation(String encoding) throws FileFormatException {
        String[] parts = encoding.split(";");
        if (parts.length != 3) {
            throw new FileFormatException("Expected 3 parts, got " + parts.length, 1);
        }
        return new CellLocation(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    /**
     * Returns a string containing the value at a particular cell.
     *
     * @param encoding the encoded string from which the value is returned
     * @return a string containing the value at a particular cell
     * @throws FileFormatException if the number of parts of the string is not equal to 3
     */
    public String fromFileValue(String encoding) throws FileFormatException {
        String[] parts = encoding.split(";");
        if (parts.length != 3) {
            throw new FileFormatException("Expected 3 parts, got " + parts.length, 1);
        }
        if (parts[2].equals("E")) {
            parts[2] = "";
        }
        return parts[2];
    }

    /**
     * Parses the header line of the file.
     *
     * @param headerLine a string which is the first line of the file.
     * @return an array of integers containing the rows, columns, and number of cells
     *         that the sheet must have.
     * @throws FileFormatException if the header does not exist, does not contain exactly 3
     *                             parts, or if any of the values are not integers.
     */
    private int[] readHeaderLine(String headerLine) throws FileFormatException {
        if (headerLine == null) {
            throw new FileFormatException("No file header - empty file", 0);
        }
        String[] parts = headerLine.split(";");
        if (parts.length != 3) {
            throw new FileFormatException("File header malformed", 1);
        }
        int[] headerValues = new int[3];
        for (int i = 0; i < parts.length; i++) {
            try {
                headerValues[i] = Integer.parseInt(parts[i]);
            } catch (Exception e) {
                throw new FileFormatException("Header value expected to be integer", 1);
            }
        }
        return headerValues;
    }

    /**
     * Reads all the lines in the file and constructs the respective CellLocation objects. The
     * CellLocation and corresponding value are placed in a map which is returned.
     *
     * @param reader the reader that is to be used to read the file.
     * @return a map of CellLocation to value for each line in the file.
     * @throws FileFormatException if it is unable to read all lines in the file, or if the value
     *                             is not able to be placed in the map.
     */
    protected Map<CellLocation, String> readValues(Reader reader) throws
            IOException, FileFormatException {
        BufferedReader buff = new BufferedReader(reader);
        String headerLine = buff.readLine();

        int[] headerValues = readHeaderLine(headerLine); //the header line is parsed
        setRows(headerValues[0]); //number of rows and columns are set, which will be used
        //to set the dimensions of the sheet
        setCols(headerValues[1]);

        //maps each location to its corresponding value
        Map<CellLocation, String> sheetValues = new HashMap<>();
        int lineNumber = 2;
        for (int i = 0; i < headerValues[2]; i++) {
            String line = buff.readLine();
            if (line == null) {
                throw new FileFormatException("Unable to read enough lines", lineNumber);
            }
            try {
                sheetValues.put(fromFileLocation(line), fromFileValue(line));
            } catch (FileFormatException e) {
                throw new FileFormatException(e.getMessage(), lineNumber);
            }
            lineNumber++;
        }
        return sheetValues;
    }

    /**
     * The action to be performed when a file is loaded.
     *
     * @return an instance of the Load class.
     */
    public Perform load() {
        return new Load(getSheet());
    }

    /**
     * Returns the sheet that the file is to be loaded to
     */
    public Sheet getSheet() {
        return sheet;
    }

    /**
     * Returns the number of rows that the sheet must have
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns that the sheet must have
     */
    public int getCols() {
        return cols;
    }

    /**
     * Modifies the number of rows of the sheet.
     *
     * @param rows the new number of rows
     */
    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * Modifies the number of columns of the sheet.
     *
     * @param cols the new number of columns
     */
    public void setCols(int cols) {
        this.cols = cols;
    }
}
