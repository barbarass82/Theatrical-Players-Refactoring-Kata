import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class StatementPrinter {

    //TODO rename frmt
    private static final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

    public String print(Invoice invoice, Map<String, Play> plays) {

        List<Whatever.WhateverDetail> details = invoice
                .performances
                .stream()
                .map(performance -> {
                    return new Whatever.WhateverDetail(
                            performance,
                            plays.get(performance.playID),
                            getAmountForPerformance(performance, plays.get(performance.playID)),
                            getVolumeCredits(performance, plays.get(performance.playID)));
                })
                .collect(Collectors.toList());
        Whatever whatever = new Whatever(
                invoice,
                details.stream().map(Whatever.WhateverDetail::getAmount).reduce((amount1, amount2) -> amount1 + amount2).get(), // TODO remove .get()
                details.stream().map(Whatever.WhateverDetail::getVolumeCredits).collect(Collectors.summingInt(Integer::intValue)),
                details);

        return print2(whatever);
    }



    /**
     * TODO rename method
     * @param whatever
     * @return
     */
    private String print2(Whatever whatever) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Statement for %s\n", whatever.getInvoice().customer));

        whatever
                .getDetails()
                .stream()
                .map(detail -> String.format("  %s: %s (%s seats)\n", detail.getPlay().name, frmt.format(detail.getAmount() / 100), detail.getPerformance().audience))
                .forEach(stringBuilder::append);

        stringBuilder.append(String.format("Amount owed is %s\n", frmt.format(whatever.totalAmount / 100)));
        stringBuilder.append(String.format("You earned %s credits\n", whatever.volumeCredits));
        return stringBuilder.toString();
    }

    /**
     * TODO extract play type strings and magic numbers
     * @param perf
     * @param play
     * @return
     */
    private int getVolumeCredits(final Performance perf, final Play play) {
        int volumeCredits = Math.max(perf.audience - 30, 0);
        // add extra credit for every ten comedy attendees
        if ("comedy".equals(play.type)) volumeCredits += Math.floor(perf.audience / 5);
        return volumeCredits;
    }

    /**
     * TODO extract play type strings and magic numbers
     * @param perf
     * @param play
     * @return
     */
    private int getAmountForPerformance(final Performance perf, final Play play) {

        if ("tragedy".equals(play.type)) {
            int thisAmount = 40000;
            if (perf.audience > 30) {
                thisAmount += 1000 * (perf.audience - 30);
            }
            return thisAmount;
        }
        if ("comedy".equals(play.type)) {
            int thisAmount = 30000;
            if (perf.audience > 20) {
                thisAmount += 10000 + 500 * (perf.audience - 20);
            }
            return thisAmount += 300 * perf.audience;
        }

        throw new Error("unknown type: ${play.type}");

    }

    public static class Whatever {
        private Invoice invoice;
        private int totalAmount;
        private int volumeCredits;
        private List<WhateverDetail> details;

        public Whatever(Invoice invoice, int totalAmount, int volumeCredits, List<WhateverDetail> details) {
            this.invoice = invoice;
            this.totalAmount = totalAmount;
            this.volumeCredits = volumeCredits;
            this.details = details;
        }

        public Invoice getInvoice() {
            return invoice;
        }

        public int getTotalAmount() {
            return totalAmount;
        }

        public int getVolumeCredits() {
            return volumeCredits;
        }

        public List<WhateverDetail> getDetails() {
            return details;
        }

        public static class WhateverDetail {
            private Performance performance;
            private Play play;
            private int amount;
            private int volumeCredits;

            public WhateverDetail(Performance performance, Play play, int amount, int volumeCredits) {
                this.performance = performance;
                this.play = play;
                this.amount = amount;
                this.volumeCredits = volumeCredits;
            }

            public Performance getPerformance() {
                return performance;
            }

            public Play getPlay() {
                return play;
            }

            public int getAmount() {
                return amount;
            }

            public int getVolumeCredits() {
                return volumeCredits;
            }
        }
    }
}