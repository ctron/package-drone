package de.dentrassi.pm.testing;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

public class SetupTest extends AbstractServerTest
{

    private static FirefoxDriver driver;

    @BeforeClass
    public static void setupBrowser ()
    {
        driver = new FirefoxDriver ();
    }

    @AfterClass
    public static void destroyBrowser ()
    {
        driver.close ();
    }

    @Test
    public void test1 () throws Exception
    {
        driver.get ( getBase () );
        Assert.assertEquals ( resolve ( "/setup" ), driver.getCurrentUrl () );

        final Select select = new Select ( driver.findElementById ( "jdbcDriver" ) );
        select.selectByValue ( "com.mysql.jdbc.Driver" );

        driver.findElementById ( "url" ).sendKeys ( "jdbc:mysql://localhost/pm" );
        driver.findElementById ( "user" ).sendKeys ( "pm" );
        driver.findElementById ( "password" ).sendKeys ( "pm" );

        driver.findElementById ( "command" ).submit ();

        new WebDriverWait ( driver, 5000 ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return driver.findElementById ( "service-present" ).getText ().equals ( "true" );
            }
        } );
    }
}
