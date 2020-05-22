import arc.ApplicationListener;
import arc.Core;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.entities.type.Player;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.mod.Mods;
import mindustry.plugin.Plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Main extends Plugin {
    public ApplicationListener listener;

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
                Connection conn = DriverManager.getConnection("jdbc://localhost:9079/player", "", "");
                listener = new ApplicationListener() {
                    @Override
                    public void update() {

                    }
                };

                Core.app.addListener(listener);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("cha", "Test geo", (arg, player) -> {

        });
    }
}
