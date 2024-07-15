package sheep.features.files;

import sheep.sheets.CellLocation;
import sheep.sheets.Sheet;
import sheep.ui.Perform;
import sheep.ui.Prompt;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;

/**
 * A class representing the action that is performed when a file is to be loaded
 */
public class Load extends FileLoading implements Perform {

    /**
     * The constructor method for this class
     *
     * @param sheet the sheet that the file is to be loaded to
     */
    public Load(Sheet sheet) {
        super(sheet);
    }

    /**
     * Prompts the user for a file name. If a file name is given, the method attempts to read
     * from the file, clears the sheet and updates its dimensions, and updates the sheet with
     * the values stored in the file.
     *
     * @param row not used in this method.
     * @param column not used in this method.
     * @param prompt used to ask the user for a file name, and used to inform the user about
     *               any errors that occurred.
     */
    @Override
    public void perform(int row, int column, Prompt prompt) {
        Sheet sheet = super.getSheet();
        Optional<String> filePath = prompt.ask("Enter file name");
        String fileName = filePath.orElse(null); //set filename to null if no file name is given
        Map<CellLocation, String> sheetValues;

        if (fileName != null) {
            try (Reader reader = new FileReader(fileName)) {
                sheetValues = super.readValues(reader);
                int rows = super.getRows();
                int cols = super.getCols();

                sheet.clear();
                //dimensions must be updated to ensure the file can be loaded properly
                sheet.updateDimensions(rows, cols);

                for (Map.Entry<CellLocation, String> entry : sheetValues.entrySet()) {
                    sheet.update(entry.getKey().getRow(), entry.getKey().getColumn(),
                            entry.getValue());
                }

            } catch (FileFormatException e) {
                prompt.message("Incorrect file format");
            } catch (FileNotFoundException e) {
                prompt.message("File not found");
            } catch (IOException e) {
                prompt.message("File cannot be read");
            }

        } else {
            prompt.message("File not provided");
        }
    }

}
