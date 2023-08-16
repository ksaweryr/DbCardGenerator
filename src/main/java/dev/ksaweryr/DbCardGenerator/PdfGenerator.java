package dev.ksaweryr.DbCardGenerator;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfGenerator {
    private final int n;
    private final List<BufferedImage> symbols;
    private static final int CARD_SIZE = 500;

    public PdfGenerator(int n, List<BufferedImage> symbols) {
        this.n = n;
        this.symbols = symbols;
    }

    public void save(FileOutputStream outputStream) throws IOException {
        CardsetGenerator gen = new CardsetGenerator(n, symbols, CARD_SIZE);

        Document doc = new Document(PageSize.A4, 0, 0, 0, 0);
        PdfWriter.getInstance(doc, outputStream);
        doc.open();

        for(BufferedImage page : gen.getPages()) {
            var img = com.lowagie.text.Image.getInstance(page, null);
            img.scaleAbsolute(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
            doc.add(img);
        }

        doc.close();
    }
}
