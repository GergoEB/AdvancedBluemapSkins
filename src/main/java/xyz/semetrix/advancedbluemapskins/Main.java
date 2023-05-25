package xyz.semetrix.advancedbluemapskins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.SkinProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.semetrix.advancedbluemapskins.providers.FloodgateSkinProvider;
import xyz.semetrix.advancedbluemapskins.providers.SkinsRestorerSkinProvider;
import xyz.semetrix.advancedbluemapskins.providers.UrlSkinProvider;

public final class Main extends JavaPlugin {
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
		boolean verbose = getConfig().getBoolean("verbose-logging", false);
		if (verbose) {
			getLogger().setLevel(Level.FINE);
		} else {
			getLogger().setLevel(Level.INFO);
		}

		List<AbstractSkinProvider> skinProviders = skinProviders();

		SkinProvider floodgateSkinProvider = uuid -> {
			Optional<AbstractSkinProvider> first = skinProviders.stream()
					.filter(provider -> provider.test(uuid)).findFirst();
			if (first.isPresent()) {
				return first.get().load(uuid);
			}

			return Optional.empty();
		};

		blueMapAPI.getPlugin().setSkinProvider(floodgateSkinProvider);
	};

	@Override
	public void onDisable() {
		BlueMapAPI.unregisterListener(blueMapOnEnableListener);

		getLogger().info("BlueMap Custom Skin compatibility plugin disabled!");
	}

	private List<AbstractSkinProvider> skinProviders() {
		PluginManager pluginManager = getServer().getPluginManager();
		Builder<AbstractSkinProvider> builder = ImmutableList.builder();
		if (pluginManager.isPluginEnabled("floodgate")) {
			builder.add(new FloodgateSkinProvider(this));
		}
		if (pluginManager.isPluginEnabled("SkinsRestorer")) {
			builder.add(new SkinsRestorerSkinProvider(this));
		}
		builder.add(new UrlSkinProvider(this));

		return builder.build();
	}
}
