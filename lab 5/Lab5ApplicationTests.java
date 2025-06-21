import java.util.Comparator;
import java.util.Scanner;

class Player implements Comparable<Player> {
    private String playerName;
    private int wins;
    private int draws;
    private int losses;

    public Player(String playerName) {
        this.playerName = playerName;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
    }

    public void addWin() { wins++; }
    public void addDraw() { draws++; }
    public void addLoss() { losses++; }

    public double winRate() {
        int total = wins + draws + losses;
        return total == 0 ? 0 : (double)wins / total;
    }

    public String getPlayerName() { return playerName; }
    public int getWins() { return wins; }

    public int compareTo(Player other) {
        return this.playerName.compareTo(other.playerName);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return playerName.equals(player.playerName);
    }

    public int hashCode() {
        return playerName.hashCode();
    }
}

interface BST<K extends Comparable<K>, V> {
    void put(K key, V value);
    V get(K key);
    void delete(K key);
    Iterable<V> rangeSearch(K lo, K hi);
    Iterable<V> successors(K key);
}

class SimpleBST<K extends Comparable<K>, V> implements BST<K, V> {
    private Node root;

    private class Node {
        K key;
        V value;
        Node left, right;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private Node put(Node x, K key, V value) {
        if (x == null) return new Node(key, value);
        int cmp = key.compareTo(x.key);
        if (cmp < 0) x.left = put(x.left, key, value);
        else if (cmp > 0) x.right = put(x.right, key, value);
        else x.value = value;
        return x;
    }

    public V get(K key) {
        return get(root, key);
    }

    private V get(Node x, K key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp < 0) return get(x.left, key);
        else if (cmp > 0) return get(x.right, key);
        else return x.value;
    }

    public void delete(K key) {
        root = delete(root, key);
    }

    private Node delete(Node x, K key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp < 0) x.left = delete(x.left, key);
        else if (cmp > 0) x.right = delete(x.right, key);
        else {
            if (x.right == null) return x.left;
            if (x.left == null) return x.right;
            Node t = x;
            x = min(t.right);
            x.right = deleteMin(t.right);
            x.left = t.left;
        }
        return x;
    }

    private Node min(Node x) {
        if (x.left == null) return x;
        return min(x.left);
    }

    private Node deleteMin(Node x) {
        if (x.left == null) return x.right;
        x.left = deleteMin(x.left);
        return x;
    }

    public Iterable<V> rangeSearch(K lo, K hi) {
        java.util.List<V> list = new java.util.ArrayList<>();
        rangeSearch(root, list, lo, hi);
        return list;
    }

    private void rangeSearch(Node x, java.util.List<V> list, K lo, K hi) {
        if (x == null) return;
        int cmplo = lo.compareTo(x.key);
        int cmphi = hi.compareTo(x.key);
        if (cmplo < 0) rangeSearch(x.left, list, lo, hi);
        if (cmplo <= 0 && cmphi >= 0) list.add(x.value);
        if (cmphi > 0) rangeSearch(x.right, list, lo, hi);
    }

    public Iterable<V> successors(K key) {
        java.util.List<V> list = new java.util.ArrayList<>();
        successors(root, list, key);
        return list;
    }

    private void successors(Node x, java.util.List<V> list, K key) {
        if (x == null) return;
        int cmp = key.compareTo(x.key);
        if (cmp < 0) {
            successors(x.left, list, key);
            list.add(x.value);
            return;
        }
        successors(x.right, list, key);
    }
}

interface HashST<K, V> {
    void put(K key, V value);
    V get(K key);
    void delete(K key);
    boolean contains(K key);
}

class SimpleHashST<K, V> implements HashST<K, V> {
    private static final int INIT_CAPACITY = 16;
    private int size;
    private Node[] st;

    private static class Node {
        Object key;
        Object value;
        Node next;

