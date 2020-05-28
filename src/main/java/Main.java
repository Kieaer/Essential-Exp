import arc.ApplicationListener;
import arc.Core;
import arc.util.Align;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.content.Mechs;
import mindustry.entities.type.Player;
import mindustry.game.Rules;
import mindustry.gen.Call;
import mindustry.gen.Sounds;
import mindustry.mod.Mods;
import mindustry.plugin.Plugin;
import mindustry.type.Mech;
import mindustry.type.Weapon;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static mindustry.Vars.*;
import static mindustry.content.Mechs.*;

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

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(Mods.LoadedMod mods : mods.list()){
            System.out.println(mods.meta.name);
            if(mods.meta.name.equals("Essentials")){
                work = true;
                System.out.println("work!");
                break;
            }
        }

        if(work){
            try {
                org.h2.Driver.load();
                conn = DriverManager.getConnection("jdbc:h2:tcp://localhost:9079/player", "", "");
                try(PreparedStatement pstmt = conn.prepareStatement("SELECT * from players WHERE connected=1")){
                    try(ResultSet rs = pstmt.executeQuery()){
                        while(rs.next()){
                            System.out.println(rs.getString("name")+" Player data catch!");
                            data.put(rs.getString("name"), rs.getInt("level"));
                        }
                    }
                } catch (SQLException e){
                    e.printStackTrace();
                }

                listener = new ApplicationListener() {
                    @Override
                    public void update() {
                        data.forEach((name, level) -> {
                            Player p = playerGroup.find(pl -> pl.name.equalsIgnoreCase(name));
                            if (p != null) {
                                if (level > 5 && level < 10) {
                                } else if (level > 11 && level < 20) {
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
                    data.forEach((name, level) -> {
                        Player p = playerGroup.find(pl -> pl.name.equalsIgnoreCase(name));
                        if(p == null) return;

                        //if (level > 5 && level < 10) {
                        p.mech.mineSpeed = p.mech.mineSpeed * 1.07f;
                        p.mech.health = p.mech.health * 1.07f;
                        p.mech.boostSpeed = p.mech.boostSpeed * 1.07f;
                        p.mech.maxSpeed = p.mech.maxSpeed * 1.07f;
                        p.mech.speed = p.mech.speed * 1.07f;
                        Call.onInfoPopup(p.con, "채광 속도" + p.mech.mineSpeed + "\n체력" + p.mech.health + "\n현재 체력:" + p.health + "\n이속" + p.mech.speed + "\n최대 속도" + p.mech.maxSpeed, 1f, Align.top, 0, 0, 0, 0);
                        //} else if (level > 11 && level < 20) {

                        //}
                    });

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
