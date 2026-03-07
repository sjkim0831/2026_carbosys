class CustomViewer extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });

        this.shadowRoot.innerHTML = `
            <style>
                :host {
                    display: block;
                    width: 100%;
                    height: auto;
                }
                iframe {
                    width: 100%;
                    border: none;
                    display: block;
                    overflow: hidden;
                }
            </style>
            <iframe></iframe>
        `;

        this.iframe = this.shadowRoot.querySelector("iframe");
        this.observer = null;
        this.resizeObserver = null;
        this.loadTimeout = null;
        this.lastSrc = "";

        this.iframe.addEventListener("error", () => {
            this.renderFallback("페이지를 불러오지 못했습니다.", this.lastSrc);
        });

        this.iframe.addEventListener("load", () => {
            if (this.loadTimeout) {
                clearTimeout(this.loadTimeout);
                this.loadTimeout = null;
            }
            this.adjustHeight();
            this.setupMutationObserver();
            this.setupResizeObserver();
        });

        window.addEventListener("resize", () => this.adjustHeight());
    }

    static get observedAttributes() {
        return ["src"];
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name === "src" && newValue) {
            this.lastSrc = newValue;
            this.iframe.src = newValue;
            if (this.loadTimeout) {
                clearTimeout(this.loadTimeout);
            }
            this.loadTimeout = setTimeout(() => {
                this.renderFallback("페이지 응답 시간이 초과되었습니다.", newValue);
            }, 10000);
        }
    }

    renderFallback(message, path) {
        if (this.loadTimeout) {
            clearTimeout(this.loadTimeout);
            this.loadTimeout = null;
        }
        this.iframe.srcdoc = `
            <html lang="ko">
                <head>
                    <meta charset="UTF-8" />
                    <style>
                        body { margin:0; font-family: Arial, sans-serif; background:#f8fafc; color:#111827; }
                        .wrap { padding: 20px; }
                        .card { background:#fff; border:1px solid #e5e7eb; border-radius:10px; padding:16px; }
                        .title { margin:0 0 8px; color:#991b1b; font-size:18px; font-weight:700; }
                        .text { margin:0 0 8px; font-size:14px; }
                        .meta { margin:0; font-size:12px; color:#4b5563; word-break:break-all; }
                    </style>
                </head>
                <body>
                    <div class="wrap">
                        <div class="card">
                            <p class="title">페이지 오류</p>
                            <p class="text">${message}</p>
                            <p class="meta">path: ${path || "-"}</p>
                        </div>
                    </div>
                </body>
            </html>`;
        this.adjustHeight();
    }

    setupMutationObserver() {
        try {
            const iframeDoc = this.iframe.contentDocument || this.iframe.contentWindow.document;
            if (!iframeDoc) return;

            if (this.observer) {
                this.observer.disconnect();
            }

            const targetNode = iframeDoc.body;
            if (!targetNode) return;

            this.observer = new MutationObserver(() => this.adjustHeight());

            this.observer.observe(targetNode, {
                childList: true,
                subtree: true,
                attributes: true,
                characterData: true
            });

            this.adjustHeight();
        } catch (error) {
            console.warn("Cannot setup MutationObserver:", error);
        }
    }

    setupResizeObserver() {
        try {
            const iframeDoc = this.iframe.contentDocument || this.iframe.contentWindow.document;
            const container = iframeDoc.querySelector("#container");

            if (!container || typeof ResizeObserver === "undefined") return;

            this.resizeObserver?.disconnect?.();

            this.resizeObserver = new ResizeObserver(() => {
                this.adjustHeight();
            });

            this.resizeObserver.observe(container);
        } catch (error) {
            console.warn("Cannot setup ResizeObserver:", error);
        }
    }

    adjustHeight() {
        try {
            const iframeDoc = this.iframe.contentDocument || this.iframe.contentWindow.document;
            if (!iframeDoc) return;

            setTimeout(() => {
                const container = iframeDoc.querySelector("#container");
                const contHeight = container?.scrollHeight || iframeDoc.body.scrollHeight || 0;
                const buffer = 2;
                const finalHeight = contHeight + buffer;

                const mainContainer = document.querySelector('.main-container');
                if (mainContainer) {
                    mainContainer.style.height = finalHeight + "px";
                }

                this.style.height = finalHeight + "px";
                this.iframe.style.height = finalHeight + "px";
            }, 150);
        } catch (error) {
            console.warn("Cannot adjust iframe height:", error);
        }
    }
}

customElements.define("custom-viewer", CustomViewer);
