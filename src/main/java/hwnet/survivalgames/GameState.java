package hwnet.survivalgames;

public enum GameState {
    WAITING(true), INGAME(false), ENDGAME(false), POSTGAME(false), RESTARTING(false);

    private boolean canJoin;

    private static GameState currentState;

    GameState(boolean canJoin) {
        this.canJoin = canJoin;
    }

    public boolean canJoin() {
        return canJoin;
    }

    public static void setState(GameState state) {
        GameState.currentState = state;
    }

    public static boolean isState(GameState state) {
        return GameState.currentState == state;
    }

    public static GameState getState() {
        return currentState;
    }
}
