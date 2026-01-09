import java.util.List;
import java.util.Scanner;

public class HumanPlayer extends Player { 
    // Console Input
    private Scanner scanner;

    // --- GUI STATE FIELDS ---
    // These store the current situation for the Interface
    private List<Integer> currentPossibleMoves;
    private int selectedPiece = -1;

    public HumanPlayer() {
        this.scanner = new Scanner(System.in);
    }

    // ---------------------------------------------------------
    // 1. CONSOLE METHOD (Keeps GameMain.java working)
    // ---------------------------------------------------------
    public int chooseMove(List<Integer> possibleMoves) {
        System.out.println("--- Human Player's Turn ---");
        System.out.println("Available Moves:");
        for (int i = 0; i < possibleMoves.size(); i++) {
            int move = possibleMoves.get(i);
            System.out.println("[" + i + "] Piece " + (move / 100) + " to Square " + (move % 100));
        }
        int choice = -1;
        while (true) {
            System.out.print("Select move index: ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                if (choice >= 0 && choice < possibleMoves.size()) break;
            } else {
                scanner.next();
            }
        }
        return possibleMoves.get(choice);
    }

    // ---------------------------------------------------------
    // 2. GUI METHODS (Connects to GamePanel)
    // ---------------------------------------------------------
    
    /**
     * Called by GamePanel when a new turn starts.
     */
    public void setCurrentMoves(List<Integer> moves) {
        this.currentPossibleMoves = moves;
        this.selectedPiece = -1; // Reset selection
    }

    /**
     * Checks if a specific piece (1-6) is allowed to be selected.
     */
    public boolean canPieceMove(int pieceId) {
        if (currentPossibleMoves == null) return false;
        for (int move : currentPossibleMoves) {
            if (move / 100 == pieceId) return true;
        }
        return false;
    }

    /**
     * Attempts to select a piece. Returns true if valid.
     */
    public boolean selectPiece(int pieceId) {
        if (canPieceMove(pieceId)) {
            this.selectedPiece = pieceId;
            return true;
        }
        return false;
    }
    
    public int getSelectedPiece() {
        return selectedPiece;
    }

    /**
     * Checks if the clicked square is a valid destination for the selected piece.
     */
    public boolean isValidDestination(int destIndex) {
        if (selectedPiece == -1 || currentPossibleMoves == null) return false;
        for (int move : currentPossibleMoves) {
            if (move / 100 == selectedPiece && move % 100 == destIndex) {
                return true;
            }
        }
        return false;
    }
}