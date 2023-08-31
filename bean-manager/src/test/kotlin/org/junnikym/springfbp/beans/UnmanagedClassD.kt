package org.junnikym.springfbp.beans

class UnmanagedClassD(private val extraContents: String? = null): DummyInterface {

    companion object {
        fun of() = UnmanagedClassD()

        fun of(isOption: Boolean): DummyInterface {
            if(isOption)
                return UnmanagedClassD("Selected")

            return UnmanagedClassD("Non-Selected")
        }
    }

    override fun run(): String {
        return "dummy class D run :: $extraContents"
    }

}