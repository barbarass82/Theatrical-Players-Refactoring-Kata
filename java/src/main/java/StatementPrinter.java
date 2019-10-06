import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StatementPrinter {

    private static final String COMEDY = "comedy";
    private static final String TRAGEDY = "tragedy";

    private static final NumberFormat amountFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public String print(Invoice invoice, List<Play> plays) {

        if(plays == null || plays.isEmpty()) {
            throw new RuntimeException("No plays available.");
        }

        Map<String, Play> playMap = buildPlayMap(plays);
        List<Statement.StatementDetail> details = getStatementDetails(invoice, playMap);
        Statement statement = getStatement(invoice, details);

        return printStatementDetails(statement);
    }

    private Statement getStatement(Invoice invoice, List<Statement.StatementDetail> details) {
        return new Statement(
                invoice,
                details.stream().map(Statement.StatementDetail::getAmount)
                        .reduce((amount1, amount2) -> amount1 + amount2).get(), // TODO remove .get()
                details.stream().map(Statement.StatementDetail::getVolumeCredits)
                        .collect(Collectors.summingInt(Integer::intValue)),
                details);
    }

    private List<Statement.StatementDetail> getStatementDetails(Invoice invoice, Map<String, Play> playMap) {
        return invoice
                .performances
                .stream()
                .map(performance -> {
                    Play play = Optional.ofNullable(playMap.get(performance.playID))
                            .orElseThrow(() -> new RuntimeException("Play not found"));

                    return new Statement.StatementDetail(
                            performance,
                            play,
                            getAmountForPerformance(performance, play),
                            getVolumeCredits(performance, play));
                })
                .collect(Collectors.toList());
    }

    private Map<String, Play> buildPlayMap(List<Play> plays) {
        Map<String, Play> playMap = new HashMap<>();
        plays.forEach(play -> playMap.put(play.getId(),play));
        return playMap;
    }


    /**
     * TODO rename method - printStatementDetails, is ok?
     * @param statement
     * @return
     */
    private String printStatementDetails(Statement statement) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Statement for %s\n", statement.getInvoice().customer));

        statement
                .getDetails()
                .stream()
                .map(detail -> String.format("  %s: %s (%s seats)\n", detail.getPlay().getName(), amountFormat.format(detail.getAmount() / 100), detail.getPerformance().audience))
                .forEach(stringBuilder::append);

        stringBuilder.append(String.format("Amount owed is %s\n", amountFormat.format(statement.totalAmount / 100)));
        stringBuilder.append(String.format("You earned %s credits\n", statement.volumeCredits));
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
        if (COMEDY.equals(play.getType())) volumeCredits += Math.floor(perf.audience / 5);
        return volumeCredits;
    }

    /**
     * TODO extract play type strings and magic numbers
     * @param perf
     * @param play
     * @return
     */
    private int getAmountForPerformance(final Performance perf, final Play play) {

        if (TRAGEDY.equals(play.getType())) {
            int thisAmount = 40000;
            if (perf.audience > 30) {
                thisAmount += 1000 * (perf.audience - 30);
            }
            return thisAmount;
        }
        if (COMEDY.equals(play.getType())) {
            int thisAmount = 30000;
            if (perf.audience > 20) {
                thisAmount += 10000 + 500 * (perf.audience - 20);
            }
            return thisAmount += 300 * perf.audience;
        }

        throw new Error("unknown type: ${play.type}");

    }

    public static class Statement {
        private Invoice invoice;
        private int totalAmount;
        private int volumeCredits;
        private List<StatementDetail> details;

        public Statement(Invoice invoice, int totalAmount, int volumeCredits, List<StatementDetail> details) {
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

        public List<StatementDetail> getDetails() {
            return details;
        }

        public static class StatementDetail {
            private Performance performance;
            private Play play;
            private int amount;
            private int volumeCredits;

            public StatementDetail(Performance performance, Play play, int amount, int volumeCredits) {
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