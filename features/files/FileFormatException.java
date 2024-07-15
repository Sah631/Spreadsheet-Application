package sheep.features.files;

/**
 * A class representing an exception that is to be thrown whenever there is an error in
 * processing files due to the file format.
 */
public class FileFormatException extends Exception {

    /**
     * Construct a new FileFormatException with a description of the exception
     * @param message the message that is to be displayed
     * @param lineNum the linenumber at which the exception occurred
     */
    public FileFormatException(String message, int lineNum) {
        super(message + " at " + lineNum);
    }
}
