/* (c) 2017 Peter Stephens - Apache 2 License */
(function ($doc, $win) {
    var _data_png = 'data-png',
        _img = 'img',
        _hashchange = 'hashchange',
        kj = $win.kj = $win.kj || {},
        isOperaMini = Object.prototype.toString.call($win.operamini) === "[object OperaMini]",
        activeId = '_main';

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

    function setActive()
    {
        var raw = $win.location.hash.substr(1),
            id = '_' + raw;
        if(!setActiveElem(id)) {
            setActiveElem('_main');
        }
    }

    function scrollToY(y)
    {
        if(!isOperaMini && $win.scrollY !== y) {
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

    function kj_svg_polyfill()
    {
        if(!$win.Modernizr.svgasimage) {
            var imgs = $doc.getElementsByTagName(_img),
                img,
                i;
            for(i = 0; i < imgs.length; i++) {
                img = imgs[i];
                if(img.hasAttribute(_data_png))
                    img.src = img.getAttribute(_data_png);
            }
        }
    }

    function bootstrap1() {
        setActive();
        doScroll();
    }

    function kj_bootstrap()
    {
        bootstrap1();
        kj_svg_polyfill();

        $win.addEventListener(_hashchange, bootstrap1);

        $win.domready(bootstrap1);
    }

    // exports (some are for testing)
    kj.bootstrap = kj_bootstrap;
    kj.svg_polyfill = kj_svg_polyfill;

})(document, window);
