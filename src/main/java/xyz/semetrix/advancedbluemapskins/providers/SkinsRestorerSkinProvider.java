package xyz.semetrix.advancedbluemapskins.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.property.IProperty;
import xyz.semetrix.advancedbluemapskins.AbstractSkinProvider;
import xyz.semetrix.advancedbluemapskins.Main;

public class SkinsRestorerSkinProvider extends AbstractSkinProvider {

  private final SkinsRestorerAPI api;

  public SkinsRestorerSkinProvider(Main plugin) {
    super(plugin);
    this.api = SkinsRestorerAPI.getApi();
  }

  @Override
  public URL skinUrl(UUID uuid) throws MalformedURLException {
    if (api == null) {
      return null;
    }

    String username = plugin.getServer().getOfflinePlayer(uuid).getName();
    try {
      String skin = api.getSkinName(username);
      IProperty data = api.getSkinData(skin == null ? username : skin);
      if (data != null) {
        return new URL(api.getSkinTextureUrl(data));
      }
    } catch (IllegalStateException e) {
      plugin.getLogger().log(Level.WARNING, "There is currently no support for SkinsRestorer on the proxy.");
    }

    return null;
  }

  @Override
  public boolean test(UUID uuid) {
    return api != null;
  }
}
