package org.junnikym.springfbp

import java.io.File

class ImportFileCompileException(
        files: File
): RuntimeException("'${files.absolutePath}' didn't compiled") {

}