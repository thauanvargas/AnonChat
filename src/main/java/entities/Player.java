package entities;


public class Player
{
    private int playerId;
    private String playerName = "";
    private int index;
    private boolean isWithKey = false;

    public Player(Integer playerId, String playerName, int playerIndex)
    {
        this.playerId = playerId;
        this.playerName = playerName;
        this.index = playerIndex;
    }

    public boolean isWithKey() {
        return isWithKey;
    }

    public void setWithKey(boolean withKey) {
        isWithKey = withKey;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}