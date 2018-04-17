package fr.atesab.sqldatareader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin implements Listener {
	private DataBaseConnector connector;
	private String username = "root";
	private String password = "";
	private String url = "127.0.0.1:3306/database";
	private String table = "table";

	private String chatPattern = "&7(&e%_points%&7) &r%role%%playername%&7: &f%text%";
	private String rolePointField = "points";
	private String[] roles = new String[] { "10 &cExample Role " };
	private String[] fields = new String[] { "uuid", "points" };
	private FileConfiguration config = getConfig();

	public void loadConfigs() {
		username = config.getString("sql_username");
		password = config.getString("sql_password");
		url = config.getString("sql_url");
		table = config.getString("sql_table");

		chatPattern = config.getString("chatPattern");
		rolePointField = config.getString("rolePointField");
		List<String> roles = config.getStringList("roles");
		this.roles = roles.toArray(new String[roles.size()]);
		List<String> fields = config.getStringList("fields");
		this.fields = fields.toArray(new String[fields.size()]);

		savePluginConfig();
	}

	public void savePluginConfig() {
		config.set("sql_username", username);
		config.set("sql_password", password);
		config.set("sql_url", url);
		config.set("sql_table", table);

		config.set("chatPattern", chatPattern);
		config.set("rolePointField", rolePointField);
		List<String> roles = new ArrayList<String>(this.roles.length);
		for (String r : this.roles)
			roles.add(r);
		List<String> fields = new ArrayList<String>(this.fields.length);
		for (String f : this.fields)
			fields.add(f);
		config.set("roles", roles);
		config.set("fields", fields);
		saveConfig();
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.loadConfigs();
		connector = new DataBaseConnector(username, password, url, table);
		if (connector.openConnection())
			getServer().getPluginManager().registerEvents(this, this);
		else
			this.getLogger().info("An error occured when opening database connection.");
		super.onEnable();
	}

	public String getRoleByPoints(int points) {
		int i;
		for (i = 0; i < roles.length && Integer.valueOf(roles[i].split(" ", 2)[0]) <= points; i++)
			;
		i--;
		return i<0 ? "" : (roles[i].split(" ", 2)[1]);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent ev) throws SQLException {
		ResultSet result = connector.loadData(ev.getPlayer().getUniqueId().toString());
		String msg = chatPattern;
		if (result != null) {
			if (!rolePointField.isEmpty())
				msg = msg.replaceAll("%role%", getRoleByPoints(result.getInt(rolePointField)));

			for (String field : fields)
				msg = msg.replaceAll("%_" + field + "%", result.getString(field));

		} else {
			if (!rolePointField.isEmpty())
				msg = msg.replaceAll("%role%", "");

			for (String field : fields)
				msg = msg.replaceAll("%_" + field + "%", "");
		}
		msg = msg.replaceAll("[&]([A-Fa-f0-9k-oK-OrR]){1}", ChatColor.COLOR_CHAR + "$1")
				.replaceAll("[&]" + ChatColor.COLOR_CHAR, "&")
				.replaceAll("%text%", ev.getMessage())
				.replaceAll("%world%", ev.getPlayer().getWorld().getName())
				.replaceAll("%name%", ev.getPlayer().getName())
				.replaceAll("%displayname%", ev.getPlayer().getDisplayName())
				.replaceAll("%playerlistname%", ev.getPlayer().getPlayerListName());
		for (Player p : getServer().getOnlinePlayers())
			p.sendMessage(msg);
		ev.setCancelled(true);
	}

	@Override
	public void onDisable() {
		try {
			if (!connector.getConnection().isClosed())
				connector.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.savePluginConfig();
		super.onDisable();
	}

}
