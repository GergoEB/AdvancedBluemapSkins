package xyz.semetrix.advancedbluemapskins;

import de.bluecolored.bluemap.api.plugin.SkinProvider;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSkinProvider implements SkinProvider, Predicate<UUID> {

  protected final Main plugin;

  public AbstractSkinProvider(Main plugin) {
    this.plugin = plugin;
  }

  public URL skinUrl(UUID uuid) throws MalformedURLException {
    return null;
  }

  @Override
  public Optional<BufferedImage> load(UUID uuid) {
    URL url = null;
    try {
      url = skinUrl(uuid);
    } catch (MalformedURLException e) {
      plugin.getLogger().log(Level.SEVERE, "URL is malformed: " + url, e);
    }
    if (url == null) {
      return Optional.empty();
    }

    plugin.getLogger().log(Level.FINE, "Downloading skin for " + uuid + " from " + url);
    BufferedImage img = imageFromURL(url);
    return Optional.ofNullable(img);
  }

  /**
   * @param url URL of the image
   * @return the image, or null if it could not be found
   */
  private @Nullable BufferedImage imageFromURL(@NotNull URL url) {
    try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
      return ImageIO.read(bis);
    } catch (IOException e) {
      plugin.getLogger().log(Level.SEVERE, "Failed to get the image from " + url, e);
    }

    return null;
  }
}
