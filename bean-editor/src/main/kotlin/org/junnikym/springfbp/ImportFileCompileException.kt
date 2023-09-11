package org.junnikym.springfbp

import java.io.File

class ImportFileCompileException(
        files: List<File>
): RuntimeException("'[${files.map(File::getAbsolutePath).reduce{total, it-> "$total, '$it'"}}] didn't compiled") {

}