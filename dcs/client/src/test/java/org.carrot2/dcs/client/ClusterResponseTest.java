package org.carrot2.dcs.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.carrot2.clustering.Cluster;
import org.junit.Test;

public class ClusterResponseTest {
  @Test
  public void testStructure() throws JsonProcessingException {
    List<Cluster<Integer>> clusters = new ArrayList<>();

    Cluster<Integer> c = new Cluster<>();
    c.addLabel("foo");
    c.addLabel("bar");
    c.setScore(36.6);
    c.addDocument(3);
    c.addDocument(2);
    c.addDocument(1);
    clusters.add(c);

    c = new Cluster<>();
    c.addLabel("baz");
    c.addDocument(4);
    clusters.add(c);

    ClusterResponse response = new ClusterResponse(clusters);
    ObjectMapper om = new ObjectMapper();
    String s = om.writerWithDefaultPrettyPrinter().writeValueAsString(response);
    System.out.println(s);
  }
}