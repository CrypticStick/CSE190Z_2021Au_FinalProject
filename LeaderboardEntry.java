public class LeaderboardEntry {

    private int rank;
    private String name;
    private long time;

    /**
     * Creates a new leaderboard entry.
     * 
     * @param rank the rank for the respective track.
     * @param name the name of the user who achieved this rank.
     * @param time the time associated with this rank.
     */
    public LeaderboardEntry(int rank, String name, long time) {
        this.rank = rank;
        this.name = name;
        this.time = time;
    }

    /**
     * Updates the stored rank to be a new value.
     * 
     * @param rank the new rank.
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Gets the rank associated with this entry.
     * 
     * @return the entry's rank.
     */
    public int getRank() {
        return rank;
    }

    /**
     * Gets the name associated with this entry.
     * 
     * @return the entry's name.
     */
    public String getName() {
        return name;
    }


    /**
     * Gets the time associated with this entry.
     * 
     * @return the entry's time.
     */
    public long getTime() {
        return time;
    }
}
