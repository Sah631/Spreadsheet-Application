package sheep.games.tetros;

import sheep.expression.TypeError;
import sheep.expression.basic.Constant;
import sheep.expression.basic.Nothing;
import sheep.features.Feature;
import sheep.games.random.RandomTile;
import sheep.sheets.CellLocation;
import sheep.sheets.Sheet;
import sheep.ui.*;

import java.util.*;

/**
 * A class representing the game Tetros.
 */
public class Tetros implements Tick, Feature {
    /**
     * The sheet which the game is to be played on
     */
    private final Sheet sheet;

    /**
     * A variable to keep track of when the game has started and ended
     */
    private boolean started = false;

    /**
     * Determines the associated number for each piece.
     */
    private int fallingType = 1;

    /**
     * A list of locations of the current piece that is falling.
     */
    private List<CellLocation> contents = new ArrayList<>();

    /**
     * Used to choose which piece falls after the current piece is dropped.
     */
    private final RandomTile randomTile;

    /**
     * The constructor method for this class.
     *
     * @param sheet The sheet which the game is to be played on
     * @param randomTile the randomTile interface which is to be used to choose which piece falls
     *                   after the current piece is dropped.
     */
    public Tetros(Sheet sheet, RandomTile randomTile) {
        this.sheet = sheet;
        this.randomTile = randomTile;
    }

    @Override
    public void register(UI ui) {
        ui.onTick(this);
        ui.addFeature("tetros", "Start Tetros", this.getStart());
        ui.onKey("a", "Move Left", this.getMove(-1));
        ui.onKey("d", "Move Right", this.getMove(1));
        ui.onKey("q", "Rotate Left", this.getRotate(-1));
        ui.onKey("e", "Rotate Right", this.getRotate(1));
        ui.onKey("s", "Drop", this.fullDropTile());
    }

    /**
     * Checks whether the location on the sheet is on the edge of the sheet.
     *
     * @param sheet the sheet to be used for the game
     * @param location the location to check
     * @return true if the location is on the border of the sheet and is empty, otherwise false.
     */
    private boolean isStopper(Sheet sheet, CellLocation location) {
        if (location.getRow() >= sheet.getRows() || location.getColumn() >= sheet.getColumns()) {
            return true;
        }
        return !sheet.valueAt(location.getRow(), location.getColumn()).getContent().isEmpty();
    }

