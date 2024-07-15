package sheep.features.files;

import sheep.features.Feature;
import sheep.sheets.CellLocation;
import sheep.sheets.Sheet;
import sheep.ui.Perform;
import sheep.ui.UI;
import java.util.ArrayList;

/**
 * A class representing saving a sheet to a file.
 */
public class FileSaving implements Feature {

    /**
     * The sheet that is to be saved
     */
    private final Sheet sheet;

    /**
     * A list of items representing values on the sheet
     */
    private final ArrayList<String> sheetItems = new ArrayList<>();

    /**
     * The constructor method for this class.
     *
     * @param sheet the sheet that is to be saved
     */
    public FileSaving(Sheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public void register(UI ui) {
        ui.addFeature("save-file", "Save File", save());
    }

    /**
     * Creates the file header which includes the rows, columns, and total number of cells
     * in the sheet.
     *
     * @param sheet the sheet that is to be saved
     * @return A string containing the rows, columns and total number of cells, each separated
     * by semicolons.
     */
    public String fileHeader(Sheet sheet) {
        int rows = sheet.getRows();
        int cols = sheet.getColumns();
        int numCells = rows * cols;
        return rows + ";" + cols + ";" + numCells;
    }

    /**
     * Returns a string containing the row, column and formula stored at the given location
     * in the sheet.
     *
     * @param sheet the sheet that is to be saved
     * @param location the cell location that is to be stored
     * @return A string containing the rows, columns and value at the location, each separated
     * by semicolons.
     */
    public String cellInfo(Sheet sheet, CellLocation location) {
        int row = location.getRow();
        int col = location.getColumn();
        String value = sheet.formulaAt(location).render();
        if (value.isEmpty()) {
            value = "E"; //value is set to "E" for empty cells
        }
        return row + ";" + col + ";" + value;
    }

    /**
     * Adds the header string, and strings representing each cell on the sheet to the sheetItems
     * list.
     *
     * @param sheet the sheet that is to be saved
     * @param sheetItems a list containing all the strings that are to be stored in the file
     */
    public void populateSheetItems(Sheet sheet, ArrayList<String> sheetItems) {
        sheetItems.add(fileHeader(sheet));
        for (int i = 0; i < sheet.getRows(); i++) {
            for (int j = 0; j < sheet.getColumns(); j++) {
                CellLocation curLocation = new CellLocation(i, j);
                sheetItems.add(cellInfo(sheet, curLocation));
            }
        }
    }

    /**
     * The action to be performed when the save button is pressed.
     *
     * @return an instance of the Save class.
     */
    public Perform save() {
        return new Save(getSheet());
    }

    /**
     * Returns the sheet to be saved
     *
     * @return the sheet that is to be saved
     */
    public Sheet getSheet() {
        return sheet;
    }

    /**
     * Returns a list of strings containing all strings that must be saved to the file.
     *
     * @return A list of strings representing each cell on the sheet.
     */
    public ArrayList<String> getSheetItems() {
        return sheetItems;
    }
}
