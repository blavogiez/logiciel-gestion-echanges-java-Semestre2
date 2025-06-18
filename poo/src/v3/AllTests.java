package v3;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

// This class runs all test classes in the 'current' package
@Suite
@SelectClasses({
    PersonTest.class,
    CriteriaTest.class,
    ExchangeTest.class,
    CountryVisitTest.class,
    PeopleManagerTest.class
})
public class AllTests {
    // No code needed, the annotations handle everything
}