        Node(Object key, Object value, Node next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    public SimpleHashST() {
        this(INIT_CAPACITY);
    }

    public SimpleHashST(int capacity) {
        st = new Node[capacity];
    }

    private int hash(K key) {
        return (key.hashCode() & 0x7fffffff) % st.length;
    }

    public void put(K key, V value) {
        if (size >= 10*st.length) resize(2*st.length);
        int i = hash(key);
        for (Node x = st[i]; x != null; x = x.next) {
            if (key.equals(x.key)) {
                x.value = value;
                return;
            }
        }
        st[i] = new Node(key, value, st[i]);
        size++;
    }

    public V get(K key) {
        int i = hash(key);
        for (Node x = st[i]; x != null; x = x.next) {
            if (key.equals(x.key)) return (V)x.value;
        }
        return null;
    }

    public void delete(K key) {
        int i = hash(key);
        st[i] = delete(st[i], key);
        if (size > 0 && size <= 2*st.length) resize(st.length/2);
    }

    private Node delete(Node x, K key) {
        if (x == null) return null;
        if (key.equals(x.key)) {
            size--;
            return x.next;
        }
        x.next = delete(x.next, key);
        return x;
    }

    public boolean contains(K key) {
        return get(key) != null;
    }

    private void resize(int chains) {
        SimpleHashST<K, V> temp = new SimpleHashST<>(chains);
        for (int i = 0; i < st.length; i++) {
            for (Node x = st[i]; x != null; x = x.next) {
                temp.put((K)x.key, (V)x.value);
            }
        }
        this.st = temp.st;
    }
}

class Scoreboard {
    private BST<Integer, Player> winTree;
    private HashST<String, Player> players;
    private int playedGames;

    public Scoreboard() {
        this.winTree = new SimpleBST<>();
        this.players = new SimpleHashST<>();
        this.playedGames = 0;
    }

    public void addGameResult(String winnerPlayerName, String looserPlayerName, boolean draw) {
        playedGames++;

        Player winner = players.get(winnerPlayerName);
        Player looser = players.get(looserPlayerName);

        if (winner == null || looser == null) return;

        winTree.delete(winner.getWins());
        winTree.delete(looser.getWins());

        if (draw) {
            winner.addDraw();
            looser.addDraw();
        } else {
            winner.addWin();
            looser.addLoss();
        }

        winTree.put(winner.getWins(), winner);
        winTree.put(looser.getWins(), looser);
    }

    public void registerPlayer(String playerName) {
        if (!players.contains(playerName)) {
            Player player = new Player(playerName);
            players.put(playerName, player);
            winTree.put(0, player);
        }
    }

    public boolean checkPlayer(String playerName) {
        return players.contains(playerName);
    }

    public Player[] winRange(int lo, int hi) {
        java.util.List<Player> result = new java.util.ArrayList<>();
        for (Player p : winTree.rangeSearch(lo, hi)) {
            result.add(p);
        }
        return result.toArray(new Player[0]);
    }

    public Player[] winSuccessor(int wins) {
        java.util.List<Player> result = new java.util.ArrayList<>();
        for (Player p : winTree.successors(wins)) {
            result.add(p);
        }
        return result.toArray(new Player[0]);
    }
}

class ConnectFour {
    private char[][] grid;
    public char currentSymbol;

    public char getCurrentSymbol() {
        return currentSymbol;
    }

    public ConnectFour() {
        grid = new char[6][7];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                grid[i][j] = ' ';
            }
        }
        currentSymbol = 'X';
    }

    public boolean makeMove(int col) {
        if (col < 0 || col >= 7) return false;

        for (int row = 5; row >= 0; row--) {
            if (grid[row][col] == ' ') {
                grid[row][col] = currentSymbol;
                currentSymbol = (currentSymbol == 'X') ? 'O' : 'X';
                return true;
            }
        }
        return false;
    }

