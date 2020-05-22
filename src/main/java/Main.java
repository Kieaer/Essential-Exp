import arc.ApplicationListener;
import arc.Core;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.content.Mechs;
import mindustry.entities.type.Player;
import mindustry.game.Rules;
import mindustry.gen.Call;
import mindustry.mod.Mods;
import mindustry.plugin.Plugin;
import mindustry.type.Mech;
import mindustry.type.Weapon;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static mindustry.Vars.playerGroup;
import static mindustry.Vars.state;

public class Main extends Plugin {
    public ApplicationListener listener;
    public Connection conn;
    public Map<String, Integer> data = new HashMap<>();
    Thread mainThread;
    TimerTask tickThread;
    Timer timer = new Timer();

    public Main(){

    }

    @Override
    public void init() {
        boolean work = false;

        for(Mods.LoadedMod mods : Vars.mods.list()){
            if(mods.meta.name.contains("Essential")){
                work = true;
                break;
            }
        }

        if(work){
            try {
                conn = DriverManager.getConnection("jdbc://localhost:9079/player", "", "");

                listener = new ApplicationListener() {
                    @Override
                    public void update() {
                        data.forEach((name, level) -> {
                            Player p = playerGroup.find(pl -> pl.name.equalsIgnoreCase(name));
                            if(p != null) {
                                if(level > 5 && level < 10){
                                    p.mech.mineSpeed = (p.mech.mineSpeed * 100f) / 7;
                                    p.mech.health = (p.mech.health * 100f) / 7;
                                    p.mech.boostSpeed = (p.mech.boostSpeed * 100f) / 7;
                                    p.mech.maxSpeed = (p.mech.maxSpeed * 100f) / 7;
                                } else if (level > 11 && level < 20){

                                }
                            }
                        });
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

            });

            tickThread = new TimerTask() {
                int time = 0;

                @Override
                public void run() {
                    if(time == 30){
                        data.clear();
                        try(PreparedStatement pstmt = conn.prepareStatement("SELECT * from players WHERE connected=1")){
                            try(ResultSet rs = pstmt.executeQuery()){
                                while(rs.next()){
                                    data.put(rs.getString("name"), rs.getInt("level"));
                                }
                            }
                        } catch (SQLException e){
                            e.printStackTrace();
                        }
                    }

                    time++;
                }
            };

            timer.scheduleAtFixedRate(tickThread, 1000, 1000);
        }
    }
}
