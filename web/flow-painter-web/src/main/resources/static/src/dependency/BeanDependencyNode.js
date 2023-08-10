class BeanDependencyNode {

    static nodes = new Map()

    static DEFAULT_HEIGHT = 120
    static DEFAULT_WIDTH  = 240

    static layers
    static linkLineGap = 10
    static nodeHorizontalMargin = 0
    static nodeVerticalMargin = 0

    static initLayers() {
        BeanDependencyNode.layers = [...document.getElementsByClassName("bean-dependency-layer")]
        const horizontalGaps = BeanDependencyNode.layers
                .map(x=> ([...x.getElementsByClassName("bean-dependency-node")]).length)
                .map(x=> x * BeanDependencyNode.linkLineGap + BeanDependencyNode.nodeHorizontalMargin)

        for(let i = 1; i < horizontalGaps.length; i++) {
            horizontalGaps[i] += horizontalGaps[i-1]
        }

        const createNode = (it, layerNum, idx) => {
            const x = (layerNum===0) ? 0 : horizontalGaps[layerNum-1] + (BeanDependencyNode.DEFAULT_WIDTH * layerNum)
            const y = (BeanDependencyNode.nodeVerticalMargin + BeanDependencyNode.DEFAULT_HEIGHT) * idx
            new BeanDependencyNode(it, x, y)
        }
        const createAllNodeInLayer = (layerList, layerNum) => {
            layerList.forEach((it, idx) => createNode(it, layerNum, idx))
        }

        BeanDependencyNode.layers
                .map(x=> [...x.getElementsByClassName("bean-dependency-node")])
                .forEach((layerList, layerNum) => createAllNodeInLayer(layerList, layerNum))

        const nodeKeys = ([...BeanDependencyNode.nodes.keys()])
        nodeKeys.forEach(key=> BeanDependencyNode.nodes.get(key).updateLinkLines())
    }
    static setLinkLineGap(gap) {
        BeanDependencyNode.linkLineGap = gap
    }
    static setNodeMargin(horizontalMargin, verticalMargin) {
        BeanDependencyNode.nodeHorizontalMargin = horizontalMargin
        BeanDependencyNode.nodeVerticalMargin = verticalMargin
    }

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
        this.linkDoms = [...dom.getElementsByClassName("bean-dependency-node.link.path")]

        this.leftLinkAnchor = dom.getElementsByClassName("bean-dependency-node.link-anchor.left")[0]
        this.rightLinkAnchor = dom.getElementsByClassName("bean-dependency-node.link-anchor.right")[0]

        this.init()
        BeanDependencyNode.nodes.set(dom.id, this)
    }

    init() {
        this.dom.setAttribute('transform', `translate(${this.x}, ${this.y})`)
        this.applySize();
    }

    applySize() {
        this.bodyDom.setAttribute("width", this.width)
        this.bodyDom.setAttribute("height", this.height)
    }

    updateLinkLines() {
        this.updateLinkCorners()

        const group = this.dom.getElementsByClassName("bean-dependency-node.link")[0]

        const x = this.centerInGroup.x + (this.width/2)
        const y = this.centerInGroup.y
        group.setAttribute('transform', `translate(${x}, ${y})`)
        this.leftLinkAnchor.setAttribute('transform', `translate(0, ${y})`)
        this.rightLinkAnchor.setAttribute('transform', `translate(${x}, ${y})`)

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
            let type = idx===0 ? 'M' : idx===1 ? 'C' : ','
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
            x: (target.center.x/2) - this.center.x,
            y: target.center.y - this.center.y
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
