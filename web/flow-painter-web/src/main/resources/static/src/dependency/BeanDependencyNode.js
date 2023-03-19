class BeanDependencyNode {

    static nodes = new Map()

    static DEFAULT_HEIGHT = 120
    static DEFAULT_WIDTH  = 240

    constructor(
        dom,
        x, y,
        width= BeanDependencyNode.DEFAULT_WIDTH,
        height = BeanDependencyNode.DEFAULT_HEIGHT,
        borderRadius = 0,
    ) {
        this.x = x; this.y = y
        this.width = width; this.height= height
        this.borderRadius = borderRadius;

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
        this.linkDoms = [...dom.getElementsByClassName("bean-dependency-link.path")]

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

    updateLinkLines() {
        this.updateLinkCorners()

        const group = this.dom.getElementsByClassName("bean-dependency-link")[0]
        group.setAttribute(
            'transform',
            `translate(${this.centerInGroup.x}, ${this.centerInGroup.y})`
        )

        this.linkDoms.forEach(line=> {
            const targetId = line.getAttribute("value")
            if(!this.linkCorners.has(targetId))
                return

            const pathAttr = this.getLinkLinePath(targetId);
            line.setAttribute("d", pathAttr)
        })
    }

    getLinkLinePath(targetId) {
        let result = ''
        this.linkCorners.get(targetId).forEach((it, idx)=> {
            let type = idx===0 ? 'M' : 'L'
            result += `${type} ${it.x} ${it.y} `
        })

        return result;
    }

    updateLinkCorners() {
        this.linkCorners = new Map()
        this.linkDoms.forEach(line=> {
            const targetId = line.getAttribute("value")
            if(!BeanDependencyNode.nodes.has(targetId))
                return

            const corners = this.getLinkLineCorners(targetId)
            this.linkCorners.set(targetId, corners)
        })
    }

    getLinkLineCorners(targetId) {
        const target = BeanDependencyNode.nodes.get(targetId)
        const from = { x: 0, y: 0 }
        const to = {
            x: target.center.x-this.center.x,
            y: target.center.y-this.center.y
        }

        const topConner = {
            x: to.x / 2,
            y: 0
        }
        const bottomConner = {
            x: to.x / 2,
            y: to.y
        }

        return [from, topConner, bottomConner, to];
    }
}
