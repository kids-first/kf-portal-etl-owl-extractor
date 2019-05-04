package io.kf.etl.ontology

import java.io.{BufferedInputStream, FileInputStream, IOException}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.util.zip.GZIPInputStream

import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

import scala.io.Source

class ExtractorTest extends FeatureSpec with GivenWhenThen with Matchers {

  feature("Owl Extractor") {

    scenario("Extract ID-Name for mondo and ncit files") {
      Given("An OWL file for mondo")
      val mondoInput = getClass.getClassLoader.getResource("mondo.xml").toString

      And("An OWL file for ncit")
      val ncitInput = getClass.getClassLoader.getResource("ncit.xml").toString

      withTempDirectory { output =>
        And("An output file for mondo")
        val mondoOutput = output.resolve("mondo.tsv.tgz")

        And("An output file for ncit")
        val ncitOutput = output.resolve("ncit.tsv.tgz")

        When("Extract terms")
        Extractor.main(Array(mondoInput, mondoOutput.toUri.toString, ncitInput, ncitOutput.toUri.toString))

        Then("A compressed TSV file containing columns id and name has been produced for mondo")
        gis(mondoOutput).getLines().toSeq shouldBe Seq(
          "MONDO:0005072\tneuroblastoma",
          "MONDO:0005073\tmelanocytic nevus"
        )

        And("A compressed TSV file containing columns id and name has been produced for ncit")
        gis(ncitOutput).getLines().toSeq shouldBe Seq(
          "NCIT:C18009\tTumor Tissue",
          "NCIT:C43234\tNot Reported"
        )
      }


    }

    def gis(p: Path): Source = Source.fromInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(p.toFile))))

    def withTempDirectory[T](block: Path => T): T = {
      val output = Files.createTempDirectory("output")
      try {
        block(output)
      } finally {
        Files.walkFileTree(output, new SimpleFileVisitor[Path]() {
          override def visitFile(file: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult = {
            Files.delete(file)
            FileVisitResult.CONTINUE
          }

          override def postVisitDirectory(dir: Path, e: IOException): FileVisitResult = {
            import java.nio.file.{FileVisitResult, Files}
            if (e == null) {
              Files.delete(dir)
              return FileVisitResult.CONTINUE
            }
            throw e

          }
        })


      }


    }
  }
}