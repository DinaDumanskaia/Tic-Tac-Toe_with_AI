package tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static tictactoe.GAME_STATUS.*;
import static tictactoe.Main.Controller.checkField;
import static tictactoe.Main.Controller.isFieldHasEmptyCells;

public class Main {
    private final static char EMPTY_SYMBOL = ' ';
    private final static char X_SYMBOL = 'X';
    private final static char O_SYMBOL = 'O';
    private final static boolean AI_TURN = true;
    private final static boolean USER_TURN = false;

    public static void main(String[] args) {
        // objects initialization
        Model model = new Model();
        View view = new View();
        Controller controller = new Controller();
        controller.setModel(model);
        controller.setView(view);
        view.setController(controller);
        Scanner scanner = new Scanner(System.in);

        // get initial field state
        // String userInput = scanUserInput(scanner);
        // todo: create a loop
        String inputCommand;
        do {
            Player player1;
            Player player2;
            do {
                inputCommand = readUserTask(scanner);
                if (inputCommand.equals("exit")) {
                    return;
                }
                String[] command = inputCommand.split(" ");
                player1 = factoryMethod(command[0]);
                player2 = factoryMethod(command[1]);
            } while (player1 == null || player2 == null);

            playGame(scanner, controller, view, player1, player2);
        } while (!inputCommand.equals("exit"));
    }

    private static Player factoryMethod(String playerType) {
        Player retValue = null;
        switch (playerType) {
            case "easy":
                retValue = new EasyAI();
                break;
            case "medium":
                retValue = new MediumAI();
                break;
            case "hard":
                retValue = new HardAI();
                break;
            case "user":
                retValue = new Human();
                break;
            default:
                System.out.println("Bad parameters!");
                break;

        }
        return retValue;
    }

    private static String readUserTask(Scanner scanner) {
        String toReturn = "";
        String input;
        boolean isInputIncorrect = true;
        do {
            System.out.print("Input command: ");
            input = scanner.nextLine();
            String[] parameters = input.split(" ");
            if (parameters.length == 3 && parameters[0].equals("start")) {
                isInputIncorrect = false;
                toReturn = parameters[1] + " " + parameters[2];
            } else if (parameters.length == 1 && parameters[0].equals("exit")) {
                isInputIncorrect = false;
                toReturn = "exit";
            } else {
                System.out.println("Bad parameters!");
            }
        } while (isInputIncorrect);
        return toReturn;
    }

    private static void playGame(Scanner scanner, Controller controller, View view, Player player1, Player player2) {
        controller.initModel("_________");
        view.showField();
        String status = "Game not finished";

        while (status.equals("Game not finished")) {
            player1.makeStep(controller, scanner, view);
            status = controller.getCurrentGameState();
            if (!status.equals("Game not finished")) {
                break;
            }
            player2.makeStep(controller, scanner, view);
            status = controller.getCurrentGameState();
        }

        view.printGameState();
    }

    private static Coordinate scanUserStep(Controller controller, Scanner scanner, Move move) {
        String input;
        String[] numbers;
        boolean correctCoordinates = false;
        int x = 0;
        int y = 0;
        while (!correctCoordinates) {
            System.out.print("Enter the coordinates: ");
            input = scanner.nextLine();
            numbers = input.split(" ");
            try {
                x = Integer.parseInt(numbers[0]);
                y = Integer.parseInt(numbers[1]);
                if (controller.isCoordinatesValid(x, y, move)) {
                    correctCoordinates = true;
                }
            } catch (NumberFormatException exception) {
                System.out.println("You should enter numbers!");
            }
        }
        return new Coordinate(x, y);
    }


    private static String scanUserInput(Scanner scanner) {
        System.out.print("Enter the cells: ");
        return scanner.nextLine();
    }


    static class Coordinate {
        int x, y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class View {
        Controller controller;

        void setController(Controller controller) {
            this.controller = controller;
        }

        void showField() {
            char[][] field = controller.getField();
            System.out.println("---------");
            for (char[] aField : field) {
                System.out.print('|' + " ");
                for (int y = 0; y < field.length; y++) {
                    System.out.print(aField[y] + " ");
                }
                System.out.print('|');
                System.out.println();
            }
            System.out.println("---------");
        }

        public void printGameState() {
            String result = controller.getCurrentGameState();
            System.out.println(result);
        }
    }

    static class Controller {
        Model model;
        View view;

        public void setModel(Model model) {
            this.model = model;
        }

        public void setView(View view) {
            this.view = view;
        }

