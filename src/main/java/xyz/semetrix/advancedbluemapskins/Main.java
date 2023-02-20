package xyz.semetrix.advancedbluemapskins;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.SkinProvider;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
	private static String FLOODGATE_URL;
	private static String NORMAL_URL;
	private static boolean VERBOSE_LOGGING = true;
	private void verboseLog(String message) {
		if (VERBOSE_LOGGING) getLogger().info(message);
	}

	@Override
	public void onEnable() {
		Metrics metrics = new Metrics(this, 17778);

		UpdateChecker.check("Semetrix", "AdvancedBluemapSkins", getDescription().getVersion());

		// Copy config into folder
		if(getDataFolder().mkdirs()) getLogger().info("Created plugin config directory");
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			try {
				getLogger().info("Creating config file");
				Files.copy(Objects.requireNonNull(getResource("config.yml")), configFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		BlueMapAPI.onEnable(blueMapOnEnableListener);



		getLogger().info("BlueMap Custom Skin compatibility plugin enabled!");
	}

	private final Consumer<BlueMapAPI> blueMapOnEnableListener = blueMapAPI -> {
		UpdateChecker.logUpdateMessage(getLogger());

		//Load config from disk
		reloadConfig();

		//Load config values into variables
		NORMAL_URL = getConfig().getString("normal-url");
		FLOODGATE_URL = getConfig().getString("floodgate-url");
		VERBOSE_LOGGING = Boolean.parseBoolean(Objects.requireNonNull(getConfig().getString("verbose-logging")).toLowerCase());

		SkinProvider floodgateSkinProvider = new SkinProvider() {
			@Override
			public Optional<BufferedImage> load(UUID playerUUID) {
				String username = getServer().getOfflinePlayer(playerUUID).getName();
				assert username != null;

				String localUrl;

				if (playerUUID.version() == 0) { //check for floodgate player
					long xuid = getXuid(playerUUID);

					localUrl = FLOODGATE_URL
							.replace("{UUID}", playerUUID.toString())
							.replace("{USERNAME}", username)
							.replace("{XUID}", Long.toString(xuid));

				} else {
					localUrl = NORMAL_URL.replace("{UUID}", playerUUID.toString()).replace("{USERNAME}", username);
				}
				verboseLog("Downloading skin for " + username + " from " + localUrl);
				BufferedImage img = imageFromURL(localUrl);
				return Optional.ofNullable(img);
			}
		};

		blueMapAPI.getPlugin().setSkinProvider(floodgateSkinProvider);
	};

	@Override
	public void onDisable() {
		BlueMapAPI.unregisterListener(blueMapOnEnableListener);

		getLogger().info("BlueMap Custom Skin compatibility plugin disabled!");
	}

	// ================================================================================================================
	// ===============================================Util Methods=====================================================
	// ================================================================================================================

	private long getXuid(UUID playerUUID) {
		return playerUUID.getLeastSignificantBits();
	}

	/**
	 * @param url URL of the image
	 * @return the image, or null if it could not be found
	 */
	private @Nullable BufferedImage imageFromURL(@NotNull String url) {
		BufferedImage result;
		try {
			URL imageUrl = new URL(url);
			try {
				InputStream in = imageUrl.openStream();
				result = ImageIO.read(in);
				in.close();
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Failed to get the image from " + url, e);
				return null;
			}
		} catch (MalformedURLException e) {
			getLogger().log(Level.SEVERE, "URL is malformed: " + url, e);
			return null;
		}
		return result;
	}
}
