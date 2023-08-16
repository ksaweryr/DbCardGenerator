package dev.ksaweryr.DbCardGenerator;

import dev.ksaweryr.DbCardGenerator.util.ThrowingFunction;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public class SymbolLoader {
    public static List<BufferedImage> load(File dir) throws IOException {
        if(!dir.exists()) {
            throw new IOException("Directory `" + dir.getAbsolutePath() + "` doesn't exist.");
        }

        if(!dir.isDirectory()) {
            throw new IOException("`" + dir.getAbsolutePath() + "` is not a directory.");
        }

        try {
            return Stream.of(dir.listFiles())
                    .map(ThrowingFunction.wrapper(ImageIO::read))
                    .map(img -> {
                        int symbolSize = (int)Math.ceil(Math.max(img.getWidth(), img.getHeight()) * Math.sqrt(2));
                        BufferedImage target = new BufferedImage(symbolSize, symbolSize, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = target.createGraphics();
                        g.drawImage(img, (symbolSize - img.getWidth()) / 2, (symbolSize - img.getHeight()) / 2, null);
                        g.dispose();

                        return target;
                    })
                    .collect(Collectors.toList());
        } catch(RuntimeException ex) {
            throw new IOException(ex);
        }
    }
}
