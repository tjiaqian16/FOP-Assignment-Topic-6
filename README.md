# Einstein Würfelt Nicht (Einstein Does Not Play Dice)

**FOP Assignment - Topic 6**

This project is a Java-based implementation of the board game logic puzzle "Einstein Würfelt Nicht". It features a full Graphical User Interface (GUI) built with Java Swing, as well as a console-based version for testing logic. The game includes multiple modes, including a Human player, a Random bot, and an intelligent AI solver using the A* Algorithm.

## 📋 Features

* **Interactive GUI**: Built using Java Swing with custom components and background music.
* **3 Game Modes**:
    1.  **Human Player**: Play manually against the puzzle logic.
    2.  **Random Player**: Watch a bot make random valid moves.
    3.  **AI Player**: Watch an AI solve the puzzle using the **A* Search Algorithm**.
* **Level System**: 4 distinct levels with varying difficulties and dice sequences.
* **Leaderboard**: Tracks wins, losses, and player rankings (saved locally to `leaderboard.txt`).
* **Settings**: Toggle background music and sound effects.
* **Move Recording**: Game moves are logged to `moves.txt`.

## 🛠 Prerequisites

To run this project, you need:

* **Java Development Kit (JDK)**: **JDK 21** is recommended.
    * *Note: The Visual Studio Code Java extension requires a modern JDK (like JDK 21) to run the language server effectively.*
* **Visual Studio Code**: Recommended IDE.
* **Extension Pack for Java**: Install the official extension by Microsoft in VS Code.

---

## 🚀 How to Run in Visual Studio Code (Recommended)

1.  **Clone or Download** the repository.
2.  **Open the Project Folder**:
    * Open VS Code.
    * Go to `File` > `Open Folder...` and select the directory containing the `.java` files (e.g., `FOP-Assignment-Topic-6`).
    * *Important*: Ensure the images (`.png`, `.jpg`), audio (`.wav`), and text files (`.txt`) are in the root of the folder you opened. The program relies on relative paths to find these resources.
3.  **Wait for the Project to Load**:
    * VS Code will detect the Java files. Wait for the "Java Projects" view to populate in the bottom left.
4.  **Run the GUI**:
    * In the file explorer on the left, locate and click on **`MainInterface.java`**.
    * Click the **Run** button (Play icon) in the top right corner, or press `F5`.
5.  **Run the Console Version (Optional)**:
    * If you prefer a text-based interface, open **`GameMain.java`**.
    * Click the **Run** button.

## 💻 How to Run via Command Line (Terminal)

If you prefer using the terminal or command prompt:

1.  Navigate to the project directory:
    ```bash
    cd path/to/FOP-Assignment-Topic-6
    ```
2.  Compile all Java files:
    ```bash
    javac *.java
    ```
3.  Run the GUI Application:
    ```bash
    java MainInterface
    ```
    *(Or run `java GameMain` for the console version).*

---

## 🎮 Game Rules

The goal is to move your **Target Piece** (highlighted in Red) to **Square 0** (top-left corner).

1.  **Dice Roll**: A dice is rolled every turn (based on a pre-defined level sequence).
2.  **Movement**:
    * If the dice number matches a living piece, you **must** move that piece.
    * If the dice number matches a captured piece, you must choose either the next **lowest** available piece or the next **highest** available piece.
3.  **Capturing**: Moving onto a square occupied by another piece removes that piece from the board.
4.  **Winning/Losing**:
    * **Win**: The Target Piece reaches Square 0 within **30 moves**.
    * **Lose**: The Target Piece is captured, or you run out of moves.

## 📂 File Structure Overview

* **`MainInterface.java`**: The main entry point for the GUI application.
* **`GameMain.java`**: The entry point for the Console application.
* **`GamePanel.java`**: Handles the core game loop and board rendering in the GUI.
* **`AIPlayer.java`**: Contains the logic for the AI solver (A* Algorithm).
* **`GameState.java`**: Handles move generation and validation logic.
* **`SoundManager.java`**: Manages background music and sound effects.
* **Resources**:
    * `*.txt`: Level configurations and leaderboard data.
    * `*.png / *.jpg`: Game assets (icons, backgrounds).
    * `*.wav`: Audio files.

## 👥 Authors

* **502 BAD GATEWAY** 
