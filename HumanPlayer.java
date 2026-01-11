import java.util.List;
import java.util.Scanner;

public class HumanPlayer extends Player { 
    // Console Input
    private Scanner scanner;

    // GUI State
    private List<Integer> currentPossibleMoves;
    private int selectedPiece = -1;

    public HumanPlayer() {
        this.scanner = new Scanner(System.in);
    }

    // 1. CONSOLE METHOD (Delegated)
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

    @Override
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions) {
        return chooseMove(possibleMoves);
    }

    // 2. GUI METHODS
    public void setCurrentMoves(List<Integer> moves) {
        this.currentPossibleMoves = moves;
        this.selectedPiece = -1; 
    }

    public boolean canPieceMove(int pieceId) {
        if (currentPossibleMoves == null) return false;
        for (int move : currentPossibleMoves) {
            if (move / 100 == pieceId) return true;
        }
        return false;
    }

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