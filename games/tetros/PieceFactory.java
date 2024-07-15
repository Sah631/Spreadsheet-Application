package sheep.games.tetros;

import sheep.games.tetros.pieces.*;

/**
 * A factory for creating new Piece objects.
 */
public class PieceFactory {

    /**
     * Takes in an integer value and returns an instance of a Piece class based on the value.
     *
     * @param value an integer containing the type of piece to be created
     * @return an instance of a Piece class
     */
    public static TetrosPiece createPiece(int value) {
        return switch (value) {
            case 0 -> new PieceZero();
            case 1 -> new PieceOne();
            case 2 -> new PieceTwo();
            case 3 -> new PieceThree();
            case 4 -> new PieceFour();
            case 5 -> new PieceFive();
            case 6 -> new PieceSix();
            default -> throw new IllegalCallerException("Invalid value");
        };
    }
}
