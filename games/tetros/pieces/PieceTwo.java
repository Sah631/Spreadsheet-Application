package sheep.games.tetros.pieces;

import sheep.games.tetros.TetrosPiece;
import sheep.sheets.CellLocation;

import java.util.List;

/**
 * A class representing Piece 2, to be used in Tetros.
 */
public class PieceTwo implements TetrosPiece {
    private int fallingType = 5;

    @Override
    public void setContents(List<CellLocation> contents) {
        contents.add(new CellLocation(0, 1));
        contents.add(new CellLocation(1, 1));
        contents.add(new CellLocation(2, 1));
        contents.add(new CellLocation(2, 0));
    }

    @Override
    public int getFallingType() {
        return fallingType;
    }

}