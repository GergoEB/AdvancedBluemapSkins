package xyz.semetrix.advancedbluemapskins.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import xyz.semetrix.advancedbluemapskins.AbstractSkinProvider;
import xyz.semetrix.advancedbluemapskins.Main;

public class UrlSkinProvider extends AbstractSkinProvider {

  private final String url;

  public UrlSkinProvider(Main plugin) {
    super(plugin);
    this.url = plugin.getConfig().getString("normal-url");
  }

  @Override
  public URL skinUrl(UUID uuid) throws MalformedURLException {
    String url = this.url;
    if (url == null) {
      return  null;
    }

    String username = plugin.getServer().getOfflinePlayer(uuid).getName();
    url = url.replace("{UUID}", uuid.toString());
    if (username != null) {
      url = url.replace("{USERNAME}", username);
    }

    return new URL(url);
  }

  @Override
  public boolean test(UUID uuid) {
    return true;
  }
}
