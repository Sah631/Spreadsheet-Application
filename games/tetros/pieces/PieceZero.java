package sheep.games.tetros.pieces;

import sheep.games.tetros.TetrosPiece;
import sheep.sheets.CellLocation;

import java.util.List;

/**
 * A class representing Piece 0, to be used in Tetros.
 */
public class PieceZero implements TetrosPiece {
    private int fallingType = 4;

    @Override
    public void setContents(List<CellLocation> contents) {
        contents.add(new CellLocation(0, 0));
        contents.add(new CellLocation(0, 1));
        contents.add(new CellLocation(1, 1));
        contents.add(new CellLocation(1, 2));
    }

    @Override
    public int getFallingType() {
        return fallingType;
    }

}
