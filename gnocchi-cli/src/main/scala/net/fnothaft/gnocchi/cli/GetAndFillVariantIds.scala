/**
 * Copyright 2016 Taner Dagdelen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fnothaft.gnocchi.cli

import org.apache.spark.SparkContext

object GetAndFillVariantIds {
  def apply(sc: SparkContext, mapPath: String, vcfPath: String, outPath: String) {
    val mapLines = sc.textFile(mapPath)
    val idsAndPos = mapLines.map(line => {
      val info = line.split("\t")
      val chrom = info(0)
      val variantId = info(1)
      val pos = info(3)
      ((chrom, pos), variantId)
    })
    val vcfLines = sc.textFile(vcfPath)
    val vcfPos = vcfLines.map(line => {
      val info = line.split("\t")
      val variantId = info(2)
      val pos = info(1)
      val chrom = info(0)
      ((chrom, pos), info)
    })
    val editedVCF = idsAndPos.cogroup(vcfPos)
      .map(variant => {
        val ((chrom, pos), (variantId, vcfLine)) = variant
        vcfLine.toList.head.slice(0, 2) + variantId.toList.head + vcfLine.toList.head.slice(3, vcfLine.toList.head.length)
      })
    editedVCF.saveAsTextFile(outPath)
  }
}