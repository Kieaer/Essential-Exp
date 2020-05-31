public class PlayerData {
    public String uuid;
    public int exp;
    public int level;
    public String reqtotalexp;

    public PlayerData(String uuid, int exp, int level, String reqtotalexp) {
        this.uuid = uuid;
        this.exp = exp;
        this.level = level;
        this.reqtotalexp = reqtotalexp;
    }
}