        public void initModel(String userInput) {
            int letterIndex = 0;

            for (char c : userInput.toCharArray()) {
                int x = letterIndex / model.getFieldSize();
                int y = letterIndex % model.getFieldSize();
                switch (c) {
                    case 'X':
                        model.setX(x, y);
                        break;
                    case 'O':
                        model.setO(x, y);
                        break;
                    case '_':
                        model.setEmpty(x, y);
                        break;

                    default:
                        throw new IllegalArgumentException("Unexpected input:" + c);
                }
                letterIndex++;
            }
        }

        public void setStep(Coordinate userStep) {
            model.setNextStep(userStep.x, userStep.y);
        }

        public void setStep(Coordinate userStep, char toPut) {
            model.setNextStep(userStep.x, userStep.y, toPut);
        }

        public char[][] getField() {
            return model.getField();
        }

        public String getCurrentGameState() {
            char[][] field = model.getField();
            if (checkField(field, 'X')) {
                return 'X' + " wins";
            } else if (checkField(field, 'O')) {
                return 'O' + " wins";
            } else if (!isFieldHasEmptyCells(field)) {
                return "Draw";
            } else {
                return "Game not finished";
            }
        }

        public static boolean isFieldHasEmptyCells(char[][] field) {
            for (char[] aField : field) {
                for (int y = 0; y < field.length; y++) {
                    if (aField[y] == EMPTY_SYMBOL) {
                        return true;
                    }
                }
            }
            return false;
        }

        public static boolean checkField (char[][] field, char symbol) {
            return checkVerticals(field, symbol) || checkHorizontals(field, symbol) || checkDiagonals(field, symbol);
        }

        private static boolean checkDiagonals(char[][] field, char letter) {
            List<Boolean> listMainDiagonal = new ArrayList<>();
            List<Boolean> listSecondDiagonal = new ArrayList<>();
            for (int u = 0; u < field.length; u++) {
                if (field[u][u] == letter) {
                    listMainDiagonal.add(true);
                } else {
                    listMainDiagonal.clear();
                    break;
                }
            }
            for (int i = 0; i < field.length; i++) {
                if (field[i][field.length - 1 - i] == letter) {
                    listSecondDiagonal.add(true);
                } else {
                    listSecondDiagonal.clear();
                    break;
                }
            }
            return listMainDiagonal.size() != 0 || listSecondDiagonal.size() != 0;
        }

        private static boolean checkHorizontals(char[][] field, char letter) {
            List<Boolean> list = new ArrayList<>();
            for (char[] aField : field) {
                for (int x = 0; x < field.length; x++) {
                    if (aField[x] == letter) {
                        list.add(true);
                    } else {
                        list.clear();
                        break;
                    }
                }
                if (list.size() != 0) {
                    return true;
                }
            }
            return false;
        }

        private static boolean checkVerticals(char[][] field, char letter) {
            List<Boolean> list = new ArrayList<>();
            for (int x = 0; x < field.length; x++) {
                for (int y = 0; y < field.length; y++) {
                    if (field[y][x] == letter) {
                        list.add(true);
                    } else {
                        list.clear();
                        break;
                    }
                }
                if (list.size() != 0) {
                    return true;
                }
            }
            return false;
        }

        public boolean isCoordinatesValid(int x, int y, Move move) {
            if (checkCoordinateBoundaries(x, y)) {
                if (checkCoordinatesNotOccupied(x, y)) {
                    return true;
                } else {
                    if (move == Move.USER) {
                        System.out.println("This cell is occupied! Choose another one!");
                    }
                }
            } else {
                if (move == Move.USER) {
                    System.out.println("Coordinates should be from 1 to 3!");
                }
            }
            return false;
        }

        public boolean checkCoordinateBoundaries(int x, int y) {
            return x >= 1 && x <= model.getFieldSize() && y >= 1 && y <= model.getFieldSize();
        }

        public boolean checkCoordinatesNotOccupied(int x, int y) {
            return model.cellIsFree(x, y);
        }
    }

    static class Model {
        static final int FIELD_SIZE = 3;
        char[][] field = new char[FIELD_SIZE][FIELD_SIZE];

        public int getFieldSize() {
            return FIELD_SIZE;
        }

        public void setX(int x, int y) {
            field[x][y] = 'X';
        }

        public void setO(int x, int y) {
            field[x][y] = 'O';
        }

        public void setEmpty(int x, int y) {
            field[x][y] = EMPTY_SYMBOL;
        }

        public void setNextStep(int x, int y) {
            char symbol = getNextSymbol();
            field[x - 1][y - 1] = symbol;
        }

