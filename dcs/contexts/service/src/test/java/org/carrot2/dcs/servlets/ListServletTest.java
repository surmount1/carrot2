package org.carrot2.dcs.servlets;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.carrot2.dcs.client.ListResponse;
import org.carrot2.language.LanguageComponents;
import org.junit.Test;

public class ListServletTest extends AbstractServletTest {
  @Test
  public void testGet() throws Exception {
    setupMockTemplates(
        (name) -> resourceStream("template1.json"),
        "01 template2.json",
        "02-template1.json",
        "03 - template3.json");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    ListServlet servlet = new ListServlet();
    servlet.init(config);
    servlet.doGet(request, response);
    pw.flush();

    ObjectMapper om = new ObjectMapper();
    String content = sw.toString();
    ListResponse response = om.readValue(content, ListResponse.class);

    Assertions.assertThat(response.algorithms.keySet())
        .containsExactly("Bisecting K-Means", "Dummy", "Lingo", "STC");

    List<String> allLangs =
        LanguageComponents.languages().stream().sorted().collect(Collectors.toList());
    response.algorithms.forEach(
        (algorithm, langs) -> {
          Assertions.assertThat(langs)
              .as("Algorithm: " + algorithm)
              .containsExactlyElementsOf(allLangs);
        });

    Assertions.assertThat(response.templates)
        .containsExactly("template2", "template1", "template3");

    Assertions.assertThat(content).isEqualToIgnoringNewLines(resourceString("list.response.json"));
  }

  @Test
  public void testTemplateFiltering() throws Exception {
    setupMockTemplates("template1.json", "template-unavailable.json", "template-no-algorithm.json");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    ListServlet servlet = new ListServlet();
    servlet.init(config);
    servlet.doGet(request, response);
    pw.flush();

    ObjectMapper om = new ObjectMapper();
    Assertions.assertThat(om.readValue(sw.toString(), ListResponse.class).templates)
        .containsOnly("template1", "template-no-algorithm");
  }
}