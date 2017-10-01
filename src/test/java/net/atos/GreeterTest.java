package net.atos;

import java.io.File;
import java.net.URL;
import javax.inject.Inject;
import javax.ws.rs.core.Application;
import org.arquillian.cube.CubeIp;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class GreeterTest {

  @Deployment
  public static Archive<?> createDeployment() {
    // This lets us use RestAssured for in-container testing
    File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                                  .resolve("io.rest-assured:rest-assured")
                                  .withTransitivity().asFile();

    Archive war = ShrinkWrap.create(WebArchive.class, "test.war")
                    .addClass(Application.class)
                    .addClass(Greeter.class)
                    .addAsLibraries(libs)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    System.out.println(war.toString(true));
    return war;
  }

  @CubeIp(containerName = "fuse-test")
  private String cubeIp;

  @ArquillianResource
  private URL url;

  @Inject
  Greeter greeter;

  /***
   * This test uses an injected instance of our REST service to
   * do 'unit' style testing
   ***/
  @Test
  @InSequence(1)
  public void should_return_hello_world() {
    Assert.assertEquals("Hello World!", greeter.createGreeting(null).getEntity());
  }

  /***
   * This test checks that Cube is providing Enrichers
   * like URL and CubeIp.
   * It also shows that 'code coverage' means that a test
   * 'touches' a block of code. It does not ensure the
   * quality of that test. This test is rubbish!
   *
   ***/
  @Test
  @RunAsClient
  @InSequence(2)
  public void injectTest() throws Exception {

    System.out.println("URL: " + url.toExternalForm());
    System.out.println("CubeIP: " + cubeIp);

    // This shows that JaCoCo doesn't care about the quality of the test!
    final Object result = url.openConnection().getInputStream();

    assertThat(true, is(true));
  }

  /***
   * This test shows RestAssured DSL testing 'outside'
   * the container using '@RunAsClient.
   * This doesn't generate JaCoCo data _unless_ we
   * execute a further test in the remote
   * JVM - which triggers stats collection
   *
   */
  @Test
  @RunAsClient
  @InSequence(3)
  public void should_return_default_greeting() {
    given().
    when().
      get(url.toExternalForm()).
    then().
      body(is("Hello World!"));
  }

  /***
   * This test shows RestAssured DSL testing 'inside'
   * the container.
   * This does generate JaCoCo data
   *
   */
  @Test
  @InSequence(4)
  public void should_return_request_logged() {
    given().
    when().
      get(url.toExternalForm() + "?name=Bar").
    then().
      statusCode(202);
  }

  @Test
  @RunAsClient
  @InSequence(5)
  public void should_return_personal_greeting() {
    given().
    when().
      get(url.toExternalForm() + "?name=Foo").
    then().
      statusCode(200).
      body(is("Hello, Foo!"));
  }

  /***
   * This is a dummy method used solely to
   * generate JaCoCo stats by running a test
   * in the remote container JVM
   *
   */
  @Test
  @InSequence(999)
  public void dummy_to_capture_stats() {
    Assert.assertTrue(true);
  }

}