        public void setNextStep(int x, int y, char toPut) {
            field[x][y] = toPut;
        }

        private char getNextSymbol() {
            int xCount = getSymbolCount('X');
            int yCount = getSymbolCount('O');
            return xCount <= yCount ? 'X' : 'O';
        }

        private int getSymbolCount(char symbol) {
            int counter = 0;
            for (int x = 0; x < Model.FIELD_SIZE; x++) {
                for (int y = 0; y < Model.FIELD_SIZE; y++) {
                    if (field[x][y] == symbol) {
                        counter++;
                    }
                }
            }
            return counter;
        }

        public char[][] getField() {
            return field;
        }

        public boolean cellIsFree(int x, int y) {
            return field[x - 1][y - 1] == EMPTY_SYMBOL;
        }
    }

    enum Move {
        USER, COMPUTER
    }

    interface Player {
        void makeStep(Controller controller, Scanner scanner, View view);
    }

    static class Human implements Player {

        @Override
        public void makeStep(Controller controller, Scanner scanner, View view) {
            Coordinate userStep = scanUserStep(controller, scanner, Move.USER);
            controller.setStep(userStep);
            view.showField();
        }
    }

    interface AI extends Player {

    }

    static class EasyAI implements AI {
        @Override
        public void makeStep(Controller controller, Scanner scanner, View view) {
            System.out.println("Making move level \"easy\"");
            Random random = new Random();
            int x = 0;
            int y = 0;
            while (!controller.isCoordinatesValid(x, y, Move.COMPUTER)) {
                x = random.nextInt(4);
                y = random.nextInt(4);
            }
            Coordinate autoInput = new Coordinate(x, y);
            controller.setStep(autoInput);
            view.showField();
        }

    }

    static class MediumAI implements AI {

        @Override
        public void makeStep(Controller controller, Scanner scanner, View view) {
            char[][] field = controller.getField();
            System.out.println("Making move level \"medium\"");
            Random random = new Random();
            int x = 0;
            int y = 0;
            //todo: проверка поля на наличие двух в вряду
            char toPut = controller.model.getNextSymbol();
            char opponentToPut = EMPTY_SYMBOL;
            if (toPut == 'X') {
                opponentToPut = 'O';
            } else if (toPut == 'O') {
                opponentToPut = 'X';
            }

            Coordinate coordinateToCompleteLine;
            if ((coordinateToCompleteLine = isTwoInARow(toPut, field)) != null) {  //If it already has two in a row and can win with one further move, it does so.
                putThirdInARow(toPut, coordinateToCompleteLine, controller);
            } else if ((coordinateToCompleteLine = isTwoInARow(opponentToPut, field)) != null) { //If its opponent can win with one move, it plays the move necessary to block this.
                putThirdInARow(toPut, coordinateToCompleteLine, controller);
            } else {  //Otherwise, it makes a random move.
                while (!controller.isCoordinatesValid(x, y, Move.COMPUTER)) {
                    x = random.nextInt(4);
                    y = random.nextInt(4);
                }
                Coordinate autoInput = new Coordinate(x, y);
                controller.setStep(autoInput);
            }
            view.showField();
        }

        private void putThirdInARow(char toPut, Coordinate coordinateToCompleteLine, Controller controller) {
            controller.setStep(coordinateToCompleteLine, toPut);
        }

        private Coordinate isTwoInARow(char symbol, char[][] field) {
            Coordinate toFill = null;
            for (int i = 0; i < 3; i++) {
                if (field[i][0] == symbol && field[i][1] == symbol && field[i][2] == ' ') {
                    toFill = new Coordinate(i, 2);
                    break;
                } else if (field[i][1] == symbol && field[i][2] == symbol && field[i][0] == ' ') {
                    toFill = new Coordinate(i, 0);
                    break;
                } else if (field[i][0] == symbol && field[i][2] == symbol && field[i][1] == ' ') {
                    toFill = new Coordinate(i, 1);
                    break;
                } else if (field[0][i] == symbol && field[1][i] == symbol && field[2][i] == ' ') {
                    toFill = new Coordinate(2, i);
                    break;
                } else if (field[1][i] == symbol && field[2][i] == symbol && field[0][i] == ' ') {
                    toFill = new Coordinate(0, i);
                    break;
                } else if (field[0][i] == symbol && field[2][i] == symbol && field[1][i] == ' ') {
                    toFill = new Coordinate(1, i);
                    break;
                }
            }
            if (toFill == null) {
                if (field[0][0] == symbol && field[1][1] == symbol && field[2][2] == ' ') {
                    toFill = new Coordinate(2, 2);
                } else if (field[0][0] == symbol && field[2][2] == symbol && field[1][1] == ' ') {
                    toFill = new Coordinate(1, 1);
                } else if (field[1][1] == symbol && field[2][2] == symbol && field[0][0] == ' ') {
                    toFill = new Coordinate(0, 0);
                } else if (field[0][2] == symbol && field[1][1] == symbol && field[2][0] == ' ') {
                    toFill = new Coordinate(2, 0);
                } else if (field[0][2] == symbol && field[2][0] == symbol && field[1][1] == ' ') {
                    toFill = new Coordinate(1, 1);
                } else if (field[1][1] == symbol && field[2][0] == symbol && field[0][2] == ' ') {
                    toFill = new Coordinate(0, 2);
                }
            }
            return toFill;
        }
    }

