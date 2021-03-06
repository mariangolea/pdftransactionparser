package test.com.mariangolea.fintracker.banks.csvparser.ui.uncategorized;

import com.mariangolea.fintracker.banks.csvparser.api.preferences.UserPreferencesInterface;
import com.mariangolea.fintracker.banks.csvparser.impl.ui.uncategorized.edit.BankTransactionEditHandler;
import com.mariangolea.fintracker.banks.csvparser.impl.ui.uncategorized.edit.EditResult;
import com.mariangolea.fintracker.banks.csvparser.impl.ui.uncategorized.edit.UncategorizedTransactionApplyListener;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import test.com.mariangolea.fintracker.banks.csvparser.UserPreferencesTestFactory;

public class BankTransactionEditHandlerTest {

    @Test
    public void testApplyEditResult() {
        UserPreferencesTestFactory factory = new UserPreferencesTestFactory();
        UncategorizedTransactionApplyListener listener = () -> {
        };
        Extension handler = new Extension(listener, factory);
        EditResult result = new EditResult("a", "b", "c", "d");
        handler.applyResult(result);

        UserPreferencesInterface prefs = factory.getUserPreferencesHandler().getPreferences();
        assertEquals("b", prefs.getCompanyDisplayName("a"));
        assertEquals("c", prefs.getParent("b"));
        assertEquals("d", prefs.getParent("c"));
    }

    private class Extension extends BankTransactionEditHandler {

        public Extension(UncategorizedTransactionApplyListener applyListener, UserPreferencesTestFactory factory) {
            super(applyListener, factory.getUserPreferencesHandler().getPreferences());
        }

        @Override
        protected void applyResult(EditResult result) {
            super.applyResult(result);
        }
    }
}
