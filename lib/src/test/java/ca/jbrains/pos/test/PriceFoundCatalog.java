package ca.jbrains.pos.test;

import ca.jbrains.pos.Barcode;
import ca.jbrains.pos.domain.Catalog;
import io.vavr.control.Either;
import io.vavr.control.Option;

public class PriceFoundCatalog implements Catalog {
    private final int value;

    PriceFoundCatalog(int value) {
        this.value = value;
    }

    @Override
    public Option<Integer> findPrice(Barcode barcode) {
        return Option.of(value);
    }

    // REFACTOR Move into The Hole onto Catalog
    @Override
    public Either<Barcode, Integer> findProductInCatalog(Barcode barcode) {
        return findPrice(barcode).toEither(barcode);
    }
}
