package onl.jami.averUtils.managers;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.cache.ServerIconCache;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ServerIconManager {

    private static final int MAX_BYTES = 2_000_000;
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public static Path folder() {
        return AverUtils.getPlugin().getDataFolder().toPath().resolve("icons");
    }

    public static Path disabledFolder() { return folder().resolve("disabled"); }

    public static void init() throws IOException {
        Files.createDirectories(disabledFolder());
    }

    public static List<String> list(boolean enabled) {
        Path dir = enabled ? folder() : disabledFolder();
        if (!Files.isDirectory(dir)) { return List.of(); }
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".png"))
                    .map(n -> n.substring(0, n.length() - 4))
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            AverUtils.logWarning("Could not list icons", ex);
            return List.of();
        }
    }

    public static void setEnabled(String name, boolean enable) throws IOException {
        Path from = resolve(enable ? disabledFolder() : folder(), name);
        Path to = resolve(enable ? folder() : disabledFolder(), name);
        if (!Files.exists(from)) { throw new NoSuchFileException(name); }
        if (Files.exists(to)) { throw new FileAlreadyExistsException(name); }

        Files.move(from, to);
        ServerIconCache.load();
    }

    public static String add(String link, String requestedName) throws IOException, InterruptedException {
        URI uri = URI.create(link);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalArgumentException("Invalid link, must be https: " + link);
        }
        InetAddress address = InetAddress.getByName(uri.getHost());
        if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
            throw new IllegalArgumentException("Invalid link, points to private address: " + link);
        }
        String name = requestedName != null ? requestedName : nameFromUri(uri);
        Path target = resolve(folder(), name);
        if (Files.exists(target) || Files.exists(resolve(disabledFolder(), name))) {
            throw new FileAlreadyExistsException(name);
        }

        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).GET().build();
        HttpResponse<InputStream> response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new IOException("Server returned HTTP error code " + response.statusCode());
        }
        byte[] data;
        try (InputStream in = response.body()) {
            data = in.readNBytes(MAX_BYTES + 1);
        }
        if (data.length > MAX_BYTES) {
            throw new IOException("File is larger than " + MAX_BYTES + " bytes");
        }

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
        if (image == null) { throw new IOException("Link is not a readable image"); }

        init();
        ImageIO.write(toSquare64(image), "png", target.toFile());
        ServerIconCache.load();
        return name;
    }

    private static Path resolve(Path dir, String name) {
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid icon name (letters, numbers, - and _ only)");
        }
        return dir.resolve(name + ".png");
    }

    private static String nameFromUri(URI uri) {
        String path = uri.getPath() == null ? "" : uri.getPath();
        String file = path.substring(path.lastIndexOf('/') + 1);
        int dot = file.lastIndexOf(".");
        if (dot > 0) { file = file.substring(0, dot); }
        file = file.replaceAll("[^A-Za-z0-9_-]", "");
        if (file.length() > 32) { file = file.substring(0, 32); }
        return file.isEmpty() ? "icon-" + System.currentTimeMillis() % 100000 : file;
    }

    private static BufferedImage toSquare64(BufferedImage image) {
        int side = Math.min(image.getWidth(), image.getHeight());
        BufferedImage square = image.getSubimage((image.getWidth() - side) / 2, (image.getHeight() - side) / 2, side, side);
        BufferedImage out = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = out.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(square, 0, 0, 64, 64, null);
        g2d.dispose();
        return out;
    }
}
