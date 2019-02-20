package org.carrot2.clustering;

import org.carrot2.attrs.AttrString;

import java.util.*;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;

public class SharedInfrastructure {
  public static AttrString queryHintAttribute() {
    return AttrString.builder()
        .label("Query hint")
        .defaultValue(null);
  }

  private static class ClusterData<T> {
    final Cluster<T> cluster;
    final double score;
    final String label;
    final int recursiveDocumentCount;

    public ClusterData(Cluster<T> cluster, double score, int recursiveDocumentCount) {
      this.cluster = cluster;
      this.label = String.join(", ", cluster.getLabels());
      this.score = score;
      this.recursiveDocumentCount = recursiveDocumentCount;
    }
  }


  public static <T> List<Cluster<T>> reorderByWeightedScoreAndSize(List<Cluster<T>> clusters, double scoreWeight) {
    Comparator<ClusterData> comparator =
        Comparator.<ClusterData> comparingDouble(data -> data.score).reversed().thenComparing(
            Comparator.nullsFirst(
                Comparator.comparing(data -> data.label)));

    return clusters.stream()
        .map(cluster -> {
          int docCount = recursiveDocumentCount(cluster);
          double score = Math.pow(docCount, 1d - scoreWeight) * Math.pow(cluster.getScore(), scoreWeight);
          return new ClusterData<T>(cluster, score, docCount);
        })
        .sorted(comparator)
        .map(data -> data.cluster)
        .collect(Collectors.toList());
  }

  public static <T extends Document> List<Cluster<T>> reorderByDescendingSizeAndLabel(ArrayList<Cluster<T>> clusters) {
    Comparator<ClusterData> comparator =
        Comparator.<ClusterData> comparingInt(data -> data.recursiveDocumentCount).reversed().thenComparing(
            Comparator.nullsFirst(
                Comparator.comparing(data -> data.label)));

    return clusters.stream()
        .map(cluster -> {
          int docCount = recursiveDocumentCount(cluster);
          return new ClusterData<T>(cluster, 0, docCount);
        })
        .sorted(comparator)
        .map(data -> data.cluster)
        .collect(Collectors.toList());
  }

  private static int recursiveDocumentCount(Cluster<?> cluster) {
    Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    ArrayDeque<Cluster<?>> queue = new ArrayDeque<>();
    queue.add(cluster);

    while (!queue.isEmpty()) {
      Cluster<?> c = queue.removeLast();
      visited.addAll(c.getDocuments());
      queue.addAll(cluster.getSubclusters());
    }

    return visited.size();
  }
}