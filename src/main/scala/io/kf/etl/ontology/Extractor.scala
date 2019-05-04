package io.kf.etl.ontology

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.net.URL
import java.util.zip.GZIPOutputStream

object Extractor extends App {

  try {
    val Array(mondoInput, mondoOutput, ncitInput, ncitOutput) = args

    extract(mondoInput, mondoOutput)
    extract(ncitInput, ncitOutput)

  } catch {
    case e: Exception =>
      e.printStackTrace()
      System.exit(-1)
  }

  private def extract(input: String, output: String): Unit = {
    val terms = OwlManager.getOntologyTermsFromURL(new URL(input))
    write(terms, new URL(output))
  }

  private def write(terms: Seq[OntologyTerm], url: URL): Unit = {
    val bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(url.getFile))))
    try {
      terms.foreach { t =>
        bw.write(s"${t.id}\t${t.name}\n")
      }
    } finally {
      bw.close()
    }
  }


}
