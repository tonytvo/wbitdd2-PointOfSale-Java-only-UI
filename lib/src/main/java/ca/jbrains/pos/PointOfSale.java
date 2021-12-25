package ca.jbrains.pos;

import ca.jbrains.pos.domain.Basket;
import ca.jbrains.pos.domain.Catalog;
import io.vavr.control.Either;
import io.vavr.control.Option;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PointOfSale {
    public static void main(String[] args) {
        runApplication(new InputStreamReader(System.in), System.out::println);
    }

    private static void runApplication(Reader commandLinesReader, Consumer<String> consoleDisplay) {
        // SMELL Duplicates logic in PurchaseTest: stream lines, handle each line, consume the result
        streamLinesFrom(commandLinesReader)
                .map(line -> handleLine(line, createAnyBasket(), createAnyCatalog()))
                .forEachOrdered(consoleDisplay);
    }

    private static Catalog createAnyCatalog() {
        return new Catalog() {
            @Override
            public Option<Integer> findPrice(String barcode) {
                throw new RuntimeException("Not our job");
            }
        };
    }

    private static Basket createAnyBasket() {
        return new Basket() {
            @Override
            public void add(int price) {
                throw new RuntimeException("Not our job");
            }

            @Override
            public int getTotal() {
                throw new RuntimeException("Not our job");
            }
        };
    }

    public static String handleLine(String line, Basket basket, Catalog catalog) {
        if ("total".equals(line)) return String.format("Total: %s", formatPrice(basket.getTotal()));

        return Barcode.makeBarcode(line)
                .map(barcode -> handleBarcode(barcode, catalog, basket))
                .getOrElse("Scanning error: empty barcode");
    }

    // REFACTOR Reorder the parameters
    private static String handleBarcode(Barcode barcode, Catalog catalog, Basket basket) {
        return handleSellOneItemRequest(catalog, basket, barcode);
    }

    public static Stream<String> streamLinesFrom(Reader reader) {
        return new BufferedReader(reader).lines();
    }

    public static String handleSellOneItemRequest(Catalog catalog, Basket basket, Barcode barcode) {
        return findProductInCatalog(catalog, barcode).fold(
                missingBarcode -> formatProductNotFoundMessage(missingBarcode.text()),
                matchingPrice -> addToBasketAndFormatPrice(basket, matchingPrice)
        );
    }

    // REFACTOR Move into The Hole onto Catalog
    private static Either<Barcode, Integer> findProductInCatalog(Catalog catalog, Barcode barcode) {
        return catalog.findPrice(barcode.text()).toEither(barcode);
    }

    private static String formatProductNotFoundMessage(String trustedBarcodeString) {
        return String.format("Product not found: %s", trustedBarcodeString);
    }

    private static String addToBasketAndFormatPrice(Basket basket, int price) {
        basket.add(price);
        return formatPrice(price);
    }

    public static String formatPrice(int priceInCanadianCents) {
        return String.format("CAD %.2f", priceInCanadianCents / 100.0d);
    }

    public static String handleTotal(Basket basket) {
        int total = basket.getTotal();
        return String.format("Total: %s", formatPrice(total));
    }
}
