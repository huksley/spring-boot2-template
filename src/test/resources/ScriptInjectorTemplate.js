/** dynamically load some script and wait for expression to be available */
(function() {
    var scriptUrl = '#URL#';
    if (typeof window.#EXPRESSION# == 'undefined') {
        var script = document.createElement('script');
        var head = document.getElementsByTagName('head')[0];
        var done = false;
        console.log("Loading script from #URL#");
        script.onload = script.onreadystatechange = (function() {
            if (!done && (!this.readyState || this.readyState == 'loaded' || this.readyState == 'complete')) {
                done = true;
                script.onload = script.onreadystatechange = null;
                console.log("Loaded script from #URL#");
                window.__#EXPRESSION#_loaded = true;
                head.removeChild(script);
            }
        });
        script.src = scriptUrl;
        head.appendChild(script);
    } else {
        console.log("window.#EXPRESSION# already defined");
    }
})();
