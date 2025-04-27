package com.connectfour; // or any other package both client and server can access

import java.io.Serializable;

//represents a single move made by a player .
//  contains all the necessary information for the server to broadcast
// the move and for all clients to update their game boards accordingly.

public class GameMove implements Serializable {

    // Required for Java serialization
    private static final long serialVersionUID = 1L;

    private final String playerName; // Who made the move
    private final int row;           // Row where the disc was placed
    private final int col;           // Column where the disc was placed
    private final String color;      // Color of the disc ("RED" or "YELLOW")

    /**
     * Constructor for GameMove.
     *
     * @param playerName Name of the player who made the move.
     * @param row The row index where the disc landed (0 at the top).
     * @param col The column index where the disc was dropped.
     * @param color The color of the player's disc ("RED" or "YELLOW").
     */
    public GameMove(String playerName, int row, int col, String color) {
        this.playerName = playerName;
        this.row = row;
        this.col = col;
        this.color = color;
    }

    // Getters
    public String getPlayerName() {
        return playerName;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return playerName + " dropped a " + color + " disc at (" + row + ", " + col + ")";
    }
}
