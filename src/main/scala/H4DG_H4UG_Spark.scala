import org.apache.spark.{SparkConf, SparkContext}

object H4DG_H4UG_Spark {
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("Invalid parameters. Use \"spark-submit [filename.jar] [-d/-u] [num of nodes]\"")
      sys.exit(-1)
    }

    val n: Long = args(1).toLong
    val e: Long = ((2.0 / 3.0) * n * math.log(n) + (0.38481 * n)).toLong
    val directed: Boolean = if (args.contains("-u")) false else true

    val conf = new SparkConf().setAppName("H4DG-H4UG for Spark")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    val distData = sc.range(0, e)
    val pairs = distData.map(x => (new RMat().generateEdge(n)._1, 1))
    val degrees = pairs.reduceByKey(_ + _)
    val edges = if (directed) degrees.flatMap(pair => createDirectedEdges(pair._1, pair._2)) else degrees.flatMap(pair => createUndirectedEdges(pair._1, pair._2, n))

    val path = System.getProperty("user.dir")
    val timestamp = System.currentTimeMillis()
    edges.saveAsTextFile(s"$path/${if (!directed) "un"}directed-graph-$timestamp")
  }

  def createDirectedEdges(source: Long, degree: Int): Array[(Long, Long)] = {
    val edges = new Array[(Long, Long)](degree)
    var i: Int = 0
    var target: Long = source - 1
    while (i < degree && target >= 0) {
      edges(i) = (source, target)
      i += 1
      target -= 1
    }
    target = source + 1
    while (i < degree) {
      edges(i) = (source, target)
      i += 1
      target += 1
    }
    edges
  }

  def createUndirectedEdges(source: Long, degree: Int, n: Long): Array[(Long, Long)] = {
    val edges = new Array[(Long, Long)](degree)
    var i: Int = 0
    var target: Long = source + 1
    while (i < degree && target < n) {
      edges(i) = (source, target)
      i += 1
      target += 1
    }
    target = 0
    while (i < degree) {
      edges(i) = (source, target)
      i += 1
      target += 1
    }
    edges
  }
}
