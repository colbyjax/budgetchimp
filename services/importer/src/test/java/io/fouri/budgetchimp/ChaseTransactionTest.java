package io.fouri.budgetchimp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


public class ChaseTransactionTest {

    /******** Format Date Tests **********/
    @Test
    public void testGoodInputDate() {
        // Format expected M/d/yy
        assertEquals("20190101", ChaseTransaction.formatDate("1/1/19"));
        assertEquals("20190101", ChaseTransaction.formatDate("01/01/19"));
        assertEquals("20190101", ChaseTransaction.formatDate("01/1/19"));
        assertEquals("20190101", ChaseTransaction.formatDate("1/01/19"));
        assertEquals("20200101", ChaseTransaction.formatDate("01/01/2020"));
        assertEquals("20000101", ChaseTransaction.formatDate("1/1/00"));
    }

    @Test
    public void testBadInputDate() {
        assertEquals(null, ChaseTransaction.formatDate("1-1-19") );
        assertEquals(null, ChaseTransaction.formatDate("01-01-20") );
        assertEquals(null, ChaseTransaction.formatDate("20200819") );
        assertEquals(null, ChaseTransaction.formatDate("3 Jun 2008 11:05:30") );
        assertEquals(null, ChaseTransaction.formatDate("2011-12-03T10:15:30Z") );
        assertEquals(null, ChaseTransaction.formatDate("XYZLKJADF;///--&*") );
        assertEquals(null, ChaseTransaction.formatDate("") );
    }

    @Test
    public void testNullInputDate() {
        assertEquals(null, ChaseTransaction.formatDate(null) );
    }


}
