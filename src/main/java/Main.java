import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.content.Bullets;
import mindustry.entities.Units;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.mod.Mods;
import mindustry.mod.Plugin;
import mindustry.world.Block;
import mindustry.world.Tile;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.sql.*;
import java.util.Timer;

import static arc.util.async.Threads.sleep;
import static mindustry.Vars.*;

public class Main extends Plugin {
    public ApplicationListener listener;
    public Connection conn = null;
    public ObjectMap<String, Integer> data = new ObjectMap<>();
    Timer timer = new Timer();

    private static final Seq<Float> multipilers = new Seq<>();
    private static final Seq<PlayerData> playerData = new Seq<>();

    public static final Fi root = Core.settings.getDataDirectory().child("mods/Essentials/");

    boolean active = false;

    public Main() {

    }

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
        for (Mods.LoadedMod mods : mods.list()) {
            if (mods.meta.name.equals("Essentials")) {
                Log.info("Essential-Exp activated!");
                try {
                    // DB 설정
                    org.h2.Driver.load();
                    conn = DriverManager.getConnection("jdbc:h2:tcp://localhost:9079/player", "", "");

                    // 배수 계산
                    float buffer = 0.993f;
                    for (int a = 0; a < 200; a++) {
                        buffer = buffer + 0.012f;
                        multipilers.add(buffer);
                    }

                    // 메인 스레드 설정
                    listener = new ApplicationListener() {
                        @Override
                        public void dispose() {
                            timer.cancel();
                        }
                    };

                    Core.app.addListener(listener);
                } catch (SQLException e) {
                    JsonObject config = JsonValue.readHjson(root.child("config.hjson").readString()).asObject();
                    if (!config.get("DBServer").asBoolean()) {
                        Log.warn("But DBServer not enabled! Please turn on 'DBServer' option in config/mods/Essentials/config.hjson!");
                    } else {
                        Log.err("But can't connect database..");
                        e.printStackTrace();
                    }

                    Core.app.dispose();
                    Core.app.exit();
                }
                break;
            }
        }
        if (conn == null) Log.info("Essential-Exp must have Essentials to use the playerDB.");

        Events.on(EventType.PlayEvent.class, e -> {
            String name = state.map.name();
            System.out.println(name);
            if (name.equals("Bullet")) {
                active = true;
                System.out.println("Plugin activated!");
            }
        });

        ArrayMap<Tile, Thread> blockThread = new ArrayMap<>();

        Events.on(EventType.TileChangeEvent.class, e -> {
            Team team = Team.crux;

            if (active) {
                for (int x = 0; x < world.width(); x++) {
                    for (int y = 0; y < world.height(); y++) {
                        Tile tile = world.tile(x, y);
                        if (tile.block() != null || tile.block() != null) {
                            Block block = tile.block();
                            float drawx = tile.drawx();
                            float drawy = tile.drawy();

                            if (block == Blocks.scrapWall) {
                                if (!blockThread.containsKey(tile)) {
                                    int sx = x;
                                    int sy = y;
                                    Thread thread = new Thread(() -> {
                                        while (there(sx, sy, Blocks.scrapWall)) {
                                            Call.createBullet(Bullets.standardCopper, team, drawx, drawy, 0f, 1f, 1f, 1f);
                                            Call.createBullet(Bullets.standardCopper, team, drawx, drawy, 90f, 1f, 1f, 1f);
                                            Call.createBullet(Bullets.standardCopper, team, drawx, drawy, 180f, 1f, 1f, 1f);
                                            Call.createBullet(Bullets.standardCopper, team, drawx, drawy, 270f, 1f, 1f, 1f);
                                            if (there(sx, sy, Blocks.scrapWall)) sleep(500);
                                        }
                                        blockThread.removeKey(tile);
                                    });
                                    blockThread.put(tile, thread);
                                    thread.start();
                                }
                            } else if (block == Blocks.scrapWallLarge) {
                                if (!blockThread.containsKey(tile)) {
                                    int sx = x;
                                    int sy = y;
                                    Thread thread = new Thread(() -> {
                                        while (there(sx, sy, Blocks.scrapWallLarge)) {
                                            for (int rot = 0; rot < 360; rot += 35) {
                                                Call.createBullet(Bullets.standardCopper, team, drawx, drawy, 0f + rot, 1f, 1f, 1f);
                                                Call.createBullet(Bullets.standardCopper, team, drawx, drawy, 90f + rot, 1f, 1f, 1f);
                                                Call.createBullet(Bullets.standardCopper, team, drawx, drawy, 180f + rot, 1f, 1f, 1f);
                                                Call.createBullet(Bullets.standardCopper, team, drawx, drawy, 270f + rot, 1f, 1f, 1f);
                                                if (there(sx, sy, Blocks.scrapWallLarge)) sleep(500);
                                            }
                                        }
                                        blockThread.removeKey(tile);
                                    });
                                    blockThread.put(tile, thread);
                                    thread.start();
                                }
                            } else if (block == Blocks.scrapWallGigantic) {
                                if (!blockThread.containsKey(tile)) {
                                    int sx = x;
                                    int sy = y;
                                    Thread thread = new Thread(() -> {
                                        while (there(sx, sy, Blocks.scrapWallGigantic)) {
                                            for (int rot = 0; rot < 360; rot += 2) {
                                                Call.createBullet(Bullets.standardCopper, team, drawx, drawy, rot, 1f, 1f, 1f);
                                                if (there(sx, sy, Blocks.scrapWallGigantic)) sleep(64);
                                            }
                                        }
                                        blockThread.removeKey(tile);
                                    });
                                    blockThread.put(tile, thread);
                                    thread.start();
                                }
                            } else if (block == Blocks.scrapWallHuge) {
                                if (!blockThread.containsKey(tile)) {
                                    int sx = x;
                                    int sy = y;
                                    Thread thread = new Thread(() -> {
                                        while (there(sx, sy, Blocks.scrapWallHuge)) {
                                            float angle = getClosestPlayer(tile) != null ? tile.angleTo(getClosestPlayer(tile).getX(), getClosestPlayer(tile).getY()) : 0f;

                                            Call.createBullet(Bullets.flakScrap, team, drawx, drawy, angle, 1f, 1f, 1f);
                                            if (there(sx, sy, Blocks.scrapWallHuge)) sleep(96);
                                        }
                                        blockThread.removeKey(tile);
                                    });
                                    blockThread.put(tile, thread);
                                    thread.start();
                                }
                            /*} else if (block == Blocks.copperWall) {
                            } else if (block == Blocks.copperWallLarge) {
                            } else if (block == Blocks.titaniumWall) {
                            } else if (block == Blocks.titaniumWallLarge) {
                            } else if (block == Blocks.thoriumWall) {
                            } else if (block == Blocks.thoriumWallLarge) {
                            } else if (block == Blocks.surgeWall) {
                            } else if (block == Blocks.surgeWallLarge) {
                            } else if (block == Blocks.plastaniumWall) {
                            } else if (block == Blocks.plastaniumWallLarge) {*/
                            }
                        }
                    }
                }
            }
        });
    }

    public boolean there(int x, int y, Block block) {
        return world.tile(x, y).block() == block;
    }

    public Unit getClosestPlayer(Tile tile) {
        return Units.closestEnemy(tile.team(), tile.drawx(), tile.drawy(), 300f, e -> !e.dead() && e.isFlying());
        //return Units.closestEnemy(tile.getTeam(), tile.drawx(), tile.drawy(), 50f, Unit::isValid);
    }
}
