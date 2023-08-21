package org.junnikym.springfbp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(locations= ["classpath:test.properties"])
class DefaultBeanDependencyLinkFactoryTest(
    @Autowired private val beanFactory: ConfigurableListableBeanFactory,
) {

    /**
     * [ Test Bean Structure ]
     *                         <must be root>
     * ┌────────┐   ┌────────┐   ┌────────┐
     * │ Bean C ├─┬─┤ Bean B ├───┤ Bean A │
     * └────────┘ │ └────────┘   └────────┘
     * ┌────────┐ │              ┌────────┐
     * │ Bean D ├─└──────────────┤ Bean E │
     * └────────┘                └────────┘
     */

    private val factory: DefaultBeanDependencyLinkFactory = DefaultBeanDependencyLinkFactory()

    @Test
    @Order(1)
    fun add_link() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")

        // when
        factory.add(linkAtoB)
        factory.add(linkBtoC)
        factory.add(linkBtoD)
        factory.add(linkEtoD)

        // then
        verifyLinkMap_whenAdd(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        verifyReverseLinkMap_whenAdd(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        verifyLinkList_whenAdd(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        verifyRootNodeNameSet_whenAdd(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
    }

    @Test
    @Order(1)
    fun add_link_by_list() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")
        val linkList = listOf(linkAtoB, linkBtoC, linkBtoD, linkEtoD)

        // when
        factory.add(linkList)

        // then
        verifyLinkMap_whenAdd(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        verifyReverseLinkMap_whenAdd(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        verifyLinkList_whenAdd(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        verifyRootNodeNameSet_whenAdd(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
    }

    @Test
    @Order(2)
    fun has_parent_test() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")
        val linkList = listOf(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        factory.add(linkList)

        // when
        val hasParentA = factory.hasParent("dummyBeanA")
        val hasParentB = factory.hasParent("dummyBeanB")
        val hasParentC = factory.hasParent("dummyBeanC")
        val hasParentD = factory.hasParent("dummyBeanD")
        val hasParentE = factory.hasParent("dummyBeanE")

        // then
        assertEquals(hasParentA, true)
        assertEquals(hasParentB, true)
        assertEquals(hasParentC, false)
        assertEquals(hasParentD, false)
        assertEquals(hasParentE, true)
    }

    @Test
    @Order(2)
    fun has_parent_of_from_node_test() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")
        val linkList = listOf(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        factory.add(linkList)

        // when
        val hasParentOfFromNodeInLinkAtoB = factory.hasParentOfFromNode(linkAtoB)
        val hasParentOfFromNodeInLinkBtoC = factory.hasParentOfFromNode(linkBtoC)
        val hasParentOfFromNodeInLinkBtoD = factory.hasParentOfFromNode(linkBtoD)
        val hasParentOfFromNodeInLinkEtoD = factory.hasParentOfFromNode(linkEtoD)

        // then
        assertEquals(hasParentOfFromNodeInLinkAtoB, true)
        assertEquals(hasParentOfFromNodeInLinkBtoC, false)
        assertEquals(hasParentOfFromNodeInLinkBtoD, false)
        assertEquals(hasParentOfFromNodeInLinkEtoD, false)
    }

    @Test
    @Order(2)
    fun get_parent_name_test() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")
        val linkList = listOf(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        factory.add(linkList)

        // when
        val parentNamesOfBeanA = HashSet(factory.getParentNames("dummyBeanA"))
        val parentNamesOfBeanB = HashSet(factory.getParentNames("dummyBeanB"))
        val parentNamesOfBeanC = HashSet(factory.getParentNames("dummyBeanC"))
        val parentNamesOfBeanD = HashSet(factory.getParentNames("dummyBeanD"))
        val parentNamesOfBeanE = HashSet(factory.getParentNames("dummyBeanE"))

        // then
        assertEquals(parentNamesOfBeanA.size, 1)
        assertTrue(parentNamesOfBeanA.contains("dummyBeanB"))

        assertEquals(parentNamesOfBeanB.size, 2)
        assertTrue(parentNamesOfBeanB.contains("dummyBeanC"))
        assertTrue(parentNamesOfBeanB.contains("dummyBeanD"))

        assertEquals(parentNamesOfBeanC.size, 0)

        assertEquals(parentNamesOfBeanD.size, 0)

        assertEquals(parentNamesOfBeanE.size, 1)
        assertTrue(parentNamesOfBeanB.contains("dummyBeanD"))
    }

    @Test
    @Order(2)
    fun is_root_test() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")
        val linkList = listOf(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        factory.add(linkList)

        // when
        val isA_Root = factory.isRoot("dummyBeanA")
        val isB_Root = factory.isRoot("dummyBeanB")
        val isC_Root = factory.isRoot("dummyBeanC")
        val isD_Root = factory.isRoot("dummyBeanD")
        val isE_Root = factory.isRoot("dummyBeanE")

        // then
        assertEquals(isA_Root, false)
        assertEquals(isB_Root, false)
        assertEquals(isC_Root, true)
        assertEquals(isD_Root, true)
        assertEquals(isE_Root, false)
    }

    @Test
    @Order(2)
    fun get_root_links() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")
        val linkList = listOf(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        factory.add(linkList)

        // when
        val roots = HashSet(factory.getRootLinks())

        // then
        assertEquals(roots.contains(linkAtoB), false)
        assertEquals(roots.contains(linkBtoC), true)
        assertEquals(roots.contains(linkBtoD), true)
        assertEquals(roots.contains(linkEtoD), true)
    }

    @Test
    @Order(2)
    fun get_root_names() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")
        val linkList = listOf(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        factory.add(linkList)

        // when
        val roots = HashSet(factory.getRootNames())

        // then
        assertEquals(roots.contains("dummyBeanA"), false)
        assertEquals(roots.contains("dummyBeanB"), false)
        assertEquals(roots.contains("dummyBeanC"), true)
        assertEquals(roots.contains("dummyBeanD"), true)
        assertEquals(roots.contains("dummyBeanE"), false)
    }

    @Test
    @Order(2)
    fun get_links() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")
        val linkList = listOf(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        factory.add(linkList)

        // when
        val links = HashSet(factory.getLinks() as ArrayList<*>)

        // then
        assertTrue(links.contains(linkAtoB))
        assertTrue(links.contains(linkBtoC))
        assertTrue(links.contains(linkBtoD))
        assertTrue(links.contains(linkEtoD))
    }

    @Test
    @Order(2)
    fun get_links_individually() {
        // given
        val linkAtoB = beanDependencyLinkOf("dummyBeanA", "dummyBeanB")
        val linkBtoC = beanDependencyLinkOf("dummyBeanB", "dummyBeanC")
        val linkBtoD = beanDependencyLinkOf("dummyBeanB", "dummyBeanD")
        val linkEtoD = beanDependencyLinkOf("dummyBeanE", "dummyBeanD")
        val linkList = listOf(linkAtoB, linkBtoC, linkBtoD, linkEtoD)
        factory.add(linkList)

        // when
        val resultA = HashSet(factory.getLinks("dummyBeanA") as List<*>)
        val resultB = HashSet(factory.getLinks("dummyBeanB") as List<*>)
        val resultC = HashSet(factory.getLinks("dummyBeanC") as List<*>)
        val resultD = HashSet(factory.getLinks("dummyBeanD") as List<*>)
        val resultE = HashSet(factory.getLinks("dummyBeanE") as List<*>)


        // then
        assertEquals(resultA.size, 0)

        assertEquals(resultB.size, 1)
        assertTrue(resultB.contains(linkAtoB))

        assertEquals(resultC.size, 1)
        assertTrue(resultC.contains(linkBtoC))

        assertEquals(resultD.size, 2)
        assertTrue(resultD.contains(linkBtoD))
        assertTrue(resultD.contains(linkEtoD))

        assertEquals(resultE.size, 0)
    }



    private fun verifyLinkMap_whenAdd(
        linkAtoB: BeanDependencyLink,
        linkBtoC: BeanDependencyLink,
        linkBtoD: BeanDependencyLink,
        linkEtoD: BeanDependencyLink,
    ) {
        // [ verify link map ]
        val linkMap = getFactoryField("linkMap") as HashMap<*, *>
        assertEquals(linkMap.keys.size, 3)

        // supposed to be
        assertTrue(linkMap.containsKey("dummyBeanB"))
        assertTrue(linkMap.containsKey("dummyBeanC"))
        assertTrue(linkMap.containsKey("dummyBeanD"))

        // supposed not to be
        assertFalse(linkMap.containsKey("dummyBeanA"))
        assertFalse(linkMap.containsKey("dummyBeanE"))

        // [ verify list in link map ]
        val bLinks = HashSet(linkMap["dummyBeanB"] as List<*>)
        val cLinks = HashSet(linkMap["dummyBeanC"] as List<*>)
        val dLinks = HashSet(linkMap["dummyBeanD"] as List<*>)
        assertEquals(bLinks.size, 1)
        assertEquals(cLinks.size, 1)
        assertEquals(dLinks.size, 2)

        // supposed to be
        assertTrue(bLinks.contains(linkAtoB))
        assertTrue(cLinks.contains(linkBtoC))
        assertTrue(dLinks.contains(linkBtoD))
        assertTrue(dLinks.contains(linkEtoD))

        // supposed not to be
        assertFalse(bLinks.contains(linkBtoC))
        assertFalse(bLinks.contains(linkBtoD))
        assertFalse(bLinks.contains(linkEtoD))
        assertFalse(cLinks.contains(linkAtoB))
        assertFalse(cLinks.contains(linkEtoD))
        assertFalse(cLinks.contains(linkBtoD))
        assertFalse(dLinks.contains(linkAtoB))
        assertFalse(dLinks.contains(linkBtoC))
    }

    private fun verifyReverseLinkMap_whenAdd(
        linkAtoB: BeanDependencyLink,
        linkBtoC: BeanDependencyLink,
        linkBtoD: BeanDependencyLink,
        linkEtoD: BeanDependencyLink,
    ) {
        // [ verify link map ]
        val linkMap = getFactoryField("reverseLinkMap") as HashMap<*, *>
        assertEquals(linkMap.keys.size, 3)

        // supposed to be
        assertTrue(linkMap.containsKey("dummyBeanB"))
        assertTrue(linkMap.containsKey("dummyBeanA"))
        assertTrue(linkMap.containsKey("dummyBeanE"))

        // supposed not to be
        assertFalse(linkMap.containsKey("dummyBeanC"))
        assertFalse(linkMap.containsKey("dummyBeanD"))

        // [ verify list in link map ]
        val aLinks = HashSet(linkMap["dummyBeanA"] as List<*>)
        val bLinks = HashSet(linkMap["dummyBeanB"] as List<*>)
        val eLinks = HashSet(linkMap["dummyBeanE"] as List<*>)
        assertEquals(aLinks.size, 1)
        assertEquals(bLinks.size, 2)
        assertEquals(eLinks.size, 1)

        // supposed to be
        assertTrue(aLinks.contains(linkAtoB))
        assertTrue(bLinks.contains(linkBtoC))
        assertTrue(bLinks.contains(linkBtoD))
        assertTrue(eLinks.contains(linkEtoD))

        // supposed not to be
        assertFalse(aLinks.contains(linkBtoC))
        assertFalse(aLinks.contains(linkBtoD))
        assertFalse(aLinks.contains(linkEtoD))
        assertFalse(bLinks.contains(linkAtoB))
        assertFalse(bLinks.contains(linkEtoD))
        assertFalse(eLinks.contains(linkBtoD))
        assertFalse(eLinks.contains(linkAtoB))
        assertFalse(eLinks.contains(linkBtoC))
    }

    private fun verifyLinkList_whenAdd(
        linkAtoB: BeanDependencyLink,
        linkBtoC: BeanDependencyLink,
        linkBtoD: BeanDependencyLink,
        linkEtoD: BeanDependencyLink,
    ) {
        val linkList = HashSet(getFactoryField("linkList") as ArrayList<*>);
        assertEquals(linkList.size, 4)
        assertTrue(linkList.contains(linkAtoB))
        assertTrue(linkList.contains(linkBtoC))
        assertTrue(linkList.contains(linkBtoD))
        assertTrue(linkList.contains(linkEtoD))
    }

    private fun verifyRootNodeNameSet_whenAdd(
        linkAtoB: BeanDependencyLink,
        linkBtoC: BeanDependencyLink,
        linkBtoD: BeanDependencyLink,
        linkEtoD: BeanDependencyLink,
    ) {
        val linkNameSet = getFactoryField("rootNodeNameSet") as HashSet<*>
        assertEquals(linkNameSet.size, 2)

        // supposed to be
        assertTrue(linkNameSet.contains(linkBtoC.to.name))
        assertTrue(linkNameSet.contains(linkBtoD.to.name))
        assertTrue(linkNameSet.contains(linkEtoD.to.name))

        // supposed not to be
        assertFalse(linkNameSet.contains(linkAtoB.from.name))
        assertFalse(linkNameSet.contains(linkBtoC.from.name))
        assertFalse(linkNameSet.contains(linkBtoD.from.name))
        assertFalse(linkNameSet.contains(linkEtoD.from.name))
    }

    private fun getFactoryField(name: String): Any {
        val field = factory::class.java.getDeclaredField(name)
        field.isAccessible = true
        return field.get(factory)
    }

    private fun beanDependencyLinkOf(
        lhsBeanName: String,
        rhsBeanName: String
    ): BeanDependencyLink {
        val lhs = beanDependencyNodeOf(lhsBeanName)
        val rhs = beanDependencyNodeOf(rhsBeanName)
        return BeanDependencyLink(lhs, rhs)
    }

    private fun beanDependencyNodeOf(
        beanName: String
    ): BeanDependencyNode {
        val bean = beanFactory.getBean("dummyBeanA")
        val definition = beanFactory.getBeanDefinition("dummyBeanA")
        return BeanDependencyNode(beanName, bean, definition)
    }

}
