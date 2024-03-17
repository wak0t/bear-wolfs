import java.util.*;
import java.io.*;

// Piotr "wakot" Wakieć

class BoardGame {
    private Tile[][] board;
    private int width;
    private int height;
    private Player bear;
    private List<Player> wolves;
    private List<Sheep> sheeps;
    private Shepherd shepherd;
    private final Random random = new Random();

    public BoardGame(int width, int height, int sheepCount, int numberOfWolves) {
        // Validate board size constraints
        if (width < 7 || width > 40 || height < 7 || height > 40) {
            throw new IllegalArgumentException("Board size must be between 7x7 and 40x40.");
        }

        // Validate the number of wolves
        if (numberOfWolves < 1 || numberOfWolves > 3) {
            throw new IllegalArgumentException("The number of wolves must be between 1 and 3.");
        }

        this.width = width;
        this.height = height;
        this.board = new Tile[height][width];
        this.wolves = new ArrayList<>();
        this.sheeps = new ArrayList<>();

        // Validate sheep count constraints
        int maxSheep = (width * height) / 2; // Maximum number of sheep
        int minSheep = (width * height) / 5; // Minimum number of sheep
        if (sheepCount < minSheep || sheepCount > maxSheep) {
            throw new IllegalArgumentException("The number of sheep must be between " + minSheep + " and " + maxSheep + ".");
        }

        initializeBoard();
        addPlayersAndPieces(sheepCount, numberOfWolves);
    }

    private boolean isGameEnded() {
        boolean bearFinished = bear.getPosition().getX() == 0;
        boolean wolvesFinished = wolves.stream().allMatch(wolf -> wolf.getPosition().getX() == 0);
        return bearFinished && wolvesFinished;
    }

    private void initializeBoard() {
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                this.board[i][j] = new Tile(i, j);
            }
        }
    }

    private void addPlayersAndPieces(int sheepCount, int numberOfWolves) {
        int bearY = this.random.nextInt(this.width);
        this.bear = new Player('B', new Position(this.height - 1, bearY), "Bear");
        this.board[this.height - 1][bearY].setPlayer(this.bear);

        for (int i = 0; i < sheepCount; i++) {
            int x, y;
            do {
                x = this.random.nextInt(this.height - 1);
                y = this.random.nextInt(this.width);
            } while (this.board[x][y].isOccupied());
            Sheep sheep = new Sheep(new Position(x, y));
            this.sheeps.add(sheep);
            this.board[x][y].setSheep(sheep);
        }

        for (int i = 0; i < numberOfWolves; i++) {
            int wolfY;
            do {
                wolfY = random.nextInt(width);
            } while (board[height - 1][wolfY].isOccupied());
            Player wolf = new Player('W', new Position(height - 1, wolfY), "Wolf" + (i + 1));
            wolves.add(wolf);
            board[height - 1][wolfY].setPlayer(wolf);
        }

        int shepherdX, shepherdY;
        do {
            shepherdX = this.random.nextInt(this.height - 1);
            shepherdY = this.random.nextInt(this.width);
        } while (this.board[shepherdX][shepherdY].isOccupied());
        this.shepherd = new Shepherd(new Position(shepherdX, shepherdY));
        this.board[shepherdX][shepherdY].setShepherd(this.shepherd);

        printBoard();
    }

    public void printBoard() {
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                System.out.print(this.board[i][j]);
            }
            System.out.println();
        }
    }

    private void moveBearAndScore(int moves) {
        Position bearPos = this.bear.getPosition();
        if (bearPos.getX() == 0) {
            System.out.println("The Bear has already reached the goal.");
            return;
        }

        for (int i = 0; i < moves && bearPos.getX() - i >= 0; i++) {
            int newX = bearPos.getX() - i;
            Tile currentTile = this.board[newX][bearPos.getY()];

            if (currentTile.getSheep() != null) {
                this.bear.addScore();
                currentTile.setSheep(null);
                System.out.println("The Bear ate a sheep on the way and scored a point. Current score: " + this.bear.getScore());
            }

            if (currentTile.getShepherd() != null) {
                currentTile.setShepherd(null);
                System.out.println("The Bear ate the shepherd on the way.");
            }
        }

        int finalNewX = bearPos.getX() - moves;
        finalNewX = Math.max(finalNewX, 0);

        this.board[bearPos.getX()][bearPos.getY()].setPlayer(null);
        this.bear.setPosition(new Position(finalNewX, bearPos.getY()));
        this.board[finalNewX][bearPos.getY()].setPlayer(this.bear);

        if (this.board[finalNewX][bearPos.getY()].getSheep() != null) {
            System.out.println("The Bear ate a sheep on its tile and scored a point. Current score: " + this.bear.getScore());
            this.board[finalNewX][bearPos.getY()].setSheep(null);
        }

        if (this.board[finalNewX][bearPos.getY()].getShepherd() != null) {
            this.board[finalNewX][bearPos.getY()].setShepherd(null);
            System.out.println("The Bear ate the shepherd on its tile.");
        }

        printBoard();
    }

    public void playGame() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (!isGameEnded()) {
                System.out.println("--- New turn --- Press Enter to continue");
                reader.readLine();
                moveBear();
                moveWolves();
                printBoard();
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading from the console: " + e.getMessage());
        }
        endGame();
    }

    private void moveBear() {
        int bearRoll = rollDice();
        System.out.println("The Bear rolls the dice and got: " + bearRoll);
        moveBearAndScore(bearRoll);
    }

    private void moveWolves() {
        for (Player wolf : this.wolves) {
            int wolfRoll = rollDice();
            System.out.println(wolf.getId() + " rolls the dice and got: " + wolfRoll);
            moveWolfAndScore(wolf, wolfRoll);
        }
    }

    private int rollDice() {
        return this.random.nextInt(6) + 1;
    }

    private void endGame() {
        System.out.println("The game is over!");
        System.out.println("Bear's score: " + this.bear.getScore());

        // Determine the winner or if there's a tie
        Player winner = this.bear;
        boolean isTie = false;
        for (Player wolf : this.wolves) {
            System.out.println(wolf.getId() + ": " + wolf.getScore());
            if (wolf.getScore() > winner.getScore()) {
                winner = wolf;
                isTie = false;
            } else if (wolf.getScore() == winner.getScore()) {
                isTie = true;
            }
        }

        if (isTie) {
            System.out.println("The game ends in a tie!");
        } else {
            System.out.println("The winner is: " + winner.getId() + " with a score of: " + winner.getScore());
        }
    }

    private void moveWolfAndScore(Player wolf, int moves) {
        boolean moveRight = this.random.nextBoolean();
        for (int i = 0; i < moves; i++) {
            Position wolfPos = wolf.getPosition();
            if (wolfPos.getX() == 0) {
                System.out.println(wolf.getId() + " has already reached the goal.");
                return;
            }
            int newX = wolfPos.getX() - 1;
            int newY = wolfPos.getY() + (moveRight ? 1 : -1);

            if (newX < 0) newX = 0;
            if (newY < 0 || newY >= this.width) {
                moveRight = !moveRight;
                newY = wolfPos.getY() + (moveRight ? 1 : -1);
                newY = Math.max(Math.min(newY, this.width - 1), 0);
            }

            if (this.board[newX][newY].getSheep() != null) {
                wolf.addScore();
                this.board[newX][newY].setSheep(null);
                System.out.println(wolf.getId() + " ate a sheep and scored a point. Score " + wolf.getId() + ": " + wolf.getScore());
            }

            this.board[wolfPos.getX()][wolfPos.getY()].setPlayer(null);
            wolf.setPosition(new Position(newX, newY));
            this.board[newX][newY].setPlayer(wolf);
        }
    }

}

