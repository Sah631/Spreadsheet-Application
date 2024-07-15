package sheep.games.tetros.pieces;

import sheep.games.tetros.TetrosPiece;
import sheep.sheets.CellLocation;

import java.util.List;

/**
 * A class representing Piece 5, to be used in Tetros.
 */
public class PieceFive implements TetrosPiece {
    private int fallingType = 6;

    @Override
    public void setContents(List<CellLocation> contents) {
        contents.add(new CellLocation(0, 0));
        contents.add(new CellLocation(1, 0));
        contents.add(new CellLocation(2, 0));
        contents.add(new CellLocation(3, 0));
    }

    @Override
    public int getFallingType() {
        return fallingType;
    }

}
