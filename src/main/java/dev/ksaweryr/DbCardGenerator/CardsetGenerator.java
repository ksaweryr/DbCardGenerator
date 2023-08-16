package dev.ksaweryr.DbCardGenerator;

import dev.ksaweryr.DbCardGenerator.util.StreamUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CardsetGenerator {
    private final int n;
    private final int cardSize;
    private final BufferedImage cardBase;
    private final List<Image> symbols;
    private List<BufferedImage> cards;

    public CardsetGenerator(int n, List<BufferedImage> symbols, int cardSize) {
        this.n = n;
        this.cards = null;
        this.cardSize = cardSize;
        this.cardBase = createCardBase(3);
        this.symbols = symbols.stream().map(s -> s.getScaledInstance(getSymbolSize(), getSymbolSize(), Image.SCALE_SMOOTH)).toList();

        if(symbols.size() != getNumberOfCards()) {
            throw new IllegalArgumentException("The number of symbols must be equal to n^2+n+1");
        }
    }

    private BufferedImage createCardBase(int strokeWidth) {
        BufferedImage cardBase = new BufferedImage(cardSize, cardSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = cardBase.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, cardSize, cardSize);
        g.setPaint(Color.BLACK);
        g.setStroke(new BasicStroke(strokeWidth));
        g.draw(new Ellipse2D.Double(strokeWidth / 2.0, strokeWidth / 2.0, cardSize - strokeWidth, cardSize - strokeWidth));
        g.dispose();

        return cardBase;
    }

    private BufferedImage getCardBase() {
        BufferedImage result = new BufferedImage(cardSize, cardSize, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = result.createGraphics();
        g.drawImage(cardBase, 0, 0, null);
        g.dispose();

        return result;
    }

    private int getNumberOfCards() {
        return n * n + n + 1;
    }

    private int getSymbolSize() {
        return cardSize / 3;
    }

    private ArrayList<int[]> indicesOnCards() {
        int numberOfCards = n * n + n + 1;
        ArrayList<int[]> result = new ArrayList<>(getNumberOfCards());

        // 1st card
        result.add(IntStream.range(0, n + 1).toArray());

        for(int i = 0; i < n; i++) {
            // "Variable used in lambda expression should be final or effectively final"
            int _i = i;
            result.add(
                    IntStream.concat(
                            IntStream.of(0),
                            IntStream.range(0, n)
                                    .map(j -> n + 1 + n * _i + j)
                    ).toArray()
            );
        }

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                // see the loop above
                int _i = i;
                int _j = j;
                result.add(
                        IntStream.concat(
                                IntStream.of(i + 1),
                                IntStream.range(0, n)
                                        .map(k -> n + 1 + n * k + (_i * k + _j) % n)
                        ).toArray()
                );
            }
        }

        return result;
    }

    public List<BufferedImage> getCards() {
        return indicesOnCards().stream().map(this::createCard).toList();
    }

    private BufferedImage createCard(int[] indices) {
        BufferedImage card = getCardBase();

        ArrayList<Image> cardSymbols = Arrays.stream(indices)
                .mapToObj(this.symbols::get)
                .map(this::transformSymbol)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(cardSymbols);

        Graphics2D g = card.createGraphics();
        g.drawImage(cardSymbols.get(0), cardSize / 2 - getSymbolSize() / 2, cardSize / 2 - getSymbolSize() / 2, null);

        for(int i = 1; i < n + 1; i++) {
            int r = getSymbolSize();
            double theta = i * 2 * Math.PI / n;
            int x = (int)(r * Math.cos(theta)) + cardSize / 2;
            int y = (int)(r * Math.sin(theta)) + cardSize / 2;

            Image s = cardSymbols.get(i);
            double scale = Math.random() / 2 + .4;
            int size = (int)(getSymbolSize() * scale);

            g.drawImage(s.getScaledInstance(size, size, Image.SCALE_SMOOTH), x - size / 2, y - size / 2, null);
        }

        g.dispose();

        return card;
    }

    private Image transformSymbol(Image symbol) {
        BufferedImage target = new BufferedImage(getSymbolSize(), getSymbolSize(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.rotate(Math.random() * 2 * Math.PI, getSymbolSize() / 2.0, getSymbolSize() / 2.0);
        g.drawImage(symbol, 0, 0, null);
        g.dispose();

        return target;
    }

    public ArrayList<BufferedImage> getPages() {
        ArrayList<BufferedImage> pages = new ArrayList<>();
        List<BufferedImage> cards = getCards();
        // dimension are selected so that the cards printed on A4 page are about 9.5cm in diamater
        int pageWidth = (int)(cardSize / (9.5 / 21));
        int pageHeight = (int)(cardSize / (9.5 / 29.7));
        int horizontalPad = (pageWidth - 2 * cardSize) / 3;
        int verticalPad = (pageHeight - 3 * cardSize) / 4;

        for(List<BufferedImage> pageCards : StreamUtils.chunked(cards.stream(), 6).toList()) {
            BufferedImage page = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = page.createGraphics();
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, pageWidth, pageHeight);
            for(int i = 0; i < Math.min(6, pageCards.size()); i++) {
                int x = horizontalPad + (i % 2) * (horizontalPad + cardSize);
                int y = verticalPad + (i / 2) * (verticalPad + cardSize);

                g.drawImage(pageCards.get(i), x, y, null);
            }
            g.dispose();
            pages.add(page);
        }

        return pages;
    }
}