// Class representing a tile on the board
class Tile {
    private int x, y;
    private Player player;
    private Sheep sheep;
    private Shepherd shepherd;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setSheep(Sheep sheep) {
        this.sheep = sheep;
    }

    public void setShepherd(Shepherd shepherd) {
        this.shepherd = shepherd;
    }

    public boolean isOccupied() {
        return player != null || sheep != null || shepherd != null;
    }

    @Override
    public String toString() {
        if (shepherd != null) return "S ";
        if (player != null) return player.getSymbol() + " ";
        if (sheep != null) return "O ";
        return ". ";
    }

    public Sheep getSheep() {
        return sheep;
    }

    public Shepherd getShepherd() {
        return shepherd;
    }
}

// Class representing a player (bear or wolf)
class Player {
    private char symbol;
    private Position position;
    private int score;
    private String id;

    public Player(char symbol, Position position, String id) {
        this.symbol = symbol;
        this.position = position;
        this.id = id;
        this.score = 0;
    }

    public String getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public void addScore() {
        this.score++;
    }

    public char getSymbol() {
        return symbol;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}

// Class representing a sheep
class Sheep {
    private Position position;

    public Sheep(Position position) {
        this.position = position;
    }
}

// Class representing a shepherd
class Shepherd {
    private Position position;

    public Shepherd(Position position) {
        this.position = position;
    }
}

// Helper class for positions
class Position {
    private int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the board size (square): ");
        int size = scanner.nextInt();

        int maxSheep = (size * size) / 2;
        int minSheep = (size * size) / 5;
        System.out.println("Enter the number of sheep on the board (between " + minSheep + " and " + maxSheep + "): ");
        int sheepCount = scanner.nextInt();
        while (sheepCount < minSheep || sheepCount > maxSheep) {
            System.out.println("Invalid number of sheep. Please enter a number between " + minSheep + " and " + maxSheep + ": ");
            sheepCount = scanner.nextInt();
        }

        System.out.println("Enter the number of wolves on the board (1-3): ");
        int numberOfWolves = scanner.nextInt();
        while (numberOfWolves < 1 || numberOfWolves > 3) {
            System.out.println("Invalid number of wolves. Please enter a number between 1 and 3: ");
            numberOfWolves = scanner.nextInt();
        }

        BoardGame game = new BoardGame(size, size, sheepCount, numberOfWolves);
        game.playGame();
    }
}
