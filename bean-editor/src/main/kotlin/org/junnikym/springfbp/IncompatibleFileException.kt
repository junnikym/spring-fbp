package org.junnikym.springfbp

import java.io.File

class IncompatibleFileException(file: File): RuntimeException("${file.path} is incompatible file") {
}