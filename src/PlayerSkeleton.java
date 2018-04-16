import java.util.OptionalDouble;
import java.util.stream.IntStream;

public class PlayerSkeleton {

    private static boolean SHOW_WINDOW = false;

    //implement this function to have a working system
    public int pickMove(State s, int[][] legalMoves) {

        return 0;
    }

    public static void main(String[] args) {
        PlayerSkeleton.playMultiple(5);
    }

    public static void play() {
        State s = new State();
        if (PlayerSkeleton.SHOW_WINDOW) {
            new TFrame(s);
        }
        PlayerSkeleton p = new PlayerSkeleton();
        while (!s.hasLost()) {
            s.makeMove(p.pickMove(s, s.legalMoves()));
            s.draw();
            s.drawNext(0, 0);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("You have completed " + s.getRowsCleared() + " rows.");
    }

    public static void playMultiple(int n) {
        OptionalDouble avgScore = IntStream.range(0, n).parallel().map(i -> {
            State s = new State();
            if (PlayerSkeleton.SHOW_WINDOW) {
                new TFrame(s);
            }
            PlayerSkeleton p = new PlayerSkeleton();
            while (!s.hasLost()) {
                s.makeMove(p.pickMove(s, s.legalMoves()));
            }
            return s.getRowsCleared();
        }).average();

        System.out.println("You have completed " + avgScore.getAsDouble() + " rows.");
    }

}
