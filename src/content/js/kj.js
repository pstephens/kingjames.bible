/* (c) 2017 Peter Stephens - Apache 2 License */
;(function ($doc, $win) {
    var _data_png = 'data-png',
        _img = 'img',
        _hashchange = 'hashchange',
        kj = $win.kj = $win.kj || {},
        activeId = '_main';

    kj.isOperaMini = Object.prototype.toString.call($win.operamini) === "[object OperaMini]";

    function findElem(id)
    {
        return id === null ? null : $doc.getElementById(id);
    }

    function activate(id, active)
    {
        var el = $doc.getElementById(id);
        if(el) {
            var t = el.className;
            t = t.replace('active', '').trim();
            if(active) {
                t = (t + ' active').trim();
            }
            if(el.className !== t) {
                el.className = t;
            }
        }
        return !!el;
    }

    function setActiveElem(id)
    {
        var ret;
        if(id !== activeId) {
            activate(activeId, false);
            ret = activate(id, true);
            activeId = id;
        }
        else {
            ret = true;
        }
        return ret;
    }

    function setActiveElemOrMain(id)
    {
        if(!setActiveElem(id)) {
            setActiveElem('_main');
        }
    }

    function setActive()
    {
        var raw = $win.location.hash.substr(1),
            id = '_' + raw;
        setActiveElemOrMain(id);
    }

    function scrollToY(y)
    {
        if(!kj.isOperaMini && $win.scrollY !== y) {
            $win.scrollTo($win.scrollX, y);
        }
    }

    function centerElem(el)
    {
        if(el) {
            var elRect = el.getBoundingClientRect(),
                elHeight = elRect.bottom - elRect.top,
                docEl = $doc.documentElement,
                newY = $win.scrollY +
                    elRect.top -
                    (docEl.clientHeight - elHeight) / 2;
            scrollToY(newY);
        }
    }

    function doScroll()
    {
        $win.setTimeout(
            function() {
                if(kj.centeractive) {
                    centerElem(findElem(activeId));
                }
                if(kj.scrolltop) {
                    scrollToY(0);
                }
            }, 0);
    }

    function svg_polyfill()
    {
        var imgs = $doc.getElementsByTagName(_img),
            img,
            i,
            png;
        for(i = 0; i < imgs.length; i++) {
            img = imgs[i];
            if(img.hasAttribute(_data_png)) {
                png = img.getAttribute(_data_png);
                if(img.src !== png)
                    img.src = png;
            }
        }
    }

    function init() {
        setActive();
        doScroll();
    }

    function bootstrap()
    {
        init();
        if(!$win.Modernizr.svgasimg) {
            svg_polyfill();
        }

        $win.addEventListener(_hashchange, init);

        $win.domready(init);
    }

    // exports (some are for testing)
    kj.bootstrap = bootstrap;
    kj.svg_polyfill = svg_polyfill;
    kj.setActiveElem = setActiveElem;
    kj.setActiveElemOrMain = setActiveElemOrMain;

})(document, window);
