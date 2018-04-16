public class PlayerSkeleton {
    private static final int W_LINES = 0;
    private static final int W_HEIGHT = 1;
    private static final int W_HOLES = 2;
    private static final int W_BUMPINESS = 3;
    private static final double[] WEIGHTS = {0.510066, -0.760666, -0.35663, -0.184483};

    /**
     * Fit one block only since no future blocks and no swap buffer
     * currently fixed weight
     * TODO: genetic algorithm
     *
     * @param s
     * @param legalMoves
     * @return picked move
     */
    public int[] pickMove(State s, int[][] legalMoves) {
        int[] best = {0, 0};
        double bestScore = -10000;

        for (int[] legalMove : legalMoves) {
            int[][] field = tryMove(s, legalMove[0], legalMove[1]);
            double score = -10000;
            if (field == null) {
//                System.out.println("Invalid: " + legalMove[0] + ", " + legalMove[1]);
            } else {
                score = WEIGHTS[W_LINES] * calculateLines(field) +
                        WEIGHTS[W_HEIGHT] * calculateHeight(field) +
                        WEIGHTS[W_HOLES] * calculateHoles(field) +
                        WEIGHTS[W_BUMPINESS] * calculateBumpiness(field);
            }
            if (score > bestScore || bestScore == -1) {
                bestScore = score;
                best = legalMove;
            }
        }
//        System.out.println("Best Score:" + bestScore);
//        System.out.println("Best:" + best[0] + "," + best[1]);
        return best;
    }

    /**
     * steepness
     *
     * @param field
     * @return sum of absolute differences between adjacent columns [0,len-1]
     */
    private double calculateBumpiness(int[][] field) {
        int total = 0;
        for (int c = 0; c < State.COLS - 1; c++) {
            int hcl = -1;
            int hcr = -1;
            for (int r = State.ROWS - 1; r >= 0; r--) {
                if (field[r][c] != 0) {
                    hcl = r;
                }
                if (field[r][c + 1] != 0) {
                    hcr = r;
                }
                if (hcl != -1 && hcr != -1)
                    break;
            }
            total += Math.abs(hcl - hcr);
        }
        return total;
    }

    /**
     * TODO: clear doubts about holes calculation
     * there are three utterly different implementations
     * Be Careful!!!
     * now is counting all non-block even above height
     *
     * @param field
     * @return hole count
     */
    private int calculateHoles(int[][] field) {
        int count = 0;
        for (int c = 0; c < State.COLS; c++) {
            boolean block = false;
            for (int r = State.ROWS - 1; r >= 0; r--) {
                if (field[r][c] != 0) {
                    block = true;
                } else if (block && field[r][c] == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * sum of top of all columns
     *
     * @param field
     * @return total height
     */
    private int calculateHeight(int[][] field) {
        int total = 0;
        for (int c = 0; c < State.COLS; c++) {
            for (int r = State.ROWS - 1; r >= 0; r--) {
                if (field[r][c] != 0) {
                    total += r;
                    break;
                }
            }
        }
        return total;
    }

    /**
     * number of full lines without removal
     *
     * @param field
     * @return number of full lines
     */
    private int calculateLines(int[][] field) {
        int count = 0;
        for (int r = 0; r < State.ROWS; r++) {
            boolean full = true;
            for (int c = 0; c < State.COLS; c++) {
                if (field[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                count++;
            }
        }
        return count;
    }

    /**
     * Similar to State::makeMove(int, int)
     * without clearing the lines and returning lose state
     *
     * @param s
     * @param rot
     * @param col
     * @return attempted field
     */
    private int[][] tryMove(State s, int rot, int col) {
        int[][] field = s.getField();
        int[][] field_copy = new int[field.length][];
        for (int n = 0; n < field.length; n++) {
            field_copy[n] = field[n].clone();
        }
        int turn = s.getTurnNumber();

        // inner-state of State
        int[] top = s.getTop();
        int[][][] pBottom = State.getpBottom();
        int[][][] pTop = State.getpTop();
        int[][] pHeight = State.getpHeight();
        int[][] pWidth = State.getpWidth();
        int nextPiece = s.getNextPiece();

        // mark increment
        turn++;
        // collision detection
        int height = top[col] - pBottom[nextPiece][rot][0];
        for (int c = 1; c < pWidth[nextPiece][rot]; c++) {
            height = Math.max(height, top[col + c] - pBottom[nextPiece][rot][c]);
        }
        // check if game ended
        if (height + pHeight[nextPiece][rot] >= State.ROWS) {
            // loss flag as true
            return null;
        }
        // for each column in the piece - fill in the appropriate blocks
        for (int i = 0; i < pWidth[nextPiece][rot]; i++) {
            //from bottom to top of brick
            for (int h = height + pBottom[nextPiece][rot][i]; h < height + pTop[nextPiece][rot][i]; h++) {
                field_copy[h][i + col] = turn;
            }
        }
        return field_copy;
    }

    public static void main(String[] args) {
        State s = new State();
        new TFrame(s);
        PlayerSkeleton p = new PlayerSkeleton();
        while (!s.hasLost()) {
            s.makeMove(p.pickMove(s, s.legalMoves()));
            s.draw();
            s.drawNext(0, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("You have completed " + s.getRowsCleared() + " rows.");
    }
}