    public int isGameOver() {
        if (checkWin('X')) return 1;
        if (checkWin('O')) return 2;

        boolean isFull = true;
        for (int col = 0; col < 7; col++) {
            if (grid[0][col] == ' ') {
                isFull = false;
                break;
            }
        }
        if (isFull) return 3;

        return 0;
    }

    private boolean checkWin(char symbol) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                if (grid[row][col] == symbol &&
                        grid[row][col+1] == symbol &&
                        grid[row][col+2] == symbol &&
                        grid[row][col+3] == symbol) {
                    return true;
                }
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 7; col++) {
                if (grid[row][col] == symbol &&
                        grid[row+1][col] == symbol &&
                        grid[row+2][col] == symbol &&
                        grid[row+3][col] == symbol) {
                    return true;
                }
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (grid[row][col] == symbol &&
                        grid[row+1][col+1] == symbol &&
                        grid[row+2][col+2] == symbol &&
                        grid[row+3][col+3] == symbol) {
                    return true;
                }
            }
        }

        for (int row = 3; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                if (grid[row][col] == symbol &&
                        grid[row-1][col+1] == symbol &&
                        grid[row-2][col+2] == symbol &&
                        grid[row-3][col+3] == symbol) {
                    return true;
                }
            }
        }

        return false;
    }

    public void printBoard() {
        System.out.println(" 0 1 2 3 4 5 6");
        for (int row = 0; row < 6; row++) {
            System.out.print("|");
            for (int col = 0; col < 7; col++) {
                System.out.print(grid[row][col] + "|");
            }
            System.out.println();
        }
        System.out.println("---------------");
    }
}


class Game {
    private String status;
    private String winnerPlayerName;
    private String playerNameA;
    private String playerNameB;
    private ConnectFour connectFour;

    public Game(String playerNameA, String playerNameB) {
        this.status = "en partida";
        this.winnerPlayerName = "";
        this.playerNameA = playerNameA;
        this.playerNameB = playerNameB;
        this.connectFour = new ConnectFour();
    }

    public String play() {
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        while (status.equals("en partida")) {
            connectFour.printBoard();
            System.out.println("Turno de " + (connectFour.getCurrentSymbol() == 'X' ? playerNameA : playerNameB));
            System.out.print("Ingrese columna: ");
            int col = scanner.nextInt();

            if (!connectFour.makeMove(col)) {
                System.out.println("Movimiento inválido. Intente nuevamente.");
                continue;
            }

            int result = connectFour.isGameOver();
            if (result == 1) {
                status = "VICTORY";
                winnerPlayerName = playerNameA;
            } else if (result == 2) {
                status = "VICTORY";
                winnerPlayerName = playerNameB;
            } else if (result == 3) {
                status = "DRAW";
                winnerPlayerName = "";
            }
        }

        connectFour.printBoard();
        if (status.equals("VICTORY")) {
            System.out.println("Ganador: " + winnerPlayerName);
        }

        return winnerPlayerName;
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Scoreboard scoreboard = new Scoreboard();

        scoreboard.registerPlayer("Jugador1");
        scoreboard.registerPlayer("Jugador2");
        while(true){
            Game game = new Game("Jugador1", "Jugador2");
            String winner = game.play();

            if (!winner.isEmpty()) {
                String loser = winner.equals("Jugador1") ? "Jugador2" : "Jugador1";
                scoreboard.addGameResult(winner, loser, false);
            } else {
                scoreboard.addGameResult("Jugador1", "Jugador2", true);
            }

            Player[] topPlayers = scoreboard.winRange(1, 10);
            System.out.println("\nTop jugadores:");
            for (Player p : topPlayers) {
                System.out.println(p.getPlayerName() + ": " + p.getWins() + " victorias");
            }


            System.out.println ("¿Fin de la partida?  (1)yes o (0)no ");
            int  a = scanner.nextInt();
            if(a==1)
            {
                break;
            }

        }
    }

}