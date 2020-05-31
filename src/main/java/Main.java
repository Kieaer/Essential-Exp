import arc.ApplicationListener;
import arc.Core;
import arc.struct.Array;
import arc.util.Align;
import arc.util.Log;
import mindustry.content.Mechs;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.mod.Mods;
import mindustry.plugin.Plugin;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static mindustry.Vars.mods;
import static mindustry.Vars.playerGroup;

public class Main extends Plugin {
    public ApplicationListener listener;
    public Connection conn;
    public Map<String, Integer> data = new HashMap<>();
    Thread mainThread;
    TimerTask tickThread;
    Timer timer = new Timer();

    private static Array<Float> multipilers = new Array<>();
    private static Array<PlayerData> playerData = new Array<>();

    public Main() {

    }

    /*public void updateData(boolean clean){
        if(clean) {
            data.clear();
            playerData = new JsonObject();
        }
        try(PreparedStatement pstmt = conn.prepareStatement("SELECT * from players")){
            try(ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                final int columnCount = meta.getColumnCount();

                JsonObject buffer = new JsonObject();
                while (rs.next()) {
                    data.put(rs.getString("name"), rs.getInt("level"));

                    for (int i = 1; i <= columnCount; i++) {
                        int type = meta.getColumnType(i);
                        if(type == Types.CLOB){
                            buffer.add(meta.getColumnName(i).toLowerCase(), rs.getString(meta.getColumnName(i)));
                        } else if(type == Types.TINYINT){
                            buffer.add(meta.getColumnName(i).toLowerCase(), rs.getBoolean(meta.getColumnName(i)));
                        } else if(type == Types.INTEGER){
                            buffer.add(meta.getColumnName(i).toLowerCase(), rs.getInt(meta.getColumnName(i)));
                        } else {
                            System.out.println(meta.getColumnName(i).toLowerCase()+" // " +rs.getObject(meta.getColumnName(i))+ "//" + rs.getType());
                        }
                    }
                    playerData.add(rs.getString("uuid"), buffer);
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }*/

    public PlayerData updateData(String uuid) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * from players WHERE uuid=?")) {
            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new PlayerData(rs.getString("uuid"), rs.getInt("exp"), rs.getInt("level"), rs.getString("reqtotalexp"));
                } else {
                    return new PlayerData("Unauthorized", 0, 0, "No data found");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new PlayerData("Unauthorized", 0, 0, "No data found");
        }
    }

    @Override
    public void init() {
        boolean work = false;

        //try {
        //    TimeUnit.SECONDS.sleep(2);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
        for (Mods.LoadedMod mods : mods.list()) {
            if (mods.meta.name.equals("Essentials")) {
                work = true;
                break;
            }
        }

        if (work) {
            Log.info("Essential-Exp activated!");
            try {
                // DB 설정
                org.h2.Driver.load();
                conn = DriverManager.getConnection("jdbc:h2:tcp://localhost:9079/player", "", "");

                // 배수 계산
                float buffer = 0.993f;
                for (int a = 0; a < 100; a++) {
                    buffer = buffer + 0.007f;
                    for (int b = 0; b < 5; b++) {
                        multipilers.add(buffer);
                    }
                }

                // 메인 스레드 설정
                listener = new ApplicationListener() {
                    @Override
                    public void update() {
                        for (Player player : playerGroup.all()) {
                            Call.onInfoPopup(player.con, "Health: " + player.health, 0.016f, Align.left, 40, 0, 0, 0);
                        }
                    }

                    @Override
                    public void dispose() {
                        timer.cancel();
                        mainThread.interrupt();
                    }
                };

                Core.app.addListener(listener);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            mainThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    for (Player player : playerGroup.all()) {
                        PlayerData data = updateData(player.uuid);

                        if (!data.uuid.equals("Unauthorized")) {
                            float multipiler = multipilers.get(data.level);
                            player.mech.health = 200 * multipiler;
                            player.mech.mineSpeed = Mechs.alpha.mineSpeed * multipiler;

                            String message = "Name: " + player.name + "\nLevel: " + data.level + "\nExp: " + data.reqtotalexp + "\nMultipiler: " + multipilers.get(data.level) + "x";
                            Call.onInfoPopup(player.con, message, 1f, Align.left, 0, 0, 68, 0);
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            mainThread.start();
        } else {
            Log.info("Essential-Exp must have Essentials to use the playerDB.");
            Core.app.dispose();
            Core.app.exit();
        }
    }
}
