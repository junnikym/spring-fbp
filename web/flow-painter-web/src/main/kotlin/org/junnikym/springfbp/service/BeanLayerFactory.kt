package org.junnikym.springfbp.service

interface BeanLayerFactory {

    /**
     * update bean layer map in class
     */
    fun update()

    /**
     * it returns layer number of bean
     *
     * @param beanName name of bean
     * @return layer number
     */
    fun get(beanName: String): Int;

}