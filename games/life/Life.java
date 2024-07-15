package sheep.games.life;

import sheep.features.Feature;
import sheep.sheets.CellLocation;
import sheep.sheets.Sheet;
import sheep.ui.Perform;
import sheep.ui.Prompt;
import sheep.ui.Tick;
import sheep.ui.UI;

import java.util.HashMap;
import java.util.Map;

/**
 * A class representing the Game of Life
 */
public class Life implements Feature, Tick {

    /**
     * Determines whether the current tick is the first tick
     */
    private boolean firstTick = false;

    /**
     * Keeps track of whether the game is being played
     */
    private boolean gameStarted = false;

    /**
     * A mapping of locations on the sheet to the value that must be stored at these locations
     */
    private final Map<CellLocation, String> sheetMap = new HashMap<>();

    /**
     * The sheet that is to be used for the game
     */
    private final Sheet sheet;

    /**
     * The constructor method for this class
     *
     * @param sheet the sheet to be used for the game
     */
    public Life(Sheet sheet) {
        this.sheet = sheet;

    }

    @Override
    public void register(UI ui) {
        ui.onTick(this);
        ui.addFeature("gol-start", "Start GOL", actionStart());
        ui.addFeature("gol-end", "End GOL", actionStop());
    }

    /**
     * Sets the gameStarted variable to the value of started.
     *
     * @param started the value that the gameStarted variable is set to
     */
    public void setGameStarted(boolean started) {
        this.gameStarted = started;
    }

    /**
     * Checks the number of on and off neighbours for the given location. The checkType parameter
     * determines whether this method checks for the number of on or off neighbours.
     *
     * @param location the location on the sheet whose neighbours are checked
     * @param checkType determines whether to return the number of on or off neighbours.
     * @return returns the number of on or off neighbours
     */
    private int checkNeighbours(CellLocation location, String checkType) {
        int numOnNeighbours = 0;
        int numOffNeighbours = 0;
        int row = location.getRow();
        int col = location.getColumn();
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (i >= 0 && j >= 0) {
                    CellLocation checkLocation = new CellLocation(i, j);
                    if (sheet.contains(checkLocation) && !checkLocation.equals(location)) {
                        // ensures that the location given is not included when determining
                        // number of neighbours
                        if (sheet.valueAt(checkLocation).render().equals("1")) {
                            numOnNeighbours++;
                        } else if (!sheet.valueAt(checkLocation).render().equals("1")) {
                            numOffNeighbours++;
                        }
                    }
                }
            }
        }
        if (checkType.equals("1")) {
            return numOnNeighbours;
        } else {
            return numOffNeighbours;
        }
    }

    /**
     * Turns the cell on at the given location
     *
     * @param location the location of the cell on the sheet to turn on
     */
    public void turnCellOn(CellLocation location) {
        sheetMap.replace(location, "1");
    }

    /**
     * Turns the cell off at the given location
     *
     * @param location the location of the cell on the sheet to turn off
     */
    public void turnCellOff(CellLocation location) {
        sheetMap.replace(location, "");
    }

    /**
     * Applies the rules of the game of life and determines whether to turn the cell
     * at the given location on or off.
     *
     * @param location the location of the cell on the sheet to check whether to turn on or off
     */
    public void updateCell(CellLocation location) {
        int numOn = checkNeighbours(location, "1");
        boolean cellType = sheet.valueAt(location).render().equals("1");
        if (cellType) {
            if (numOn < 2) {
                turnCellOff(location);
            } else if (numOn > 3) {
                turnCellOff(location);
            }
        } else {
            if (numOn == 3) {
                turnCellOn(location);
            }
        }
    }

    /**
     * Updates the value of all locations on the sheet in the sheetMap after applying the rules
     * of the game.
     */
    public void updateMap() {
        for (Map.Entry<CellLocation, String> entry : sheetMap.entrySet()) {
            updateCell(entry.getKey());
        }
    }

    /**
     * The action to be performed when the game is started.
     * @return an instance of the Start class
     */
    public Perform actionStart() {
        return new Start();
    }

    /**
     * The action to be performed when the game is stopped.
     * @return an instance of the Stop class
     */
    public Perform actionStop() {
        return new Stop();
    }

    /**
     * The action to be performed on each tick. Checks whether the game is started, populates the
     * sheetMap on the first tick, updates the sheetMap, and updates the sheet using the
     * sheetMap.
     *
     * @param prompt this parameter was not used in this method, but must be included as this
     *               class implements the Tick interface.
     * @return true if the game was started and the actions were performed, otherwise false.
     */
    @Override
    public boolean onTick(Prompt prompt) {
        if (!gameStarted) {
            return false;
        }

        //initially populate the map with all current values on the sheet on the first tick
        if (!firstTick) {
            for (int i = 0; i < sheet.getRows(); i++) {
                for (int j = 0; j < sheet.getColumns(); j++) {
                    sheetMap.put(new CellLocation(i, j), sheet.valueAt(i, j).getContent());
                }
            }
            firstTick = true;

        }

        //update the map, and update the sheet using the values in the map
        updateMap();
        for (Map.Entry<CellLocation, String> entry : sheetMap.entrySet()) {
            if (entry.getValue().equals("1")) {
                sheet.update(entry.getKey().getRow(), entry.getKey().getColumn(), "1");
            } else {
                sheet.update(entry.getKey().getRow(), entry.getKey().getColumn(), "");
            }

        }
        return true;
    }

    /**
     * A class representing the action to be performed when the Game of Life is started
     */
    public class Start implements Perform {

        @Override
        public void perform(int row, int column, Prompt prompt) {
            setGameStarted(true);
        }
    }

    /**
     * A class representing the action to be performed when the Game of Life is ended
     */
    public class Stop implements Perform {
        @Override
        public void perform(int row, int column, Prompt prompt) {
            setGameStarted(false);
        }
    }
}
