package sheep.games.tetros;

import sheep.sheets.CellLocation;

import java.util.List;

/**
 * This interface is used as a way of hiding the process of setting the contents
 * for individual pieces in Tetros.
 */
public interface TetrosPiece {
    /**
     * Adds all cells taken up by a current piece to the list passed into the method.
     * @param contents A list of CellLocation objects, for which all the CellLocations
     *                 currently taken up by the piece are input.
     */
    void setContents(List<CellLocation> contents);

    /**
     * Returns the fallingtype of a piece.
     * @return an integer containing the fallingType of the piece.
     */
    int getFallingType();
}
