package org.junnikym.springfbp

import java.io.File

data class ImportClassFile (
        val beanName: String?,
        val classFile: File,
) {

    constructor(classFilePath: String) : this(null, File(classFilePath))
    constructor(beanName: String? = null, classFilePath: String) : this(beanName, File(classFilePath))

}