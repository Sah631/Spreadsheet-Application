package sheep.games.snake;

import sheep.expression.basic.Constant;
import sheep.features.Feature;
import sheep.games.random.RandomCell;
import sheep.sheets.CellLocation;
import sheep.sheets.Sheet;
import sheep.ui.Perform;
import sheep.ui.Prompt;
import sheep.ui.Tick;
import sheep.ui.UI;

import java.util.HashMap;
import java.util.Map;

/**
 * A class representing the game Snake
 */
public class Snake implements Feature, Tick {
    /**
     * The sheet which the game is to be played on
     */
    private final Sheet sheet;

    /**
     * An instance of the RandomCell interface, used to choose a new cell to place
     * food in once food is eaten.
     */
    private final RandomCell cell;

    /**
     * A map of the body of the snake
     */
    private final Map<Integer, CellLocation> snakeBody = new HashMap<>();

    /**
     * A variable to track when food has been eaten
     */
    private boolean eaten = false;

    /**
     * Keeps track of the current direction the snake is moving
     */
    private String curDirection = "s";

    /**
     * Keeps track of whether the game has started or not
     */
    private boolean gameStarted = false;

    /**
     * Stores the starting position of the snake
     */
    private CellLocation startPosition;

    /**
     * Keeps track of if the current tick is the first tick
     */
    private boolean firstTick = true;

    /**
     * The constructor method for this class
     * @param sheet the sheet on which the game is to be played
     * @param cell the RandomCell interface which is to be used to choose the cells
     *             on which to put food once it is eaten
     */
    public Snake(Sheet sheet, RandomCell cell) {
        this.sheet = sheet;
        this.cell = cell;
    }

    @Override
    public void register(UI ui) {
        ui.onTick(this);
        ui.addFeature("snake", "Start Snake", startSnake());
        ui.onKey("w", "Move North", new MoveSnake("w"));
        ui.onKey("a", "Move West", new MoveSnake("a"));
        ui.onKey("s", "Move South", new MoveSnake("s"));
        ui.onKey("d", "Move East", new MoveSnake("d"));
    }

    /**
     * The action to be performed when the game is started
     * @return a new instance of the StartSnake class
     */
    public Perform startSnake() {
        return new StartSnake();
    }

    /**
     * Determines the position of the head of the snake based on the key that is pressed
     * @param direction A string containing the key which is pressed
     * @return a CellLocation containing the new position of the head
     */
    public CellLocation newHeadPosition(String direction) {
        CellLocation curPosition = snakeBody.get(0);
        CellLocation newPosition = switch (direction) {
            case "w" -> new CellLocation(curPosition.getRow() - 1, curPosition.getColumn());
            case "a" -> new CellLocation(curPosition.getRow(), curPosition.getColumn() - 1);
            case "s" -> new CellLocation(curPosition.getRow() + 1, curPosition.getColumn());
            default -> new CellLocation(curPosition.getRow(), curPosition.getColumn() + 1);
        };

        curDirection = direction;
        snakeBody.replace(0, newPosition);
        return newPosition;
    }

    /**
     * Places all locations of the body of the snake in an array, in order.
     * @return an Array with each index containing the location of a cell of the body of the snake
     */
    public CellLocation[] getSnakeLocations() {
        //the size of the array should be equal to the size of the map
        CellLocation[] snakeLocations = new CellLocation[snakeBody.size()];
        for (Map.Entry<Integer, CellLocation> entry : snakeBody.entrySet()) {
            //the keys of the snakeBody map start from 0, so the index of the array
            //is equal to the key of the map
            snakeLocations[entry.getKey()] = entry.getValue();
        }
        return snakeLocations;
    }

    /**
     * Replaces the head of the snake with headPosition. All subsequent values in the map
     * are replaced with the value of the previous location of the previous cell. If food is
     * eaten, another key-value pair is added to the map, whose location is the previous location
     * of the previous end of the snake.
     *
     * @param headPosition a CellLocation containing the position of the head of the snake
     * @param bodyPositions an Array of CellLocation objects which contain the locations of each
     *                      cell of the body of the snake
     */
    public void moveSnake(CellLocation headPosition, CellLocation[] bodyPositions) {
        snakeBody.replace(0, headPosition);
        for (int i = 0; i < bodyPositions.length - 1; i++) {
            //when a snake moves, each cell in the body will move in the same direction
            //so the location at position i + 1 in the body will be the previous location of
            //value i.
            snakeBody.replace(i + 1, bodyPositions[i]);
        }
        if (eaten) {
            snakeBody.put(bodyPositions.length, bodyPositions[bodyPositions.length - 1]);
        }
    }

    /**
     * Resets all class variables to the default values and clears the sheet.
     */
    public void reset() {
        snakeBody.clear();
        eaten = false;
        curDirection = "s";
        gameStarted = false;
        firstTick = true;
        for (int i = 0; i < sheet.getRows(); i++) {
            for (int j = 0; j < sheet.getColumns(); j++) {
                sheet.update(i, j, "");
            }
        }
    }

