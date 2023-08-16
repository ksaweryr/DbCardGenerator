package dev.ksaweryr.DbCardGenerator;

import org.apache.commons.cli.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CLI {
    public static void run(String[] args) {
        CommandLine cmd = parseArgs(args);
        File inputDir = new File(cmd.getOptionValue("input-path"));
        String outputFileName = cmd.hasOption("output") ? cmd.getOptionValue("output") : inputDir.getName() + ".pdf";

        try {
            List<BufferedImage> symbols = SymbolLoader.load(inputDir);
            int n = verifySymbolCount(symbols.size());
            if(n == -1) {
                System.err.println("The number of symbols (a) must satisfy the equation n^2+n+1=a where n is a prime.");
                System.exit(1);
            }
            new PdfGenerator(n, symbols).save(new FileOutputStream(outputFileName));
        } catch(IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    private static CommandLine parseArgs(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        Option inputDirOption = new Option("i", "input-path", true, "path to directory containing symbols to use on the cards");
        inputDirOption.setRequired(true);
        Option outputFileOption = new Option("o", "output", true, "output file (defaults to <input-path>.pdf)");

        options.addOption(inputDirOption);
        options.addOption(outputFileOption);

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("DbCardGenerator", options);
            System.exit(1);
            // apparently System.exit(1); is not enough
            return null;
        }
    }

    private static int verifySymbolCount(int cnt) {
        // p^2 + p + 1 = cnt
        // p^2 + p + 1 - cnt = 0
        // a = 1, b = 1, c = 1 - cnt
        // delta = b^2 - 4ac = 1 - 4(1 - cnt) = 1 + 4(cnt - 1) = 4cnt - 3
        int delta = 4 * cnt - 3;

        if(delta < 0) {
            return -1;
        }

        int deltaSqrt = (int)Math.round(Math.sqrt(delta));
        if(deltaSqrt * deltaSqrt != delta) {
            return -1;
        }

        // deltaSqrt - 1 is even because sqrt(delta) must be odd because delta = 4cnt - 3 which is a difference
        // of an even and an odd number
        return isPrime((deltaSqrt - 1) / 2) ? (deltaSqrt - 1) / 2 : -1;
    }

    private static boolean isPrime(int n) {
        if(n < 2) {
            return false;
        }

        if(n % 2 == 0) {
            return false;
        }

        for(int i = 3; i * i <= n; i += 2) {
            if(n % i == 0) {
                return false;
            }
        }

        return true;
    }
}
