/*******************************************************************************
 * (C) Copyright 2015 Haifeng Li
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
 *******************************************************************************/

package smile.io

import java.io.PrintWriter
import com.thoughtworks.xstream.XStream
import smile.data._, parser._, parser.microarray._
import smile.math.matrix.SparseMatrix

/** High level I/O operators.
  *
  * @author Haifeng Li
  */
trait Operators {

  /** Writes an object/model to a file. */
  def write[T <: Object](x: T, file: String): Unit = {
    val xstream = new XStream
    val xml = xstream.toXML(x)
    new PrintWriter(file) {
      write(xml)
      close
    }
  }

  /** Reads an object/model back from a file created by write command. */
  def read(file: String): AnyRef = {
    val xml = scala.io.Source.fromFile(file).mkString
    val xstream = new XStream
    xstream.fromXML(xml)
  }

  /** Reads an ARFF file. */
  def readArff(file: String, responseIndex: Int = -1): AttributeDataset = {
    new ArffParser().setResponseIndex(responseIndex).parse(file)
  }

  /** Reads a LivSVM file. */
  def readLibsvm(file: String): SparseDataset = {
    new LibsvmParser().parse(file)
  }

  /** Reads Harwell-Boeing column-compressed sparse matrix. */
  def readSparseMatrix(file: String): SparseMatrix = {
    new SparseMatrixParser().parse(file)
  }

  /** Reads spare dataset in coordinate triple tuple list format.
    * Coordinate file stores a list of (row, column, value) tuples:
    * {{{
    * instanceID attributeID value
    * instanceID attributeID value
    * instanceID attributeID value
    * instanceID attributeID value
    * ...
    * instanceID attributeID value
    * instanceID attributeID value
    * instanceID attributeID value
    * }}}
    * Ideally, the entries are sorted (by row index, then column index) to
    * improve random access times. This format is good for incremental matrix
    * construction.
    *
    * Optionally, there may be 2 header lines
    * {{{
    * D    // The number of instances
    * W    // The number of attributes
    * }}}
    * or 3 header lines
    * {{{
    * D    // The number of instances
    * W    // The number of attributes
    * N    // The total number of nonzero items in the dataset.
    * }}}
    * These header lines will be ignored.
    *
    * @param file the file path.
    * @param arrayIndexOrigin the starting index of array. By default, it is
    * 0 as in C/C++ and Java. But it could be 1 to parse data produced
    * by other programming language such as Fortran.
    */
  def readSparseData(file: String, arrayIndexOrigin: Int = 0): SparseDataset = {
    new SparseDatasetParser(arrayIndexOrigin).parse(file)
  }

  /** Reads binary sparse dataset. Each item is stored as an integer array, which
    * are the indices of nonzero elements in ascending order
    */
  def readBinarySparseData(file: String): BinarySparseDataset = {
    new BinarySparseDatasetParser().parse(file)
  }

  /** Reads a delimited text file. By default, the parser expects a
    * white-space-separated-values file. Each line in the file corresponds
    * to a row in the table. Within a line, fields are separated by white spaces,
    * each field belonging to one table column. This class can also be
    * used to read other text tabular files by setting delimiter character
    * such ash ','. The file may contain comment lines (starting with '%')
    * and missing values (indicated by placeholder '?').
    *
    * @param file the file path
    * @param delimiter delimiter string
    * @param comment the start of comment lines
    * @param missing the missing value placeholder
    * @param header true if the first row is header/column names
    * @param rowNames true if the first column is row id/names
    * @return an attribute dataset
    */
  def readTable(file: String, delimiter: String = "\\s+", comment: String = "%", missing: String = "?", header: Boolean = false, rowNames: Boolean = false): AttributeDataset = {
    val parser = new DelimitedTextParser
    parser.setDelimiter(delimiter)
      .setCommentStartWith(comment)
      .setMissingValuePlaceholder(missing)
      .setColumnNames(header)
      .setRowNames(rowNames)
      .parse(file)
  }

  /** Reads a delimited text file with response variable. By default, the parser expects a
    * white-space-separated-values file. Each line in the file corresponds
    * to a row in the table. Within a line, fields are separated by white spaces,
    * each field belonging to one table column. This class can also be
    * used to read other text tabular files by setting delimiter character
    * such ash ','. The file may contain comment lines (starting with '%')
    * and missing values (indicated by placeholder '?').
    *
    * @param file the file path
    * @param response the attribute type of response variable
    * @param responseIndex the column index of response variable. The column index starts at 0.
    * @param delimiter delimiter string
    * @param comment the start of comment lines
    * @param missing the missing value placeholder
    * @param header true if the first row is header/column names
    * @param rowNames true if the first column is row id/names
    * @return an attribute dataset
    */
  def readTable2(file: String, response: Attribute, responseIndex: Int, delimiter: String = "\\s+", comment: String = "%", missing: String = "?", header: Boolean = false, rowNames: Boolean = false): AttributeDataset = {
    val parser = new DelimitedTextParser
    parser.setResponseIndex(response, responseIndex)
      .setDelimiter(delimiter)
      .setCommentStartWith(comment)
      .setMissingValuePlaceholder(missing)
      .setColumnNames(header)
      .setRowNames(rowNames)
      .parse(file)
  }

  /** Reads a CSV file. */
  def readCsv(file: String, comment: String = "%", missing: String = "?", header: Boolean = false, rowNames: Boolean = false): AttributeDataset = {
    readTable(file, ",", comment, missing, header, rowNames)
  }

  /** Reads a CSV file  with response variable. */
  def readCsv2(file: String, response: Attribute, responseIndex: Int, comment: String = "%", missing: String = "?", header: Boolean = false, rowNames: Boolean = false): AttributeDataset = {
    readTable2(file, response, responseIndex, ",", comment, missing, header, rowNames)
  }

  /** Reads GCT microarray gene expression file. */
  def readGct(file: String): AttributeDataset = {
    new GCTParser().parse(file)
  }

  /** Reads PCL microarray gene expression file. */
  def readPcl(file: String): AttributeDataset = {
    new PCLParser().parse(file)
  }

  /** Reads RES microarray gene expression file. */
  def readRes(file: String): AttributeDataset = {
    new RESParser().parse(file)
  }

  /** Reads TXT microarray gene expression file.
    * The TXT format is a tab delimited file
    * format that describes an expression dataset. It is organized as follows:
    *
    * The first line contains the labels Name and Description followed by the
    * identifiers for each sample in the dataset. The Description is optional.
    *
    * Line format:
    * {{{
    * Name(tab)Description(tab)(sample 1 name)(tab)(sample 2 name) (tab) ... (sample N name)
    * }}}
    * Example:
    * {{{
    * Name Description DLBC1_1 DLBC2_1 ... DLBC58_0
    * }}}
    * The remainder of the file contains data for each of the genes. There is one
    * line for each gene. Each line contains the gene name, gene description, and
    * a value for each sample in the dataset. If the first line contains the
    * Description label, include a description for each gene. If the first line
    * does not contain the Description label, do not include descriptions for
    * any gene. Gene names and descriptions can contain spaces since fields are
    * separated by tabs.
    *
    * Line format:
    * {{{
    * (gene name) (tab) (gene description) (tab) (col 1 data) (tab) (col 2 data) (tab) ... (col N data)
    * }}}
    *
    * Example:
    * {{{
    *  AFFX-BioB-5_at AFFX-BioB-5_at (endogenous control) -104 -152 -158 ... -44
    * }}}
    */
  def readTxt(file: String): AttributeDataset = {
    new TXTParser().parse(file)
  }
}