    static class HardAI implements AI {
        @Override
        public void makeStep(Controller controller, Scanner scanner, View view) {
            System.out.println("Making move level \"hard\"");
            int xCoord = 0;
            int yCoord = 0;
            char toPut = controller.model.getNextSymbol();

            int bestScore = Integer.MIN_VALUE;
            int score;
            Coordinate bestMove;

            char[][] board = controller.getField().clone();
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    if(board[x][y] == EMPTY_SYMBOL) {
                        board[x][y] = toPut;
                        score = miniMax(board, USER_TURN, convertedSymbol(toPut), controller);
                        board[x][y] = EMPTY_SYMBOL;
                        if (score > bestScore) {
                            bestScore = score;
                            xCoord = x;
                            yCoord = y;
                        }
                    }
                }
            }
            bestMove = new Coordinate(xCoord, yCoord);
            controller.setStep(bestMove, toPut);
            view.showField();
        }

        private char convertedSymbol(char toPut) {
            if (toPut == X_SYMBOL) {
                return O_SYMBOL;
            } else if (toPut == O_SYMBOL) {
                return X_SYMBOL;
            }
            return EMPTY_SYMBOL;
        }

        private int miniMax(char[][] board, boolean isAITurn, char toPut, Controller controller) {
            int OPPONENT_WINS_REWARD = -100;
            int THIS_AI_WINS_REWARD = 100;
            int DRAW_REWARD = 0;

            int FIELD_SIZE = controller.model.getFieldSize();
            GAME_STATUS status = getCurrentGameState(board);

            int bestScore = 5;
            if (status.equals(NOT_FINISHED)) {
                if (isAITurn) {
                    bestScore = Integer.MIN_VALUE;
                    for (int x = 0; x < FIELD_SIZE; x++) {
                        for (int y = 0; y < FIELD_SIZE; y++) {
                            if (board[x][y] == EMPTY_SYMBOL) {
                                board[x][y] = toPut;
                                int score = miniMax(board, USER_TURN, convertedSymbol(toPut), controller);
                                bestScore = Integer.max(bestScore, score);
                                board[x][y] = EMPTY_SYMBOL;
                            }
                        }
                    }
                } else {
                    bestScore = Integer.MAX_VALUE;
                    for (int x = 0; x < FIELD_SIZE; x++) {
                        for (int y = 0; y < FIELD_SIZE; y++) {
                            if (board[x][y] == EMPTY_SYMBOL) {
                                board[x][y] = toPut;
                                int score = miniMax(board, AI_TURN, convertedSymbol(toPut), controller);
                                bestScore = Integer.min(bestScore, score);
                                board[x][y] = EMPTY_SYMBOL;
                            }
                        }
                    }
                }
            } else if (!isAITurn && status.equals(WIN_X)) { //todo: проверить на универсальность возвращаемого значения
                return THIS_AI_WINS_REWARD;
            } else if (isAITurn && status.equals(WIN_X)) {
                return OPPONENT_WINS_REWARD;
            } else if (!isAITurn && status.equals(WIN_O)) {
                return THIS_AI_WINS_REWARD;
            } else if (isAITurn && status.equals(WIN_O)) {
                return OPPONENT_WINS_REWARD;
            } else if (status.equals(DRAW)) {
                return DRAW_REWARD;
            }
            return bestScore;
        }

        private GAME_STATUS getCurrentGameState(char[][] field) {
            if (checkField(field, 'X')) {
                return WIN_X;
            } else if (checkField(field, 'O')) {
                return WIN_O;
            } else if (!isFieldHasEmptyCells(field)) {
                return DRAW;
            } else {
                return NOT_FINISHED;
            }
        }
    }
}

enum GAME_STATUS {
    WIN_X,
    WIN_O,
    DRAW,
    NOT_FINISHED
}

