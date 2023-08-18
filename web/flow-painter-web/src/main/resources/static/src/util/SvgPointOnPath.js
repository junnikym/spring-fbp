class SvgPointOnPath {

    constructor(svg, line, isHide, isDisposable) {
        this.line = line
        this.point = document.createElementNS("http://www.w3.org/2000/svg","circle");

        const startOfLine = line.getTransformToElement(this.point)
        this.point.setAttributeNS(null,"cx",startOfLine.e)
        this.point.setAttributeNS(null,"cy",startOfLine.f)
        this.isDisposable = isDisposable
        this.isHide = isHide
        if(isHide)
            this.hide()

        svg.appendChild(this.point)

        this.tZero = Date.now();
    }

    remove() {
        this.point.remove()
        delete this
    }

    show() {
        this.point.style.display = "block";
        this.isHide = false
    }

    hide() {
        this.point.style.display = "none";
        this.isHide = true
    }

    run(duration=750) {
        if(this.isHide)
            this.point.style.display = "block";

        this.duration = duration;
        requestAnimationFrame(() => this.#play())
    }

    #play() {
        let u = Math.min((Date.now() - this.tZero) / this.duration, 1);
        if(u < 1) {
            requestAnimationFrame(() => this.#play());
        }
        else {
            if(this.isHide)
                this.point.style.display = "none";

            if(this.isDisposable) {
                this.remove()
            }
        }

        this.move(u)
    }

    move(u) {
        const p = this.line.getPointAtLength(u * this.line.getTotalLength());
        this.point.setAttribute("transform", `translate(${p.x}, ${p.y})`);
    }
}