    /**
     * Checks whether a piece is in bounds.
     *
     * @param sheet the sheet to be used to play the game
     * @param locations a list of CellLocations containing all locations on the sheet which are
     *                  taken up by the piece.
     * @return true if all locations taken up by the piece are within the bounds of the sheet,
     *         otherwise false.
     */
    public boolean inBounds(Sheet sheet, List<CellLocation> locations) {
        for (CellLocation location : locations) {
            if (!sheet.contains(location)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Drops a piece by 1 row, increasing the row value of all cell locations taken up by the
     * piece by one.
     *
     * @param contents a list of CellLocations containing all locations on the sheet which are
     *                 taken up by the piece.
     * @return true if the piece cannot be dropped anymore since it is at the bottom of the sheet,
     *         otherwise returns false.
     */
    public boolean dropTile(List<CellLocation> contents) {
        List<CellLocation> newContents = new ArrayList<>();
        for (CellLocation tile : contents) {
            newContents.add(new CellLocation(tile.getRow() + 1, tile.getColumn()));
        }
        unrender();

        //ensures that the piece remains in bounds after dropping
        for (CellLocation newLoc : newContents) {
            if (isStopper(getSheet(), newLoc)) {
                ununrender(contents);
                return true;
            }
        }
        ununrender(newContents);
        this.contents = newContents;
        return false;
    }

    /**
     * Drops a piece all the way to the bottom of the sheet, or until it reaches a stopper.
     */
    public void fullDrop() {
        boolean drop = false;
        while (!drop) {
            drop = dropTile(getContents());
        }
    }

    /**
     * Moves a piece horizontally by 'x' amount. If x is 2, then the piece is fully dropped.
     *
     * @param x an integer describing which direction to move the piece.
     */
    public void shift(int x) {
        if (x == 2) {
            fullDrop();
        }
        List<CellLocation> newContents = new ArrayList<>();
        for (CellLocation tile : contents) {
            newContents.add(new CellLocation(tile.getRow(), tile.getColumn() + x));
        }

        //ensures that the piece is in bounds after shifting
        if (!inBounds(getSheet(), newContents)) {
            return;
        }

        //clear old piece and render the piece in the new location
        unrender();
        ununrender(newContents);
        this.contents = newContents;
    }

    /**
     * Clears the current piece from the sheet.
     */
    public void unrender() {
        for (CellLocation cell : contents) {
            try {
                sheet.update(cell, new Nothing());
            } catch (TypeError e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Updates the sheet with the new location of the piece.
     *
     * @param items a list of CellLocations containing all locations on the sheet which are
     *              taken up by the piece.
     */
    public void ununrender(List<CellLocation> items) {
        for (CellLocation cell : items) {
            try {
                sheet.update(cell, new Constant(fallingType));
            } catch (TypeError e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Checks whether a new piece can be dropped, or if the sheet is full.
     *
     * @return false if a new piece can be dropped, otherwise true if the sheet is full
     */
    private boolean drop() {
        contents = new ArrayList<>();
        newPiece();
        for (CellLocation location : contents) {
            if (!sheet.valueAt(location).render().isEmpty()) {
                return true;
            }
        }
        ununrender(contents);

        return false;
    }

    /**
     * Creates a new piece using the PieceFactory class, and a random value chosen by
     * randomTile. The contents and fallingType are set using the TetrosPiece interface
     * methods.
     */
    private void newPiece() {
        int value = randomTile.pick();
        TetrosPiece piece = PieceFactory.createPiece(value);
        piece.setContents(getContents());
        setFallingType(piece.getFallingType());

    }

    /**
     * Rotates a piece in the specified direction
     *
     * @param direction the direction in which to rotate the piece
     */
    private void flip(int direction) {
        int x = 0;
        int y = 0;
        for (CellLocation cellLocation : contents) {
            x += cellLocation.getColumn();
            y += cellLocation.getRow();
        }
        x /= contents.size();
        y /= contents.size();
        List<CellLocation> newCells = new ArrayList<>();

        //performs the rotation of each cell in the piece
        for (CellLocation location : contents) {
            int lx = x + ((y - location.getRow()) * direction);
            int ly = y + ((x - location.getColumn()) * direction);
            CellLocation replacement = new CellLocation(ly, lx);
            newCells.add(replacement);
        }

        //ensures that the rotated piece is in bounds
        if (!inBounds(getSheet(), newCells)) {
            return;
        }
        unrender();
        contents = newCells;
        ununrender(newCells);
    }

    /**
     * Returns the current state of the sheet
     */
    public Sheet getSheet() {
        return sheet;
    }

    /**
     * Returns the current contents list
     */
    public List<CellLocation> getContents() {
        return contents;
    }

    /**
     * Sets the contents list to the contents parameter.
     *
     * @param contents a list of CellLocation objects which the contents class variable is
     *                 set to.
     */
    public void setContents(List<CellLocation> contents) {
        this.contents = contents;
    }

    /**
     * Sets the fallingType class variable.
     *
     * @param fallingType The new fallingType integer which the class variable is set to.
     */
    public void setFallingType(int fallingType) {
        this.fallingType = fallingType;
    }

    /**
     * Checks whether the game is started. If so, a piece is dropped, and checks are done to
     * ensure that the piece can be dropped. On each tick, the piece automatically drops by
     * one row unless the user forces a full drop, or the piece is already as low as it can go.
     *
     * @param prompt Provide a mechanism to interact with the user interface
     *               after a tick occurs, if required.
     * @return true if the game is started and an action is performed, otherwise false.
     */
    @Override
    public boolean onTick(Prompt prompt) {
        if (!started) {
            return false;
        }

        if (dropTile(getContents())) {
            if (drop()) {
                prompt.message("Game Over!");
                started = false;
            }
        }
        clear(getSheet(), getContents());
        return true;
    }

    /**
     * Checks whether the sheet is full. If it is full, the 'eating' operations are performed
     * if applicable.
     *
     * @param sheet the sheet that the game is being played on
     * @param contents a list of CellLocation objects which the contents class variable is
     *                 set to.
     */
    private void clear(Sheet sheet, List<CellLocation> contents) {
        for (int row = sheet.getRows() - 1; row >= 0; row--) {
            boolean full = true;
            for (int col = 0; col < sheet.getColumns(); col++) {
                if (sheet.valueAt(row, col).getContent().isEmpty()) {
                    //if there are any empty cells, the sheet is not full
                    full = false;
                }
            }
            if (full) {
                for (int rowX = row; rowX > 0; rowX--) {
                    for (int col = 0; col < sheet.getColumns(); col++) {
                        try {
                            if (contents.contains(new CellLocation(rowX - 1, col))) {
                                continue;
                            }
                            sheet.update(new CellLocation(rowX, col),
                                    sheet.valueAt(new CellLocation(rowX - 1, col)));
                        } catch (TypeError e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                row = row + 1;
            }
        }
    }

    /**
     * The action to be performed when the game is started
     *
     * @return an instance of the GameStart class.
     */
    public Perform getStart() {
        return new GameStart();
    }

    /**
     * The action to be performed when a move is made
     *
     * @param direction the direction in which to move
     * @return an instance of the Move class
     */
    public Perform getMove(int direction) {
        return new Move(direction);
    }

    /**
     * The action to be performed when rotating a piece
     *
     * @param direction the direction in which to rotate
     * @return an instance of the Rotate class
     */
    public Perform getRotate(int direction) {
        return new Rotate(direction);
    }

    /**
     * The action to be performed when a tile is fully dropped
     *
     * @return an instance of the Drop class
     */
    public Perform fullDropTile() {
        return new Drop();
    }

    /**
     * A class representing the action to be performed when the game is started.
     */
    public class GameStart implements Perform {
        @Override
        public void perform(int row, int column, Prompt prompt) {
            started = true;
            drop();
        }
    }

    /**
     * A class representing the action to be performed when a piece is moved.
     */
    public class Move implements Perform {
        private final int direction;

        /**
         * The constructor method for this class
         *
         * @param direction the direction in which the piece is to be moved
         */
        public Move(int direction) {
            this.direction = direction;
        }

        @Override
        public void perform(int row, int column, Prompt prompt) {
            if (!started) {
                return;
            }
            shift(direction);
        }
    }

    /**
     * A class representation the action to be performed when a piece is rotated
     */
    public class Rotate implements Perform {
        private final int direction;

        /**
         * The constructor method for this class.
         *
         * @param direction the direction in which the piece is to be rotated
         */
        public Rotate(int direction) {
            this.direction = direction;
        }

        @Override
        public void perform(int row, int column, Prompt prompt) {
            if (!started) {
                return;
            }
            flip(direction);
        }
    }

    /**
     * A class representing the action to be performed when a piece is dropped
     */
    public class Drop implements Perform {

        @Override
        public void perform(int row, int column, Prompt prompt) {
            if (!started) {
                return;
            }
            fullDrop();
        }
    }
}