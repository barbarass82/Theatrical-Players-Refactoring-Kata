import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.approvaltests.Approvals.verify;

public class StatementPrinterTests {

    @Test
    void exampleStatement() {
        List<Play> plays = new ArrayList<>();
        plays.add(new Play("hamlet", "Hamlet", "tragedy"));
        plays.add(new Play("as-like","As You Like It", "comedy"));
        plays.add(new Play("othello","Othello", "tragedy"));

        Invoice invoice = new Invoice("BigCo", Arrays.asList(
                new Performance[]{
                        new Performance("hamlet", 55),
                        new Performance("as-like", 35),
                        new Performance("othello", 40)
                }));

        StatementPrinter statementPrinter = new StatementPrinter();
        String result = statementPrinter.print(invoice, plays);

        verify(result);
    }

    @Test
    void statementWithNewPlayTypes() {
        List<Play> plays = new ArrayList<>();
        plays.add(new Play("henry-v","Henry V", "history"));
        plays.add(new Play("as-like","As You Like It", "pastoral"));

        Invoice invoice = getDefaultInvoice();

        StatementPrinter statementPrinter = new StatementPrinter();
        Assertions.assertThrows(Error.class, () -> {
            statementPrinter.print(invoice, plays);
        });
    }

    @Test
    public void whenPrintStatement_givenNoPlaysAvailable_shouldThrowException() {
        Invoice invoice = getDefaultInvoice();

        StatementPrinter statementPrinter = new StatementPrinter();
        Assertions.assertThrows(Error.class, () -> {
            statementPrinter.print(invoice, null);
        });
    }

    private Invoice getDefaultInvoice() {
        return new Invoice("BigCo", Arrays.asList(
                new Performance[]{
                        new Performance("henry-v", 53),
                        new Performance("as-like", 55)
                }));
    }
}
