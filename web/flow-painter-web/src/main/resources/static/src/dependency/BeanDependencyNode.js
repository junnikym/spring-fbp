class BeanDependencyNode {

    static DEFAULT_HEIGHT = 360
    static DEFAULT_WIDTH  = 240

    constructor(
        dom,
        x, y,
        width= BeanDependencyNode.DEFAULT_WIDTH,
        height = BeanDependencyNode.DEFAULT_HEIGHT
    ) {
        this.x = x; this.y = y
        this.width = width; this.height= height

        this.dom = dom
        this.bodyDom = dom.getElementsByClassName("bean-dependency-node.body")[0]
        this.linkDoms = [...dom.getElementsByClassName("bean-dependency-link-with")]

        console.log(this.linkDoms)

        this.init()
    }

    init() {
        this.dom.setAttribute(
            'transform', `translate(${this.x}, ${this.y})`
        )
        this.applySize();
    }

    applySize() {
        this.bodyDom.setAttribute("width", this.width)
        this.bodyDom.setAttribute("height", this.height)
    }
}
