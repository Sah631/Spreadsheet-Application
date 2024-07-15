package sheep.features.files;

import sheep.sheets.Sheet;
import sheep.ui.Perform;
import sheep.ui.Prompt;

import java.io.*;
import java.util.Optional;

/**
 * A class representing the action that is performed when a sheet is to be saved to a file
 */
public class Save extends FileSaving implements Perform {

    /**
     * The constructor method for this class
     *
     * @param sheet the sheet that is to be saved to a file
     */
    public Save(Sheet sheet) {
        super(sheet);
    }

    /**
     * The user is prompted for a file path. If a file is specified, the method attempts to
     * write each respective string on a new line of the file.
     *
     * @param row not used in this method.
     * @param column not used in this method.
     * @param prompt used to ask the user for a file name, and used to inform users if any
     *               error occurred while writing.
     */
    @Override
    public void perform(int row, int column, Prompt prompt) {
        Optional<String> filePathOpt = prompt.ask("Enter file name");
        String fileName = filePathOpt.orElse(null); //set file name to null if no name is given

        if (fileName != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                populateSheetItems(super.getSheet(), super.getSheetItems());
                for (String item : super.getSheetItems()) {
                    writer.write(item, 0, item.length());
                    writer.newLine(); //ensures that the string for each cell is on a new line
                }
            } catch (FileNotFoundException f) {
                prompt.message("'" + fileName + "' does not exist");
            } catch (IOException e) {
                prompt.message("Unable to write to file '" + fileName + "'");
            }
        } else {
            prompt.message("File not specified");
        }
    }
}
