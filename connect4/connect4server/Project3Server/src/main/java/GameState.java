//package com.connectfour;

public class GameState {
    private final String[][] board;
    private final int rows = 6;
    private final int cols = 7;
    private String currentPlayer;

    public GameState() {
        board = new String[rows][cols];
        resetBoard();
        currentPlayer = "RED";  // Starting player
    }

    // Resets the game board
    public void resetBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = null;  // Empty space
            }
        }
    }

    // Check if a player has won
    public boolean checkWin(String player) {
        // Check horizontal, vertical, and diagonal
        return checkHorizontal(player) || checkVertical(player) || checkDiagonal(player);
    }

    // Check for a horizontal win
    private boolean checkHorizontal(String player) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols - 3; j++) {
                if (board[i][j] != null && board[i][j].equals(player) &&
                        board[i][j].equals(board[i][j+1]) &&
                        board[i][j].equals(board[i][j+2]) &&
                        board[i][j].equals(board[i][j+3])) {
                    return true;
                }
            }
        }
        return false;
    }

    // Check for a vertical win
    private boolean checkVertical(String player) {
        for (int i = 0; i < rows - 3; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] != null && board[i][j].equals(player) &&
                        board[i][j].equals(board[i+1][j]) &&
                        board[i][j].equals(board[i+2][j]) &&
                        board[i][j].equals(board[i+3][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    // Check for a diagonal win
    private boolean checkDiagonal(String player) {
        for (int i = 0; i < rows - 3; i++) {
            for (int j = 0; j < cols - 3; j++) {
                if (board[i][j] != null && board[i][j].equals(player) &&
                        board[i][j].equals(board[i+1][j+1]) &&
                        board[i][j].equals(board[i+2][j+2]) &&
                        board[i][j].equals(board[i+3][j+3])) {
                    return true;
                }
            }
        }

        for (int i = 3; i < rows; i++) {
            for (int j = 0; j < cols - 3; j++) {
                if (board[i][j] != null && board[i][j].equals(player) &&
                        board[i][j].equals(board[i-1][j+1]) &&
                        board[i][j].equals(board[i-2][j+2]) &&
                        board[i][j].equals(board[i-3][j+3])) {
                    return true;
                }
            }
        }
        return false;
    }

    // Place a disc in a column, returns the row where the disc lands

    public int placeDisc(int col, String player) {
        for (int i = rows - 1; i >= 0; i--) {
            if (board[i][col] == null) {
                board[i][col] = player;
                return i;  // Row where the disc landed
            }
        }
        return -1;  // Column is full
    }

    // Check if a column is full
    public boolean isColumnFull(int col) {
        return board[0][col] != null;
    }

    // Get the current player's turn
    public String getCurrentPlayer() {
        return currentPlayer;
    }

    // Switch turns
    public void switchPlayer() {
        currentPlayer = (currentPlayer.equals("RED")) ? "YELLOW" : "RED";
    }
    // Wrapper method to match the ClientThread logic
    public boolean makeMove(int col) {
        int row = placeDisc(col, currentPlayer);
        if (row != -1) {
            switchPlayer(); // After a valid move, switch player
            return true;
        }
        return false; // Invalid move (column full)
    }


    // Print the board (for debugging)
    public void printBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print((board[i][j] != null ? board[i][j].substring(0, 1) : "_") + " ");
            }
            System.out.println();
        }
    }
}
