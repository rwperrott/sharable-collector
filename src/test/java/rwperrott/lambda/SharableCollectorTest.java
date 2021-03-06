package rwperrott.lambda;

import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.testng.Assert.*;

import org.jooq.lambda.Agg;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.testng.annotations.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class SharableCollectorTest {
    static final Double[] values = {0d, 10d, 20d, 30d, 40d, 50d, 60d, 70d, 80d, 90d};

    static final List<Tuple2<String, Double>> namedValues = Arrays.asList(
            tuple("Zero    ", 0d),
            tuple("Ten    ", 10d),
            tuple("Twenty ", 20d),
            tuple("Thirty ", 30d),
            tuple("Forty  ", 40d),
            tuple("Fifth  ", 50d),
            tuple("Sixty  ", 60d),
            tuple("Seventy", 70d),
            tuple("Eighty ", 80d),
            tuple("Ninety ", 90d));

    @Test
    public void collectForTest() {
        System.out.println();
        System.out.println("collectForTest");

        // Defined separately so that SharableCollector can eliminate redundant code for dependent columns.
        final UnaryOperator<List<Double>> andThenR = Finishers.sortList();

        final String header = "percentile -> |  Agg | floor | halfUp | interpolate | ceil";
        System.out.println(header);
        for (double p = 0d; p <= 1.00d; p += 0.05d) {
            var col =
                    SharableCollector.of(Collectors.toList(),
                                         andThenR,
                                         Finishers.percentile(p, PercentileFunction.floor()));
            var r =
                    Seq.of(values)
                       .collect(Tuple.collectors(
                               Agg.<Double>percentile(p, Comparator.naturalOrder()),
                               col,
                               col.share(andThenR, Finishers.percentile(p, PercentileFunction.halfUp())),
                               col.share(andThenR, Finishers.percentile(p, PercentileFunction.interpolateDouble())),
                               col.share(andThenR, Finishers.percentile(p, PercentileFunction.ceil()))
                                                ));
            System.out.printf("   %5.3f   -> | %4.1f |  %4.1f |   %4.1f |    %4.1f     | %4.1f%n",
                              p,
                              r.v1.orElse(0d),
                              r.v2.orElse(0d),
                              r.v3.orElse(0d),
                              r.v4.orElse(0d),
                              r.v5.orElse(0d));
        }
        System.out.println(header);
    }

    @Test
    public void collectForIdTest() {
        System.out.println();
        System.out.println("collectForIdTest");
        // Defined separately so that SharableCollector can eliminate redundant code for dependent columns.
        final UnaryOperator<List<Double>> andThenR = Finishers.sortList();

        final String header = "percentile -> |  Agg | floor | halfUp | interpolate | ceil";
        System.out.println(header);
        for (double p = 0d; p <= 1.00d; p += 0.05d) {
            var idMap = new SharableCollector.IdMap();
            var r = Seq.of(values)
                       .collect(Tuple.collectors(
                               Agg.<Double>percentile(p, Comparator.naturalOrder()),
                               idMap.share("A", Collectors.toList(), andThenR, Finishers.percentile(p, PercentileFunction.floor())),
                               idMap.share("A", Collectors.toList(), andThenR, Finishers.percentile(p, PercentileFunction.halfUp())),
                               idMap.share("A", Collectors.toList(), andThenR, Finishers.percentile(p, PercentileFunction.interpolateDouble())),
                               idMap.share("A", Collectors.toList(), andThenR, Finishers.percentile(p, PercentileFunction.ceil()))
                                                ));
            System.out.printf("   %5.3f   -> | %4.1f |  %4.1f |   %4.1f |    %4.1f     | %4.1f%n",
                              p,
                              r.v1.orElse(0d),
                              r.v2.orElse(0d),
                              r.v3.orElse(0d),
                              r.v4.orElse(0d),
                              r.v5.orElse(0d));
        }
        System.out.println(header);
    }

    @Test
    public void collectByTest() {
        System.out.println();
        System.out.println("collectByTest");

        // Defined separately so that SharableCollector can eliminate redundant code for dependent columns.
        final Function<Tuple2<String, Double>, Double> keyExtractor = t -> t.v2;
        // Defined separately so that SharableCollector can eliminate redundant code for dependent columns.
        final UnaryOperator<List<Tuple2<String, Double>>> andThenR = Finishers.sortList(keyExtractor);
        final String header = "percentile -> | Agg             | floor           | halfUp          | ceil";
        System.out.println(header);
        for (double p = 0d; p <= 1.00d; p += 0.05d) {
            var col =
                    SharableCollector.of(Collectors.toList(), andThenR, Finishers.percentile(p, PercentileFunction.floor()));
            var r =
                    Seq.seq(namedValues)
                       .collect(Tuple.collectors(
                               Agg.percentileBy(p, keyExtractor),
                               col,
                               col.share(andThenR, Finishers.percentile(p, PercentileFunction.halfUp())),
                               col.share(andThenR, Finishers.percentile(p, PercentileFunction.ceil()))
                                                ));
            System.out.printf("   %5.3f   -> | %s | %s | %s | %s %n",
                              p,
                              r.v1.orElse(null),
                              r.v2.orElse(null),
                              r.v3.orElse(null),
                              r.v4.orElse(null));
        }
        System.out.println(header);
    }

    @Test
    public void collectByIdTest() {
        System.out.println();
        System.out.println("collectByIdTest");

        // Defined separately so that SharableCollector can eliminate redundant code for dependent columns.
        final Function<Tuple2<String, Double>, Double> keyExtractor = t -> t.v2;
        // Defined separately so that SharableCollector can eliminate redundant code for dependent columns.
        final UnaryOperator<List<Tuple2<String, Double>>> andThenR = Finishers.sortList(keyExtractor);

        final String header = "percentile -> | Agg             | floor           | halfUp          | ceil";
        System.out.println(header);
        for (double p = 0d; p <= 1.00d; p += 0.05d) {
            var idMap = new SharableCollector.IdMap();
            var r =
                    Seq.seq(namedValues)
                       .collect(Tuple.collectors(
                               Agg.percentileBy(p, keyExtractor),
                               idMap.share("A", Collectors.toList(), andThenR, Finishers.percentile(p, PercentileFunction.floor())),
                               idMap.share("A", Collectors.toList(), andThenR, Finishers.percentile(p, PercentileFunction.halfUp())),
                               idMap.share("A", Collectors.toList(), andThenR, Finishers.percentile(p, PercentileFunction.ceil()))
                                                ));
            System.out.printf("   %5.3f   -> | %s | %s | %s | %s %n",
                              p,
                              r.v1.orElse(null),
                              r.v2.orElse(null),
                              r.v3.orElse(null),
                              r.v4.orElse(null));
        }
        System.out.println(header);
    }
}
