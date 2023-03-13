class BeanDependencyNode {

    static nodes = new Map()

    static DEFAULT_HEIGHT = 120
    static DEFAULT_WIDTH  = 240

    constructor(
        dom,
        x, y,
        width= BeanDependencyNode.DEFAULT_WIDTH,
        height = BeanDependencyNode.DEFAULT_HEIGHT
    ) {
        this.x = x; this.y = y
        this.width = width; this.height= height
        this.center = {
            x: this.x + (width/2),
            y: this.y + (height/2)
        }
        this.centerInGroup = {
            x: width/2,
            y: height/2,
        }
        this.dom = dom

        this.bodyDom = dom.getElementsByClassName("bean-dependency-node.body")[0]
        this.linkDoms = [...dom.getElementsByClassName("bean-dependency-link.line")]

        this.init()
        BeanDependencyNode.nodes.set(dom.id, this)
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

    updateLines() {
        if(this.linkDoms.length === 0)
            return;

        const group = this.dom.getElementsByClassName("bean-dependency-link")[0]
        group.setAttribute(
            'transform',
            `translate(${this.centerInGroup.x}, ${this.centerInGroup.y})`
        )

        this.linkDoms.forEach(line=> {
            const targetId = line.getAttribute("value")
            if(!BeanDependencyNode.nodes.has(targetId))
                return

            const target = BeanDependencyNode.nodes.get(targetId)
            line.setAttribute("x2", target.center.x-this.center.x)
            line.setAttribute("y2", target.center.y-this.center.y)
        })
    }
}