    /**
     * Clears cells which were previously part of the snake body, but are no longer part of
     * the body since the snake has moved.
     *
     * @param snakeLocations an Array of CellLocation objects which contain the locations of each
     *                       cell of the body of the snake
     */
    public void clearCells(CellLocation[] snakeLocations) {
        for (int i = 0; i < sheet.getRows(); i++) {
            for (int j = 0; j < sheet.getColumns(); j++) {
                CellLocation curLoc = new CellLocation(i, j);
                boolean inSnake = false;
                for (CellLocation location : snakeLocations) {
                    //for each location in the snake, if the location at (i, j) is equal
                    //to any location in the snake, the loop is broken, since this means
                    //that (i, j) is part of the snake body and should not be cleared.
                    if (curLoc.equals(location)) {
                        inSnake = true;
                        break;
                    }
                }

                if (sheet.valueAt(curLoc).render().equals("1") && !inSnake) {
                    sheet.update(curLoc.getRow(), curLoc.getColumn(), "");
                }
            }
        }
    }

    /**
     * Checks if the move is valid.
     * @param headPosition a CellLocation containing the position of the head of the snake
     * @return true if the sheet contains the headPosition, false otherwise.
     */
    public boolean checkValidMove(CellLocation headPosition) {
        if (!sheet.contains(headPosition)) {
            return false;
        } else {
            return !sheet.valueAt(headPosition).render().equals("1");
        }
    }

    /**
     * Checks if the head of the snake is on a cell which contains food.
     * @param headPosition a CellLocation containing the position of the head of the snake
     */
    public void checkFood(CellLocation headPosition) {
        if (!sheet.valueAt(headPosition).render().equals("1")
                && !sheet.valueAt(headPosition).render().isEmpty()) {
            eaten = true;
        }
    }

    /**
     * Checks if the game is started. If it is, on the first tick the head of the snake is
     * placed at startPosition. Then the move is checked to see if it is valid. If the move
     * is invalid, the game ends. The snake is then moved. If the head of the snake is on
     * a food cell, a new cell is picked and food is placed there. Then the sheet is updated
     * with the new locations of the snake body.
     *
     * @param prompt Provide a mechanism to interact with the user interface
     *               after a tick occurs, if required.
     * @return false if the game is not started, or an invalid move is made. True otherwise.
     */
    @Override
    public boolean onTick(Prompt prompt) {
        if (!gameStarted) {
            return false;
        }

        if (!firstTick) {
            snakeBody.put(0, startPosition);
            sheet.update(startPosition.getRow(), startPosition.getColumn(), "1");
            firstTick = true;
        }
        CellLocation headPosition = newHeadPosition(curDirection);
        if (!checkValidMove(headPosition)) {
            prompt.message("Game Over!");
            gameStarted = false;
            reset();
            return false;
        }

        checkFood(headPosition);

        CellLocation[] snakeLocations = getSnakeLocations();
        moveSnake(headPosition, snakeLocations);

        //a new random cell is picked to place food once food has been eaten
        if (eaten) {
            sheet.update(cell.pick().getRow(), cell.pick().getColumn(), "2");
            eaten = false;
        }

        for (Map.Entry<Integer, CellLocation> entry : snakeBody.entrySet()) {
            sheet.update(entry.getValue().getRow(), entry.getValue().getColumn(), "1");

        }
        clearCells(snakeLocations);
        return true;
    }

    /**
     * A class representing the action to be performed when the game is started
     */
    public class StartSnake implements Perform {

        /**
         * Sets the start position to a new CellLocation object at (row, column). If the sheet
         * does not contain this position, then the startPosition is set to (0, 0).
         *
         * @param row The currently selected row of the user, or -2 if none selected.
         * @param column The currently selected column of the user, or -2 if none selected.
         * @param prompt Provides a mechanism to interact with the user interface
         *               after an interaction, if required.
         */
        @Override
        public void perform(int row, int column, Prompt prompt) {
            startPosition = new CellLocation(row, column);
            if (!sheet.contains(startPosition)) {
                startPosition = new CellLocation(0, 0);
            }
            snakeBody.put(0, startPosition);
            sheet.update(startPosition.getRow(), startPosition.getColumn(), "1");
            gameStarted = true;
        }
    }

    /**
     * A class representing the action to be performed when the snake is moved
     */
    public class MoveSnake implements Perform {
        private final String direction;

        /**
         * The constructor method for this class
         * @param direction A string containing the direction in which the snake is moving
         */
        public MoveSnake(String direction) {
            this.direction = direction;
        }

        @Override
        public void perform(int row, int column, Prompt prompt) {
            curDirection = direction;
        }
    }
}
