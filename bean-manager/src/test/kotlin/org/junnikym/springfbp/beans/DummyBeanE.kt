package org.junnikym.springfbp.beans

import org.springframework.stereotype.Component

@Component
class DummyBeanE ( private val dummyBeanD: DummyBeanD ) {

    private var generalUnmanagedClass: DummyInterface? = UnmanagedClassB()
    
    fun switchByJavaFactoryMethod() {
        this.generalUnmanagedClass = UnmanagedClassC.of(true)
        this.generalUnmanagedClass = UnmanagedClassC.of(1)
        this.generalUnmanagedClass = UnmanagedClassC.of()
        this.generalUnmanagedClass = UnmanagedClassC.STATIC_OBJECT
    }

    fun switchByKotlinFactoryMethod() {
        this.generalUnmanagedClass = UnmanagedClassD.of()
        this.generalUnmanagedClass = UnmanagedClassD.of(true)
        this.generalUnmanagedClass = unmanagedClassDOf()
    }

    fun run() {
        val unmanagedClassA = UnmanagedClassA()
        val result = unmanagedClassA.run()
        val resultMessage = java.lang.String("unmanagedClassB result is '$result'")
        println(resultMessage)
    }

    fun getStaticDummyField(): String {
        return UnmanagedClassC.DUMMY_STATIC_FIELD
    }

    private fun unmanagedClassDOf(): UnmanagedClassD {
        return UnmanagedClassD("in private method")
    }

}